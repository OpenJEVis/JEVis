package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.shape.*;

import java.util.*;

/**
 * MultiAxisAreaChart - Plots the area between the line that connects the data
 * points and the 0 line on the Y axis.
 *
 * @since JavaFX 2.0
 */
public class MultiAxisAreaChart<X, Y> extends MultiAxisChart<X, Y> {

    // -------------- PRIVATE FIELDS ------------------------------------------

    /**
     * A multiplier for the Y values that we store for each series, it is used to
     * animate in a new series
     */
    private Map<MultiAxisChart.Series<X, Y>, DoubleProperty> seriesYMultiplierMap = new HashMap<>();
    private Legend legend = new Legend();

    // -------------- PUBLIC PROPERTIES ----------------------------------------

    /**
     * When true, CSS styleable symbols are created for any data items that don't
     * have a symbol node specified.
     *
     * @since JavaFX 8.0
     */
    private BooleanProperty createSymbols = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
//            for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
//                MultiAxisChart.Series<X, Y> series = getData().get(seriesIndex);
//                for (int itemIndex = 0; itemIndex < series.getData().size(); itemIndex++) {
//                    Data<X, Y> item = series.getData().get(itemIndex);
//                    Node symbol = item.getNode();
//                    if (get() && symbol == null) { // create any symbols
//                        symbol = createSymbol(series, getData().indexOf(series), item, itemIndex);
//                        if (null != symbol) {
//                            getPlotChildren().add(symbol);
//                        }
//                    } else if (!get() && symbol != null) { // remove symbols
//                        getPlotChildren().remove(symbol);
//                        symbol = null;
//                        item.setNode(null);
//                    }
//                }
//            }
//            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "createSymbols";
        }

        @Override
        public CssMetaData<MultiAxisAreaChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };

    /**
     * Construct a new MultiAxisAreaChart with the given axis
     *
     * @param xAxis  The x axis to use
     * @param y1Axis The y1 axis to use
     * @param y2Axis The y2 axis to use
     */
    public MultiAxisAreaChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis) {
        this(xAxis, y1Axis, y2Axis, FXCollections.observableArrayList());
    }

    /**
     * Construct a new Area Chart with the given axis and data
     *
     * @param xAxis  The x axis to use
     * @param y1Axis The first y axis to use
     * @param y2Axis The first y axis to use
     * @param data   The data to use, this is the actual list used so any changes to it
     *               will be reflected in the chart
     */
    public MultiAxisAreaChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis,
                              ObservableList<MultiAxisChart.Series<X, Y>> data) {
        super(xAxis, y1Axis, y2Axis);
        setLegend(legend);
        setData(data);
    }

    private static double doubleValue(Number number) {
        return doubleValue(number, 0);
    }

    // -------------- CONSTRUCTORS ----------------------------------------------

    private static double doubleValue(Number number, double nullDefault) {
        return (number == null) ? nullDefault : number.doubleValue();
    }

    /**
     * Indicates whether symbols for data points will be created or not.
     *
     * @return true if symbols for data points will be created and false otherwise.
     * @since JavaFX 8.0
     */
    public final boolean getCreateSymbols() {
        return createSymbols.getValue();
    }

    // -------------- METHODS
    // ------------------------------------------------------------------------------------------

    public final void setCreateSymbols(boolean value) {
        createSymbols.setValue(value);
    }

    public final BooleanProperty createSymbolsProperty() {
        return createSymbols;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> y1a = getY1Axis();
        final Axis<Y> y2a = getY2Axis();

        List<X> xData = null;
        List<Y> y1Data = null;
        List<Y> y2Data = null;

        if (xa.isAutoRanging())
            xData = new ArrayList<X>();
        if (y1a.isAutoRanging())
            y1Data = new ArrayList<Y>();
        if (y2a != null && y2a.isAutoRanging())
            y2Data = new ArrayList<Y>();

        if (xData != null || y1Data != null) {
            for (MultiAxisChart.Series<X, Y> series : getData()) {
                for (Data<X, Y> data : series.getData()) {
                    if (xData != null)
                        xData.add(data.getXValue());
                    if (y1Data != null && (data.getExtraValue() == null || (int) data.getExtraValue() == Y1_AXIS)) {
                        y1Data.add(data.getYValue());
                    } else if (y2Data != null) {
                        if (y2a == null)
                            throw new NullPointerException("Y2 Axis is not defined.");
                        y2Data.add(data.getYValue());
                    }
                }
            }
            if (xData != null && !(xData.size() == 1 && getXAxis().toNumericValue(xData.get(0)) == 0)) {
                xa.invalidateRange(xData);
            }
            if (y1Data != null && !(y1Data.size() == 1 && getY1Axis().toNumericValue(y1Data.get(0)) == 0)) {
                y1a.invalidateRange(y1Data);
            }
            if (y2Data != null && !(y2Data.size() == 1 && getY2Axis().toNumericValue(y2Data.get(0)) == 0)) {
                y2a.invalidateRange(y2Data);
            }
        }
    }

    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        final Node symbol = createSymbol(series, getData().indexOf(series), item, itemIndex);
        if (symbol != null) {
            getPlotChildren().add(symbol);
            symbol.getStyleClass().setAll("chart-series-area-fill", "series" + itemIndex, series.defaultColorStyleClass,
                    "chart-series-area-line", "series" + itemIndex, series.defaultColorStyleClass);
        }
    }

    @Override
    protected void dataItemRemoved(final Data<X, Y> item, final Series<X, Y> series) {
        final Node symbol = item.getNode();

        if (symbol != null) {
            symbol.focusTraversableProperty().unbind();
        }

        // remove item from sorted list
        int itemIndex = series.getItemIndex(item);

        item.setSeries(null);
        getPlotChildren().remove(symbol);
        removeDataItemFromDisplay(series, item);

        // Note: better animation here, point should move from old position to new
        // position at center point between prev and next symbols
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void dataItemChanged(Data<X, Y> item) {
    }

    @Override
    protected void seriesChanged(Change<? extends MultiAxisChart.Series<X, Y>> c) {
        // Update style classes for all series lines and symbols
        // Note: is there a more efficient way of doing this?
        for (int i = 0; i < getDataSize(); i++) {
            final MultiAxisChart.Series<X, Y> s = getData().get(i);
            Path seriesLine = (Path) ((Group) s.getNode()).getChildren().get(1);
            Path fillPath = (Path) ((Group) s.getNode()).getChildren().get(0);
            seriesLine.getStyleClass().setAll("chart-series-area-line", "series" + i, s.defaultColorStyleClass);
            fillPath.getStyleClass().setAll("chart-series-area-fill", "series" + i, s.defaultColorStyleClass);
            for (int j = 0; j < s.getData().size(); j++) {
                final Data<X, Y> item = s.getData().get(j);
                final Node node = item.getNode();
                if (node != null) {
                    node.getStyleClass().setAll("chart-area-symbol", "series" + i, "data" + j,
                            s.defaultColorStyleClass);
                }
            }
        }
    }

    @Override
    protected void seriesAdded(MultiAxisChart.Series<X, Y> series, int seriesIndex) {
        // create new paths for series
        Path seriesLine = new Path();
        Path fillPath = new Path();
        seriesLine.setStrokeLineJoin(StrokeLineJoin.BEVEL);
        Group areaGroup = new Group(fillPath, seriesLine);
        series.setNode(areaGroup);
        // create series Y multiplier
        DoubleProperty seriesYAnimMultiplier = new SimpleDoubleProperty(this, "seriesYMultiplier");
        seriesYMultiplierMap.put(series, seriesYAnimMultiplier);
        // handle any data already in series

        seriesYAnimMultiplier.setValue(1d);

        getPlotChildren().add(areaGroup);

        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            final Node symbol = createSymbol(series, seriesIndex, item, j);
            if (symbol != null) {
                getPlotChildren().add(symbol);
            }
        }

    }

    private void updateDefaultColorIndex(final MultiAxisChart.Series<X, Y> series) {
        int clearIndex = seriesColorMap.get(series);
        Path seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
        Path fillPath = (Path) ((Group) series.getNode()).getChildren().get(0);
        if (seriesLine != null) {
            seriesLine.getStyleClass().remove(DEFAULT_COLOR + clearIndex);
        }
        if (fillPath != null) {
            fillPath.getStyleClass().remove(DEFAULT_COLOR + clearIndex);
        }
        for (int j = 0; j < series.getData().size(); j++) {
            final Node node = series.getData().get(j).getNode();
            if (node != null) {
                node.getStyleClass().remove(DEFAULT_COLOR + clearIndex);
            }
        }
    }

    @Override
    protected void seriesRemoved(final MultiAxisChart.Series<X, Y> series) {
        updateDefaultColorIndex(series);
        // remove series Y multiplier
        seriesYMultiplierMap.remove(series);
        // remove all symbol nodes

        getPlotChildren().remove(series.getNode());
        for (Data<X, Y> d : series.getData())
            getPlotChildren().remove(d.getNode());
        removeSeriesFromDisplay(series);

    }

    /**
     * @inheritDoc
     */
    @Override
    protected void layoutPlotChildren() {
        List<LineTo> constructedPath = new ArrayList<>(getDataSize());
        for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
            MultiAxisChart.Series<X, Y> series = getData().get(seriesIndex);
            DoubleProperty seriesYAnimMultiplier = seriesYMultiplierMap.get(series);
            double lastX = 0;
            final ObservableList<Node> children = ((Group) series.getNode()).getChildren();
            ObservableList<PathElement> seriesLine = ((Path) children.get(1)).getElements();
            ObservableList<PathElement> fillPath = ((Path) children.get(0)).getElements();
            seriesLine.clear();
            fillPath.clear();
            constructedPath.clear();
            for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(series); it.hasNext(); ) {
                Data<X, Y> item = it.next();
                double x = getXAxis().getDisplayPosition(item.getCurrentX());

                double y = -1;
                if (item.getExtraValue() == null || (int) item.getExtraValue() == MultiAxisChart.Y1_AXIS) {
                    y = getY1Axis().getDisplayPosition(getY1Axis().toRealValue(
                            getY1Axis().toNumericValue(item.getCurrentY()) * seriesYAnimMultiplier.getValue()));
                } else {
                    if (getY2Axis() != null) {
                        if (getY2Axis().isVisible()) {
                            y = getY2Axis().getDisplayPosition(getY2Axis().toRealValue(
                                    getY2Axis().toNumericValue(item.getCurrentY()) * seriesYAnimMultiplier.getValue()));
                        } else {
                            continue;
                        }
                    } else {
                        throw new NullPointerException("Y2 axis is not defined.");
                    }
                }

                constructedPath.add(new LineTo(x, y));
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                lastX = x;
                Node symbol = item.getNode();
                if (symbol != null) {
                    final double w = symbol.prefWidth(-1);
                    final double h = symbol.prefHeight(-1);
                    symbol.resizeRelocate(x - (w / 2), y - (h / 2), w, h);
                }
            }

            if (!constructedPath.isEmpty()) {
                constructedPath.sort(Comparator.comparingDouble(LineTo::getX));
                LineTo first = constructedPath.get(0);

                final double displayYPos = first.getY();
                final double numericYPos = getY1Axis().toNumericValue(getY1Axis().getValueForDisplay(displayYPos));

                // RT-34626: We can't always use getZeroPosition(), as it may be the case
                // that the zero position of the y-axis is not visible on the chart. In these
                // cases, we need to use the height between the point and the y-axis line.
                final double yAxisZeroPos = getY1Axis().getZeroPosition();
                final boolean isYAxisZeroPosVisible = !Double.isNaN(yAxisZeroPos);
                final double yAxisHeight = getY1Axis().getHeight();
                final double yFillPos = isYAxisZeroPosVisible ? yAxisZeroPos
                        : numericYPos < 0 ? numericYPos - yAxisHeight : yAxisHeight;

                seriesLine.add(new MoveTo(first.getX(), displayYPos));
                fillPath.add(new MoveTo(first.getX(), yFillPos));

                seriesLine.addAll(constructedPath);
                fillPath.addAll(constructedPath);
                fillPath.add(new LineTo(lastX, yFillPos));
                fillPath.add(new ClosePath());
            }
        }
    }

    private Node createSymbol(MultiAxisChart.Series<X, Y> series, int seriesIndex, final Data<X, Y> item,
                              int itemIndex) {
        Node symbol = item.getNode();
        // check if symbol has already been created
//        if (symbol == null && getCreateSymbols()) {
//            symbol = new StackPane();
//            symbol.setAccessibleRole(AccessibleRole.TEXT);
//            symbol.setAccessibleRoleDescription("Point");
//            symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
//            item.setNode(symbol);
//        }
        // set symbol styles
        // Note: not sure if we want to add or check, ie be more careful and efficient
        // here
        if (symbol != null)
            symbol.getStyleClass().addAll("chart-area-symbol", "series" + seriesIndex, "data" + itemIndex,
                    series.defaultColorStyleClass);
        return symbol;
    }

    /**
     * This is called whenever a series is added or removed and the legend needs to
     * be updated
     */
    @Override
    protected void updateLegend() {
        legend.getItems().clear();
        if (getData() != null) {
            for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
                MultiAxisChart.Series<X, Y> series = getData().get(seriesIndex);
                LegendItem legenditem = new LegendItem(series.getName());
                legenditem.getSymbol().getStyleClass().addAll("chart-area-symbol", "series" + seriesIndex,
                        "area-legend-symbol", series.defaultColorStyleClass);
                legend.getItems().add(legenditem);
            }
        }
        if (legend.getItems().size() > 0) {
            if (getLegend() == null) {
                setLegend(legend);
            }
        } else {
            setLegend(null);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
