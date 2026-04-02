/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.ws.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.*;
import org.jevis.commons.ws.sql.tables.*;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import tech.units.indriya.AbstractUnit;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Per-request data source that provides a unified facade over all SQL table
 * operations. A new instance is created for each HTTP request; the three
 * in-memory lists ({@code allRelationships}, {@code allObjects},
 * {@code allDeletedObjects}) act as a request-scoped cache that avoids
 * repeated database round-trips within a single request.
 *
 * <p>Thread safety: instances are NOT shared across threads. The instance-level
 * lists are plain {@link ArrayList} and must not be accessed concurrently.
 *
 * @author fs
 */
public class SQLDataSource {

    private static final Logger logger = LogManager.getLogger(SQLDataSource.class);

    /**
     * Shared, thread-safe Jackson mapper. {@link ObjectMapper} is thread-safe
     * after configuration; creating one per request is wasteful.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Connection dbConn;
    private JEVisUserSQL user;
    private LoginTable lTable;
    private ObjectTable oTable;
    private AttributeTable aTable;
    private SampleTable sTable;
    private RelationshipTable rTable;
    /**
     * Request-scoped cache of all relationships (loaded lazily or via preload).
     */
    private List<JsonRelationship> allRelationships = new ArrayList<>();
    /**
     * Request-scoped cache of all non-deleted objects.
     */
    private List<JsonObject> allObjects = new ArrayList<>();
    /**
     * Request-scoped cache of deleted objects.
     */
    private List<JsonObject> allDeletedObjects = new ArrayList<>();
    private UserRightManagerForWS um;

    /**
     * Creates a new data source and authenticates the caller using the
     * {@code Authorization} or {@code session} HTTP header.
     *
     * @param httpHeaders incoming request headers (used for auth)
     * @param request     the JAX-RS request context
     * @param url         the request URI info
     * @throws AuthenticationException if authentication fails
     * @throws JEVisException          if the database connection cannot be established
     */
    public SQLDataSource(HttpHeaders httpHeaders, Request request, UriInfo url) throws AuthenticationException, JEVisException {
        this(httpHeaders, request, url, true);
    }

    /**
     * Creates a new data source, optionally skipping authentication.
     *
     * @param httpHeaders incoming request headers
     * @param request     the JAX-RS request context
     * @param url         the request URI info
     * @param needLogin   {@code true} to enforce authentication; {@code false}
     *                    to skip it (e.g., for public endpoints)
     * @throws AuthenticationException if {@code needLogin} is true and auth fails
     * @throws JEVisException          if the database connection cannot be established
     */
    public SQLDataSource(HttpHeaders httpHeaders, Request request, UriInfo url, boolean needLogin) throws AuthenticationException, JEVisException {

        try {
            ConnectionFactory.getInstance().registerMySQLDriver(Config.getDBHost(), Config.getDBPort(), Config.getSchema(), Config.getDBUser(), Config.getDBPW(), Config.getConnectionOptions());

            this.dbConn = ConnectionFactory.getInstance().getConnection();

            if (this.dbConn.isValid(2000)) {

                this.lTable = new LoginTable(this);
                this.rTable = new RelationshipTable(this);
                if (needLogin) jevisLogin(httpHeaders);
                this.oTable = new ObjectTable(this);
                this.aTable = new AttributeTable(this);
                this.sTable = new SampleTable(this);
                this.um = new UserRightManagerForWS(this, true);


            }
        } catch (SQLException se) {
            logger.error(se);
            throw new JEVisException("Database connection error", 5438, se);
        }
    }

    /**
     * Creates a data source wrapping an already-open connection, without
     * performing any authentication. Intended for internal/test use.
     *
     * @param dbConn an open JDBC connection
     * @throws AuthenticationException never thrown, declared for signature compatibility
     * @throws JEVisException          never thrown, declared for signature compatibility
     */
    public SQLDataSource(Connection dbConn) throws AuthenticationException, JEVisException {
        this.dbConn = dbConn;
        this.lTable = new LoginTable(this);
        this.oTable = new ObjectTable(this);
        this.aTable = new AttributeTable(this);
        this.sTable = new SampleTable(this);
        this.rTable = new RelationshipTable(this);
    }

    /**
     * Returns the underlying JDBC connection held by this data source.
     *
     * @return the active JDBC connection
     * @throws SQLException if the connection is not available
     */
    public Connection getConnection() throws SQLException {
        return this.dbConn;
    }

    /**
     * Logs a user action for audit purposes. Sys-admin actions are suppressed
     * to avoid excessive log volume.
     *
     * @param event the type of action performed
     * @param msg   a human-readable description of the action
     */
    public void logUserAction(LOG_EVENT event, String msg) {
        if (getCurrentUser().isSysAdmin()) {
            /** we do not log SysAdmin because of the huge amount of event for the services. The Logging is for user events. **/
            return;
        }
        try {
            logger.debug("Event '{}'| {}: {} ", getCurrentUser().getAccountName(), event, msg);
            JsonSample newSample = new JsonSample();
            newSample.setTs(JsonFactory.sampleDTF.print(DateTime.now()));
            newSample.setValue(String.format("%s|%s|%s", user.getAccountName(), event, msg));
            getSampleTable().insertSamples(getCurrentUser().getUserID(), "Activities", JEVisConstants.PrimitiveType.STRING, Collections.singletonList(newSample));
        } catch (Exception ex) {
            logger.error("Error while logging Event: {}:{}:{}", event, msg, ex);
        }
    }

