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
@XmlRootElement(name = "ChartSettings")
public class JsonChartSetting {

    private String id;
    private String name;
    private String chartType;
    private String height;
    private String colorMapping;
    private String orientation;
    private String groupingInterval;
    private String minFractionDigits;
    private String maxFractionDigits;

    private String filterEnabled;
    private JsonChartTimeFrame analysisTimeFrame;

    public JsonChartSetting() {
    }

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "chartType")
    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    @XmlElement(name = "height")
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    @XmlElement(name = "colorMapping")
    public String getColorMapping() {
        return colorMapping;
    }

    public void setColorMapping(String colorMapping) {
        this.colorMapping = colorMapping;
    }

    @XmlElement(name = "orientation")
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    @XmlElement(name = "groupingInterval")
    public String getGroupingInterval() {
        return groupingInterval;
    }

    public void setGroupingInterval(String groupingInterval) {
        this.groupingInterval = groupingInterval;
    }

    @XmlElement(name = "minFractionDigits")
    public String getMinFractionDigits() {
        return minFractionDigits;
    }

    public void setMinFractionDigits(String minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    @XmlElement(name = "maxFractionDigits")
    public String getMaxFractionDigits() {
        return maxFractionDigits;
    }

    public void setMaxFractionDigits(String maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    @XmlElement(name = "filterEnabled")
    public String getFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(String filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    @XmlElement(name = "analysisTimeFrame")
    public JsonChartTimeFrame getAnalysisTimeFrame() {
        return analysisTimeFrame;
    }

    public void setAnalysisTimeFrame(JsonChartTimeFrame analysisTimeFrame) {
        this.analysisTimeFrame = analysisTimeFrame;
    }

    @Override
    public String toString() {
        try {
            return JsonTools.prettyObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
