/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI.
 *
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisClass extends JEVisComponent, JEVisCommittable, Comparable<JEVisClass> {

    /**
     * Get the name of this JEVisClass. Every class-name is unique.
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    String getName() throws JEVisException;

    /**
     * Set The name of this JEVisClass. The class-name has to be unique.
     *
     * @param name
     * @throws org.jevis.api.JEVisException
     */
    void setName(String name) throws JEVisException;

    /**
     * Get the Icon representing this JEVisClass.
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    BufferedImage getIcon() throws JEVisException;

    /**
     * Set the Icon representing this JEVisClass
     *
     * @param icon
     * @throws org.jevis.api.JEVisException
     */
    void setIcon(BufferedImage icon) throws JEVisException;

    /**
     * Set the Icon representing this JEVisClass
     *
     * @param icon
     * @throws org.jevis.api.JEVisException
     */
    void setIcon(File icon) throws JEVisException;

    /**
     * Get the description for this JEVisClass. The description is a help text
     * for the end user.
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    String getDescription() throws JEVisException;

    /**
     * Set the description for this JEVisClass.
     *
     * @param discription
     * @throws org.jevis.api.JEVisException
     */
    void setDescription(String discription) throws JEVisException;

    /**
     * Get all types which are present for this JEVisClass
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisType> getTypes() throws JEVisException;

    /**
     * Get a specific type by its unique name.
     *
     * @param typename
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisType getType(String typename) throws JEVisException;

    /**
     * Build and add a new type under this JEVisClass. Every type has to be
     * unique under a JEVisClass.
     *
     * @param name
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisType buildType(String name) throws JEVisException;

    /**
     * Get the inheritance class. This JEVisClass will inherit all types from
     * the parent class. If the JEVIsClass does not have an inheritance it will
     * return NULL
     *
     * @return Inheritance JEVisClass, null if it does not have an inheritance
     * @throws org.jevis.api.JEVisException
     */
    JEVisClass getInheritance() throws JEVisException;

    /**
     * Get all heir classes.
     *
     * @return List of the heirs of this JEVisClass
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisClass> getHeirs() throws JEVisException;

    /**
     * Get the list of all parents this class is allowed under
     *
     * @return List of valid parents, empty list if none exists
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisClass> getValidParents() throws JEVisException;

    /**
     * Get the list of all classes where an object from this class can be
     * created under.
     *
     * @return
     * @throws JEVisException
     */
    List<JEVisClass> getValidChildren() throws JEVisException;

    /**
     * Check if this JEVisClass is allowed under the given JEVisClass
     *
     * @param jevisClass
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean isAllowedUnder(JEVisClass jevisClass) throws JEVisException;

    /**
     * Check if this JEVisClass has to be unique under one JEVisObject. This
     * function allows to control the structure of the JEVis tree
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean isUnique() throws JEVisException;

    /**
     * Set if the JEVisClass is unique under another JEVisObject
     *
     * @param unique
     * @throws org.jevis.api.JEVisException
     */
    void setUnique(boolean unique) throws JEVisException;

    /**
     * Delete this JEVisClass.
     *
     * @deprecated Use JEVisDataSource.deleteClass
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean delete() throws JEVisException;

    /**
     * Delete an JEVisType of this class
     *
     * @param type
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean deleteType(String type) throws JEVisException;

    /**
     * Return all relationships this class has
     *
     * @return A list of relationships
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisClassRelationship> getRelationships() throws JEVisException;

    /**
     * Return all relationships from the given type
     *
     * @param type
     * @return all relationships from the given type
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisClassRelationship> getRelationships(int type) throws JEVisException;

    /**
     * Return all relationships from the given type and direction
     *
     * @param type {@link org.jevis.jeapi.JEVisConstants.Relationship}
     * @param direction if Forward this class has to be the start, if Backward
     * the class has to be the end
     * @return all relationships from the given type and direction
     * @throws org.jevis.api.JEVisException
     */
    List<JEVisClassRelationship> getRelationships(int type, int direction) throws JEVisException;

    /**
     * Create and commit relationship to another JEVisClass
     *
     * @param jclass
     * @param type {@link org.jevis.jeapi.JEVisConstants.Relationship}
     * @param direction {@link org.jevis.jeapi.JEVisConstants.Direction}
     * @return the new relationship
     * @throws org.jevis.api.JEVisException
     */
    JEVisClassRelationship buildRelationship(JEVisClass jclass, int type, int direction) throws JEVisException;

    /**
     * Delete a relationship for this class
     *
     * @param rel
     * @throws JEVisException
     */
    void deleteRelationship(JEVisClassRelationship rel) throws JEVisException;
    
    void addEventListener(JEVisEventListener listener);
    void removeEventListener(JEVisEventListener listener);

    /**
     *
     * @param event
     */
    public void notifyListeners(JEVisEvent event);
}
