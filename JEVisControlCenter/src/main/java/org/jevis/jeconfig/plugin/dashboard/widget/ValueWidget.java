package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.LimitColoring;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public class ValueWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(ValueWidget.class);
    public static String WIDGET_ID = "Value";
    private final Label label = new Label();
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private StringProperty labelText = new SimpleStringProperty("n.a.");
    private LimitColoring limitColoring;
    private Limit limit = new Limit();
    private Interval lastInterval = null;


    public static String LIMIT_NODE_NAME = "limit";

    public ValueWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    public ValueWidget(DashboardControl control) {
        super(control);
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle("new Value Widget");
        widgetPojo.setType(typeID());


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Value.Update: {}", interval);
        lastInterval = interval;
        showProgressIndicator(true);
        Platform.runLater(() -> {
            this.label.setText(I18n.getInstance().getString("plugin.dashboard.loading"));
        });


        if (this.sampleHandler == null) return;

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();

//        updateConfig();

        /*** TODO: replace null check if hasChanged is working **/
//            System.out.println("jnode: "+config.getConfigNode(LimitColoring.JNODE_NAME));
//        if (this.limitColoring == null && this.config.getConfigNode(LimitColoring.JNODE_NAME) != null) {
//            System.out.println("Create new LimitColoring: " + this.config.getConfigNode(LimitColoring.JNODE_NAME));
//            this.limitColoring = new LimitColoring(
//                    this.config.getConfigNode(LimitColoring.JNODE_NAME), this.config.getFontColor(), this.config.getBackgroundColor());
//        }


        this.nf.setMinimumFractionDigits(this.config.getDecimals());
        this.nf.setMaximumFractionDigits(this.config.getDecimals());


        this.labelText.setValue("n.a.");
        AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
        try {
            if (!this.sampleHandler.getDataModel().isEmpty()) {
                ChartDataModel dataModel = this.sampleHandler.getDataModel().get(0);
//            dataModel.setAbsolute(true);
                List<JEVisSample> results;

                String unit = dataModel.getUnitLabel();


                results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    total.set(DataModelDataHandler.getTotal(results));

                    this.labelText.setValue((this.nf.format(total.get())) + " " + unit);
//                    if (this.limitColoring != null) {
//                        System.out.println("limitColoring formate");
//                        this.limitColoring.formateLabel(this.label, total.get());
//                    }
                }

            } else {
                logger.warn("ValueWidget is missing SampleHandler.datamodel: {}", this.sampleHandler);
            }

        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }


