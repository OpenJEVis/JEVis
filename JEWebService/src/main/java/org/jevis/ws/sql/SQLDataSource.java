/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.ws.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.*;
import org.jevis.rest.Config;
import org.jevis.rest.ErrorBuilder;
import org.jevis.ws.sql.tables.*;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import javax.measure.unit.Unit;
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

/**
 * @author fs
 */
public class SQLDataSource {

    private static final Logger logger = LogManager.getLogger(org.jevis.ws.sql.SQLDataSource.class);
    private Connection dbConn;
    private JEVisUserNew user;

    private LoginTable lTable;
    private ObjectTable oTable;
    private AttributeTable aTable;
    private SampleTable sTable;
    private RelationshipTable rTable;

    private List<JsonRelationship> allRelationships = Collections.synchronizedList(new LinkedList<>());
    private List<JsonObject> allObjects = Collections.synchronizedList(new LinkedList<>());
    private UserRightManagerForWS um;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public enum LOG_EVENT {
        USER_LOGIN,
        DELETE_OBJECT, DELETE_SAMPLE, DELETE_RELATIONSHIP,
        CREATE_OBJECT, CREATE_SAMPLE, CREATE_RELATIONSHIP,
        UPDATE_OBJECT, UPDATE_ATTRIBUTE

    }

    public SQLDataSource(HttpHeaders httpHeaders, Request request, UriInfo url) throws AuthenticationException, JEVisException {

        try {
            ConnectionFactory.getInstance().registerMySQLDriver(Config.getDBHost(), Config.getDBPort(), Config.getSchema(), Config.getDBUser(), Config.getDBPW());

            this.dbConn = ConnectionFactory.getInstance().getConnection();

            if (this.dbConn.isValid(2000)) {
                this.lTable = new LoginTable(this);
                jevisLogin(httpHeaders);
                this.oTable = new ObjectTable(this);
                this.aTable = new AttributeTable(this);
                this.sTable = new SampleTable(this);
                this.rTable = new RelationshipTable(this);
                this.um = new UserRightManagerForWS(this);

            }
        } catch (SQLException se) {
            logger.error(se);
            throw new JEVisException("Database connection error", 5438, se);
        }
    }

    public SQLDataSource(Connection dbConn) throws AuthenticationException, JEVisException {
        this.dbConn = dbConn;
        this.lTable = new LoginTable(this);
        this.oTable = new ObjectTable(this);
        this.aTable = new AttributeTable(this);
        this.sTable = new SampleTable(this);
        this.rTable = new RelationshipTable(this);
    }

