package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.WidgetLegend;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clone of the JAVA 8 PIe Chart to fix the color every time an update is made, THis is most likely cured by the Animator.
 */
public class PieWidget extends Widget implements DataModelWidget {
    private static final Logger logger = LogManager.getLogger(PieWidget.class);
    public static String WIDGET_ID = "Pie";
    private final PieChart chart = new PieChart();
    private final NumberFormat nf = NumberFormat.getInstance();
    private final WidgetLegend legend = new WidgetLegend();
    private final ObjectMapper mapper = new ObjectMapper();
    private final BorderPane borderPane = new BorderPane();
    private final VBox legendPane = new VBox();
    //private Interval lastInterval;
    private Boolean customWorkday = true;

    public PieWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    @Override
    public void debug() {

    }

    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.piewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 12, control.getActiveDashboard().xGridInterval * 12));

        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Pie.Update: [{}] {}", getConfig().getUuid(), interval);

        if (sampleHandler == null) {
            return;
        } else {
            showProgressIndicator(true);
        }

//        this.sampleHandler.setAutoAggregation(true);

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        showAlertOverview(false, "");

        List<PieChart.Data> series = new ArrayList<>();
        List<Legend.LegendItem> legendItemList = new ArrayList<>();
        List<Color> colors = new ArrayList<>();

        /** data Update **/
        AtomicDouble total = new AtomicDouble(0);
        for (ChartDataRow dataModel : this.sampleHandler.getDataModel()) {
            try {
//                chartDataModel.setAbsolute(true);
                dataModel.setCustomWorkDay(customWorkday);
                Double dataModelTotal = DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), dataModel.getSamples(), dataModel);
                total.set(total.get() + dataModelTotal);
                logger.debug("dataModelTotal: [{}] {}", dataModel.getObject().getName(), dataModelTotal);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        logger.debug("Total.Total: {}", total.get());


        for (ChartDataRow chartDataRow : this.sampleHandler.getDataModel()) {
            try {
                double value = 0;
                boolean hasNoData = chartDataRow.getSamples().isEmpty();

                String textValue = "";

                if (!hasNoData) {
                    try {
                        value = DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), chartDataRow.getSamples(), chartDataRow);
                        logger.debug("part.total: [{}] {}", chartDataRow.getObject().getName(), value);
                        double proC = (value / total.get()) * 100;
                        if (Double.isInfinite(proC)) proC = 100;
                        if (Double.isNaN(proC)) proC = 0;


                        textValue = this.nf.format(value) + " " + UnitManager.getInstance().format(chartDataRow.getUnitLabel()) + "\n" + this.nf.format(proC) + "%";


                    } catch (Exception ex) {
                        logger.error(ex);
                        ex.printStackTrace();
                    }
                } else {
                    logger.debug("Empty Samples for: {}", this.config.getTitle());
                    value = 0;
                    textValue = "n.a.  " + UnitManager.getInstance().format(chartDataRow.getUnitLabel()) + "\n" + this.nf.format(0) + "%";
//                    showAlertOverview(true, "");
                }


                legendItemList.add(this.legend.buildVerticalLegendItem(
                        chartDataRow.getName(), chartDataRow.getColor(), this.config.getFontColor(), this.config.getFontSize(),
                        chartDataRow.getObject(), hasNoData, I18n.getInstance().getString("plugin.dashboard.alert.nodata"), true));

                if (!hasNoData) {
                    PieChart.Data pieData = new PieChart.Data(textValue, value);
                    series.add(pieData);
                    colors.add(chartDataRow.getColor());
                }


//                applyColors(colors);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

//        this.chart.setStyle(".chart-pie-label {\n" +
//                "  -fx-font-size: 5pt ;\n" +
//                "};");
//        this.chart.setStyle("-fx-start-angle=20;");


        /** redrawing **/
        Platform.runLater(() -> {
            try {
                this.chart.getData().clear();
                this.legend.getItems().setAll(legendItemList);
                this.chart.getData().setAll(series);
                applyColors(colors);
                this.chart.setStartAngle(180);
                this.chart.setLabelLineLength(15d);
                this.chart.setClockwise(false);
                this.chart.setLabelsVisible(true);
                this.chart.setLabelLineLength(18);
                this.chart.setLegendVisible(false);
                this.chart.requestLayout();

                updateConfig();
                showProgressIndicator(false);
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }

        });
    }

    @Override
    public void updateLayout() {


    }

    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
