package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXCheckBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class LinearGaugePojo {


    private static final Logger logger = LogManager.getLogger(LinearGaugePojo.class);
    final DashboardControl dashboardControl;
    int gaugeWidgetID = -1;
    ObservableList<String> skins = FXCollections.observableArrayList();
    private double maximum = 0;
    private double minimum = 0;
    private boolean inPercent = false;
    private Color colorValueIndicator = Color.DARKBLUE;
    private Color colorBorder = Color.BLACK;
    private boolean showTitle = true;
    private boolean showUnit = true;
    private boolean showValue = true;
    private boolean showMajorTick = true;
    private boolean showMediumTick = true;
    private boolean showMinorTick = true;

    public double getMajorTickStep() {
        return majorTickStep;
    }

    public void setMajorTickStep(double majorTickStep) {
        this.majorTickStep = majorTickStep;
    }

    private double majorTickStep = 5.0;
    private JFXCheckBox jfxCheckBoxShowTitle;

    private JFXCheckBox jfxCheckBoxShowValue;

    private JFXCheckBox jfxCheckBoxShowUnit;

    private JFXCheckBox jfxCheckBoxInPercent;

    private JFXCheckBox jfxCheckBoxShowMajorTick;

    private JFXCheckBox jfxCheckBoxShowMediumTick;

    private JFXCheckBox jfxCheckBoxShowMinorTick;

    private MFXTextField MFXTextFieldMaxValue;

    private MFXTextField MFXTextFieldMinValue;
    private MFXTextField MFXTextFieldMajorTickStep;

    private ColorPickerAdv colorPickerAdvBoarderColor;

    private ColorPickerAdv colorPickerAdvValueIndicator;


    public LinearGaugePojo(DashboardControl control) {
        this(control, null);
    }

    public LinearGaugePojo(DashboardControl control, JsonNode jsonNode) {
        //skins.addAll(eu.hansolo.medusa.Gauge.SkinType.DASHBOARD.toString(), eu.hansolo.medusa.Gauge.SkinType.SIMPLE.toString());
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

                showTitle = jsonNode.get("showTitle").asBoolean();
            }
            if (jsonNode.has("showUnit")) {

                showUnit = jsonNode.get("showUnit").asBoolean();
            }
            if (jsonNode.has("showValue")) {

                showValue = jsonNode.get("showValue").asBoolean();
            }
            if (jsonNode.has("color")) {

                colorValueIndicator = Color.valueOf(jsonNode.get("color").asText());
            }
            if (jsonNode.has("showMajorTick")) {

                showMajorTick = jsonNode.get("showMajorTick").asBoolean();
            }
            if (jsonNode.has("showMediumTick")) {

                showMediumTick = jsonNode.get("showMediumTick").asBoolean();

            }
            if (jsonNode.has("showMinorTick")) {
                showMinorTick = jsonNode.get("showMinorTick").asBoolean();

            }
            if (jsonNode.has("colorBorder")) {

                colorBorder = Color.valueOf(jsonNode.get("colorBorder").asText());
            }
            if (jsonNode.has("majorTickStep")) {
                majorTickStep = jsonNode.get("majorTickStep").asDouble(5.0);
            }




        }


    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public boolean isInPercent() {
        return inPercent;
    }

    public void setInPercent(boolean inPercent) {
        this.inPercent = inPercent;
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

    public Color getColorValueIndicator() {
        return colorValueIndicator;
    }

    public void setColorValueIndicator(Color colorValueIndicator) {
        this.colorValueIndicator = colorValueIndicator;
    }

    @Override
    public String toString() {
        return "LinearGaugePojo{" +
                "gaugeWidgetID=" + gaugeWidgetID +
                ", maximum=" + maximum +
                ", minimum=" + minimum +
                ", inPercent=" + inPercent +
                ", color=" + colorValueIndicator +
                ", dashboardControl=" + dashboardControl +
                ", showTitle=" + showTitle +
                ", showUnit=" + showUnit +
                ", showValue=" + showValue +
                ", skins=" + skins +
                '}';
    }

    public boolean isShowMajorTick() {
        return showMajorTick;
    }

    public void setShowMajorTick(boolean showMajorTick) {
        this.showMajorTick = showMajorTick;
    }

    public boolean isShowMediumTick() {
        return showMediumTick;
    }

    public void setShowMediumTick(boolean showMediumTick) {
        this.showMediumTick = showMediumTick;
    }

    public boolean isShowMinorTick() {
        return showMinorTick;
    }

    public void setShowMinorTick(boolean showMinorTick) {
        this.showMinorTick = showMinorTick;
    }

    public Color getColorBorder() {
        return colorBorder;
    }

    public void setColorBorder(Color colorBorder) {
        this.colorBorder = colorBorder;
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.gaugewidget.tab")
                , this);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 5, 8, 5));

        int  i =0;

        jfxCheckBoxShowTitle = new JFXCheckBox();
        jfxCheckBoxShowTitle.setSelected(showTitle);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showTitle")), jfxCheckBoxShowTitle);
        i++;
        jfxCheckBoxShowUnit = new JFXCheckBox();
        jfxCheckBoxShowUnit.setSelected(showUnit);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showUnit")), jfxCheckBoxShowUnit);
        i++;
        jfxCheckBoxShowValue = new JFXCheckBox();
        jfxCheckBoxShowValue.setSelected(showValue);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showValue")), jfxCheckBoxShowValue);
        i++;
        jfxCheckBoxInPercent = new JFXCheckBox();
        jfxCheckBoxInPercent.setSelected(inPercent);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.inPercent")), jfxCheckBoxInPercent);
        i++;
        MFXTextFieldMinValue = new MFXTextField();
        MFXTextFieldMinValue.setText(String.valueOf(minimum));

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.min")), MFXTextFieldMinValue);
        i++;
        MFXTextFieldMaxValue = new MFXTextField();
        MFXTextFieldMaxValue.setText(String.valueOf(maximum));

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.max")), MFXTextFieldMaxValue);
        i++;

        MFXTextFieldMajorTickStep = new MFXTextField();
        MFXTextFieldMajorTickStep.setText(String.valueOf(majorTickStep));
        if (showMajorTick) {
            gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.majortickstep")), MFXTextFieldMajorTickStep);
            i++;
        }


        colorPickerAdvValueIndicator = new ColorPickerAdv();
        colorPickerAdvValueIndicator.setValue(colorValueIndicator);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.valueIndicatorColor")), colorPickerAdvValueIndicator);
        i++;
        jfxCheckBoxShowMajorTick = new JFXCheckBox();
        jfxCheckBoxShowMajorTick.setSelected(showMajorTick);

        jfxCheckBoxShowMajorTick.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                moveplusNodesGridpane(6, gridPane);
                gridPane.addRow(6, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.majortickstep")), MFXTextFieldMajorTickStep);

            }else {
                gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == GridPane.getRowIndex(MFXTextFieldMajorTickStep));
                moveminusNodesGridpane(6, gridPane);
            }
        });


        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.showMajorTick")), jfxCheckBoxShowMajorTick);
        i++;

        jfxCheckBoxShowMediumTick = new JFXCheckBox();
        jfxCheckBoxShowMediumTick.setSelected(showMediumTick);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.showMediumTick")), jfxCheckBoxShowMediumTick);
        i++;
        jfxCheckBoxShowMinorTick = new JFXCheckBox();
        jfxCheckBoxShowMinorTick.setSelected(showMinorTick);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.showMinorTick")), jfxCheckBoxShowMinorTick);
        i++;

        colorPickerAdvBoarderColor = new ColorPickerAdv();
        colorPickerAdvBoarderColor.setValue(colorBorder);

        gridPane.addRow(i, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.borderColor")), colorPickerAdvBoarderColor);

        tab.setContent(gridPane);
        return tab;
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
    public void moveminusNodesGridpane(int targetRowIndex, GridPane gridPane) {
        gridPane.getChildren().forEach(node -> {
            final int rowIndex = GridPane.getRowIndex(node);
            if (targetRowIndex <= rowIndex) {
                GridPane.setRowIndex(node, rowIndex - 1);
            }
        });
    }

    public void moveplusNodesGridpane(int targetRowIndex, GridPane gridPane) {
        gridPane.getChildren().forEach(node -> {
            final int rowIndex = GridPane.getRowIndex(node);
            if (targetRowIndex <= rowIndex) {
                GridPane.setRowIndex(node, rowIndex + 1);
            }
        });
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();


        dataNode.put("minimum", minimum);
        dataNode.put("maximum", maximum);
        dataNode.put("inPercent", inPercent);
        dataNode.put("showTitle", showTitle);
        dataNode.put("showValue", showValue);
        dataNode.put("showUnit", showUnit);
        dataNode.put("color", colorValueIndicator.toString());
        dataNode.put("showMajorTick", showMajorTick);
        dataNode.put("showMediumTick", showMediumTick);
        dataNode.put("showMinorTick", showMinorTick);
        dataNode.put("colorBorder", colorBorder.toString());
        dataNode.put("majorTickStep", majorTickStep);
        logger.debug(dataNode);
        return dataNode;
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        LinearGaugePojo gaugeDesign;

        public GaugeDesignTab(String text, LinearGaugePojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {

            try {
                showTitle = jfxCheckBoxShowTitle.isSelected();
                showUnit = jfxCheckBoxShowUnit.isSelected();
                showValue = jfxCheckBoxShowValue.isSelected();
                inPercent = jfxCheckBoxInPercent.isSelected();

                minimum = Double.valueOf(MFXTextFieldMinValue.getText());
                maximum = Double.valueOf(MFXTextFieldMaxValue.getText());

                majorTickStep = Double.valueOf(MFXTextFieldMajorTickStep.getText());

                showMajorTick = jfxCheckBoxShowMajorTick.isSelected();
                showMediumTick = jfxCheckBoxShowMediumTick.isSelected();
                showMinorTick = jfxCheckBoxShowMinorTick.isSelected();

                colorBorder = colorPickerAdvBoarderColor.getValue();
                colorValueIndicator = colorPickerAdvValueIndicator.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }

}