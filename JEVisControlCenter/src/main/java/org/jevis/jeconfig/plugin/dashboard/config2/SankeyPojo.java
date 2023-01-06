package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import eu.hansolo.fx.charts.Grid;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;

import java.util.List;
import java.util.Optional;

public class SankeyPojo {


    private static final Logger logger = LogManager.getLogger(SankeyPojo.class);

    private final double iconSize = 20;

    int gaugeWidgetID = -1;

    private ObservableList<SankeyDataRow> netGraphDataRows = FXCollections.observableArrayList();

    protected DataModelDataHandler sampleHandler;

    final DashboardControl dashboardControl;

    private TableView sankeyTable;


    private boolean showFlow = true;


    private boolean colorGradient = true;

    public static final String UNIT = "Unit";

    public static final String PERCENT = "%";

    private String showValueIn = PERCENT;

    private JFXComboBox<String> jfxComboBoxShowValueIn;


    private JFXCheckBox jfxCheckBoxShowFlow;


    private JFXCheckBox jfxCheckBoxColorGradient;



    public SankeyPojo(DashboardControl control) {
        this(control, null);
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public SankeyPojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;
        SankeyTableFactory sankeyTableFactory = new SankeyTableFactory();
        sankeyTable = sankeyTableFactory.buildTable(netGraphDataRows);

        if (jsonNode != null) {
            if (jsonNode.has("showFlow")) {
                showFlow = jsonNode.get("showFlow").asBoolean(true);
            }
            if (jsonNode.has("colorGradient")) {
               colorGradient = jsonNode.get("colorGradient").asBoolean(true);
            }
            if (jsonNode.has("showValueIn")) {
                showValueIn = jsonNode.get("showValueIn").asText();
            }

            try {
                retrieveSankeyDataRowObjects(jsonNode);
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void retrieveSankeyDataRowObjects(JsonNode jsonNode) throws JEVisException {
        if (jsonNode.has("sankeyDataRows")) {
            for (int i = 0; i < jsonNode.get("sankeyDataRows").size(); i++) {
                SankeyDataRow sankeyDataRow = null;
                sankeyDataRow = new SankeyDataRow(dashboardControl.getDataSource().getObject(jsonNode.get("sankeyDataRows").get(i).get("id").asLong()));
                for (int j = 0; j < (jsonNode.get("sankeyDataRows").get(i).get("children").size()); j++) {
                    sankeyDataRow.addChildren(dashboardControl.getDataSource().getObject(jsonNode.get("sankeyDataRows").get(i).get("children").get(j).asLong()));
                }
                netGraphDataRows.add(sankeyDataRow);

            }
        }
        sankeyTable.getItems().addAll(netGraphDataRows);
    }

    public Tab getConfigTab(ObservableList<ChartData> tableList) {
        GridPane gridPane = new GridPane();

        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();

        column3.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        jfxCheckBoxShowFlow = new JFXCheckBox();
        jfxCheckBoxShowFlow.setSelected(showFlow);
        jfxCheckBoxColorGradient = new JFXCheckBox();
        jfxCheckBoxColorGradient.setSelected(colorGradient);

        jfxComboBoxShowValueIn = new JFXComboBox<>();
        jfxComboBoxShowValueIn.getItems().addAll(UNIT, PERCENT);
        jfxComboBoxShowValueIn.setValue(showValueIn);




        gridPane.addRow(0,new Label("show Flow"),jfxCheckBoxShowFlow);
        gridPane.addRow(1,new Label("use Color Gradient"),jfxCheckBoxColorGradient);
        gridPane.addRow(2,new Label("ShowValueIn"),jfxComboBoxShowValueIn);




        addChangeListenerForDataTable(tableList);
        gridPane.add(sankeyTable,0,3,3,1);


        SankeyPojo.GaugeDesignTab tab = new SankeyPojo.GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.net")
                , this);

        tab.setContent(gridPane);

        return tab;


    }

    private void addChangeListenerForDataTable(ObservableList<ChartData> tableList) {
        tableList.addListener(new ListChangeListener<ChartData>() {
            @Override
            public void onChanged(Change<? extends ChartData> c) {
                sankeyTable.getItems().clear();
                while (c.next()) {
                    for (ChartData chartData : c.getRemoved()) {
                       Optional<SankeyDataRow> sankeyDataRowOptional = netGraphDataRows.stream().filter(sankeyDataRow -> sankeyDataRow.getJeVisObject().equals(chartData.getObjectName())).findAny();
                        if (sankeyDataRowOptional.isPresent()) {
                            netGraphDataRows.remove(sankeyDataRowOptional.get());
                        }
                    }
                    for (ChartData chartData : c.getAddedSubList()) {
                        try {
                            netGraphDataRows.add(new SankeyDataRow(dashboardControl.getDataSource().getObject(chartData.getId())));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                sankeyTable.getItems().addAll(netGraphDataRows);


            }
        });
    }


    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put("showFlow", showFlow);
        dataNode.put("colorGradient", colorGradient);
        dataNode.put("showValueIn", showValueIn);
        ArrayNode arrayNode = dataNode.putArray("sankeyDataRows");
        netGraphDataRows.forEach(sankeyDataRow -> {
            ObjectNode sankeyData = arrayNode.addObject();
            sankeyData.put("id", sankeyDataRow.getJeVisObject().getID());
            ArrayNode children = sankeyData.putArray("children");
            //System.out.println(sankeyDataRow.getChildren());
            for (int i = 0; i < sankeyDataRow.getChildren().size(); i++) {
                children.add(sankeyDataRow.getChildren().get(i).getID());
            }


        });

        return dataNode;
    }


    public int getLimitSource() {
        return gaugeWidgetID;
    }

    public int getGaugeWidgetID() {
        return gaugeWidgetID;
    }

    public void setGaugeWidgetID(int gaugeWidgetID) {
        this.gaugeWidgetID = gaugeWidgetID;
    }

    public ObservableList<SankeyDataRow> getNetGraphDataRows() {
        return netGraphDataRows;
    }

    public void setNetGraphDataRows(ObservableList<SankeyDataRow> netGraphDataRows) {
        this.netGraphDataRows = netGraphDataRows;
    }

    public boolean isShowFlow() {
        return showFlow;
    }

    public void setShowFlow(boolean showFlow) {
        this.showFlow = showFlow;
    }




    public boolean isColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(boolean colorGradient) {
        this.colorGradient = colorGradient;
    }

    public String getShowValueIn() {
        return showValueIn;
    }

    public void setShowValueIn(String showValueIn) {
        this.showValueIn = showValueIn;
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        SankeyPojo sankeyPojo;

        public GaugeDesignTab(String text, SankeyPojo sankeyPojo) {
            super(text);
            this.sankeyPojo = sankeyPojo;
        }

        @Override
        public void commitChanges() {
            showFlow = jfxCheckBoxShowFlow.isSelected();
            colorGradient = jfxCheckBoxColorGradient.isSelected();
            showValueIn = jfxComboBoxShowValueIn.getValue();
        }
    }

    @Override
    public String toString() {
        return "";
    }


}