    /**
     * Deletes user activity log entries older than one year, in compliance with
     * General Data Protection Regulations (GDPR).
     *
     * @throws SQLException            if a database error occurs
     * @throws AuthenticationException if the caller is not authorised
     * @throws JEVisException          if a JEVis-level error occurs
     */
    public void gdprCleanUp() throws SQLException, AuthenticationException, JEVisException {
        logger.error("Starting user log cleanup for Data Protection");
        getSampleTable().deleteOldLogging();
    }

    /**
     * Returns all class relationships from the class definition cache.
     *
     * @return a list of all {@link JsonClassRelationship} objects
     */
    public List<JsonClassRelationship> getClassRelationships() {
        List<JsonJEVisClass> list = new ArrayList<>(Config.getClassCache().values());
        List<JsonClassRelationship> tempjcr = new ArrayList<>();
        for (JsonJEVisClass jjc : list) {
            if (jjc.getRelationships() != null) tempjcr.addAll(jjc.getRelationships());
        }
        return tempjcr;
    }

    /**
     * Returns the class relationships defined for the given class name.
     *
     * @param className the name of the JEVis class
     * @return a list of {@link JsonClassRelationship} for that class; empty if none
     */
    public List<JsonClassRelationship> getClassRelationships(String className) {
        List<JsonJEVisClass> list = new ArrayList<>(Config.getClassCache().values());
        List<JsonClassRelationship> tempjcr = new ArrayList<>();
        for (JsonJEVisClass jjc : list) {
            if ((jjc.getRelationships() != null) && (jjc.getName().equals(className)))
                tempjcr.addAll(jjc.getRelationships());
        }
        return tempjcr;
    }

    /**
     * Returns the attribute type definitions for the given class.
     *
     * @param jevisClass the class whose types are requested
     * @return a list of {@link JsonType} or {@code null} if the class is not found
     */
    public List<JsonType> getTypes(JsonJEVisClass jevisClass) {
        List<JsonJEVisClass> list = new ArrayList<>(Config.getClassCache().values());
        if (list.contains(jevisClass)) {
            return list.get(list.indexOf(jevisClass)).getTypes();
        } else return null;
    }

    /**
     * Retrieves the latest file sample for the given object and attribute.
     *
     * @param ds        this data source
     * @param id        the object ID
     * @param attribute the attribute name
     * @return the {@link JEVisFile} or {@code null} if no file sample exists
     * @throws JEVisException if the database query fails
     * @throws IOException    if the file cannot be read from disk
     */
    public JEVisFile getFile(SQLDataSource ds, long id, String attribute) throws JEVisException, IOException {
        DateTime ts = null;
        JEVisFile jFile = new JEVisFileImp();
        List<JsonSample> samples = ds.getSamples(id, attribute, ts, ts, 1);
        if (!samples.isEmpty()) {
            JsonSample sample = samples.get(0);

            DateTime dbTS = JsonFactory.sampleDTF.parseDateTime(samples.get(0).getTs());

            //Pattern  /path/to/filedir/yyyyMMdd/ID_HHmmss_filename
            String fileName = createFilePattern(id, attribute, sample.getValue(), dbTS);
            File file = new File(fileName);
            if (file.exists() && file.canRead()) {
                jFile.setFilename(file.getName());
                InputStream is = new FileInputStream(file.getName());
                jFile.setBytes(IOUtils.toByteArray(is));

                return jFile;
            }
        }
        return null;
    }

    private String createFilePattern(long id, String attribute, String fileName, DateTime dateTime) {
        return Config.getFileDir().getAbsolutePath()
                + File.separator + id
                + File.separator + attribute
                + File.separator + DateTimeFormat.forPattern("yyyyMMddHHmmss").withZoneUTC().print(dateTime)
                + "_" + fileName;
    }

    /**
     * Returns all JEVis class definitions from the class definition cache.
     *
     * @return a new list containing all cached {@link JsonJEVisClass} instances
     */
    public List<JsonJEVisClass> getJEVisClasses() {
        return new ArrayList<>(Config.getClassCache().values());
    }

    /**
     * Returns the user rights manager for the currently authenticated user.
     *
     * @return the {@link UserRightManagerForWS} for this request
     */
    public UserRightManagerForWS getUserManager() {
        return this.um;
    }

