package org.jevis.jecc.plugin.action.data;

import org.jetbrains.annotations.NotNull;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class FreeObject implements JEVisObject {

    private static FreeObject instance = null;

    public static FreeObject getInstance() {
        if (instance == null) instance = new FreeObject();

        return instance;
    }

    @Override
    public String getName() {
        return I18n.getInstance().getString("plugin.action.freeobject.name");
    }

    @Override
    public void setName(String name) throws JEVisException {

    }

    @Override
    public String getLocalName(String key) {
        return null;
    }

    @Override
    public void setLocalName(String key, String name) {

    }

    @Override
    public void setLocalNames(Map<String, String> translation) {

    }

    @Override
    public Map<String, String> getLocalNameList() {
        return null;
    }

    @Override
    public Long getID() {
        return -1l;
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        return null;
    }

    @Override
    public String getJEVisClassName() throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        return null;
    }

    @Override
    public JEVisObject getParent() throws JEVisException {
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
    public JEVisEventListener[] getEventListener() {
        return new JEVisEventListener[0];
    }

    @Override
    public void notifyListeners(JEVisEvent event) {

    }

    @Override
    public DateTime getDeleteTS() {
        return null;
    }

    @Override
    public void setDeleteTS(DateTime ts) throws JEVisException {

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FreeObject;
    }

    @Override
    public int compareTo(@NotNull JEVisObject o) {
        return o.getID().compareTo(getID());
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
