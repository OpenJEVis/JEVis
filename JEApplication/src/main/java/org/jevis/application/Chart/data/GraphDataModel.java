/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.Chart.data;

import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;

import java.util.Observable;
import java.util.Set;

/**
 *
 * @author broder
 */
public class GraphDataModel extends Observable {

    Set<ChartDataModel> selectedRawData;
    Set<ChartSettings> charts;
    private Boolean hideShowIcons = true;

    public Set<ChartDataModel> getSelectedData() {
        return selectedRawData;
    }

    public void setSelectedData(Set<ChartDataModel> selectedData) {
        this.selectedRawData = selectedData;
        setChanged();
        notifyObservers();
    }

    public Set<ChartSettings> getCharts() {
        return charts;
    }

    public void setCharts(Set<ChartSettings> charts) {
        this.charts = charts;
    }

    public Boolean getHideShowIcons() {
        return hideShowIcons;
    }

    public void setHideShowIcons(Boolean hideShowIcons) {
        this.hideShowIcons = hideShowIcons;
    }
}
