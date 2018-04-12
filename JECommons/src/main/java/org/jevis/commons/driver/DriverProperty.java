/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

/**
 *
 * @author bf
 */
public class DriverProperty {

    private JEVisObject driver;
    private DateTime fileDate;
    private String className;
    private String jevisName;
    private String fileSource;

    DriverProperty(JEVisObject driver, DateTime fileDate, String className, String jevisName, String fileSource) {
        this.driver = driver;
        this.fileDate = fileDate;
        this.className = className;
        this.jevisName = jevisName;
        this.fileSource = fileSource;
    }

    public JEVisObject getDriver() {
        return driver;
    }

    public void setDriver(JEVisObject driver) {
        this.driver = driver;
    }

    public DateTime getFileDate() {
        return fileDate;
    }

    public void setFileDate(DateTime fileDate) {
        this.fileDate = fileDate;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getJevisName() {
        return jevisName;
    }

    public void setJevisName(String jevisName) {
        this.jevisName = jevisName;
    }

    public String getFileSource() {
        return fileSource;
    }

    public void setFileSource(String fileSource) {
        this.fileSource = fileSource;
    }

}
