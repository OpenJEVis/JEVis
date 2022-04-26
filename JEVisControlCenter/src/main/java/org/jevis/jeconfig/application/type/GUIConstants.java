/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fs
 *
 */
public class GUIConstants {

    public static DisplayType BASIC_TEXT = new DisplayType("Text", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType TARGET_OBJECT = new DisplayType("Object Target", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType TARGET_ATTRIBUTE = new DisplayType("Attribute Target", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_NUMBER = new DisplayType("Number", JEVisConstants.PrimitiveType.DOUBLE);
    public static DisplayType NUMBER_WITH_UNIT = new DisplayType("Number with Unit", JEVisConstants.PrimitiveType.DOUBLE);
    public static DisplayType BASIC_FILER = new DisplayType("File Selector", JEVisConstants.PrimitiveType.FILE);
    public static DisplayType BASIC_BOOLEAN = new DisplayType("Check Box", JEVisConstants.PrimitiveType.BOOLEAN);
    public static DisplayType BOOLEAN_BUTTON = new DisplayType("Button", JEVisConstants.PrimitiveType.BOOLEAN);
    public static DisplayType PASSWORD_PBKDF2 = new DisplayType("Password PBKDF2", JEVisConstants.PrimitiveType.PASSWORD_PBKDF2);
    public static DisplayType BASIC_TEXT_MULTI = new DisplayType("Text Area", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_ENUM = new DisplayType("String Enum", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType DYNAMIC_ENUM = new DisplayType("Dynamic Enum", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType LOCALE = new DisplayType("Language Selector", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_TEXT_DATE_FULL = new DisplayType("Date", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_TEXT_TIME = new DisplayType("Time", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_NUMBER_LONG = new DisplayType("Number", JEVisConstants.PrimitiveType.LONG);
    public static DisplayType BASIC_TARGET_LONG = new DisplayType("Target Selector", JEVisConstants.PrimitiveType.LONG);
    public static DisplayType BASIC_PASSWORD = new DisplayType("Password", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType DATE_TIME = new DisplayType("Date Time", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType SCHEDULE = new DisplayType("Schedule", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType TIME_ZONE = new DisplayType("Time Zone", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType GAP_FILLING_CONFIG = new DisplayType("Gap Filling Config", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType DELTA_CONFIG = new DisplayType("Delta Config", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType LIMITS_CONFIG = new DisplayType("Limits Config", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType ALARM_CONFIG = new DisplayType("Alarm Config", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType CALENDAR = new DisplayType("Calendar", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType WEB_VIEW = new DisplayType("Web View", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType OPC_PROTOCOL = new DisplayType("OPC Protocol", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType PERIOD = new DisplayType("Period", JEVisConstants.PrimitiveType.STRING);

    private static final Logger logger = LogManager.getLogger(GUIConstants.class);

    public static List<DisplayType> ALL = new ArrayList<DisplayType>() {
        {
            add(BASIC_TEXT);
            add(BASIC_TEXT_MULTI);
            add(BASIC_NUMBER);
            add(NUMBER_WITH_UNIT);
            add(BASIC_FILER);
            add(BASIC_BOOLEAN);
            add(BOOLEAN_BUTTON);
            add(PASSWORD_PBKDF2);
            add(BASIC_TEXT_DATE_FULL);
            add(BASIC_TEXT_TIME);
            add(BASIC_NUMBER_LONG);
            add(BASIC_TARGET_LONG);
            add(TARGET_ATTRIBUTE);
            add(TARGET_OBJECT);
            add(BASIC_PASSWORD);
            add(DATE_TIME);
            add(SCHEDULE);
            add(TIME_ZONE);
            add(BASIC_ENUM);
            add(LOCALE);
            add(GAP_FILLING_CONFIG);
            add(LIMITS_CONFIG);
            add(DELTA_CONFIG);
            add(ALARM_CONFIG);
            add(CALENDAR);
            add(WEB_VIEW);
            add(OPC_PROTOCOL);
            add(PERIOD);
        }
    };

    /**
     * @param primitiveType
     * @return
     * @see JEVisConstants.PrimitiveType
     */
    public static List<DisplayType> getALL(int primitiveType) {
        List<DisplayType> all = new ArrayList<DisplayType>();
        for (DisplayType id : GUIConstants.ALL) {
            if (id.getPrimitivType() == primitiveType) {
                all.add(id);
            }

        }
        return all;

    }

}
