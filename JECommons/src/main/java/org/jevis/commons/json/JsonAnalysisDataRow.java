/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
@XmlRootElement(name = "JsonAnalysisDataRow")
public class JsonAnalysisDataRow {

    private String name;
    private String selected;
    private String object;
    private String dataProcessorObject;
    private String aggregation;
    private String color;
    private String unit;
    private String selectedCharts;
    private String axis;
    private String isEnPI;
    private String calculation;
    private String bubbleType;
    private String chartType;

    public JsonAnalysisDataRow() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "object")
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @XmlElement(name = "dataProcessorObject")
    public String getDataProcessorObject() {
        return dataProcessorObject;
    }

    public void setDataProcessorObject(String dataProcessorObject) {
        this.dataProcessorObject = dataProcessorObject;
    }

    @XmlElement(name = "aggregation")
    public String getAggregation() {
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }

    @XmlElement(name = "color")
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        try {
            return JsonTools.objectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @XmlElement(name = "selected")
    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    @XmlElement(name = "unit")
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @XmlElement(name = "selectedCharts")
    public String getSelectedCharts() {
        return selectedCharts;
    }

    public void setSelectedCharts(String selectedCharts) {
        this.selectedCharts = selectedCharts;
    }

    @XmlElement(name = "axis")
    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    @XmlElement(name = "isEnPI")
    public String getIsEnPI() {
        return isEnPI;
    }

    public void setIsEnPI(String isEnPI) {
        this.isEnPI = isEnPI;
    }

    @XmlElement(name = "calculation")
    public String getCalculation() {
        return calculation;
    }

    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    @XmlElement(name = "bubbleType")
    public String getBubbleType() {
        return bubbleType;
    }

    public void setBubbleType(String bubbleType) {
        this.bubbleType = bubbleType;
    }

    @XmlElement(name = "chartType")
    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
}
