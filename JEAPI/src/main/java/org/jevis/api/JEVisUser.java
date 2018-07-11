/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.api;

import java.util.List;

/**
 *
 * @author fs
 */
public interface JEVisUser {

    public boolean isEnabled();

    public String getFirstName();

    public String getLastName();

    public boolean isSysAdmin();

    public long getUserID();

    public JEVisObject getUserObject();

    public String getAccountName();

    public boolean canRead(long objectID);

    public boolean canWrite(long objectID);

    public boolean canCreate(long objectID);

    public boolean canExecute(long objectID);

    public boolean canDelete(long objectID);

    public void reload();


//    public boolean canDeleteClass(String jclass);
}
