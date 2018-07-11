/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 *
 * @author fs
 */
public class FileCache {

    private static final Logger logger = LogManager.getLogger(FileCache.class);
    public static final String CACH_PATH = System.getProperty("java.io.tmpdir") + "/JEWebService";

    public final static String CLASS_ICON_FILE = CACH_PATH + "/allIcons.zip";
    public final static String CLASS_JSON = CACH_PATH + "/Classes.json";

    public static File getClassFile() {
        cratePath();
        return new File(CLASS_JSON);
    }

    public static File getClassIconFile() {
        cratePath();
        File tmpZipFile = new File(CLASS_ICON_FILE);
        return tmpZipFile;
    }

    public static void deleteClassFile() {
        try {
            File tmpZipFile = new File(CLASS_JSON);
            if (tmpZipFile.exists()) {
                tmpZipFile.delete();
            }else{
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
        }
    }

    public static void deleteClassIconFile() {
        cratePath();
        try {
            File tmpZipFile = new File(CLASS_ICON_FILE);
            if (tmpZipFile.exists()) {
                tmpZipFile.delete();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    public static void deleteClassCachFiles() {
        deleteClassFile();
        deleteClassIconFile();
    }

    private static void cratePath() {
        File tmpfolder = new File(CACH_PATH);
        if (!tmpfolder.exists()) {
            tmpfolder.mkdirs();
        }
    }

}
