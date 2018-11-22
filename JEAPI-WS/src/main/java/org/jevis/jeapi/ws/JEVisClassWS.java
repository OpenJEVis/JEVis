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

import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
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

    public JEVisClassWS(JEVisDataSourceWS ds, JsonJEVisClass json) {
        this.ds = ds;
        this.json = json;
    }

    /**
     * TMP solution
     * <p>
     * TODO: remove, does not belog here
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
     * TODO: remove, does not belog here
     */
    public static Image getImage(String icon) {
        try {
            return new Image(JEVisClassWS.class.getResourceAsStream("/" + icon));
        } catch (Exception ex) {
            logger.info("Could not load icon: " + "/icons/   " + icon);
            return new Image(JEVisClassWS.class.getResourceAsStream("/icons/1393355905_image-missing.png"));
        }
    }

    @Override
    public void addEventListener(JEVisEventListener listener) {
        listeners.add(JEVisEventListener.class, listener);
    }

    @Override
    public void removeEventListener(JEVisEventListener listener) {
        listeners.remove(JEVisEventListener.class, listener);
    }

    @Override
    public synchronized void notifyListeners(JEVisEvent event) {

        for (JEVisEventListener l : listeners.getListeners(JEVisEventListener.class)) {
            l.fireEvent(event);
        }
    }

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

    @Override
    public boolean deleteType(String type) {
        //TODO re-implement
        return false;
    }

    @Override
    public String getName() {
        return this.json.getName();
    }

    @Override
    public void setName(String name) {
        this.json.setName(name);
    }

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

    @Override
    public void setIcon(BufferedImage icon) {
        this.image = icon;
        iconChanged = true;

    }

    @Override
    public String getDescription() {
        return json.getDescription();
    }

    @Override
    public void setDescription(String description) {
        json.setDescription(description);
    }

    @Override
    public List<JEVisType> getTypes() {

        if (types == null && json.getTypes() != null) {
            types = new ArrayList<>();
            for (JsonType t : json.getTypes()) {
                types.add(new JEVisTypeWS(ds, t, getName()));
            }
        }
        if (types == null) {
            types = new ArrayList<>();
        }

        return types;
    }

    @Override
    public JEVisType getType(String typename) throws JEVisException {

        for (JEVisType type : getTypes()) {
            if (type.getName().equals(typename)) {
                return type;
            }
        }

        return null;

    }

    @Override
    public JEVisType buildType(String name) {
        JEVisType newType = new JEVisTypeWS(ds, name, getName());
        getTypes().add(newType);//not save, waht will happen if the user does not commit() the type
        return newType;

    }

    @Override
    public JEVisClass getInheritance() throws JEVisException {
        for (JEVisClassRelationship crel : getRelationships()) {
            if (crel.isType(JEVisConstants.ClassRelationship.INHERIT) && crel.getStart().getName().equals(getName())) {
                return crel.getEnd();
            }
        }
        return null;
    }

    @Override
    public List<JEVisClass> getHeirs() throws JEVisException {
        List<JEVisClass> heirs = new LinkedList<JEVisClass>();
        for (JEVisClassRelationship cr : getRelationships(JEVisConstants.ClassRelationship.INHERIT, JEVisConstants.Direction.BACKWARD)) {
            try {
                heirs.add(cr.getStart());
                heirs.addAll(cr.getStart().getHeirs());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return heirs;
    }

    @Override
    public List<JEVisClass> getValidParents() throws JEVisException {
        List<JEVisClass> vaildParents = new LinkedList<JEVisClass>();

        if (getInheritance() != null) {
            vaildParents.addAll(getInheritance().getValidParents());
        }

        for (JEVisClassRelationship rel : getRelationships()) {
            try {
                if (rel.isType(JEVisConstants.ClassRelationship.OK_PARENT)
                        && rel.getStart().equals(this)) {
                    if (!vaildParents.contains(rel.getOtherClass(this))) {
                        vaildParents.add(rel.getOtherClass(this));
                    }
                    vaildParents.addAll(rel.getOtherClass(this).getHeirs());

                }
            } catch (Exception ex) {

            }
        }


        Collections.sort(vaildParents);

        return vaildParents;
    }

    @Override
    public boolean isAllowedUnder(JEVisClass jevisClass) throws JEVisException {
        List<JEVisClass> vaild = getValidParents();
        for (JEVisClass pClass : vaild) {
            if (pClass.getName().equals(jevisClass.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUnique() {
        return json.getUnique();
    }

    @Override
    public void setUnique(boolean unique) {
        json.setUnique(unique);
    }

    @Override
    public boolean delete() {
        return ds.deleteClass(getName());
    }

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

    @Override
    public void deleteRelationship(JEVisClassRelationship rel) throws JEVisException {
        ds.deleteClassRelationship(rel.getStartName(), rel.getEndName(), rel.getType());

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }


    private void commitIcontoWS() {
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
            logger.error("commit icon: " + responseCode);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
        }
    }

    @Override
    public void commit() throws JEVisException {
        try {

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + getName();

            Gson gson = new Gson();
            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));

            JsonJEVisClass newJson = gson.fromJson(response.toString(), JsonJEVisClass.class);
            this.json = newJson;

            if (iconChanged) {
                commitIcontoWS();
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }


        ds.reloadClasses();
    }

    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        //TODO: class compare
        return false;
    }

    @Override
    public int compareTo(JEVisClass o) {
        try {
            return getName().compareTo(o.getName());
        } catch (JEVisException ex) {
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
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
