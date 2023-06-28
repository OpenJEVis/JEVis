package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GaugePojo {


    private static final Logger logger = LogManager.getLogger(GaugePojo.class);

    private final double iconSize = 20;

    private final JFXButton jfxButtonDelete = new JFXButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", this.iconSize, this.iconSize));
    private final JFXButton jfxButtonAdd = new JFXButton("", JEConfig.getImage("list-add.png", this.iconSize, this.iconSize));

    int gaugeWidgetID = -1;

    private double maximum = 0;

    private double minimum = 0;

    private boolean inPercent = false;

    private ObservableList<GaugeSectionPojo> sections = FXCollections.observableArrayList();

    private List<GaugeSectionPojo> sectionsComitted = new ArrayList<>();

    final DashboardControl dashboardControl;


    private final ActionEvent actionEvent = new ActionEvent();

    private boolean showTitle = true;

    private boolean showUnit = true;

    private boolean showValue = true;


    ObservableList<String> skins = FXCollections.observableArrayList();

    private JFXCheckBox jfxCheckBoxShowTitle;

    private JFXCheckBox jfxCheckBoxShowValue;

    private JFXCheckBox jfxCheckBoxShowUnit;

    private JFXCheckBox jfxCheckBoxInPercent;

    private JFXTextField minTextField;

    private JFXTextField maxTextField;

    private GaugeSectionTableView tableViewSections;

    private GridPane gridPane;

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }


    public GaugePojo(DashboardControl control) {
        this(control, null);
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public GaugePojo(DashboardControl control, JsonNode jsonNode) {

        //gauge.setSkinType(Gauge.SkinType.SIMPLE);
        skins.addAll(eu.hansolo.medusa.Gauge.SkinType.DASHBOARD.toString(), eu.hansolo.medusa.Gauge.SkinType.SIMPLE.toString());
        this.dashboardControl = control;

        if (jsonNode != null) {

            if (jsonNode.has("maximum")) {
                maximum = jsonNode.get("maximum").asDouble();
            }
            if (jsonNode.has("minimum")) {
                minimum = jsonNode.get("minimum").asDouble();
            }
            if (jsonNode.has("inPercent")) {
                inPercent = jsonNode.get("inPercent").asBoolean();
            }
            if (jsonNode.has("showTitle")) {
                showTitle = jsonNode.get("showTitle").asBoolean(true);
            }
            if (jsonNode.has("showUnit")) {
                showUnit = jsonNode.get("showUnit").asBoolean(true);
            }
            if (jsonNode.has("showValue")) {
                showValue = jsonNode.get("showValue").asBoolean(true);
            }

            for (int i = 0; i < jsonNode.get("sections").size(); i++) {
                double sectionEnd = jsonNode.get("sections").get(i).get("end").asDouble();
                double sectionStart = jsonNode.get("sections").get(i).get("start").asDouble();
                Color sectionColor = Color.valueOf(jsonNode.get("sections").get(i).get("color").asText());
                if (sections.size() > 0) {
                    sections.add(new GaugeSectionPojo(sectionEnd,sectionColor,sections.get(i-1).endProperty()));
                }else {
                    sections.add(new GaugeSectionPojo(sectionStart, sectionEnd, sectionColor));
                }



            }


        }


    }


    public List<GaugeSectionPojo> getSections() {
        return sections;
    }

//    public void setSections(List<GaugeSectionPojo> sections) {
//        this.sections = sections;
//    }


    public boolean isInPercent() {
        return inPercent;
    }

    public void setInPercent(boolean inPercent) {
        this.inPercent = inPercent;
    }

    @Override
    public String toString() {
        return "Gauge{" +
                "gaugeWidgetID=" + gaugeWidgetID +
                ", maximum=" + maximum +
                ", minimum=" + minimum +
                ", inPercent=" + inPercent +
                ", sections=" + sections +
                ", dashboardControl=" + dashboardControl +
                ", skins=" + skins +
                '}';
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isShowUnit() {
        return showUnit;
    }

    public void setShowUnit(boolean showUnit) {
        this.showUnit = showUnit;
    }

    public boolean isShowValue() {
        return showValue;
    }

    public void setShowValue(boolean showValue) {
        this.showValue = showValue;
    }

    public Tab getConfigTab() {


        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.gaugewidget.tab")
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


        createMaxMin();

//        tableViewSections = new GaugeSectionTableFactory();

        if (sections.size() > 0) {
            maxTextField.setDisable(true);
            minTextField.setDisable(true);
        }
        tableViewSections = new GaugeSectionTableView(sections);



        tableViewSections.getItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change change) {
                while (change.next()) {
                    System.out.println(change.getList());
                    if (change.getList().size() > 0) {
                        maxTextField.setText(String.valueOf(sections.stream().max(Comparator.comparing(GaugeSectionPojo::getEnd)).get().getEnd()));
                        minTextField.setText(String.valueOf(sections.stream().min(Comparator.comparing(GaugeSectionPojo::getStart)).get().getStart()));


                        maxTextField.setDisable(true);
                        minTextField.setDisable(true);

                    } else {
                        maxTextField.setDisable(false);
                        minTextField.setDisable(false);
                    }
                }
            }
        });



        //tableViewSections.getItems().addAll(sections);


        jfxButtonAdd.setOnAction(event -> {
            if (sections.size() > 0) {
                DoubleProperty doubleProperty = sections.get(sections.size() - 1).endProperty();
                sections.add(new GaugeSectionPojo(doubleProperty));
            }else {
                sections.add(new GaugeSectionPojo());
            }

        });

        jfxButtonDelete.setOnAction(event -> {
            tableViewSections.getItems().remove(tableViewSections.getSelectionModel().getSelectedItem());
        });

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 8, 5, 8));
        hBox.setSpacing(10);
        hBox.getChildren().addAll(jfxButtonAdd, jfxButtonDelete);
        gridPane.add(hBox, 0, 7, 3, 1);


        gridPane.add(tableViewSections, 0, 8, 3, 2);


        tab.setContent(gridPane);
        return tab;
    }

    private void createMaxMin() {


        jfxCheckBoxShowTitle = new JFXCheckBox();
        jfxCheckBoxShowTitle.setSelected(showTitle);
        jfxCheckBoxShowValue = new JFXCheckBox();
        jfxCheckBoxShowValue.setSelected(showValue);
        jfxCheckBoxShowUnit = new JFXCheckBox();
        jfxCheckBoxShowUnit.setSelected(showUnit);


        jfxCheckBoxInPercent = new JFXCheckBox();
        jfxCheckBoxInPercent.setSelected(inPercent);


        minTextField = new JFXTextField(String.valueOf(minimum));

        maxTextField = new JFXTextField(String.valueOf(maximum));

        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showTitle")), jfxCheckBoxShowTitle);
        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showUnit")), jfxCheckBoxShowUnit);
        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showValue")), jfxCheckBoxShowValue);
        gridPane.addRow(3, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.inPercent")), jfxCheckBoxInPercent);

        gridPane.addRow(4, new Label(I18n.getInstance().getString("plugin.dashboard.min")), minTextField);
        gridPane.addRow(5, new Label(I18n.getInstance().getString("plugin.dashboard.max")), maxTextField);
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 6, 3, 1);

    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();


        dataNode.put("minimum", minimum);
        dataNode.put("maximum", maximum);
        dataNode.put("inPercent", inPercent);
        dataNode.put("showTitle", showTitle);
        dataNode.put("showValue", showValue);
        dataNode.put("showUnit", showUnit);

        ArrayNode arrayNode = dataNode.putArray("sections");
        for (GaugeSectionPojo gaugeSection : sections) {
            ObjectNode dataNode1 = arrayNode.addObject();
            dataNode1.put("end", gaugeSection.getEnd());
            dataNode1.put("start", gaugeSection.getStart());
            dataNode1.put("color", gaugeSection.getColor().toString());

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

    private class GaugeDesignTab extends Tab implements ConfigTab {
        GaugePojo gaugeDesign;

        public GaugeDesignTab(String text, GaugePojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {
            try {
                showValue = jfxCheckBoxShowValue.isSelected();
                showUnit = jfxCheckBoxShowUnit.isSelected();
                showTitle = jfxCheckBoxShowTitle.isSelected();
                inPercent = jfxCheckBoxInPercent.isSelected();
                if (sections.size() > 0) {
                    minimum = sections.stream().min(Comparator.comparing(GaugeSectionPojo::getStart)).get().getStart();
                    maximum = sections.stream().max(Comparator.comparing(GaugeSectionPojo::getEnd)).get().getEnd();
                }else {
                    minimum = Double.parseDouble(minTextField.getText());
                    maximum = Double.parseDouble(maxTextField.getText());
                }
                tableViewSections.getItems().forEach(x -> logger.debug(x));
//                sectionsComitted.clear();
//                sectionsComitted.addAll(sections);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

}