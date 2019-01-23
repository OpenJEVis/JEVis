package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.*;

/**
 * Line Chart plots a line connecting the data points in a series. The data
 * points themselves can be represented by symbols optionally. Line charts are
 * usually used to view data trends over time or category.
 *
 * @since JavaFX 2.0
 */
public class MultiAxisLineChart<X, Y> extends MultiAxisChart<X, Y> {

    // -------------- PRIVATE FIELDS ------------------------------------------

    /**
     * A multiplier for the Y values that we store for each series, it is used to
     * animate in a new series
     */
    private Map<Series<X, Y>, DoubleProperty> seriesYMultiplierMap = new HashMap<>();
    private Legend legend = new Legend();
    private Timeline dataRemoveTimeline;
    private Series<X, Y> seriesOfDataRemoved = null;
    private Data<X, Y> dataItemBeingRemoved = null;
    private FadeTransition fadeSymbolTransition = null;
    private Map<Data<X, Y>, Double> XYValueMap = new HashMap<Data<X, Y>, Double>();
    private Timeline seriesRemoveTimeline = null;
    // -------------- PUBLIC PROPERTIES ----------------------------------------

    /**
     * When true, CSS styleable symbols are created for any data items that don't
     * have a symbol node specified.
     */
    private BooleanProperty createSymbols = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
                Series<X, Y> series = getData().get(seriesIndex);
                for (int itemIndex = 0; itemIndex < series.getData().size(); itemIndex++) {
                    Data<X, Y> item = series.getData().get(itemIndex);
                    Node symbol = item.getNode();
                    if (get() && symbol == null) { // create any symbols
                        symbol = createSymbol(series, getData().indexOf(series), item, itemIndex);
                        getPlotChildren().add(symbol);
                    } else if (!get() && symbol != null) { // remove symbols
                        getPlotChildren().remove(symbol);
                        symbol = null;
                        item.setNode(null);
                    }
                }
            }
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisLineChart.this;
        }

        @Override
        public String getName() {
            return "createSymbols";
        }

        @Override
        public CssMetaData<MultiAxisLineChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };
    /**
     * Indicates whether the data passed to MultiAxisLineChart should be sorted by
     * natural order of one of the axes. If this is set to
     * {@link SortingPolicy#NONE}, the order in {@link #dataProperty()} will be
     * used.
     *
     * @defaultValue SortingPolicy#X_AXIS
     * @see SortingPolicy
     * @since JavaFX 8u40
     */
    private ObjectProperty<SortingPolicy> axisSortingPolicy = new ObjectPropertyBase<SortingPolicy>(
            SortingPolicy.X_AXIS) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisLineChart.this;
        }

        @Override
        public String getName() {
            return "axisSortingPolicy";
        }

    };

    /**
     * Construct a new MultiAxisLineChart with the given axis.
     *
     * @param xAxis  The x axis to use
     * @param y1Axis The y1 axis to use
     * @param y2Axis The y2 axis to use
     */
    public MultiAxisLineChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis) {
        this(xAxis, y1Axis, y2Axis, FXCollections.observableArrayList());
    }

    /**
     * Construct a new MultiAxisLineChart with the given axis and data.
     *
     * @param xAxis  The x axis to use
     * @param y1Axis The first y axis to use
     * @param y2Axis The second y axis to use
     * @param data   The data to use, this is the actual list used so any changes to it
     *               will be reflected in the chart
     */
    public MultiAxisLineChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis, ObservableList<Series<X, Y>> data) {
        super(xAxis, y1Axis, y2Axis);
        setLegend(legend);
        setData(data);
    }

    /**
     * Indicates whether symbols for data points will be created or not.
     *
     * @return true if symbols for data points will be created and false otherwise.
     */
    public final boolean getCreateSymbols() {
        return createSymbols.getValue();
    }

    public final void setCreateSymbols(boolean value) {
        createSymbols.setValue(value);
    }

    public final BooleanProperty createSymbolsProperty() {
        return createSymbols;
    }

    public final SortingPolicy getAxisSortingPolicy() {
        return axisSortingPolicy.getValue();
    }

    // -------------- CONSTRUCTORS ----------------------------------------------

    public final void setAxisSortingPolicy(SortingPolicy value) {
        axisSortingPolicy.setValue(value);
    }

    public final ObjectProperty<SortingPolicy> axisSortingPolicyProperty() {
        return axisSortingPolicy;
    }

    // --------------
    // METHODS-------------------------------------------------------------

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
            // RT-32838 No need to invalidate range if there is one data item - whose value
            // is zero.
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
    protected void dataItemAdded(final Series<X, Y> series, int itemIndex, final Data<X, Y> item) {
        final Node symbol = createSymbol(series, getData().indexOf(series), item, itemIndex);

        if (symbol != null)
            getPlotChildren().add(symbol);

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
        if (symbol != null)
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
            final Series<X, Y> s = getData().get(i);
            Node seriesNode = s.getNode();
            if (seriesNode != null)
                seriesNode.getStyleClass().setAll("chart-series-line", "series" + i, s.defaultColorStyleClass);
        }
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        // create new path for series
        Path seriesLine = new Path();
        seriesLine.setStrokeLineJoin(StrokeLineJoin.BEVEL);
        series.setNode(seriesLine);
        // create series Y multiplier
        DoubleProperty seriesYAnimMultiplier = new SimpleDoubleProperty(this, "seriesYMultiplier");
        seriesYMultiplierMap.put(series, seriesYAnimMultiplier);
        // handle any data already in series

        seriesYAnimMultiplier.setValue(1d);

        getPlotChildren().add(seriesLine);

        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            final Node symbol = createSymbol(series, seriesIndex, item, j);
            if (symbol != null) {

                getPlotChildren().add(symbol);

            }
        }

    }

    private void updateDefaultColorIndex(final Series<X, Y> series) {
        int clearIndex = seriesColorMap.get(series);
        series.getNode().getStyleClass().remove(DEFAULT_COLOR + clearIndex);
        for (int j = 0; j < series.getData().size(); j++) {
            final Node node = series.getData().get(j).getNode();
            if (node != null) {
                node.getStyleClass().remove(DEFAULT_COLOR + clearIndex);
            }
        }
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        updateDefaultColorIndex(series);
        // remove all symbol nodes
        seriesYMultiplierMap.remove(series);

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
            Series<X, Y> series = getData().get(seriesIndex);
            final DoubleProperty seriesYAnimMultiplier = seriesYMultiplierMap.get(series);
            if (series.getNode() instanceof Path) {
                final ObservableList<PathElement> seriesLine = ((Path) series.getNode()).getElements();
                seriesLine.clear();
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
                                y = getY2Axis().getDisplayPosition(
                                        getY2Axis().toRealValue(getY2Axis().toNumericValue(item.getCurrentY())
                                                * seriesYAnimMultiplier.getValue()));
                            } else {
                                continue;
                            }
                        } else {
                            throw new NullPointerException("Y2 axis is not defined.");
                        }
                    }

                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }
                    constructedPath.add(new LineTo(x, y));

                    Node symbol = item.getNode();
                    if (symbol != null) {
                        final double w = symbol.prefWidth(-1);
                        final double h = symbol.prefHeight(-1);
                        symbol.resizeRelocate(x - (w / 2), y - (h / 2), w, h);
                    }
                }
                switch (getAxisSortingPolicy()) {
                    case X_AXIS:
                        constructedPath.sort(Comparator.comparingDouble(LineTo::getX));
                        break;
                    case Y_AXIS:
                        constructedPath.sort(Comparator.comparingDouble(LineTo::getY));
                        break;
                }

                if (!constructedPath.isEmpty()) {
                    LineTo first = constructedPath.get(0);
                    seriesLine.add(new MoveTo(first.getX(), first.getY()));
                    seriesLine.addAll(constructedPath);
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    void dataBeingRemovedIsAdded(Data item, Series series) {
        if (fadeSymbolTransition != null) {
            fadeSymbolTransition.setOnFinished(null);
            fadeSymbolTransition.stop();
        }
        if (dataRemoveTimeline != null) {
            dataRemoveTimeline.setOnFinished(null);
            dataRemoveTimeline.stop();
        }
        final Node symbol = item.getNode();
        if (symbol != null)
            getPlotChildren().remove(symbol);

        item.setSeries(null);
        removeDataItemFromDisplay(series, item);

        // restore values to item
        Double value = XYValueMap.get(item);
        if (value != null) {
            item.setYValue(value);
            item.setCurrentY(value);
        }
        XYValueMap.clear();
    }

    /**
     * @inheritDoc
     */
    @Override
    void seriesBeingRemovedIsAdded(Series<X, Y> series) {
        if (seriesRemoveTimeline != null) {
            seriesRemoveTimeline.setOnFinished(null);
            seriesRemoveTimeline.stop();
            getPlotChildren().remove(series.getNode());
            for (Data<X, Y> d : series.getData())
                getPlotChildren().remove(d.getNode());
            removeSeriesFromDisplay(series);
        }
    }

    private Timeline createDataRemoveTimeline(final Data<X, Y> item, final Node symbol, final Series<X, Y> series) {
        Timeline t = new Timeline();
        // save data values in case the same data item gets added immediately.
        XYValueMap.put(item, ((Number) item.getYValue()).doubleValue());

        t.getKeyFrames()
                .addAll(new KeyFrame(Duration.ZERO, new KeyValue(item.currentYProperty(), item.getCurrentY()),
                                new KeyValue(item.currentXProperty(), item.getCurrentX())),
                        new KeyFrame(Duration.millis(500), actionEvent -> {
                            if (symbol != null)
                                getPlotChildren().remove(symbol);
                            removeDataItemFromDisplay(series, item);
                            XYValueMap.clear();
                        }, new KeyValue(item.currentYProperty(), item.getYValue(), Interpolator.EASE_BOTH),
                                new KeyValue(item.currentXProperty(), item.getXValue(), Interpolator.EASE_BOTH)));
        return t;
    }

    private Node createSymbol(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {
        Node symbol = item.getNode();
        // check if symbol has already been created
        if (symbol == null && getCreateSymbols()) {
            symbol = new StackPane();
            symbol.setAccessibleRole(AccessibleRole.TEXT);
            symbol.setAccessibleRoleDescription("Point");
            symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            item.setNode(symbol);
        }
        // set symbol styles
        if (symbol != null)
            symbol.getStyleClass().addAll("chart-line-symbol", "series" + seriesIndex, "data" + itemIndex,
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
                Series<X, Y> series = getData().get(seriesIndex);
                LegendItem legenditem = new LegendItem(series.getName());
                legenditem.getSymbol().getStyleClass().addAll("chart-line-symbol", "series" + seriesIndex,
                        series.defaultColorStyleClass);
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

    /**
     * This enum defines a policy for {@link #axisSortingPolicyProperty()}.
     *
     * @since JavaFX 8u40
     */
    public enum SortingPolicy {
        /**
         * The data should be left in the order defined by the list in
         * {@link: javafx.scene.chart.#dataProperty()}.
         */
        NONE,
        /**
         * The data is ordered by x axis.
         */
        X_AXIS,
        /**
         * The data is ordered by y axis.
         */
        Y_AXIS
    }
}