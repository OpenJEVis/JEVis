package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import com.sun.javafx.collections.NonIterableChange;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleableBooleanProperty;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.*;

public abstract class MultiAxisChart<X, Y> extends Chart {

    // -------------- PRIVATE FIELDS -------------------------------------

    public static final int Y1_AXIS = 0;
    public static final int Y2_AXIS = 1;

    public static final int NONE = -1;
    public static final int DEGREE_NUM0 = 0;
    public static final int DEGREE_NUM1 = 1;
    public static final int DEGREE_NUM2 = 2;
    public static final int DEGREE_NUM3 = 3;
    public static final int DEGREE_NUM4 = 4;
    public static final int DEGREE_NUM5 = 5;
    public static final int DEGREE_NUM6 = 6;
    public static final int DEGREE_NUM7 = 7;
    static String DEFAULT_COLOR = "default-color";
    final Map<MultiAxisChart.Series<X, Y>, Integer> seriesColorMap = new HashMap<>();
    // to indicate which colors are being used for the series
    private final BitSet colorBits = new BitSet(8);
    private final Line verticalZeroLine = new Line();
    private final Line horizontalZeroLine = new Line();
    private final Path verticalGridLines = new Path();
    private final Path horizontalGridLines = new Path();
    private final Path horizontalRowFill = new Path();
    private final Path verticalRowFill = new Path();
    private final Region plotBackground = new Region();
    private final Group plotArea = new Group() {
        @Override
        public void requestLayout() {
        } // suppress layout requests
    };
    private final Group plotContent = new Group();
    private final Rectangle plotAreaClip = new Rectangle();
    private final List<MultiAxisChart.Series<X, Y>> displayedSeries = new ArrayList<>();
    private final Axis<X> xAxis;
    private final Axis<Y> y1Axis;
    private final Axis<Y> y2Axis;
    public String[] y1RegressionSeriesColors = {"#f3622d", "#fba71b", "#57b757", "#41a9c9", "#4258c9", "#9a42c8",
            "#c84164", "#888888"};
    public String[] y2RegressionSeriesColors = {"#f3622d", "#fba71b", "#57b757", "#41a9c9", "#4258c9", "#9a42c8",
            "#c84164", "#888888"};
    private boolean hasY1AxisRegression;
    private boolean hasY2AxisRegression;
    private int y1AxisRegressionType;
    private int y2AxisRegressionType;
    private ArrayList<Shape> y1RegressionLines = new ArrayList<>();

