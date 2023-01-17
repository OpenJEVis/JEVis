package org.jevis.jeconfig.tool;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class AttributeFactory {

    private static final Logger logger = LogManager.getLogger(AttributeFactory.class);
    public static DateTimeFormatter dtf = ISODateTimeFormat.dateTime();

    public static void fillProperty(ReadOnlyProperty propertyObj, JEVisObject obj) {
        try {
            //logger.info("Fetch Attribute: {}:{}", obj.getID(), propertyObj.getBean().toString());
            JEVisAttribute attribute = obj.getAttribute(propertyObj.getBean().toString());
            JEVisSample sample = attribute.getLatestSample();

            if (propertyObj instanceof SimpleStringProperty) {
                if (sample != null) {
                    ((SimpleStringProperty) propertyObj).set(sample.getValueAsString());
                }
            }

            if (propertyObj instanceof BooleanProperty) {
                if (sample != null) {
                    ((BooleanProperty) propertyObj).set(sample.getValueAsBoolean());
                }
            }

            if (propertyObj instanceof ObjectProperty) {
                if (sample != null) {
                    // logger.info("--- is Object");
                    try {
                        if (((ObjectProperty) propertyObj).get() instanceof JEVisFile) {
                            ((ObjectProperty) propertyObj).set(sample.getValueAsFile());
                        } else if (((ObjectProperty) propertyObj).get() instanceof DateTime) {
                            ((ObjectProperty) propertyObj).set(dtf.parseDateTime(sample.getValueAsString()));
                        }
                    } catch (Exception ex) {
                        logger.error(ex, ex);
                    }

                }

            }

            if (propertyObj instanceof LongProperty) {
                if (sample != null) {
                    ((LongProperty) propertyObj).set(sample.getValueAsLong());
                }
            }

            if (propertyObj instanceof IntegerProperty) {
                if (sample != null) {
                    ((IntegerProperty) propertyObj).set(sample.getValueAsLong().intValue());
                }
            }

            if (propertyObj instanceof LongProperty) {
                if (sample != null) {
                    ((LongProperty) propertyObj).set(sample.getValueAsLong());
                }
            }


        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    /**
     * Same as commitAttribute but will catch and ignore any error.
     *
     * @param propertyObj
     * @param obj
     * @param dateTime
     */
    public static void commitAttributeIE(ReadOnlyProperty propertyObj, JEVisObject obj, DateTime dateTime) {
        try {
            commitAttribute(propertyObj, obj, dateTime);
        } catch (Exception ex) {

        }
    }

    public static void commitAttribute(ReadOnlyProperty propertyObj, JEVisObject obj, DateTime dateTime) throws JEVisException {
        try {
            logger.info("Commit Attribute: {}:{}", obj.getID(), propertyObj.getBean().toString());
            JEVisAttribute attribute = obj.getAttribute(propertyObj.getBean().toString());

            if (propertyObj instanceof SimpleStringProperty) {
                attribute.buildSample(dateTime, ((SimpleStringProperty) propertyObj).get()).commit();
            }

            if (propertyObj instanceof BooleanProperty) {
                attribute.buildSample(dateTime, ((BooleanProperty) propertyObj).get()).commit();
            }

            if (propertyObj instanceof ObjectProperty) {
                if (((ObjectProperty<?>) propertyObj).get() != null) {
                    if (((ObjectProperty) propertyObj).get() instanceof JEVisFile) {
                        attribute.buildSample(dateTime, ((ObjectProperty) propertyObj).get()).commit();
                    } else if (((ObjectProperty) propertyObj).get() instanceof DateTime) {
                        attribute.buildSample(dateTime, ((DateTime) ((ObjectProperty) propertyObj).get()).toString(dtf)).commit();
                    }
                }


            }

            if (propertyObj instanceof LongProperty) {
                attribute.buildSample(dateTime, ((ObjectProperty) propertyObj).get()).commit();
            }

            if (propertyObj instanceof IntegerProperty) {
                attribute.buildSample(dateTime, ((IntegerProperty) propertyObj).get()).commit();
            }

            if (propertyObj instanceof LongProperty) {
                attribute.buildSample(dateTime, ((IntegerProperty) propertyObj).get()).commit();
            }

        } catch (Exception ex) {
            if (obj == null) {
                logger.error("Error commiting Bean: Object is null");
            } else if (propertyObj == null) {
                logger.error("Error commiting Bean: Bean is null");
            } else if (dateTime == null) {
                logger.error("Error commiting Bean: DateTime is null");
            } else {
                logger.error("Error commiting Bean: {}:{}", obj.getID(), propertyObj.getBean());
            }

            throw ex;
        }


    }

}
