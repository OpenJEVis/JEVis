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

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.commons.ws.json.JsonType;

import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * WebService-backed implementation of {@link JEVisClass}.
 *
 * <p>A JEVis class defines the schema for JEVis objects: their allowed attribute
 * types, inheritance hierarchy, valid parent/child class relationships, and
 * uniqueness constraints. This implementation fetches class metadata from the
 * JEWebService REST API and caches it locally. Icon data is loaded on demand and
 * uploaded separately when {@link #commit()} is called with a changed icon.</p>
 *
 * <p>Instances are constructed by {@link JEVisDataSourceWS} when class definitions
 * are loaded from the server. They are not intended to be created directly by
 * application code.</p>
 *
 * @author fs
 */
public class JEVisClassWS implements JEVisClass {

    private static final Logger logger = LogManager.getLogger(JEVisClassWS.class);
    private final EventListenerList listeners = new EventListenerList();
    //    private String name = "";
    private JEVisDataSourceWS ds = null;
    private List<JEVisType> types = null;
    //    private String description = "";
//    private boolean isUnique = false;
    private BufferedImage image = null;
    private JsonJEVisClass json;
    private List<JEVisClassRelationship> relations = new ArrayList<>();
    private boolean iconChanged = false;

    /**
     * Constructs a {@code JEVisClassWS} from its JSON DTO.
     *
     * @param ds   the owning data source
     * @param json the raw JSON representation of the class definition
     */
    public JEVisClassWS(JEVisDataSourceWS ds, JsonJEVisClass json) {
        this.ds = ds;
        this.json = json;
    }

    /**
     * TMP solution
     * <p>
     * TODO: remove, does not belong here
     */
    public static ImageView getImage(String icon, double height, double width) {
        ImageView image = new ImageView(getImage(icon));
        image.fitHeightProperty().set(height);
        image.fitWidthProperty().set(width);
        return image;
    }

    /**
     * TMP solution
     * <p>
     * TODO: remove, does not belong here
     */
    public static Image getImage(String icon) {
        try {
            return new Image(JEVisClassWS.class.getResourceAsStream("/" + icon));
        } catch (Exception ex) {
            logger.info("Could not load icon: /icons/{}", icon);
            return new Image(JEVisClassWS.class.getResourceAsStream("/icons/1393355905_image-missing.png"));
        }
    }

    /**
     * Registers a {@link JEVisEventListener} to be notified of changes to this
     * class definition.
     *
     * @param listener the listener to register; must not be {@code null}
     */
    @Override
    public void addEventListener(JEVisEventListener listener) {
        listeners.add(JEVisEventListener.class, listener);
    }

    /**
     * Unregisters a previously added {@link JEVisEventListener}.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeEventListener(JEVisEventListener listener) {
        listeners.remove(JEVisEventListener.class, listener);
    }

    /**
     * Dispatches the given event to all registered listeners synchronously.
     * This method is thread-safe ({@code synchronized}).
     *
     * @param event the event to broadcast; must not be {@code null}
     */
    @Override
    public synchronized void notifyListeners(JEVisEvent event) {

        for (JEVisEventListener l : listeners.getListeners(JEVisEventListener.class)) {
            l.fireEvent(event);
        }
    }


    /**
     * Deletes the type with the given name from this class definition.
     *
     * <p><b>Note:</b> not yet implemented — always returns {@code false}.</p>
     *
     * @param type the name of the type to delete
     * @return {@code false} always
     */
    @Override
    public boolean deleteType(String type) {
        //TODO re-implement
        return false;
    }

    /**
     * Returns the unique name that identifies this JEVis class.
     *
     * @return the class name; never {@code null}
     */
    @Override
    public String getName() {
        return this.json.getName();
    }

    /**
     * Sets the name of this JEVis class. Changes are not persisted until
     * {@link #commit()} is called.
     *
     * @param name the new class name; must not be {@code null}
     */
    @Override
    public void setName(String name) {
        this.json.setName(name);
    }

    /**
     * Returns the icon image for this class. The icon is fetched from the
     * server on first access and cached locally. If no icon is available, a
     * placeholder image is returned and the {@code iconChanged} flag is set so
     * the default is committed on the next {@link #commit()} call.
     *
     * @return the class icon as a {@link BufferedImage}; never {@code null}
     */
    @Override
    public BufferedImage getIcon() {
        if (image == null) {
            image = ds.getClassIcon(json.getName());
        }

        if (image == null) {
            image = SwingFXUtils.fromFXImage(JEVisClassWS.getImage("1472562626_unknown.png", 60, 60).getImage(), null);
            iconChanged = true;
        }
        return image;

    }

    /**
     * Sets the class icon from a PNG file on disk. The image is read
     * immediately; changes are uploaded to the server on the next
     * {@link #commit()} call.
     *
     * @param icon the PNG file to use as the class icon
     */
    @Override
    public void setIcon(File icon) {
        try {
            this.image = ImageIO.read(icon);
            iconChanged = true;
//            logger.info("set icon from file: " + _icon.getWidth());
        } catch (IOException ex) {
            logger.catching(ex);
        }
    }

    /**
     * Sets the class icon directly from a {@link BufferedImage}. Changes are
     * uploaded to the server on the next {@link #commit()} call.
     *
     * @param icon the image to use as the class icon; must not be {@code null}
     */
    @Override
    public void setIcon(BufferedImage icon) {
        this.image = icon;
        iconChanged = true;

    }

    /**
     * Returns the human-readable description of this JEVis class.
     *
     * @return the class description; may be {@code null} if not set
     */
    @Override
    public String getDescription() {
        return json.getDescription();
    }

    /**
     * Sets the human-readable description for this JEVis class. Changes are
     * not persisted until {@link #commit()} is called.
     *
     * @param description the description text; may be {@code null}
     */
    @Override
    public void setDescription(String description) {
        json.setDescription(description);
    }

    /**
     * Returns all attribute types defined for this JEVis class, sorted by
     * GUI position. The type list is lazily initialized from the JSON DTO on
     * first access.
     *
     * @return a sorted list of {@link JEVisType} instances; never {@code null}
     */
    @Override
    public List<JEVisType> getTypes() {

        if (types == null && json.getTypes() != null) {
            types = new ArrayList<>();
            for (JsonType t : json.getTypes()) {
                types.add(new JEVisTypeWS(ds, t, getName()));
            }

            types.sort(Comparator.comparingInt(jeVisType -> {
                try {
                    return jeVisType.getGUIPosition();
                } catch (Exception e) {
                    logger.error("Error while sorting gui types. ", e);
                }
                return 0;
            }));
        }
        if (types == null) {
            types = new ArrayList<>();
        }

        return types;
    }

    /**
     * Returns the type with the given name, or {@code null} if no such type
     * is defined for this class.
     *
     * @param typename the exact name of the type to look up
     * @return the matching {@link JEVisType}, or {@code null}
     * @throws JEVisException if the type list cannot be retrieved
     */
    @Override
    public JEVisType getType(String typename) throws JEVisException {

        for (JEVisType type : getTypes()) {
            if (type.getName().equals(typename)) {
                return type;
            }
        }

        return null;

    }

    /**
     * Creates a new attribute type with the given name and adds it to this
     * class definition. The type is not persisted until {@link #commit()} is
     * called.
     *
     * @param name the name for the new type; must not be {@code null}
     * @return the newly created {@link JEVisType}
     */
    @Override
    public JEVisType buildType(String name) {
        JEVisType newType = new JEVisTypeWS(ds, name, getName());
        getTypes().add(newType);//not save, waht will happen if the user does not commit() the type
        return newType;

    }

    /**
     * Returns the parent class that this class inherits from, or {@code null}
     * if this class has no parent (i.e., it is a root class in the hierarchy).
     *
     * @return the inherited superclass, or {@code null}
     * @throws JEVisException if the class relationships cannot be retrieved
     */
    @Override
    public JEVisClass getInheritance() throws JEVisException {
        for (JEVisClassRelationship crel : getRelationships()) {
            try {
                if (crel.isType(JEVisConstants.ClassRelationship.INHERIT) && crel.getStart().getName().equals(getName())) {
                    return crel.getEnd();
                }
            } catch (Exception ex) {
                logger.error("Error in relationship {}", crel, ex);
            }
        }
        return null;
    }

    /**
     * Returns all classes that directly or transitively inherit from this class
     * (i.e., the full set of subclasses in the inheritance hierarchy).
     *
     * @return a list of heir classes; never {@code null}
     * @throws JEVisException if the class relationships cannot be retrieved
     */
    @Override
    public List<JEVisClass> getHeirs() throws JEVisException {
        List<JEVisClass> heirs = new LinkedList<JEVisClass>();
        for (JEVisClassRelationship cr : getRelationships(JEVisConstants.ClassRelationship.INHERIT, JEVisConstants.Direction.BACKWARD)) {
            try {
                heirs.add(cr.getStart());
                if (cr.getStart().getHeirs() != null) {
                    heirs.addAll(cr.getStart().getHeirs());
                }
            } catch (Exception ex) {
                logger.error("Error in relationship {}", cr);
            }
        }
        return heirs;
    }

    /**
     * Returns all JEVis classes under which objects of this class are permitted
     * to be created (i.e., valid parent classes). Inherited valid-parent
     * relationships from the superclass hierarchy are included. The returned
     * list is sorted by class name.
     *
     * @return a sorted list of valid parent classes; never {@code null}
     * @throws JEVisException if the class relationships cannot be retrieved
     */
    @Override
    public List<JEVisClass> getValidParents() throws JEVisException {
        List<JEVisClass> validParents = new LinkedList<JEVisClass>();

        if (getInheritance() != null) {
            validParents.addAll(getInheritance().getValidParents());
        }

        for (JEVisClassRelationship rel : getRelationships()) {
            try {
                if (rel.isType(JEVisConstants.ClassRelationship.OK_PARENT)
                        && rel.getStart().equals(this)) {
                    if (!validParents.contains(rel.getOtherClass(this))) {
                        JEVisClass otherClass = rel.getOtherClass(this);
                        if (otherClass != null) {
                            validParents.add(otherClass);
                        }
                    }
                    List<JEVisClass> heirs = rel.getOtherClass(this).getHeirs();
                    if (heirs != null) {
                        validParents.addAll(heirs);
                    }

                }
            } catch (Exception ex) {

            }
        }


        Collections.sort(validParents);

        return validParents;
    }

    /**
     * Returns all JEVis classes that are permitted to be created as direct
     * children of objects of this class. Directory classes may always appear
     * under another directory of the same type.
     *
     * @return a sorted list of valid child classes; never {@code null}
     * @throws JEVisException if the class relationships cannot be retrieved
     */
    @Override
    public List<JEVisClass> getValidChildren() throws JEVisException {
        List<JEVisClass> validParents = new LinkedList<>();
        for (JEVisClassRelationship rel : getRelationships()) {
            try {
                if (rel.isType(JEVisConstants.ClassRelationship.OK_PARENT)
                        && rel.getEnd().equals(this)) {
                    if (!validParents.contains(rel.getOtherClass(this))) {
                        if (!validParents.contains(rel.getOtherClass(this))) {
                            validParents.add(rel.getOtherClass(this));
                        }
                        //We do not want heirs, every class has added by rule to have more control
                        //validParents.addAll(rel.getOtherClass(this).getHeirs());
                    }

                }


            } catch (Exception ex) {
                logger.error("An JEClassRelationship had an error for '{}': {}", getName(), ex);
            }
        }
        //Special rule, for order purpose its allows to create on directory under him self.
        if (ds.getJEVisClass("Directory").getHeirs().contains(this) && !validParents.contains(this)) {
            validParents.add(this);
        }


        Collections.sort(validParents);
        return validParents;
    }

    /**
     * Returns {@code true} if objects of this class are permitted to be
     * created as children of objects of {@code jevisClass}.
     *
     * @param jevisClass the prospective parent class
     * @return {@code true} if this class is a valid child of {@code jevisClass}
     * @throws JEVisException if the valid-parents list cannot be computed
     */
    @Override
    public boolean isAllowedUnder(JEVisClass jevisClass) throws JEVisException {
        List<JEVisClass> valid = getValidParents();
        for (JEVisClass pClass : valid) {
            if (pClass.getName().equals(jevisClass.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether only one object of this class may exist under any given
     * parent. Unique classes prevent duplicate instances under the same parent
     * object.
     *
     * @return {@code true} if this class is unique
     */
    @Override
    public boolean isUnique() {
        return json.getUnique();
    }

    /**
     * Sets the uniqueness constraint for this class. Changes are not persisted
     * until {@link #commit()} is called.
     *
     * @param unique {@code true} to allow only one instance per parent
     */
    @Override
    public void setUnique(boolean unique) {
        json.setUnique(unique);
    }

    /**
     * Deletes this JEVis class from the system. This is an irreversible
     * operation — all objects of this class will also be affected.
     *
     * @return {@code true} if the deletion succeeded
     */
    @Override
    public boolean delete() {
        return ds.deleteClass(getName());
    }

    /**
     * Returns all class-level relationships for this class (e.g., inheritance,
     * valid-parent constraints). The list is lazily populated from the JSON DTO
     * on first access.
     *
     * @return a list of class relationships; never {@code null}
     */
    @Override
    public List<JEVisClassRelationship> getRelationships() {
        if (relations.isEmpty() && json.getRelationships() != null) {
            for (JsonClassRelationship crel : json.getRelationships()) {
                relations.add(new JEVisClassRelationshipWS(ds, crel));
            }
        }

        if (relations == null) {
            relations = new ArrayList<>();
        }

        return relations;
    }

    /**
     * Returns all class relationships of the specified type.
     *
     * @param type the relationship type constant (see {@link JEVisConstants.ClassRelationship})
     * @return a filtered list of matching class relationships; never {@code null}
     * @throws JEVisException if the relationship list cannot be retrieved
     */
    @Override
    public List<JEVisClassRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisClassRelationship> tmp = new LinkedList<>();

        for (JEVisClassRelationship cr : getRelationships()) {
            if (cr.isType(type)) {
                tmp.add(cr);
            }
        }

        return tmp;
    }

    /**
     * Returns all class relationships of the specified type and direction.
     *
     * @param type      the relationship type constant
     * @param direction {@link JEVisConstants.Direction#FORWARD} if this class is the start,
     *                  {@link JEVisConstants.Direction#BACKWARD} if this class is the end
     * @return a filtered list of matching class relationships; never {@code null}
     * @throws JEVisException if the relationship list cannot be retrieved
     */
    @Override
    public List<JEVisClassRelationship> getRelationships(int type, int direction) throws JEVisException {
        List<JEVisClassRelationship> tmp = new LinkedList<JEVisClassRelationship>();

        for (JEVisClassRelationship cr : getRelationships(type)) {
            if (direction == JEVisConstants.Direction.FORWARD && cr.getStart().equals(this)) {
                tmp.add(cr);
            } else if (direction == JEVisConstants.Direction.BACKWARD && cr.getEnd().equals(this)) {
                tmp.add(cr);
            }
        }

        return tmp;
    }

    /**
     * Creates a new class relationship between this class and {@code jclass}.
     * For {@link JEVisConstants.Direction#FORWARD FORWARD} direction the
     * relationship is {@code this → jclass}; for
     * {@link JEVisConstants.Direction#BACKWARD BACKWARD} it is
     * {@code jclass → this}.
     *
     * @param jclass    the other class in the relationship
     * @param type      the relationship type constant
     * @param direction the direction of the relationship from this class's perspective
     * @return the newly created {@link JEVisClassRelationship}
     * @throws JEVisException if the server request fails
     */
    @Override
    public JEVisClassRelationship buildRelationship(JEVisClass jclass, int type, int direction) throws JEVisException {
        JEVisClassRelationship rel;
        if (direction == JEVisConstants.Direction.FORWARD) {//this to otherClass
            rel = ds.buildClassRelationship(this.getName(), jclass.getName(), type);
        } else {
            rel = ds.buildClassRelationship(jclass.getName(), this.getName(), type);
        }

        return rel;
    }

    /**
     * Removes the given class relationship from the server.
     *
     * @param rel the relationship to delete
     * @throws JEVisException if the server request fails
     */
    @Override
    public void deleteRelationship(JEVisClassRelationship rel) throws JEVisException {
        ds.deleteClassRelationship(rel.getStartName(), rel.getEndName(), rel.getType());

    }

    /**
     * Returns the {@link JEVisDataSource} this class definition belongs to.
     *
     * @return the owning data source; never {@code null}
     */
    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }


    private void commitIconToWS() {
        try {
            logger.info("post icon");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(getIcon(), "png", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + getName() + "/"
                    + REQUEST.CLASSES.ICON.PATH;

            HttpURLConnection connection = ds.getHTTPConnection().getPostIconConnection(resource);

            try (OutputStream os = connection.getOutputStream()) {

                os.write(imageInByte);
                os.flush();
                os.close();
            }
//
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            logger.error("commit icon: {}", responseCode);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
        }
    }

    /**
     * Persists all pending changes for this class definition to the server via
     * a POST request. If the icon has been changed, it is uploaded in a
     * separate request afterwards. The local JSON DTO is updated with the
     * server's response, and the data source's class cache is refreshed.
     *
     * @throws JEVisException if the server request fails or the response cannot be parsed
     */
    @Override
    public void commit() throws JEVisException {
        try {

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + getName();

//            Gson gson = new Gson();

            StringBuffer response = ds.getHTTPConnection().postRequest(resource, this.ds.getObjectMapper().writeValueAsString(json));

            this.json = this.ds.getObjectMapper().readValue(response.toString(), JsonJEVisClass.class);

            if (iconChanged) {
                commitIconToWS();
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }


        ds.reloadClasses();
    }

    /**
     * Rolls back any uncommitted changes to this class definition.
     *
     * @throws UnsupportedOperationException always — not yet implemented
     */
    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns whether this class definition has uncommitted local changes.
     *
     * <p><b>Note:</b> change detection is not yet implemented — always returns
     * {@code false}.</p>
     *
     * @return {@code false} always
     */
    @Override
    public boolean hasChanged() {
        //TODO: class compare
        return false;
    }

    /**
     * Compares this class to another {@link JEVisClass} alphabetically by name.
     *
     * @param o the other class to compare to
     * @return a negative, zero, or positive integer as this class name is
     * lexicographically less than, equal to, or greater than the other's
     */
    @Override
    public int compareTo(JEVisClass o) {
        try {
            return getName().compareTo(o.getName());
        } catch (JEVisException ex) {
            return 1;
        }
    }

    /**
     * Checks equality based solely on class name. Two {@link JEVisClass}
     * instances are considered equal if they share the same name.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is a {@link JEVisClass} with the same name
     */
    @Override
    public boolean equals(Object o) {
        try {
            /**
             * cast needs to be removed
             */

            if (o instanceof JEVisClass) {
                JEVisClass obj = (JEVisClass) o;
                if (obj.getName().equals(getName())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.info("error, cannot compare objects");
            return false;
        }
        return false;
    }

}
