package org.jevis.sql;

import org.apache.commons.collections.map.HashedMap;
import org.jevis.api.*;
import org.jevis.commons.json.JsonObject;
import org.jevis.jeapi.ws.JEVisObjectWS;
import org.jevis.rest.Config;
import org.jevis.ws.sql.ConnectionFactory;
import org.jevis.ws.sql.SQLDataSource;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JEDataSourceSQL implements JEVisDataSource {

    private final SQLDataSource ds;
    private Map<Long,JEVisObject> objectMap = Collections.synchronizedMap(new HashedMap());
    private Map<Long,List<JEVisObject>> objectChildrenMap = Collections.synchronizedMap(new HashedMap());

    public JEDataSourceSQL(String username, String password) throws Exception {
        Config.readConfigurationFile(new File("webservice.xml"));
        ConnectionFactory.getInstance().registerMySQLDriver(Config.getDBHost(), Config.getDBPort(), Config.getSchema(), Config.getDBUser(), Config.getDBPW());


        this.ds = new SQLDataSource(username,password);
        this.ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
        this.ds.preload(SQLDataSource.PRELOAD.ALL_REL);
    }

    public void init(List<JEVisOption> config) throws IllegalArgumentException {

    }

    public List<JEVisOption> getConfiguration() {
        return new ArrayList<>();
    }

    public void setConfiguration(List<JEVisOption> config) {

    }

    public JEVisClass buildClass(String name) throws JEVisException {
        return null;
    }

    public JEVisObject buildLink(String name, JEVisObject parent, JEVisObject linkedObject) throws JEVisException {
        return null;
    }

    public JEVisClassRelationship buildClassRelationship(String fromClass, String toClass, int type) throws JEVisException {
        return null;
    }

    public JEVisRelationship buildRelationship(Long fromObject, Long toObject, int type) throws JEVisException {
        return null;
    }

    public List<JEVisObject> getRootObjects() throws JEVisException {
        return null;
    }

    public List<JEVisObject> getObjects(JEVisClass jevisClass, boolean addheirs) throws JEVisException {


        ds.getObjects().parallelStream().forEach(jsonObject -> {
            objectMap.put(jsonObject.getId(),new JEVisObjecSQL(this,jsonObject));

        });

        objectMap.values().forEach(jsonObject -> {
            objectChildrenMap.put(jsonObject.getID(),Collections.synchronizedList(new ArrayList<>()));
            objectMap.values().forEach(jsonObject2 -> {

            });
        });



        return null;
    }

    public List<JsonObject> getParent(Long id){
return null;
    }

    public JEVisObject getObject(Long id) throws JEVisException {
        return null;
    }

    public List<JEVisObject> getObjects() throws JEVisException {
        return null;
    }

    public JEVisClass getJEVisClass(String name) throws JEVisException {
        return null;
    }

    public List<JEVisClass> getJEVisClasses() throws JEVisException {
        return null;
    }

    public JEVisUser getCurrentUser() throws JEVisException {
        return null;
    }

    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        return null;
    }

    public List<JEVisRelationship> getRelationships(long objectID) throws JEVisException {
        return null;
    }

    public List<JEVisRelationship> getRelationships() throws JEVisException {
        return null;
    }

    public List<JEVisClassRelationship> getClassRelationships() throws JEVisException {
        return null;
    }

    public List<JEVisClassRelationship> getClassRelationships(String jclass) throws JEVisException {
        return null;
    }

    public void getAttributes() throws JEVisException {

    }

    public boolean connect(String username, String password) throws JEVisException {
        return false;
    }

    public boolean disconnect() throws JEVisException {
        return false;
    }

    public boolean reconnect() throws JEVisException {
        return false;
    }

    public JEVisInfo getInfo() {
        return null;
    }

    public boolean isConnectionAlive() throws JEVisException {
        return false;
    }

    public List<JEVisUnit> getUnits() {
        return null;
    }

    public List<JEVisAttribute> getAttributes(long objectID) throws JEVisException {
        return null;
    }

    public List<JEVisType> getTypes(String className) throws JEVisException {
        return null;
    }

    public boolean deleteObject(long objectID) throws JEVisException {
        return false;
    }

    public boolean deleteClass(String jclass) throws JEVisException {
        return false;
    }

    public boolean deleteRelationship(Long fromObject, Long toObject, int type) throws JEVisException {
        return false;
    }

    public boolean deleteClassRelationship(String fromClass, String toClass, int type) throws JEVisException {
        return false;
    }

    public void preload() throws JEVisException {

    }

    public void reloadAttributes() throws JEVisException {

    }

    public void reloadAttribute(JEVisAttribute attribute) {

    }

    public void reloadAttribute(JEVisObject object) {

    }

    public void reloadObject(JEVisObject object) {

    }

    public void clearCache() {

    }
}
