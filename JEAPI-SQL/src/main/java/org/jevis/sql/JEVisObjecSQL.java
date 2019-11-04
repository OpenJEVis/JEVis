package org.jevis.sql;

import org.jevis.api.*;
import org.jevis.commons.ws.json.JsonObject;

import java.util.List;

public class JEVisObjecSQL implements JEVisObject {

    JsonObject jsonObject;
    JEDataSourceSQL dataSourceSQL;

    public JEVisObjecSQL(JEDataSourceSQL dataSourceSQL,JsonObject json) {
        this.jsonObject=json;
        this.dataSourceSQL=dataSourceSQL;
    }

    @Override
    public String getName() {
        return jsonObject.getName();
    }

    @Override
    public void setName(String name) throws JEVisException {

    }

    @Override
    public Long getID() {
        return jsonObject.getId();
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        return dataSourceSQL.getJEVisClass(jsonObject.getJevisClass());
    }

    @Override
    public String getJEVisClassName() throws JEVisException {
        return jsonObject.getJevisClass();
    }

    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisObject> getChildren(JEVisClass type, boolean inherit) throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisAttribute> getAttributes() throws JEVisException {
        return null;
    }

    @Override
    public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {
        return null;
    }

    @Override
    public JEVisAttribute getAttribute(String type) throws JEVisException {
        return null;
    }

    @Override
    public boolean delete() throws JEVisException {
        return false;
    }

    @Override
    public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
        return null;
    }

    @Override
    public JEVisObject getLinkedObject() throws JEVisException {
        return null;
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject obj, int type, int direction) throws JEVisException {
        return null;
    }

    @Override
    public void deleteRelationship(JEVisRelationship rel) throws JEVisException {

    }

    @Override
    public List<JEVisRelationship> getRelationships() throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type, int direction) throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisClass> getAllowedChildrenClasses() throws JEVisException {
        return null;
    }

    @Override
    public boolean isAllowedUnder(JEVisObject otherObject) throws JEVisException {
        return false;
    }

    @Override
    public boolean isPublic() throws JEVisException {
        return false;
    }

    @Override
    public void setIsPublic(boolean ispublic) throws JEVisException {

    }

    @Override
    public void addEventListener(JEVisEventListener listener) {

    }

    @Override
    public void removeEventListener(JEVisEventListener listener) {

    }

    @Override
    public void notifyListeners(JEVisEvent event) {

    }

    @Override
    public int compareTo(JEVisObject o) {
        return 0;
    }

    @Override
    public void commit() throws JEVisException {

    }

    @Override
    public void rollBack() throws JEVisException {

    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return null;
    }
}
