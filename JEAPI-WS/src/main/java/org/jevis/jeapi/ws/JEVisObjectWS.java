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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.ws.json.JsonI18n;
import org.jevis.commons.ws.json.JsonObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.util.*;

/**
 * Client-side representation of a JEVis object backed by a {@link JEVisDataSourceWS} REST
 * connection.
 *
 * <p>Attribute lookups by name are accelerated with a lazy-initialized
 * {@link HashMap} (built on the first call to {@link #getAttribute(String)}) so that
 * repeated lookups within a single session are O(1) instead of O(n).
 *
 * @author fs
 */
public class JEVisObjectWS implements JEVisObject {
    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.date();
    private static final Logger logger = LogManager.getLogger(JEVisObjectWS.class);
    private final EventListenerList listeners = new EventListenerList();
    private final JEVisDataSourceWS ds;
    private JsonObject json;
    private DateTime tsObj = null;
    /**
     * Lazy map from lower-cased attribute name → attribute, for O(1) lookup.
     */
    private Map<String, JEVisAttribute> attributeByName = null;


    /**
     * Creates a new object representation backed by the given data source.
     *
     * @param ds   the data source
     * @param json the DTO containing the object's metadata
     */
    public JEVisObjectWS(JEVisDataSourceWS ds, JsonObject json) {
        this.ds = ds;
        this.json = json;

        if (json.getDeleteTS() != null && !json.getDeleteTS().isEmpty()) {
            tsObj = sampleDTF.parseDateTime(json.getDeleteTS());
        }
    }


    /**
     * Refreshes this object's metadata from a new DTO (e.g., after a server-side change).
     * Also clears the attribute-name lookup map so it is rebuilt on next access.
     *
     * @param json the updated object DTO
     */
    public void update(JsonObject json) {
        this.json = json;
        this.attributeByName = null;
    }

    /** @return the localised display name using the current UI locale. */
    @Override
    public String getName() {
        return getLocalName(I18n.getInstance().getLocale().getLanguage());
    }

    /**
     * Updates the display name for the current UI locale (local change; call {@link #commit()} to persist).
     *
     * @param name the new display name
     */
    @Override
    public void setName(String name) {
        this.json.setName(name);
        setLocalName(I18n.getInstance().getLocale().getLanguage(), name);
    }

    /** @return the unique JEVis object ID. */
    @Override
    public Long getID() {
        return this.json.getId();
    }

