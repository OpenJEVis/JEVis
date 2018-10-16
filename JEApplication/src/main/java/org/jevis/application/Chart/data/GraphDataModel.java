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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author broder
 */
public class GraphDataModel extends Observable {

    private Set<ChartDataModel> selectedData = new HashSet<>();
    private Set<ChartSettings> charts = new HashSet<>();
    private Boolean hideShowIcons = true;
    private ObservableList<String> selectedDataNames = FXCollections.observableArrayList(new ArrayList<>());

    public Set<ChartDataModel> getSelectedData() {
//        System.out.println("GraphDataModel.getSelectedData");
        return selectedData;
    }

    public void setSelectedData(Set<ChartDataModel> selectedData) {
        this.selectedData = selectedData;
        selectedDataNames.clear();


        if (getSelectedData() != null) {
            for (ChartDataModel mdl : getSelectedData()) {

                if (mdl.getSelected()) {
                    boolean found = false;
                    for (String chartName : mdl.getSelectedcharts()) {
                        for (ChartSettings set : getCharts()) {
                            if (chartName.equals(set.getName())) {
                                if (!selectedDataNames.contains(set.getName())) {

                                    selectedDataNames.add(set.getName());
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            if (!selectedDataNames.contains(chartName)) {
                                getCharts().add(new ChartSettings(chartName));
                                selectedDataNames.add(chartName);
                            }
                        }
                    }
                }
            }
        }

        AlphanumComparator ac = new AlphanumComparator();
        selectedDataNames.sort(ac);

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
        return selectedDataNames;

//        if (getSelectedData() != null) {
//            for (ChartDataModel mdl : getSelectedData()) {
//                if (mdl.getSelected()) {
//                    boolean found = false;
//                    for (String chartName : mdl.getSelectedcharts()) {
//                        for (ChartSettings set : getCharts()) {
//                            if (chartName.equals(set.getName())) {
//                                if (!tempList.contains(set.getName())) {
//                                    tempList.add(set.getName());
//                                    found = true;
//                                }
//                            }
//                        }
//                        if (!found) {
//                            if (!tempList.contains(chartName)) {
//                                getCharts().add(new ChartSettings(chartName));
//                                tempList.add(chartName);
//                            }
//                        }
//                    }
//                }
//            }
//
//            AlphanumComparator ac = new AlphanumComparator();
//            tempList.sort(ac);
//
//            return FXCollections.observableArrayList(tempList);
//        } else return FXCollections.emptyObservableList();
    }

    public boolean containsId(Long id) {
        if (!getSelectedData().isEmpty()) {
            AtomicBoolean found = new AtomicBoolean(false);
            getSelectedData().forEach(chartDataModel -> {
                if (chartDataModel.getObject().getID().equals(id)) {
                    found.set(true);
                }
            });
            return found.get();
        } else return false;
    }

    public ChartDataModel get(Long id) {
        AtomicReference<ChartDataModel> out = new AtomicReference<>();
        getSelectedData().forEach(chartDataModel -> {
            if (chartDataModel.getObject().getID().equals(id)) {
                out.set(chartDataModel);
            }
        });
        return out.get();
    }
}
