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

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisType extends JEVisComponent, JEVisCommittable, Comparable<JEVisType> {

    /**
     * Returns the name of this type. The name is a unique identifier for a
     * type. The name does not have to be unique in the JEVis system but has to
     * be under an JEVisClass.
     *
     * @return
     * @throws JEVisException
     */
    String getName() throws JEVisException;

    /**
     * Set the name for this type. The name is an unique identifier for an type.
     * The name does not have to be unique in the JEVis system but has to be
     * under an JEVisClass.
     *
     * @param name
     * @throws JEVisException
     */
    void setName(String name) throws JEVisException;

    /**
     * Returns the primitive type.
     *
     * @see JEVisConstants
     * @return
     * @throws JEVisException
     */
    int getPrimitiveType() throws JEVisException;

    /**
     * Set the primitive type.
     *
     * @see JEVisConstants
     * @param type
     * @throws JEVisException
     */
    void setPrimitiveType(int type) throws JEVisException;

    /**
     * Returns the GUI display type. GUIs will use this type to display the
     * value, for example a String could be displayed as asterisk textfield or
     * clear text.
     *
     * @return
     * @throws JEVisException
     */
    String getGUIDisplayType() throws JEVisException;

    /**
     * Set the GUI display type.
     *
     * @see JEVisConstants
     * @param type
     * @throws JEVisException
     */
    void setGUIDisplayType(String type) throws JEVisException;

    /**
     * Set the order of the input field for this type in the GUI. The Fields
     * will be sorted from lowest-top to the highest-bottom.
     *
     * @param pos
     * @throws JEVisException
     */
    void setGUIPosition(int pos) throws JEVisException;

    /**
     * Returns positions of this type in the GUI. The Fields will be sorted from
     * lowest-top to the highest-bottom.
     *
     * @return
     * @throws JEVisException
     */
    int getGUIPosition() throws JEVisException;

    /**
     * returns the JEVisClass of this type.
     *
     * @return JEVisClass of this type
     * @throws JEVisException
     */
    JEVisClass getJEVisClass() throws JEVisException;

    /**
     * Returns the JEVisClass name of this type.
     *
     * @return
     * @throws JEVisException
     */
    String getJEVisClassName() throws JEVisException;

    /**
     * Returns the validity. The validity tells the API how to handle die
     * timestamps. For example if only the last value is valid or if every
     * timestamp is valid at this time.
     *
     * @see JEVisConstants
     * @return validity of the sample
     * @throws JEVisException
     */
    int getValidity() throws JEVisException;

    /**
     * Set the validity. The validity tells the API how to handle die
     * timestamps. For example if only the last value is valid or if every
     * timestamp is valid at this time.
     *
     * @see JEVisConstants
     * @param validity
     * @throws JEVisException
     */
    void setValidity(int validity) throws JEVisException;

    /**
     * Return the additional configuration parameter.
     *
     * @deprecated
     * @return
     * @throws JEVisException
     * @deprecated This function is not in use and will be changed?!
     */
    String getConfigurationValue() throws JEVisException;

    /**
     * Set the additional configuration parameter.
     *
     * @param value
     * @throws JEVisException
     * @deprecated This function is not in use and will be changed?!
     */
    void setConfigurationValue(String value) throws JEVisException;

    /**
     * Set the expected unit for this type. All values of attributes from type
     * type will be stored as this unit in the JEVisDataSource.
     *
     * @param unit
     * @throws JEVisException
     */
    void setUnit(JEVisUnit unit) throws JEVisException;

    /**
     * Return the expected unit for this type. All values of attributes from
     * type type will be stored as this unit in the JEVisDataSource.
     *
     * @return
     * @throws JEVisException
     */
    JEVisUnit getUnit() throws JEVisException;

    /**
     * Get the alternative Symbol for the Unit of this type
     *
     * @return
     * @throws JEVisException
     */
    String getAlternativSymbol() throws JEVisException;

    /**
     * Set an alternative symbols for the unit of this type
     *
     * @param symbol
     * @throws JEVisException
     */
    void setAlternativSymbol(String symbol) throws JEVisException;

    /**
     * Returns the human description for the type. The function may be replaced
     * with a localized version.
     *
     * @deprecated
     * @return
     * @throws JEVisException
     */
    String getDescription() throws JEVisException;

    /**
     * Set the human description for the type.
     *
     * @param discription
     * @throws JEVisException
     */
    void setDescription(String discription) throws JEVisException;

    /**
     * Delete this type from the JEVisDataSource. This function does not need a
     * commit;
     *
     * @deprecated use JEVIsClass.DeleteType
     * @return
     * @throws JEVisException
     */
    boolean delete() throws JEVisException;

    /**
     * return true if this type is inherited from an other class
     *
     * @return
     * @throws JEVisException
     */
    boolean isInherited() throws JEVisException;

    /**
     * Set if this Type is inherited from an other class
     *
     * @param inherited
     * @throws JEVisException
     */
    void setInherited(boolean inherited) throws JEVisException;
}
