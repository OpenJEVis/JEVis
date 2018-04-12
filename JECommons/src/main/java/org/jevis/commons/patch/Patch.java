/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.patch;

import org.jevis.api.JEVisDataSource;

/**
 *
 * @author broder
 */
public interface Patch {

    public void apply();

    public void undo();

    public String getVersion();

    public void setJEVisDataSource(JEVisDataSource ds);

}
