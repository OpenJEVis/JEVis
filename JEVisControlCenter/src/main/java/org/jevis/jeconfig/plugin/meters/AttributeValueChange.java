package org.jevis.jeconfig.plugin.meters;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.joda.time.DateTime;

public class AttributeValueChange {

    private int primitiveType;
    private String guiDisplayType;
    private JEVisAttribute attribute;
    private String stringValue;
    private Double doubleValue;
    private Boolean booleanValue;
    private Long longValue;
    private JEVisFile jeVisFile;
    private DateTime dateTime;

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, String stringValue) {
        this.primitiveType = primitiveType;
        this.attribute = attribute;
        this.stringValue = stringValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, Double doubleValue) {
        this.primitiveType = primitiveType;
        this.attribute = attribute;
        this.doubleValue = doubleValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, Boolean booleanValue) {
        this.primitiveType = primitiveType;
        this.attribute = attribute;
        this.booleanValue = booleanValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, Long longValue) {
        this.primitiveType = primitiveType;
        this.attribute = attribute;
        this.longValue = longValue;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, JEVisFile jeVisFile) {
        this.primitiveType = primitiveType;
        this.attribute = attribute;
        this.jeVisFile = jeVisFile;
    }

    public AttributeValueChange(int primitiveType, String guiDisplayType, JEVisAttribute attribute, DateTime dateTime) {
        this.primitiveType = primitiveType;
        this.attribute = attribute;
        this.dateTime = dateTime;
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
}
