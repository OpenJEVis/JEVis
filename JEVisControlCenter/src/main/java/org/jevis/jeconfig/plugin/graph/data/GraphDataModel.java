/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import org.jevis.application.jevistree.plugin.BarchartPlugin;

/**
 *
 * @author broder
 */
public class GraphDataModel extends Observable {

    List<GraphDataRow> dataRows = new ArrayList<>();
    Set<BarchartPlugin.DataModel> selectedRawData;

    public void setSelectedData(Set<BarchartPlugin.DataModel> selectedData) {
        this.selectedRawData = selectedData;
        setChanged();
        notifyObservers();
    }

    public Set<BarchartPlugin.DataModel> getSelectedData() {
        return selectedRawData;
    }

}
