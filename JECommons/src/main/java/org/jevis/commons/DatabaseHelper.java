/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons;

import org.jevis.api.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;


/**
 * @author bf
 */
public class DatabaseHelper {

    public static boolean checkValidStringObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        JEVisAttribute attribute = jevisObject.getAttribute(jevisType);
        if (attribute != null && attribute.hasSample()) {
            JEVisSample latestSample = attribute.getLatestSample();
            if (latestSample != null) {
                if (latestSample.getValueAsString() != null
                        && !latestSample.getValueAsString().equals("")) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    private static boolean checkValidFileObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        JEVisAttribute attribute = jevisObject.getAttribute(jevisType);
        if (attribute != null && attribute.hasSample()) {
            JEVisSample latestSample = attribute.getLatestSample();
            if (latestSample != null) {
                if (latestSample.getValueAsString() != null
                        && attribute.getLatestSample().getValueAsFile() != null) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    private static boolean checkValidSelectionObject(JEVisObject jevisObject, JEVisType jevisType) throws
            JEVisException {
        boolean isValid = false;
        JEVisAttribute attribute = jevisObject.getAttribute(jevisType);
        if (attribute != null && attribute.hasSample()) {
            JEVisSample latestSample = attribute.getLatestSample();
            if (latestSample != null) {
                if (latestSample.getValueAsString() != null && attribute.getLatestSample().getValueAsFile() instanceof JEVisSelection) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    public static boolean checkValidNumberObject(JEVisObject jevisObject, JEVisType jevisType) throws
            JEVisException {
        boolean isValid = false;
        JEVisAttribute attribute = jevisObject.getAttribute(jevisType);
        if (attribute != null && attribute.hasSample()) {
            JEVisSample latestSample = attribute.getLatestSample();
            if (latestSample != null) {
                if (latestSample.getValueAsString() != null
                        && !attribute.getLatestSample().getValueAsString().equals("")
                        && attribute.getLatestSample().getValueAsLong() != null) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    public static boolean checkValidBooleanObject(JEVisObject jevisObject, JEVisType jevisType) throws
            JEVisException {
        boolean isValid = false;
        JEVisAttribute attribute = jevisObject.getAttribute(jevisType);
        if (attribute != null && attribute.hasSample()) {
            JEVisSample latestSample = attribute.getLatestSample();
            if (latestSample != null) {
                if (latestSample.getValueAsString() != null &&
                        latestSample.getValueAsBoolean() != null) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    public static String getObjectAsString(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        if (DatabaseHelper.checkValidStringObject(jevisObject, jevisType)) {
            return jevisObject.getAttribute(jevisType).getLatestSample().getValueAsString();

        } else {
            return null;
        }
    }

    public static Boolean getObjectAsBoolean(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        if (DatabaseHelper.checkValidBooleanObject(jevisObject, jevisType)) {
            return jevisObject.getAttribute(jevisType).getLatestSample().getValueAsBoolean();
        } else {
            return false;
        }
    }

    public static Integer getObjectAsInteger(JEVisObject jevisObject, JEVisType jevisType) {
        Integer value = null;
        try {
            if (DatabaseHelper.checkValidNumberObject(jevisObject, jevisType)) {
                value = jevisObject.getAttribute(jevisType).getLatestSample().getValueAsLong().intValue();
            }
        } catch (NumberFormatException | JEVisException nfe) {
        }
        return value;
    }

    public static Long getObjectAsLong(JEVisObject jevisObject, JEVisType jevisType) {
        Long value = null;
        try {
            if (DatabaseHelper.checkValidNumberObject(jevisObject, jevisType)) {
                value = jevisObject.getAttribute(jevisType).getLatestSample().getValueAsLong();

            }
        } catch (NumberFormatException | JEVisException nfe) {
        }
        return value;
    }

    public static JEVisFile getObjectAsFile(JEVisObject jevisObject, JEVisType jevisType) {
        JEVisFile value = null;
        try {
            if (DatabaseHelper.checkValidFileObject(jevisObject, jevisType)) {
                value = jevisObject.getAttribute(jevisType).getLatestSample().getValueAsFile();

            }
        } catch (NumberFormatException | JEVisException nfe) {
        }
        return value;
    }

    public static DateTime getObjectAsDate(JEVisObject jevisObject, JEVisType jevisType) {
        DateTime datetime = new DateTime(1990, 1, 1, 0, 0).withZone(DateTimeZone.UTC);

        try {
            if (DatabaseHelper.checkValidStringObject(jevisObject, jevisType)) {
                String value = jevisObject.getAttribute(jevisType).getLatestSample().getValueAsString();
                try {
                    datetime = new DateTime(value);
                } catch (Exception e) {
                    datetime = DateTime.parse(value, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
        } catch (NumberFormatException |
                JEVisException nfe) {
        }
        return datetime;
    }

    public static JEVisSelection getObjectAsSelection(JEVisObject jevisObject, JEVisType jevisType) {
        JEVisSelection value = null;
        try {
            if (DatabaseHelper.checkValidSelectionObject(jevisObject, jevisType)) {
                value = jevisObject.getAttribute(jevisType).getLatestSample().getValueAsSelection();
            }
        } catch (NumberFormatException | JEVisException nfe) {
        }
        return value;
    }

}
