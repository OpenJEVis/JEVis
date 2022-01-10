/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.ws.sql;

import org.jevis.commons.ws.json.JsonObject;

/**
 * @author fs
 */
public class JEVisUserNew {

    private final boolean isSysAdmin;
    private final long uID;
    private final boolean enabled;
    private final String lastName;
    private final String firstname;
    private JsonObject userObj;
    private final String accountName;
    private SQLDataSource ds;
    private String password;

    public JEVisUserNew(SQLDataSource ds, String account, Long objID, boolean isSysAdmin, boolean enabled, String password) {
        this(ds, account, objID, isSysAdmin, enabled);
        this.password = password;
    }

    public JEVisUserNew(long userid, String accountname, boolean isSysAdmin, boolean enabled, String passHash, String firstName, String lastName) {
        this.isSysAdmin = isSysAdmin;
        this.uID = userid;
        this.firstname = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.accountName = accountname;
        this.password = passHash;
    }


    public JEVisUserNew(SQLDataSource ds, JsonObject obj, boolean isSysAdmin, boolean enabled, String firstName, String lastName) {
        this.isSysAdmin = isSysAdmin;
        this.uID = obj.getId();
        this.userObj = obj;
        this.firstname = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.accountName = obj.getName();
        this.ds = ds;
    }

    public JEVisUserNew(SQLDataSource ds, String account, Long objID, boolean isSysAdmin, boolean enabled) {
        this.isSysAdmin = isSysAdmin;
        this.uID = objID;
        this.userObj = null;
        this.firstname = "";
        this.lastName = "";
        this.enabled = enabled;
        this.accountName = account;
        this.ds = ds;
    }

    public void setDataSource(SQLDataSource dataSource) {
        this.ds = dataSource;
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isSysAdmin() {
        return isSysAdmin;
    }

    public long getUserID() {
        return uID;
    }

    public String getPassword() {
        return password;
    }

    @Deprecated
    public JsonObject getUserObject() {
        JsonObject userObj = new JsonObject();
        userObj.setName(accountName);
        userObj.setId(getUserID());
        userObj.setJevisClass("User");
        return userObj;

        /*
        if (userObj == null) {
            try {
                userObj = ds.getObject(uID);
            } catch (JEVisException ex) {
                ex.printStackTrace();
            }
        }

        return userObj;

         */
    }


}
