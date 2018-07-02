/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.jevis.api.*;
import org.jevis.application.jevistree.plugin.BarchartPlugin;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.ToolBarController;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author broder
 */
public class ToolBarView {

    private GraphDataModel model;
    private ToolBarController controller;
    private String nameCurrentAnalysis;
    private JEVisObject currentAnalysis;
    private List<JEVisObject> listAnalyses = new ArrayList<>();
    private JEVisObject analysesDir;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds) {
        this.model = model;
        this.controller = new ToolBarController(this, model, ds);
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        //load basic stuff
        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
        try {
            listAnalyses = ds.getObjects(ds.getJEVisClass("Analysis"), false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        for (JEVisObject obj : listAnalyses) {
            observableListAnalyses.add(obj.getName());
        }

        ComboBox listAnalysesComboBox = new ComboBox(observableListAnalyses);
        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                this.nameCurrentAnalysis = newValue.toString();
                setJEVisObjectForCurrentAnalysis(newValue.toString());

            }
        });

        Button newB = new Button("", JEConfig.getImage("list-add.png", iconSize, iconSize));
        //GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);

        Button save = new Button("", JEConfig.getImage("save.gif", iconSize, iconSize));
        //GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
        save.setOnAction(action -> {
            Dialog<ButtonType> newAnalysis = new Dialog<>();
            Label newText = new Label(I18n.getInstance().getString("newobject.name"));
            TextField name = new TextField();
            name.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    nameCurrentAnalysis = newValue;
                }
            });
            final ButtonType ok = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.FINISH);
            final ButtonType cancel = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            newAnalysis.getDialogPane().setContent(name);
            newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);
            newAnalysis.showAndWait()
                    .ifPresent(response -> {
                        if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                            try {
                                for (JEVisObject obj : ds.getObjects(ds.getJEVisClass("Analyses Directory"), false)) {
                                    analysesDir = obj;
                                    JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                                    obj.buildObject(nameCurrentAnalysis, classAnalysis);
                                    for (JEVisObject obj2 : ds.getObjects(classAnalysis, false)) {
                                        if (obj2.getName().equals(nameCurrentAnalysis)) {
                                            currentAnalysis = obj2;
                                        }
                                    }
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            saveDataModel(model.getSelectedData());
        });

        Button delete = new Button("", JEConfig.getImage("list-remove.png", iconSize, iconSize));
        //GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        save.setDisable(false);
        newB.setDisable(true);
        delete.setDisable(false);

//load new stuff
        Button select = new Button("", JEConfig.getImage("Data.png", iconSize, iconSize));
//        select.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//                controller.handleSelectRequest(ds);
//            }
//
//        });
        select.setOnAction(controller);

        toolBar.getItems().addAll(labelComboBox, listAnalysesComboBox, sep1, save, newB, delete, sep2, select);
        return toolBar;
    }

    private void saveDataModel(Set<BarchartPlugin.DataModel> selectedData) {
        try {
            JEVisAttribute dataModel = currentAnalysis.getAttribute("Data Model");
            DateTime now = DateTime.now();
            dataModel.buildSample(now.toDateTimeISO(), selectedData);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private void setJEVisObjectForCurrentAnalysis(String s) {
        JEVisObject currentAnalysis = null;
        for (JEVisObject obj : listAnalyses) {
            if (obj.getName().equals(s)) {
                currentAnalysis = obj;
            }
        }
        this.currentAnalysis = currentAnalysis;
    }

    public String getNameCurrentAnalysis() {
        return nameCurrentAnalysis;
    }
}
