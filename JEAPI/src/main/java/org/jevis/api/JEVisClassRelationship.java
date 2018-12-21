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
 * This interface models the relationship between two JEVisClasses
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisClassRelationship extends JEVisComponent {

    /**
     * Returns the start JEVisClass of this relationship
     *
     * @return start JEVisCalss
     * @throws JEVisException
     */
    JEVisClass getStart() throws JEVisException;

    String getStartName() throws JEVisException;

    /**
     * Returns the end JEVisClass of this relationship
     *
     * @return end JEVisClass
     * @throws JEVisException
     */
    JEVisClass getEnd() throws JEVisException;

    String getEndName() throws JEVisException;

    /**
     * Returns the type of the relationship.
     *
     * @return the type
     * @throws JEVisException
     */
    int getType() throws JEVisException;

    /**
     * Returns both JEVisClass
     *
     * @return both JEVisClass
     * @throws JEVisException
     */
    JEVisClass[] getJEVisClasses() throws JEVisException;

    /**
     * Returns the other JEVIClass
     *
     * @param jclass
     * @return the other JEVIClass
     * @throws JEVisException
     */
    JEVisClass getOtherClass(JEVisClass jclass) throws JEVisException;

    /**
     * Returns the name of the other Class in this relationship
     *
     * @param name
     * @return
     * @throws JEVisException
     */
    String getOtherClassName(String name) throws JEVisException;

    /**
     * Check the type
     *
     * @param type The type to compare to
     * @return <CODE>true</CODE> if its the same type
     * @throws JEVisException
     */
    boolean isType(int type) throws JEVisException;

    /**
     * return if this Relationship is inherited from another class
     *
     * @return CODE>true</CODE> if is inherited
     * @throws JEVisException
     */
    boolean isInherited() throws JEVisException;

}
