package org.jevis.jeconfig.plugin.equipment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.constants.GUIConstants;
import org.joda.time.DateTime;

public class AttributeValueChange {
    private static final Logger logger = LogManager.getLogger(AttributeValueChange.class);
    private int primitiveType;
    private String guiDisplayType;
    private JEVisAttribute attribute;
    private String stringValue;
    private Double doubleValue;
    private Boolean booleanValue;
    private Long longValue;
    private JEVisFile jeVisFile;
    private DateTime dateTime;
    boolean changed = false;

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, String stringValue) {
        this.primitiveType = primitiveType;
        this.guiDisplayType = guiDisplayType;
        this.attribute = attribute;
        this.stringValue = stringValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, Double doubleValue) {
        this.primitiveType = primitiveType;
        this.guiDisplayType = guiDisplayType;
        this.attribute = attribute;
        this.doubleValue = doubleValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, Boolean booleanValue) {
        this.primitiveType = primitiveType;
        this.guiDisplayType = guiDisplayType;
        this.attribute = attribute;
        this.booleanValue = booleanValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, Long longValue) {
        this.primitiveType = primitiveType;
        this.guiDisplayType = guiDisplayType;
        this.attribute = attribute;
        this.longValue = longValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, JEVisFile jeVisFile) {
        this.primitiveType = primitiveType;
        this.guiDisplayType = guiDisplayType;
        this.attribute = attribute;
        this.jeVisFile = jeVisFile;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, DateTime dateTime) {
        this.primitiveType = primitiveType;
        this.guiDisplayType = guiDisplayType;
        this.attribute = attribute;
        this.dateTime = dateTime;
    }

    public AttributeValueChange() {
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public JEVisFile getJeVisFile() {
        return jeVisFile;
    }

    public void setJeVisFile(JEVisFile jeVisFile) {
        this.jeVisFile = jeVisFile;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setPrimitiveType(int primitiveType) {
        this.primitiveType = primitiveType;
    }

    public void setGuiDisplayType(String guiDisplayType) {
        this.guiDisplayType = guiDisplayType;
    }

    public void setAttribute(JEVisAttribute attribute) {
        this.attribute = attribute;
    }

    public JEVisAttribute getAttribute() {
        return this.attribute;
    }

    public void commit(DateTime dateTime) throws JEVisException {
        logger.debug("Commit attribute: " + primitiveType + " " + attribute.getName());
        switch (primitiveType) {
            case JEVisConstants.PrimitiveType.LONG:
                JEVisSample longSample = attribute.buildSample(dateTime, longValue);
                longSample.commit();
                break;
            case JEVisConstants.PrimitiveType.DOUBLE:
                JEVisSample doubleSample = attribute.buildSample(dateTime, doubleValue);
                doubleSample.commit();
                break;
            case JEVisConstants.PrimitiveType.FILE:
                JEVisSample fileSample = attribute.buildSample(dateTime, jeVisFile);
                fileSample.commit();
                break;
            case JEVisConstants.PrimitiveType.BOOLEAN:
                JEVisSample boolSample = attribute.buildSample(dateTime, booleanValue);
                boolSample.commit();
                break;
            default:
                if (primitiveType == JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {

                } else if (guiDisplayType.equals(GUIConstants.TARGET_OBJECT.getId()) || guiDisplayType.equals(GUIConstants.TARGET_ATTRIBUTE.getId())) {
                    JEVisSample targetSample = attribute.buildSample(dateTime, stringValue);
                    targetSample.commit();
                } else if (guiDisplayType.equals(GUIConstants.DATE_TIME.getId()) || guiDisplayType.equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                    JEVisSample dateSample = attribute.buildSample(dateTime, this.dateTime.toString());
                    dateSample.commit();
                } else {
                    JEVisSample stringSample = attribute.buildSample(dateTime, stringValue);
                    stringSample.commit();
                }

                break;
        }
        logger.debug("->done");
    }

    @Override
    public String toString() {
        if (stringValue != null) {
            return stringValue;
        } else if (doubleValue != null) {
            return doubleValue.toString();
        } else if (booleanValue != null) {
            return booleanValue.toString();
        } else if (longValue != null) {
            return longValue.toString();
        } else if (jeVisFile != null) {
            return jeVisFile.getFilename();
        } else if (dateTime != null) {
            return dateTime.toString();
        } else return "";
    }
}
