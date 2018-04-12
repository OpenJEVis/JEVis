/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.relationship;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;

/**
 * The Class helps building the most commen types of JEVisClassRealtionships
 * between JEVisClasses.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RelationshipFactory {

    /**
     * Build an inheritance relationship between an superclass and its inherit
     * subclass
     *
     * @param superclass The superclass where the subclass is inherit its
     * attributes from.
     * @param subclass The subclass which inherit the attributes from the
     * suberclass.
     * @return The new JEVisClassRealtionship between this classes
     * @throws org.jevis.api.JEVisException
     */
    public static JEVisClassRelationship buildInheritance(JEVisClass superclass, JEVisClass subclass) throws JEVisException {
        return superclass.buildRelationship(subclass, JEVisConstants.ClassRelationship.INHERIT, JEVisConstants.Direction.BACKWARD);
    }

    /**
     * Build an membership relationship between an Group and User.
     *
     * @param group the group where the user should be an menber of
     * @param user User who shoul be member of the group
     * @param type Type of the relationship(Read,Write,etc)
     * JEVisConstants.ObjectRelationship
     *
     * @return
     * @throws JEVisException
     */
    public static JEVisRelationship buildMembership(JEVisObject group, JEVisObject user, int type) throws JEVisException {
        return user.buildRelationship(group, type, JEVisConstants.Direction.FORWARD);
    }

    /**
     * Build ownership relationship. This funltion worts with recursion an can
     * also add the right to all children
     *
     *
     * @param group The owner(group) of the objeckt
     * @param obj The object who should be owned by the group
     * @param recursion If ture all children ob the obj will also ge the new
     * owner
     */
    public static void buildOwnership(JEVisObject group, JEVisObject obj, Boolean recursion) {
        try {
            for (JEVisObject children : obj.getChildren()) {
                try {
                    JEVisRelationship newRel = children.buildRelationship(group, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
                    if (recursion) {
                        buildOwnership(group, children, recursion);
                    }
                } catch (JEVisException ex) {
                    Logger.getLogger(RelationshipFactory.class.getName()).log(Level.WARNING, "Error while creating userright", ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RelationshipFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Build an Root relationship group and its root
     *
     * @param group the group the root belongs to
     * @param rootObject the root directory
     * @throws JEVisException
     */
    public static void buildRoot(JEVisObject group, JEVisObject rootObject) throws JEVisException {
        group.buildRelationship(rootObject, JEVisConstants.ObjectRelationship.ROOT, JEVisConstants.Direction.FORWARD);
    }

}
