package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import eu.hansolo.fx.charts.data.PlotItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
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
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SankeyWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(SankeyWidget.class);
    public static String WIDGET_ID = "Sankey";

    public static String Sankey_DESIGN_NODE_NAME = "Sankey";
    private final NumberFormat nf = NumberFormat.getInstance();
    private SankeyPlot sankeyPlot;
    private final WidgetLegend legend = new WidgetLegend();
    private final Pane borderPane = new Pane();
    private Interval lastInterval = null;
    private final BorderPane bottomBorderPane = new BorderPane();
    private final List<JEVisPlotItem> plotItems = new ArrayList<>();
    private Boolean customWorkday = true;

    private SankeyPojo sankeyPojo;

    private String alermsg = "";

    private boolean error = false;

    private final List<JEVisObject> sankeyDataRowsWithMultipleParents = new ArrayList<>();


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
        showAlertOverview(false, "");
        alermsg = "";
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

        this.sampleHandler.setAutoAggregation(true);
        this.sampleHandler.update(interval);

        updateChart();
        try {

            Platform.runLater(() -> {
                borderPane.setPadding(new Insets(10));

                Size configSize = getConfig().getSize();
                buildSankeyPlot(configSize);
                updateConfig();
            });
            /** workaround because we make a new chart every time**/
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
        }
        showProgressIndicator(false);
        updateConfig();
    }

    private void buildSankeyPlot(Size configSize) {
        if (plotItems.size() > 0) {
            logger.info("create Sankey plot");

            this.sankeyPlot = SankeyPlotBuilder.create()
                    .items(plotItems)
                    .autoItemWidth(true)
                    .minSize(configSize.getWidth(), configSize.getHeight())
                    .streamFillMode(eu.hansolo.fx.charts.SankeyPlot.StreamFillMode.GRADIENT)
                    .showFlowDirection(true)
                    .textColor(config.getFontColor())
                    .build();
            sankeyPlot.setManaged(true);

        }

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

                        sankeyPlot.setFontSize(config.getFontSize());
                        if (sankeyPojo.isAutoGap()) {
                            sankeyPlot.setAutoItemGap(true);
                        } else {
                            sankeyPlot.setAutoItemGap(false);
                            sankeyPlot.setItemGap(sankeyPojo.getGap());
                        }
                        sankeyPlot.setPadding(new Insets(0, 10, 10, 10));
                        this.borderPane.getChildren().setAll(sankeyPlot);
                        if (error) {
                            sankeyPlot.setTextColor(config.getBackgroundColor());
                        } else {
                            sankeyPlot.setTextColor(config.getFontColor());
                        }
                        sankeyPlot.setMinSize(config.getSize().getWidth(), config.getSize().getHeight());
                        sankeyPlot.setOffsetMap(sankeyPojo.getOffsetMap());
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    private void updateChart() {
        try {


            plotItems.clear();
            for (SankeyDataRow sankeyDataRow : sankeyPojo.getNetGraphDataRows()) {
                JEVisValueUnitPair jeVisValueUnitPair = getJevisValueUnitPair(sankeyDataRow);
                JEVisPlotItem jeVisPlotItem = new JEVisPlotItem(sankeyDataRow.toString(), getRandomColor(), sankeyDataRow.getJeVisObject(), jeVisValueUnitPair.getValue(), jeVisValueUnitPair.getJeVisUnit());
                logger.info("create Plot Item: {}",jeVisPlotItem);
                plotItems.add(jeVisPlotItem);
            }

            for (SankeyDataRow sankeyDataRow : sankeyPojo.getNetGraphDataRows()) {
                JEVisPlotItem jeVisPlotItem = getFromJEvisObject(sankeyDataRow.getJeVisObject());
                Optional<ChartDataRow> dataRowOptional = this.sampleHandler.getChartDataRows().stream().filter(chartDataRow -> {
                    try {
                        return chartDataRow.getObject().equals(sankeyDataRow.getJeVisObject().getParent());
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }).findAny();
                if (dataRowOptional.isPresent()) {
                    try {

                        jeVisPlotItem.setName(dataRowOptional.get().getName());
                        jeVisPlotItem.setFill(dataRowOptional.get().getColor());
                    } catch (Exception e) {
                      logger.error(e);
                    }
                }

                addOutgoingObject(sankeyDataRow, jeVisPlotItem);
            }
            try {
                addOutgoingWithMultipleParents();

                check();
                createPlotNames();

                addSpacing(plotItems.stream().max(Comparator.comparing(JEVisPlotItem::getLevel)).orElseThrow(NoSuchElementException::new).getLevel(),true);
                addSpacing(plotItems.stream().max(Comparator.comparing(JEVisPlotItem::getLevel)).orElseThrow(NoSuchElementException::new).getLevel(),false);
            } catch (Exception e) {
                logger.error(e);
            }
        } catch (Exception e) {
            logger.error(e);
        }


    }

    private void addOutgoingWithMultipleParents() {
        for (JEVisObject jeVisObject : sankeyDataRowsWithMultipleParents) {
            logger.info("Plot Item with multiple parents: {}",sankeyDataRowsWithMultipleParents);
            JEVisPlotItem child = getFromJEvisObject(jeVisObject);
            double remaining = child.getValue();
            for (int i = 0; i < getParents(jeVisObject).size(); i++) {
                JEVisPlotItem parent = getFromJEvisObject(getParents(jeVisObject).get(i).getJeVisObject());
                double diff = parent.getValue() - parent.getSumOfOutgoing();
                if (i < getParents(jeVisObject).size() + 1) {
                    if (diff > 0 && remaining > 0) {
                        parent.addToOutgoing(child, diff);
                        remaining = remaining - diff;
                    }

                } else {
                    if (remaining > 0) {
                        parent.addToOutgoing(child, remaining);
                    }
                }


            }

        }
    }

    private JEVisValueUnitPair getJevisValueUnitPair(SankeyDataRow sankeyDataRow) {
        JEVisValueUnitPair jeVisValueUnitPair = new JEVisValueUnitPair(0, null);

        for (ChartDataRow dataModel : this.sampleHandler.getChartDataRows()) {
            jeVisValueUnitPair.setJeVisUnit(dataModel.getUnit());
            AtomicDouble total = new AtomicDouble(0);
            if (dataModel.getId() == sankeyDataRow.getJeVisObject().getID().doubleValue()) {
                List<JEVisSample> results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    try {
                        total.set(dataModel.getSamples().get(0).getValueAsDouble());
                    } catch (Exception e) {
                        logger.error(e);
                    }
                    jeVisValueUnitPair.setValue(total.get());
                }
            }

        }
        return jeVisValueUnitPair;
    }

    private List<SankeyDataRow> getParents(JEVisObject jeVisObject) {
        List<SankeyDataRow> parents = new ArrayList<>();
        for (SankeyDataRow netGraphDataRow : sankeyPojo.getNetGraphDataRows()) {
            for (JEVisObject jeVisObject1 : netGraphDataRow.getChildren()) {
                if (jeVisObject1.getID().intValue() == jeVisObject.getID().intValue()) {
                    parents.add(netGraphDataRow);
                    break;
                }
            }
        }
        return parents;
    }


    private void addOutgoingObject(SankeyDataRow sankeyDataRow, JEVisPlotItem jeVisPlotItem) {
            sankeyDataRow.getChildren().forEach(jeVisObject -> {
                if (getParents(jeVisObject).size() > 1) {
                    if (!sankeyDataRowsWithMultipleParents.contains(jeVisObject)) {
                        sankeyDataRowsWithMultipleParents.add(jeVisObject);
                    }

                } else {
                    logger.info("add Plot Item: {} the outgoing object: {}",jeVisPlotItem,getFromJEvisObject(jeVisObject));
                    jeVisPlotItem.addToOutgoing(getFromJEvisObject(jeVisObject), getFromJEvisObject(jeVisObject).getValue());
                }
            });
    }

    private void check() {
        error = false;


       long sizeRootNotNull = plotItems.stream().filter(jeVisPlotItem -> jeVisPlotItem.isRoot()).filter(jeVisPlotItem -> jeVisPlotItem.getValue() != 0).count();
        if (sizeRootNotNull == 0) {
            showAlertOverview(true, ("Root has no Data"));
                error = true;
                return;
        }

            plotItems.forEach(jeVisPlotItem -> {

//            if (jeVisPlotItem.isRoot() && jeVisPlotItem.getValue() == 0) {
//                showAlertOverview(true, ("Root has no Data"));
//                error = true;
//                return;
//            }
                BigDecimal value = BigDecimal.valueOf(jeVisPlotItem.getValue());
                value = value.setScale(config.getDecimals(), RoundingMode.HALF_UP);
                BigDecimal outgoing = BigDecimal.valueOf(jeVisPlotItem.getSumOfOutgoing());
                outgoing = outgoing.setScale(config.getDecimals(), RoundingMode.HALF_UP);
                if (value.doubleValue() * (1 + sankeyPojo.getErrorTolerance()) < outgoing.doubleValue()) {
                    jeVisPlotItem.setFill(Color.RED);
                    jeVisPlotItem.setOnItemEvent(itemEvent -> {
                    });
                    alermsg = alermsg + jeVisPlotItem.getName() + " Value: " + value.doubleValue() + " < Sum Outgoing: " + outgoing.doubleValue() + "\n";
                    showAlertOverview(true, (alermsg));
                }

            });
        }



    private void createPlotNames() {
        for (JEVisPlotItem jeVisPlotItem : plotItems) {
            if (sankeyPojo.isShowValue()) {
                jeVisPlotItem.setName(createNameWithUnit(jeVisPlotItem));
            }
            if (jeVisPlotItem.hasIncoming() && sankeyPojo.isShowPercent()) {
                if (sankeyPojo.getPercentRefersTo().equals(REFERS_TO.PARENT)) {
                    createNamesRefersToParent(jeVisPlotItem);
                } else if(sankeyPojo.getPercentRefersTo().equals(REFERS_TO.ROOT)) {
                    createNamesRefersToRoot(jeVisPlotItem);
                }


            }


        }
    }

    private void createNamesRefersToRoot(JEVisPlotItem jeVisPlotItem) {
        List<PlotItem> roots = plotItems.stream().filter(jeVisPlotItem1 -> jeVisPlotItem1.isRoot() && jeVisPlotItem1.getValue() != 0).collect(Collectors.toList());
        if (roots.size() > 0) {
            String stringBuilder = jeVisPlotItem.getName() +
                    "\n" +
                    "(" +
                    inPercent(roots.get(0).getValue(), jeVisPlotItem.getValue()) +
                    "%)";
            jeVisPlotItem.setName(stringBuilder);
        }
    }

    private void createNamesRefersToParent(JEVisPlotItem jeVisPlotItem) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(jeVisPlotItem.getName());
        if (!sankeyPojo.isShowValue()) {
            stringBuilder.append("\n");
        }
        stringBuilder.append("(");
        for (Map.Entry<PlotItem, Double> jeVisPlotItemChild : jeVisPlotItem.getIncoming().entrySet()) {
            stringBuilder.append(inPercent(jeVisPlotItemChild.getKey().getValue(), jeVisPlotItemChild.getValue()));
            stringBuilder.append("%");
            stringBuilder.append("\n");
        }
        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        stringBuilder.append(")");
        if (jeVisPlotItem.getValue() != 0) {

            jeVisPlotItem.setName(stringBuilder.toString());
        }
    }

    private String createNameWithUnit(JEVisPlotItem jeVisPlotItem) {
        BigDecimal bigDecimal = BigDecimal.valueOf(jeVisPlotItem.getValue());
        bigDecimal = bigDecimal.setScale(config.getDecimals(), RoundingMode.HALF_UP);
        return jeVisPlotItem.getName() + "\n" + bigDecimal.doubleValue() + " " + jeVisPlotItem.getJeVisUnit().getLabel();
    }

    private void addSpacing(int maxLevel,boolean toTop) {
        try {
            int level = maxLevel;
            JEVisPlotItem jeVisPlotItem = new JEVisPlotItem("", Color.AQUA, null, 0, null);
            if (toTop) {
                plotItems.add(0,jeVisPlotItem);
            }else {
                plotItems.add(jeVisPlotItem);
            }
            for (int i = 0; i < level; i++) {
                if (toTop) {

                    jeVisPlotItem = addSpacingObject(jeVisPlotItem,true);
                }else {
                    jeVisPlotItem = addSpacingObject(jeVisPlotItem,false);

                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

    }

    private JEVisPlotItem addSpacingObject(JEVisPlotItem jeVisPlotItem, boolean toTop) {
        JEVisPlotItem jeVisPlotItemNew = new JEVisPlotItem("", Color.AQUA, null, 0, null);
        jeVisPlotItem.addToOutgoing(jeVisPlotItemNew, 0);
        if (toTop) {

            plotItems.add(0,jeVisPlotItemNew);
        }else {

            plotItems.add(jeVisPlotItemNew);
        }
        return jeVisPlotItemNew;
    }

    private String inPercent(double in, double out) {
        try {
            BigDecimal bd = BigDecimal.valueOf((out / in) * 100);
            bd = bd.setScale(config.getDecimals(), RoundingMode.HALF_UP);
            return bd.toString();
        } catch (Exception e) {
            logger.error(e);
        }

        return "";
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
        logger.debug("isSelected: " + parent + " item: " + item);
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
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config, WIDGET_ID);
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
            setValue(value);
            this.object = object;
            this.jeVisUnit = jeVisUnit;
        }

        public JEVisObject getObject() {
            return object;
        }

        public void setObject(JEVisObject object) {
            this.object = object;
        }


        public JEVisUnit getJeVisUnit() {
            return jeVisUnit;
        }

        public void setJeVisUnit(JEVisUnit jeVisUnit) {
            this.jeVisUnit = jeVisUnit;
        }
    }

    private class JEVisValueUnitPair {
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

        private void calcNumberOfLevels() {

        }




    }
    public enum REFERS_TO {
        PARENT,
        ROOT,
    }

    public enum SHOW_VALUE_IN{
        PERCENT,
        UNIT,
    }

}
