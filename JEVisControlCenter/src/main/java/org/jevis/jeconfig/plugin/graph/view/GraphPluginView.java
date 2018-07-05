/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.graph.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.converter.LocalTimeStringConverter;
import jfxtras.scene.control.ListView;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.dialog.GraphSelectionDialog;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.graph.GraphController;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPluginView implements Plugin, Observer {

    private ToolBarView toolBarView;
    private GraphDataModel dataModel;
    private GraphController controller;
    private AreaChartView chartView;

    private StringProperty name = new SimpleStringProperty("Graph");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border;
    private boolean firstStart = true;
    DateTime selectedStart;

    private ToolBar toolBar;
    DateTime selectedEnd;
    private ListView<String> lv = new ListView<>();
    private List<JEVisObject> listAnalyses;
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private List<JsonAnalysisModel> listAnalysisModel;
    private JEVisObject currentAnalysis;
    private String nameCurrentAnalysis;
    private Dialog<ButtonType> dialog = new Dialog<>();
    private BarChartDataModel data = new BarChartDataModel();
    private JFXDatePicker pickerDateStart = new JFXDatePicker();
    private JFXTimePicker pickerTimeStart = new JFXTimePicker();
//    private ObjectTree tf;
private JFXDatePicker pickerDateEnd = new JFXDatePicker();
    private JFXTimePicker pickerTimeEnd = new JFXTimePicker();
    public GraphPluginView(JEVisDataSource ds, String newname) {
        dataModel = new GraphDataModel();
        dataModel.addObserver(this);

        controller = new GraphController(this, dataModel);
        toolBarView = new ToolBarView(dataModel, ds, chartView);
        chartView = new AreaChartView(dataModel);

        this.ds = ds;
        name.set(newname);
    }

    @Override
    public void setHasFocus() {

        if (firstStart) {
            firstStart = false;


            dialog.setTitle(I18n.getInstance().getString("plugin.graph.analysis.dialog.title"));

            final ButtonType newGraph = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.new"), ButtonBar.ButtonData.FINISH);
            final ButtonType loadGraph = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.load"), ButtonBar.ButtonData.NO);

            updateListAnalyses();
            getListAnalysis();

            HBox hbox_list = new HBox();
            hbox_list.getChildren().add(lv);
            HBox.setHgrow(lv, Priority.ALWAYS);

            Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate"));
            pickerDateStart.setPrefWidth(120d);
            pickerTimeStart.setIs24HourView(true);
            pickerTimeStart.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

            Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
            pickerDateEnd.setPrefWidth(120d);
            pickerTimeEnd.setIs24HourView(true);
            pickerTimeEnd.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

            ToggleButton lastDay = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlastday"));
            ToggleButton last30Days = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days"));
            ToggleButton lastWeek = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek"));
            ToggleButton lastMonth = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth"));

            if (!listAnalysisModel.isEmpty()) {
                DateTime start = DateTime.parse(listAnalysisModel.get(0).getSelectedStart());
                LocalDate ld_start = LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth());
                LocalTime lt_start = LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour());
                pickerDateStart.valueProperty().setValue(ld_start);
                pickerTimeStart.valueProperty().setValue(lt_start);
                selectedStart = start;

                DateTime end = DateTime.parse(listAnalysisModel.get(0).getSelectedEnd());
                LocalDate ld_end = LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth());
                LocalTime lt_end = LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour());
                pickerDateEnd.valueProperty().setValue(ld_end);
                pickerTimeEnd.valueProperty().setValue(lt_end);
                selectedEnd = end;
            }

            lastDay.setOnAction(event -> {
                LocalDate ld = LocalDate.now();
                LocalDate ld_start = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth());
                LocalDate ld_end = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth());
                pickerDateStart.valueProperty().setValue(ld_start);
                pickerDateEnd.valueProperty().setValue(ld_end);

                pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
            });

            last30Days.setOnAction(event -> {
                LocalDate ld = LocalDate.now();
                LocalDate ld_start = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth());
                LocalDate ld_end = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth());
                pickerDateStart.valueProperty().setValue(ld_start.minusDays(30));
                pickerDateEnd.valueProperty().setValue(ld_end);

                pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
            });

            lastWeek.setOnAction(event -> {
                LocalDate ld = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
                ld = ld.minusWeeks(1);
                LocalDate ld_start = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth());
                LocalDate ld_end = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth());
                ld_end = ld_end.plusDays(6);
                pickerDateStart.valueProperty().setValue(ld_start);
                pickerDateEnd.valueProperty().setValue(ld_end);

                pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
            });

            lastMonth.setOnAction(event -> {
                LocalDate ld = LocalDate.now();
                ld = ld.minusDays(LocalDate.now().getDayOfMonth() - 1);
                LocalDate ld_start = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth()).minusMonths(1);
                LocalDate ld_end = LocalDate.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth()).minusDays(1);
                pickerDateStart.valueProperty().setValue(ld_start);
                pickerDateEnd.valueProperty().setValue(ld_end);
            });

            pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    selectedStart = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedStart.getHourOfDay(), selectedStart.getMinuteOfHour(), selectedStart.getMillisOfSecond());
                    lastDay.setSelected(false);
                    lastWeek.setSelected(false);
                    lastMonth.setSelected(false);
                }
            });

            pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    selectedEnd = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedEnd.getHourOfDay(), selectedEnd.getMinuteOfHour(), selectedEnd.getMillisOfSecond());
                    lastDay.setSelected(false);
                    lastWeek.setSelected(false);
                    lastMonth.setSelected(false);
                }
            });

            GridPane gp_date = new GridPane();
            Separator sep1 = new Separator();
            Separator sep2 = new Separator();
            sep1.setOrientation(Orientation.HORIZONTAL);
            sep2.setOrientation(Orientation.HORIZONTAL);

            HBox startBox = new HBox();
            startBox.setSpacing(4);
            startBox.getChildren().addAll(pickerDateStart, pickerTimeStart);

            HBox endBox = new HBox();
            endBox.setSpacing(4);
            endBox.getChildren().addAll(pickerDateEnd, pickerTimeEnd);

            VBox vbox_picker = new VBox();
            vbox_picker.setSpacing(4);
            vbox_picker.getChildren().addAll(startText, startBox, sep1, endText, endBox);
            VBox vbox_buttons = new VBox();
            vbox_buttons.setSpacing(4);
            vbox_buttons.getChildren().addAll(lastDay, last30Days, lastWeek, lastMonth);
            gp_date.add(vbox_picker, 0, 0);
            gp_date.add(vbox_buttons, 1, 0);
            gp_date.setPrefWidth(hbox_list.getWidth());
            gp_date.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            VBox vbox = new VBox();
            vbox.setSpacing(14);
            vbox.getChildren().addAll(hbox_list, gp_date);
            vbox.setPrefWidth(600);

            lv.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    this.nameCurrentAnalysis = newValue;
                    setJEVisObjectForCurrentAnalysis(newValue);

                    if (oldValue == null) {
                        dialog.getDialogPane().getButtonTypes().clear();
                        dialog.getDialogPane().getButtonTypes().addAll(newGraph, loadGraph);
                    }
                }
            });

            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().getButtonTypes().add(newGraph);
            dialog.showAndWait()
                    .ifPresent(response -> {
                        if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                            GraphSelectionDialog selectionDialog = new GraphSelectionDialog(ds);

                            if (selectionDialog.show(JEConfig.getStage()) == GraphSelectionDialog.Response.OK) {

                                Set<BarChartDataModel> selectedData = new HashSet<>();
                                for (Map.Entry<String, BarChartDataModel> entrySet : selectionDialog.getSelectedData().entrySet()) {
                                    BarChartDataModel value = entrySet.getValue();
                                    if (value.getSelected()) {
                                        selectedData.add(value);
                                    }
                                }
                                dataModel.setSelectedData(selectedData);
                            }
                        } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                            for (JsonAnalysisModel mdl : listAnalysisModel) {
                                mdl.setSelectedStart(selectedStart.toString());
                                mdl.setSelectedEnd(selectedEnd.toString());
                            }
                            updateToolBarView();
                            toolBarView.select(lv.getSelectionModel().getSelectedItem());
                        }
                    });


        }

    }

    private void updateToolBarView() {

        toolBarView.setCurrentAnalysis(this.currentAnalysis);
        toolBarView.setListAnalyses(this.listAnalyses);
        toolBarView.setListAnalysisModel(this.listAnalysisModel);
        toolBarView.setNameCurrentAnalysis(this.nameCurrentAnalysis);
        toolBarView.setSelectedStart(this.selectedStart);
        toolBarView.setSelectedEnd(this.selectedEnd);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String value) {
        name.set(value);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getUUID() {
        return id.get();
    }

    @Override
    public void setUUID(String newid) {
        id.set(newid);
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getConntentNode() {
        if (border == null) {
            border = new BorderPane();
            chartView.drawDefaultAreaChart();
            border.setCenter(chartView.getAreaChartRegion());
//            border.setCenter(new Button("click me"));

//            border.setCenter(lineChart);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        }

        return border;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {
        if (toolBar == null) {
            toolBar = toolBarView.getToolbar(getDataSource());
        }
        return toolBar;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        //TODO: implement
        return false;
    }

    @Override
    public void handelRequest(int cmdType) {
        try {
            System.out.println("Command to ClassPlugin: " + cmdType);
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    System.out.println("save");
                    break;
                case Constants.Plugin.Command.DELTE:
                    break;
                case Constants.Plugin.Command.EXPAND:
                    System.out.println("Expand");
                    break;
                case Constants.Plugin.Command.NEW:
                    break;
                case Constants.Plugin.Command.RELOAD:
                    System.out.println("reload");
                    break;
                default:
                    System.out.println("Unknows command ignore...");
            }
        } catch (Exception ex) {
        }

    }

    @Override
    public void fireCloseEvent() {
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("1415314386_Graph.png", 20, 20);
    }

    @Override
    public void update(Observable o, Object arg) {
        //create new chart
        System.out.println("update view");
        System.out.println(chartView.getAreaChart().getTitle());
//        border.setCenter(new Button("click me"));
        border.setTop(chartView.getLegend());
        border.setCenter(chartView.getAreaChartRegion());
        border.setBottom(chartView.getVbox());

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
        lv.setItems(observableListAnalyses);
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

    private void updateData() {
        Set<BarChartDataModel> selectedData = getBarChartDataModels();

        dataModel.setSelectedData(selectedData);
    }

    private Set<BarChartDataModel> getBarChartDataModels() {
        Map<String, BarChartDataModel> data = new HashMap<>();

        for (JsonAnalysisModel mdl : listAnalysisModel) {
            BarChartDataModel newData = new BarChartDataModel();
            try {
                Long id = Long.parseLong(mdl.getObject());
                Long id_dp = Long.parseLong(mdl.getDataProcessorObject());
                JEVisObject obj = ds.getObject(id);
                JEVisObject obj_dp = ds.getObject(id_dp);
                DateTime start = DateTime.parse(mdl.getSelectedStart());
                DateTime end = DateTime.parse(mdl.getSelectedEnd());
                Boolean selected = Boolean.parseBoolean(mdl.getSelected());
                newData.setObject(obj);
                newData.setSelectedStart(start);
                newData.setSelectedEnd(end);
                newData.setColor(Color.valueOf(mdl.getColor()));
                newData.setTitle(mdl.getName());
                newData.setDataProcessor(obj_dp);
                newData.getAttribute();
                newData.setSelected(selected);
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
        return selectedData;
    }

    private enum DATE_TYPE {
        START, END
    }
}
