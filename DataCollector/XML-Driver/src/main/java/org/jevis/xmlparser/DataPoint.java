/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

/**
 * @author bf
 */
public class DataPoint {

    private String mappingIdentifier;
    private String valueIdentifier;
    private Long target;

    public String getMappingIdentifier() {
        return mappingIdentifier;
    }

    public void setMappingIdentifier(String mappingIdentifier) {
        this.mappingIdentifier = mappingIdentifier;
    }

    public String getValueIdentifier() {
        return valueIdentifier;
    }

    public void setValueIdentifier(String valueIdentifier) {
        this.valueIdentifier = valueIdentifier;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }


}
