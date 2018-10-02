/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.Chart.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.jevistree.AlphanumComparator;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author broder
 */
public class GraphDataModel extends Observable {

    private Set<ChartDataModel> selectedRawData = new HashSet<>();
    private Set<ChartSettings> charts = new HashSet<>();
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

    public ObservableList<String> getChartsList() {
        List<String> tempList = new ArrayList<>();

        if (getSelectedData() != null) {
            for (ChartDataModel mdl : getSelectedData()) {
                if (mdl.getSelected()) {
                    boolean found = false;
                    for (String chartName : mdl.get_selectedCharts()) {
                        for (ChartSettings set : getCharts()) {
                            if (chartName.equals(set.getName())) {
                                if (!tempList.contains(set.getName())) {
                                    tempList.add(set.getName());
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            if (!tempList.contains(chartName)) {
                                getCharts().add(new ChartSettings(chartName));
                                tempList.add(chartName);
                            }
                        }
                    }
                }
            }

            AlphanumComparator ac = new AlphanumComparator();
            tempList.sort(ac);

            return FXCollections.observableArrayList(tempList);
        } else return FXCollections.emptyObservableList();
    }

    public boolean containsId(Long id) {
        if (!getSelectedData().isEmpty()) {
            AtomicBoolean found = new AtomicBoolean(false);
            getSelectedData().parallelStream().forEach(chartDataModel -> {
                if (chartDataModel.getObject().getID() == id) {
                    found.set(true);
                }
            });
            return found.get();
        } else return false;
    }

    public ChartDataModel get(Long id) {
        AtomicReference<ChartDataModel> out = new AtomicReference<>();
        getSelectedData().parallelStream().forEach(chartDataModel -> {
            if (chartDataModel.getObject().getID() == id) {
                out.set(chartDataModel);
            }
        });
        return out.get();
    }
}
