/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.jevis.api.*;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.ToolBarController;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author broder
 */
public class ToolBarView {

    private final JEVisDataSource ds;
    private GraphDataModel model;
    private ToolBarController controller;
    private String nameCurrentAnalysis;
    private JEVisObject currentAnalysis;
    private List<JEVisObject> listAnalyses = new ArrayList<>();
    private JEVisObject analysesDir;
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private ComboBox listAnalysesComboBox;
    private List<JsonAnalysisModel> listAnalysisModel;
    private BorderPane border;
    private AreaChartView view;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds) {
        this.model = model;
        this.controller = new ToolBarController(this, model, ds);
        this.ds = ds;
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        //load basic stuff
        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        listAnalysesComboBox = new ComboBox();
        updateListAnalyses();
        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                this.nameCurrentAnalysis = newValue.toString();
                setJEVisObjectForCurrentAnalysis(newValue.toString());
                getListAnalysis();
                newChart();
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
                                    System.out.println("Analyseverz: " + analysesDir.getName());
                                    System.out.println("nameCurrAnalysis: " + nameCurrentAnalysis);
                                    JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                                    currentAnalysis = obj.buildObject(nameCurrentAnalysis, classAnalysis);
                                    currentAnalysis.commit();
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        }
                        saveDataModel(model.getSelectedData());
                        updateListAnalyses();
                    });
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

    private void saveDataModel(Set<BarChartDataModel> selectedData) {
        try {
            JEVisAttribute dataModel = currentAnalysis.getAttribute("Data Model");

            List<JsonAnalysisModel> jsonDataModels = new ArrayList<>();
            for (BarChartDataModel mdl : selectedData) {
                JsonAnalysisModel json = new JsonAnalysisModel();
                json.setName(mdl.getTitle());
                json.setColor(mdl.getColor().toString());
                json.setObject(mdl.getObject().getID().toString());
                json.setDataProcessorObject(mdl.getDataProcessor().getID().toString());
                json.setSelectedStart(mdl.getSelectedStart().toString());
                json.setSelectedEnd(mdl.getSelectedEnd().toString());
                jsonDataModels.add(json);
            }
            DateTime now = DateTime.now();
            JEVisSample smp = dataModel.buildSample(now.toDateTimeISO(), jsonDataModels.toString());
            smp.commit();

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

    public void updateListAnalyses() {
        try {
            listAnalyses = ds.getObjects(ds.getJEVisClass("Analysis"), false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        observableListAnalyses.clear();
        for (JEVisObject obj : listAnalyses) {
            observableListAnalyses.add(obj.getName());
        }
        listAnalysesComboBox.setItems(observableListAnalyses);
    }

    public void getListAnalysis() {
        try {
            if (currentAnalysis == null) {
                updateListAnalyses();
                setJEVisObjectForCurrentAnalysis(observableListAnalyses.get(0));
            }
            if (Objects.nonNull(currentAnalysis.getAttribute("Data Model"))) {
                if (currentAnalysis.getAttribute("Data Model").hasSample()) {
                    String str = currentAnalysis.getAttribute("Data Model").getLatestSample().getValueAsString();
                    if (str.endsWith("]")) {
                        listAnalysisModel = new Gson().fromJson(str, new TypeToken<List<JsonAnalysisModel>>() {
                        }.getType());

                    } else {
                        listAnalysisModel = new ArrayList<>();
                        listAnalysisModel.add(new Gson().fromJson(str, JsonAnalysisModel.class));
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public void newChart() {
        Map<String, BarChartDataModel> data = new HashMap<>();

        for (JsonAnalysisModel mdl : listAnalysisModel) {
            BarChartDataModel newData = new BarChartDataModel();
            try {
                Long id = Long.parseLong(mdl.getObject());
                Long id_dp = Long.parseLong(mdl.getDataProcessorObject());
                JEVisObject obj = ds.getObject(id);
                JEVisObject obj_dp = ds.getObject(id_dp);
                newData.setObject(obj);
                newData.setColor(Color.valueOf(mdl.getColor()));
                newData.setTitle(mdl.getName());
                newData.setDataProcessor(obj_dp);
                newData.getAttribute();
                newData.setSelected(true);
                newData.set_somethingChanged(true);
                newData.getSamples();
                data.put(id.toString(), newData);
            } catch (JEVisException e) {
                e.printStackTrace();
            }

        }
        Set<BarChartDataModel> selectedData = new HashSet<>();
        for (Map.Entry<String, BarChartDataModel> entrySet : data.entrySet()) {
            BarChartDataModel value = entrySet.getValue();
            if (value.getSelected()) {
                selectedData.add(value);
            }
        }
        model.setSelectedData(selectedData);

        view = new AreaChartView(model);
        try {
            view.drawAreaChart();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public void selectFirst() {
        listAnalysesComboBox.getSelectionModel().selectFirst();
    }

}
