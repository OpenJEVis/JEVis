package org.jevis.loytecxmldl.jevis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;


public class Helper {

    private final static Logger log = LogManager.getLogger(Helper.class.getName());

    /**
     * Returns the last value of a JEVis object attribute. Invalid values are set to null.
     *
     * @param jevisObject   JEVis Object used for getting attribute values
     * @param attributeName Name of the attribute
     * @return The attributes last value
     */
    public String getValue(JEVisObject jevisObject, String attributeName) {
        return getValue(jevisObject, attributeName, null, false);
    }

    /**
     * Returns the last value of a JEVis object attribute. Invalid sample values are replaced with default.
     *
     * @param jevisObject   JEVis Object used for getting attribute values
     * @param attributeName Name of the attribute
     * @param defaultValue  Optional default value
     * @return The attributes last value
     */
    public String getValue(JEVisObject jevisObject, String attributeName, String defaultValue) {
        return getValue(jevisObject, attributeName, defaultValue, true);
    }

    /**
     * Returns the last value of a JEVis object attribute
     *
     * @param jevisObject       JEVis Object used for getting attribute values
     * @param attributeName     Name of the attribute
     * @param defaultValue      Optional default value
     * @param replaceEmptyValue Optional replace the value with default if sample value is invalid
     * @return The attributes last value
     */
    public String getValue(JEVisObject jevisObject, String attributeName, String defaultValue, Boolean replaceEmptyValue) {

        // Set default value
        String value = defaultValue;

        try {
            JEVisAttribute attribute = jevisObject.getAttribute(attributeName);
            if (attribute != null) {
                JEVisSample sample;
                if (attribute.hasSample()) {
                    sample = attribute.getLatestSample();
                    if (sample != null) {
                        value = sample.getValueAsString();
                        if (value.isEmpty()) {
                            if (replaceEmptyValue) {
                                log.warn("Node '" + jevisObject.getName() + "' Attribute '"
                                        + attributeName + "' not specified. Replaced with default: "
                                        + defaultValue);
                                value = defaultValue;
                            } else {
                                log.warn("Node '" + jevisObject.getName() + "' Attribute '"
                                        + attributeName + "' not specified.");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error while getting attribute value " + attributeName + " from " + jevisObject.getName());
            log.debug(ex.getMessage());
        }
        return value;
    }
}