    // -------------- PUBLIC PROPERTIES -------------------------------------
    private ArrayList<Shape> y2RegressionLines = new ArrayList<>();
    private boolean rangeValid = false;
    /**
     * This is called when a series is added or removed from the chart
     */
    private final ListChangeListener<MultiAxisChart.Series<X, Y>> seriesChanged = c -> {
        ObservableList<? extends MultiAxisChart.Series<X, Y>> series = c.getList();
        while (c.next()) {

            if (c.wasPermutated()) {
                displayedSeries.sort((o1, o2) -> series.indexOf(o2) - series.indexOf(o1));
            }

            if (c.getRemoved().size() > 0)
                updateLegend();

            Set<MultiAxisChart.Series<X, Y>> dupCheck = new HashSet<>(displayedSeries);
            dupCheck.removeAll(c.getRemoved());
            for (MultiAxisChart.Series<X, Y> d : c.getAddedSubList()) {
                if (!dupCheck.add(d)) {
                    throw new IllegalArgumentException("Duplicate series added");
                }
            }

            for (MultiAxisChart.Series<X, Y> s : c.getRemoved()) {
                s.setToRemove = true;
                seriesRemoved(s);
                int idx = seriesColorMap.remove(s);
                colorBits.clear(idx);
            }

            for (int i = c.getFrom(); i < c.getTo() && !c.wasPermutated(); i++) {
                final MultiAxisChart.Series<X, Y> s = c.getList().get(i);
                // add new listener to data
                s.setChart(MultiAxisChart.this);
                if (s.setToRemove) {
                    s.setToRemove = false;
                    s.getChart().seriesBeingRemovedIsAdded(s);
                }
                // update linkedList Pointers for series
                displayedSeries.add(s);
                // update default color style class
                int nextClearBit = colorBits.nextClearBit(0);
                colorBits.set(nextClearBit, true);
                s.defaultColorStyleClass = DEFAULT_COLOR + (nextClearBit % 8);
                seriesColorMap.put(s, nextClearBit % 8);
                // inform sub-classes of series added
                seriesAdded(s, i);
            }
            if (c.getFrom() < c.getTo())
                updateLegend();
            seriesChanged(c);

        }
        // update axis ranges
        invalidateRange();
        // lay everything out
        requestChartLayout();
    };
    /**
     * MultiAxisCharts data
     */
    private ObjectProperty<ObservableList<MultiAxisChart.Series<X, Y>>> data = new ObjectPropertyBase<ObservableList<MultiAxisChart.Series<X, Y>>>() {
        private ObservableList<MultiAxisChart.Series<X, Y>> old;

        @Override
        protected void invalidated() {
            final ObservableList<MultiAxisChart.Series<X, Y>> current = getValue();
            int saveAnimationState = -1;
            // add remove listeners
            if (old != null) {
                old.removeListener(seriesChanged);
                // Set animated to false so we don't animate both remove and add
                // at the same time. RT-14163
                // RT-21295 - disable animated only when current is also not null.
                if (current != null && old.size() > 0) {
                    saveAnimationState = (old.get(0).getChart().getAnimated()) ? 1 : 2;
                    old.get(0).getChart().setAnimated(false);
                }
            }
            if (current != null)
                current.addListener(seriesChanged);
            // fire series change event if series are added or removed
            if (old != null || current != null) {
                final List<MultiAxisChart.Series<X, Y>> removed = (old != null) ? old
                        : Collections.emptyList();
                final int toIndex = (current != null) ? current.size() : 0;
                // let series listener know all old series have been removed and new that have
                // been added
                if (toIndex > 0 || !removed.isEmpty()) {
                    seriesChanged.onChanged(new NonIterableChange<MultiAxisChart.Series<X, Y>>(0, toIndex, current) {
                        @Override
                        public List<MultiAxisChart.Series<X, Y>> getRemoved() {
                            return removed;
                        }

                        @Override
                        protected int[] getPermutation() {
                            return new int[0];
                        }
                    });
                }
            } else if (old != null && old.size() > 0) {
                // let series listener know all old series have been removed
                seriesChanged.onChanged(new NonIterableChange<MultiAxisChart.Series<X, Y>>(0, 0, current) {
                    @Override
                    public List<MultiAxisChart.Series<X, Y>> getRemoved() {
                        return old;
                    }

                    @Override
                    protected int[] getPermutation() {
                        return new int[0];
                    }
                });
            }
            // restore animated on chart.
            if (current != null && current.size() > 0 && saveAnimationState != -1) {
                current.get(0).getChart().setAnimated(saveAnimationState == 1);
            }
            old = current;
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "data";
        }
    };
    /**
     * True if vertical grid lines should be drawn
     */
    private BooleanProperty verticalGridLinesVisible = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "verticalGridLinesVisible";
        }

        @Override
        public CssMetaData<MultiAxisChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };
    /**
     * True if horizontal grid lines should be drawn
     */
    private BooleanProperty horizontalGridLinesVisible = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "horizontalGridLinesVisible";
        }

        @Override
        public CssMetaData<MultiAxisChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };
    /**
     * If true then alternative vertical columns will have fills
     */
    private BooleanProperty alternativeColumnFillVisible = new StyleableBooleanProperty(false) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "alternativeColumnFillVisible";
        }

        @Override
        public CssMetaData<MultiAxisChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };
    /**
     * If true then alternative horizontal rows will have fills
     */
    private BooleanProperty alternativeRowFillVisible = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "alternativeRowFillVisible";
        }

        @Override
        public CssMetaData<MultiAxisChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };
    /**
     * If this is true and the vertical axis has both positive and negative values
     * then a additional axis line will be drawn at the zero point
     *
     * @defaultValue true
     */
    private BooleanProperty verticalZeroLineVisible = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "verticalZeroLineVisible";
        }

        @Override
        public CssMetaData<MultiAxisChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };
    /**
     * If this is true and the horizontal axis has both positive and negative values
     * then a additional axis line will be drawn at the zero point
     *
     * @defaultValue true
     */
    private BooleanProperty horizontalZeroLineVisible = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            requestChartLayout();
        }

        @Override
        public Object getBean() {
            return MultiAxisChart.this;
        }

        @Override
        public String getName() {
            return "horizontalZeroLineVisible";
        }

        @Override
        public CssMetaData<MultiAxisChart<?, ?>, Boolean> getCssMetaData() {
            return null;
        }
    };

    /**
     * Constructs a MultiAxisChart given the two axes. The initial content for the
     * chart plot background and plot area that includes vertical and horizontal
     * grid lines and fills, are added.
     *
     * @param xAxis  X Axis for this XY chart
     * @param y1Axis Y1 Axis for this XY chart
     * @param y2Axis Y2 Axis for this XY chart
     */
    public MultiAxisChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis) {
        this.xAxis = xAxis;
        if (xAxis.getSide() == null)
            xAxis.setSide(Side.BOTTOM);

        this.y1Axis = y1Axis;
        if (y1Axis.getSide() == null)
            y1Axis.setSide(Side.LEFT);

        this.y2Axis = y2Axis;
        if (y2Axis != null && y2Axis.getSide() == null)
            y2Axis.setSide(Side.RIGHT);

        if (y2Axis != null) {
            y2Axis.visibleProperty().addListener(e -> {
                layoutPlotChildren();
            });

            y2Axis.autoRangingProperty().addListener((ov, t, t1) -> {
                updateAxisRange();
            });
            getChartChildren().add(y2Axis);
        }

        // RT-23123 autoranging leads to charts incorrect appearance.
        xAxis.autoRangingProperty().addListener((ov, t, t1) -> {
            updateAxisRange();
        });

        y1Axis.autoRangingProperty().addListener((ov, t, t1) -> {
            updateAxisRange();
        });

        // add initial content to chart content
        getChartChildren().addAll(plotBackground, plotArea, xAxis, y1Axis);
        // We don't want plotArea or plotContent to autoSize or do layout
        plotArea.setAutoSizeChildren(false);
        plotContent.setAutoSizeChildren(false);
        // setup clipping on plot area
        plotAreaClip.setSmooth(false);
        plotArea.setClip(plotAreaClip);
        // add children to plot area
        plotArea.getChildren().addAll(verticalRowFill, horizontalRowFill, verticalGridLines, horizontalGridLines,
                verticalZeroLine, horizontalZeroLine, plotContent);
        // setup css style classes
        plotContent.getStyleClass().setAll("plot-content");
        plotBackground.getStyleClass().setAll("chart-plot-background");
        verticalRowFill.getStyleClass().setAll("chart-alternative-column-fill");
        horizontalRowFill.getStyleClass().setAll("chart-alternative-row-fill");
        verticalGridLines.getStyleClass().setAll("chart-vertical-grid-lines");
        horizontalGridLines.getStyleClass().setAll("chart-horizontal-grid-lines");
        verticalZeroLine.getStyleClass().setAll("chart-vertical-zero-line");
        horizontalZeroLine.getStyleClass().setAll("chart-horizontal-zero-line");
        // mark plotContent as unmanaged as its preferred size changes do not effect our
        // layout
        plotContent.setManaged(false);
        plotArea.setManaged(false);
        // listen to animation on/off and sync to axis
        animatedProperty().addListener((valueModel, oldValue, newValue) -> {
            if (getXAxis() != null)
                getXAxis().setAnimated(newValue);
            if (getY1Axis() != null)
                getY1Axis().setAnimated(newValue);
            if (getY2Axis() != null)
                getY2Axis().setAnimated(newValue);
        });

    }

    /**
     * Get the X axis, by default it is along the bottom of the plot
     */
    public Axis<X> getXAxis() {
        return xAxis;
    }

    /**
     * Get the Y axis, by default it is along the left of the plot
     */
    public Axis<Y> getY1Axis() {
        return y1Axis;
    }

    /**
     * Get the Y axis, by default it is along the left of the plot
     */
    public Axis<Y> getY2Axis() {
        return y2Axis;
    }

    public final ObservableList<MultiAxisChart.Series<X, Y>> getData() {
        return data.getValue();
    }

    public final void setData(ObservableList<MultiAxisChart.Series<X, Y>> value) {
        data.setValue(value);
    }

    public final ObjectProperty<ObservableList<MultiAxisChart.Series<X, Y>>> dataProperty() {
        return data;
    }

    /**
     * Indicates whether vertical grid lines are visible or not.
     *
     * @return true if verticalGridLines are visible else false.
     * @see #verticalGridLinesVisible
     */
    public final boolean getVerticalGridLinesVisible() {
        return verticalGridLinesVisible.get();
    }

    public final void setVerticalGridLinesVisible(boolean value) {
        verticalGridLinesVisible.set(value);
    }

    public final BooleanProperty verticalGridLinesVisibleProperty() {
        return verticalGridLinesVisible;
    }

    public final boolean isHorizontalGridLinesVisible() {
        return horizontalGridLinesVisible.get();
    }

    public final void setHorizontalGridLinesVisible(boolean value) {
        horizontalGridLinesVisible.set(value);
    }

    public final BooleanProperty horizontalGridLinesVisibleProperty() {
        return horizontalGridLinesVisible;
    }

    public final boolean isAlternativeColumnFillVisible() {
        return alternativeColumnFillVisible.getValue();
    }

    public final void setAlternativeColumnFillVisible(boolean value) {
        alternativeColumnFillVisible.setValue(value);
    }

    public final BooleanProperty alternativeColumnFillVisibleProperty() {
        return alternativeColumnFillVisible;
    }

    public final boolean isAlternativeRowFillVisible() {
        return alternativeRowFillVisible.getValue();
    }

    public final void setAlternativeRowFillVisible(boolean value) {
        alternativeRowFillVisible.setValue(value);
    }

    public final BooleanProperty alternativeRowFillVisibleProperty() {
        return alternativeRowFillVisible;
    }

    public final boolean isVerticalZeroLineVisible() {
        return verticalZeroLineVisible.get();
    }

    public final void setVerticalZeroLineVisible(boolean value) {
        verticalZeroLineVisible.set(value);
    }

    public final BooleanProperty verticalZeroLineVisibleProperty() {
        return verticalZeroLineVisible;
    }

    public final boolean isHorizontalZeroLineVisible() {
        return horizontalZeroLineVisible.get();
    }

    public final void setHorizontalZeroLineVisible(boolean value) {
        horizontalZeroLineVisible.set(value);
    }

    public final BooleanProperty horizontalZeroLineVisibleProperty() {
        return horizontalZeroLineVisible;
    }

    // -------------- PROTECTED PROPERTIES ------------------------

    /**
     * Creates an array of KeyFrames for fading out nodes representing a series
     *
     * @param series      The series to remove
     * @param fadeOutTime Time to fade out, in milliseconds
     * @return array of two KeyFrames from zero to fadeOutTime
     */
    final KeyFrame[] createSeriesRemoveTimeLine(Series<X, Y> series, long fadeOutTime) {
        final List<Node> nodes = new ArrayList<>();
        nodes.add(series.getNode());
        for (Data<X, Y> d : series.getData()) {
            if (d.getNode() != null) {
                nodes.add(d.getNode());
            }
        }
        // fade out series node and symbols
        KeyValue[] startValues = new KeyValue[nodes.size()];
        KeyValue[] endValues = new KeyValue[nodes.size()];
        for (int j = 0; j < nodes.size(); j++) {
            startValues[j] = new KeyValue(nodes.get(j).opacityProperty(), 1);
            endValues[j] = new KeyValue(nodes.get(j).opacityProperty(), 0);
        }
        return new KeyFrame[]{new KeyFrame(Duration.ZERO, startValues),
                new KeyFrame(Duration.millis(fadeOutTime), actionEvent -> {
                    getPlotChildren().removeAll(nodes);
                    removeSeriesFromDisplay(series);
                }, endValues)};
    }

    // -------------- CONSTRUCTOR ---------------------------------

    /**
     * Modifiable and observable list of all content in the plot. This is where
     * implementations of MultiAxisChart should add any nodes they use to draw their
     * plot.
     *
     * @return Observable list of plot children
     */
    protected ObservableList<Node> getPlotChildren() {
        return plotContent.getChildren();
    }

    // -------------- METHODS -------------------------------------

    protected final Iterator<Data<X, Y>> getDisplayedDataIterator(final MultiAxisChart.Series<X, Y> series) {
        return Collections.unmodifiableList(series.displayedData).iterator();
    }

    protected final void removeDataItemFromDisplay(MultiAxisChart.Series<X, Y> series, Data<X, Y> item) {
        series.removeDataItemRef(item);
    }

    protected final void removeSeriesFromDisplay(MultiAxisChart.Series<X, Y> series) {
        if (series != null)
            series.setToRemove = false;
        series.setChart(null);
        displayedSeries.remove(series);
    }

    /**
     * Gets the size of the data returning 0 if the data is null
     *
     * @return The number of items in data, or null if data is null
     */
    public final int getDataSize() {
        final ObservableList<MultiAxisChart.Series<X, Y>> data = getData();
        return (data != null) ? data.size() : 0;
    }

    /**
     * Called when a series's name has changed
     */
    private void seriesNameChanged() {
        updateLegend();
        requestChartLayout();
    }

    private void dataItemsChanged(MultiAxisChart.Series<X, Y> series, List<Data<X, Y>> removed, int addedFrom,
                                  int addedTo, boolean permutation) {
        for (Data<X, Y> item : removed) {
            dataItemRemoved(item, series);
        }
        for (int i = addedFrom; i < addedTo; i++) {
            Data<X, Y> item = series.getData().get(i);
            dataItemAdded(series, i, item);
        }
        invalidateRange();
        requestChartLayout();
    }

    private void dataXValueChanged(Data<X, Y> item) {
        if (item.getCurrentX() != item.getXValue()) {
            invalidateRange();
        }
        dataItemChanged(item);

        item.setCurrentX(item.getXValue());
        requestChartLayout();

    }

    private void dataYValueChanged(Data<X, Y> item) {
        if (item.getCurrentY() != item.getYValue())
            invalidateRange();
        dataItemChanged(item);

        item.setCurrentY(item.getYValue());
        requestChartLayout();

    }

    private void dataExtraValueChanged(Data<X, Y> item) {
        if (item.getCurrentY() != item.getYValue())
            invalidateRange();
        dataItemChanged(item);

        item.setCurrentY(item.getYValue());
        requestChartLayout();

    }

    /**
     * Called to update and layout the plot children. This should include all work
     * to updates nodes representing the plot on top of the axis and grid lines etc.
     * The origin is the top left of the plot area, the plot area with can be got by
     * getting the width of the x axis and its height from the height of the y axis.
     */
    protected abstract void layoutPlotChildren();

    /**
     * This is called whenever a series is added or removed and the legend needs to
     * be updated
     */
    protected void updateLegend() {
    }

    /**
     * This method is called when there is an attempt to add series that was set to
     * be removed, and the removal might not have completed.
     *
     * @param series
     */
    void seriesBeingRemovedIsAdded(MultiAxisChart.Series<X, Y> series) {
    }

    /**
     * This method is called when there is an attempt to add a Data item that was
     * set to be removed, and the removal might not have completed.
     *
     * @param item
     */
    void dataBeingRemovedIsAdded(Data<X, Y> item, MultiAxisChart.Series<X, Y> series) {
    }

    /**
     * Called when a data item has been added to a series. This is where
     * implementations of MultiAxisChart can create/add new nodes to getPlotChildren
     * to represent this data item. They also may animate that data add with a fade
     * in or similar if animated = true.
     *
     * @param series    The series the data item was added to
     * @param itemIndex The index of the new item within the series
     * @param item      The new data item that was added
     */
    protected abstract void dataItemAdded(MultiAxisChart.Series<X, Y> series, int itemIndex, Data<X, Y> item);

    /**
     * Called when a data item has been removed from data model but it is still
     * visible on the chart. Its still visible so that you can handle animation for
     * removing it in this method. After you are done animating the data item you
     * must call removeDataItemFromDisplay() to remove the items node from being
     * displayed on the chart.
     *
     * @param item   The item that has been removed from the series
     * @param series The series the item was removed from
     */
    protected abstract void dataItemRemoved(Data<X, Y> item, MultiAxisChart.Series<X, Y> series);

    /**
     * Called when a data item has changed, ie its xValue, yValue or extraValue has
     * changed.
     *
     * @param item The data item who was changed
     */
    protected abstract void dataItemChanged(Data<X, Y> item);

    /**
     * A series has been added to the charts data model. This is where
     * implementations of MultiAxisChart can create/add new nodes to getPlotChildren
     * to represent this series. Also you have to handle adding any data items that
     * are already in the series. You may simply call dataItemAdded() for each one
     * or provide some different animation for a whole series being added.
     *
     * @param series      The series that has been added
     * @param seriesIndex The index of the new series
     */
    protected abstract void seriesAdded(MultiAxisChart.Series<X, Y> series, int seriesIndex);

    /**
     * A series has been removed from the data model but it is still visible on the
     * chart. Its still visible so that you can handle animation for removing it in
     * this method. After you are done animating the data item you must call
     * removeSeriesFromDisplay() to remove the series from the display list.
     *
     * @param series The series that has been removed
     */
    protected abstract void seriesRemoved(MultiAxisChart.Series<X, Y> series);

    /**
     * Called when each atomic change is made to the list of series for this chart
     */
    protected void seriesChanged(Change<? extends MultiAxisChart.Series<X, Y>> c) {
    }

    /**
     * This is called when a data change has happened that may cause the range to be
     * invalid.
     */
    private void invalidateRange() {
        rangeValid = false;
    }

    /**
     * This is called when the range has been invalidated and we need to update it.
     * If the axis are auto ranging then we compile a list of all data that the
     * given axis has to plot and call invalidateRange() on the axis passing it that
     * data.
     */
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
            if (xData != null)
                xa.invalidateRange(xData);
            if (y1Data != null)
                y1a.invalidateRange(y1Data);
            if (y2Data != null)
                y2a.invalidateRange(y2Data);
        }
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        if (getData() == null)
            return;
        if (!rangeValid) {
            rangeValid = true;
            if (getData() != null)
                updateAxisRange();
        }
        // snap top and left to pixels
        top = snapPosition(top);
        left = snapPosition(left);
        // get starting stuff
        final Axis<X> xa = getXAxis();
        final ObservableList<Axis.TickMark<X>> xaTickMarks = xa.getTickMarks();
        final Axis<Y> ya = getY1Axis();
        final ObservableList<Axis.TickMark<Y>> yaTickMarks = ya.getTickMarks();
        final Axis<Y> y2a = getY2Axis();

        // check we have 2 axises and know their sides
        if (xa == null || ya == null)
            return;
        // try and work out width and height of axises
        double xAxisWidth = 0;
        double xAxisHeight = 30; // guess x axis height to start with
        double yAxisWidth = 0;
        double yAxisHeight = 0;

        double y2AxisWidth = 0;
        double y2AxisHeight = 0;

        for (int count = 0; count < 5; count++) {
            yAxisHeight = snapSize(height - xAxisHeight);
            if (yAxisHeight < 0) {
                yAxisHeight = 0;
            }
            yAxisWidth = ya.prefWidth(yAxisHeight);

            y2AxisHeight = snapSize(height - xAxisHeight);
            if (y2AxisHeight < 0) {
                y2AxisHeight = 0;
            }
            if (y2a != null)
                y2AxisWidth = y2a.prefWidth(y2AxisHeight);

            xAxisWidth = snapSize(width - yAxisWidth - y2AxisWidth);
            if (xAxisWidth < 0) {
                xAxisWidth = 0;
            }

            double newXAxisHeight = xa.prefHeight(xAxisWidth);
            if (newXAxisHeight == xAxisHeight)
                break;
            xAxisHeight = newXAxisHeight;
        }
        // round axis sizes up to whole integers to snap to pixel
        xAxisWidth = Math.ceil(xAxisWidth);
        xAxisHeight = Math.ceil(xAxisHeight);
        yAxisWidth = Math.ceil(yAxisWidth);
        yAxisHeight = Math.ceil(yAxisHeight);
        y2AxisWidth = Math.ceil(y2AxisWidth);
        y2AxisHeight = Math.ceil(y2AxisHeight);

        // calc xAxis height
        double xAxisY = 0;
        xa.setVisible(true);
        xAxisY = top + yAxisHeight;

        // calc yAxis width
        double yAxisX = 0;
        ya.setVisible(true);
        yAxisX = left + 1;
        left += yAxisWidth;

        xAxisWidth = width - y2AxisWidth - left;

        // TODO : Check again the approach below
        // resize axises
        xa.resizeRelocate(left, xAxisY, xAxisWidth, xAxisHeight);
        ya.resizeRelocate(yAxisX, top, yAxisWidth, yAxisHeight);
        if (y2a != null)
            y2a.resizeRelocate(width - y2AxisWidth, top, y2AxisWidth, y2AxisHeight);

        // When the chart is resized, need to specifically call out the axises
        // to lay out as they are unmanaged.
        xa.requestAxisLayout();
        xa.layout();
        ya.requestAxisLayout();
        ya.layout();
        if (y2a != null) {
            y2a.requestAxisLayout();
            y2a.layout();
        }

        // layout plot content
        layoutPlotChildren();
        // get axis zero points
        final double xAxisZero = xa.getZeroPosition();
        final double yAxisZero = ya.getZeroPosition();
        // position vertical and horizontal zero lines
        if (Double.isNaN(xAxisZero) || !isVerticalZeroLineVisible()) {
            verticalZeroLine.setVisible(false);
        } else {
            verticalZeroLine.setStartX(left + xAxisZero + 0.5);
            verticalZeroLine.setStartY(top);
            verticalZeroLine.setEndX(left + xAxisZero + 0.5);
            verticalZeroLine.setEndY(top + yAxisHeight);
            verticalZeroLine.setVisible(true);
        }
        if (Double.isNaN(yAxisZero) || !isHorizontalZeroLineVisible()) {
            horizontalZeroLine.setVisible(false);
        } else {
            horizontalZeroLine.setStartX(left);
            horizontalZeroLine.setStartY(top + yAxisZero + 0.5);
            horizontalZeroLine.setEndX(left + xAxisWidth);
            horizontalZeroLine.setEndY(top + yAxisZero + 0.5);
            horizontalZeroLine.setVisible(true);
        }
        // layout plot background
        plotBackground.resizeRelocate(left, top, xAxisWidth, yAxisHeight);
        // update clip
        plotAreaClip.setX(left);
        plotAreaClip.setY(top);
        plotAreaClip.setWidth(xAxisWidth + 1);
        plotAreaClip.setHeight(yAxisHeight + 1);
        // plotArea.setClip(new Rectangle(left, top, xAxisWidth, yAxisHeight));
        // position plot group, its origin is the bottom left corner of the plot area
        plotContent.setLayoutX(left);
        plotContent.setLayoutY(top);
        plotContent.requestLayout(); // Note: not sure this is right, maybe plotContent should be resizeable
        // update vertical grid lines
        verticalGridLines.getElements().clear();
        if (getVerticalGridLinesVisible()) {
            for (int i = 0; i < xaTickMarks.size(); i++) {
                Axis.TickMark<X> tick = xaTickMarks.get(i);
                final double x = xa.getDisplayPosition(tick.getValue());
                if ((x != xAxisZero || !isVerticalZeroLineVisible()) && x > 0 && x <= xAxisWidth) {
                    verticalGridLines.getElements().add(new MoveTo(left + x + 0.5, top));
                    verticalGridLines.getElements().add(new LineTo(left + x + 0.5, top + yAxisHeight));
                }
            }
        }
        // update horizontal grid lines
        horizontalGridLines.getElements().clear();
        if (isHorizontalGridLinesVisible()) {
            for (int i = 0; i < yaTickMarks.size(); i++) {
                Axis.TickMark<Y> tick = yaTickMarks.get(i);
                final double y = ya.getDisplayPosition(tick.getValue());
                if ((y != yAxisZero || !isHorizontalZeroLineVisible()) && y >= 0 && y < yAxisHeight) {
                    horizontalGridLines.getElements().add(new MoveTo(left, top + y + 0.5));
                    horizontalGridLines.getElements().add(new LineTo(left + xAxisWidth, top + y + 0.5));
                }
            }
        }
        // Note: is there a more efficient way to calculate horizontal and vertical row
        // fills?
        // update vertical row fill
        verticalRowFill.getElements().clear();
        if (isAlternativeColumnFillVisible()) {
            // tick marks are not sorted so get all the positions and sort them
            final List<Double> tickPositionsPositive = new ArrayList<Double>();
            final List<Double> tickPositionsNegative = new ArrayList<Double>();
            for (int i = 0; i < xaTickMarks.size(); i++) {
                double pos = xa.getDisplayPosition(xaTickMarks.get(i).getValue());
                if (pos == xAxisZero) {
                    tickPositionsPositive.add(pos);
                    tickPositionsNegative.add(pos);
                } else if (pos < xAxisZero) {
                    tickPositionsPositive.add(pos);
                } else {
                    tickPositionsNegative.add(pos);
                }
            }
            Collections.sort(tickPositionsPositive);
            Collections.sort(tickPositionsNegative);
            // iterate over every pair of positive tick marks and create fill
            for (int i = 1; i < tickPositionsPositive.size(); i += 2) {
                if ((i + 1) < tickPositionsPositive.size()) {
                    final double x1 = tickPositionsPositive.get(i);
                    final double x2 = tickPositionsPositive.get(i + 1);
                    verticalRowFill.getElements().addAll(new MoveTo(left + x1, top),
                            new LineTo(left + x1, top + yAxisHeight), new LineTo(left + x2, top + yAxisHeight),
                            new LineTo(left + x2, top), new ClosePath());
                }
            }
            // iterate over every pair of positive tick marks and create fill
            for (int i = 0; i < tickPositionsNegative.size(); i += 2) {
                if ((i + 1) < tickPositionsNegative.size()) {
                    final double x1 = tickPositionsNegative.get(i);
                    final double x2 = tickPositionsNegative.get(i + 1);
                    verticalRowFill.getElements().addAll(new MoveTo(left + x1, top),
                            new LineTo(left + x1, top + yAxisHeight), new LineTo(left + x2, top + yAxisHeight),
                            new LineTo(left + x2, top), new ClosePath());
                }
            }
        }
        // update horizontal row fill
        horizontalRowFill.getElements().clear();
        if (isAlternativeRowFillVisible()) {
            // tick marks are not sorted so get all the positions and sort them
            final List<Double> tickPositionsPositive = new ArrayList<Double>();
            final List<Double> tickPositionsNegative = new ArrayList<Double>();
            for (int i = 0; i < yaTickMarks.size(); i++) {
                double pos = ya.getDisplayPosition(yaTickMarks.get(i).getValue());
                if (pos == yAxisZero) {
                    tickPositionsPositive.add(pos);
                    tickPositionsNegative.add(pos);
                } else if (pos < yAxisZero) {
                    tickPositionsPositive.add(pos);
                } else {
                    tickPositionsNegative.add(pos);
                }
            }
            Collections.sort(tickPositionsPositive);
            Collections.sort(tickPositionsNegative);
            // iterate over every pair of positive tick marks and create fill
            for (int i = 1; i < tickPositionsPositive.size(); i += 2) {
                if ((i + 1) < tickPositionsPositive.size()) {
                    final double y1 = tickPositionsPositive.get(i);
                    final double y2 = tickPositionsPositive.get(i + 1);
                    horizontalRowFill.getElements().addAll(new MoveTo(left, top + y1),
                            new LineTo(left + xAxisWidth, top + y1), new LineTo(left + xAxisWidth, top + y2),
                            new LineTo(left, top + y2), new ClosePath());
                }
            }
            // iterate over every pair of positive tick marks and create fill
            for (int i = 0; i < tickPositionsNegative.size(); i += 2) {
                if ((i + 1) < tickPositionsNegative.size()) {
                    final double y1 = tickPositionsNegative.get(i);
                    final double y2 = tickPositionsNegative.get(i + 1);
                    horizontalRowFill.getElements().addAll(new MoveTo(left, top + y1),
                            new LineTo(left + xAxisWidth, top + y1), new LineTo(left + xAxisWidth, top + y2),
                            new LineTo(left, top + y2), new ClosePath());
                }
            }
        }

        drawRegressions();
    }

    private void drawRegressions() {

        getPlotChildren().removeAll(y1RegressionLines);
        getPlotChildren().removeAll(y2RegressionLines);

        y1RegressionLines.clear();
        y2RegressionLines.clear();

        if (hasY1AxisRegression) {
            ObservableList<MultiAxisChart.Series<X, Y>> series = getData();
            for (MultiAxisChart.Series<X, Y> s : series) {
                Path p = calcRegression(s, MultiAxisChart.Y1_AXIS, y1AxisRegressionType);

                if (p != null) {
                    y1RegressionLines.add(p);
                }
            }
        }

        if (hasY2AxisRegression) {
            ObservableList<MultiAxisChart.Series<X, Y>> series = getData();
            for (MultiAxisChart.Series<X, Y> s : series) {
                Path p = calcRegression(s, MultiAxisChart.Y2_AXIS, y2AxisRegressionType);

                if (p != null) {
                    y2RegressionLines.add(p);
                }
            }
        }

        int index = 0;
        for (Shape s : y1RegressionLines) {
            s.setStrokeWidth(2);
            s.setStroke(Color.web(y1RegressionSeriesColors[index++]));
            getPlotChildren().add(s);
        }

        index = 0;
        for (Shape s : y2RegressionLines) {
            s.setStrokeWidth(2);
            s.setStroke(Color.web(y2RegressionSeriesColors[index++]));
            getPlotChildren().add(s);
        }

    }

    private Path calcRegression(Series<X, Y> s, int yAxisIndex, int polyDegree) {

        if (yAxisIndex == Y2_AXIS && y2Axis == null)
            throw new NullPointerException("Y2 Axis is not defind.");

        Axis yAxis = yAxisIndex == Y2_AXIS ? y2Axis : y1Axis;

        ArrayList<Point> regressionPoints = new ArrayList<>();

        PolynomialRegression quadraticPolyFilter = new PolynomialRegression(polyDegree);

        double index = 0;
        for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(s); it.hasNext(); ) {
            Data<X, Y> item = it.next();

            if ((yAxisIndex == Y1_AXIS && item.getExtraValue() == null) || (int) item.getExtraValue() == yAxisIndex) {
                if (getXAxis() instanceof NumberAxis) {
                    regressionPoints.add(new MultiAxisChart.Point(item.getCurrentX(), item.getCurrentY()));
                    double x = getValue(item.getCurrentX());
                    double y = getValue(item.getCurrentY());
                    quadraticPolyFilter.addPoint(x, y);
                } else {
                    regressionPoints.add(new MultiAxisChart.Point(index++, item.getCurrentY()));
                    double x = getValue(index++);
                    double y = getValue(item.getCurrentY());
                    quadraticPolyFilter.addPoint(x, y);
                }
            }
        }

        if (regressionPoints.size() == 0)
            return new Path();

        double xMax = Double.MIN_VALUE;
        double xMin = Double.MAX_VALUE;

        for (Point p : regressionPoints) {
            double currentX = getValue(p.getX());
            if (currentX > xMax) {
                xMax = currentX;
            }
            if (currentX < xMin) {
                xMin = currentX;
            }
        }

        PolynomialList polynomial = quadraticPolyFilter.getPolynomial();

        if (regressionPoints.size() < 2) {
            return null;
        } else {

            Path path = new Path();
            path.setStrokeWidth(2);

            MoveTo moveTo = new MoveTo();

            if (xAxis instanceof CategoryAxis) {
                moveTo.setX(findXCategoryChartCord(xMin, xMin, xMax));
            } else {
                moveTo.setX(findXChartCord(xMin));
            }

            if (polyDegree == DEGREE_NUM0) {
                moveTo.setY(findYChartCord(getValue(regressionPoints.remove(0).getY()), yAxis));

                path.getElements().add(moveTo);

                for (Point p : regressionPoints) {

                    double xValue = getValue(p.getX());
                    double yValue = getValue(p.getY());

                    LineTo lineTo = new LineTo();
                    if (xAxis instanceof CategoryAxis) {
                        lineTo.setX(findXCategoryChartCord(xValue, xMin, xMax));
                    } else {
                        lineTo.setX(findXChartCord(xValue));
                    }
                    lineTo.setY(findYChartCord(yValue, yAxis));

                    path.getElements().add(lineTo);
                }

            } else {

                moveTo.setY(findYChartCord(polynomial.getY(xMin), yAxis));

                path.getElements().add(moveTo);

                double stepDivider = 10.0;

                for (double x = xMin + 1; x <= xMax; x = x + getXAxis().getTickLabelGap() / stepDivider) {
                    LineTo lineTo = new LineTo();
                    if (xAxis instanceof CategoryAxis) {
                        lineTo.setX(findXCategoryChartCord(x, xMin, xMax));
                    } else {
                        lineTo.setX(findXChartCord(x));
                    }
                    lineTo.setY(findYChartCord(polynomial.getY(x), yAxis));

                    path.getElements().add(lineTo);

                    if (x + getXAxis().getTickLabelGap() / stepDivider > xMax && x < xMax) {
                        stepDivider = stepDivider * 10.0;
                    }

                }
            }

            return path;
        }
    }

    private double findXChartCord(double x) {
        double chartX = -1;
        chartX = ((NumberAxis) getXAxis()).getDisplayPosition(x);
        return chartX;
    }

    private double findXCategoryChartCord(double x, double xMin, double xMax) {
        CategoryAxis xAxis = (CategoryAxis) getXAxis();
        double xStartPos = xAxis.getDisplayPosition(xAxis.getCategories().get(0));
        double xEndPos = xAxis.getDisplayPosition(xAxis.getCategories().get(xAxis.getCategories().size() - 1));

        double ratio = x / xMax;

        return (xEndPos - xStartPos) * ratio + xStartPos;
    }

    private double findYChartCord(double y, Axis<?> yAxis) {
        double chartY = -1;
        chartY = ((NumberAxis) yAxis).getDisplayPosition(y);
        return chartY;
    }

    public void setRegressionColor(int axisPos, int index, String webColor) {
        if (axisPos == Y1_AXIS) {
            y1RegressionSeriesColors[index] = webColor;
        } else {
            y2RegressionSeriesColors[index] = webColor;
        }
    }

    private Line calcLinearRegression(Series<X, Y> s, int yAxisIndex) {

        if (yAxisIndex == Y2_AXIS && y2Axis == null)
            throw new NullPointerException("Y2 Axis is not defind.");

        ArrayList<Point> regressionPoints = new ArrayList<>();

        double index = 0;
        for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(s); it.hasNext(); ) {
            Data<X, Y> item = it.next();

            if ((yAxisIndex == Y1_AXIS && item.getExtraValue() == null) || (int) item.getExtraValue() == yAxisIndex) {
                if (getXAxis() instanceof NumberAxis) {
                    regressionPoints.add(new MultiAxisChart.Point(item.getCurrentX(), item.getCurrentY()));
                } else {
                    regressionPoints.add(new MultiAxisChart.Point(index++, item.getCurrentY()));
                }
            }
        }

        double xMean = 0;
        double yMean = 0;

        double xMax = Double.MIN_VALUE;
        double xMin = Double.MAX_VALUE;

        for (Point p : regressionPoints) {

            double currentX = getValue(p.getX());
            double currentY = getValue(p.getY());

            xMean += currentX;
            yMean += currentY;

            if (currentX > xMax)
                xMax = currentX;
            if (currentX < xMin)
                xMin = currentX;
        }

        xMean = xMean / regressionPoints.size();
        yMean = yMean / regressionPoints.size();
        // b1 is the slope
        double b1 = 0;
        double aSum = 0;
        double bSum = 0;

        for (Point p : regressionPoints) {

            double currentX = getValue(p.getX());
            double currentY = getValue(p.getY());

            double xMeanDist = currentX - xMean;
            double yMeanDist = currentY - yMean;

            double xMeanPow = Math.pow(xMeanDist, 2);

            aSum += xMeanPow;
            bSum += (xMeanDist * yMeanDist);

        }

        b1 = bSum / aSum;
        double y1 = b1 * xMin - b1 * xMean + yMean;
        double y2 = b1 * xMax - b1 * xMean + yMean;

        if (regressionPoints.size() < 2) {
            return null;
        } else {
            return new Line(xMin, y1, xMax, y2);
        }
    }

    private double getValue(Object x) {
        try {
            return (double) x;
        } catch (Exception e) {
            return (int) x;
        }
    }

    public void setRegression(int yAxisIndex, int type) {
        if (yAxisIndex == Y1_AXIS) {
            hasY1AxisRegression = type != NONE;
            y1AxisRegressionType = type;
        } else {
            hasY2AxisRegression = type != NONE;
            y2AxisRegressionType = type;
        }
    }

    /**
     * Computes the size of series linked list
     *
     * @return size of series linked list
     */
    int getSeriesSize() {
        return displayedSeries.size();
    }

    /**
     * XYChart maintains a list of all series currently displayed this includes all
     * current series + any series that have recently been deleted that are in the
     * process of being faded(animated) out. This creates and returns a iterator
     * over that list. This is what implementations of XYChart should use when
     * plotting data.
     *
     * @return iterator over currently displayed series
     */
    protected final Iterator<Series<X, Y>> getDisplayedSeriesIterator() {
        return Collections.unmodifiableList(displayedSeries).iterator();
    }

    // -------------- INNER CLASSES -------------------------------------

    /**
     * A single data item with data for 2 axis charts
     *
     * @since JavaFX 2.0
     */
    public final static class Data<X, Y> {
        // -------------- PUBLIC PROPERTIES ----------------------------------------

        private boolean setToRemove = false;
        /**
         * The series this data belongs to
         */
        private MultiAxisChart.Series<X, Y> series;
        /**
         * The current displayed data value plotted on the X axis. This may be the same
         * as xValue or different. It is used by MultiAxisChart to animate the xValue
         * from the old value to the new value. This is what you should plot in any
         * custom MultiAxisChart implementations. Some MultiAxisChart chart
         * implementations such as LineChart also use this to animate when data is added
         * or removed.
         */
        private ObjectProperty<X> currentX = new SimpleObjectProperty<X>(this, "currentX");
        /**
         * The generic data value to be plotted on the X axis
         */
        private ObjectProperty<X> xValue = new ObjectPropertyBase<X>() {
            @Override
            protected void invalidated() {
                // Note: calling get to make non-lazy, replace with change listener when
                // available
                get();
                if (series != null) {
                    MultiAxisChart<X, Y> chart = series.getChart();
                    if (chart != null)
                        chart.dataXValueChanged(Data.this);
                } else {
                    // data has not been added to series yet :
                    // so currentX and X should be the same
                    setCurrentX(get());
                }
            }

            @Override
            public Object getBean() {
                return Data.this;
            }

            @Override
            public String getName() {
                return "XValue";
            }
        };
        /**
         * The current displayed data value plotted on the Y axis. This may be the same
         * as yValue or different. It is used by MultiAxisChart to animate the yValue
         * from the old value to the new value. This is what you should plot in any
         * custom MultiAxisChart implementations. Some MultiAxisChart chart
         * implementations such as LineChart also use this to animate when data is added
         * or removed.
         */
        private ObjectProperty<Y> currentY = new SimpleObjectProperty<Y>(this, "currentY");
        /**
         * The generic data value to be plotted on the Y axis
         */
        private ObjectProperty<Y> yValue = new ObjectPropertyBase<Y>() {
            @Override
            protected void invalidated() {
                // Note: calling get to make non-lazy, replace with change listener when
                // available
                get();
                if (series != null) {
                    MultiAxisChart<X, Y> chart = series.getChart();
                    if (chart != null)
                        chart.dataYValueChanged(Data.this);
                } else {
                    // data has not been added to series yet :
                    // so currentY and Y should be the same
                    setCurrentY(get());
                }
            }

            @Override
            public Object getBean() {
                return Data.this;
            }

            @Override
            public String getName() {
                return "YValue";
            }
        };
        /**
         * The generic data value to be plotted in any way the chart needs. For example
         * used as the radius for BubbleChart.
         */
        private ObjectProperty<Object> extraValue = new ObjectPropertyBase<Object>() {
            @Override
            protected void invalidated() {
                // Note: calling get to make non-lazy, replace with change listener when
                // available
                get();
                if (series != null) {
                    MultiAxisChart<X, Y> chart = series.getChart();
                    if (chart != null)
                        chart.dataExtraValueChanged(Data.this);
                }
            }

            @Override
            public Object getBean() {
                return Data.this;
            }

            @Override
            public String getName() {
                return "extraValue";
            }
        };
        /**
         * The node to display for this data item. You can either create your own node
         * and set it on the data item before you add the item to the chart. Otherwise
         * the chart will create a node for you that has the default representation for
         * the chart type. This node will be set as soon as the data is added to the
         * chart. You can then get it to add mouse listeners etc. Charts will do their
         * best to position and size the node appropriately, for example on a Line or
         * Scatter chart this node will be positioned centered on the data values
         * position. For a bar chart this is positioned and resized as the bar for this
         * data item.
         */
        private ObjectProperty<Node> node = new SimpleObjectProperty<Node>(this, "node") {
            @Override
            protected void invalidated() {
                Node node = get();
                if (node != null) {
                    node.accessibleTextProperty().unbind();
                    node.accessibleTextProperty().bind(new StringBinding() {
                        {
                            bind(currentXProperty(), currentYProperty());
                        }

                        @Override
                        protected String computeValue() {
                            String seriesName = series != null ? series.getName() : "";
                            return seriesName + " X Axis is " + getCurrentX() + " Y Axis is " + getCurrentY();
                        }
                    });
                }
            }
        };
        /**
         * The current displayed data extra value. This may be the same as extraValue or
         * different. It is used by MultiAxisChart to animate the extraValue from the
         * old value to the new value. This is what you should plot in any custom
         * MultiAxisChart implementations.
         */
        private ObjectProperty<Object> currentExtraValue = new SimpleObjectProperty<Object>(this, "currentExtraValue");

        /**
         * Creates an empty MultiAxisChart.Data object.
         */
        public Data() {
        }

        /**
         * Creates an instance of MultiAxisChart.Data object and initializes the X,Y
         * data values.
         *
         * @param xValue The X axis data value
         * @param yValue The Y axis data value
         */
        public Data(X xValue, Y yValue) {
            setXValue(xValue);
            setYValue(yValue);
            setCurrentX(xValue);
            setCurrentY(yValue);
        }

        /**
         * Creates an instance of MultiAxisChart.Data object and initializes the X,Y
         * data values and extraValue.
         *
         * @param xValue     The X axis data value.
         * @param yValue     The Y axis data value.
         * @param extraValue Chart extra value.
         */
        public Data(X xValue, Y yValue, Object extraValue) {
            setXValue(xValue);
            setYValue(yValue);
            setExtraValue(extraValue);
            setCurrentX(xValue);
            setCurrentY(yValue);
            setCurrentExtraValue(extraValue);
        }

        void setSeries(MultiAxisChart.Series<X, Y> series) {
            this.series = series;
        }

        /**
         * Gets the generic data value to be plotted on the X axis.
         *
         * @return the generic data value to be plotted on the X axis.
         */
        public final X getXValue() {
            return xValue.get();
        }

        /**
         * Sets the generic data value to be plotted on the X axis.
         *
         * @param value the generic data value to be plotted on the X axis.
         */
        public final void setXValue(X value) {
            xValue.set(value);
            // handle the case where this is a init because the default constructor was used
            // and the case when series is not associated to a chart due to a remove series
            if (currentX.get() == null || (series != null && series.getChart() == null))
                currentX.setValue(value);
        }

        /**
         * The generic data value to be plotted on the X axis.
         *
         * @return The XValue property
         */
        public final ObjectProperty<X> XValueProperty() {
            return xValue;
        }

        /**
         * Gets the generic data value to be plotted on the Y axis.
         *
         * @return the generic data value to be plotted on the Y axis.
         */
        public final Y getYValue() {
            return yValue.get();
        }

        /**
         * Sets the generic data value to be plotted on the Y axis.
         *
         * @param value the generic data value to be plotted on the Y axis.
         */
        public final void setYValue(Y value) {
            yValue.set(value);
            // handle the case where this is a init because the default constructor was used
            // and the case when series is not associated to a chart due to a remove series
            if (currentY.get() == null || (series != null && series.getChart() == null))
                currentY.setValue(value);

        }

        /**
         * The generic data value to be plotted on the Y axis.
         *
         * @return the YValue property
         */
        public final ObjectProperty<Y> YValueProperty() {
            return yValue;
        }

        public final Object getExtraValue() {
            return extraValue.get();
        }

        public final void setExtraValue(Object value) {
            extraValue.set(value);
        }

        public final ObjectProperty<Object> extraValueProperty() {
            return extraValue;
        }

        public final Node getNode() {
            return node.get();
        }

        public final void setNode(Node value) {
            node.set(value);
        }

        public final ObjectProperty<Node> nodeProperty() {
            return node;
        }

        final X getCurrentX() {
            return currentX.get();
        }

        final void setCurrentX(X value) {
            currentX.set(value);
        }

        final ObjectProperty<X> currentXProperty() {
            return currentX;
        }

        final Y getCurrentY() {
            return currentY.get();
        }

        final void setCurrentY(Y value) {
            currentY.set(value);
        }

        final ObjectProperty<Y> currentYProperty() {
            return currentY;
        }

        // -------------- CONSTRUCTOR -------------------------------------------------

        final Object getCurrentExtraValue() {
            return currentExtraValue.getValue();
        }

        final void setCurrentExtraValue(Object value) {
            currentExtraValue.setValue(value);
        }

        final ObjectProperty<Object> currentExtraValueProperty() {
            return currentExtraValue;
        }

        // -------------- PUBLIC METHODS ----------------------------------------------

        /**
         * Returns a string representation of this {@code Data} object.
         *
         * @return a string representation of this {@code Data} object.
         */
        @Override
        public String toString() {
            return "Data[" + getXValue() + "," + getYValue() + "," + getExtraValue() + "]";
        }

    }

    /**
     * A named series of data items
     *
     * @since JavaFX 2.0
     */
    public static final class Series<X, Y> {

        // -------------- PRIVATE PROPERTIES ----------------------------------------

        /**
         * The user displayable name for this series
         */
        private final StringProperty name = new StringPropertyBase() {
            @Override
            protected void invalidated() {
                get(); // make non-lazy
                if (getChart() != null)
                    getChart().seriesNameChanged();
            }

            @Override
            public Object getBean() {
                return Series.this;
            }

            @Override
            public String getName() {
                return "name";
            }
        };
        /**
         * the style class for default color for this series
         */
        String defaultColorStyleClass;
        boolean setToRemove = false;
        private List<Data<X, Y>> displayedData = new ArrayList<>();

        // -------------- PUBLIC PROPERTIES ----------------------------------------
        /**
         * ObservableList of data items that make up this series
         */
        private final ObjectProperty<ObservableList<Data<X, Y>>> data = new ObjectPropertyBase<ObservableList<Data<X, Y>>>() {
            private ObservableList<Data<X, Y>> old;

            @Override
            protected void invalidated() {
                final ObservableList<Data<X, Y>> current = getValue();
                // add remove listeners
                if (old != null)
                    old.removeListener(dataChangeListener);
                if (current != null)
                    current.addListener(dataChangeListener);
                // fire data change event if series are added or removed
                if (old != null || current != null) {
                    final List<Data<X, Y>> removed = (old != null) ? old : Collections.emptyList();
                    final int toIndex = (current != null) ? current.size() : 0;
                    // let data listener know all old data have been removed and new data that has
                    // been added
                    if (toIndex > 0 || !removed.isEmpty()) {
                        dataChangeListener.onChanged(new NonIterableChange<Data<X, Y>>(0, toIndex, current) {
                            @Override
                            public List<Data<X, Y>> getRemoved() {
                                return removed;
                            }

                            @Override
                            protected int[] getPermutation() {
                                return new int[0];
                            }
                        });
                    }
                } else if (old != null && old.size() > 0) {
                    // let series listener know all old series have been removed
                    dataChangeListener.onChanged(new NonIterableChange<Data<X, Y>>(0, 0, current) {
                        @Override
                        public List<Data<X, Y>> getRemoved() {
                            return old;
                        }

                        @Override
                        protected int[] getPermutation() {
                            return new int[0];
                        }
                    });
                }
                old = current;
            }

            @Override
            public Object getBean() {
                return Series.this;
            }

            @Override
            public String getName() {
                return "data";
            }
        };
        /**
         * Reference to the chart this series belongs to
         */
        private final ReadOnlyObjectWrapper<MultiAxisChart<X, Y>> chart = new ReadOnlyObjectWrapper<MultiAxisChart<X, Y>>(
                this, "chart") {
            @Override
            protected void invalidated() {
                if (get() == null) {
                    displayedData.clear();
                } else {
                    displayedData.addAll(getData());
                }
            }
        };
        private final ListChangeListener<Data<X, Y>> dataChangeListener = new ListChangeListener<Data<X, Y>>() {
            @Override
            public void onChanged(Change<? extends Data<X, Y>> c) {
                ObservableList<? extends Data<X, Y>> data = c.getList();
                final MultiAxisChart<X, Y> chart = getChart();
                while (c.next()) {
                    if (chart != null) {
                        // RT-25187 Probably a sort happened, just reorder the pointers and return.
                        if (c.wasPermutated()) {
                            displayedData.sort((o1, o2) -> data.indexOf(o2) - data.indexOf(o1));
                            return;
                        }

                        Set<Data<X, Y>> dupCheck = new HashSet<>(displayedData);
                        dupCheck.removeAll(c.getRemoved());
                        for (Data<X, Y> d : c.getAddedSubList()) {
                            if (!dupCheck.add(d)) {
                                throw new IllegalArgumentException("Duplicate data added");
                            }
                        }

                        // update data items reference to series
                        for (Data<X, Y> item : c.getRemoved()) {
                            item.setToRemove = true;
                        }

                        if (c.getAddedSize() > 0) {
                            for (Data<X, Y> itemPtr : c.getAddedSubList()) {
                                if (itemPtr.setToRemove) {
                                    if (chart != null)
                                        chart.dataBeingRemovedIsAdded(itemPtr, MultiAxisChart.Series.this);
                                    itemPtr.setToRemove = false;
                                }
                            }

                            for (Data<X, Y> d : c.getAddedSubList()) {
                                d.setSeries(MultiAxisChart.Series.this);
                            }
                            if (c.getFrom() == 0) {
                                displayedData.addAll(0, c.getAddedSubList());
                            } else {
                                displayedData.addAll(displayedData.indexOf(data.get(c.getFrom() - 1)) + 1,
                                        c.getAddedSubList());
                            }
                        }
                        // inform chart
                        chart.dataItemsChanged(MultiAxisChart.Series.this, (List<Data<X, Y>>) c.getRemoved(),
                                c.getFrom(), c.getTo(), c.wasPermutated());
                    } else {
                        Set<Data<X, Y>> dupCheck = new HashSet<>();
                        for (Data<X, Y> d : data) {
                            if (!dupCheck.add(d)) {
                                throw new IllegalArgumentException("Duplicate data added");
                            }
                        }

                        for (Data<X, Y> d : c.getAddedSubList()) {
                            d.setSeries(MultiAxisChart.Series.this);
                        }

                    }
                }
            }
        };
        /**
         * The node to display for this series. This is created by the chart if it uses
         * nodes to represent the whole series. For example line chart uses this for the
         * line but scatter chart does not use it. This node will be set as soon as the
         * series is added to the chart. You can then get it to add mouse listeners etc.
         */
        private ObjectProperty<Node> node = new SimpleObjectProperty<Node>(this, "node");

        /**
         * Construct a empty series
         */
        public Series() {
            this(FXCollections.observableArrayList());
        }

        /**
         * Constructs a Series and populates it with the given {@link ObservableList}
         * data.
         *
         * @param data ObservableList of MultiAxisChart.Data
         */
        public Series(ObservableList<Data<X, Y>> data) {
            setData(data);
            for (Data<X, Y> item : data)
                item.setSeries(this);
        }

        /**
         * Constructs a named Series and populates it with the given
         * {@link ObservableList} data.
         *
         * @param name a name for the series
         * @param data ObservableList of MultiAxisChart.Data
         */
        public Series(String name, ObservableList<Data<X, Y>> data) {
            this(data);
            setName(name);
        }

        public final MultiAxisChart<X, Y> getChart() {
            return chart.get();
        }

        private void setChart(MultiAxisChart<X, Y> value) {
            chart.set(value);
        }

        public final ReadOnlyObjectProperty<MultiAxisChart<X, Y>> chartProperty() {
            return chart.getReadOnlyProperty();
        }

        public final String getName() {
            return name.get();
        }

        public final void setName(String value) {
            name.set(value);
        }

        public final StringProperty nameProperty() {
            return name;
        }

        public final Node getNode() {
            return node.get();
        }

        public final void setNode(Node value) {
            node.set(value);
        }

        public final ObjectProperty<Node> nodeProperty() {
            return node;
        }

        // -------------- CONSTRUCTORS ----------------------------------------------

        public final ObservableList<Data<X, Y>> getData() {
            return data.getValue();
        }

        public final void setData(ObservableList<Data<X, Y>> value) {
            data.setValue(value);
        }

        public final ObjectProperty<ObservableList<Data<X, Y>>> dataProperty() {
            return data;
        }

        // -------------- PUBLIC METHODS ----------------------------------------------

        /**
         * Returns a string representation of this {@code Series} object.
         *
         * @return a string representation of this {@code Series} object.
         */
        @Override
        public String toString() {
            return "Series[" + getName() + "]";
        }

        // -------------- PRIVATE/PROTECTED METHODS -----------------------------------

        /*
         * The following methods are for manipulating the pointers in the linked list
         * when data is deleted.
         */
        private void removeDataItemRef(Data<X, Y> item) {
            if (item != null)
                item.setToRemove = false;
            displayedData.remove(item);
        }

        int getItemIndex(Data<X, Y> item) {
            return displayedData.indexOf(item);
        }

        Data<X, Y> getItem(int i) {
            return displayedData.get(i);
        }

        int getDataSize() {
            return displayedData.size();
        }
    }

    private class Point {
        X x;
        Y y;

        public Point(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public X getX() {
            return x;
        }

        public void setX(X x) {
            this.x = x;
        }

        public Y getY() {
            return y;
        }

        public void setY(Y y) {
            this.y = y;
        }
    }
}
