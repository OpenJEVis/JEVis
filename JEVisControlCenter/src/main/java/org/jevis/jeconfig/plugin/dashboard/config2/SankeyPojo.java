package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.widget.SankeyWidget;

import java.util.*;

public class SankeyPojo {


    private static final Logger logger = LogManager.getLogger(SankeyPojo.class);

    private final double iconSize = 20;

    int gaugeWidgetID = -1;

    private ObservableList<SankeyDataRow> netGraphDataRows = FXCollections.observableArrayList();

    protected DataModelDataHandler sampleHandler;

    final DashboardControl dashboardControl;

    private TableView sankeyTable;


    private boolean showFlow = true;

    private boolean autoGap;

    private int gap;

    private boolean showValue = true;

    private boolean showPercent = true;

    private JFXCheckBox jfxCheckBoxShowValue;

    private JFXCheckBox jfxCheckBoxShowPercent;


    public static final String PARENT = I18n.getInstance().getString("plugin.dashboard.sankey.parent");

    public static final String ROOT = I18n.getInstance().getString("plugin.dashboard.sankey.root");


    private SankeyWidget.REFERS_TO percentRefersTo = SankeyWidget.REFERS_TO.PARENT;

    private JFXComboBox<String> jfxComboBoxRefersTo;

    private JFXCheckBox jfxCheckBoxAutoGap;
    private JFXTextField jfxTextFieldGap;
    private JFXCheckBox jfxCheckBoxShowFlow;

    private Map<Integer, Spinner<Integer>> offsetUIList = new HashMap<>();

    private Map<Integer, Integer> offsetMap = new HashMap<>();


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
            if (jsonNode.has("showValue")) {
                showValue = jsonNode.get("showValue").asBoolean(true);
            }
            if (jsonNode.has("showPercent")) {
                showPercent = jsonNode.get("showPercent").asBoolean(true);
            }
            if (jsonNode.has("refersTo")) {
                percentRefersTo = SankeyWidget.REFERS_TO.valueOf(jsonNode.get("refersTo").asText());
            }
            if (jsonNode.has("autoGap")) {
                autoGap = jsonNode.get("autoGap").asBoolean();
            }
            if (jsonNode.has("gap")) {
                gap = jsonNode.get("gap").asInt(0);
            }


