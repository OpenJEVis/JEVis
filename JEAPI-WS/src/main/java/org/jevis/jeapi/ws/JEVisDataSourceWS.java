/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisInfo;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisRelationship;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;
import org.jevis.api.JEVisUser;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.ws.json.*;
import org.joda.time.DateTime;

/**
 *
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

        //TODo init()
        //Hello world hardcode workaround
        ////http://openjevis.org:14443/JEWebService/v1/objects/5270
//        con = new HTTPConnection("http://openjevis.org:14443", "Sys Admin", "OpenJEVis2016");
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
                types.add(new JEVisTypeWS(this, type, jclass));
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
     * @TODO: we may need to cache the relationships but for now its fast
     * enough. IF so we need an Cach implementaion for the relationships
     *
     * @param fromClass
     * @param toClass
     * @param type
     * @return
     * @throws JEVisException
     */
    @Override
    public JEVisClassRelationship buildClassRelationship(String fromClass, String toClass, int type) throws JEVisException {
        try {
            JsonClassRelationship newJsonRel = new JsonClassRelationship();
            newJsonRel.setStart(fromClass);
            newJsonRel.setEnd(toClass);
            newJsonRel.setType(type);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASS_RELATIONSHIPS.PATH;

            Gson gson = new Gson();
            StringBuffer response = getHTTPConnection().postRequest(resource, gson.toJson(newJsonRel));

            JsonClassRelationship newJson = gson.fromJson(response.toString(), JsonClassRelationship.class);
            JEVisClassRelationship newRel = new JEVisClassRelationshipWS(this, newJson);

            return newRel;

        } catch (Exception ex) {
            logger.catching(ex);
            return null;//TODO throw error
        }
    }

    public List<JEVisAttribute> getAttributes(JEVisObjectWS obj) {
        logger.trace("Get Attribute for: {}", obj.getID());
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_OBJECTS + "/" + obj.getID() + "/" + HTTPConnection.RESOURCE_ATTRIBUTES;
            StringBuffer response = con.getRequest(resource);

            logger.trace("raw response: '{}'", response.toString());

            Type listType = new TypeToken<List<JsonAttribute>>() {
            }.getType();
            List<JsonAttribute> jsons = gson.fromJson(response.toString(), listType);
            List<JEVisAttribute> attributes = new ArrayList<>();
            for (JsonAttribute att : jsons) {
                logger.trace("New Attribute: " + att);
                attributes.add(new JEVisAttributeWS(this, att, obj));
            }

            return attributes;

        } catch (Exception ex) {
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
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
//                logger.trace("New obj: " + obj);
                objects.add(new JEVisObjectWS(this, obj));
            }

            logger.trace("Object.count: {}", objects.size());
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
        logger.trace("Get ALL Relationships");
        try {
            List<JEVisRelationship> objects = new ArrayList<>();
            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.RELATIONSHIPS.PATH;
//                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonRelationship>>() {
            }.getType();
            List<JsonRelationship> jsons = gson.fromJson(response.toString(), listType);
            for (JsonRelationship rel : jsons) {
//                logger.trace("New rel: " + rel);
                objects.add(new JEVisRelationshipWS(this, rel));
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
    public List<JEVisClassRelationship> getClassRelationships() throws JEVisException {
        logger.trace("Get ALL ClassRelationships");
        try {
            List<JEVisClassRelationship> objects = new ArrayList<>();
            String resource = HTTPConnection.API_PATH_V1
                    + REQUEST.CLASS_RELATIONSHIPS.PATH;
//                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonClassRelationship>>() {
            }.getType();
            List<JsonClassRelationship> jsons = gson.fromJson(response.toString(), listType);
            for (JsonClassRelationship rel : jsons) {
//                logger.trace("New rel: " + rel);
                objects.add(new JEVisClassRelationshipWS(this, rel));
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
    public List<JEVisClassRelationship> getClassRelationships(String jclass) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisAttribute> getAttributes(long objectID) throws JEVisException {
        logger.trace("Get  getAttributes: {}", objectID);
        StringBuffer response = new StringBuffer();
        try {
            JEVisObject obj = getObject(objectID);
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
                    attributes.add(new JEVisAttributeWS(this, att, obj));
                } catch (Exception ex) {
                    Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return attributes;

        }catch (JsonSyntaxException jex){
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, null, jex);
            Logger.getLogger(JEVisDataSourceWS.class.getName()).log(Level.SEVERE, response.toString());
            return new ArrayList<>();
        }catch (ProtocolException ex) {
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
        try {
            JEVisClass jclass = getJEVisClass(className);
            List<JEVisType> types = new ArrayList<>();
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + className + "/"
                    + REQUEST.CLASSES.TYPES.PATH;
//                    + "?" + REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
            StringBuffer response = con.getRequest(resource);

            Type listType = new TypeToken<List<JsonType>>() {
            }.getType();
            List<JsonType> jsons = gson.fromJson(response.toString(), listType);
            for (JsonType type : jsons) {
//                logger.trace("New rel: " + rel);
                try {
                    types.add(new JEVisTypeWS(this, type, jclass));
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }

            return types;

        } catch (ProtocolException ex) {
            logger.catching(ex);
            //TODO: throw excption?! so the other function can handel it?
            return new ArrayList<>();
        } catch (IOException ex) {
            logger.catching(ex);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteObject(long objectID) throws JEVisException {
        try {
            logger.trace("Delete: {}", objectID);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + objectID;

            HttpURLConnection response = getHTTPConnection().getDeleteConnection(resource);
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
        try {
            logger.trace("Delete: '{}' -> '{}' type:{}", fromClass, toClass, type);

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASS_RELATIONSHIPS.PATH
                    + "?"
                    + REQUEST.CLASS_RELATIONSHIPS.OPTIONS.FROM + fromClass
                    + "&"
                    + REQUEST.CLASS_RELATIONSHIPS.OPTIONS.TO + toClass
                    + "&"
                    + REQUEST.CLASS_RELATIONSHIPS.OPTIONS.TYPE + type;

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
//        newObj.commit();

        return newObj;
    }

    public HTTPConnection getHTTPConnection() {
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
        newClass.commit();

        return newClass;
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
            if(addheirs){
                resource+="&inherit=true";
            }
            System.out.println("--------------Resource: "+resource);
            
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
     * Returns the Icon for an JEVIsClass.
     *
     * This workaround implementation will first try to read an local zip file
     * with the icon and if it not exist fetch it from the webservice. If that
     * fails use the origal webservice requet to get the icon.
     *
     * @param jclass
     * @return
     */
    public BufferedImage getClassIcon(String jclass) {
        logger.debug("TMP dir: {}", System.getProperty("java.io.tmpdir"));

        logger.debug("getClassIconNeu: {}", jclass);
        String resource = REQUEST.API_PATH_V1 + REQUEST.CLASS_ICONS.PATH;
        try {

            String tmpdir = System.getProperty("java.io.tmpdir");
            File zipDir = new File(tmpdir + "/JEVisCC/");

            ClassIconHandler cih = new ClassIconHandler(zipDir);
            if (!cih.fileExists()) {
                cih.readStream(con.getInputStreamRequest(resource));
            }

            if (cih.getClassIcon(jclass) != null) {
                return cih.getClassIcon(jclass);
            } else {
                return getClassIconFormWS(jclass);
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
        logger.error("Connection: " + con);
        try {
            String resource = HTTPConnection.API_PATH_V1 + HTTPConnection.RESOURCE_CLASSES;
            List<JEVisClass> classes = new ArrayList<>();
            StringBuffer response = con.getRequest(resource);
            if (response == null) {
                logger.error("Emty response for getClasses");
                return new ArrayList<>();//hmmm not the best solutuin
            }

//            ObjectMapper mapper = new ObjectMapper();
//
//            List<JsonJEVisClass> jsons = mapper.readValue(response.toString(), new TypeReference<List<JsonJEVisClass>>() {
//            });
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean connect(String username, String password) throws JEVisException {
        logger.debug("Connect with user {} to: {}", username, host);

        con = new HTTPConnection(host, username, password);
//        con = new HTTPConnection("http://localhost:8080", username, password);//hmm wrong place

        try {
            String resource
                    = REQUEST.API_PATH_V1
                    + REQUEST.JEVISUSER.PATH;

//            Gson gson = new Gson();
            HttpURLConnection conn = getHTTPConnection().getGetConnection(resource);
            logger.debug("Login Response: {}", conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                logger.debug("Login response: {}", conn.getContent().toString());
                String payload = IOUtils.toString((InputStream) conn.getContent(), "UTF-8");

                JsonObject json = gson.fromJson(payload, JsonObject.class);

                user = new JEVisUserWS(this, new JEVisObjectWS(this, json)); //TODO: implement
                logger.trace("User.object: " + user.getUserObject());
                logger.trace("User.canRead1: " + user.canRead(1));
                return true;
            } else {
                logger.error("Login faild: [{}] {}", conn.getResponseCode(), conn.getResponseMessage());
                return false;
            }

            //new
//            Gson gson = new Gson();
//            HttpURLConnection conn = getHTTPConnection().getGetConnection(resource);
//            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                logger.debug("Login response: {}", conn.getResponseMessage());
//                JsonObject json = gson.fromJson(conn.getResponseMessage(), JsonObject.class);
//
//                user = new JEVisUserWS(this, new JEVisObjectWS(this, json)); //TODO: implement
//                logger.trace("User.object: " + user.getUserObject());
//                logger.trace("User.canRead1: " + user.canRead(1));
//                return true;
//            } else {
//                logger.error("Login faild: [{}] {}", conn.getResponseCode(), conn.getResponseMessage());
//                return false;
//            }
            //old
//            StringBuffer response = con.getRequest(resource);
//
//            JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
//            user = new JEVisUserWS(this, new JEVisObjectWS(this, json)); //TODO: implement
//            logger.trace("User.object: " + user.getUserObject());
//            logger.trace("User.canRead1: " + user.canRead(1));
//            return true;
        } catch (Exception ex) {
            logger.catching(ex);
//            return false;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // TODO : implement preload
    @Override
    public void preload() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
