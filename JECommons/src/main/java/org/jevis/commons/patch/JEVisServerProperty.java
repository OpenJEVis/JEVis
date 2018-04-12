/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.patch;

/**
 *
 * @author broder
 */
public interface JEVisServerProperty {

    public String getServer();

    public String getPort();

    public String getDbSchema();

    public String getDbUser();

    public String getDbPassword();

    public String getJEVisUser();

    public String getJEVisPassword();
}
