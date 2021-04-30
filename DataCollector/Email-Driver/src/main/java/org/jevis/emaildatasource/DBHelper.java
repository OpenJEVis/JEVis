/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI.
 * <p>
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.emaildatasource;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

/**
 * The DBHelper class helps to get data from a database or set the default
 * values.
 *
 * @author Artur Iablokov
 */
public class DBHelper {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(DBHelper.class);

    /**
     * Get the attributes values.
     *
     * @param <T>
     * @param rtype    expected return type
     * @param obj      email object
     * @param attType  specific attribute of an email object (see EMailConstants)
     * @param error    specific error object for attribute
     * @param defValue default value of attribute
     * @return T value of attribute
     */
    public static <T> T getAttValue(RetType rtype, JEVisObject obj, String attType, MailError error, T defValue) throws NullPointerException {
        try {
            JEVisAttribute att = obj.getAttribute(attType);
            if (att == null) {
                logger.error("{} Attribute is missing", error.getMessage());
            }
            if (!att.hasSample()) {
                logger.error("Warning! {} has no samples", error.getMessage());
                return getDefValue(defValue, error);
//                if (defValue != null) {
//                    return (T) defValue;
//                } else {
//                    throw new NullPointerException(error.getMessage() + " is empty");
//                }
            }

            JEVisSample lastS = att.getLatestSample();

            switch (rtype) {
                case STRING:
                    try {
                        String str = lastS.getValueAsString();
                        if (null == str || str.isEmpty()) {
                            logger.error("{} is empty. Trying to set the default value", error.getMessage());
                            return getDefValue(defValue, error);
                        } else {
                            return (T) lastS.getValueAsString();
                        }
                    } catch (Exception ex) {
                        logger.error("Attribute {}: failed to get the value", error.getMessage());
                        throw new NullPointerException();
                    }
                case INTEGER:
                    try {
                        Long longValue = lastS.getValueAsLong();
                        if (null == longValue || longValue == -1L) {
                            logger.error("{} is empty. Trying to set the default value", error.getMessage());
                            return getDefValue(defValue, error);
                        }
                        return (T) new Integer(longValue.intValue());
                    } catch (Exception ex) {
                        logger.error("Attribute {}: failed to get the value", error.getMessage());
                        return getDefValue(defValue, error);
                    }
                case BOOLEAN:
                    try {
                        return (T) lastS.getValueAsBoolean();
                    } catch (Exception ex) {
                        logger.error("Attribute {}: failed to get the value. ", error.getMessage());
                        throw new NullPointerException();
                    }
                case DATETIME:
                    DateTime datetime = null;
                    try {
                        String value = lastS.getValueAsString();
                        if (null == value || value.isEmpty()) {
                            logger.error("{} is empty. Trying to set the default value", error.getMessage());
                            return getDefValue(defValue, error);
                        }
                        try {
                            datetime = new DateTime(value);
                        } catch (Exception ex) {
                            logger.error("the format of the {} is not valid.", error.getMessage());
                            datetime = DateTime.parse(value, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                        }
                        return (T) datetime;

                    } catch (JEVisException ex) {
                        logger.error("Attribute {}: failed to get the value. ", error.getMessage());
                    }

                default:
                    logger.error("Attribute {}: return type is wrong or unknown.", error.getMessage());
                    throw new NullPointerException();
            }

        } catch (JEVisException jex) {
            throw new NullPointerException();
        }
    }

    /**
     * Get the default attributes values.
     *
     * @return T default
     */
    private static <T> T getDefValue(T defValue, MailError error) {
        if (defValue != null) {
            return defValue;
        } else {
            throw new NullPointerException(error.getMessage() + " default value is wrong");
        }
    }

    static void setLastReadout(List<Result> results, JEVisObject channel) {
        DateTime timestamp = new org.joda.time.DateTime();

        try {
            JEVisAttribute lastReadout = channel.getAttribute(EMailConstants.EMailChannel.LAST_READOUT);
            JEVisSample sample = lastReadout.getLatestSample();

            String lts = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(timestamp);
            lastReadout.buildSample(new DateTime(), lts).commit();
            logger.debug("Set LastReadout to: " + timestamp);

        } catch (Exception ex) {
            logger.error("Error while setting lastReadout");
        }

    }

    /**
     * return types
     */
    public enum RetType {
        STRING, BOOLEAN, INTEGER, DATETIME
    }
}
