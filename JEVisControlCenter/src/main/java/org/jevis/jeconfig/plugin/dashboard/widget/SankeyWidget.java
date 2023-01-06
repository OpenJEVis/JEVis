package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import eu.hansolo.fx.charts.SankeyPlot;
import eu.hansolo.fx.charts.SankeyPlotBuilder;
import eu.hansolo.fx.charts.data.PlotItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.WidgetLegend;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

public class SankeyWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(SankeyWidget.class);
    public static String WIDGET_ID = "Sankey";

    public static String Sankey_DESIGN_NODE_NAME = "Sankey";
    private final NumberFormat nf = NumberFormat.getInstance();
    private SankeyPlot sankeyPlot;
    //private DataModelDataHandler sampleHandler;
    private final WidgetLegend legend = new WidgetLegend();
    private final GridPane borderPane = new GridPane();
    private Interval lastInterval = null;
    private final BorderPane bottomBorderPane = new BorderPane();
    private List<JEVisPlotItem> plotItems = new ArrayList<>();
    private Boolean customWorkday = true;

    private SankeyPojo sankeyPojo;


    public SankeyWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
    }

    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle("new Chart Widget");
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 12, control.getActiveDashboard().xGridInterval * 20));

        return widgetPojo;
    }

    @Override
    public void updateData(Interval interval) {
        logger.error("Sankey.Update: {}", interval);
        this.lastInterval = interval;

        if (sampleHandler == null) {
            showProgressIndicator(false);
            return;
        } else {
            showProgressIndicator(true);
        }

        showProgressIndicator(true);
        showAlertOverview(false, "");

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        try {

            updateChart();
            Platform.runLater(() -> {
                borderPane.setPadding(new Insets(10));

                Size configSize = getConfig().getSize();
                buildSankeyPlot(configSize);
                updateConfig();
            });
            /** workaround because we make a new chart every time**/
        } catch (Exception ex) {
            logger.error(ex);
        }
        showProgressIndicator(false);
    }

    private void buildSankeyPlot(Size configSize) {
        SankeyPlot.StreamFillMode streamFillMode = null;
        if (sankeyPojo.isColorGradient()) {
            streamFillMode = SankeyPlot.StreamFillMode.GRADIENT;
        } else {
            streamFillMode = SankeyPlot.StreamFillMode.COLOR;
        }


        this.sankeyPlot = SankeyPlotBuilder.create()
                .items(plotItems)
                .showFlowDirection(sankeyPojo.isShowFlow())
                .autoItemWidth(false)
                .autoItemGap(false)
                .minSize(configSize.getWidth() - 10, configSize.getHeight() - 10)
                .streamFillMode(streamFillMode)
                .build();
        sankeyPlot.setManaged(true);
        //todo


        sankeyPlot.setPadding(new Insets(0, 10, 10, 10));
        this.borderPane.getChildren().setAll(sankeyPlot);
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
                this.setBackground(bgColorTrans);
                this.legend.setBackground(bgColorTrans);
                this.borderPane.setBackground(bgColor);


                try {
                    if (sankeyPlot != null) {
                        sankeyPlot.setTextColor(config.getFontColor());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        });
    }

    private void updateChart() {
        plotItems.clear();
        for (SankeyDataRow sankeyDataRow : sankeyPojo.getNetGraphDataRows()) {
            JEVisValueUnitPair jeVisValueUnitPair = getJevisValueUnitPair(sankeyDataRow);
            plotItems.add(new JEVisPlotItem(sankeyDataRow.toString(), getRandomColor(), sankeyDataRow.getJeVisObject(), jeVisValueUnitPair.getValue(),jeVisValueUnitPair.getJeVisUnit()));
        }

        for (SankeyDataRow sankeyDataRow : sankeyPojo.getNetGraphDataRows()) {
            JEVisPlotItem jeVisPlotItem = getFromJEvisObject(sankeyDataRow.getJeVisObject());
            Optional<ChartDataRow> dataRowOptional = this.sampleHandler.getDataModel().stream().filter(chartDataRow -> {
                try {
                    return chartDataRow.getObject().equals(sankeyDataRow.getJeVisObject().getParent());
                } catch (JEVisException e) {
                    throw new RuntimeException(e);
                }
            }).findAny();
            if (dataRowOptional.isPresent()) {
                jeVisPlotItem.setName(dataRowOptional.get().getName());
                jeVisPlotItem.setFill(dataRowOptional.get().getColor());
            }
            addOutgoingObject(sankeyDataRow, jeVisPlotItem);
        }
        createPlotNames();
        addSpacing();


    }

    private JEVisValueUnitPair getJevisValueUnitPair(SankeyDataRow sankeyDataRow) {
        JEVisValueUnitPair jeVisValueUnitPair = new JEVisValueUnitPair(0,null);

        for (ChartDataRow dataModel : this.sampleHandler.getDataModel()) {
            jeVisValueUnitPair.setJeVisUnit(dataModel.getUnit());
            System.out.println("unit");
            System.out.println(dataModel.getUnit());
            System.out.printf(dataModel.getUnitLabel());
            AtomicDouble total = new AtomicDouble(0);
            if (dataModel.getId() == sankeyDataRow.getJeVisObject().getID().doubleValue()) {
                List<JEVisSample> results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    total.set(DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), results, dataModel));
                   jeVisValueUnitPair.setValue(total.get());
                }
            }

        }
        return jeVisValueUnitPair;
    }

    private void addOutgoingObject(SankeyDataRow sankeyDataRow, JEVisPlotItem jeVisPlotItem) {
        for (JEVisObject child : sankeyDataRow.getChildren()) {
            double sumoutgoing = 0;
            double childsumincoming = 0;

            JEVisPlotItem childPlotItem = getFromJEvisObject(child);


            if (jeVisPlotItem.hasOutgoing()) {
                sumoutgoing = jeVisPlotItem.getSumOfOutgoing();
            }
            if (getFromJEvisObject(child).getJevisValue() < (jeVisPlotItem.getJevisValue() - jeVisPlotItem.getSumOfOutgoing())) {
                jeVisPlotItem.addToOutgoing(getFromJEvisObject(child), childPlotItem.getJevisValue());
            } else {
                if (childPlotItem.hasIncoming()) {
                    childsumincoming = childPlotItem.getSumOfIncoming();
                }

                double remainingValue = jeVisPlotItem.getJevisValue() - sumoutgoing;

                double childrenRemainingValue = childPlotItem.getJevisValue() - childsumincoming;


                if (childrenRemainingValue >= remainingValue) {
                    jeVisPlotItem.addToOutgoing(getFromJEvisObject(child), remainingValue);
                } else {
                    jeVisPlotItem.addToOutgoing(getFromJEvisObject(child), childrenRemainingValue);
                }
            }
        }

    }

    private void createPlotNames() {
        for (JEVisPlotItem jeVisPlotItem : plotItems) {
            if (jeVisPlotItem.getIncoming().size() > 0) {
                if (sankeyPojo.getShowValueIn().equals(SankeyPojo.UNIT)) {
                    jeVisPlotItem.setName(createNameWithUnit(jeVisPlotItem));
                }else {
                    for (Map.Entry<PlotItem, Double> jeVisPlotItem1 : jeVisPlotItem.getIncoming().entrySet()) {
                        if (jeVisPlotItem1.getKey().hasIncoming()) {
                            jeVisPlotItem.setName(jeVisPlotItem.getName() + " (" + inPercent(jeVisPlotItem1.getKey().getSumOfIncoming(), jeVisPlotItem.getSumOfIncoming()) + "%)");
                        } else {
                            jeVisPlotItem.setName(jeVisPlotItem.getName());
                        }
                    }
                }
            }
        }
    }

    private String createNameWithUnit(JEVisPlotItem jeVisPlotItem) {
        return jeVisPlotItem.getName() + " (" + jeVisPlotItem.getJevisValue() + " " + jeVisPlotItem.getJeVisUnit().getLabel() + " )";
    }

    private void addSpacing() {
        int level = plotItems.stream().max(Comparator.comparing(JEVisPlotItem::getLevel)).orElseThrow(NoSuchElementException::new).getLevel();
        JEVisPlotItem jeVisPlotItem = new JEVisPlotItem("", Color.AQUA, null, 0,null);
        plotItems.add(jeVisPlotItem);
        for (int i = 0; i < level; i++) {
            jeVisPlotItem = addSpacingObject(jeVisPlotItem);
        }
    }

    private JEVisPlotItem addSpacingObject(JEVisPlotItem jeVisPlotItem) {
        JEVisPlotItem jeVisPlotItemNew = new JEVisPlotItem("", Color.AQUA, null, 0,null);
        jeVisPlotItem.addToOutgoing(jeVisPlotItemNew, 0);
        plotItems.add(jeVisPlotItemNew);
        return jeVisPlotItemNew;
    }

    private String inPercent(double in, double out) {
        BigDecimal bd = new BigDecimal((out / in) * 100);
        bd = bd.setScale(config.getDecimals(), RoundingMode.HALF_UP);
        return bd.toString();
    }

    @Override
    public void resize(double v, double v1) {
        super.resize(v, v1);
        if (sankeyPlot != null) {
            sankeyPlot.setMinSize(v - 10, v1 - 10);
            sankeyPlot.resize(v - 10, v1 - 10);
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


    private boolean isSelected(JEVisPlotItem parent, JEVisPlotItem item) {
        System.out.println("isSelected: " + parent + " item: " + item);
        for (Map.Entry<PlotItem, Double> entry : parent.getOutgoing().entrySet()) {
            JEVisPlotItem plotItem = (JEVisPlotItem) entry.getKey();
            if (plotItem.getObject().getID().equals(item.getObject().getID())) {
                return true;
            }
        }
        return false;
    }


    private Color getRandomColor() {
        Random rand = new Random();
        double r = rand.nextFloat() / 2f + 0.5;
        double g = rand.nextFloat() / 2f + 0.5;
        double b = rand.nextFloat() / 2f + 0.5;
        final Color color = new Color(r, g, b, 0.9);
        return color;
    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);

        ObservableList<ChartData> dataTable = widgetConfigDialog.addGeneralTabsDataModelNetGraph(this.sampleHandler);

        if (sankeyPojo != null) {
            widgetConfigDialog.addTab(sankeyPojo.getConfigTab(dataTable));
        }

        widgetConfigDialog.requestFirstTabFocus();

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.debug("OK Pressed {}", this);
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
                updateConfig();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public void init() {
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), WIDGET_ID);
        this.sampleHandler.setMultiSelect(true);

        this.legend.setAlignment(Pos.CENTER);

        try {

            this.sankeyPojo = new SankeyPojo(this.control, this.config.getConfigNode(Sankey_DESIGN_NODE_NAME));
        } catch (Exception exception) {
            logger.error(exception);
        }
        if (sankeyPojo == null) {
            logger.error("Gauge Setting is null make new: " + config.getUuid());
            this.sankeyPojo = new SankeyPojo(this.control);
        }

        setGraphic(this.borderPane);

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


        if (sankeyPojo != null) {
            dashBoardNode
                    .set(Sankey_DESIGN_NODE_NAME, sankeyPojo.toJSON());
        }

        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ChartWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
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

    private JEVisPlotItem getFromJEvisObject(JEVisObject jeVisObject) {
        Optional<JEVisPlotItem> jeVisPlotItem = plotItems.stream().filter(jeVisPlotItem2 -> jeVisPlotItem2.object.getID().intValue() == jeVisObject.getID().intValue()).findAny();
        if (jeVisPlotItem.isPresent()) {
            return jeVisPlotItem.get();
        } else {
            return null;
        }
    }

    public class JEVisPlotItem extends PlotItem {


        private JEVisObject object;
        private double jevisValue;

        private JEVisUnit jeVisUnit;


        @Override
        public String toString() {
            return "JEVisPlotItem{" +
                    "object=" + this.getName() +
                    "value=" + this.getValue() +
                    "outgoingSum=" + this.getSumOfOutgoing() +
                    "incomingSum=" + this.getSumOfIncoming() +
                    '}';
        }

        public JEVisPlotItem(String NAME, Color FILL, JEVisObject object, double value, JEVisUnit jeVisUnit) {
            super(NAME, 0, "", FILL);
            this.jevisValue = value;
            this.object = object;
            this.jeVisUnit = jeVisUnit;
        }

        public JEVisObject getObject() {
            return object;
        }

        public void setObject(JEVisObject object) {
            this.object = object;
        }

        public double getJevisValue() {
            return jevisValue;
        }

        public void setJevisValue(double jevisValue) {
            this.jevisValue = jevisValue;
        }

        public JEVisUnit getJeVisUnit() {
            return jeVisUnit;
        }

        public void setJeVisUnit(JEVisUnit jeVisUnit) {
            this.jeVisUnit = jeVisUnit;
        }
    }

    private class JEVisValueUnitPair{
        private double value;

        private JEVisUnit jeVisUnit;

        public JEVisValueUnitPair(double value, JEVisUnit jeVisUnit) {
            this.value = value;
            this.jeVisUnit = jeVisUnit;
        }

        public JEVisValueUnitPair() {
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public JEVisUnit getJeVisUnit() {
            return jeVisUnit;
        }

        public void setJeVisUnit(JEVisUnit jeVisUnit) {
            this.jeVisUnit = jeVisUnit;
        }
    }

}
