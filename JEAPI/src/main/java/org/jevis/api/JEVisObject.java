/**
 * Copyright (C) 2013 - 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI.
 * <p>
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * This interface represents a JEVis Object.
 * <p>
 * Examples for an object are a customer, a measurement device or a building. A
 * JEVisObject cannot store any samples. Measurable attributes of an object like
 * the build year of an building are stored in JEVisAttribute.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisObject extends JEVisComponent, JEVisCommittable, Comparable<JEVisObject> {

    /**
     * Returns the name of the JEVisObject entity as String based on the set local.
     * Return default name if no translation for the local exist.
     * <p>
     * Names are not unique in the JEVis system. For a unique identifier use the
     * ID.
     *
     * @return Name as String
     */
    String getName();

    /**
     * Returns the name of the JEVisObject for the given ISO 639 local code.
     * <pre>
     * new Locale("he").getLanguage()
     *    ...
     * </pre>
     *
     * @param key
     * @return
     */
    String getLocalName(String key);

    /**
     * Set the local name of the JEVisObject. The key is local name as ISO 639.
     *
     * <pre>
     * new Locale("he").getLanguage()
     *    ...
     * </pre>
     *
     * @param key
     * @param name
     */
    void setLocalName(String key, String name);

    void setLocalNames(Map<String, String> translation);

    /**
     * returns a Mit with all localissations for the name. The key is the  ISO 639 language name.
     * <pre>
     * new Locale("he").getLanguage()
     *    ...
     * </pre>
     *
     * @return
     */
    Map<String, String> getLocalNameList();


    /**
     * Set the name of the JEVisObject.
     *
     * @param name
     * @throws org.jevis.api.JEVisException
     */
    void setName(String name) throws JEVisException;

    /**
     * Returns the unique identifier of this JEVisObject entity. The same ID
     * cannot appear twice on the same JEVis server. Its possible and very
     * likely that the same ID appear on different JEVis server.
     * <p>
     * The ID cannot be set by the client and will be given from the server.
     *
     * @return identifier as Long
     */
    Long getID();

    /**
     * Returns the JEVis Object Type of this JEVisObject entity. Every
     * JEVisObject is from a certain Type which describes the object attributes
     * and its handling.
     * <p>
     * There can be unlimited JEVisObject entities from one JEVisObjectType.
     *
     * @return JEVisCalss
     * @throws org.jevis.api.JEVisException
     */
    JEVisClass getJEVisClass() throws JEVisException;

    /**
     * Returns the JEVis Class name.
     *
     * @return
     * @throws JEVisException
     */
    String getJEVisClassName() throws JEVisException;

    /**
     * Returns hierarchy parent of this JEVisObject entity.
     * <p>
     * The JEVisObject is stored in tree like structure where every JEVisObject
     * can have an unlimited amount of JEVisObject-children but just one
     * JEVisObject-parent.
     *
     * @return Parent as JEVisObject
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisObject> getParents() throws JEVisException;

    /**
     * Set the parent JEVisObject.
     *
     * @param object parent as JEVisObject
     */
//    void setParent(JEVisObject object) throws JEVisException;
    /**
     * move this JEVisObject to an other parent
     */
