/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.data;

import org.jevis.application.jevistree.plugin.ChartDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

/**
 *
 * @author broder
 */
public class GraphDataModel extends Observable {

    List<GraphDataRow> dataRows = new ArrayList<>();
    Set<ChartDataModel> selectedRawData;

    public Set<ChartDataModel> getSelectedData() {
        return selectedRawData;
    }

    public void setSelectedData(Set<ChartDataModel> selectedData) {
        this.selectedRawData = selectedData;
        setChanged();
        notifyObservers();
    }

}
