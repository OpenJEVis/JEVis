/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.tool;

import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author fs
 */
public class PreloadTask {

    private String name = "";
    private SimpleBooleanProperty runProperty = new SimpleBooleanProperty(false);

    public PreloadTask(String name, boolean start) {
        this.name = name;
        this.runProperty.set(start);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleBooleanProperty getRunProperty() {
        return runProperty;
    }

    public void setRunProperty(SimpleBooleanProperty runProperty) {
        this.runProperty = runProperty;
    }

}