    public Connection getConnection() throws SQLException {
        return this.dbConn;
    }


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
            getSampleTable().insertSamples(getCurrentUser().getUserID(), "Activities", JEVisConstants.PrimitiveType.STRING, Arrays.asList(newSample));
        } catch (Exception ex) {
            logger.error("Error while logging Event: {}:{}:{}", event, msg, ex);
        }
    }


    /**
     * Clean up User Event for "General Data Protection Regulations"
     * TODO: make it configurable
     */
    public void gdprCleanUp() throws SQLException, AuthenticationException, JEVisException {
        logger.error("Starting user log cleanup for Data Protection");
        getSampleTable().deleteOldLogging();
    }


    public List<JsonClassRelationship> getClassRelationships() {
        List<JsonJEVisClass> list = new ArrayList<>(Config.getClassCache().values());
        List<JsonClassRelationship> tempjcr = new ArrayList<>();
        for (JsonJEVisClass jjc : list) {
            if (jjc.getRelationships() != null) tempjcr.addAll(jjc.getRelationships());
        }
        return tempjcr;
    }

    public List<JsonClassRelationship> getClassRelationships(String className) {
        List<JsonJEVisClass> list = new ArrayList<>(Config.getClassCache().values());
        List<JsonClassRelationship> tempjcr = new ArrayList<>();
        for (JsonJEVisClass jjc : list) {
            if ((jjc.getRelationships() != null) && (jjc.getName().equals(className)))
                tempjcr.addAll(jjc.getRelationships());
        }
        return tempjcr;
    }

    public List<JsonType> getTypes(JsonJEVisClass jevisClass) {
        List<JsonJEVisClass> list = new ArrayList<>(Config.getClassCache().values());
        if (list.contains(jevisClass)) {
            return list.get(list.indexOf(jevisClass)).getTypes();
        } else return null;
    }

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

    public List<JsonJEVisClass> getJEVisClasses() {
        return new ArrayList<>(Config.getClassCache().values());
    }


    public UserRightManagerForWS getUserManager() {
        return this.um;
    }

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

    public RelationshipTable getRelationshipTable() {
        return this.rTable;
    }

    public ObjectTable getObjectTable() {
        return this.oTable;
    }

    public LoginTable getLoginTable() {
        return this.lTable;
    }


    public SampleTable getSampleTable() {
        return this.sTable;
    }

    public AttributeTable getAttributeTable() {
        return this.aTable;
    }


    public List<JsonObject> filterObjectByClass(List<JsonObject> objects, String jclass) {
        List<JsonObject> filterd = new ArrayList<>();
        List<String> heir = new ArrayList<>();
        heir.add(jclass);
        JEVisClassHelper.findHeir(jclass, heir);
        for (JsonObject obj : objects) {
            if (heir.contains(obj.getJevisClass())) {
                filterd.add(obj);
            }
        }
        return filterd;
    }

    private void jevisLogin(HttpHeaders httpHeaders) throws AuthenticationException {
        if (httpHeaders.getRequestHeader("authorization") == null || httpHeaders.getRequestHeader("authorization").isEmpty()) {
            throw new AuthenticationException("Authorization header is missing");
        }
//        try {
        String auth = httpHeaders.getRequestHeader("authorization").get(0);
        if (auth != null && !auth.isEmpty()) {

            auth = auth.replaceFirst("[Bb]asic ", "");
            byte[] decoded = Base64.decodeBase64(auth);

            try {
                String decodeS = (new String(decoded, StandardCharsets.UTF_8));
                String[] dauth = decodeS.split(":");
                if (dauth.length == 2) {

                    String username = dauth[0];
                    String password = dauth[1];
                    try {
                        this.user = this.lTable.loginUser(username, password);

                    } catch (JEVisException ex) {
                        ex.printStackTrace();
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
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//            throw new AuthenticationException("Authorization header incorrect", ex);
//        }
    }


    public JEVisRelationship buildRelationship(Long fromObject, Long toObject, int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<JsonObject> getRootObjects() {
        return getUserManager().getRoots();
    }

    public List<JsonObject> getObjects(String jevisClass, boolean addheirs) throws JEVisException {
        List<JsonObject> list = Collections.synchronizedList(new LinkedList<>());
        List<String> allHeir = new ArrayList<>();
        allHeir.add(jevisClass);
        if (addheirs) {
            JEVisClassHelper.findHeir(jevisClass, allHeir);
        }

        getObjects().parallelStream().forEach(ob -> {
            if (allHeir.contains(ob.getJevisClass())) {
                list.add(ob);
            }
        });
        return list;

    }

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
                getRelationshipTable().getAllForObject(ob.getId()).forEach(rel -> {
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
        JsonObject ob = this.oTable.getObject(id);
        if (ob != null) {
            this.allObjects.add(ob);
        }
        return ob;

    }

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

    public JsonObject updateObject(long id, String newname, boolean ispublic, String i18n) throws JEVisException {
        logger.debug("updateObject");
        return getObjectTable().updateObject(id, newname, ispublic,i18n);
    }

    public List<JsonObject> getObjects() throws JEVisException {
        logger.debug("getObjectS");
        if (!this.allObjects.isEmpty()) {
            logger.debug("Cache");
            return this.allObjects;
        }

        this.allObjects = this.oTable.getAllObjects();
        logger.debug("NONE-Cache");
        return this.allObjects;
    }

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

    public JsonRelationship setRelationships(JsonRelationship rel) throws JEVisException {
        return getRelationshipTable().insert(rel.getFrom(), rel.getTo(), rel.getType());
    }

    public JsonJEVisClass getJEVisClass(String name) {
        return Config.getClassCache().get(name);
    }

    public JEVisUserNew getCurrentUser() {
        if (this.user == null) {
            return new JEVisUserNew(this, "Unkown", -1l, false, false);
        }
        return this.user;
    }

    public List<JsonSample> getSamples(long obj, String attribute, DateTime from, DateTime until, long limit) throws JEVisException {
        return getSampleTable().getSamples(obj, attribute, from, until, limit);
    }

    public int setSamples(long obj, String attribute, int primitiveType, List<JsonSample> samples) throws JEVisException {
        return getSampleTable().insertSamples(obj, attribute, primitiveType, samples);
    }

    public JsonSample getLastSample(long obj, String attribute) throws JEVisException {
        return getSampleTable().getLatest(obj, attribute);
    }

    public List<JsonRelationship> getRelationships(long object) {
        if (!this.allRelationships.isEmpty()) {

            List<JsonRelationship> list = Collections.synchronizedList(new LinkedList<>());
            this.allRelationships.parallelStream().forEach(rel -> {
                if (rel.getTo() == object || rel.getFrom() == object) {
                    if (!list.contains(rel)) {
                        list.add(rel);
                    }

                }
            });
            return list;
        }
        return getRelationshipTable().getAllForObject(object);

    }

    public List<JsonRelationship> getRelationships(int type) {
        List<JsonRelationship> list = Collections.synchronizedList(new ArrayList<>());

        getRelationships().parallelStream().forEach(rel -> {
            if (rel.getType() == type) {
                list.add(rel);
            }
        });

        return list;
    }

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

    public boolean disconnect() throws JEVisException {
        if (this.dbConn != null) {
            try {
                this.dbConn.close();
//                dbConn=null;
                return true;
            } catch (SQLException se) {
                throw new JEVisException("Error while closing DB connection", 6492, se);
            }
        }
        return false;
    }

    public JsonAttribute getAttribute(long objectID, String name) {
        for (JsonAttribute att : getAttributes(objectID)) {
            if (att.getType().equals(name)) {
                return att;
            }
        }
        return null;
    }

    public boolean deleteAllSample(long object, String attribute) {
        return getSampleTable().deleteAllSamples(object, attribute);
    }

    public boolean deleteSamplesBetween(long object, String attribute, DateTime startDate, DateTime endDate) {
        return getSampleTable().deleteSamples(object, attribute, startDate, endDate);
    }

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

                        JsonUnit unit = JsonFactory.buildUnit(new JEVisUnitImp(Unit.ONE));

                        newAtt.setDisplayUnit(unit);
                        newAtt.setInputUnit(unit);
                        result.add(newAtt);
                    }
                }

                return result;

            }
            logger.info("Emty Type list for class: " + jc.getName());
            return new ArrayList<>();
        } catch (Exception ex) {
            logger.info("================= Error in attribute: " + objectID);
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean setAttribute(long objectID, JsonAttribute att) throws JEVisException {
        getAttributeTable().updateAttribute(objectID, att);
        return true;
    }

    /**
     * Build an new object and copy the user right relationships from the parent
     * <p>
     * TODO: if something failed rollback?
     *
     * @param newObjecrequest
     * @param parent
     * @return
     * @throws JEVisException
     */
    public JsonObject buildObject(JsonObject newObjecrequest, long parent, String i18n) throws JEVisException {
        //        copyOwnerPermissions(parent, newObj.getId());
//        JsonRelationship parentRelationship = new JsonRelationship();
//        parentRelationship.setType(JEVisConstants.ObjectRelationship.PARENT);
//        parentRelationship.setFrom(newObj.getId());
//        parentRelationship.setTo(parent);
//        setRelationships(parentRelationship);

        return getObjectTable().insertObject(newObjecrequest.getName(), newObjecrequest.getJevisClass(), parent, newObjecrequest.getisPublic(),i18n);
    }

    /**
     * Copy all user rights related relationships from an object to an other.
     *
     * @param sourceObj
     * @param targetObj
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

    public boolean deleteObject(JsonObject objectID) {
        return getObjectTable().deleteObject(objectID);
    }

    public boolean deleteRelationship(Long fromObject, Long toObject, int type) {
        return getRelationshipTable().delete(fromObject, toObject, type);
    }


    /**
     * Let us try to help the garbage collector to clean up
     */
    public void clear() {
        this.um.clear();
        this.um = null;

        this.lTable = null;
        this.oTable = null;
        this.aTable = null;
        this.sTable = null;
        this.rTable = null;
        this.lTable = null;

        this.allRelationships.clear();
        this.allRelationships = null;
        this.allObjects.clear();
        this.allObjects = null;
    }

    public enum PRELOAD {
        ALL_REL, ALL_CLASSES, ALL_OBJECT
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