    /**
     * Returns the {@link JEVisClass} definition for this object.
     * For link objects, the class of the linked target is returned.
     *
     * @return the JEVis class
     * @throws JEVisException if the class cannot be fetched
     */
    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        if (isLink()) {
            JEVisObject linkedObject = getLinkedObject();
            if (linkedObject != null) {
                return linkedObject.getJEVisClass();
            }
        }
        return this.ds.getJEVisClass(this.json.getJevisClass());
    }

    /** @return the JEVis class name string (no server round-trip). */
    @Override
    public String getJEVisClassName() {
        return this.json.getJevisClass();
    }

    /**
     * Returns all parent objects (objects linked via a PARENT relationship).
     *
     * @return the list of parent objects
     * @throws JEVisException if relationships cannot be loaded
     */
    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        List<JEVisObject> parents = new ArrayList<>();
        try {
            for (JEVisRelationship rel : getRelationships()) {
                if (rel.getType() == 1) {
                    if (rel.getStartObject().getID().equals(getID())) {
                        if (rel.getEndObject() != null && !parents.contains(rel.getEndObject())) {
                            parents.add(rel.getEndObject());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Missing or unacceptable parent: {}", getID());
        }

        return parents;

    }

    /**
     * Returns the first parent object, or {@code null} if this is a root object.
     *
     * @return the primary parent, or {@code null}
     * @throws JEVisException if relationships cannot be loaded
     */
    @Override
    public JEVisObject getParent() throws JEVisException {
        JEVisObject obj = getParents().isEmpty() ? null : getParents().get(0);
        return obj;
    }

    /**
     * Returns all direct children of this object (objects whose PARENT is this object).
     *
     * @return the list of direct child objects
     * @throws JEVisException if relationships cannot be loaded
     */
    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
        List<JEVisObject> children = new ArrayList<>();
        try {
            for (JEVisRelationship rel : getRelationships()) {
                try {
                    Long id = rel.getEndID();
                    if ((rel.getType() == JEVisConstants.ObjectRelationship.PARENT) && (id.equals(getID()))) {
                        if (rel.getStartObject() != null && !children.contains(rel.getStartObject())) {
                            children.add(rel.getStartObject());
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            logger.warn("Could not get all children", ex);
        }
        logger.trace("Child.size: {}.{}", getID(), children.size());
        return children;
    }

    /**
     * TODO: this seems to not work properly needs testing
     *
     * @param jclass
     * @param inherit Include inherited classes
     * @return
     * @throws JEVisException
     */
    @Override
    public List<JEVisObject> getChildren(JEVisClass jclass, boolean inherit) throws JEVisException {
        List<JEVisObject> filterLIst = new ArrayList<>();
        for (JEVisObject obj : getChildren()) {
            try {

                JEVisClass oClass = obj.getJEVisClass();
                if (oClass != null && oClass.equals(jclass)) {
                    filterLIst.add(obj);
                } else if (oClass != null) {
                    Set<JEVisClass> inheritanceClasses = getInheritanceClasses(new HashSet<>(), oClass);
                    for (JEVisClass curClass : inheritanceClasses) {
                        if (curClass.equals(jclass)) {
                            filterLIst.add(obj);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex);
            }
        }

        return filterLIst;
    }

    /**
     * Returns all attributes for this object (or the linked target if this is a link).
     * Attributes are served from the data-source cache after the initial server fetch.
     *
     * @return the list of all attributes
     */
    @Override
    public List<JEVisAttribute> getAttributes() {
        if (isLink()) {
            JEVisObject linkedObject = getLinkedObject();
            if (linkedObject != null) {
                return this.ds.getAttributes(linkedObject.getID());
            }
        }

        return this.ds.getAttributes(getID());
    }

    /**
     * Returns the attribute matching the given {@link JEVisType} by type name.
     *
     * @param type the type to look up
     * @return the matching attribute, or {@code null} if not found
     * @throws JEVisException if attributes cannot be loaded
     */
    @Override
    public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {
        //TODO not optimal, getAttribute() will not cached if we call all this in a loop we do no Webservice calls
        for (JEVisAttribute att : getAttributes()) {
            if (att.getName().equals(type.getName())) {
                return att;
            }
        }
        return null;
    }

    /**
     * Returns the attribute with the given name (case-insensitive), or {@code null} if none exists.
     *
     * <p>A per-object name→attribute map is built lazily on the first call and reused
     * on all subsequent calls, making lookup O(1) rather than O(n).
     *
     * @param type attribute name
     * @return the matching {@link JEVisAttribute}, or {@code null}
     */
    @Override
    public JEVisAttribute getAttribute(String type) {
        if (type == null) {
            return null;
        }
        if (attributeByName == null) {
            List<JEVisAttribute> atts = getAttributes();
            attributeByName = new HashMap<>(atts.size() * 2);
            for (JEVisAttribute att : atts) {
                attributeByName.put(att.getName().toLowerCase(Locale.ROOT), att);
            }
        }
        return attributeByName.get(type.toLowerCase(Locale.ROOT));
    }

    /**
     * Deletes this object from the JEVis system, including all of its children
     * and associated data. The deletion is propagated through the data source
     * and cannot be undone.
     *
     * @return {@code true} if the deletion succeeded, {@code false} otherwise
     */
    @Override
    public boolean delete() {
        return this.ds.deleteObject(getID(), false);
    }

    /**
     * Creates and returns a new child object of the specified JEVis class under
     * this object. The returned object is not yet persisted; call
     * {@link #commit()} to save it to the server.
     *
     * @param name the display name for the new object
     * @param type the JEVis class defining the type of the new object
     * @return a new, uncommitted {@link JEVisObject}
     * @throws JEVisException if the class name cannot be resolved
     */
    @Override
    public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
        logger.trace("buildObject: {} {}", name, type.getName());
        JsonObject newJson = new JsonObject();
        newJson.setName(name);
        newJson.setJevisClass(type.getName());
        newJson.setParent(getID());


        return new JEVisObjectWS(this.ds, newJson);
    }

    /**
     * Returns the original object that this object links to via a
     * {@link JEVisConstants.ObjectRelationship#LINK LINK} relationship, or
     * {@code null} if this object is not a link or the link target is
     * inaccessible.
     *
     * @return the linked target object, or {@code null}
     */
    @Override
    public JEVisObject getLinkedObject() {
        try {
            for (JEVisRelationship relationship : getRelationships(JEVisConstants.ObjectRelationship.LINK, JEVisConstants.Direction.FORWARD)) {
                try {
                    JEVisObject originalObj = relationship.getEndObject();
                    if (originalObj != null) {
                        return originalObj;
                    }
                    /** TODO: maybe return some spezial object of the user has not access to the otherObject **/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new relationship between this object and {@code otherObj}.
     * For {@link JEVisConstants.Direction#FORWARD FORWARD} direction the
     * relationship is stored as {@code this → otherObj}; for
     * {@link JEVisConstants.Direction#BACKWARD BACKWARD} direction the
     * delegate call is reversed so {@code otherObj → this}.
     * <p>
     * If the relationship type is {@link JEVisConstants.ObjectRelationship#PARENT PARENT},
     * the end-object's listeners are notified of a new child.
     *
     * @param otherObj  the other object in the relationship
     * @param type      the relationship type constant from {@link JEVisConstants.ObjectRelationship}
     * @param direction {@link JEVisConstants.Direction#FORWARD} or {@link JEVisConstants.Direction#BACKWARD}
     * @return the newly created {@link JEVisRelationship}
     * @throws JEVisException if the server request fails
     */
    @Override
    public JEVisRelationship buildRelationship(JEVisObject otherObj, int type, int direction) throws JEVisException {
        JEVisRelationship rel;
        if (direction == JEVisConstants.Direction.FORWARD) {
            rel = this.ds.buildRelationship(getID(), otherObj.getID(), type);

            if (type == JEVisConstants.ObjectRelationship.PARENT) {
                otherObj.notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_NEW_CHILD, rel.getStartObject()));
            }

        } else {
            rel = otherObj.buildRelationship(this, type, JEVisConstants.Direction.FORWARD);
        }


        return rel;
    }

    /**
     * Removes the given relationship from the server and notifies affected
     * listeners. If the relationship type is
     * {@link JEVisConstants.ObjectRelationship#PARENT PARENT}, the end-object's
     * listeners receive an {@link JEVisEvent.TYPE#OBJECT_CHILD_DELETED} event.
     *
     * @param rel the relationship to delete
     * @throws JEVisException if the server request fails
     */
    @Override
    public void deleteRelationship(JEVisRelationship rel) throws JEVisException {
        this.ds.deleteRelationship(rel.getStartID(), rel.getEndID(), rel.getType());

        /**
         * Delete form cache and other objects
         */
        if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
            rel.getEndObject().notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_CHILD_DELETED, rel.getStartObject()));
        }

        notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED, this));
    }

    /**
     * Returns all relationships (of any type and direction) in which this
     * object participates. The list is fetched from the data-source cache.
     *
     * @return a list of all relationships for this object; never {@code null}
     */
    @Override
    public List<JEVisRelationship> getRelationships() {
        return this.ds.getRelationships(getID());
    }

    /**
     * Returns all relationships of the specified type in which this object
     * participates, regardless of direction.
     *
     * @param type the relationship type constant (see {@link JEVisConstants.ObjectRelationship})
     * @return a filtered list of matching relationships; never {@code null}
     * @throws JEVisException if the underlying relationship list cannot be retrieved
     */
    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisRelationship> filter = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.isType(type)) {
                filter.add(rel);
            }
        }

        return filter;
    }

    /**
     * Returns all relationships of the specified type and direction for this
     * object.
     *
     * @param type      the relationship type constant (see {@link JEVisConstants.ObjectRelationship})
     * @param direction {@link JEVisConstants.Direction#FORWARD} if this object is the start,
     *                  {@link JEVisConstants.Direction#BACKWARD} if this object is the end
     * @return a filtered list of matching relationships; never {@code null}
     * @throws JEVisException if the underlying relationship list cannot be retrieved
     */
    @Override
    public List<JEVisRelationship> getRelationships(int type, int direction) throws JEVisException {
        List<JEVisRelationship> filter = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.isType(type)) {
                if (rel.getStartID() == getID() && direction == JEVisConstants.Direction.FORWARD) {
                    filter.add(rel);
                } else if (rel.getEndID() == getID() && direction == JEVisConstants.Direction.BACKWARD) {
                    filter.add(rel);
                }
            }
        }

        return filter;
    }

    /**
     * Returns the list of JEVis classes that are permitted to be created as
     * direct children of this object. For unique classes, a class is only
     * included if no child of that class already exists.
     *
     * @return a list of allowed child class types; never {@code null}
     * @throws JEVisException if the JEVis class or child objects cannot be retrieved
     */
    @Override
    public List<JEVisClass> getAllowedChildrenClasses() throws JEVisException {
        List<JEVisClass> allowedChildren = new ArrayList<>();
        for (JEVisClass vp : getJEVisClass().getValidChildren()) {
            if (vp.isUnique()) {
                if (getChildren(vp, false).isEmpty()) {
                    allowedChildren.add(vp);
                }
            } else {
                allowedChildren.add(vp);
            }
        }

        return allowedChildren;
    }

//    public List<JEVisAttribute> getAttributesWS() {
//        return this.ds.getAttributes(getID());
//    }

    /**
     * Determines whether this object may be placed as a child of
     * {@code otherObject}. The check considers:
     * <ul>
     *   <li>Whether this object's class is a valid child of the other object's class.</li>
     *   <li>Whether this class is marked as unique (only one instance per parent).</li>
     *   <li>Whether a child of the same class already exists under {@code otherObject}.</li>
     *   <li>Directory objects are always allowed under another directory of the same class.</li>
     * </ul>
     *
     * @param otherObject the prospective parent object
     * @return {@code true} if this object is allowed under {@code otherObject}
     * @throws JEVisException if class or child information cannot be retrieved
     */
    @Override
    public boolean isAllowedUnder(JEVisObject otherObject) throws JEVisException {
        boolean classIsAllowedUnderClass = getJEVisClass().isAllowedUnder(otherObject.getJEVisClass());
        boolean isDirectory = this.ds.getJEVisClass("Directory").getHeirs().contains(this.getJEVisClass());
        boolean isUnique = getJEVisClass().isUnique();
        boolean isAlreadyUnderParent = false;
        if (otherObject.getParents() != null) {
            for (JEVisObject child : otherObject.getChildren()) {
                try {
                    if (child.getJEVisClassName().equals(getJEVisClassName())) {
                        isAlreadyUnderParent = true;
                    }
                } catch (Exception ex) {

                }
            }
        }

        /**
         * first check if its allowed to be created under other object class
         */
        if (classIsAllowedUnderClass) {
            if (!isUnique) {
                /**
                 * if its not unique its always allowed to be created
                 */
                return true;
            } else /**
             *  if it is a directory and its of the same class of its parent its allowed to be created
             */if (!isAlreadyUnderParent) {
                /**
                 * if it is unique and not already created its allowed to be created
                 */
                return true;
            } else return isDirectory && otherObject.getJEVisClassName().equals(getJEVisClassName());
        }

        return false;
    }

    /**
     * Returns whether this object is visible to unauthenticated (public) users.
     *
     * @return {@code true} if this object is public
     */
    @Override
    public boolean isPublic() {
        return this.json.getisPublic();
    }

    /**
     * Sets the public-visibility flag for this object. Changes are not
     * persisted until {@link #commit()} is called.
     *
     * @param ispublic {@code true} to make the object publicly visible
     */
    @Override
    public void setIsPublic(boolean ispublic) {
        this.json.setisPublic(ispublic);
    }

    /**
     * Registers a {@link JEVisEventListener} to be notified of events that
     * affect this object (e.g. attribute changes, child additions/removals).
     * Duplicate listeners are logged at DEBUG level.
     *
     * @param listener the listener to register; must not be {@code null}
     */
    @Override
    public void addEventListener(JEVisEventListener listener) {
        if (this.listeners.getListeners(JEVisEventListener.class).length > 0) {
            logger.debug("Duplicate Listener: {}", json.getId());
        }

        this.listeners.add(JEVisEventListener.class, listener);
    }

    /**
     * Unregisters a previously added {@link JEVisEventListener}.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeEventListener(JEVisEventListener listener) {
        this.listeners.remove(JEVisEventListener.class, listener);
    }

    /**
     * Returns all currently registered event listeners for this object.
     *
     * @return an array of registered {@link JEVisEventListener} instances;
     *         empty if none are registered
     */
    @Override
    public JEVisEventListener[] getEventListener() {
        return this.listeners.getListeners(JEVisEventListener.class);
    }

    /**
     * Dispatches the given event to all registered listeners synchronously.
     * This method is thread-safe ({@code synchronized}).
     *
     * @param event the event to broadcast; must not be {@code null}
     */
    @Override
    public synchronized void notifyListeners(JEVisEvent event) {
        logger.trace("Object event[{}] listeners: {} event:", getID(), this.listeners.getListenerCount(), event.getType());
        for (JEVisEventListener l : this.listeners.getListeners(JEVisEventListener.class)) {
            l.fireEvent(event);
        }
    }

    /**
     * Returns the scheduled deletion timestamp for this object, or {@code null}
     * if no deletion is scheduled.
     *
     * @return the deletion {@link DateTime}, or {@code null}
     */
    @Override
    public DateTime getDeleteTS() {
        return tsObj;
    }

    /**
     * Sets the scheduled deletion timestamp for this object. Pass {@code null}
     * to clear a previously set timestamp. Changes are not persisted until
     * {@link #commit()} is called.
     *
     * @param ts the deletion timestamp, or {@code null} to clear it
     * @throws JEVisException if the timestamp cannot be formatted
     */
    @Override
    public void setDeleteTS(DateTime ts) throws JEVisException {
        tsObj = ts;
        if (ts != null) {
            json.setDeleteTS(sampleDTF.print(ts));
        } else {
            json.setDeleteTS(null);
        }

    }

    /**
     * Returns the localized display name for the given locale key. If the key
     * is {@code "default"} or no matching translation exists, the object's
     * primary name is returned.
     *
     * @param key the locale key (e.g. {@code "de"}, {@code "en"}, {@code "default"})
     * @return the localized name, or the primary name if no translation is found
     */
    @Override
    public String getLocalName(String key) {
        if (key.equalsIgnoreCase("default")) {
            return this.json.getName();
        }

        if (!json.getI18n().isEmpty()) {
            for (JsonI18n jsonI18n : json.getI18n()) {
                if (jsonI18n.getKey().equals(key)) {
                    return jsonI18n.getValue();
                }
            }
        }

        return json.getName();
    }

    /**
     * Sets or updates the localized display name for the given locale key.
     * Changes are not persisted until {@link #commit()} is called.
     *
     * @param key  the locale key (e.g. {@code "de"}, {@code "en"})
     * @param name the translated name to associate with the key
     */
    @Override
    public void setLocalName(String key, String name) {

        if (!json.getI18n().isEmpty()) {
            for (JsonI18n jsonI18n : json.getI18n()) {
                if (jsonI18n.getKey().equals(key)) {
                    jsonI18n.setValue(name);
                }
            }
        } else {
            JsonI18n newI18n = new JsonI18n();
            newI18n.setKey(key);
            newI18n.setValue(name);

            json.getI18n().add(newI18n);
        }
    }

    /**
     * Replaces all existing localizations for this object with the supplied
     * map. Any previously stored translations are cleared before the new ones
     * are applied. Changes are not persisted until {@link #commit()} is called.
     *
     * @param translation a map from locale key to translated name; must not be {@code null}
     */
    @Override
    public void setLocalNames(Map<String, String> translation) {
        json.getI18n().clear();
        translation.forEach((s, s2) -> {
            JsonI18n jsonI18n = new JsonI18n();
            jsonI18n.setKey(s);
            jsonI18n.setValue(s2);
            json.getI18n().add(jsonI18n);
        });

    }

    /**
     * Returns all stored localizations for this object as a map from locale
     * key to translated name.
     *
     * @return a mutable map of locale key → name; empty if no translations are stored
     */
    @Override
    public Map<String, String> getLocalNameList() {
        Map<String, String> names = new HashedMap();
        json.getI18n().forEach(jsonI18n -> {
            names.put(jsonI18n.getKey(), jsonI18n.getValue());
        });

        return names;
    }

    private boolean isLink() {
        return this.json.getJevisClass().equals("Link");
    }

    /**
     * Returns the {@link JEVisDataSource} that this object belongs to.
     *
     * @return the owning data source; never {@code null}
     */
    @Override
    public JEVisDataSource getDataSource() {
        return this.ds;
    }

    /**
     * Persists all pending changes for this object to the JEVis server.
     * If the object has no ID yet (i.e. it is new), a POST request is issued
     * and the server-assigned ID is stored. If the object already has an ID,
     * a PUT request updates the existing record. After a successful commit,
     * relationships are reloaded and event listeners are notified.
     *
     * @throws JEVisException if the HTTP request fails or the server response cannot be parsed
     */
    @Override
    public void commit() throws JEVisException {
        try {
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH;

            boolean update = false;

            if (this.json.getId() > 0) {//update existing
                resource += getID();
//                resource += REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
//                resource += "true";
                update = true;
            }


            JsonObject newJson = this.ds.getObjectMapper().readValue(this.ds.getHTTPConnection().postRequest(resource, this.ds.getObjectMapper().writeValueAsString(this.json)).toString(), JsonObject.class);
//
            logger.debug("commit object ID: {} public: {}", newJson.getId(), newJson.getisPublic());
            this.json = newJson;
            this.ds.reloadRelationships(this.json.getId());
            /** reload object to be sure all events will be handled and the cache is working correctly **/
            this.ds.addToObjectCache(this);
            if (update) {
                notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED, this));
            } else {
                this.ds.reloadAttribute(this);
                if (!getParents().isEmpty()) {
                    try {
                        getParents().get(0).notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_NEW_CHILD, this));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }


//            benchmark.printBenchmarkDetail("done commit");
        } catch (JsonParseException ex) {
            throw new JEVisException("Json parse exception. Could not commit to server", 8236341, ex);
        } catch (JsonMappingException ex) {
            throw new JEVisException("Json mapping exception. Could not commit to server", 8236342, ex);
        } catch (JsonProcessingException ex) {
            throw new JEVisException("Json processing exception. Could not commit to server", 8236343, ex);
        } catch (IOException ex) {
            throw new JEVisException("IO exception. Could not commit to server", 8236344, ex);
        } catch (Exception ex) {
            throw new JEVisException("Could not commit to server", 8236348, ex);
        }

    }

    /**
     * Rolls back any uncommitted changes to this object.
     *
     * @throws UnsupportedOperationException always — rollback is not yet implemented
     */
    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns whether this object has uncommitted local changes.
     * <p>
     * <b>Note:</b> the current implementation always returns {@code true}
     * (change tracking is not yet implemented).
     *
     * @return {@code true} always
     */
    @Override
    public boolean hasChanged() {
        //TODO hasChanged
        return true;
    }

    /**
     * Compares this object to another {@link JEVisObject} by object ID.
     *
     * @param o the other object to compare to
     * @return a negative, zero, or positive integer as this object's ID is
     *         less than, equal to, or greater than the other's ID
     */
    @Override
    public int compareTo(JEVisObject o) {

        return getID().compareTo(o.getID());
    }

    /**
     * Checks equality based solely on object ID. Two {@link JEVisObject}
     * instances are considered equal if they share the same ID.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is a {@link JEVisObject} with the same ID
     */
    @Override
    public boolean equals(Object o) {
        try {
            if (o instanceof JEVisObject) {
                return ((JEVisObject) o).getID().equals(getID());
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        return "JEVisObjectWS [ id: '" + getID() + "' name: '" + getName() + "' jclass: '" + getJEVisClassName() + "']";
    }

    private Set<JEVisClass> getInheritanceClasses(Set<JEVisClass> hashSet, JEVisClass obj) {
        try {
            JEVisClass inheritance = obj.getInheritance();
            if (inheritance == null) {
                return hashSet;
            } else {
                hashSet.add(inheritance);
                return getInheritanceClasses(hashSet, inheritance);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return hashSet;
    }

    public JsonObject toJSON() {
        return json;
    }
}