//    void moveTo(JEVisObject newParent) throws JEVisException;

    /**
     * Returns all hierarchy children as a list of JEVisObject. The List will be
     * empty if this JEobject has no children.
     *
     * @return List of all JEVisObject children
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisObject> getChildren() throws JEVisException;

    /**
     * Returns all children as a List of JEVisObject from the certain given
     * JEVisObjectType or all JEVisObjectTypes which inherit the type.
     *
     * @param type    Requested type as JEVisObjectType
     * @param inherit Include inherited classes
     * @return List of all accessible JEVisObject from the same Type or inherit
     * type.
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisObject> getChildren(JEVisClass type, boolean inherit) throws JEVisException;

    /**
     * Returns a List of all JEVisAttributes of this JEVisObject. If a
     * JEVisAttribute is not set the default value will be returned.
     *
     * @return List of JEVisAttributes which this JEVisObject has.
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisAttribute> getAttributes() throws JEVisException;

    /**
     * Returns an specific JEVisAttribute which is of the given JEVisType. If
     * the JEVisAttribute is not set the default value will be returned.
     * <p>
     * Will return an Exception if the JEAttributeType is not allowed unter the
     * JEVisObjectType
     *
     * @param type
     * @return JEAttribute from the given JEAttributeType
     * @throws org.jevis.api.JEVisException
     */
    JEVisAttribute getAttribute(JEVisType type) throws JEVisException;

    /**
     * Returns a specific JEVisAttribute. If the JEVisAttribute is not set the
     * default value will be returned.
     * <p>
     * Will return an Exception if the JEVisType is not allowed under the
     * JEVisObjectType
     *
     * @param type
     * @return JEVisAttribute from the given JEVisAttributeType
     * @throws org.jevis.api.JEVisException
     */
    JEVisAttribute getAttribute(String type) throws JEVisException;

    /**
     * Delete this JEVisObject on the JEVis Server. This JEVisObject will be set
     * to null and will be removed from the child list of the parent.
     * <p>
     * If this JEVisObject is a link it will only delete the link an not the
     * linked reference.
     * <p>
     * All linked references will also be deleted.
     *
     * @return true if the delete was successful
     * @throws org.jevis.api.JEVisException
     */
    boolean delete() throws JEVisException;

    /**
     * Build n new JEVisObject from the given JEVisObjectType and name under
     * this JEVisObject.
     * <p>
     * Throws Exception if the JEVisObjectType is not allowed under this
     * JEVisObject.
     *
     * @param name of the new created JEVisObject
     * @param type JEVisObjectType of the new created JEVisObject
     * @return new created JEVisObject
     * @throws org.jevis.api.JEVisException
     */
    JEVisObject buildObject(String name, JEVisClass type) throws JEVisException;

    /**
     * Get the JEVisObject this JEVisObject points to
     *
     * @return Linked JEVisOBject or null if its not linked
     * @throws JEVisException
     */
    JEVisObject getLinkedObject() throws JEVisException;

    /**
     * Create and commit a new Relationship
     *
     * @param obj
     * @param type      {@link org.jevis.jeapi.JEVisConstants.Relationship}
     * @param direction {@link org.jevis.jeapi.JEVisConstants.Direction}
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisRelationship buildRelationship(JEVisObject obj, int type, int direction) throws JEVisException;

    /**
     * Delete a relationship for this object
     *
     * @param rel
     * @throws JEVisException
     */
    void deleteRelationship(JEVisRelationship rel) throws JEVisException;

    /**
     * Return all relationships this object has
     *
     * @return
     * @throws JEVisException
     */
    List<JEVisRelationship> getRelationships() throws JEVisException;

    /**
     * Return all relationships from the given type
     *
     * @param type
     * @return
     * @throws JEVisException
     */
    List<JEVisRelationship> getRelationships(int type) throws JEVisException;

    /**
     * Return all relationships from the given type
     *
     * @param type      {@link org.jevis.jeapi.JEVisConstants.Relationship}
     * @param direction if Forward the the Class has to be the start, if
     *                  Backward the class has to be the end
     * @return
     * @throws JEVisException
     */
    List<JEVisRelationship> getRelationships(int type, int direction) throws JEVisException;

    /**
     * Return a list of all JEVisClasses who are allowed under this JEVisbject.
     *
     * @return
     * @throws JEVisException
     */
    List<JEVisClass> getAllowedChildrenClasses() throws JEVisException;

    /**
     * Check if this object is allowed under the given object.
     *
     * @param otherObject
     * @return true if the object is allowed under the other object
     * @throws JEVisException
     */
    boolean isAllowedUnder(JEVisObject otherObject) throws JEVisException;

    /**
     * Returns true if this Object can be read by everyone. Public Objects will
     * be used for System wide configuration.
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean isPublic() throws JEVisException;

    /**
     * Set if this Object can ne read by everyone.
     *
     * @param ispublic
     * @throws org.jevis.api.JEVisException
     */
    void setIsPublic(boolean ispublic) throws JEVisException;

    void addEventListener(JEVisEventListener listener);

    void removeEventListener(JEVisEventListener listener);


    JEVisEventListener[] getEventListener();

    /**
     * @param event
     */
    void notifyListeners(JEVisEvent event);

    /**
     * Get the date when this object was deleted.
     * returns null if object is not deleted.
     */
    DateTime getDeleteTS();

    void setDeleteTS(DateTime ts) throws JEVisException;

}