//            this.legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                this.setBackground(bgColorTrans);
//            this.chart.setBackground(bgColorTrans);
                this.legend.setBackground(bgColorTrans);
                this.borderPane.setBackground(bgColor);
                this.borderPane.setMaxWidth(this.config.getSize().getWidth());
                this.nf.setMinimumFractionDigits(this.config.getDecimals());
                this.nf.setMaximumFractionDigits(this.config.getDecimals());
                this.layout();
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
        return sampleHandler.getMaxTimeStamps();
    }


    @Override
    public void init() {

        try {
            this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), WIDGET_ID);
            this.sampleHandler.setMultiSelect(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /** we have to disable animation or the color will be wrong ever second update**/
        chart.setAnimated(false);


        /** Dummy data to render pie**/
//        ObservableList<ColorPieChart.Data> series = FXCollections.observableArrayList();
//        series.add(new ColorPieChart.Data("A", 1));
//        series.add(new ColorPieChart.Data("B", 1));
//        series.add(new ColorPieChart.Data("C", 1));
//        series.add(new ColorPieChart.Data("D", 1));
//        this.chart.setData(series);

        this.legendPane.setPadding(new Insets(10, 5, 5, 0));

        this.legend.setMaxWidth(100);
        this.legend.setPrefWidth(100);
        this.legend.setPrefHeight(10);


        Platform.runLater(() -> {
            try {
                this.legendPane.getChildren().setAll(this.legend);
                this.borderPane.setCenter(this.chart);
                this.borderPane.setRight(this.legendPane);
                setGraphic(this.borderPane);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });


    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);
        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public ObjectNode toNode() {

        ObjectNode dashBoardNode = super.createDefaultNode();
        dashBoardNode
                .set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());
        return dashBoardNode;
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

    public void applyColors(List<Color> colors) {

        for (int i = 0; i < colors.size(); i++) {

            Color currentColor = colors.get(i);
            String hexColor = ColorHelper.toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = this.chart.lookup(preIdent + ".chart-pie");
            node.setStyle("-fx-pie-color: " + hexColor + ";");

//            System.out.println(preIdent + ".chart-pie " + "-fx-pie-color: " + hexColor + ";" + " color: " + currentColor.toString());
        }
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

//    public class PieChart2 extends PieChart {
//
//        @Override
//        protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
////            super.layoutChartChildren(top, left, contentWidth, contentHeight);
////            @Override protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
//                centerX = contentWidth/2 + left;
//                centerY = contentHeight/2 + top;
//                double total = 0.0;
//                for (Data item = begin; item != null; item = item.next) {
//                    total+= Math.abs(item.getCurrentPieValue());
//                }
//                double scale = (total != 0) ? 360 / total : 0;
//
//                labelLinePath.getElements().clear();
//                // calculate combined bounds of all labels & pie radius
//                double[] labelsX = null;
//                double[] labelsY = null;
//                double[] labelAngles = null;
//                double labelScale = 1;
//                ArrayList<LabelLayoutInfo> fullPie = null;
//                boolean shouldShowLabels = getLabelsVisible();
//                if(getLabelsVisible()) {
//
//                    double xPad = 0d;
//                    double yPad = 0d;
//
//                    labelsX = new double[getDataSize()];
//                    labelsY = new double[getDataSize()];
//                    labelAngles = new double[getDataSize()];
//                    fullPie = new ArrayList<LabelLayoutInfo>();
//                    int index = 0;
//                    double start = getStartAngle();
//                    for (Data item = begin; item != null; item = item.next) {
//                        // remove any scale on the text node
//                        item.textNode.getTransforms().clear();
//
//                        double size = (isClockwise()) ? (-scale * Math.abs(item.getCurrentPieValue())) : (scale * Math.abs(item.getCurrentPieValue()));
//                        labelAngles[index] = normalizeAngle(start + (size / 2));
//                        final double sproutX = calcX(labelAngles[index], getLabelLineLength(), 0);
//                        final double sproutY = calcY(labelAngles[index], getLabelLineLength(), 0);
//                        labelsX[index] = sproutX;
//                        labelsY[index] = sproutY;
//                        xPad = Math.max(xPad, 2 * (item.textNode.getLayoutBounds().getWidth() + LABEL_TICK_GAP + Math.abs(sproutX)));
//                        if (sproutY > 0) { // on bottom
//                            yPad = Math.max(yPad, 2 * Math.abs(sproutY+item.textNode.getLayoutBounds().getMaxY()));
//                        } else { // on top
//                            yPad = Math.max(yPad, 2 * Math.abs(sproutY + item.textNode.getLayoutBounds().getMinY()));
//                        }
//                        start+= size;
//                        index++;
//                    }
//                    pieRadius = Math.min(contentWidth - xPad, contentHeight - yPad) / 2;
//                    // check if this makes the pie too small
//                    if (pieRadius < MIN_PIE_RADIUS ) {
//                        // calculate scale for text to fit labels in
//                        final double roomX = contentWidth-MIN_PIE_RADIUS-MIN_PIE_RADIUS;
//                        final double roomY = contentHeight-MIN_PIE_RADIUS-MIN_PIE_RADIUS;
//                        labelScale = Math.min(
//                                roomX/xPad,
//                                roomY/yPad
//                        );
//                        // hide labels if pie radius is less than minimum
//                        if ((begin == null && labelScale < 0.7) || ((begin.textNode.getFont().getSize()*labelScale) < 9)) {
//                            shouldShowLabels = false;
//                            labelScale = 1;
//                        } else {
//                            // set pieRadius to minimum
//                            pieRadius = MIN_PIE_RADIUS;
//                            // apply scale to all label positions
//                            for(int i=0; i< labelsX.length; i++) {
//                                labelsX[i] =  labelsX[i] * labelScale;
//                                labelsY[i] =  labelsY[i] * labelScale;
//                            }
//                        }
//                    }
//                }
//
//                if(!shouldShowLabels) {
//                    pieRadius = Math.min(contentWidth,contentHeight) / 2;
//                }
//
//                if (getChartChildren().size() > 0) {
//                    int index = 0;
//                    for (Data item = begin; item != null; item = item.next) {
//                        // layout labels for pie slice
//                        item.textNode.setVisible(shouldShowLabels);
//                        if (shouldShowLabels) {
//                            double size = (isClockwise()) ? (-scale * Math.abs(item.getCurrentPieValue())) : (scale * Math.abs(item.getCurrentPieValue()));
//                            final boolean isLeftSide = !(labelAngles[index] > -90 && labelAngles[index] < 90);
//
//                            double sliceCenterEdgeX = calcX(labelAngles[index], pieRadius, centerX);
//                            double sliceCenterEdgeY = calcY(labelAngles[index], pieRadius, centerY);
//                            double xval = isLeftSide ?
//                                    (labelsX[index] + sliceCenterEdgeX - item.textNode.getLayoutBounds().getMaxX() - LABEL_TICK_GAP) :
//                                    (labelsX[index] + sliceCenterEdgeX - item.textNode.getLayoutBounds().getMinX() + LABEL_TICK_GAP);
//                            double yval = labelsY[index] + sliceCenterEdgeY - (item.textNode.getLayoutBounds().getMinY()/2) -2;
//
//                            // do the line (Path)for labels
//                            double lineEndX = sliceCenterEdgeX +labelsX[index];
//                            double lineEndY = sliceCenterEdgeY +labelsY[index];
//                            LabelLayoutInfo info = new LabelLayoutInfo(sliceCenterEdgeX,
//                                    sliceCenterEdgeY,lineEndX, lineEndY, xval, yval, item.textNode, Math.abs(size));
//                            fullPie.add(info);
//
//                            // set label scales
//                            if (labelScale < 1) {
//                                item.textNode.getTransforms().add(
//                                        new Scale(
//                                                labelScale, labelScale,
//                                                isLeftSide ? item.textNode.getLayoutBounds().getWidth() : 0,
////                                    0,
//                                                0
//                                        )
//                                );
//                            }
//                        }
//                        index++;
//                    }
//
//                    // Check for collision and resolve by hiding the label of the smaller pie slice
//                    resolveCollision(fullPie);
//
//                    // update/draw pie slices
//                    double sAngle = getStartAngle();
//                    for (Data item = begin; item != null; item = item.next) {
//                        Node node = item.getNode();
//                        Arc arc = null;
//                        if (node != null) {
//                            if (node instanceof Region) {
//                                Region arcRegion = (Region)node;
//                                if( arcRegion.getShape() == null) {
//                                    arc = new Arc();
//                                    arcRegion.setShape(arc);
//                                } else {
//                                    arc = (Arc)arcRegion.getShape();
//                                }
//                                arcRegion.setShape(null);
//                                arcRegion.setShape(arc);
//                                arcRegion.setScaleShape(false);
//                                arcRegion.setCenterShape(false);
//                                arcRegion.setCacheShape(false);
//                            }
//                        }
//                        double size = (isClockwise()) ? (-scale * Math.abs(item.getCurrentPieValue())) : (scale * Math.abs(item.getCurrentPieValue()));
//                        // update slice arc size
//                        arc.setStartAngle(sAngle);
//                        arc.setLength(size);
//                        arc.setType(ArcType.ROUND);
//                        arc.setRadiusX(pieRadius * item.getRadiusMultiplier());
//                        arc.setRadiusY(pieRadius * item.getRadiusMultiplier());
//                        node.setLayoutX(centerX);
//                        node.setLayoutY(centerY);
//                        sAngle += size;
//                    }
//                    // finally draw the text and line
//                    if (fullPie != null) {
//                        for (LabelLayoutInfo info : fullPie) {
//                            if (info.text.isVisible()) drawLabelLinePath(info);
//                        }
//                    }
//                }
//            }
//
//
//        }
//    }

}
