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
public interface JEVisRelationship {

    /**
     * Returns the start node of the relationship.
     *
     * @return Start node of the relationship
     * @throws org.jevis.api.JEVisException
     */
    JEVisObject getStartObject() throws JEVisException;

    /**
     * Returns the ID of the start Object. This function is resource saveing
     * against the getStartObject() because it has not have to initialize the
     * JEVIsObject
     *
     * @return
     */
    long getStartID();

    /**
     * returns the ID of the end Obejct. This function is resource saveing
     * against the getStartObject() because it has not have to initialize the
     * JEVIsObject
     *
     * @return
     */
    long getEndID();

    /**
     * Returns the end node of the relationship.
     *
     * @return the end node of the relationship
     * @throws org.jevis.api.JEVisException
     */
    JEVisObject getEndObject() throws JEVisException;

    /**
     * Returns both Objects of this relationship
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisObject[] getObjects() throws JEVisException;

    /**
     * Returns the other Object of this relationship.
     *
     * @param object the other JEVisObject
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisObject getOtherObject(JEVisObject object) throws JEVisException;

    /**
     * Returns the type of this relationship
     *
     * @throws org.jevis.api.JEVisException
     * @see org.jevis.jeapi.JEVisConstants.Relationship
     * @return Type of this relationship
     */
    int getType() throws JEVisException;

    /**
     * Checks if this relationship is from the given type
     *
     * @throws org.jevis.api.JEVisException
     * @see org.jevis.jeapi.JEVisConstants.Relationship
     * @param type
     * @return
     */
    boolean isType(int type) throws JEVisException;

    /**
     * Delete this relationship
     */
    void delete();
}