//        limit.setHasLowerLimit(true);
//        limit.setHasUpperLimit(true);
//        limit.setLowerLimit(80000d);
//        limit.setUpperLimit(150000d);

        System.out.println("limit: " + limit);
        Platform.runLater(() -> {
            this.label.setText(this.labelText.getValue());
            Color fontColor = this.config.getFontColor();

            if (limit != null) {
                System.out.println("limit exists");
                if (limit.hasLowerLimit) {
                    System.out.println("has Lower Limit");
                    if (total.get() <= limit.getLowerLimit()) {
                        System.out.println("is Lower Limit");
                        fontColor = limit.getLowerLimitColor();
                    }
                }
                if (limit.hasUpperLimit) {
                    System.out.println("has Upper Limit");
                    if (total.get() >= limit.getUpperLimit()) {
                        System.out.println("is Upper Limit");
                        fontColor = limit.getUpperLimitColor();
                    }
                }
            }
            this.label.setTextFill(fontColor);


            showProgressIndicator(false);
        });


    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void openConfig() {

        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(null);
        widgetConfigDialog.addDataModel(this.sampleHandler);

        if (limit != null) {
            widgetConfigDialog.addTab(limit.getConfigTab());
        }

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.get() == ButtonType.OK) {
            updateData(lastInterval);
        }
    }


    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                this.label.setBackground(bgColor);
                this.label.setTextFill(this.config.getFontColor());

                this.label.setContentDisplay(ContentDisplay.CENTER);
            });
        });
    }


    @Override
    public void init() {
        logger.debug("init");
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        this.sampleHandler.setMultiSelect(false);

        try {
            this.limit = new Limit(this.config.getConfigNode(LIMIT_NODE_NAME));
        } catch (Exception ex) {
            this.limit = new Limit();
        }

        this.label.setPadding(new Insets(0, 8, 0, 8));
        setGraphic(this.label);
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ObjectNode toNode() {

        ObjectNode dashBoardNode = super.createDefaultNode();
        dashBoardNode
                .set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());


        if (limit != null) {
            dashBoardNode
                    .set(LIMIT_NODE_NAME, limit.toJSON());
        }


        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ValueWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    class Limit {


        Double lowerLimit = 0d;
        Double upperLimit = 0d;

        boolean hasLowerLimit = false;
        boolean hasUpperLimit = false;

        Color upperLimitColor = Color.RED;
        Color lowerLimitColor = Color.GREEN;

        public Limit() {
        }

        public Limit(JsonNode jsonNode) {
            hasLowerLimit = jsonNode.get("lowerLimit").asBoolean(false);
            hasUpperLimit = jsonNode.get("upperLimit").asBoolean(false);

            lowerLimit = jsonNode.get("lowerLimitValue").asDouble(0);
            upperLimit = jsonNode.get("upperLimitValue").asDouble(0);

            lowerLimitColor = Color.valueOf(jsonNode.get("lowerLimitColor").asText(Color.RED.toString()));
            upperLimitColor = Color.valueOf(jsonNode.get("upperLimitColor").asText(Color.GREEN.toString()));
        }

        public ObjectNode toJSON() {
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            dataNode.put("lowerLimit", isHasLowerLimit());
            dataNode.put("upperLimit", isHasUpperLimit());
            dataNode.put("lowerLimitValue", lowerLimit);
            dataNode.put("upperLimitValue", upperLimit);
            dataNode.put("lowerLimitColor", lowerLimitColor.toString());
            dataNode.put("upperLimitColor", upperLimitColor.toString());
            return dataNode;
        }

        public Tab getConfigTab() {
            CheckBox enableUpperBox = new CheckBox("Enable upper limit");
            CheckBox enableLowerBox = new CheckBox("Enable lower limit");
            enableLowerBox.setSelected(true);
            enableUpperBox.setSelected(true);
            Label upperVlabel = new Label("Upper Limit Value");
            Label lowerVlabel = new Label("Lower Limit Value");
            Label upperColorlabel = new Label("Upper Color");
            Label lowerColorlabel = new Label("Lower Color");
            TextField upperValueField = new TextField();
            TextField lowerValueField = new TextField();
            ColorPicker upperColorPicker = new ColorPicker();
            ColorPicker lowerColorPicker = new ColorPicker();
            Separator separator = new Separator(Orientation.HORIZONTAL);

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setVgap(8);
            gridPane.setHgap(8);
            gridPane.add(enableUpperBox, 0, 0, 2, 1);
            gridPane.add(upperVlabel, 0, 1);
            gridPane.add(upperValueField, 1, 1);
            gridPane.add(upperColorlabel, 0, 2);
            gridPane.add(upperColorPicker, 1, 2);

            gridPane.add(separator, 0, 3, 2, 1);

            gridPane.add(enableLowerBox, 0, 4, 2, 1);
            gridPane.add(lowerVlabel, 0, 5);
            gridPane.add(lowerValueField, 1, 5);
            gridPane.add(lowerColorlabel, 0, 6);
            gridPane.add(lowerColorPicker, 1, 6);

            Tab tab = new Tab("Limit");
            tab.setContent(gridPane);

            GridPane.setFillWidth(lowerColorPicker, true);


            enableUpperBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("enableUpperBox: " + newValue);
                upperValueField.setDisable(!newValue);
                upperColorPicker.setDisable(!newValue);
                hasUpperLimit = newValue;
            });

            enableLowerBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                lowerValueField.setDisable(!newValue);
                lowerColorPicker.setDisable(!newValue);
                hasLowerLimit = newValue;
            });


            Platform.runLater(() -> {
                enableUpperBox.setSelected(hasUpperLimit);
                enableLowerBox.setSelected(hasLowerLimit);
            });

            upperValueField.setText(upperLimit.toString());
            lowerValueField.setText(lowerLimit.toString());

            upperValueField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    upperLimit = Double.parseDouble(newValue.replaceAll(",", "."));
                }
            });

            lowerValueField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    lowerLimit = Double.parseDouble(newValue.replaceAll(",", "."));
                }
            });


            upperColorPicker.setValue(upperLimitColor);
            lowerColorPicker.setValue(lowerLimitColor);

            upperColorPicker.setOnAction(event -> {
                upperLimitColor = upperColorPicker.getValue();
            });

            lowerColorPicker.setOnAction(event -> {
                lowerLimitColor = lowerColorPicker.getValue();
            });

            return tab;

        }

        public Double getLowerLimit() {
            return lowerLimit;
        }

        public void setLowerLimit(Double lowerLimit) {
            this.lowerLimit = lowerLimit;
        }

        public Double getUpperLimit() {
            return upperLimit;
        }

        public void setUpperLimit(Double upperLimit) {
            this.upperLimit = upperLimit;
        }

        public boolean isHasLowerLimit() {
            return hasLowerLimit;
        }

        public void setHasLowerLimit(boolean hasLowerLimit) {
            this.hasLowerLimit = hasLowerLimit;
        }

        public boolean isHasUpperLimit() {
            return hasUpperLimit;
        }

        public void setHasUpperLimit(boolean hasUpperLimit) {
            this.hasUpperLimit = hasUpperLimit;
        }

        public Color getUpperLimitColor() {
            return upperLimitColor;
        }

        public void setUpperLimitColor(Color upperLimitColor) {
            this.upperLimitColor = upperLimitColor;
        }

        public Color getLowerLimitColor() {
            return lowerLimitColor;
        }

        public void setLowerLimitColor(Color lowerLimitColor) {
            this.lowerLimitColor = lowerLimitColor;
        }


        @Override
        public String toString() {
            return "Limit{" +
                    "lowerLimit=" + lowerLimit +
                    ", upperLimit=" + upperLimit +
                    ", hasLowerLimit=" + hasLowerLimit +
                    ", hasUpperLimit=" + hasUpperLimit +
                    ", upperLimitColor=" + upperLimitColor +
                    ", lowerLimitColor=" + lowerLimitColor +
                    '}';
        }
    }
}
