/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import jersey.repackaged.com.google.common.collect.Lists;
import org.jevis.api.*;
import org.jevis.commons.user.UserRightManager;

import java.util.List;

/**
 *
 * @author fs
 */
public class JEVisUserWS implements JEVisUser {

    private String firstName = "";
    private String lastName = "";
    private boolean isSysAdmin = false;
    private JEVisDataSourceWS ds;
    private JEVisObjectWS obj;
    private boolean enabled = false;
    private UserRightManager urm;
    /** List of classes which can be updated with special rules **/
    private final List<String> executeUpdateExceptions = Lists.newArrayList(new String[]{"Data Notes","User Data","Clean Data"});

    public JEVisUserWS(JEVisDataSourceWS ds, JEVisObjectWS obj) throws Exception {
        this.ds = ds;
        this.obj = obj;
        this.urm = new UserRightManager(ds, this);
        fetchData();
    }

    private void fetchData() throws JEVisException {
        for (JEVisAttribute att : obj.getAttributes()) {
            switch (att.getName()) {
                case JEVisConstants.Attribute.USER_SYS_ADMIN:
                    JEVisSample sysAdminAtt = att.getLatestSample();
                    if (sysAdminAtt != null) {
                        isSysAdmin = sysAdminAtt.getValueAsBoolean();
                    } else {
                        isSysAdmin = false;
                    }

                    break;
                case JEVisConstants.Attribute.USER_FIRST_NAME:
                    JEVisSample firstNameAtt = att.getLatestSample();
                    if (firstNameAtt != null) {
                        firstName = firstNameAtt.getValueAsString();
                    } else {
                        firstName = "";
                    }
                    break;
                case JEVisConstants.Attribute.USER_LAST_NAME:
                    JEVisSample lastNameAtt = att.getLatestSample();
                    if (lastNameAtt != null) {
                        lastName = lastNameAtt.getValueAsString();
                    } else {
                        lastName = "";
                    }
                    break;
                case JEVisConstants.Attribute.USER_ENABLED:
                    //can only be true in the moment because if not the user can not access this information for him self
                    JEVisSample enabledAtt = att.getLatestSample();
                    if (enabledAtt != null) {
                        enabled = enabledAtt.getValueAsBoolean();
                    } else {
                        enabled = false;
                    }
                    break;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean isSysAdmin() {
        return isSysAdmin;
    }

    @Override
    public long getUserID() {
        return obj.getID();
    }

    @Override
    public JEVisObject getUserObject() {
        return obj;//or return ds.getObject....?
    }

    @Override
    public String getAccountName() {
        return obj.getName();
    }

    @Override
    public boolean canRead(long objectID) {
        return urm.canRead(objectID);
    }

    @Override
    public boolean canWrite(long objectID) {

        try {
            boolean canWrite = urm.canWrite(objectID);

            if(canWrite){
                return true;
            }else{
                if(executeUpdateExceptions.contains(ds.getObject(objectID).getJEVisClassName()) && canExecute(objectID)){
                    return true;
                }
            }
            return false;


        }catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean canCreate(long objectID) {

        return urm.canCreate(objectID);
    }

    @Override
    public boolean canCreate(long objectID, String jevisClass) {

        boolean canCreate = canCreate(objectID);

        if(canCreate){
            return true;
        }else{
            if(executeUpdateExceptions.contains(jevisClass) && canExecute(objectID)){
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canExecute(long objectID) {
        return urm.canExecute(objectID);
    }

    @Override
    public boolean canDelete(long objectID) {
        return urm.canDelete(objectID);
    }

    @Override
    public void reload() {
        urm.reload();
    }

}
