package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ShapeWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ShapeWidget.class);
    public static String WIDGET_ID = "Shape";
    public static String SHAPE_NODE_NAME = "shape";
    public static String PERCENT_NODE_NAME = "percent";
    public static String SHAPE_DESIGN_NODE_NAME = "shapeDesign";
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty displayedUnit = new SimpleStringProperty("");
    private final ChangeListener<Number> limitListener = null;
    private final ChangeListener<Number> percentListener = null;
    private final ShapeWidget limitWidget = null;
    private final ShapeWidget percentWidget = null;
    private final String percentText = "";
    private final AnchorPane anchorPane = new AnchorPane();
    private final StackPane stackPane = new StackPane();
    private ShapePojo shapeConfig;
    private double blue = 1;
    private double green = 0;
    private double red = 0;
    private Interval lastInterval = null;
    private Boolean customWorkday = true;

    private double borderWidth = 0;


 ;


    public ShapeWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.titlewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 5));
        widgetPojo.setShowShadow(true);
        widgetPojo.setBackgroundColor(Color.TRANSPARENT);
        widgetPojo.setFontColorSecondary(Color.BLACK);
        widgetPojo.setBorderSize(BorderWidths.EMPTY);

        return widgetPojo;

    }

    @Override
    public void updateData(Interval interval) {

        logger.debug("Value.updateData: {} {}", this.getConfig().getTitle(), interval);
        lastInterval = interval;

        logger.debug(interval);
        Platform.runLater(() -> {
            showAlertOverview(false, "");
        });

        if (sampleHandler == null) {
            return;
        } else {
            showProgressIndicator(true);
        }


        Platform.runLater(() -> {

            String widgetUUID = "-1";
            AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
            widgetUUID = getConfig().getUuid() + "";

            this.sampleHandler.setAutoAggregation(true);

            this.sampleHandler.setInterval(interval);
            setIntervallForLastValue(interval);
            this.sampleHandler.update();
            if (!this.sampleHandler.getDataModel().isEmpty()) {

                ChartDataRow dataModel = this.sampleHandler.getDataModel().get(0);
                dataModel.setCustomWorkDay(customWorkday);
                List<JEVisSample> results;

                results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    total.set(DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), results, dataModel));
                    try {
                        System.out.println(this.sampleHandler.getJeVisDataSource().getObjects().get(0).getName());
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                    calculateColors(total.get());


                } else {
                    showAlertOverview(true,I18n.getInstance().getString("plugin.dashboard.alert.nodata"));
                    red = 0.5;
                    blue = 0.5;
                    green = 0.5;
                }

            }
        });


        updateShape();


    }

    private double calcColor(double minColor, double maxColor, double valueRange, double value, double diffColor) {
        double newColor;
        if (minColor < maxColor) {
            newColor = minColor + ((value / valueRange) * diffColor);
        } else {
            newColor = minColor - ((value / valueRange) * diffColor);

        }
        if (newColor > 1) {
            newColor = 1;
        } else if (newColor < 0) {
            newColor = 0;
        }
        return newColor;
    }

    private void calculateColors(double value) {

        double valueRange = (shapeConfig.getMaxValue() - (shapeConfig.getMinValue()));

        double diffBlue = Math.abs(shapeConfig.getMaxColor().getBlue() - shapeConfig.getMinColor().getBlue());
        double diffRed = Math.abs(shapeConfig.getMaxColor().getRed() - shapeConfig.getMinColor().getRed());
        double diffGreen = Math.abs(shapeConfig.getMaxColor().getGreen() - shapeConfig.getMinColor().getGreen());


        blue = calcColor(shapeConfig.getMinColor().getBlue(), shapeConfig.getMaxColor().getBlue(), valueRange, value - shapeConfig.getMinValue(), diffBlue);
        red = calcColor(shapeConfig.getMinColor().getRed(), shapeConfig.getMaxColor().getRed(), valueRange, value - shapeConfig.getMinValue(), diffRed);
        green = calcColor(shapeConfig.getMinColor().getGreen(), shapeConfig.getMaxColor().getGreen(), valueRange, value - shapeConfig.getMinValue(), diffGreen);
    }

    private void setIntervallForLastValue(Interval interval) {
        if (this.getDataHandler().getTimeFrameFactory() != null) {
            if (!this.getControl().getAllTimeFrames().getAll().contains(this.getDataHandler().getTimeFrameFactory()) && sampleHandler != null) {
                sampleHandler.durationProperty().setValue(this.sampleHandler.getDashboardControl().getInterval());
                sampleHandler.update();
                if (this.sampleHandler.getDataModel().get(0).getSamples().size() > 0) {
                    Interval interval1 = null;
                    try {
                        interval1 = new Interval(this.sampleHandler.getDataModel().get(0).getSamples().get(this.sampleHandler.getDataModel().get(0).getSamples().size() - 1).getTimestamp().minusMinutes(1), this.sampleHandler.getDataModel().get(0).getSamples().get(this.sampleHandler.getDataModel().get(0).getSamples().size() - 1).getTimestamp());
                        sampleHandler.durationProperty().setValue(interval1);
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }


            } else {
                this.sampleHandler.setInterval(interval);
            }
        } else {
            this.sampleHandler.setInterval(interval);
        }

    }

    private void updateText() {

    }

    @Override
    public DataModelDataHandler getDataHandler() {
        return this.sampleHandler;
    }

    @Override
    public void setDataHandler(DataModelDataHandler dataHandler) {
        this.sampleHandler = dataHandler;
    }

    @Override
    public void setCustomWorkday(Boolean customWorkday) {
        this.customWorkday = customWorkday;
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
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);
        sampleHandler.setAutoAggregation(true);

        logger.debug("Value.openConfig() [{}] limit ={}", config.getUuid(), shapeConfig);
        if (shapeConfig != null) {
            widgetConfigDialog.addTab(shapeConfig.getConfigTab());
        }

        widgetConfigDialog.requestFirstTabFocus();

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.debug("OK Pressed {}", this);
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public void updateConfig() {
        logger.debug("UpdateConfig");
        Platform.runLater(() -> {
            try {

                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                anchorPane.setBackground(bgColor);
                updateShape();
                /**
                 this.label.setBackground(bgColor);
                 this.label.setTextFill(this.config.getFontColor());
                 this.label.setText(this.config.getTitle());
                 this.label.setFont(new Font(this.config.getFontSize()));
                 this.label.setPrefWidth(this.config.getSize().getWidth());
                 this.label.setAlignment(this.config.getTitlePosition());

                 anchorPane.getChildren().setAll(drawArrowPath(0, 20, 50, 20, 5));
                 **/
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

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

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), this.getId());
        this.sampleHandler.setMultiSelect(false);


        anchorPane.setBackground(null);
        //stackPane.getChildren().add(anchorPane);
        setGraphic(anchorPane);



        try {
            logger.debug(this.config.getConfigNode(SHAPE_DESIGN_NODE_NAME));
            this.shapeConfig = new ShapePojo(this.control, this.config.getConfigNode(SHAPE_DESIGN_NODE_NAME));
            logger.debug(shapeConfig);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (shapeConfig == null) {
            logger.error("Limit is null make new: " + config.getUuid());
            this.shapeConfig = new ShapePojo(this.control);
        }
        anchorPane.heightProperty().addListener(observable -> updateConfig());
        anchorPane.widthProperty().addListener(observable -> updateConfig());


    }

    private Pane draw(double parentWidth, double parentHeight, SHAPE shape) {
        double xStart = 0;
        double yStart = 0;
        double xWidth = parentWidth + 1;//-1 to remove gap
        double yHeight = parentHeight + 1;


        Rectangle rectangle = new Rectangle(xStart, yStart, xWidth, yHeight);
        rectangle.setFill(new Color(red, green, blue, 1));
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(this.config.getBorderSize().getBottom());
        rectangle.setOpacity(this.config.getBackgroundColor().getOpacity());

        Ellipse ellipse = new Ellipse(parentWidth / 2, parentHeight / 2, parentWidth / 2, parentHeight / 2);
        ellipse.setFill(new Color(red, green, blue, 1));
        ellipse.setStroke(Color.BLACK);
        ellipse.setStrokeWidth(this.config.getBorderSize().getBottom());
        ellipse.setOpacity(this.config.getBackgroundColor().getOpacity());
        Pane arrow = new Pane();


        switch (shape) {
            case RECTANGLE:
                arrow.getChildren().addAll(rectangle);
                break;
            case ELLIPSE:
                arrow.getChildren().add(ellipse);
                break;
        }


        return arrow;
    }

    private void updateShape() {
        Platform.runLater(() -> {
            if (shapeConfig != null) {
                anchorPane.getChildren().setAll(draw(anchorPane.getWidth(), anchorPane.getHeight(), shapeConfig.getShape()));
                setBorder(null);
            }
        });

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


        if (shapeConfig != null) {
            dashBoardNode
                    .set(SHAPE_DESIGN_NODE_NAME, shapeConfig.toJSON());
        }
//        }


        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("fontawesome-free-6.1.2-desktop/svgs/solid/address-book.svg", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

    public DoubleProperty getDisplayedSampleProperty() {
        return displayedSample;
    }


    public enum SHAPE {
        ELLIPSE,
        RECTANGLE,
    }


}
