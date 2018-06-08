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

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.utils.Benchmark;
import org.jevis.commons.ws.json.*;
import org.joda.time.DateTime;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fs
 */
public class JEVisDataSourceWS implements JEVisDataSource {

    private String host = "http://localhost";
    private final int port = 8080;
    private HTTPConnection con;
    private Gson gson = new Gson();
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisDataSourceWS.class);
    private JEVisObject currentUser = null;
    private boolean fetchedAllClasses = false;
    private JEVisUser user;
    private List<JEVisOption> config = new ArrayList<>();

    //    private Cache<Integer, List> relationshipCache;
    private List<JEVisRelationship> objectRelCache = Collections.synchronizedList(new ArrayList<JEVisRelationship>());
    private Map<String, JEVisClass> classCache = Collections.synchronizedMap(new HashMap<String, JEVisClass>());
    private Map<Long, JEVisObject> objectCache = Collections.synchronizedMap(new HashMap<Long, JEVisObject>());
    private Map<Long, List<JEVisRelationship>> objectRelMapCache = Collections.synchronizedMap(new HashMap<Long, List<JEVisRelationship>>());
    private boolean classLoaded = false;
    private boolean objectLoaded = false;
    private boolean orLoaded = false;

    public JEVisDataSourceWS(String host) {
        this.host = host;

    }

    public JEVisDataSourceWS() {
    }

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

    @Override
    public void init(List<JEVisOption> config) throws IllegalArgumentException {
        logger.info("Start JEVisDataSourceWS Version: " + info.getVersion());

    }

    public List<JEVisType> getTypes(JEVisClassWS jclass) {

        try {
            String resource = HTTPConnection.API_PATH_V1
                    + HTTPConnection.RESOURCE_CLASSES + "/" + jclass.getName()
                    + "/" + HTTPConnection.RESOURCE_TYPES;

            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonType>>() {
            }.getType();
            List<JsonType> jsons = gson.fromJson(response.toString(), listType);
            List<JEVisType> types = new ArrayList<>();
            for (JsonType type : jsons) {
//                logger.trace("Type: {}", type);
                types.add(new JEVisTypeWS(this, type, jclass.getName()));
            }

            return types;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: throw excption?! so the other function can handel it?
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JEVisException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    /**
     * @param fromClass
     * @param toClass
     * @param type
     * @return
     * @throws JEVisException
     * @TODO: we may need to cache the relationships but for now its fast
     * enough. IF so we need an Cach implementaion for the relationships
     */
    @Override
    public JEVisClassRelationship buildClassRelationship(String fromClass, String toClass, int type) throws JEVisException {

        //TODO: re-impalement after Webservice change
        try {
            JsonClassRelationship newJsonRel = new JsonClassRelationship();
            newJsonRel.setStart(fromClass);
            newJsonRel.setEnd(toClass);
            newJsonRel.setType(type);
            JEVisClassRelationship rel = new JEVisClassRelationshipWS(this, newJsonRel);
            return rel;


        } catch (Exception ex) {
            logger.catching(ex);
            return null;//TODO throw error
        }
    }

    @Override
    public JEVisRelationship buildRelationship(Long fromObject, Long toObject, int type) throws JEVisException {
        try {
            JsonRelationship newJsonRel = new JsonRelationship();
            newJsonRel.setFrom(fromObject);
            newJsonRel.setTo(toObject);
            newJsonRel.setType(type);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH;

            logger.debug("playload: {}", gson.toJson(newJsonRel));
            StringBuffer response = getHTTPConnection().postRequest(resource, gson.toJson(newJsonRel));

            JsonRelationship newJson = gson.fromJson(response.toString(), JsonRelationship.class);
            JEVisRelationship newRel = new JEVisRelationshipWS(this, newJson);

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
    public List<JEVisObject> getObjects() throws JEVisException {
        if (!objectLoaded) {
            for (JEVisObject o : getObjectsWS()) {
                objectCache.put(o.getID(), o);
            }
            objectLoaded = true;
        }

        return new ArrayList<>(objectCache.values());

    }

    public List<JEVisObject> getObjectsWS() throws JEVisException {
        logger.trace("Get ALL Objects");
        try {
            List<JEVisObject> objects = new ArrayList<>();
            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS + "false"
                    + "&" + REQUEST.OBJECTS.OPTIONS.ONLY_ROOT + "false";
            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<JsonObject> jsons = gson.fromJson(response.toString(), listType);
            logger.trace("JsonObject.count: {}", jsons.size());
            for (JsonObject obj : jsons) {
                logger.trace("New obj: " + obj);
                objects.add(new JEVisObjectWS(this, obj));
            }

            return objects;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public JEVisUser getCurrentUser() throws JEVisException {
        return user;

    }

    @Override
    public List<JEVisRelationship> getRelationships() throws JEVisException {
        if (!orLoaded) {

            objectRelCache = getRelationshipsWS();
            objectRelMapCache.clear();

            for (JEVisRelationship re : objectRelCache) {
                long startID = re.getStartID();
                long endID = re.getEndID();
                if (!objectRelMapCache.containsKey(startID)) {
                    objectRelMapCache.put(startID, new ArrayList<JEVisRelationship>());
                }
                objectRelMapCache.get(startID).add(re);

                if (!objectRelMapCache.containsKey(endID)) {
                    objectRelMapCache.put(endID, new ArrayList<JEVisRelationship>());
                }
                objectRelMapCache.get(endID).add(re);

            }

            orLoaded = true;
        }

        return objectRelCache;
    }

    public List<JEVisRelationship> getRelationshipsWS() throws JEVisException {
        logger.error("Get ALL RelationshipsWS");
        try {
            Benchmark bm = new Benchmark();
            List<JEVisRelationship> objects = new ArrayList<>();
            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH;
//                                  + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonRelationship>>() {
            }.getType();
            List<JsonRelationship> jsons = gson.fromJson(response.toString(), listType);
            for (JsonRelationship rel : jsons) {
                objects.add(new JEVisRelationshipWS(JEVisDataSourceWS.this, rel));
            }
            bm.printBenchmarkDetail("Time to get Relationships");
            return objects;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }

    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships() throws JEVisException {
        logger.trace("Get ALL ClassRelationships");
        //TODO: re-impalement after Webservice change
        return new ArrayList<>();
    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships(String jclass) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisAttribute> getAttributes(long objectID) throws JEVisException {
        logger.trace("Get  getAttributes: {}", objectID);
        StringBuffer response = new StringBuffer();
        try {
//            JEVisObject obj = getObject(objectID);
            List<JEVisAttribute> attributes = new ArrayList<>();
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH;
//                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
            response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonAttribute>>() {
            }.getType();
            List<JsonAttribute> jsons = gson.fromJson(response.toString(), listType);
            for (JsonAttribute att : jsons) {
                try {
                    attributes.add(new JEVisAttributeWS(this, att, objectID));
                } catch (Exception ex) {
                    Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return attributes;


        } catch (NullPointerException | JsonSyntaxException jex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, jex);
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, response.toString());
            return new ArrayList<>();
        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<JEVisType> getTypes(String className) throws JEVisException {
        logger.trace("Get  getTypes: {}", className);

        return getJEVisClass(className).getTypes();

    }

    private void removeObjectFromCache(long objectID) {
        try {
            JEVisObject object = getObject(objectID);
            List<Long> ids = new ArrayList<>();
            for (JEVisObject c : object.getChildren()) {
                ids.add(c.getID());
            }
            for (Long id : ids) {
                removeObjectFromCache(id);
            }
            reloadRelationships();//takes 30ms

//            List<JEVisRelationship> toDelete = new ArrayList<>();
//            for (JEVisRelationship rel : objectRelCache) {
//                if (ids.contains(rel.getStartID()) || ids.contains(rel.getEndID())) {
//                    toDelete.add(rel);
//                }
//            }
//
//            for (JEVisRelationship rel : toDelete) {
//                try {
//                    objectRelCache.remove(rel);
//                } catch (NullPointerException ne) {
//
//                }
//            }
//            objectCache.remove(objectID);
        } catch (JEVisException | NullPointerException ne) {

        }

    }

    public synchronized void reloadRelationships() {
        orLoaded = false;
    }

    @Override
    public boolean deleteObject(long objectID) throws JEVisException {
        try {
            logger.error("Delete: {}", objectID);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID;

            HttpURLConnection response = getHTTPConnection().getDeleteConnection(resource);
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {

                reloadRelationships();
                removeObjectFromCache(objectID);

                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @Override
    public boolean deleteClass(String jclass) throws JEVisException {
        try {
            logger.trace("Delete: {}", jclass);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + jclass;

            Gson gson = new Gson();
            HttpURLConnection conn = getHTTPConnection().getDeleteConnection(resource);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @Override
    public boolean deleteRelationship(Long fromObject, Long toObject, int type) throws JEVisException {
        try {
            logger.trace("Delete: '{}' -> '{}' type:{}", fromObject, toObject, type);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH
                    + "?"
                    + REQUEST.RELATIONSHIPS.OPTIONS.FROM + fromObject
                    + "&"
                    + REQUEST.RELATIONSHIPS.OPTIONS.TO + toObject
                    + "&"
                    + REQUEST.RELATIONSHIPS.OPTIONS.TYPE + type;

//            Gson gson = new Gson();
            HttpURLConnection conn = getHTTPConnection().getDeleteConnection(resource);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //TODO: maybe remove from the list of cached relationships but for now we dont have such a list
                return true;
            }

            return false;

        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @Override
    public boolean deleteClassRelationship(String fromClass, String toClass, int type) throws JEVisException {
        //TODO: re-implement after webservice change
        return false;
    }

    @Override
    public List<JEVisOption> getConfiguration() {
        logger.debug("GetConfig");
        return this.config;
    }

    @Override
    public void setConfiguration(List<JEVisOption> config) {
        logger.debug("SetConfig: {}", config);
        this.config = config;
        for (JEVisOption opt : config) {
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                host = opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue();
//                _dbPort = opt.getOption(CommonOptions.DataSource.PORT.getKey()).getValue();
//                _dbSchema = opt.getOption(CommonOptions.DataSource.SCHEMA.getKey()).getValue();
//                _dbUser = opt.getOption(CommonOptions.DataSource.USERNAME.getKey()).getValue();
//                _dbPW = opt.getOption(CommonOptions.DataSource.PASSWORD.getKey()).getValue();
            }
        }
    }

    public JEVisObject buildObject(long parentID, String jclass, String name) throws JEVisException {
        logger.trace("ds.buildObject: {} {} {}", parentID, jclass, name);
        JEVisObject parent = getObject(parentID);
        JEVisClass newObjClass = getJEVisClass(jclass);
        JEVisObject newObj = parent.buildObject(name, newObjClass);
        newObj.commit();

        parent.notifyListeners(new JEVisEvent(parent, JEVisEvent.TYPE.OBJECT_NEW_CHILD));

        return newObj;
    }

    public synchronized HTTPConnection getHTTPConnection() {
        return con;
    }

    public List<JEVisSample> getSamples(JEVisAttribute att, DateTime from, DateTime until) {

        logger.trace("Get  getSamples: {} {}-{}", att.getName(), from, until);
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

            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonSample>>() {
            }.getType();
            List<JsonSample> jsons = gson.fromJson(response.toString(), listType);
            for (JsonSample sample : jsons) {
//                logger.trace("New rel: " + rel);
                try {
                    samples.add(new JEVisSampleWS(this, sample, att));
                } catch (Exception ex) {

                }
            }

            return samples;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    public int addSamples(JEVisAttribute att, List<JEVisSample> samples) {
        //TODO
        return 0;
    }

    @Override
    public JEVisClass buildClass(String name) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(name);
        JEVisClass newClass = new JEVisClassWS(this, json);

        return newClass;
    }

    public void reloadClasses() throws JEVisException {
        classLoaded = false;
        getJEVisClasses();
    }

    @Override
    public JEVisObject buildLink(String name, JEVisObject parent, JEVisObject linkedObject) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisObject> getRootObjects() throws JEVisException {

        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "?root=true";
            List<JEVisObject> objs = new ArrayList<>();
            StringBuffer response = con.getRequest(resource);

            //TODO: make an exception handling
            if (response == null) {
                return objs;
            }

            Type listType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<JsonObject> jsons = gson.fromJson(response.toString(), listType);

            for (JsonObject jobj : jsons) {
                try {
                    objs.add(new JEVisObjectWS(this, jobj));
                } catch (Exception ex) {

                }

            }

            return objs;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    @Override
    public List<JEVisObject> getObjects(JEVisClass jevisClass, boolean addheirs) throws JEVisException {
        //TODO
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "?root=false&class=" + jevisClass.getName();
            if (addheirs) {
                resource += "&inherit=true";
            }

            List<JEVisObject> objs = new ArrayList<>();
            StringBuffer response = con.getRequest(resource);

            //TODO: make an exception handling
            if (response == null) {
                return objs;
            }

            Type listType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<JsonObject> jsons = gson.fromJson(response.toString(), listType);

            for (JsonObject jobj : jsons) {
                try {
                    objs.add(new JEVisObjectWS(this, jobj));
                } catch (Exception ex) {

                }

            }

            return objs;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }


    /**
     * @param jclass
     * @return
     */
    public BufferedImage getClassIcon(String jclass) {
        logger.debug("getClassIconNeu: {}", jclass);
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASS_ICONS.PATH;
        try {

            String tmpdir = System.getProperty("java.io.tmpdir");
            File zipDir = new File(tmpdir + "/JEVisCC/");

            ClassIconHandler cih = new ClassIconHandler(zipDir);
            if (!cih.fileExists()) {
                cih.readStream(con.getInputStreamRequest(resource));
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
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
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
            if (zipDir.listFiles().length == 0) {
                cih.readStream(con.getInputStreamRequest(resource));
            }

            return cih.getClassIcon();

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (Exception ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public BufferedImage getClassIconFormWS(String jclass) {
        logger.debug("getClassIcon: {}", jclass);
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASSES.PATH + jclass + "/" + REQUEST.CLASSES.ICON.PATH;
        try {

            return con.getIconRequest(resource);

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        }
        return null;
    }

    @Override
    public JEVisObject getObject(Long id) throws JEVisException {
        if (objectCache.get(id) != null) {
            return objectCache.get(id);
        }

        JEVisObject ob = getObjectWS(id);
        if (ob != null) {
            objectCache.put(id, ob);
        }

        return ob;

    }

    public JEVisObject getObjectWS(Long id) throws JEVisException {
        logger.debug("GetObject: {}", id);
        String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "/" + id;
        try {

            StringBuffer response = con.getRequest(resource);
            JsonObject json = gson.fromJson(response.toString(), JsonObject.class);

            JEVisObject obj = new JEVisObjectWS(this, json);
            return obj;

        } catch (ProtocolException ex) {
            logger.error("Error while fetching Object", ex);
        } catch (IOException ex) {
            logger.error("Error while fetching Object", ex);
        }
        return null;
    }

    @Override
    public JEVisClass getJEVisClass(String name) throws JEVisException {
        //for now we assume classes are allways cached
        if (classCache.isEmpty()) {
            getJEVisClasses();
        }

        return classCache.get(name);

    }

    public JEVisClass getJEVisClassWS(String name) throws JEVisException {
        logger.trace("GetClass: {}", name);
        try {

            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES + "/" + name;
            StringBuffer response = con.getRequest(resource);

            JsonJEVisClass json = gson.fromJson(response.toString(), JsonJEVisClass.class);
            JEVisClass newClass = new JEVisClassWS(this, json);
            return newClass;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<JEVisClass> getJEVisClasses() throws JEVisException {
        if (!classLoaded) {
            //For now we allways cache all classes because there are quit static
            Map<String, JEVisClass> map = Maps.uniqueIndex(getJEVisClassesWS(), new Function<JEVisClass, String>() {
                @Override
                public String apply(JEVisClass f) {
                    try {
                        return f.getName();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            });
            classCache = map;
            classLoaded = true;
        }

        return new ArrayList<>(classCache.values());
    }

    public List<JEVisClass> getJEVisClassesWS() throws JEVisException {
        logger.error("Connection: " + con);
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES;
            List<JEVisClass> classes = new ArrayList<>();
            StringBuffer response = con.getRequest(resource);
            if (response == null) {
                logger.error("Emty response for getClasses");
                return new ArrayList<>();//hmmm not the best solutuin
            }

            Type listType = new TypeToken<List<JsonJEVisClass>>() {
            }.getType();
            List<JsonJEVisClass> jsons = gson.fromJson(response.toString(), listType);
            for (JsonJEVisClass jc : jsons) {

                JEVisClass newClass = new JEVisClassWS(this, jc);
                classes.add(newClass);
            }

            return classes;

        } catch (ProtocolException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        List list = new ArrayList();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.getType() == type) {
                list.add(rel);
            }
        }
        return list;
    }

    @Override
    public boolean connect(String username, String password) throws JEVisException {
        logger.debug("Connect with user {} to: {}", username, host);

        //TODO implement config paramter to set trustAllCertificates
        HTTPConnection.trustAllCertificates();
        con = new HTTPConnection(host, username, password);


        try {
            String resource
                    = REQUEST.API_PATH_V1
                    + REQUEST.JEVISUSER.PATH;

            HttpURLConnection conn = getHTTPConnection().getGetConnection(resource);
            logger.debug("Login Response: {}", conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                logger.debug("Login response: {}", conn.getContent().toString());
                String payload = IOUtils.toString((InputStream) conn.getContent(), "UTF-8");

                JsonObject json = gson.fromJson(payload, JsonObject.class);

                user = new JEVisUserWS(this, new JEVisObjectWS(this, json)); //TODO: implement
                logger.trace("User.object: " + user.getUserObject());
                return true;
            } else {
                logger.error("Login faild: [{}] {}", conn.getResponseCode(), conn.getResponseMessage());
                return false;
            }

        } catch (Exception ex) {
            logger.catching(ex);
            throw new JEVisException(ex.getMessage(), 402, ex);
        }

    }

    @Override
    public boolean disconnect() throws JEVisException {
        //TODO: implement
        return true;
    }

    @Override
    public boolean reconnect() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisInfo getInfo() {
        return info;
    }

    @Override
    public boolean isConnectionAlive() throws JEVisException {
        //TODO: implement
        return true;
    }

    @Override
    public List<JEVisUnit> getUnits() {
        //TODo: implement
        return new ArrayList<>();
    }

    @Override
    public List<JEVisRelationship> getRelationships(long objectID) throws JEVisException {
        if (!orLoaded) {
            getRelationships();
        }

        return objectRelMapCache.get(objectID);

    }

    @Override
    public void preload() throws JEVisException {
        getJEVisClasses();
        getClassIcon();
    }

    public List<JsonI18nClass> getTranslation() {
        logger.trace("Get I18n");
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.REAOURCE_I18N;//+"?jclass="+Organisation;
            StringBuffer response = con.getRequest(resource);

            logger.trace("raw response: '{}'", response.toString());

            Type listType = new TypeToken<List<JsonI18nClass>>() {
            }.getType();
            List<JsonI18nClass> jsons = gson.fromJson(response.toString(), listType);
            return jsons;

        } catch (Exception ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }

    }

}
