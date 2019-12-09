package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jevis.jeconfig.application.Chart.Charts.jfx.Axis;
import org.jevis.jeconfig.application.Chart.Charts.jfx.CategoryAxis;
import org.jevis.jeconfig.application.Chart.Charts.jfx.ValueAxis;

import java.util.*;

/**
 * Line Chart plots a line connecting the data points in a series. The data
 * points themselves can be represented by symbols optionally. Line charts are
 * usually used to view data trends over time or category.
 *
 * @since JavaFX 2.0
 */
public class MultiAxisBubbleChart<X, Y> extends MultiAxisChart<X, Y> {

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
            return MultiAxisBubbleChart.this;
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
            return MultiAxisBubbleChart.this;
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
    public MultiAxisBubbleChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis) {
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
    public MultiAxisBubbleChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis, ObservableList<Series<X, Y>> data) {
        super(xAxis, y1Axis, y2Axis);
        setLegend(legend);
        setData(data);
    }

    private static double getDoubleValue(Object number, double nullDefault) {
        return !(number instanceof Number) ? nullDefault : ((Number) number).doubleValue();
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

    // -------------- CONSTRUCTORS ----------------------------------------------

    public final SortingPolicy getAxisSortingPolicy() {
        return axisSortingPolicy.getValue();
    }

    public final void setAxisSortingPolicy(SortingPolicy value) {
        axisSortingPolicy.setValue(value);
    }

    // --------------
    // METHODS-------------------------------------------------------------

    public final ObjectProperty<SortingPolicy> axisSortingPolicyProperty() {
        return axisSortingPolicy;
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
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        Node bubble = createBubble(series, getData().indexOf(series), item, itemIndex);
        if (shouldAnimate()) {
            // fade in new bubble
            bubble.setOpacity(0);
            getPlotChildren().add(bubble);
            FadeTransition ft = new FadeTransition(Duration.millis(500), bubble);
            ft.setToValue(1);
            ft.play();
        } else {
            getPlotChildren().add(bubble);
        }
    }

    @Override
    protected void dataItemRemoved(final Data<X, Y> item, final Series<X, Y> series) {
        final Node bubble = item.getNode();
        if (shouldAnimate()) {
            // fade out old bubble
            FadeTransition ft = new FadeTransition(Duration.millis(500), bubble);
            ft.setToValue(0);
            ft.setOnFinished(actionEvent -> {
                getPlotChildren().remove(bubble);
                removeDataItemFromDisplay(series, item);
                bubble.setOpacity(1.0);
            });
            ft.play();
        } else {
            getPlotChildren().remove(bubble);
            removeDataItemFromDisplay(series, item);
        }
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
        // handle any data already in series
        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            Node bubble = createBubble(series, seriesIndex, item, j);
            if (shouldAnimate()) {
                bubble.setOpacity(0);
                getPlotChildren().add(bubble);
                // fade in new bubble
                FadeTransition ft = new FadeTransition(Duration.millis(500), bubble);
                ft.setToValue(1);
                ft.play();
            } else {
                getPlotChildren().add(bubble);
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
        // remove all bubble nodes
        if (shouldAnimate()) {
            ParallelTransition pt = new ParallelTransition();
            pt.setOnFinished(event -> {
                removeSeriesFromDisplay(series);
            });
            for (MultiAxisBubbleChart.Data<X, Y> d : series.getData()) {
                final Node bubble = d.getNode();
                // fade out old bubble
                FadeTransition ft = new FadeTransition(Duration.millis(500), bubble);
                ft.setToValue(0);
                ft.setOnFinished(actionEvent -> {
                    getPlotChildren().remove(bubble);
                    bubble.setOpacity(1.0);
                });
                pt.getChildren().add(ft);
            }
            pt.play();
        } else {
            for (MultiAxisBubbleChart.Data<X, Y> d : series.getData()) {
                final Node bubble = d.getNode();
                getPlotChildren().remove(bubble);
            }
            removeSeriesFromDisplay(series);
        }
    }

    private Node createBubble(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {
        Node bubble = item.getNode();
        // check if bubble has already been created
        if (bubble == null) {
            bubble = new StackPane();
            item.setNode(bubble);
        }
        // set bubble styles
        bubble.getStyleClass().setAll("chart-bubble", "series" + seriesIndex, "data" + itemIndex,
                DEFAULT_COLOR + (seriesIndex % 8));
        return bubble;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void layoutPlotChildren() {
        //get max size
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
            Series<X, Y> series = getData().get(seriesIndex);
            Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
            while (iter.hasNext()) {
                Data<X, Y> item = iter.next();
                double doubleValue = getDoubleValue(item.getExtraValue(), 0);
                max = Math.max(doubleValue, max);
                min = Math.min(doubleValue, min);
            }
        }
        if (min == 1) min = min + 1;
        double factor = 1 / max;

        // update bubble positions
        for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
            Series<X, Y> series = getData().get(seriesIndex);
//            for (Data<X,Y> item = series.begin; item != null; item = item.next) {
            Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
            while (iter.hasNext()) {
                Data<X, Y> item = iter.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getY1Axis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node bubble = item.getNode();
                Circle circle;
                if (bubble != null) {
                    if (bubble instanceof StackPane) {
                        StackPane region = (StackPane) item.getNode();
                        if (region.getShape() == null) {
                            circle = new Circle(getDoubleValue(item.getExtraValue(), 1));
                        } else if (region.getShape() instanceof Circle) {
                            circle = (Circle) region.getShape();
                        } else {
                            return;
                        }

                        double r = getDoubleValue(item.getExtraValue(), 1) * factor * 90;
                        if (r < 30) {
                            r = 30;
                        }
                        circle.setRadius(r);

//                        circle.setRadius(getDoubleValue(item.getExtraValue(), 1) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getXAxis()).getScale()) : 1));
                        // Note: workaround for RT-7689 - saw this in ProgressControlSkin
                        // The region doesn't update itself when the shape is mutated in place, so we
                        // null out and then restore the shape in order to force invalidation.
                        region.setShape(null);
                        region.setShape(circle);
                        region.setScaleShape(false);
                        region.setCenterShape(false);
                        region.setCacheShape(false);
                        // position the bubble
                        bubble.setLayoutX(x);
                        bubble.setLayoutY(y);
                    }
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
                legenditem.getSymbol().getStyleClass().addAll("series" + seriesIndex, "chart-bubble",
                        "bubble-legend-symbol", DEFAULT_COLOR + (seriesIndex % 8));
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

    @Override
    public WeightedObservedPoints getWeightedObservedPoints(Series<X, Y> s, int yAxisIndex) {
        double zero = getData().get(getData().indexOf(s)).getData().stream().findFirst().map(xyData -> getValue(xyData.getXValue())).orElse(0.0);
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        double index = 0;
        for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(s); it.hasNext(); ) {
            Data<X, Y> item = it.next();
            if (getXAxis() instanceof CategoryAxis) {
                double x = getValue(index++);
                double y = getValue(item.getCurrentY());
                obs.add(x, y);
            } else if (getValue(item.getCurrentX()) >= ((ValueAxis) getXAxis()).getLowerBound()
                    && getValue(item.getCurrentX()) <= ((ValueAxis) getXAxis()).getUpperBound()) {
                double x = (getValue(item.getCurrentX()) - zero) / 1000 / 60;
                double y = getValue(item.getCurrentY());
                obs.add(x, y);
            }
        }
        return obs;
    }

    @Override
    public Path calcPolyRegression(Series<X, Y> s, int yAxisIndex, int polyDegree) {
        if (yAxisIndex == Y2_AXIS && getY2Axis() == null)
            throw new NullPointerException("Y2 Axis is not defined.");

        Axis yAxis = yAxisIndex == Y2_AXIS ? getY2Axis() : getY1Axis();

        final WeightedObservedPoints obs = new WeightedObservedPoints();

        double index = 0;
        for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(s); it.hasNext(); ) {
            Data<X, Y> item = it.next();
            if (getXAxis() instanceof CategoryAxis) {
                double x = getValue(index++);
                double y = getValue(item.getCurrentY());
                obs.add(x, y);
            } else if (getValue(item.getCurrentX()) >= ((ValueAxis) getXAxis()).getLowerBound()
                    && getValue(item.getCurrentX()) <= ((ValueAxis) getXAxis()).getUpperBound()) {
                double x1 = getValue(item.getCurrentX());
                double y = getValue(item.getCurrentY());
                obs.add(x1, y);
            }
        }

        if (obs.toList().size() == 0)
            return new Path();

        double xMax = Double.MIN_VALUE;
        double xMin = Double.MAX_VALUE;

        for (WeightedObservedPoint p : obs.toList()) {
            if (p.getX() > xMax) {
                xMax = p.getX();
            }
            if (p.getX() < xMin) {
                xMin = p.getX();
            }
        }

        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(polyDegree);
        final double[] coefficient;
        try {
            coefficient = fitter.fit(obs.toList());
        } catch (Exception e) {
            return new Path();
        }

        if (obs.toList().size() < 2) {
            return null;
        } else {

            Path path = new Path();
            path.setStrokeWidth(2);

            MoveTo moveTo = new MoveTo();

            if (this.getXAxis() instanceof CategoryAxis) {
                moveTo.setX(findXCategoryChartCord(xMin, xMin, xMax));
            } else {
                moveTo.setX(findXChartCord(xMin));
            }

            if (polyDegree == DEGREE_NUM0) {
                moveTo.setY(findYChartCord(obs.toList().remove(0).getY(), yAxis));

                path.getElements().add(moveTo);
                List<WeightedObservedPoint> weightedObservedPoints = obs.toList();
                weightedObservedPoints.sort(Comparator.comparingDouble(WeightedObservedPoint::getX));
                for (WeightedObservedPoint p : weightedObservedPoints) {

                    double xValue = p.getX();
                    double yValue = p.getY();

                    LineTo lineTo = new LineTo();
                    if (this.getXAxis() instanceof CategoryAxis) {
                        lineTo.setX(findXCategoryChartCord(xValue, xMin, xMax));
                    } else {
                        lineTo.setX(findXChartCord(xValue));
                    }
                    lineTo.setY(findYChartCord(yValue, yAxis));

                    path.getElements().add(lineTo);
                }

            } else {

                moveTo.setY(findYChartCord(getYFromFitter(coefficient, xMin), yAxis));

                path.getElements().add(moveTo);

                List<WeightedObservedPoint> weightedObservedPoints = obs.toList();
                weightedObservedPoints.sort(Comparator.comparingDouble(WeightedObservedPoint::getX));
                Map<Double, Double> xyMap = new HashMap<>();
                weightedObservedPoints.forEach(weightedObservedPoint -> xyMap.put(weightedObservedPoint.getX(), weightedObservedPoint.getY()));
                List<WeightedObservedPoint> newList = new ArrayList<>();
                Double lastX = weightedObservedPoints.get(weightedObservedPoints.size() - 1).getX();
                Double firstX = weightedObservedPoints.get(0).getX();
                long diff = lastX.longValue() - firstX.longValue();
                for (int i = 0; i < diff; i++) {
                    if (xyMap.get(firstX + i) != null) {
                        newList.add(new WeightedObservedPoint(1, firstX + i, xyMap.get(firstX + i)));
                    } else {
                        newList.add(new WeightedObservedPoint(1, firstX + i, 0));
                    }
                }

                for (WeightedObservedPoint regressionPoint : newList) {
                    LineTo lineTo = new LineTo();
                    double x = regressionPoint.getX();
                    if (this.getXAxis() instanceof CategoryAxis) {
                        lineTo.setX(findXCategoryChartCord(x, xMin, xMax));
                    } else {
                        lineTo.setX(findXChartCord(x));
                    }
                    lineTo.setY(findYChartCord(getYFromFitter(coefficient, x), yAxis));

                    path.getElements().add(lineTo);
                }
            }
            return path;
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