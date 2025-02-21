/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.utils.Benchmark;
import org.jevis.commons.utils.Optimization;
import org.jevis.commons.utils.PrettyError;
import org.jevis.commons.ws.json.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author fs
 */
public class JEVisDataSourceWS implements JEVisDataSource {
    private static final Logger logger = LogManager.getLogger(JEVisDataSourceWS.class);
    private final int port = 8080;
    final private JEVisInfo info = new JEVisInfo() {

        @Override
        public String getVersion() {
            return "3.0.1";
        }

        @Override
        public String getName() {
            return "JEAPI-WS";
        }
    };
    private final ObjectMapper objectMapper = new ObjectMapper();
    //    private List<JEVisRelationship> objectRelCache = Collections.synchronizedList(new ArrayList<JEVisRelationship>());
    private final ConcurrentHashMap<String, JEVisClass> classCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, JEVisObject> objectCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, JEVisObject> deltedObjectCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<JEVisRelationship>> objectRelMapCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Object> objectNullCache = new ConcurrentHashMap<>();


    /**
     * Amount of Samples in one request
     **/
    private final int SAMPLE_REQUEST_SIZE = 10000;
    private final ConcurrentHashMap<Long, List<JEVisAttribute>> attributeCache = new ConcurrentHashMap<>();
    private String host = "http://localhost";
    private HTTPConnection con;
    /*
     * Connection to check if the connection is alive
     */
    private HttpURLConnection isAliveConnection = null;
    //    private Gson gson = new Gson();
    private JEVisUser user;
    private List<JEVisOption> config = new ArrayList<>();
    private boolean allAttributesPreloaded = false;
    private boolean classLoaded = false;
    private boolean objectLoaded = false;
    private boolean deletedObjectLoaded = false;
    private boolean orLoaded = false;
    private HTTPConnection.Trust sslTrustMode = HTTPConnection.Trust.SYSTEM;
    /**
     * fallback because some old client will call preload but we now a days do per default
     **/
    private boolean hasPreloaded = false;

    public JEVisDataSourceWS(String host) {
        this.host = host;
        configureObjectMapper();
    }

    public JEVisDataSourceWS() {
        configureObjectMapper();
    }

    private void configureObjectMapper() {
        objectMapper.registerModule(new AfterburnerModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void init(List<JEVisOption> config) throws IllegalArgumentException {
        logger.info("Start JEVisDataSourceWS Version: " + this.info.getVersion());

    }

    @Override
    public List<JEVisOption> getConfiguration() {
        return this.config;
    }

    @Override
    public void setConfiguration(List<JEVisOption> config) {
        this.config = config;
        for (JEVisOption opt : config) {
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                try {
                    this.host = opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue();
                } catch (Exception ex) {
                    logger.error("Error while parsing option: {}", ex.getMessage(), ex);
                }
            }
            if (opt.getKey().equals(CommonOptions.DataSource.SSLTRUST.getKey())) {
                String sslModeOpt = opt.getValue();
                if (sslModeOpt != null && !sslModeOpt.isEmpty()) {
                    this.sslTrustMode = HTTPConnection.Trust.valueOf(sslModeOpt.toUpperCase());
                } else {
                    logger.error("No SSL-Trust-Mode set, using default: {}", sslTrustMode.toString());
                }
            }

        }

        /** create dummy connection for request which need not user name password **/
        this.con = new HTTPConnection(this.host, "", "", sslTrustMode);
    }

    @Override
    public JEVisClass buildClass(String name) {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(name);

        return new JEVisClassWS(this, json);
    }

    @Override
    public JEVisObject buildLink(String name, JEVisObject parent, JEVisObject linkedObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @param fromClass
     * @param toClass
     * @param type
     * @return
     * @TODO: we may need to cache the relationships but for now its fast
     * enough. IF so we need an Cach implementaion for the relationships
     */
    @Override
    public JEVisClassRelationship buildClassRelationship(String fromClass, String toClass, int type) {

        //TODO: re-impalement after Webservice change
        try {
            JsonClassRelationship newJsonRel = new JsonClassRelationship();
            newJsonRel.setStart(fromClass);
            newJsonRel.setEnd(toClass);
            newJsonRel.setType(type);
            return new JEVisClassRelationshipWS(this, newJsonRel);


        } catch (Exception ex) {
            logger.catching(ex);
            return null;//TODO throw error
        }
    }

    @Override
    public JEVisRelationship buildRelationship(Long fromObject, Long toObject, int type) {
        try {
            JsonRelationship newJsonRel = new JsonRelationship();
            newJsonRel.setFrom(fromObject);
            newJsonRel.setTo(toObject);
            newJsonRel.setType(type);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH;

//            logger.debug("payload: {}", this.gson.toJson(newJsonRel));
//            StringBuffer response = getHTTPConnection().postRequest(resource, this.gson.toJson(newJsonRel));

            JsonRelationship newJson = this.objectMapper.readValue(this.getHTTPConnection().postRequest(resource, this.objectMapper.writeValueAsString(newJsonRel)).toString(), JsonRelationship.class);

//            JsonRelationship newJson = this.gson.fromJson(response.toString(), JsonRelationship.class);
            JEVisRelationship newRel = new JEVisRelationshipWS(this, newJson);

            this.objectRelMapCache.get(newRel.getStartID()).add(newRel);
            this.objectRelMapCache.get(newRel.getEndID()).add(newRel);

            if ((newRel.getType() >= JEVisConstants.ObjectRelationship.MEMBER_READ
                    && newRel.getType() <= JEVisConstants.ObjectRelationship.MEMBER_DELETE) || newRel.getType() == JEVisConstants.ObjectRelationship.OWNER) {
                getCurrentUser().reload();
            }

            return newRel;

        } catch (Exception ex) {
            logger.catching(ex);
            return null;//TODO throw error
        }

    }

    @Override
    public List<JEVisObject> getRootObjects() {
        /**
         * We now load the list from the cache to improve performance
         */
        List<JEVisObject> objs = new ArrayList<>();


        List<Long> groupIds = new ArrayList<>();
        List<JEVisRelationship> jeVisRelationships = this.relationshipMapToList();
        for (JEVisRelationship rel : jeVisRelationships) {
            try {
                if (rel.isType(JEVisConstants.ObjectRelationship.MEMBER_READ)
                        && getCurrentUser().getUserID() == rel.getStartID()) {

                    groupIds.add(rel.getEndID());
                }
            } catch (Exception ex) {

            }
        }

        for (JEVisRelationship rel : jeVisRelationships) {
            try {
                //group to root
                if (rel.isType(JEVisConstants.ObjectRelationship.ROOT) && groupIds.contains(rel.getStartID())) {
                    objs.add(rel.getEndObject());
                }
            } catch (Exception ex) {

            }
        }


        return objs.stream().filter(jeVisObject -> !parentIsInList(objs, jeVisObject)).collect(Collectors.toList());

        //return objs;

    }

    @Override
    public List<JEVisObject> getObjects(JEVisClass jevisClass, boolean addheirs) throws JEVisException {

        if (jevisClass == null) {
            logger.error("Class does not exist {}", jevisClass);
            return new ArrayList<>();
        }

        /**
         * We now load the list from the cache to improve performance
         */
        List<JEVisClass> filterClass = new ArrayList<>();
        filterClass.add(jevisClass);
        if (addheirs) {
            if (jevisClass.getHeirs() != null) {
                filterClass.addAll(jevisClass.getHeirs());
            }

        }

        List<JEVisObject> objs = new ArrayList<>();

        for (JEVisObject obj : this.objectCache.values()) {
            if (filterClass.contains(obj.getJEVisClass()) && obj.getDeleteTS() == null) {
                objs.add(obj);
            }
        }


        return objs;
    }

    @Override
    public JEVisObject getObject(Long id) {
        if (id != null) {
            if (objectNullCache.containsKey(id)) return null;

            JEVisObject obj = this.objectCache.getOrDefault(id, null);
            if (obj != null) {
                return obj;
            } else {
                logger.debug("Object without cache: " + id);
                JEVisObject wsobj = getObjectWS(id);
                if (wsobj == null) {
                    objectNullCache.put(id, id);
                }
                return wsobj;
            }
        } else return null;

    }

    @Override
    public List<JEVisObject> getObjects() {
        logger.debug("getObjects");
        if (!this.objectLoaded) {
            getObjectsWS(true);
            this.objectLoaded = true;
        }

        //return this.objectCache.values().stream().filter(jeVisObject -> jeVisObject.getDeleteTS() == null).collect(Collectors.toList());
        return new ArrayList<>(this.objectCache.values());

    }

    @Override
    public List<JEVisObject> getDeletedObjects() throws JEVisException {
        logger.debug("getDeletedObjects");
        if (!this.deletedObjectLoaded) {
            getDeletedObjectsWS();
            this.deletedObjectLoaded = true;
        }

        return new ArrayList<>(this.deltedObjectCache.values());
    }

    @Override
    public JEVisClass getJEVisClass(String name) {
        //for now we assume classes are allways cached
        if (this.classCache.isEmpty()) {
            getJEVisClasses();
        }

        return this.classCache.get(name);

    }

    @Override
    public List<JEVisClass> getJEVisClasses() {
        if (!this.classLoaded) {

            getJEVisClassesWS().parallelStream().forEach(jclass -> {
                try {
                    this.classCache.put(jclass.getName(), jclass);
                    jclass.getRelationships();
                } catch (Exception ex) {
                    logger.error("Error in class relationship {}", jclass, ex);
                }
            });
            this.classLoaded = true;
        }

        return new ArrayList<>(this.classCache.values());
    }

    @Override
    public JEVisUser getCurrentUser() {
        return this.user;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisRelationship> list = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.getType() == type) {
                list.add(rel);
            }
        }
        return list;
    }

