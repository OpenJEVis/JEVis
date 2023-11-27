package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.data.ChartData;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jecc.plugin.dashboard.widget.NetGraphWidget;
import org.jevis.jecc.plugin.dashboard.widget.ShapeWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NetGraphPojo {


    private static final Logger logger = LogManager.getLogger(NetGraphPojo.class);
    final DashboardControl dashboardControl;
    private final double iconSize = 20;
    private final Button ButtonDelete = new Button("", ControlCenter.getImage("if_trash_(delete)_16x16_10030.gif", this.iconSize, this.iconSize));
    private final Button ButtonAdd = new Button("", ControlCenter.getImage("list-add.png", this.iconSize, this.iconSize));
    private final ActionEvent actionEvent = new ActionEvent();
    protected DataModelDataHandler sampleHandler;
    int gaugeWidgetID = -1;
    ObservableList<String> skins = FXCollections.observableArrayList();
    private Map<Long, NetGraphDataRow> netGraphDataRows = new HashMap<>();
    private boolean inPercent = false;
    private double max = 35;
    private NetGraphWidget.SKIN skin = NetGraphWidget.SKIN.SECTOR;
    private ComboBox<NetGraphWidget.SKIN> comboBox;
    private CheckBox jfxCheckBoxInPercent;

    private TextField textFieldMax;


    private TableView tableViewSections;

    private GridPane gridPane;

    public NetGraphPojo(DashboardControl control) {
        this(control, null);

    }

    public NetGraphPojo(DashboardControl control, JsonNode jsonNode) {
        skins.addAll(eu.hansolo.medusa.Gauge.SkinType.DASHBOARD.toString(), eu.hansolo.medusa.Gauge.SkinType.SIMPLE.toString());
        this.dashboardControl = control;

        if (jsonNode != null) {

            if (jsonNode.has("skin")) {
                String jsonskin = jsonNode.get("skin").asText(ShapeWidget.SHAPE.RECTANGLE.toString());
                try {
                    skin = NetGraphWidget.SKIN.valueOf(jsonskin);
                } catch (Exception e) {
                    logger.error(e);
                }
            }

            if (jsonNode.has("inPercent")) {
                inPercent = jsonNode.get("inPercent").asBoolean(false);
            }
            if (jsonNode.has("max")) {
                max = jsonNode.get("max").asDouble(35);
            }
            if (jsonNode.has("netGraphDataRows")) {
                for (int i = 0; i < jsonNode.get("netGraphDataRows").size(); i++) {
                    double max = 100;
                    double min = 0;
                    Long id = Long.valueOf(0);
                    String displayname = null;

                    if (jsonNode.get("netGraphDataRows").get(i).has("max")) {
                        max = jsonNode.get("netGraphDataRows").get(i).get("max").asDouble();
                    }
                    if (jsonNode.get("netGraphDataRows").get(i).has("min")) {
                        min = jsonNode.get("netGraphDataRows").get(i).get("min").asDouble();

                    }
                    if (jsonNode.get("netGraphDataRows").get(i).has("id")) {
                        id = jsonNode.get("netGraphDataRows").get(i).get("id").asLong();
                    }
                    if (id != 0) {
                        try {
                            netGraphDataRows.put(id, new NetGraphDataRow(control.getDataSource().getObject(id), min, max));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    logger.debug(netGraphDataRows);
                }


            } else {
                logger.debug("no entry");
            }


        }


    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public Tab getConfigTab(ObservableList<ChartData> tableList) {

        tableList.addListener(new ListChangeListener<ChartData>() {
            @Override
            public void onChanged(Change<? extends ChartData> c) {
                tableViewSections.getItems().clear();
                while (c.next()) {
                    for (ChartData chartData : c.getRemoved()) {
                        netGraphDataRows.remove(chartData.getId());

                    }
                    for (ChartData chartData : c.getAddedSubList()) {
                        try {
                            netGraphDataRows.putIfAbsent(chartData.getId(), new NetGraphDataRow(dashboardControl.getDataSource().getObject(chartData.getId()), 0, 100));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                tableViewSections.getItems().addAll(netGraphDataRows.entrySet().stream().map(longNetGraphPojoEntryEntry -> longNetGraphPojoEntryEntry.getValue()).collect(Collectors.toList()));


            }
        });


        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.net")
                , this);
        gridPane = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        gridPane.setPadding(new Insets(8, 5, 8, 5));

        NetGraphTableFactory netGraphTableFactory = new NetGraphTableFactory();
        tableViewSections = netGraphTableFactory.buildTable(!inPercent);
        createShowCheckboxes(netGraphTableFactory);


        tab.setContent(gridPane);
        return tab;
    }

    private void createShowCheckboxes(NetGraphTableFactory netGraphTableFactory) {

        textFieldMax = new TextField(String.valueOf(max));
        jfxCheckBoxInPercent = new CheckBox();
        jfxCheckBoxInPercent.setSelected(inPercent);
        netGraphTableFactory.setDisable(!inPercent);
        jfxCheckBoxInPercent.selectedProperty().addListener((observable, oldValue, newValue) -> {
            netGraphTableFactory.setDisable(!newValue);
            if (!newValue) {
                showMinMax();
            } else if (newValue) {
                hideMinMax();
            }
        });
        comboBox = new ComboBox<>();

        for (NetGraphWidget.SKIN s : NetGraphWidget.SKIN.values()) {
            comboBox.getItems().add(s);
        }
        comboBox.setValue(skin);
        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.inPercent")), jfxCheckBoxInPercent);
        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.graph.dashboard.gaugewidget.section")), comboBox);

        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 2, 3, 1);
        tableViewSections.getItems().addAll(netGraphDataRows.entrySet().stream().map(longNetGraphPojoEntryEntry -> longNetGraphPojoEntryEntry.getValue()).collect(Collectors.toList()));
        gridPane.add(tableViewSections, 0, 3, 3, 2);
        if (!isInPercent()) {
            showMinMax();
        }

    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put("inPercent", inPercent);
        dataNode.put("skin", skin.toString());
        dataNode.put("max", max);

        ArrayNode arrayNode = dataNode.putArray("netGraphDataRows");
        for (Map.Entry<Long, NetGraphDataRow> entry : netGraphDataRows.entrySet()) {
            ObjectNode dataNode1 = arrayNode.addObject();
            dataNode1.put("min", entry.getValue().getMin());
            dataNode1.put("max", entry.getValue().getMax());
            dataNode1.put("id", entry.getValue().getJeVisObject().getID());

        }
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

    public NetGraphDataRow getNetGraphDataRow(Long id) {
        return netGraphDataRows.get(id);
    }

    public void addNetGraphDataRow(long id, NetGraphDataRow netGraphDataRow) {
        netGraphDataRows.put(id, netGraphDataRow);
    }

    public Map<Long, NetGraphDataRow> getNetGraphDataRows() {
        return netGraphDataRows;
    }

    public void setNetGraphDataRows(Map<Long, NetGraphDataRow> netGraphDataRows) {
        this.netGraphDataRows = netGraphDataRows;
    }

    public boolean isInPercent() {
        return inPercent;
    }

    public void setInPercent(boolean inPercent) {
        this.inPercent = inPercent;
    }

    public NetGraphWidget.SKIN getSkin() {
        return skin;
    }

    public void setSkin(NetGraphWidget.SKIN skin) {
        this.skin = skin;
    }

    @Override
    public String toString() {
        return "NetGraphPojo{" +
                "iconSize=" + iconSize +
                ", ButtonDelete=" + ButtonDelete +
                ", ButtonAdd=" + ButtonAdd +
                ", gaugeWidgetID=" + gaugeWidgetID +
                ", netGraphPojoEntries=" + netGraphDataRows +
                ", sampleHandler=" + sampleHandler +
                ", dashboardControl=" + dashboardControl +
                ", actionEvent=" + actionEvent +
                ", inPercent=" + inPercent +
                ", skin=" + skin +
                ", comboBox=" + comboBox +
                ", skins=" + skins +
                ", jfxCheckBoxInPercent=" + jfxCheckBoxInPercent +
                ", tableViewSections=" + tableViewSections +
                ", gridPane=" + gridPane +
                '}';
    }


    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    private void showMinMax() {
        moveplusNodesGridpane(3, 1);
        gridPane.addRow(4, new Label(I18n.getInstance().getString("plugin.dashboard.net.max")), textFieldMax);


    }

    private void hideMinMax() {
        gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == GridPane.getRowIndex(textFieldMax));
        moveminusNodesGridpane(3, 1);


    }

    private void moveminusNodesGridpane(int targetRowIndex, int movePos) {
        gridPane.getChildren().forEach(node -> {
            final int rowIndex = GridPane.getRowIndex(node);
            if (targetRowIndex <= rowIndex) {
                GridPane.setRowIndex(node, rowIndex - 2);
            }
        });
    }

    private void moveplusNodesGridpane(int targetRowIndex, int movePos) {
        gridPane.getChildren().forEach(node -> {
            final int rowIndex = GridPane.getRowIndex(node);
            if (targetRowIndex <= rowIndex) {
                GridPane.setRowIndex(node, rowIndex + 2);
            }
        });
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        NetGraphPojo gaugeDesign;

        public GaugeDesignTab(String text, NetGraphPojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {
            try {
                inPercent = jfxCheckBoxInPercent.isSelected();

                if (!textFieldMax.getText().isEmpty()) {
                    max = Double.parseDouble(textFieldMax.getText());
                }

                skin = comboBox.getValue();


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


}