            try {
                retrieveSankeyDataRowObjects(jsonNode);
                retrieveOffsets(jsonNode);
            } catch (JEVisException e) {
                e.printStackTrace();
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

    private void retrieveOffsets(JsonNode jsonNode) {
        if (jsonNode.has("offsets")) {
            jsonNode.get("offsets").forEach(jsonNode1 -> {
                offsetMap.put(jsonNode1.get("level").asInt(), jsonNode1.get("offset").asInt());
            });
        }

    }

    public Tab getConfigTab(ObservableList<ChartData> tableList) {
        GridPane gridPane = createGridpane();

        jfxCheckBoxShowFlow = new JFXCheckBox();
        jfxCheckBoxShowFlow.setSelected(showFlow);


        jfxCheckBoxShowValue = new JFXCheckBox();
        jfxCheckBoxShowValue.setSelected(showValue);

        jfxCheckBoxShowPercent = new JFXCheckBox();
        jfxCheckBoxShowPercent.setSelected(showPercent);

        jfxComboBoxRefersTo = new JFXComboBox<>();
        if (percentRefersTo.equals(SankeyWidget.REFERS_TO.PARENT)) {
            jfxComboBoxRefersTo.setValue(PARENT);
        } else if (percentRefersTo.equals(SankeyWidget.REFERS_TO.ROOT)) {
            jfxComboBoxRefersTo.setValue(ROOT);
        }

        jfxComboBoxRefersTo.getItems().addAll(PARENT, ROOT);
        Label label = new Label(I18n.getInstance().getString("plugin.dashboard.sankey.refersto"));


        if (jfxCheckBoxShowPercent.isSelected()) {
            jfxComboBoxRefersTo.setVisible(true);
            label.setVisible(true);
        }else {
            jfxComboBoxRefersTo.setVisible(false);
            label.setVisible(false);
        }

        jfxCheckBoxShowPercent.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                jfxComboBoxRefersTo.setVisible(true);
                label.setVisible(true);
            }else {
                jfxComboBoxRefersTo.setVisible(false);
                label.setVisible(false);
            }
        });

        jfxTextFieldGap = new JFXTextField();
        jfxTextFieldGap.setText(String.valueOf(gap));
        Label label1 = new Label();
        label1.setText(I18n.getInstance().getString("plugin.dashboard.sankey.gap"));

        if (autoGap) {
            jfxTextFieldGap.setDisable(true);
            jfxTextFieldGap.setVisible(false);
            label1.setDisable(true);
            label1.setVisible(false);
        }


        jfxCheckBoxAutoGap = new JFXCheckBox();
        jfxCheckBoxAutoGap.setSelected(autoGap);
        jfxCheckBoxAutoGap.setOnAction(actionEvent -> {
            if (jfxCheckBoxAutoGap.isSelected()) {
                jfxTextFieldGap.setDisable(true);
                jfxTextFieldGap.setVisible(false);
                label1.setDisable(true);
                label1.setVisible(false);
            } else {
                jfxTextFieldGap.setDisable(false);
                jfxTextFieldGap.setVisible(true);
                label1.setDisable(false);
                label1.setVisible(true);
            }
        });


        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.sankey.showflow")), jfxCheckBoxShowFlow);

        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 1, 5, 1);

        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.dashboard.sankey.showvaluein")),jfxCheckBoxShowValue);
        gridPane.addRow(3, new Label(I18n.getInstance().getString("plugin.dashboard.sankey.showpercent")),jfxCheckBoxShowPercent,label,jfxComboBoxRefersTo);

        gridPane.addRow(4, new Label(I18n.getInstance().getString("plugin.dashboard.sankey.autogap")), jfxCheckBoxAutoGap, label1, jfxTextFieldGap);


        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 5, 5, 1);
        int j = 6;
        for (int i = 1; i < getMaxLevel(); i++) {
            if (offsetMap.containsKey(i)) {
                createOffsetLevelRow(gridPane, i, offsetMap.get(i), j);
            } else {
                createOffsetLevelRow(gridPane, i, 0, j);
            }


            j++;
        }


        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, j, 5, 1);
        j++;

        addChangeListenerForDataTable(tableList);
        gridPane.add(sankeyTable, 0, j, 5, 5);

        for (int i = 0; i <= j; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            if (i == j) {
                rowConstraints.setVgrow(Priority.ALWAYS);
            } else {
                rowConstraints.setVgrow(Priority.NEVER);
            }
            gridPane.getRowConstraints().add(rowConstraints);
        }


        SankeyPojo.GaugeDesignTab tab = new SankeyPojo.GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.sankey")
                , this);

        tab.setContent(gridPane);

        return tab;


    }

    @NotNull
    private static GridPane createGridpane() {
        GridPane gridPane = new GridPane();


        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();
        ColumnConstraints column4 = new ColumnConstraints();
        ColumnConstraints column5 = new ColumnConstraints();

        column1.setHgrow(Priority.NEVER);
        column2.setHgrow(Priority.NEVER);
        column3.setHgrow(Priority.NEVER);
        column4.setHgrow(Priority.NEVER);
        column5.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2, column3, column4, column5);
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 5, 8, 5));
        return gridPane;
    }

    private void createOffsetLevelRow(GridPane gridPane, int level, int offset, int row) {


        Label levelLabel = new Label(I18n.getInstance().getString("plugin.dashboard.sankey.level") + ": " + (level + 1));
        Label offsetLabel = new Label(I18n.getInstance().getString("plugin.dashboard.sankey.offset"));

        Tooltip tooltipLevel = new Tooltip(I18n.getInstance().getString("plugin.dashboard.sankey.tolltip.level"));
        Tooltip tooltipOffset = new Tooltip(I18n.getInstance().getString("plugin.dashboard.sankey.tolltip.offset"));
        levelLabel.setTooltip(tooltipLevel);
        offsetLabel.setTooltip(tooltipOffset);

        Spinner<Integer> offsetSpinner = new Spinner<>();
        offsetSpinner.setEditable(true);
        SpinnerValueFactory<Integer> valueFactoryOffset = new SpinnerValueFactory.IntegerSpinnerValueFactory(-800, 800, offset);


        gridPane.addRow(row, levelLabel, offsetLabel, offsetSpinner);


        offsetSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (nv) return;
            commitEditorText(offsetSpinner);
        });

        offsetSpinner.setValueFactory(valueFactoryOffset);


        this.offsetUIList.put(level, offsetSpinner);

    }

    public int getMaxLevel() {
        int level = 0;
        for (SankeyDataRow sankeyDataRow : netGraphDataRows) {
            int i = calculateMaxLevel(sankeyDataRow, 0);
            if (level < i) {
                level = i;
            }
        }
        return level;
    }

    private int calculateMaxLevel(SankeyDataRow sankeyDataRow, int i) {
        try {
            int z = i;
            if (sankeyDataRow.getChildren().size() > 0) {

                for (JEVisObject jeVisObject : sankeyDataRow.getChildren()) {
                    Optional<SankeyDataRow> sankeyDataRow1 = netGraphDataRows.stream().filter(sankeyDataRow2 -> sankeyDataRow2.getJeVisObject().getID().intValue() == jeVisObject.getID().intValue()).findAny();
                    if (sankeyDataRow1.isPresent()) {
                        int j = calculateMaxLevel(sankeyDataRow1.get(), i + 1);
                        if (j > z) {
                            z = j;
                        }
                    }
                }
            }
            return z;
        } catch (Exception e) {
            logger.error(e);
        }
        return 0;

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

    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }


    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put("showFlow", showFlow);
        dataNode.put("showValue", showValue);
        dataNode.put("showPercent", showPercent);
        dataNode.put("refersTo", percentRefersTo.toString());
        dataNode.put("autoGap", autoGap);
        dataNode.put("gap", gap);
        ArrayNode arrayNode = dataNode.putArray("sankeyDataRows");
        netGraphDataRows.forEach(sankeyDataRow -> {
            ObjectNode sankeyData = arrayNode.addObject();
            sankeyData.put("id", sankeyDataRow.getJeVisObject().getID());
            ArrayNode children = sankeyData.putArray("children");
            for (int i = 0; i < sankeyDataRow.getChildren().size(); i++) {
                children.add(sankeyDataRow.getChildren().get(i).getID());
            }
        });
        ArrayNode arrayNodeOffset = dataNode.putArray("offsets");
        offsetMap.forEach((integer, integer2) -> {
            ObjectNode objectNode = arrayNodeOffset.addObject();
            objectNode.put("level", integer);
            objectNode.put("offset", integer2);
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

    public SankeyWidget.REFERS_TO getPercentRefersTo() {
        return percentRefersTo;
    }

    public void setPercentRefersTo(String percentRefersTo) {
        this.percentRefersTo = SankeyWidget.REFERS_TO.valueOf(percentRefersTo);
    }

    public boolean isAutoGap() {
        return autoGap;
    }

    public void setAutoGap(boolean autoGap) {
        this.autoGap = autoGap;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }


    public Map<Integer, Integer> getOffsetMap() {
        return offsetMap;
    }

    public void setOffsetMap(Map<Integer, Integer> offsetMap) {
        this.offsetMap = offsetMap;
    }

    public boolean isShowValue() {
        return showValue;
    }

    public void setShowValue(boolean showValue) {
        this.showValue = showValue;
    }

    public boolean isShowPercent() {
        return showPercent;
    }

    public void setShowPercent(boolean showPercent) {
        this.showPercent = showPercent;
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
            showPercent = jfxCheckBoxShowPercent.isSelected();
            showValue = jfxCheckBoxShowValue.isSelected();
            if (jfxComboBoxRefersTo.getValue().equals(PARENT)) {
                percentRefersTo = SankeyWidget.REFERS_TO.PARENT;
            } else if (jfxComboBoxRefersTo.getValue().equals(ROOT)) {
                percentRefersTo = SankeyWidget.REFERS_TO.ROOT;
            }

            autoGap = jfxCheckBoxAutoGap.isSelected();
            gap = Integer.valueOf(jfxTextFieldGap.getText());


            offsetMap.clear();
            offsetUIList.forEach((integer, integerSpinner) -> {
                offsetMap.put(integer, integerSpinner.getValue());
            });

        }
    }

    @Override
    public String toString() {
        return "";
    }


}