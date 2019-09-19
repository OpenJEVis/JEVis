package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
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
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ValueWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(ValueWidget.class);
    public static String WIDGET_ID = "Value";
    private final Label label = new Label();
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private StringProperty labelText = new SimpleStringProperty("n.a.");
    private DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private LimitColoring limitColoring;
    private Limit limit;
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
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Value.Update: {}", interval);
        lastInterval = interval;
        if (this.sampleHandler == null) {
            showProgressIndicator(false);
            return;
        }


        Platform.runLater(() -> {
            this.label.setText(I18n.getInstance().getString("plugin.dashboard.loading"));
        });

        AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
        try {
            this.sampleHandler.setInterval(interval);
            this.sampleHandler.update();

            this.nf.setMinimumFractionDigits(this.config.getDecimals());
            this.nf.setMaximumFractionDigits(this.config.getDecimals());


            this.labelText.setValue("n.a.");

            if (!this.sampleHandler.getDataModel().isEmpty()) {
                ChartDataModel dataModel = this.sampleHandler.getDataModel().get(0);
//            dataModel.setAbsolute(true);
                List<JEVisSample> results;

                String unit = dataModel.getUnitLabel();


                results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    total.set(DataModelDataHandler.getTotal(results));
                    displayedSample.setValue(total.get());
                    this.labelText.setValue((this.nf.format(total.get())) + " " + unit);
                    checkLimit();
                } else {
//                    showAlertOverview(true, "");
                }

            } else {
                logger.warn("ValueWidget is missing SampleHandler.datamodel: {}", this.sampleHandler);
            }

        } catch (Exception ex) {
            logger.error("Error while updating ValueWidget: {}:{}", getConfig().getUuid(), ex);
//            ex.printStackTrace();
        }


        Platform.runLater(() -> {
            showProgressIndicator(false);
        });


    }

    private void checkLimit() {
        Platform.runLater(() -> {
            try {
                this.label.setText(this.labelText.getValue());
                Color fontColor = this.config.getFontColor();

                if (limit != null) {
                    if (limit.hasLowerLimit) {
                        if (displayedSample.get() <= limit.getLowerLimit()) {
                            fontColor = limit.getLowerLimitColor();
                        }
                    }
                    if (limit.hasUpperLimit) {
                        if (displayedSample.get() >= limit.getUpperLimit()) {
                            fontColor = limit.getUpperLimitColor();
                        }
                    }
                }
                this.label.setTextFill(fontColor);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    @Override
    public void debug() {
        this.sampleHandler.debug();
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
            updateConfig();
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

        try {
            if (limit != null && limit.limitWidget > 0) {
                for (Widget sourceWidget : ValueWidget.this.control.getWidgets()) {
                    if (sourceWidget.getConfig().getUuid() == (limit.limitWidget)) {

                        ((ValueWidget) sourceWidget).getDisplayedSampleProperty().addListener((observable, oldValue, newValue) -> {
                            if (!Double.isNaN(newValue.doubleValue())) {
                                limit.setLowerLimit(newValue.doubleValue() * limit.getLowerLimitOffset());
                                limit.setUpperLimit(newValue.doubleValue() * limit.getUpperLimitOffset());
                                checkLimit();
                            }
                        });
                        break;
                    }

                }

            }
        } catch (Exception ex) {
            logger.error("Error while update config: {}|{}", ex.getStackTrace()[0].getLineNumber(), ex);
        }

    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public List<DateTime> getMaxTimeStamps() {
        if (sampleHandler != null) {
            return sampleHandler.getMaxTimeStamps();
        } else {
            return new ArrayList<>();
        }
    }


    @Override
    public void init() {
        logger.debug("init");
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        this.sampleHandler.setMultiSelect(false);

        try {
            this.limit = new Limit(this.control, this.config.getConfigNode(LIMIT_NODE_NAME));
        } catch (Exception ex) {
            this.limit = new Limit(this.control);
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


    public DoubleProperty getDisplayedSampleProperty() {
        return displayedSample;
    }

    class Limit {


        Double lowerLimit = 0d;
        Double upperLimit = 0d;
        Double lowerLimitOffset = 1d;
        Double upperLimitOffset = 1d;

        boolean hasLowerLimit = false;
        boolean hasUpperLimit = false;

        Color upperLimitColor = Color.RED;
        Color lowerLimitColor = Color.GREEN;


        int limitWidget;
        final DashboardControl dashboardControl;

        public Limit(DashboardControl control) {
            this.dashboardControl = control;
        }

        public Limit(DashboardControl control, JsonNode jsonNode) {
            this.dashboardControl = control;
            if (!jsonNode.get("source").asText("").isEmpty()) {
                try {
                    limitWidget = jsonNode.get("source").asInt(0);
                } catch (Exception ex) {
                    logger.error(ex);
                    limitWidget = -1;
                }
                //limitWidget = UUID.fromString(jsonNode.get("source").asText(""));
            }

            hasLowerLimit = jsonNode.get("lowerLimit").asBoolean(false);
            hasUpperLimit = jsonNode.get("upperLimit").asBoolean(false);

            lowerLimit = jsonNode.get("lowerLimitValue").asDouble(0);
            upperLimit = jsonNode.get("upperLimitValue").asDouble(0);

            lowerLimitColor = Color.valueOf(jsonNode.get("lowerLimitColor").asText(Color.RED.toString()));
            upperLimitColor = Color.valueOf(jsonNode.get("upperLimitColor").asText(Color.GREEN.toString()));
        }

        public int getLimitSource() {
            return limitWidget;
        }

        public Double getLowerLimitOffset() {
            return lowerLimitOffset;
        }

        public int getLimitWidget() {
            return limitWidget;
        }

        public void setLimitWidget(int limitWidget) {
            this.limitWidget = limitWidget;
        }

        public void setLowerLimitOffset(Double lowerLimitOffset) {
            this.lowerLimitOffset = lowerLimitOffset;
        }

        public Double getUpperLimitOffset() {
            return upperLimitOffset;
        }

        public void setUpperLimitOffset(Double upperLimitOffset) {
            this.upperLimitOffset = upperLimitOffset;
        }

        public ObjectNode toJSON() {
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            if (limitWidget > 0) {
                dataNode.put("source", isHasLowerLimit());
                dataNode.put("lowerLimitOffset", lowerLimitOffset);
                dataNode.put("upperLimitOffset", upperLimitOffset);
            } else {
                dataNode.put("lowerLimitValue", lowerLimit);
                dataNode.put("upperLimitValue", upperLimit);
            }

            dataNode.put("lowerLimit", isHasLowerLimit());
            dataNode.put("upperLimit", isHasUpperLimit());
            dataNode.put("lowerLimitColor", lowerLimitColor.toString());
            dataNode.put("upperLimitColor", upperLimitColor.toString());
            return dataNode;
        }


        public Tab getConfigTab() {
            /** dynamic alarm **/
            String staticString = "Static";
            String dynamicString = "Dynamic";

            ObservableList<String> types = FXCollections.observableArrayList(staticString, dynamicString);
            ComboBox<String> limitTypeBox = new ComboBox<>(types);

            Label limitTypeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type"));

            Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.tab"));


            limitTypeBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                GridPane gridPane = new GridPane();

                if (limitTypeBox.getSelectionModel().getSelectedItem().equals("Static")) {
                    limitWidget = -1;
                    updateSettingPane(gridPane, 1);
                } else {
                    updateSettingPane(gridPane, 0);
                }
                gridPane.add(limitTypeLabel, 0, 0);
                gridPane.add(limitTypeBox, 1, 0);
                gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 1, 2, 1);

                Platform.runLater(() -> {
//                        borderPane.setCenter(gridPane);
                    tab.setContent(gridPane);
                    tab.getContent().requestFocus();
                });
            });


            Platform.runLater(() -> {
//                System.out.println("Select type:" + limitWidget);

                if (limitWidget > 0) {//
//                    limitTypeBox.getSelectionModel().select(dynamicString);// dump workaround,
                    limitTypeBox.getSelectionModel().select(0);
                } else {
                    limitTypeBox.getSelectionModel().select(1);
                }
                limitTypeBox.fireEvent(new ActionEvent());
            });
            return tab;

        }

        public void updateSettingPane(GridPane gridPane, int mode) {
            gridPane.setPadding(new Insets(10));
            gridPane.setVgap(8);
            gridPane.setHgap(8);

            CheckBox enableUpperBox = new CheckBox(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.enable.upper"));
            CheckBox enableLowerBox = new CheckBox(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.enable.lower"));
            enableLowerBox.setSelected(true);
            enableUpperBox.setSelected(true);
            Label upperVlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.upper"));
            Label lowerVlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.lower"));

            Label upperVOffsetlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.upper"));
            Label lowerVOffsetlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.lower"));

            Label upperColorlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.color.upper"));
            Label lowerColorlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.color.lower"));
            TextField upperValueField = new TextField();
            TextField lowerValueField = new TextField();

            ColorPicker upperColorPicker = new ColorPicker();
            ColorPicker lowerColorPicker = new ColorPicker();

            enableUpperBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
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


            upperColorPicker.setValue(upperLimitColor);
            lowerColorPicker.setValue(lowerLimitColor);

            upperColorPicker.setOnAction(event -> {
                upperLimitColor = upperColorPicker.getValue();
            });

            lowerColorPicker.setOnAction(event -> {
                lowerLimitColor = lowerColorPicker.getValue();
            });

            GridPane.setFillWidth(lowerColorPicker, true);
            GridPane.setFillWidth(upperColorPicker, true);

            Separator separator = new Separator(Orientation.HORIZONTAL);

            Label sourceLable = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.source"));

            ComboBox<Widget> widgetBox = new ComboBox<>(
                    dashboardControl.getWidgetList()
                            .filtered(widget -> widget.typeID().equals(ValueWidget.WIDGET_ID)));

            Callback<ListView<Widget>, ListCell<Widget>> cellFactory = new Callback<javafx.scene.control.ListView<Widget>, ListCell<Widget>>() {
                @Override
                public ListCell<Widget> call(ListView<Widget> param) {
                    return new ListCell<Widget>() {
                        @Override
                        protected void updateItem(Widget item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null && !empty) {
                                try {
                                    ValueWidget widget = ((ValueWidget) item);
                                    String title = item.getConfig().getTitle().isEmpty()
                                            ? I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.list.nottitle")
                                            : item.getConfig().getTitle();

                                    setText(String.format("[%d] '%s' | %.2f",
                                            item.getConfig().getUuid(),
                                            title,
                                            widget.getDisplayedSampleProperty().getValue()));
                                } catch (Exception ex) {
                                    logger.error(ex);
                                    setText(item.toString());
                                }
                            }
                        }
                    };
                }
            };


            widgetBox.setButtonCell(cellFactory.call(null));
            widgetBox.setCellFactory(cellFactory);

            widgetBox.setOnAction(event -> {
                limitWidget = widgetBox.getSelectionModel().getSelectedItem().getConfig().getUuid();
            });

            int row = 3;

            if (mode == 1) {
                /** static mode **/
                upperValueField.setText(upperLimit.toString());
                lowerValueField.setText(lowerLimit.toString());

                upperValueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            upperLimit = Double.parseDouble(newValue.replaceAll(",", "."));
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                });

                lowerValueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            lowerLimit = Double.parseDouble(newValue.replaceAll(",", "."));
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                });

                gridPane.add(enableUpperBox, 0, row, 2, 1);

                gridPane.add(upperVlabel, 0, ++row);
                gridPane.add(upperValueField, 1, row);

                gridPane.add(upperColorlabel, 0, ++row);
                gridPane.add(upperColorPicker, 1, row);

                gridPane.add(separator, 0, ++row, 2, 1);

                gridPane.add(enableLowerBox, 0, ++row, 2, 1);

                gridPane.add(lowerVlabel, 0, ++row);
                gridPane.add(lowerValueField, 1, row);

                gridPane.add(lowerColorlabel, 0, ++row);
                gridPane.add(lowerColorPicker, 1, row);
            } else {
                /** dynamic mode **/

                upperValueField.setText(upperLimitOffset.toString());
                lowerValueField.setText(lowerLimitOffset.toString());

                upperValueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            upperLimitOffset = Double.parseDouble(newValue.replaceAll(",", "."));
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                });

                lowerValueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            lowerLimitOffset = Double.parseDouble(newValue.replaceAll(",", "."));
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                });
                //sourceLable
                gridPane.add(sourceLable, 0, row);
                gridPane.add(widgetBox, 1, row);

                gridPane.add(new Separator(Orientation.HORIZONTAL), 0, ++row, 2, 1);


                gridPane.add(enableUpperBox, 0, ++row, 2, 1);
                gridPane.add(upperVOffsetlabel, 0, ++row);
                gridPane.add(upperValueField, 1, row);
                gridPane.add(upperColorlabel, 0, ++row);
                gridPane.add(upperColorPicker, 1, row);

                gridPane.add(separator, 0, ++row, 2, 1);

                gridPane.add(enableLowerBox, 0, ++row, 2, 1);
                gridPane.add(lowerVOffsetlabel, 0, ++row);
                gridPane.add(lowerValueField, 1, row);
                gridPane.add(lowerColorlabel, 0, ++row);
                gridPane.add(lowerColorPicker, 1, row);


            }

            /** workaround so that the typeBox looses focus **/
            enableUpperBox.requestFocus();


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
                    ", lowerLimitOffset=" + lowerLimitOffset +
                    ", upperLimitOffset=" + upperLimitOffset +
                    ", hasLowerLimit=" + hasLowerLimit +
                    ", hasUpperLimit=" + hasUpperLimit +
                    ", upperLimitColor=" + upperLimitColor +
                    ", lowerLimitColor=" + lowerLimitColor +
                    ", limitWidget=" + limitWidget +
                    '}';
        }
    }
}