    /**
     * Pre-loads a specific category of data into the request-scoped cache.
     * Subsequent calls to the corresponding getter will be served from the
     * cache instead of issuing new database queries.
     *
     * @param preload the category to load ({@code ALL_REL}, {@code ALL_OBJECT},
     *                or {@code ALL_CLASSES})
     */
    public void preload(PRELOAD preload) {
        logger.debug("prelaod {}", preload.toString());
        try {
            switch (preload) {
                case ALL_REL:
                    this.allRelationships = getRelationshipTable().getAll();
                    break;
                case ALL_CLASSES:
                    Config.getClassCache();
//                    allTypes = getTypeTable().//todo
                    break;
                case ALL_OBJECT:
                    this.allObjects = getObjectTable().getAllObjects();
                    break;
            }
        } catch (Exception sx) {
            logger.error("Error while reloading", sx);
        }
    }

    /**
     * Returns the relationship table accessor.
     *
     * @return the {@link RelationshipTable}
     */
    public RelationshipTable getRelationshipTable() {
        return this.rTable;
    }

    /**
     * Returns the object table accessor, creating it lazily if needed.
     *
     * @return the {@link ObjectTable}
     */
    public ObjectTable getObjectTable() {
        if (oTable == null) {
            this.oTable = new ObjectTable(this);
        }
        return this.oTable;
    }

    /**
     * Returns the login table accessor.
     *
     * @return the {@link LoginTable}
     */
    public LoginTable getLoginTable() {
        return this.lTable;
    }

    /**
     * Returns the sample table accessor.
     *
     * @return the {@link SampleTable}
     */
    public SampleTable getSampleTable() {
        return this.sTable;
    }

    /**
     * Returns the attribute table accessor.
     *
     * @return the {@link AttributeTable}
     */
    public AttributeTable getAttributeTable() {
        return this.aTable;
    }

    /**
     * Filters a list of objects by JEVis class, optionally including subclasses.
     *
     * @param objects the full object list to filter
     * @param jClass  the class name to filter by
     * @param inherit if {@code true}, objects of subclasses are also included
     * @return a new list containing only the matching objects
     */
    public List<JsonObject> filterObjectByClass(List<JsonObject> objects, String jClass, boolean inherit) {
        List<JsonObject> filtered = new ArrayList<>();
        List<String> classList = new ArrayList<>();
        classList.add(jClass);

        if (inherit) {
            JEVisClassHelper.findHeir(jClass, classList);
        }

        for (JsonObject obj : objects) {
            if (classList.contains(obj.getJevisClass())) {
                filtered.add(obj);
            }
        }

        return filtered;
    }

    /**
     * Sets the currently authenticated user for this data source.
     *
     * @param user the authenticated user
     */
    public void setUser(JEVisUserSQL user) {
        this.user = user;
    }

    private void jevisLogin(HttpHeaders httpHeaders) throws AuthenticationException {


        boolean isSession = httpHeaders.getRequestHeader("session") != null && !httpHeaders.getRequestHeader("session").isEmpty();
        boolean isAuth = httpHeaders.getRequestHeader("authorization") != null && !httpHeaders.getRequestHeader("authorization").isEmpty();

        if (isSession) {
            String session = httpHeaders.getRequestHeader("session").get(0);
            logger.debug("Login with Session: {}  ", session);
            try {
                CachedAccessControl cac = CachedAccessControl.getInstance(this, true);
                Session cachSession = cac.getSessions().getIfPresent(session);

                if (cachSession != null) {
                    System.out.println("Session Found: " + cachSession);
                    this.user = cachSession.getJevisUser();
                    return;
                } else {
                    throw new AuthenticationException("Invalid Session.");
                }
            } catch (Exception ex) {
                logger.error(ex, ex);
                throw new AuthenticationException("Session is not correct.");
            }
        }


        if (!isAuth) {
            throw new AuthenticationException("Authorization header is missing");
        }


        String auth = httpHeaders.getRequestHeader("authorization").get(0);
        if (auth != null && !auth.isEmpty()) {

            auth = auth.replaceFirst("[Bb]asic ", "");
            byte[] decoded = Base64.getDecoder().decode(auth);
            try {
                String decodeS = (new String(decoded, StandardCharsets.UTF_8));
                String[] dauth = decodeS.split(":");
                if (dauth.length == 2) {

                    String username = dauth[0];
                    String password = dauth[1];
                    try {
                        logger.debug("User: {}  PW: {}", username, password);
                        CachedAccessControl fastUserManager = CachedAccessControl.getInstance(this, false);
                        this.user = fastUserManager.getUser(username);

                        logger.debug("FastUserManager PW Check: {} User: {}", fastUserManager.validLogin(username, password), this.user);
                        if (!fastUserManager.validLogin(username, password)) {
                            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
                        }

                    } catch (Exception ex) {
                        logger.error(ex, ex);
                        throw new AuthenticationException("Username/Password is not correct.");
                    }
                } else {
                    throw ErrorBuilder.ErrorBuilder(Response.Status.BAD_REQUEST.getStatusCode(), 2002, "The HTML authorization header is not correct format");
                }
            } catch (NullPointerException nex) {
                throw new AuthenticationException("Username/Password is not correct.");
            }
        } else {
            throw new AuthenticationException("Authorization header is missing");
        }


    }

