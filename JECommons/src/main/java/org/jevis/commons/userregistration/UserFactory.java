/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
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
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.userregistration;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.relationship.RelationshipFactory;
import org.jevis.commons.unit.JEVisUnitImp;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UserFactory {

    /**
     * Very basic implementaion of an factory to create an default demo user
     *
     * @TODO implement an rollback function
     * @TODO use some public static names for the classes and types
     * @TODO add dataprocessing
     * @TODO check if user exists allready
     *
     * @param ds
     * @param parentOrgaDir
     * @param username
     * @param password
     * @param email
     * @param firstName
     * @param lastName
     * @param orgaNmae
     * @param demoGroups
     * @return
     */
    public static boolean buildMobileDemoStructure(JEVisDataSource ds, JEVisObject parentOrgaDir, String username, String password, String email, String firstName, String lastName, String orgaNmae, List<JEVisObject> demoGroups) {

        try {
            //Create Organization
            JEVisObject myNewOrganisation = parentOrgaDir.buildObject(orgaNmae, ds.getJEVisClass("Organization"));

            //Create Administration Dir
            JEVisObject adminDir = myNewOrganisation.buildObject("Administration Directory", ds.getJEVisClass("Administration Directory"));

            //Create User Dir
            JEVisObject userDir = adminDir.buildObject("User Directory", ds.getJEVisClass("User Directory"));
            JEVisObject user = userDir.buildObject(username, ds.getJEVisClass("User"));
            JEVisAttribute passAtt = user.getAttribute("Password");
            JEVisSample ps = passAtt.buildSample(new DateTime(), password);
            ps.commit();

            JEVisAttribute userEnabledAtt = user.getAttribute("Enabled");
            JEVisSample enable = userEnabledAtt.buildSample(new DateTime(), true);
            enable.commit();

            JEVisAttribute firstname = user.getAttribute("First Name");
            JEVisSample firstnameSample = firstname.buildSample(new DateTime(), firstName);
            firstnameSample.commit();

            JEVisAttribute lastname = user.getAttribute("Last Name");
            JEVisSample lastnameSample = lastname.buildSample(new DateTime(), lastName);
            lastnameSample.commit();

            JEVisAttribute emailOIbj = user.getAttribute("E-Mail");
            JEVisSample emaiLSample = emailOIbj.buildSample(new DateTime(), email);
            emaiLSample.commit();

            //Create Group dir
            //-- Put User to group
            //-- Set Root
            JEVisObject groupDir = adminDir.buildObject("Group Directory", ds.getJEVisClass("Group Directory"));
            JEVisObject group = groupDir.buildObject("My Group", ds.getJEVisClass("Group"));

            //-- enable
            //-- setPW
            //Create Monitored Object Directory
            JEVisObject monitoredObjectDir = myNewOrganisation.buildObject("Monitored Object Directory", ds.getJEVisClass("Monitored Object Directory"));

            //Create Building Dir
            JEVisObject building = monitoredObjectDir.buildObject("My Building", ds.getJEVisClass("Building"));

            //Create Data Dir
            //ToDo Set Unit
            JEVisObject dataDir = building.buildObject("Data Directory", ds.getJEVisClass("Data Directory"));
            JEVisObject dataElectric = dataDir.buildObject("Electricity Main Meter", ds.getJEVisClass("Data"));
            JEVisObject dataHeat = dataDir.buildObject("Heat Main Meter", ds.getJEVisClass("Data"));
            JEVisObject dataOutdoor = dataDir.buildObject("Outdoor Temperature", ds.getJEVisClass("Data"));
            JEVisObject dataWater = dataDir.buildObject("Water Main Meter", ds.getJEVisClass("Data"));

//            JEVisObject dpEl = dataElectric.buildObject("Transformer", ds.getJEVisClass("Data Processor"));
//            JEVisAttribute taskEl = dpEl.getAttribute("Task Description");
//            JEVisSample taskSampleEl = taskEl.buildSample(new DateTime(), buildComulativProcessor(dataElectric));
//            taskSampleEl.commit();
//
//            JEVisObject dpHeat = dataHeat.buildObject("Transformer", ds.getJEVisClass("Data Processor"));
//            JEVisAttribute taskHeat = dpHeat.getAttribute("Task Description");
//            JEVisSample taskSampleHeat = taskHeat.buildSample(new DateTime(), buildComulativProcessor(dataHeat));
//            taskSampleHeat.commit();
//
//            JEVisObject spWater = dataWater.buildObject("Transformer", ds.getJEVisClass("Data Processor"));
//            JEVisAttribute taskWater = spWater.getAttribute("Task Description");
//            JEVisSample taskSampleWater = taskWater.buildSample(new DateTime(), buildComulativProcessor(dataWater));
//            taskSampleWater.commit();
            //Set Units
            dataOutdoor.getAttribute("Value").setInputUnit(new JEVisUnitImp(SI.CELSIUS));
            dataOutdoor.getAttribute("Value").setDisplayUnit(new JEVisUnitImp(SI.CELSIUS));
            dataOutdoor.commit();

            dataElectric.getAttribute("Value").setInputUnit(new JEVisUnitImp(SI.WATT.times(NonSI.HOUR), "Wh", JEVisUnit.Prefix.KILO));
            dataElectric.getAttribute("Value").setDisplayUnit(new JEVisUnitImp(SI.WATT.times(NonSI.HOUR), "Wh", JEVisUnit.Prefix.KILO));
            dataElectric.commit();

            dataHeat.getAttribute("Value").setInputUnit(new JEVisUnitImp(SI.WATT.times(NonSI.HOUR), "Wh", JEVisUnit.Prefix.KILO));
            dataHeat.getAttribute("Value").setDisplayUnit(new JEVisUnitImp(SI.WATT.times(NonSI.HOUR), "Wh", JEVisUnit.Prefix.KILO));
            dataHeat.commit();

            dataWater.getAttribute("Value").setInputUnit(new JEVisUnitImp(SI.CUBIC_METRE));
            dataWater.getAttribute("Value").setDisplayUnit(new JEVisUnitImp(SI.CUBIC_METRE));
            dataWater.commit();

            //Add the Userrights
            RelationshipFactory.buildOwnership(group, myNewOrganisation, true);
            RelationshipFactory.buildMembership(group, user, JEVisConstants.ObjectRelationship.MEMBER_READ);
            RelationshipFactory.buildMembership(group, user, JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            RelationshipFactory.buildRoot(group, myNewOrganisation);

            for (JEVisObject demoObj : demoGroups) {
                RelationshipFactory.buildMembership(demoObj, user, JEVisConstants.ObjectRelationship.MEMBER_READ);
            }

            return true;
        } catch (JEVisException ex) {
            Logger.getLogger(UserFactory.class.getName()).log(Level.SEVERE, null, ex);

        }

        return false;
    }

    private static String buildComulativProcessor(JEVisObject obj) {
        return "\"processor\":\"Counter Processor\",\"options\":{},\"tasks\":[{\"processor\":\"Input\",\"options\":{\"attribute-id\":\"Value\",\"object-id\":\"" + obj.getID() + "\"},\"tasks\":[],\"id\":\"Import\"}],\"id\":\"Diff\"}";

    }

}
