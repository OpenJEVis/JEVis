/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.api;

/**
 *
 * @author fs
 */
public interface JEVisUser {

    boolean isEnabled();

    String getFirstName();

    String getLastName();

    boolean isSysAdmin();

    long getUserID();

    JEVisObject getUserObject();

    String getAccountName();

    boolean canRead(long objectID);

    boolean canWrite(long objectID);

    boolean canCreate(long objectID);

    boolean canExecute(long objectID);

    boolean canDelete(long objectID);

    void reload();


//    public boolean canDeleteClass(String jclass);
}
