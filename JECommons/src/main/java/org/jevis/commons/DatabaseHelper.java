/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons;

import org.jevis.api.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author bf
 */
public class DatabaseHelper {

    public static boolean checkValidStringObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        if (jevisObject.getAttribute(jevisType).hasSample() && jevisObject.getAttribute(jevisType).getLatestSample() != null && jevisObject.getAttribute(jevisType).getLatestSample().getValueAsString() instanceof String && !jevisObject.getAttribute(jevisType).getLatestSample().getValueAsString().equals("")) {
            isValid = true;
        }
        return isValid;
    }

    private static boolean checkValidFileObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        if (jevisObject.getAttribute(jevisType).hasSample() && jevisObject.getAttribute(jevisType).getLatestSample() != null && jevisObject.getAttribute(jevisType).getLatestSample().getValueAsFile() instanceof JEVisFile) {
            isValid = true;
        }
        return isValid;
    }

    private static boolean checkValidSelectionObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        if (jevisObject.getAttribute(jevisType).hasSample() && jevisObject.getAttribute(jevisType).getLatestSample() != null && jevisObject.getAttribute(jevisType).getLatestSample().getValueAsFile() instanceof JEVisSelection) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean checkValidNumberObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        if (jevisObject.getAttribute(jevisType).hasSample() && jevisObject.getAttribute(jevisType).getLatestSample() != null && !jevisObject.getAttribute(jevisType).getLatestSample().getValueAsString().equals("") && jevisObject.getAttribute(jevisType).getLatestSample().getValueAsLong() instanceof Long) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean checkValidBooleanObject(JEVisObject jevisObject, JEVisType jevisType) throws JEVisException {
        boolean isValid = false;
        if (jevisObject.getAttribute(jevisType).hasSample() && jevisObject.getAttribute(jevisType).getLatestSample() != null) {
            isValid = true;
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

    public static DateTime getObjectAsDate(JEVisObject jevisObject, JEVisType jevisType, DateTimeFormatter timeFormat) {
        DateTime datetime = null;
        try {
            if (DatabaseHelper.checkValidStringObject(jevisObject, jevisType)) {
                String value = jevisObject.getAttribute(jevisType).getLatestSample().getValueAsString();
                datetime = timeFormat.parseDateTime(value);
            }
        } catch (NumberFormatException | JEVisException nfe) {
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