    /**
     * Builds a new in-memory {@link JEVisRelationship}. This operation is not
     * yet supported and always throws {@link UnsupportedOperationException}.
     *
     * @param fromObject the source object ID
     * @param toObject   the target object ID
     * @param type       the relationship type constant
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    public JEVisRelationship buildRelationship(Long fromObject, Long toObject, int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns all root objects accessible to the current user.
     *
     * @return a list of root {@link JsonObject} instances
     */
    public List<JsonObject> getRootObjects() {
        return getUserManager().getRoots();
    }

    /**
     * Returns all objects whose JEVis class matches the given name, optionally
     * including objects of subclasses.
     *
     * <p>Uses a {@link HashSet} for O(1) class-name lookup, avoiding the O(n²)
     * cost of calling {@code List.contains()} inside a stream.
     *
     * @param jevisClass the class name to filter by
     * @param addheirs   if {@code true}, subclasses are included
     * @return a filtered list of matching objects
     * @throws JEVisException if the object list cannot be loaded
     */
    public List<JsonObject> getObjects(String jevisClass, boolean addheirs) throws JEVisException {
        List<String> allHeir = new ArrayList<>();
        allHeir.add(jevisClass);
        if (addheirs) {
            JEVisClassHelper.findHeir(jevisClass, allHeir);
        }
        Set<String> allHeirSet = new HashSet<>(allHeir);
        return getObjects().stream()
                .filter(ob -> allHeirSet.contains(ob.getJevisClass()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a single object by ID, optionally populating its direct children.
     * The result is added to the request-scoped object cache.
     *
     * @param id       the object ID to look up
     * @param children if {@code true}, the object's child list is populated
     * @return the matching {@link JsonObject}, or {@code null} if not found
     * @throws JEVisException if a database error occurs
     */
    public JsonObject getObject(Long id, boolean children) throws JEVisException {
        logger.debug("getObject: {}", id);
        if (!this.allObjects.isEmpty()) {
            for (JsonObject ob : this.allObjects) {
                if (id.equals(ob.getId())) {
                    logger.debug("getObject- cache");
                    return ob;
                }
            }
        }
        logger.debug("getObject- NON cache");
        JsonObject ob = this.oTable.getObject(id);
        if (ob != null) {
            this.allObjects.add(ob);

            if (children) {
                ob.setObjects(new ArrayList<>());
                getRelationshipTable().getAllForObject(ob.getId(), JEVisConstants.ObjectRelationship.PARENT).forEach(rel -> {
                    if (rel.getTo() == ob.getId() && rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
                        try {
                            JsonObject child = getObject(rel.getFrom(), false);
                            if (getUserManager().canRead(child)) {
                                ob.getObjects().add(child);
                            }

                        } catch (Exception ex) {
                            logger.error("Could not add {} to object {}", rel.getFrom(), ob.toString());
                        }
                    }
                });

            }
        }


        return ob;

    }

    /**
     * Returns a single object by ID without populating its children.
     * The result is added to the request-scoped object cache.
     *
     * @param id the object ID to look up
     * @return the matching {@link JsonObject}, or {@code null} if not found
     * @throws JEVisException if a database error occurs
     */
    public JsonObject getObject(Long id) throws JEVisException {
        logger.debug("getObject: {}", id);
        if (!this.allObjects.isEmpty()) {
            for (JsonObject ob : this.allObjects) {
                if (id.equals(ob.getId())) {
                    logger.debug("getObject- cache");
                    return ob;
                }
            }
        }
        logger.debug("getObject- NON cache");
        JsonObject ob = this.getObjectTable().getObject(id);
        if (ob != null) {
            this.allObjects.add(ob);
        }
        return ob;

    }

    /**
     * Attaches relationships to their corresponding objects, setting parent
     * IDs for PARENT-type relationships.
     *
     * @param objs the objects to annotate
     * @param rels the full relationship list to consult
     */
    public void addRelationshipsToObjects(List<JsonObject> objs, List<JsonRelationship> rels) {
        for (JsonObject ob : objs) {
            for (JsonRelationship rel : rels) {
                try {
                    if (rel.getFrom() == ob.getId() || rel.getTo() == ob.getId()) {
                        if (ob.getRelationships() == null) {
                            ob.setRelationships(new ArrayList<JsonRelationship>());
                        }
                        ob.getRelationships().add(rel);
                        if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT && rel.getFrom() == ob.getId()) {
                            //child -> parent
                            ob.setParent(rel.getTo());
                        }

                    }
                } catch (Exception ex) {
                    logger.error("Error with relationship: {}", rel);
                }
            }
        }
    }

    /**
     * Updates an object's name, public flag, and i18n metadata in the database.
     *
     * @param id       the object ID to update
     * @param newname  the new display name
     * @param ispublic whether the object should be publicly visible
     * @param i18n     i18n JSON string, may be {@code null}
     * @return the updated {@link JsonObject}
     * @throws JEVisException if the update fails
     */
    public JsonObject updateObject(long id, String newname, boolean ispublic, String i18n) throws JEVisException {
        logger.debug("updateObject");
        return getObjectTable().updateObject(id, newname, ispublic, i18n);
    }

    /**
     * Returns all non-deleted objects, using the request-scoped cache.
     * On the first call the full object list is loaded from the database.
     *
     * @return the (possibly cached) list of all objects
     * @throws JEVisException if the database query fails
     */
    public List<JsonObject> getObjects() throws JEVisException {
        logger.debug("getObjects");
        if (!this.allObjects.isEmpty()) {
            logger.debug("Cache");
            return this.allObjects;
        }

        this.allObjects = this.oTable.getAllObjects();
        logger.debug("NONE-Cache");
        return this.allObjects;
    }

    /**
     * Returns all deleted objects, using the request-scoped cache.
     *
     * @return the (possibly cached) list of deleted objects
     * @throws JEVisException if the database query fails
     */
    public List<JsonObject> getDeletedObjects() throws JEVisException {
        logger.debug("getDeletedObjects");
        if (!this.allDeletedObjects.isEmpty()) {
            logger.debug("Cache");
            return this.allDeletedObjects;
        }

        this.allDeletedObjects = this.oTable.getAllDeletedObjects();
        logger.debug("NONE-Cache");
        return this.allDeletedObjects;
    }

    /**
     * Persists a list of relationships to the database, skipping any that fail.
     *
     * @param rels the relationships to create
     * @return the successfully created relationships
     */
    public List<JsonRelationship> setRelationships(List<JsonRelationship> rels) {
        List<JsonRelationship> newRels = new ArrayList<>();
        for (JsonRelationship rel : rels) {
            try {
                newRels.add(setRelationships(rel));
            } catch (Exception ex) {
                logger.error("could not add relationship {} to nothing", rel.toString());
            }
        }
        return newRels;
    }

    /**
     * Persists a single relationship to the database.
     *
     * @param rel the relationship to create
     * @return the persisted {@link JsonRelationship}
     * @throws JEVisException if the insert fails
     */
    public JsonRelationship setRelationships(JsonRelationship rel) throws JEVisException {
        return getRelationshipTable().insert(rel.getFrom(), rel.getTo(), rel.getType());
    }

    /**
     * Returns the class definition for the given name from the class cache.
     *
     * @param name the class name
     * @return the {@link JsonJEVisClass}, or {@code null} if not found
     */
    public JsonJEVisClass getJEVisClass(String name) {
        return Config.getClassCache().get(name);
    }

    /**
     * Returns the currently authenticated user. If no user is set, returns
     * a synthetic "Unknown" user with ID {@code -1}.
     *
     * @return the current {@link JEVisUserSQL}; never {@code null}
     */
    public JEVisUserSQL getCurrentUser() {
        if (this.user == null) {
            return new JEVisUserSQL(this, "Unkown", -1L, false, false);
        }
        return this.user;
    }

    /**
     * Retrieves samples for the given object/attribute combination within an
     * optional time range.
     *
     * @param obj       the object ID
     * @param attribute the attribute name
     * @param from      the inclusive start timestamp, or {@code null} for no lower bound
     * @param until     the inclusive end timestamp, or {@code null} for no upper bound
     * @param limit     the maximum number of samples to return
     * @return a list of matching {@link JsonSample} instances
     * @throws JEVisException if the query fails
     */
    public List<JsonSample> getSamples(long obj, String attribute, DateTime from, DateTime until, long limit) throws JEVisException {
        return getSampleTable().getSamples(obj, attribute, from, until, limit);
    }

    /**
     * Inserts or updates samples for the given object/attribute.
     *
     * @param obj           the object ID
     * @param attribute     the attribute name
     * @param primitiveType the JEVis primitive type constant
     * @param samples       the samples to persist
     * @return the number of rows affected
     * @throws JEVisException if the insert fails
     */
    public int setSamples(long obj, String attribute, int primitiveType, List<JsonSample> samples) throws JEVisException {
        return getSampleTable().insertSamples(obj, attribute, primitiveType, samples);
    }

    /**
     * Returns the most recent sample for the given object and attribute.
     *
     * @param obj       the object ID
     * @param attribute the attribute name
     * @return the latest {@link JsonSample}, or {@code null} if none exists
     * @throws JEVisException if the query fails
     */
    public JsonSample getLastSample(long obj, String attribute) throws JEVisException {
        return getSampleTable().getLatest(obj, attribute);
    }

    /**
     * Returns all relationships that involve the given object (either as source
     * or target), using the request-scoped cache when available.
     *
     * <p>Uses a stream {@code filter} to avoid the O(n²) cost of calling
     * {@code List.contains()} inside the former parallel loop.
     *
     * @param object the object ID
     * @return a list of relationships involving that object
     */
    public List<JsonRelationship> getParentRelationships(long object) {

        if (!this.allRelationships.isEmpty()) {
            return this.allRelationships.stream()
                    .filter(rel -> rel.getTo() == object || rel.getFrom() == object)
                    .collect(Collectors.toList());
        }

        return getRelationshipTable().getParentObject(object);

    }

    /**
     * Returns all ownership relationships that involve the given object,
     * using the request-scoped cache when available.
     *
     * @param object the object ID
     * @return a list of ownership-related relationships
     */
    public List<JsonRelationship> getGroupOwnerRelationships(long object) {

        if (!this.allRelationships.isEmpty()) {
            return this.allRelationships.stream()
                    .filter(rel -> rel.getTo() == object || rel.getFrom() == object)
                    .collect(Collectors.toList());
        }

        return getRelationshipTable().getGroupOwnerObject(object);

    }

    /**
     * Returns all relationships of a specific type that involve the given object,
     * using the request-scoped cache when available.
     *
     * @param object the object ID
     * @param type   the relationship type constant (see {@link JEVisConstants.ObjectRelationship})
     * @return a filtered list of relationships
     */
    public List<JsonRelationship> getRelationships(long object, int type) {
        if (!this.allRelationships.isEmpty()) {
            return this.allRelationships.stream()
                    .filter(rel -> (rel.getTo() == object || rel.getFrom() == object) && rel.getType() == type)
                    .collect(Collectors.toList());
        }
        return getRelationshipTable().getAllForObject(object, type);
    }

    /**
     * Returns all relationships that involve the given object (either source or
     * target), using the request-scoped cache when available.
     *
     * @param object the object ID
     * @return a list of all relationships for that object
     */
    public List<JsonRelationship> getRelationships(long object) {
        if (!this.allRelationships.isEmpty()) {
            return this.allRelationships.stream()
                    .filter(rel -> rel.getTo() == object || rel.getFrom() == object)
                    .collect(Collectors.toList());
        }
        return getRelationshipTable().getAllForObject(object);
    }

    /**
     * Returns all relationships of a specific type, filtering from the full
     * cached relationship list.
     *
     * @param type the relationship type constant
     * @return a list of relationships matching the type
     */
    public List<JsonRelationship> getRelationships(int type) {
        return getRelationships().stream()
                .filter(rel -> rel.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Returns all relationships, using the request-scoped cache.
     * On the first call the full relationship list is loaded from the database.
     *
     * @return the (possibly cached) list of all relationships
     */
    public List<JsonRelationship> getRelationships() {
        logger.debug("getRelationships");
        if (!this.allRelationships.isEmpty()) {
            logger.debug("getRelationships - cache");
            return this.allRelationships;
        }

        this.allRelationships = this.rTable.getAll();
        logger.debug("getRelationships - None cache");
        return this.allRelationships;

    }

    /**
     * Closes the underlying JDBC connection.
     *
     * @return {@code true} if the connection was closed successfully
     * @throws JEVisException if a SQL error occurs while closing
     */
    public boolean disconnect() throws JEVisException {
        if (this.dbConn != null) {
            try {
                this.dbConn.close();
                return true;
            } catch (SQLException se) {
                throw new JEVisException("Error while closing DB connection", 6492, se);
            }
        }
        return false;
    }

    /**
     * Deletes all samples for the given object and attribute.
     *
     * @param object    the object ID
     * @param attribute the attribute name
     * @return {@code true} if the deletion succeeded
     */
    public boolean deleteAllSample(long object, String attribute) {
        return getSampleTable().deleteAllSamples(object, attribute);
    }

    /**
     * Deletes samples for the given object/attribute within the specified time range.
     *
     * @param object     the object ID
     * @param attribute  the attribute name
     * @param startDate  inclusive start of the deletion window
     * @param endDate    inclusive end of the deletion window
     * @return {@code true} if the deletion succeeded
     */
    public boolean deleteSamplesBetween(long object, String attribute, DateTime startDate, DateTime endDate) {
        return getSampleTable().deleteSamples(object, attribute, startDate, endDate);
    }

    /**
     * Returns all attributes that the current user may read, across all objects.
     * Permission is evaluated using the {@link UserRightManagerForWS}.
     *
     * @return a list of readable {@link JsonAttribute} instances
     */
    public List<JsonAttribute> getAttributes() {
        //TODO userright check
        try {
            List<JsonAttribute> result = Collections.synchronizedList(new ArrayList<>());

            List<JsonObject> allObjects = getObjects();
            List<JsonAttribute> attributes = getAttributeTable().getAllAttributes();
            Set<Long> objectMap = Collections.newSetFromMap(new ConcurrentHashMap<>());

            allObjects.parallelStream().forEach(jsonObject -> {
                try {
                    if (this.um.canRead(jsonObject)) {
                        objectMap.add(jsonObject.getId());
                    }
                } catch (Exception ex) {
                }
            });

            attributes.parallelStream().forEach(attribute -> {
                try {
                    if (objectMap.contains(attribute.getObjectID())) {
                        result.add(attribute);
                    }
                } catch (Exception ex) {
                }
            });

            return result;
        } catch (Exception ex) {
            logger.error("Error while loading AllAttributes", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Returns a single attribute by object ID and attribute name.
     * If no database row exists for the attribute, a virtual default attribute
     * is constructed from the class type definition.
     *
     * @param objectID the object ID
     * @param name     the attribute name
     * @return the {@link JsonAttribute}, or {@code null} if not found
     */
    public JsonAttribute getAttribute(long objectID, String name) {
        try {
            JsonObject ob = getObject(objectID);
            JsonJEVisClass jc = Config.getClassCache().get(ob.getJevisClass());
            JsonAttribute attribute = getAttributeTable().getAttribute(objectID, name);

            if (attribute != null) {
                return attribute;
            } else {
                if (jc != null && jc.getTypes() != null) {
                    for (JsonType type : jc.getTypes()) {
                        boolean exists = false;

                        if (type.getName().equals(name)) {
                            JsonAttribute newAtt = new JsonAttribute();
                            newAtt.setObjectID(objectID);
                            newAtt.setType(type.getName());
                            newAtt.setBegins("");
                            newAtt.setEnds("");
                            newAtt.setDisplaySampleRate(Period.ZERO.toString());
                            newAtt.setInputSampleRate("");
                            newAtt.setSampleCount(0);
                            newAtt.setPrimitiveType(type.getPrimitiveType());

                            JsonUnit unit = JsonFactory.buildUnit(new JEVisUnitImp(AbstractUnit.ONE));

                            newAtt.setDisplayUnit(unit);
                            newAtt.setInputUnit(unit);
                            return newAtt;
                        }

                    }
                }
            }

            // because jevis will not create default attributes or manage the update of types
            // we check that all and only all types are there

            logger.warn("Empty Type list for class: {}", (jc != null ? jc.getName() : "null (class not in cache)"));
            return null;
        } catch (Exception ex) {
            logger.error("Error in getAttribute objectID={} name={}", objectID, name, ex);
            return null;
        }
    }

    /**
     * Returns all attributes for the given object. Attributes that exist in the
     * class definition but not yet in the database are returned as virtual
     * defaults with zero sample count.
     *
     * @param objectID the object ID
     * @return a list of {@link JsonAttribute} instances; empty if none found
     */
    public List<JsonAttribute> getAttributes(long objectID) {
        try {
            JsonObject ob = getObject(objectID);
            JsonJEVisClass jc = Config.getClassCache().get(ob.getJevisClass());
            List<JsonAttribute> atts = getAttributeTable().getAttributes(objectID);
            List<JsonAttribute> result = new ArrayList<>();

            // because jevis will not create default attributes or manage the update of types
            // we check that all and only all types are there
            if (jc.getTypes() != null) {
                for (JsonType type : jc.getTypes()) {
                    boolean exists = false;
                    for (JsonAttribute att : atts) {
                        if (type.getName().equals(att.getType())) {
                            exists = true;
                            result.add(att);
                        }
                    }
                    if (!exists) {
                        //new Default Attribute
                        JsonAttribute newAtt = new JsonAttribute();
                        newAtt.setObjectID(objectID);
                        newAtt.setType(type.getName());
                        newAtt.setBegins("");
                        newAtt.setEnds("");
                        newAtt.setDisplaySampleRate(Period.ZERO.toString());
                        newAtt.setInputSampleRate("");
                        newAtt.setSampleCount(0);
                        newAtt.setPrimitiveType(type.getPrimitiveType());

                        try {
                            JsonUnit unit = JsonFactory.buildUnit(new JEVisUnitImp(AbstractUnit.ONE));
                            newAtt.setDisplayUnit(unit);
                            newAtt.setInputUnit(unit);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                        result.add(newAtt);
                    }
                }

                return result;

            }
            logger.info("Empty Type list for class: " + jc.getName());
            return new ArrayList<>();
        } catch (Exception ex) {
            logger.info("================= Error in attribute: " + objectID);
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Persists updated attribute metadata (units, sample rates) to the database.
     *
     * @param objectID the owning object ID
     * @param att      the attribute to update
     * @return {@code true} always (the method delegates to the table layer)
     * @throws JEVisException if the update fails
     */
    public boolean setAttribute(long objectID, JsonAttribute att) throws JEVisException {
        getAttributeTable().updateAttribute(objectID, att);
        return true;
    }

    /**
     * Creates a new object in the database under the specified parent, copying
     * ownership relationships from the parent.
     *
     * @param newObjecrequest the template object (name, class, public flag)
     * @param parent          the ID of the parent object
     * @param i18n            i18n JSON string, may be {@code null}
     * @return the newly created {@link JsonObject}
     * @throws JEVisException if the insert or relationship copy fails
     */
    public JsonObject buildObject(JsonObject newObjecrequest, long parent, String i18n) throws JEVisException {
        return getObjectTable().insertObject(newObjecrequest.getName(), newObjecrequest.getJevisClass(), parent, newObjecrequest.getisPublic(), i18n);
    }

    /**
     * Copies all OWNER-type relationships from one object to another.
     * Used when moving objects to preserve user rights.
     *
     * @param sourceObj the source object whose rights are copied
     * @param targetObj the target object that receives the rights
     * @throws JEVisException if a relationship insert fails
     */
    public void copyOwnerPermissions(long sourceObj, long targetObj) throws JEVisException {
        List<JsonRelationship> relationships = getRelationships(sourceObj);
        for (JsonRelationship rel : relationships) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER) {
                JsonRelationship newRelationship = new JsonRelationship();
                newRelationship.setType(rel.getType());
                newRelationship.setFrom(targetObj);
                newRelationship.setTo(rel.getTo());
                logger.debug("Add ower rel: " + newRelationship);
                setRelationships(newRelationship);
            }
        }
    }

    /**
     * Moves an object to a new parent by deleting and re-creating all
     * permission relationships recursively.
     *
     * @param original    the ID of the object to move
     * @param newParentID the ID of the new parent object
     * @throws JEVisException if the move operation fails
     */
    public void moveObject(long original, long newParentID) throws JEVisException {
        JsonObject newParent = getObject(newParentID);

        deleteRelationshipsRecursion(original);
        addRelationshipsRecursion(getRelationships(newParentID), original, newParentID);

        buildRelationship(original, newParentID, JEVisConstants.ObjectRelationship.PARENT);
    }

    private void addRelationshipsRecursion(List<JsonRelationship> rels, long oID, long oldID) throws JEVisException {
        for (JsonRelationship rel : getRelationships(oID)) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT && rel.getTo() == oID) {
                addRelationshipsRecursion(rels, rel.getFrom(), oID);
                for (JsonRelationship copyRel : rels) {
                    Integer[] rightsTypes = new Integer[]{
                            JEVisConstants.ObjectRelationship.MEMBER_READ, JEVisConstants.ObjectRelationship.MEMBER_WRITE,
                            JEVisConstants.ObjectRelationship.MEMBER_DELETE, JEVisConstants.ObjectRelationship.MEMBER_CREATE,
                            JEVisConstants.ObjectRelationship.MEMBER_EXECUTE, JEVisConstants.ObjectRelationship.OWNER};
                    if (Arrays.asList(rightsTypes).contains(copyRel.getType())) {
                        if (copyRel.getFrom() == oldID) {
                            getRelationshipTable().insert(oID, copyRel.getTo(), copyRel.getType());
                        } else if (copyRel.getTo() == oldID) {
                            getRelationshipTable().insert(copyRel.getFrom(), oID, copyRel.getType());
                        }

                    }
                }

            }
        }
    }

    private void deleteRelationshipsRecursion(long oID) {
        for (JsonRelationship rel : getRelationships(oID)) {
            if (rel.getType() != JEVisConstants.ObjectRelationship.PARENT) {
                getRelationshipTable().delete(rel);
            } else if (rel.getTo() == oID) {
                deleteRelationshipsRecursion(rel.getFrom());
            }
        }
    }

    /**
     * Marks the given object (and all its descendants) as deleted without
     * physically removing them from the database.
     *
     * @param objectID the object to mark as deleted
     * @return {@code true} if the operation succeeded
     */
    public boolean markAsDeletedObject(JsonObject objectID) {
        return getObjectTable().markObjectAsDeleted(objectID);
    }

    /**
     * Permanently removes the given object (and all its descendants) from the
     * database.
     *
     * @param objectID the object to delete
     * @return {@code true} if the deletion succeeded
     */
    public boolean deleteObject(JsonObject objectID) {
        return getObjectTable().deleteObjectFromDB(objectID);
    }

    /**
     * Deletes a single relationship identified by its from/to/type triple.
     *
     * @param fromObject the source object ID
     * @param toObject   the target object ID
     * @param type       the relationship type constant
     * @return {@code true} if the deletion succeeded
     */
    public boolean deleteRelationship(Long fromObject, Long toObject, int type) {
        return getRelationshipTable().delete(fromObject, toObject, type);
    }


    /**
     * Releases all resources held by this data source to assist the garbage
     * collector. The instance must not be used after calling this method.
     */
    public void clear() {
        this.um.clear();
        this.um = null;

        this.lTable = null;
        this.oTable = null;
        this.aTable = null;
        this.sTable = null;
        this.rTable = null;

        this.allRelationships.clear();
        this.allRelationships = null;
        this.allObjects.clear();
        this.allObjects = null;
    }

    /**
     * Returns the shared, thread-safe Jackson {@link ObjectMapper}.
     *
     * @return the shared mapper instance
     */
    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Event types recorded in the user activity log.
     */
    public enum LOG_EVENT {
        USER_LOGIN,
        DELETE_OBJECT, MARK_AS_DELETE_OBJECT, DELETE_SAMPLE, DELETE_RELATIONSHIP,
        CREATE_OBJECT, CREATE_SAMPLE, CREATE_RELATIONSHIP,
        UPDATE_OBJECT, UPDATE_ATTRIBUTE

    }

    /**
     * Categories of data that can be bulk-loaded into the request-scoped cache
     * via {@link #preload(PRELOAD)}.
     */
    public enum PRELOAD {
        /**
         * Pre-load all relationships.
         */
        ALL_REL,
        /**
         * Pre-load all class definitions.
         */
        ALL_CLASSES,
        /** Pre-load all objects. */
        ALL_OBJECT
    }
}
