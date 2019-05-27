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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.utils.Benchmark;
import org.jevis.commons.utils.Optimization;
import org.jevis.commons.ws.json.*;
import org.joda.time.DateTime;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private String host = "http://localhost";
    private HTTPConnection con;
    private Gson gson = new Gson();
    //    private JEVisObject currentUser = null;
    private boolean fetchedAllClasses = false;
    private JEVisUser user;
    private List<JEVisOption> config = new ArrayList<>();
    //    private Cache<Integer, List> relationshipCache;
    private List<JEVisRelationship> objectRelCache = Collections.synchronizedList(new ArrayList<JEVisRelationship>());
    private ConcurrentHashMap<String, JEVisClass> classCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, JEVisObject> objectCache = new ConcurrentHashMap<>();
    //    private Map<Long, List<JEVisAttribute>> attributeMapCache = Collections.synchronizedMap(new HashMap<Long, List<JEVisAttribute>>());
    private ConcurrentHashMap<Long, List<JEVisRelationship>> objectRelMapCache = new ConcurrentHashMap<>();
    private boolean allAttributesPreloaded = false;
    private boolean classLoaded = false;
    private boolean objectLoaded = false;
    private boolean orLoaded = false;
    private ConcurrentHashMap<Long, List<JEVisAttribute>> attributeCache = new ConcurrentHashMap<>();

    public JEVisDataSourceWS(String host) {
        this.host = host;

    }

    public JEVisDataSourceWS() {
    }

    @Override
    public void init(List<JEVisOption> config) throws IllegalArgumentException {
        logger.info("Start JEVisDataSourceWS Version: " + this.info.getVersion());

    }

    public List<JEVisType> getTypes(JEVisClassWS jclass) {

        try {
            String resource = HTTPConnection.API_PATH_V1
                    + HTTPConnection.RESOURCE_CLASSES + "/" + jclass.getName()
                    + "/" + HTTPConnection.RESOURCE_TYPES;

            StringBuffer response = this.con.getRequest(resource);

            Type listType = new TypeToken<List<JsonType>>() {
            }.getType();
            List<JsonType> jsons = this.gson.fromJson(response.toString(), listType);
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
        }
        return new ArrayList<>();
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


    /**
     * Load all attributes for all objects.
     *
     * @return
     * @throws JEVisException
     */
    @Override
    public List<JEVisAttribute> getAttributes() throws JEVisException {
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
                List<JsonAttribute> jsons = Arrays.asList(this.objectMapper.readValue(this.con.getInputStreamRequest(resource), JsonAttribute[].class));
//                JsonAttribute[] myObjects = objectMapper.readValue(con.getInputStreamRequest(resource), JsonAttribute[].class);


//                Type listType = new TypeToken<List<JsonAttribute>>() {
//                }.getType();
//                List<JsonAttribute> jsons = gson.fromJson(response.toString(), listType);
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
                 * 1. use fixMissingAttributes(attributes); fi fix missing attribes
                 * 2. is the function belov oK?
                 */

                /**
                 * Give objects which have no attributes an empty list
                 */
                this.objectCache.keySet().parallelStream().forEach(aLong -> {
                    if (!this.attributeCache.containsKey(aLong)) {
                        this.attributeCache.put(aLong, new ArrayList<>());
                    }
                    fixMissingAttributes(getObject(aLong), this.attributeCache.get(aLong));

                });
                logger.debug("done fixing attributes");

                this.allAttributesPreloaded = true;

                return attributeList;
            }

            /**
             * Load from cache
             */
            Collection<List<JEVisAttribute>> attributes = this.attributeCache.values();
            List<JEVisAttribute> result = new ArrayList<>();
            attributes.forEach(result::addAll);
//                System.out.println("end get attributes");
            return result;

        } catch (ProtocolException ex) {
            logger.error(ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (Exception ex) {
            logger.error(ex);
            return new ArrayList<>();
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

            logger.debug("payload: {}", this.gson.toJson(newJsonRel));
            StringBuffer response = getHTTPConnection().postRequest(resource, this.gson.toJson(newJsonRel));

            JsonRelationship newJson = this.gson.fromJson(response.toString(), JsonRelationship.class);
            JEVisRelationship newRel = new JEVisRelationshipWS(this, newJson);

            if ((newRel.getType() >= JEVisConstants.ObjectRelationship.MEMBER_READ
                    && newRel.getType() <= JEVisConstants.ObjectRelationship.MEMBER_DELETE) || newRel.getType() == JEVisConstants.ObjectRelationship.OWNER) {
                getCurrentUser().reload();
            }


            this.objectRelMapCache.get(newRel.getStartID()).add(newRel);
            this.objectRelMapCache.get(newRel.getEndID()).add(newRel);

            return newRel;

        } catch (Exception ex) {
            logger.catching(ex);
            return null;//TODO throw error
        }

    }

    @Override
    public List<JEVisObject> getObjects() {
        logger.debug("getObjects");
        if (!this.objectLoaded) {
            getObjectsWS();
            this.objectLoaded = true;
        }

        return new ArrayList<>(this.objectCache.values());

    }

    private void updateObject(JsonObject jsonObj) {
        Long id = jsonObj.getId();
        if (this.objectCache.containsKey(id)) {
            /**
             * cast needs to be removed
             */
            this.objectCache.remove(id);
            JEVisObjectWS newOBject = new JEVisObjectWS(this, jsonObj);
            this.objectCache.put(newOBject.getID(), newOBject);
            //((JEVisObjectWS) objectCache.get(id)).update(jsonObj);
        } else {
            JEVisObjectWS newOBject = new JEVisObjectWS(this, jsonObj);
            this.objectCache.put(newOBject.getID(), newOBject);
        }
        //this.reloadAttribute(objectCache.get(id));

    }

    public void getObjectsWS() {
        logger.trace("Get ALL ObjectsWS");
        try {
            List<JEVisObject> objects = new ArrayList<>();
            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS + "false"
                    + "&" + REQUEST.OBJECTS.OPTIONS.ONLY_ROOT + "false";


            List<JsonObject> jsons = Arrays.asList(this.objectMapper.readValue(this.con.getInputStreamRequest(resource), JsonObject[].class));

//            StringBuffer response = con.getRequest(resource);
//            Type listType = new TypeToken<List<JsonObject>>() {
//            }.getType();
//            List<JsonObject> jsons = gson.fromJson(response.toString(), listType);
            logger.debug("JsonObject.count: {}", jsons.size());
            for (JsonObject obj : jsons) {
                try {
                    updateObject(obj);
                } catch (Exception ex) {
                    logger.error("Error while parsing object: {}", ex.getMessage());
                }
            }


        } catch (ProtocolException ex) {
            logger.error(ex);
            //TODO: throw excption?! so the other function can handel it?
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Override
    public JEVisUser getCurrentUser() {
        return this.user;

    }

    @Override
    public List<JEVisRelationship> getRelationships() {
        logger.debug("getRelationships");
        if (!this.orLoaded) {

            this.objectRelMapCache.clear();
            this.objectRelCache = getRelationshipsWS();


            this.objectRelCache.parallelStream().forEach(re -> {
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

        return this.objectRelCache;
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

            /**
             * Workaround, i guess we dont want the relationship list but only the map but its there and in use
             */
            List<JEVisRelationship> all = getRelationships();
            for (JEVisRelationship rel : getRelationships()) {
                if (rel.getStartID() == startID && rel.getEndID() == endID && rel.getType() == type) {
                    all.remove(rel);
                    break;
                }
            }

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public List<JEVisRelationship> getRelationships(long objectID) {
        if (!this.orLoaded) {
            getRelationships();
        }

        return this.objectRelMapCache.get(objectID);

    }

    public List<JEVisRelationship> getRelationshipsWS() {
        logger.debug("Get ALL RelationshipsWS");
        try {
//            Benchmark bm = new Benchmark();
            List<JEVisRelationship> relationships = Collections.synchronizedList(new ArrayList<>());
            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH;
//                                  + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
//            StringBuffer response = con.getRequest(resource);
//
//            Type listType = new TypeToken<List<JsonRelationship>>() {
//            }.getType();
//            List<JsonRelationship> jsons = gson.fromJson(response.toString(), listType);

//            ObjectMapper objectMapper = new ObjectMapper();
            List<JsonRelationship> jsons = Arrays.asList(this.objectMapper.readValue(this.con.getInputStreamRequest(resource), JsonRelationship[].class));

            jsons.parallelStream().forEach(jsonRelationship -> {
                try {
                    if (jsonRelationship != null) {
//                        if (jsonRelationship.getType() == JEVisConstants.ObjectRelationship.PARENT) {
//                            System.out.print(".");
//                        } else if (jsonRelationship.getType() >= 100) {
//                            System.out.print(",");
//                        }
//
//                        if (jsonRelationship.getFrom() == 3 || jsonRelationship.getTo() == 3) {
//                            System.out.println("debug hook");
//                        }

                        relationships.add(new JEVisRelationshipWS(JEVisDataSourceWS.this, jsonRelationship));
                    }
                } catch (Exception ex) {
                    logger.error("Error in Relationship: {}", ex.getMessage());
                }
            });

//            bm.printBenchmarkDetail("Time to get Relationships");
            return relationships;

        } catch (ProtocolException ex) {
            logger.error(ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            logger.error(ex);
            return new ArrayList<>();
        }

    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships(String jclass) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            logger.warn("Reload Attribute: {}", object);
            getAttributesFromWS(object.getID());
        } catch (Exception ex) {
            logger.error("Error, can not reload attribute", ex);
        }
    }


    private List<JEVisAttribute> getAttributesFromWS(long objectID) {
        logger.debug("Get attribute from Webservice: {}", objectID);
        List<JEVisAttribute> attributes = new ArrayList<>();

        StringBuffer response = new StringBuffer();
        try {
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH;
//                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
            response = this.con.getRequest(resource);

            if (response == null || response.toString().isEmpty()) {
                logger.debug("Empty response for {}.attributes ", objectID);
                return new ArrayList<>();
            }

            Type listType = new TypeToken<List<JsonAttribute>>() {
            }.getType();
            List<JsonAttribute> jsons = this.gson.fromJson(response.toString(), listType);
            for (JsonAttribute att : jsons) {
                try {
                    attributes.add(updateAttributeCache(att));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
            fixMissingAttributes(getObject(objectID), attributes);

        } catch (Exception ex) {
            logger.error(ex);
        }

        return attributes;

    }

    private void fixMissingAttributes(JEVisObject object, List<JEVisAttribute> attributes) {
        try {

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
            logger.debug("Create attribute list for : {}", jSonAttribute.getObjectID());
            this.attributeCache.put(jSonAttribute.getObjectID(), new ArrayList<>());
        }


        JEVisAttributeWS newAttribute = new JEVisAttributeWS(this, jSonAttribute);
        this.attributeCache.get(jSonAttribute.getObjectID()).add(newAttribute);
        logger.debug("add new attribute: {}.{}", jSonAttribute.getObjectID(), jSonAttribute.getType());

        return newAttribute;


        //TODO: this should not happen in the normal workflow but we never know?


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

    private void removeObjectFromCache(long objectID) {
        try {
            this.objectCache.remove(objectID);

            JEVisObject object = getObject(objectID);
            List<Long> ids = new ArrayList<>();
            for (JEVisObject c : object.getChildren()) {
                ids.add(c.getID());
            }
            for (Long id : ids) {
                removeObjectFromCache(id);
            }
            reloadRelationships();//takes 30ms

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

    @Override
    public boolean deleteObject(long objectID) {
        try {
            logger.debug("Delete: {}", objectID);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID;

            JEVisObject object = getObject(objectID);

            if (object != null) {
                HttpURLConnection response = getHTTPConnection().getDeleteConnection(resource);
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    reloadRelationships();
                    removeObjectFromCache(objectID);

                    object.getParents().forEach(parent -> {
                        parent.notifyListeners(new JEVisEvent(parent, JEVisEvent.TYPE.OBJECT_CHILD_DELETED, object));
                    });


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

            Gson gson = new Gson();
            HttpURLConnection conn = getHTTPConnection().getDeleteConnection(resource);

            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;

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


            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;

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
    public List<JEVisOption> getConfiguration() {
        return this.config;
    }

    @Override
    public void setConfiguration(List<JEVisOption> config) {
        this.config = config;
        for (JEVisOption opt : config) {
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                this.host = opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue();
            }
        }
    }

    public JEVisObject buildObject(long parentID, String jclass, String name) throws JEVisException {
        logger.error("ds.buildObject: {} {} {}", parentID, jclass, name);
        JEVisObject parent = getObject(parentID);
        JEVisClass newObjClass = getJEVisClass(jclass);
        JEVisObject newObj = parent.buildObject(name, newObjClass);
        newObj.commit();

        parent.notifyListeners(new JEVisEvent(parent, JEVisEvent.TYPE.OBJECT_NEW_CHILD, newObj));

        return newObj;
    }

    public synchronized HTTPConnection getHTTPConnection() {
        return this.con;
    }

    public List<JEVisSample> getSamples(JEVisAttribute att, DateTime from, DateTime until) {

        logger.debug("Get  getSamples: {} {}-{}", att.getName(), from, until);
        try {
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

//            StringBuffer response = con.getRequest(resource);
//
//            Type listType = new TypeToken<List<JsonSample>>() {
//            }.getType();
//            List<JsonSample> jsons = gson.fromJson(response.toString(), listType);

            //            ObjectMapper objectMapper = new ObjectMapper();
            JsonSample[] jsons = this.objectMapper.readValue(this.con.getInputStreamRequest(resource), JsonSample[].class);


            for (JsonSample sample : jsons) {
//                logger.trace("New rel: " + rel);
                try {
                    samples.add(new JEVisSampleWS(this, sample, att));
                } catch (Exception ex) {

                }
            }

            return samples;

        } catch (ProtocolException ex) {
            logger.fatal(ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            logger.fatal(ex);
            return new ArrayList<>();
        }
    }

    public int addSamples(JEVisAttribute att, List<JEVisSample> samples) {
        //TODO
        return 0;
    }

    @Override
    public JEVisClass buildClass(String name) {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(name);

        return new JEVisClassWS(this, json);
    }

    public void reloadClasses() {
        this.classLoaded = false;
        getJEVisClasses();
    }

    @Override
    public JEVisObject buildLink(String name, JEVisObject parent, JEVisObject linkedObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisObject> getRootObjects() {
        /**
         * We now load the list from the cache to improve performance
         */
        List<JEVisObject> objs = new ArrayList<>();


        List<Long> groupIds = new ArrayList<>();
        for (JEVisRelationship rel : this.objectRelCache) {
            try {
                if (rel.isType(JEVisConstants.ObjectRelationship.MEMBER_READ)
                        && getCurrentUser().getUserID() == rel.getStartID()) {

                    groupIds.add(rel.getEndID());
                }
            } catch (Exception ex) {

            }
        }

        for (JEVisRelationship rel : this.objectRelCache) {
            try {
                //group to root
                if (rel.isType(JEVisConstants.ObjectRelationship.ROOT) && groupIds.contains(rel.getStartID())) {
                    objs.add(rel.getEndObject());
                }
            } catch (Exception ex) {

            }
        }


        return objs;

    }

    public List<JEVisObject> getRootObjectsWS() {

        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "?root=true";
            List<JEVisObject> objs = new ArrayList<>();
            StringBuffer response = this.con.getRequest(resource);

            //TODO: make an exception handling
            if (response == null) {
                return objs;
            }

            Type listType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<JsonObject> jsons = this.gson.fromJson(response.toString(), listType);

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
        }
        return new ArrayList<>();
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
            if (filterClass.contains(obj.getJEVisClass())) {
                objs.add(obj);
            }
        }


        return objs;
    }


    public List<JEVisObject> getObjectsWS(JEVisClass jevisClass, boolean addheirs) throws JEVisException {

        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "?root=false&class=" + jevisClass.getName();
            if (addheirs) {
                resource += "&inherit=true";
            }

            List<JEVisObject> objs = new ArrayList<>();
            StringBuffer response = this.con.getRequest(resource);

            //TODO: make an exception handling
            if (response == null) {
                return objs;
            }

            Type listType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<JsonObject> jsons = this.gson.fromJson(response.toString(), listType);

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
                cih.readStream(this.con.getInputStreamRequest(resource));
            }
            if (cih.getClassIcon(jclass) == null) {
//                SwingFXUtils.fromFXImage(JEVisClassWS.getImage("1472562626_unknown.png", 60, 60).getImage(), null);
                return getClassIconFormWS(jclass);//fallback
            } else {
                return cih.getClassIcon(jclass);
            }

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (Exception ex) {
            logger.fatal(ex);
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
                cih.readStream(this.con.getInputStreamRequest(resource));
            }

            return cih.getClassIcon();

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (Exception ex) {
            logger.fatal(ex);
        }
        return null;
    }

    public BufferedImage getClassIconFormWS(String jclass) {
        logger.debug("getClassIcon: {}", jclass);
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASSES.PATH + jclass + "/" + REQUEST.CLASSES.ICON.PATH;
        try {

            return this.con.getIconRequest(resource);

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        }
        return null;
    }

    @Override
    public JEVisObject getObject(Long id) {
//        System.out.println("getObject: " + id);
        if (this.objectLoaded) {
            JEVisObject obj = this.objectCache.getOrDefault(id, null);
            if (obj == null) {
                logger.warn("Warning: Request for object {} was null", id);
            }
            return obj;
        } else if (this.objectCache.get(id) != null) {
            return this.objectCache.get(id);
        }

        return getObjectWS(id);

    }

    public JEVisObject getObjectWS(Long id) {
        logger.debug("GetObject: {}", id);
        String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "/" + id
                + "?"
                + REQUEST.OBJECTS.OPTIONS.INCLUDE_CHILDREN + "true";
        try {

            StringBuffer response = this.con.getRequest(resource);
            JsonObject json = this.gson.fromJson(response.toString(), JsonObject.class);


            if (this.objectCache.containsKey(json.getId())) {
                /**
                 * cast needs to be removed
                 */
                ((JEVisObjectWS) this.objectCache.get(json.getId())).update(json);
                return this.objectCache.get(json.getId());
            } else {
                JEVisObject obj = new JEVisObjectWS(this, json);
                this.objectCache.put(obj.getID(), obj);
                return obj;
            }

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        }
        return null;
    }

    @Override
    public JEVisClass getJEVisClass(String name) {
        //for now we assume classes are allways cached
        if (this.classCache.isEmpty()) {
            getJEVisClasses();
        }

        return this.classCache.get(name);

    }

    public JEVisClass getJEVisClassWS(String name) {
        logger.trace("GetClass: {}", name);
        try {

            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES + "/" + name;
            StringBuffer response = this.con.getRequest(resource);

            JsonJEVisClass json = this.gson.fromJson(response.toString(), JsonJEVisClass.class);
            return new JEVisClassWS(this, json);

        } catch (ProtocolException ex) {
            logger.fatal(ex);
        } catch (IOException ex) {
            logger.fatal(ex);
        }
        return null;
    }

    @Override
    public List<JEVisClass> getJEVisClasses() {
        if (!this.classLoaded) {

            getJEVisClassesWS().parallelStream().forEach(jclass -> {
                try {
                    this.classCache.put(jclass.getName(), jclass);
                } catch (Exception ex) {

                }
            });
            this.classLoaded = true;
        }

        return new ArrayList<>(this.classCache.values());
    }

    public List<JEVisClass> getJEVisClassesWS() {
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES;
            List<JEVisClass> classes = new ArrayList<>();
//            StringBuffer response = con.getRequest(resource);
//            if (response == null) {
//                return new ArrayList<>();//hmmm not the best solutuin
//            }

//            Type listType = new TypeToken<List<JsonJEVisClass>>() {
//            }.getType();
//            List<JsonJEVisClass> jsons = gson.fromJson(response.toString(), listType);

            JsonJEVisClass[] jsons = this.objectMapper.readValue(this.con.getInputStreamRequest(resource), JsonJEVisClass[].class);


            for (JsonJEVisClass jc : jsons) {

                JEVisClass newClass = new JEVisClassWS(this, jc);
                classes.add(newClass);
            }

            return classes;

        } catch (ProtocolException ex) {
            logger.fatal(ex);
        } catch (IOException ex) {
            logger.fatal(ex);
        }
        return new ArrayList<>();
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

    public boolean confirmPassword(String username, String password) throws JEVisException {
        HTTPConnection con = new HTTPConnection(this.host, username, password);
        try {
            String resource
                    = REQUEST.API_PATH_V1
                    + REQUEST.JEVISUSER.PATH;

            HttpURLConnection conn = getHTTPConnection().getGetConnection(resource);
            logger.debug("Login Response: {}", conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                logger.debug("Login response: {}", conn.getContent().toString());
                String payload = IOUtils.toString((InputStream) conn.getContent(), "UTF-8");

                JsonObject json = this.gson.fromJson(payload, JsonObject.class);

                this.user = new JEVisUserWS(this, new JEVisObjectWS(this, json)); //TODO: implement
                logger.trace("User.object: " + this.user.getUserObject());
                return true;
            } else {
                logger.error("Login failed: [{}] {}", conn.getResponseCode(), conn.getResponseMessage());
                return false;
            }

        } catch (Exception ex) {
            logger.catching(ex);
            throw new JEVisException(ex.getMessage(), 402, ex);
        }
    }

    @Override
    public boolean connect(String username, String password) throws JEVisException {
        logger.debug("Connect with user {} to: {}", username, this.host);

        //TODO implement config paramter to set trustAllCertificates
        HTTPConnection.trustAllCertificates();
        this.con = new HTTPConnection(this.host, username, password);


        try {
            String resource
                    = REQUEST.API_PATH_V1
                    + REQUEST.JEVISUSER.PATH;

            HttpURLConnection conn = getHTTPConnection().getGetConnection(resource);
            logger.debug("Login Response: {}", conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                logger.debug("Login response: {}", conn.getContent().toString());
                String payload = IOUtils.toString((InputStream) conn.getContent(), "UTF-8");

                JsonObject json = this.gson.fromJson(payload, JsonObject.class);

                this.user = new JEVisUserWS(this, new JEVisObjectWS(this, json)); //TODO: implement
                logger.trace("User.object: " + this.user.getUserObject());
                return true;
            } else {
                logger.error("Login failed: [{}] {}", conn.getResponseCode(), conn.getResponseMessage());
                return false;
            }

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
        String resource = "api/rest/version";
        try {
            URL url = new URL(this.host + "/" + resource);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(1000);
            conn.setRequestMethod("GET");

            logger.debug("HTTP request {}", conn.getURL());

            conn.connect();
            int responseCode = conn.getResponseCode();


            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<JEVisUnit> getUnits() {
        //TODo: implement
        return new ArrayList<>();
    }


    @Override
    public void preload() {
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
            if (!this.objectCache.isEmpty()) this.objectCache.clear();
            getObjects();
            benchmark.printBenchmarkDetail("Preload - Objects");
            Optimization.getInstance().printStatistics();
            if (!this.attributeCache.isEmpty()) this.attributeCache.clear();
            getAttributes();
            benchmark.printBenchmarkDetail("Preload - Attributes");
            Optimization.getInstance().printStatistics();

        } catch (Exception ex) {
            logger.error("Error while preloading data source", ex);
        }
    }

    public List<JsonI18nClass> getTranslation() {
        logger.trace("Get I18n");
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.REAOURCE_I18N;//+"?jclass="+Organisation;
            StringBuffer response = this.con.getRequest(resource);

            logger.trace("raw response: '{}'", response.toString());

            Type listType = new TypeToken<List<JsonI18nClass>>() {
            }.getType();
            return this.gson.fromJson(response.toString(), listType);

        } catch (Exception ex) {
            logger.fatal(ex);
            return new ArrayList<>();
        }

    }

    public void addToObjectCache(JEVisObject obj) {
        this.objectCache.put(obj.getID(), obj);
    }

    @Override
    public void reloadObject(JEVisObject object) {
        JEVisObject newObj = getObjectWS(object.getID());


    }

    @Override
    public void clearCache() {
//        classCache.clear();

        this.objectRelMapCache.clear();
        this.objectRelCache.clear();
        this.objectCache.clear();
        this.attributeCache.clear();

        this.allAttributesPreloaded = false;
        this.classLoaded = false;
        this.objectLoaded = false;
        this.orLoaded = false;

        System.gc();
        Optimization.getInstance().clearCache();
    }
}