    @Override
    public List<JEVisRelationship> getRelationships(long objectID) {
        if (!this.orLoaded) {
            getRelationships();
        }

        return this.objectRelMapCache.getOrDefault(objectID, new ArrayList<>());

    }

    @Override
    public List<JEVisRelationship> getRelationships() {
        logger.debug("getAllRelationships");
        if (!this.orLoaded) {

            this.objectRelMapCache.clear();


            this.getRelationshipsWS().forEach(re -> {
                try {
                    long startID = re.getStartID();
                    long endID = re.getEndID();

                    if (!this.objectRelMapCache.containsKey(startID)) {
                        this.objectRelMapCache.put(startID, new CopyOnWriteArrayList<>());
                    }

                    if (!this.objectRelMapCache.containsKey(endID)) {
                        this.objectRelMapCache.put(endID, new CopyOnWriteArrayList<>());
                    }

                    this.objectRelMapCache.get(startID).add(re);
                    this.objectRelMapCache.get(endID).add(re);
                } catch (Exception ex) {
                    logger.error("incorrect relationship: {}\n{}", re, ex);
                }
            });

            logger.debug("Relationship amount: {}", this.objectRelMapCache.size());

            this.orLoaded = true;
        }

        return relationshipMapToList();
    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships(String jclass) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Load all attributes for all objects.
     *
     * @return
     * @throws JEVisException
     */
    @Override
    public void getAttributes() throws JEVisException {
        logger.debug("Get all attributes Objects");

        try {

            List<JEVisAttribute> attributeList = Collections.synchronizedList(new ArrayList<>());

            /**
             * 1. case, attributes are loaded and need no update. Return list from cache.
             */
            if (!this.allAttributesPreloaded) {

                String resource = HTTPConnection.API_PATH_V1
                        + REQUEST.ATTRIBUTES.PATH;
//                StringBuffer response = con.getRequest(resource);
//                ObjectMapper objectMapper = new ObjectMapper();
                InputStream inputStream = this.con.getInputStreamRequest(resource);
                List<JsonAttribute> jsons = Arrays.asList(this.objectMapper.readValue(inputStream, JsonAttribute[].class));
                inputStream.close();
                logger.debug("JsonAttribute.count: {}", jsons.size());


                for (JsonAttribute jsonAttribute : jsons) {
                    try {
                        if (jsonAttribute != null) {
                            attributeList.add(updateAttributeCache(jsonAttribute));
                        }
                    } catch (Exception ex) {
                        logger.error("Error while parsing attribute: {}", ex.getMessage());
                    }
                }
                logger.debug("Done parsing attributes");


                /**
                 * TODO:
                 * 1. use fixMissingAttributes(attributes); fi fix missing attributes
                 * 2. is the function below oK?
                 */

                /**
                 * Give objects which have no attributes an empty list
                 */
                this.objectCache.keySet().forEach(aLong -> {
                    if (!this.attributeCache.containsKey(aLong)) {
                        this.attributeCache.put(aLong, new ArrayList<>());
                    }
                    fixMissingAttributes(getObject(aLong), this.attributeCache.get(aLong));

                });
                logger.debug("done fixing attributes");

                this.allAttributesPreloaded = true;

//                return attributeList;
            }

            /**
             * Load from cache
             */
//            List<JEVisAttribute> result = new ArrayList<>();
//            this.attributeCache.values().forEach(result::addAll);
//                System.out.println("end get attributes");
//            return result;

        } catch (ProtocolException ex) {
            logger.error(ex.getMessage(), ex);
            //TODO: throw excption?! so the other function can handel it?
//            return new ArrayList<>();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
//            return new ArrayList<>();
        }
    }

    @Override
    public boolean connect(String username, String password) throws JEVisException {
        logger.debug("Connect with user {} to: {}", username, this.host);

        this.con = new HTTPConnection(this.host, username, password, sslTrustMode);

        try {
            String resource
                    = REQUEST.API_PATH_V1
                    + REQUEST.JEVISUSER.PATH;


            InputStream inputStream = this.con.getInputStreamRequest(resource);
            if (inputStream != null) {
                JsonObject json = this.objectMapper.readValue(inputStream, JsonObject.class);
                inputStream.close();
                this.user = new JEVisUserWS(this, new JEVisObjectWS(this, json));
                return true;
            } else {
                return false;
            }


            //HttpURLConnection conn = getHTTPConnection().getGetConnection(resource);
            //logger.debug("Login Response: {}", conn.getResponseCode());
            /*
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

//                logger.debug("Login response: {}", conn.getContent().toString());
//                String payload = IOUtils.toString((InputStream) conn.getContent(), "UTF-8");

                InputStream inputStream = this.con.getInputStreamRequest(resource);
                JsonObject json = this.objectMapper.readValue(inputStream, JsonObject.class);
                inputStream.close();


                this.preload();

                this.user = new JEVisUserWS(this, new JEVisObjectWS(this, json));
                logger.debug("User.object: " + this.user.getUserObject());
                return true;
            } else {
                logger.error("Login failed: [{}] {}", conn.getResponseCode(), conn.getResponseMessage());
                return false;
            }
        */

        } catch (Exception ex) {
            logger.catching(ex);
            throw new JEVisException(ex.getMessage(), 402, ex);
        }
    }

    @Override
    public boolean disconnect() {
        //TODO: implement
        return true;
    }

    @Override
    public boolean reconnect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisInfo getInfo() {
        return this.info;
    }

    @Override
    public boolean isConnectionAlive() {
//        return true;

        try {
            if (isAliveConnection == null) {
                String resource = "api/rest/version";
                URL url = new URL(this.host + "/" + resource);
                isAliveConnection = (HttpURLConnection) url.openConnection();
                isAliveConnection.setConnectTimeout(25000);
                isAliveConnection.setReadTimeout(5000);
                isAliveConnection.setRequestMethod("GET");
            }


            logger.debug("HTTP request {}", isAliveConnection.getURL());

            isAliveConnection.connect();
            int responseCode = isAliveConnection.getResponseCode();
            //isAliveConnection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (ProtocolException e) {
            logger.error(PrettyError.getJEVisLineFilter(e));
//            e.printStackTrace();
        } catch (MalformedURLException e) {
            logger.error(PrettyError.getJEVisLineFilter(e));
//            e.printStackTrace();
        } catch (IOException e) {
            logger.error(PrettyError.getJEVisLineFilter(e));
//            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<JEVisUnit> getUnits() {
        //TODo: implement
        return new ArrayList<>();
    }

    @Override
    public List<JEVisAttribute> getAttributes(long objectID) {
//        logger.debug("Get  getAttributes: {}", objectID);

        if (this.attributeCache.containsKey(objectID)) {
//            logger.warn("Attribute is not in cache: {}", objectID);
//            logger.error("Cache size: " + attributeCache);
            return this.attributeCache.get(objectID);
        } else {
            return getAttributesFromWS(objectID);
        }

    }

    @Override
    public List<JEVisType> getTypes(String className) throws JEVisException {
        return getJEVisClass(className).getTypes();
    }

    @Override
    public boolean deleteObject(long objectID, boolean deleteForever) {
        try {
            logger.error("Delete: {}, {}", objectID, deleteForever);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID;

            if (deleteForever) {
                resource += "?" + REQUEST.OBJECTS.OPTIONS.DELETE_FOREVER + "true";
            }

            JEVisObject object = getObject(objectID);


            if (object != null) {
                HttpURLConnection response = getHTTPConnection().getDeleteConnection(resource);
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    response.disconnect();

                    object.getParents().forEach(parent -> {
                        try {
                            removeRelationshipFromCache(object.getID(), parent.getID(), JEVisConstants.ObjectRelationship.PARENT);
                            /* HOTFIX: The code below should do the same as the line above but does not why */
                            /*
                            List<JEVisRelationship> toRemove = new ArrayList<>();
                            objectRelMapCache.get(parent.getID()).forEach(rel -> {
                                try {
                                    Long id = rel.getEndID();
                                    if ((rel.getType() == JEVisConstants.ObjectRelationship.PARENT) && (id.equals(parent.getID()))) {
                                        if (rel.getStartObject() != null && object.getID().equals(rel.getStartID())) {
                                            toRemove.add(rel);
                                        }
                                    }
                                } catch (Exception ex) {
                                    logger.error(ex);
                                    ex.printStackTrace();
                                }
                            });
                            objectRelMapCache.get(parent.getID()).removeAll(toRemove);
                            objectRelMapCache.remove(object.getID());
                            */

                        } catch (Exception ex) {
                            logger.error(ex, ex);
                        }
                        parent.notifyListeners(new JEVisEvent(parent, JEVisEvent.TYPE.OBJECT_CHILD_DELETED, object));
                    });

                    if (deleteForever) {
                        object.notifyListeners(new JEVisEvent(object, JEVisEvent.TYPE.OBJECT_DELETE, object));
                    } else {
                        object.notifyListeners(new JEVisEvent(object, JEVisEvent.TYPE.OBJECT_DELETE_BIN, object));
                    }
                    /* reload should already have the delete ts but it seems to not work */

                    object.setDeleteTS(new DateTime());
                    reloadObject(object);


                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }


        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @Override
    public boolean deleteClass(String jclass) {
        try {
            logger.debug("Delete: {}", jclass);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + jclass;

            HttpURLConnection conn = getHTTPConnection().getDeleteConnection(resource);
            boolean response = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
            conn.disconnect();
            return response;

        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @Override
    public boolean deleteRelationship(Long fromObject, Long toObject, int type) {
        try {
            logger.debug("Delete: '{}' -> '{}' type:{}", fromObject, toObject, type);


            String resource = REQUEST.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH
                    + "?"
                    + REQUEST.RELATIONSHIPS.OPTIONS.FROM + fromObject
                    + "&"
                    + REQUEST.RELATIONSHIPS.OPTIONS.TO + toObject
                    + "&"
                    + REQUEST.RELATIONSHIPS.OPTIONS.TYPE + type;

            HttpURLConnection conn = getHTTPConnection().getDeleteConnection(resource);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                removeRelationshipFromCache(fromObject, toObject, type);
            } else {
                logger.error("Error while deleting Relationship {}", conn.getResponseCode());
            }

            boolean response = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
            conn.disconnect();
            return response;

        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @Override
    public boolean deleteClassRelationship(String fromClass, String toClass, int type) {
        //TODO: re-implement after webservice change
        return false;
    }

    @Override
    public void preload() {
        if (this.hasPreloaded) return;

        try {
            logger.info("Start preload");
            Benchmark benchmark = new Benchmark();
            getJEVisClasses();
            benchmark.printBenchmarkDetail("Preload - Classes");
            Optimization.getInstance().printStatistics();
            /**
             * removed until integration of class icon cache
             */
            //getClassIcon();
            //benchmark.printBenchmarkDetail("Preload - Icons");
            //Optimization.getInstance().printStatistics();


            this.getRelationships();

            if (!this.objectCache.isEmpty()) {
                this.objectNullCache.clear();
                this.objectCache.clear();
            }
            getObjects();
            benchmark.printBenchmarkDetail("Preload - Objects");
            Optimization.getInstance().printStatistics();

            if (!this.attributeCache.isEmpty()) this.attributeCache.clear();
            getAttributes();
            benchmark.printBenchmarkDetail("Preload - Attributes");
            Optimization.getInstance().printStatistics();
            logger.info("preload Done");
            this.hasPreloaded = true;
        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    @Override
    public void reloadObjects() {

        try {
            logger.info("Start reload");
            Benchmark benchmark = new Benchmark();

            this.objectNullCache.clear();
            this.objectRelMapCache.clear();
            this.objectCache.clear();
            this.attributeCache.clear();

            this.allAttributesPreloaded = false;
            this.objectLoaded = false;
            this.orLoaded = false;
            this.hasPreloaded = false;

            System.gc();
            Optimization.getInstance().clearCache();

            benchmark.printBenchmarkDetail("Reload - Cleared Cache");

            this.getRelationships();

            benchmark.printBenchmarkDetail("Reload - Relationships");

            if (!this.objectCache.isEmpty()) {
                this.objectNullCache.clear();
                this.objectCache.clear();
            }
            getObjects();
            benchmark.printBenchmarkDetail("Reload - Objects");
            Optimization.getInstance().printStatistics();

            if (!this.attributeCache.isEmpty()) this.attributeCache.clear();
            getAttributes();
            benchmark.printBenchmarkDetail("Reload - Attributes");
            System.gc();
            Optimization.getInstance().printStatistics();
            logger.info("Reload Done");
            this.hasPreloaded = true;
        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    @Override
    public void reloadAttributes() throws JEVisException {
        logger.debug("Complete attribute reload");
//        attributeCache.clear();
        this.allAttributesPreloaded = false;
    }

    @Override
    public void reloadAttribute(JEVisAttribute attribute) {
        try {
            if (attribute == null) {
                logger.error("Cannot reload null attribute");
                return;
            }
            logger.debug("Reload Attribute: {}", attribute);
            getAttributesFromWS(attribute.getObjectID());
        } catch (Exception ex) {
            logger.error("Error, can not reload attribute", ex);
        }
    }

    @Override
    public void reloadAttribute(JEVisObject object) {
        try {
            if (object != null) {
                logger.warn("Reload Attribute: {}", object);
                getAttributesFromWS(object.getID());
            } else {
                logger.error("Error trying to reload null object");
            }

        } catch (Exception ex) {
            logger.error("Error, can not reload attribute: {}", ex, ex);
        }
    }

    @Override
    public void reloadObject(JEVisObject object) {
        JEVisObject newObj = getObjectWS(object.getID());
    }

    @Override
    public void clearCache() {
//        classCache.clear();

        this.objectNullCache.clear();
        this.objectRelMapCache.clear();
//        this.objectRelCache.clear();
        this.objectCache.clear();
        this.attributeCache.clear();

        this.allAttributesPreloaded = false;
        this.classLoaded = false;
        this.objectLoaded = false;
        this.orLoaded = false;
        this.hasPreloaded = false;

        System.gc();
        Optimization.getInstance().clearCache();
    }

    @Override
    public void updateAccessControl() {
        logger.debug("updateAccessControl");
        String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_ACCESSCONTROL + "/update";
        try {

            InputStream inputStream = this.con.getInputStreamRequest(resource);
            this.objectMapper.readValue(inputStream, JsonObject.class);
            /*
            JsonObject json = null;
            if (inputStream != null) {
                json = this.objectMapper.readValue(inputStream, JsonObject.class);
                inputStream.close();
            }
            */

            /** TODO: implement error handling **/

        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error while fetching Object: {}", ex);
        } catch (JsonMappingException ex) {
            logger.error("Object is not accessible: {}", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error while fetching Object: {}", ex);
        } catch (Exception ex) {
            logger.error("Unexpected exception while fetching Object: {}, reason: {}", ex.getMessage());
        }
    }

    private boolean parentIsInList(List<JEVisObject> list, JEVisObject obj) {

        try {
            for (JEVisObject jeVisObject : obj.getParents()) {
                if (list.contains(obj.getParent())) {
                    return true;
                }
                return parentIsInList(list, jeVisObject);
            }
        } catch (JEVisException jex) {

        }
        return false;
    }

    public List<JEVisType> getTypes(JEVisClassWS jclass) {

        try {
            String resource = HTTPConnection.API_PATH_V1
                    + HTTPConnection.RESOURCE_CLASSES + "/" + jclass.getName()
                    + "/" + HTTPConnection.RESOURCE_TYPES;

            InputStream inputStream = this.con.getInputStreamRequest(resource);
            JsonType[] jsons = this.objectMapper.readValue(inputStream, JsonType[].class);
            inputStream.close();

            List<JEVisType> types = new ArrayList<>();
            for (JsonType type : jsons) {
//                logger.trace("Type: {}", type);
                types.add(new JEVisTypeWS(this, type, jclass.getName()));
            }

            return types;

        } catch (ProtocolException ex) {
            logger.error(ex);
            //TODO: throw excption?! so the other function can handel it?
        } catch (IOException ex) {
            logger.error(ex);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        return new ArrayList<>();
    }

    /**
     * Returns the installed version of the JEVisControlCenter on the server.
     *
     * @return Version of jecc. 0 if no version is set or unreachable.
     */
    public String getJEVisCCVersion() {
        String resource = "jecc/version";
        String version = "0";
        try {
            StringBuffer stringBuffer = this.con.getRequest(resource);
            version = stringBuffer.toString();
            logger.info("Version: {}", version);
        } catch (SSLHandshakeException sslex) {
            logger.error("SSl Exception: ", sslex);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }

        return version;
    }

    private void updateObject(JsonObject jsonObj) {
        JEVisObjectWS newObject = new JEVisObjectWS(this, jsonObj);
        this.objectCache.put(newObject.getID(), newObject);

    }

    private void updateDeletedObject(JsonObject jsonObj) {
        JEVisObjectWS newObject = new JEVisObjectWS(this, jsonObj);
        this.deltedObjectCache.put(newObject.getID(), newObject);

    }

    public void getObjectsWS(boolean includeDeleted) {
        logger.trace("Get ALL ObjectsWS");
        //TODO: throw exception?! so the other function can handle it?
        Benchmark benchmark = new Benchmark();
        String resource = HTTPConnection.API_PATH_V1
                + REQUEST.OBJECTS.PATH
                + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS + "false"
                + "?" + REQUEST.OBJECTS.OPTIONS.DELETED + includeDeleted
                + "&" + REQUEST.OBJECTS.OPTIONS.ONLY_ROOT + "false";
        List<JsonObject> jsonObjects = new ArrayList<>();
        try {
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            jsonObjects = Arrays.asList(this.objectMapper.readValue(inputStream, JsonObject[].class));
            inputStream.close();
        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error getting all objects. ", ex);
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error getting all objects. ", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error getting all objects. ", ex);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error getting all objects. ", e);
        }

        benchmark.printBechmark("reading object input stream done");
        logger.debug("JsonObject.count: {}", jsonObjects.size());
        jsonObjects.forEach(jsonObject -> {
            try {
                updateObject(jsonObject);
            } catch (Exception ex) {
                logger.error("Error while parsing object: {}", ex.getMessage());
            }
        });
        benchmark.printBechmark("updating object cache done for " + jsonObjects.size() + " objects");
    }

    public void getDeletedObjectsWS() {
        logger.trace("Get ALL DeletedObjectsWS");
        //TODO: throw exception?! so the other function can handle it?
        Benchmark benchmark = new Benchmark();
        String resource = HTTPConnection.API_PATH_V1
                + REQUEST.OBJECTS.PATH
                + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS + "false"
                + "&" + REQUEST.OBJECTS.OPTIONS.ONLY_ROOT + "false"
                + "&" + REQUEST.OBJECTS.OPTIONS.DELETED + "true";
        List<JsonObject> jsonObjects = new ArrayList<>();
        try {
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            jsonObjects = Arrays.asList(this.objectMapper.readValue(inputStream, JsonObject[].class));
            inputStream.close();
        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error getting all objects. ", ex);
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error getting all objects. ", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error getting all objects. ", ex);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error getting all objects. ", e);
        }

        benchmark.printBechmark("reading deleted object input stream done");
        logger.debug("JsonObject.count: {}", jsonObjects.size());
        jsonObjects.forEach(jsonObject -> {
            try {
                updateDeletedObject(jsonObject);
            } catch (Exception ex) {
                logger.error("Error while parsing object: {}", ex.getMessage());
            }
        });
        benchmark.printBechmark("updating deleted object cache done for " + jsonObjects.size() + " objects");
    }

    private List<JEVisRelationship> relationshipMapToList() {
        return this.objectRelMapCache.entrySet().stream().flatMap(entry -> entry.getValue().stream()).distinct().collect(Collectors.toList());
    }

    public void reloadRelationships(long id) {
        logger.debug("getAllRelationships");
        String resource = HTTPConnection.API_PATH_V1
                + REQUEST.RELATIONSHIPS.PATH
                + id;
        try {
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            List<JsonRelationship> jsons = new ArrayList<>(Arrays.asList(this.objectMapper.readValue(inputStream, JsonRelationship[].class)));
            inputStream.close();
            List<JEVisRelationship> rels = new ArrayList<>();


            for (JsonRelationship re : jsons) {
                rels.add(new JEVisRelationshipWS(JEVisDataSourceWS.this, re));
            }

            for (JEVisRelationship re : rels) {
                long startID = re.getStartID();
                long endID = re.getEndID();

                if (!this.objectRelMapCache.containsKey(startID)) {
                    this.objectRelMapCache.put(startID, new CopyOnWriteArrayList<>());
                }

                if (!this.objectRelMapCache.containsKey(endID)) {
                    this.objectRelMapCache.put(endID, new CopyOnWriteArrayList<>());
                }

                this.objectRelMapCache.get(startID).add(re);
                this.objectRelMapCache.get(endID).add(re);
            }

        } catch (JsonParseException e) {
            logger.error("Json parse exception. Error reloading relationship for {}", id, e);
        } catch (JsonMappingException e) {
            logger.error("Json mapping exception. Error reloading relationship for {}", id, e);
        } catch (IOException e) {
            logger.error("IO exception. Error reloading relationship for {}", id, e);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error reloading relationship for {}", id, e);
        }
    }

    private void removeRelationshipFromCache(long startID, long endID, int type) {
        getRelationships();
        List<JEVisRelationship> startRels = this.objectRelMapCache.get(startID);
        List<JEVisRelationship> endRels = this.objectRelMapCache.get(endID);

        try {
            for (JEVisRelationship rel : startRels) {
                if (rel.getStartID() == startID && rel.getEndID() == endID && rel.getType() == type) {
                    startRels.remove(rel);
                    break;
                }
            }

            for (JEVisRelationship rel : endRels) {
                if (rel.getStartID() == startID && rel.getEndID() == endID && rel.getType() == type) {
                    endRels.remove(rel);
                    break;
                }
            }

//            /**
//             * Workaround, i guess we dont want the relationship list but only the map but its there and in use
//             */
//            List<JEVisRelationship> all = getRelationships();
//            for (JEVisRelationship rel : getRelationships()) {
//                if (rel.getStartID() == startID && rel.getEndID() == endID && rel.getType() == type) {
//                    all.remove(rel);
//                    break;
//                }
//            }

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public List<JEVisRelationship> getRelationshipsWS() {
        logger.debug("Get ALL RelationshipsWS");
        //TODO: throw excption?! so the other function can handle it?
        List<JEVisRelationship> relationships = Collections.synchronizedList(new ArrayList<>());
        List<JsonRelationship> jsons = new ArrayList<>();
        try {

            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH;
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            jsons = new ArrayList<>(Arrays.asList(this.objectMapper.readValue(inputStream, JsonRelationship[].class)));
            inputStream.close();
        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error getting relationships. ", ex);
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error getting relationships. ", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error getting relationships. ", ex);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error getting relationships. ", e);
        }

        for (JsonRelationship jsonRelationship : jsons) {
            try {
                relationships.add(new JEVisRelationshipWS(JEVisDataSourceWS.this, jsonRelationship));

            } catch (Exception ex) {
                logger.error("Error in Relationship: {}", ex.getMessage());
            }
        }

        return relationships;
    }

    private List<JEVisAttribute> getAttributesFromWS(long objectID) {
        logger.debug("Get attribute from Webservice: {}", objectID);
        List<JEVisAttribute> attributes = new ArrayList<>();

//        StringBuffer response = new StringBuffer();
        try {
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH;
//                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
//            response = this.con.getRequest(resource);
//

            InputStream inputStream = this.con.getInputStreamRequest(resource);
            if (inputStream != null) {
                JsonAttribute[] jsons = this.objectMapper.readValue(inputStream, JsonAttribute[].class);
                inputStream.close();
                for (JsonAttribute att : jsons) {
                    try {
                        attributes.add(updateAttributeCache(att));
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }
                fixMissingAttributes(getObject(objectID), attributes);
            } else {
                logger.warn("Could not get attributes for object with id {}", objectID);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        return attributes;

    }

    private void fixMissingAttributes(JEVisObject object, List<JEVisAttribute> attributes) {
        try {
            if (object != null) {
                JEVisClass oClass = object.getJEVisClass();

                if (oClass != null && oClass.getTypes() != null) {
                    oClass.getTypes().forEach(jeVisType -> {
                        boolean exists = false;
                        try {
                            for (JEVisAttribute att : attributes) {
                                if (att.getName().equals(jeVisType.getName())) exists = true;
                            }
                            if (!exists) {
                                JsonAttribute json = new JsonAttribute();
                                json.setObjectID(object.getID());
                                json.setType(jeVisType.getName());
                                json.setPrimitiveType(jeVisType.getPrimitiveType());
                                json.setSampleCount(0);

                                JEVisAttribute newAttribute = new JEVisAttributeWS(this, json);
                                attributes.add(newAttribute);
                            }
                        } catch (Exception ex) {
                            logger.error(ex);
                        }

                    });
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private JEVisAttribute updateAttributeCache(JsonAttribute jSonAttribute) {
        if (this.attributeCache.containsKey(jSonAttribute.getObjectID())) {
            for (JEVisAttribute att : this.attributeCache.get(jSonAttribute.getObjectID())) {
                if (att.getName().equals(jSonAttribute.getType())) {
                    /**
                     * cast needs to be removed
                     */
                    ((JEVisAttributeWS) att).update(jSonAttribute);
                    logger.debug("Update existing att: {}.{}", jSonAttribute.getObjectID(), jSonAttribute.getType());
                    return att;
                }
            }
        } else {
            logger.trace("Create attribute list for : {}", jSonAttribute.getObjectID());
            this.attributeCache.put(jSonAttribute.getObjectID(), new ArrayList<>());
        }


        JEVisAttributeWS newAttribute = new JEVisAttributeWS(this, jSonAttribute);
        this.attributeCache.get(jSonAttribute.getObjectID()).add(newAttribute);
//        logger.debug("add new attribute: {}.{}", jSonAttribute.getObjectID(), jSonAttribute.getType());

        return newAttribute;


        //TODO: this should not happen in the normal workflow but we never know?


    }

    private void removeObjectFromRelationshipsCache(long objectID) {
        for (Map.Entry<Long, List<JEVisRelationship>> entry : objectRelMapCache.entrySet()) {
            List<JEVisRelationship> jeVisRelationships = entry.getValue();
            List<JEVisRelationship> toDelete = new ArrayList<>();
            for (JEVisRelationship jeVisRelationship : jeVisRelationships) {
                if (jeVisRelationship.getStartID() == objectID || jeVisRelationship.getEndID() == objectID) {
                    toDelete.add(jeVisRelationship);
                }
            }


            if (!toDelete.isEmpty()) jeVisRelationships.removeAll(toDelete);
        }
        if (objectRelMapCache.contains(objectID)) objectRelMapCache.remove(objectID);

    }

    private void removeObjectFromCache(long objectID) {
        try {

            JEVisObject object = this.objectCache.get(objectID);
            if (object != null) {
                List<Long> ids = new ArrayList<>();
                for (JEVisObject c : object.getChildren()) {
                    ids.add(c.getID());
                }
                for (Long id : ids) {
                    removeObjectFromCache(id);
                }

                this.objectCache.remove(objectID);
                removeObjectFromRelationshipsCache(objectID);
            }

            //reloadRelationships();//save but takes  a lot of time

        } catch (JEVisException | NullPointerException ne) {
            logger.error(ne);
        }

    }

    public synchronized void reloadRelationships() {
        logger.debug("reloadRelationships()");
        this.orLoaded = false;
        if (this.user != null) {
            this.user.reload();
        }
    }

    public JEVisObject buildObject(long parentID, String jclass, String name) throws JEVisException {
        logger.error("ds.buildObject: {} {} {}", parentID, jclass, name);
        Benchmark bm = new Benchmark();
        JEVisObject parent = getObject(parentID);
        JEVisClass newObjClass = getJEVisClass(jclass);
        JEVisObject newObj = parent.buildObject(name, newObjClass);
//        newObj.commit();
//
//        parent.notifyListeners(new JEVisEvent(parent, JEVisEvent.TYPE.OBJECT_NEW_CHILD, newObj));

        bm.printBechmark("done building object");
        return newObj;
    }

    public synchronized HTTPConnection getHTTPConnection() {
        return this.con;
    }

    public List<JEVisSample> getSamples(JEVisAttribute att, DateTime from, DateTime until, boolean customWorkDay, String aggregationPeriod, String manipulationMode, String timeZone) {
        logger.debug("Get  getSamples: {} {}-{} with aggregation {}, manipulation {} and in Timezone {}", att.getName(), from, until, aggregationPeriod, manipulationMode, timeZone);

        List<JEVisSample> samples = new ArrayList<>();
        String resource = REQUEST.API_PATH_V1
                + REQUEST.OBJECTS.PATH
                + att.getObjectID() + "/"
                + REQUEST.OBJECTS.ATTRIBUTES.PATH
                + att.getName() + "/"
                + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH;

        boolean isfirst = true;
        if (from != null) {

            resource += "?" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.FROM + HTTPConnection.FMT.print(from);
            isfirst = false;
        }
        if (until != null) {
            if (!isfirst) {
                resource += "&";
            } else {
                resource += "?";
            }
            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.UNTIL + HTTPConnection.FMT.print(until);
        }

        if (from != null && aggregationPeriod != null && manipulationMode != null && timeZone != null) {
            resource += "&";
            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.customWorkDay + customWorkDay;
            resource += "&";
            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.aggregationPeriod + aggregationPeriod;
            resource += "&";
            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.manipulationMode + manipulationMode;
            resource += "&";
            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.timeZone + timeZone;
        }

        List<JsonSample> jsons = new ArrayList<>();
        try {
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            if (inputStream != null) {
                jsons = new ArrayList<>(Arrays.asList(this.objectMapper.readValue(inputStream, JsonSample[].class)));
                inputStream.close();
            }
        } catch (IllegalArgumentException ex) {
            logger.error("Illegal argument exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (IOException ex) {
            logger.error("IO exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error in getting samples.", e);
        }

        for (JsonSample sample : jsons) {
            try {
                samples.add(new JEVisSampleWS(this, sample, att));
            } catch (Exception ex) {
                logger.error("Error parsing sample {} of attribute {}:{}", sample.toString(), att.getObject().getID(), att.getName());
            }
        }

        return samples;
    }

    public List<JEVisSample> getSamples(JEVisAttribute att, DateTime from, DateTime until) {
        logger.debug("Get  getSamples: {} {}-{}", att.getName(), from, until);

        List<JEVisSample> samples = new ArrayList<>();
        String resource = REQUEST.API_PATH_V1
                + REQUEST.OBJECTS.PATH
                + att.getObjectID() + "/"
                + REQUEST.OBJECTS.ATTRIBUTES.PATH
                + att.getName() + "/"
                + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH;

        boolean isfirst = true;
        if (from != null) {

            resource += "?" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.FROM + HTTPConnection.FMT.print(from);
            isfirst = false;
        }
        if (until != null) {
            if (!isfirst) {
                resource += "&";
            } else {
                resource += "?";
            }
            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.UNTIL + HTTPConnection.FMT.print(until);
        }
        String prefix = "&";
        if (!resource.contains("?")) {
            prefix = "?";
        }
        resource += prefix + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.LIMIT + "=" + SAMPLE_REQUEST_SIZE;

        List<JsonSample> jsons = new ArrayList<>();
        try {
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            if (inputStream != null) {
                jsons = new ArrayList<>(Arrays.asList(this.objectMapper.readValue(inputStream, JsonSample[].class)));
                inputStream.close();
            }
        } catch (IllegalArgumentException ex) {
            logger.error("Illegal argument exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (IOException ex) {
            logger.error("IO exception. Error in getting samples.", ex);
            return new ArrayList<>();
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error in getting samples.", e);
        }

        for (JsonSample sample : jsons) {
            try {
                samples.add(new JEVisSampleWS(this, sample, att));
            } catch (Exception ex) {
                logger.error("Error parsing sample {} of attribute {}:{}", sample.toString(), att.getObject().getID(), att.getName());
            }
        }

        if (!samples.isEmpty() && samples.size() == SAMPLE_REQUEST_SIZE) {
            JEVisSample lastInList = samples.get(samples.size() - 1);
            try {
                List<JEVisSample> nextList = getSamples(att, lastInList.getTimestamp().plus(Duration.standardSeconds(1)), until);
                logger.debug("Add additional samples: {}", nextList.size());
                samples.addAll(nextList);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        //logger.error("GetSamples: {} total: {} from/until {}....{}",att,samples.size(),from,until);
        return samples;
    }

    public void reloadClasses() {
        this.classLoaded = false;
        getJEVisClasses();
    }

    public List<JEVisObject> getRootObjectsWS() {

        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "?root=true";
            List<JEVisObject> objs = new ArrayList<>();
//            StringBuffer response = this.con.getRequest(resource);
//
//            //TODO: make an exception handling
//            if (response == null) {
//                return objs;
//            }
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            JsonObject[] jsons = this.objectMapper.readValue(inputStream, JsonObject[].class);
            inputStream.close();
//            Type listType = new TypeToken<List<JsonObject>>() {
//            }.getType();
//            List<JsonObject> jsons = this.gson.fromJson(response.toString(), listType);

            for (JsonObject jobj : jsons) {
                try {
                    objs.add(new JEVisObjectWS(this, jobj));
                } catch (Exception ex) {

                }

            }

            return objs;

        } catch (ProtocolException ex) {
            logger.fatal(ex);
        } catch (IOException ex) {
            logger.fatal(ex);
        } catch (InterruptedException e) {
            logger.fatal(e);
        }
        return new ArrayList<>();
    }

    public List<JEVisObject> getObjectsWS(JEVisClass jevisClass, boolean addheirs) throws JEVisException {

        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "?root=false&class=" + jevisClass.getName();
            if (addheirs) {
                resource += "&inherit=true";
            }

            List<JEVisObject> objs = new ArrayList<>();
//            StringBuffer response = this.con.getRequest(resource);
//
//            //TODO: make an exception handling
//            if (response == null) {
//                return objs;
//            }

            InputStream inputStream = this.con.getInputStreamRequest(resource);
            JsonObject[] jsons = this.objectMapper.readValue(inputStream, JsonObject[].class);
            inputStream.close();

            for (JsonObject jobj : jsons) {
                try {
                    objs.add(new JEVisObjectWS(this, jobj));
                } catch (Exception ex) {
                    logger.error(ex);
                }

            }

            return objs;

        } catch (ProtocolException ex) {
            logger.fatal(ex);
        } catch (IOException ex) {
            logger.fatal(ex);
        } catch (InterruptedException e) {
            logger.fatal(e);
        }
        return new ArrayList<>();
    }

    /**
     * @param jclass
     * @return
     */
    public BufferedImage getClassIcon(String jclass) {
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASS_ICONS.PATH;
        try {

            String tmpdir = System.getProperty("java.io.tmpdir");
            File zipDir = new File(tmpdir + "/JEVisCC/");

            ClassIconHandler cih = new ClassIconHandler(zipDir);
            if (!cih.fileExists()) {
                InputStream inputStream = this.con.getInputStreamRequest(resource);
                cih.readStream(inputStream);
                inputStream.close();
            }
            if (cih.getClassIcon(jclass) == null) {
//                SwingFXUtils.fromFXImage(JEVisClassWS.getImage("1472562626_unknown.png", 60, 60).getImage(), null);
                return getClassIconFromWS(jclass);//fallback
            } else {
                return cih.getClassIcon(jclass);
            }

        } catch (ProtocolException ex) {
            logger.error("Protocol exception. Error while fetching class icon for {}", jclass, ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error while fetching class icon for {}", jclass, ex);
        } catch (Exception ex) {
            logger.error("Exception. Error while fetching class icon for {}", jclass, ex);
        }
        return null;
    }

    public Map<String, BufferedImage> getClassIcon() {
        logger.debug("TMP dir: {}", System.getProperty("java.io.tmpdir"));
        Map<String, BufferedImage> icons = new HashMap<>();
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASS_ICONS.PATH;
        try {

            String tmpdir = System.getProperty("java.io.tmpdir");
            File zipDir = new File(tmpdir + "/JEVisCC/ClassIcons/");

            if (!zipDir.exists()) {
                zipDir.mkdirs();
            }
            ClassIconHandler cih = new ClassIconHandler(zipDir);
            if (Objects.requireNonNull(zipDir.listFiles()).length == 0) {
                InputStream inputStream = this.con.getInputStreamRequest(resource);
                cih.readStream(inputStream);
                inputStream.close();
            }

            return cih.getClassIcon();

        } catch (ProtocolException ex) {
            logger.error("Protocol exception. Error while fetching class icons", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error while fetching class icons", ex);
        } catch (Exception ex) {
            logger.error("Exception. Error while fetching class icons", ex);
        }
        return null;
    }

    public BufferedImage getClassIconFromWS(String jclass) {
        logger.debug("getClassIcon: {}", jclass);
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASSES.PATH + jclass + "/" + REQUEST.CLASSES.ICON.PATH;
        try {

            return this.con.getIconRequest(resource);

        } catch (ProtocolException ex) {
            logger.error("Protocol exception. Error while fetching class icon for {}", jclass, ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error while fetching class icon for {}", jclass, ex);
        } catch (Exception ex) {
            logger.error("Exception. Error while fetching class icon for {}", jclass, ex);
        }
        return null;
    }

    public JEVisObject getObjectWS(long id) {
        logger.debug("GetObject: {}", id);
        String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "/" + id
                + "?"
                + REQUEST.OBJECTS.OPTIONS.INCLUDE_CHILDREN + "true";
        try {

            InputStream inputStream = this.con.getInputStreamRequest(resource);
            JsonObject json = null;
            if (inputStream != null) {
                json = this.objectMapper.readValue(inputStream, JsonObject.class);
                inputStream.close();
            }

            if (json != null && this.objectCache.containsKey(json.getId())) {
                ((JEVisObjectWS) this.objectCache.get(json.getId())).update(json);
                return this.objectCache.get(json.getId());
            } else if (json != null) {
                JEVisObject obj = new JEVisObjectWS(this, json);
                this.objectCache.put(obj.getID(), obj);
                return obj;
            } else throw new Exception("InputStream is null, object of id " + id + " does not seem to exist");

        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error while fetching Object: {}", id, ex);
        } catch (JsonMappingException ex) {
            logger.error("Object is not accessible: {}", id, ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error while fetching Object: {}", id, ex);
        } catch (Exception ex) {
            logger.error("Unexpected exception while fetching Object: {}, reason: {}", id, ex.getMessage());
        }
        return null;
    }

    public JEVisClass getJEVisClassWS(String name) {
        logger.trace("GetClass: {}", name);
        try {

            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES + "/" + name;
//            StringBuffer response = this.con.getRequest(resource);

            InputStream inputStream = this.con.getInputStreamRequest(resource);
            JsonJEVisClass json = this.objectMapper.readValue(inputStream, JsonJEVisClass.class);
            inputStream.close();

            return new JEVisClassWS(this, json);

        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error getting jevis {} class from ws.", name, ex);
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error getting jevis {} class from ws.", name, ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error getting jevis {} class from ws.", name, ex);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error getting jevis {} class from ws.", name, e);
        }
        return null;
    }

    public List<JEVisClass> getJEVisClassesWS() {

        String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES;
        List<JEVisClass> classes = new ArrayList<>();
        List<JsonJEVisClass> jsons = new ArrayList<>();

        try {
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            jsons = new ArrayList<>(Arrays.asList(this.objectMapper.readValue(inputStream, JsonJEVisClass[].class)));
            inputStream.close();
        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error getting classes from ws.", ex);
        } catch (JsonMappingException ex) {
            logger.error("Json mapping exception. Error getting classes from ws.", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error getting classes from ws.", ex);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception. Error getting classes from ws.", e);
        }

        for (JsonJEVisClass jc : jsons) {
            try {
                classes.add(new JEVisClassWS(this, jc));
            } catch (Exception e) {
                logger.error("Exception. Error parsing jevisclass {}", jc.toString(), e);
            }
        }

        return classes;
    }

    public boolean confirmPassword(String username, String password) throws JEVisException {
        HTTPConnection httpConnection = new HTTPConnection(this.host, username, password, sslTrustMode);


        try {
            String resource
                    = REQUEST.API_PATH_V1
                    + REQUEST.JEVISUSER.PATH;

            HttpURLConnection conn = httpConnection.getGetConnection(resource);

            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;

        } catch (Exception ex) {
            logger.catching(ex);
            throw new JEVisException(ex.getMessage(), 402, ex);
        }
    }

    public void preloadClasses() {
        if (this.hasPreloaded) return;
        try {
            getJEVisClasses();
        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    public void preloadRelationships() {
        if (this.hasPreloaded) return;
        try {
            this.getRelationships();
        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    public void preloadObjects() {
        if (this.hasPreloaded) return;
        try {
            if (!this.objectCache.isEmpty()) this.objectCache.clear();
            getObjects();
        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    public void preloadAttributes() {
        if (this.hasPreloaded) return;
        try {
            if (!this.attributeCache.isEmpty()) this.attributeCache.clear();
            getAttributes();
            this.hasPreloaded = true;
        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    public List<JsonI18nClass> getTranslation() {
        logger.trace("Get I18n");
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_I18N;//+"?jclass="+Organisation;
//            StringBuffer response = this.con.getRequest(resource);
//
//            logger.trace("raw response: '{}'", response.toString());

            return new ArrayList<>(Arrays.asList(this.objectMapper.readValue(this.con.getInputStreamRequest(resource), JsonI18nClass[].class)));
//            Type listType = new TypeToken<List<JsonI18nClass>>() {
//            }.getType();
//            return this.gson.fromJson(response.toString(), listType);

        } catch (Exception ex) {
            logger.fatal(ex);
            return new ArrayList<>();
        }

    }

    public void addToObjectCache(JEVisObject obj) {
        this.objectCache.put(obj.getID(), obj);
    }

    /* removed from WS because its not working wiht the dataprocessor workflow
    public List<JEVisObject> getDataProcessorTask() {
        logger.debug("updateAccessControl");
        String resource = HTTPConnection.API_PATH_V1 + "/task/dataprocessor";
        try {


            List<JsonObject> jsonObjects = new ArrayList<>();
            InputStream inputStream = this.con.getInputStreamRequest(resource);
            jsonObjects = Arrays.asList(this.objectMapper.readValue(inputStream, JsonObject[].class));
            inputStream.close();

            List<JEVisObject> objects = new ArrayList<>();
            jsonObjects.forEach(jsonObject -> {
                JEVisObjectWS newObject = new JEVisObjectWS(this, jsonObject);
                objects.add(newObject);
            });

            return objects;


        } catch (JsonParseException ex) {
            logger.error("Json parse exception. Error while fetching Object: {}", ex);
        } catch (JsonMappingException ex) {
            logger.error("Object is not accessible: {}", ex);
        } catch (IOException ex) {
            logger.error("IO exception. Error while fetching Object: {}", ex);
        } catch (Exception ex) {
            logger.error("Unexpected exception while fetching Object: {}, reason: {}", ex.getMessage());
        }
        return new ArrayList<>();
    }
    */

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
}
