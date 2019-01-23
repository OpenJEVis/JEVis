/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A chart that plots bars indicating data values for a category. The bars can
 * be vertical or horizontal depending on which axis is a category axis.
 *
 * @since JavaFX 2.0
 */
public class MultiAxisBarChart<X, Y> extends MultiAxisChart<X, Y> {

    // -------------- PRIVATE FIELDS -------------------------------------------

    /**
     * Pseudoclass indicating this is a vertical chart.
     */
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
    /**
     * Pseudoclass indicating this is a horizontal chart.
     */
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");
    private static String NEGATIVE_STYLE = "negative";
    private final Orientation orientation;
    private Map<Series<X, Y>, Map<String, Data<X, Y>>> seriesCategoryMap = new HashMap<>();
    private Legend legend = new Legend();
    private CategoryAxis categoryAxis;
    private ValueAxis valueAxis;
    private Timeline dataRemoveTimeline;
    private double bottomPos = 0;
    // -------------- PUBLIC PROPERTIES ----------------------------------------
    private ParallelTransition pt;
    // For storing data values in case removed and added immediately.
    private Map<Data<X, Y>, Double> XYValueMap = new HashMap<Data<X, Y>, Double>();
    /**
     * The gap to leave between bars in the same category
     */
    private DoubleProperty barGap = new StyleableDoubleProperty(4) {
        @Override
        protected void invalidated() {
            get();
            layout();
        }

        @Override
        public Object getBean() {
            return MultiAxisBarChart.this;
        }

        @Override
        public String getName() {
            return "barGap";
        }

        @Override
        public CssMetaData<MultiAxisBarChart<?, ?>, Number> getCssMetaData() {
            return null;
        }
    };
    /**
     * The gap to leave between bars in separate categories
     */
    private DoubleProperty categoryGap = new StyleableDoubleProperty(10) {
        @Override
        protected void invalidated() {
            get();
            layout();
        }

        @Override
        public Object getBean() {
            return MultiAxisBarChart.this;
        }

        @Override
        public String getName() {
            return "categoryGap";
        }

        @Override
        public CssMetaData<MultiAxisBarChart<?, ?>, Number> getCssMetaData() {
            return null;
        }
    };

    /**
     * Construct a new MultiAxisBarChart with the given axis. The two axis should be
     * a ValueAxis/NumberAxis (Y1, Y2) and a CategoryAxis ( X-Axis), they can be in
     * either order depending on if you want a horizontal or vertical bar chart.
     *
     * @param xAxis  The x axis to use
     * @param y1Axis The y1 axis to use
     * @param y2Axis The y2 axis to use
     */
    public MultiAxisBarChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis) {
        this(xAxis, y1Axis, y2Axis, FXCollections.observableArrayList());
    }

    /**
     * Construct a new MultiAxisBarChart with the given axis and data. The two axis
     * should be a ValueAxis/NumberAxis and a CategoryAxis, they can be in either
     * order depending on if you want a horizontal or vertical bar chart.
     *
     * @param xAxis  The x axis to use
     * @param y1Axis The first y axis to use
     * @param y2Axis The second y axis to use
     * @param data   The data to use, this is the actual list used so any changes to it
     *               will be reflected in the chart
     */
    public MultiAxisBarChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis, ObservableList<Series<X, Y>> data) {
        super(xAxis, y1Axis, y2Axis);
        getStyleClass().add("bar-chart");
        setLegend(legend);
        if (!(y1Axis instanceof ValueAxis && xAxis instanceof CategoryAxis)) {
            throw new IllegalArgumentException(
                    "Axis type incorrect, X axis should be CategoryAxis and the other/s NumberAxis");
        }

        categoryAxis = (CategoryAxis) xAxis;
        valueAxis = (ValueAxis) y1Axis;
        orientation = Orientation.VERTICAL;

        // update css
        pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, orientation == Orientation.HORIZONTAL);
        pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, orientation == Orientation.VERTICAL);
        setData(data);
    }

    /**
     * Construct a new MultiAxisBarChart with the given axis and data. The two axis
     * should be a ValueAxis/NumberAxis and a CategoryAxis, they can be in either
     * order depending on if you want a horizontal or vertical bar chart.
     *
     * @param xAxis       The x axis to use
     * @param y1Axis      The first y axis to use
     * @param y2Axis      The second y axis to use
     * @param data        The data to use, this is the actual list used so any changes to it
     *                    will be reflected in the chart
     * @param categoryGap The gap to leave between bars in separate categories
     */
    public MultiAxisBarChart(Axis<X> xAxis, Axis<Y> y1Axis, Axis<Y> y2Axis, ObservableList<Series<X, Y>> data,
                             @NamedArg("categoryGap") double categoryGap) {
        this(xAxis, y1Axis, y2Axis);
        setData(data);
        setCategoryGap(categoryGap);
    }

    public final double getBarGap() {
        return barGap.getValue();
    }

    // -------------- CONSTRUCTOR ----------------------------------------------

    public final void setBarGap(double value) {
        barGap.setValue(value);
    }

    public final DoubleProperty barGapProperty() {
        return barGap;
    }

    public final double getCategoryGap() {
        return categoryGap.getValue();
    }

    // -------------- PROTECTED METHODS ----------------------------------------

    public final void setCategoryGap(double value) {
        categoryGap.setValue(value);
    }

    public final DoubleProperty categoryGapProperty() {
        return categoryGap;
    }

    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        String category;
        if (orientation == Orientation.VERTICAL) {
            category = (String) item.getXValue();
        } else {
            category = (String) item.getYValue();
        }
        Map<String, Data<X, Y>> categoryMap = seriesCategoryMap.computeIfAbsent(series, k -> new HashMap<String, Data<X, Y>>());

        // check if category is already present
        if (!categoryAxis.getCategories().contains(category)) {
            // note: cat axis categories can be updated only when autoranging is true.
            categoryAxis.getCategories().add(itemIndex, category);
        } else if (categoryMap.containsKey(category)) {
            // RT-21162 : replacing the previous data, first remove the node from
            // scenegraph.
            Data<X, Y> data = categoryMap.get(category);
            getPlotChildren().remove(data.getNode());
            removeDataItemFromDisplay(series, data);
            requestChartLayout();
            categoryMap.remove(category);
        }
        categoryMap.put(category, item);
        Node bar = createBar(series, getData().indexOf(series), item, itemIndex);

        getPlotChildren().add(bar);

    }

    @Override
    protected void dataItemRemoved(final Data<X, Y> item, final Series<X, Y> series) {
        final Node bar = item.getNode();

        if (bar != null) {
            bar.focusTraversableProperty().unbind();
        }

        processDataRemove(series, item);
        removeDataItemFromDisplay(series, item);

    }

    /**
     * @inheritDoc
     */
    @Override
    protected void dataItemChanged(Data<X, Y> item) {
        double barVal;
        double currentVal;
        if (orientation == Orientation.VERTICAL) {
            barVal = ((Number) item.getYValue()).doubleValue();
            currentVal = ((Number) item.getCurrentY()).doubleValue();
        } else {
            barVal = ((Number) item.getXValue()).doubleValue();
            currentVal = ((Number) item.getCurrentX()).doubleValue();
        }
        if (currentVal > 0 && barVal < 0) { // going from positive to negative
            // add style class negative
            item.getNode().getStyleClass().add(NEGATIVE_STYLE);
        } else if (currentVal < 0 && barVal > 0) { // going from negative to positive
            // remove style class negative
            // RT-21164 upside down bars: was adding NEGATIVE_STYLE styleclass
            // instead of removing it; when going from negative to positive
            item.getNode().getStyleClass().remove(NEGATIVE_STYLE);
        }
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        // handle any data already in series
        // create entry in the map
        Map<String, Data<X, Y>> categoryMap = new HashMap<String, Data<X, Y>>();
        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            Node bar = createBar(series, seriesIndex, item, j);
            String category;
            if (orientation == Orientation.VERTICAL) {
                category = (String) item.getXValue();
            } else {
                category = (String) item.getYValue();
            }
            categoryMap.put(category, item);

            // RT-21164 check if bar value is negative to add NEGATIVE_STYLE style class
            double barVal = (orientation == Orientation.VERTICAL) ? ((Number) item.getYValue()).doubleValue()
                    : ((Number) item.getXValue()).doubleValue();
            if (barVal < 0) {
                bar.getStyleClass().add(NEGATIVE_STYLE);
            }
            getPlotChildren().add(bar);

        }
        if (categoryMap.size() > 0)
            seriesCategoryMap.put(series, categoryMap);
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        updateDefaultColorIndex(series);
        // remove all symbol nodes

        for (Data<X, Y> d : series.getData()) {
            final Node bar = d.getNode();
            getPlotChildren().remove(bar);
            updateMap(series, d);
        }
        removeSeriesFromDisplay(series);

    }

    // -------------- PRIVATE METHODS ------------------------------------------

    /**
     * @inheritDoc
     */
    @Override
    protected void layoutPlotChildren() {
        double catSpace = categoryAxis.getCategorySpacing();
        // calculate bar spacing
        final double avilableBarSpace = catSpace - (getCategoryGap() + getBarGap());
        double barWidth = (avilableBarSpace / getSeriesSize()) - getBarGap();
        final double barOffset = -((catSpace - getCategoryGap()) / 2);
        final double zeroPos = (valueAxis.getLowerBound() > 0) ? valueAxis.getDisplayPosition(valueAxis.getLowerBound())
                : valueAxis.getZeroPosition();
        // RT-24813 : if the data in a series gets too large, barWidth can get negative.
        if (barWidth <= 0)
            barWidth = 1;
        // update bar positions and sizes
        int catIndex = 0;
        for (String category : categoryAxis.getCategories()) {
            int index = 0;
            for (Iterator<Series<X, Y>> sit = getDisplayedSeriesIterator(); sit.hasNext(); ) {
                Series<X, Y> series = sit.next();
                final Data<X, Y> item = getDataItem(series, index, catIndex, category);
                if (item != null) {
                    final Node bar = item.getNode();
                    final double categoryPos;
                    final double valPos;

                    categoryPos = getXAxis().getDisplayPosition(item.getCurrentX());

                    if (item.getExtraValue() == null || (int) item.getExtraValue() == MultiAxisChart.Y1_AXIS) {
                        valPos = getY1Axis().getDisplayPosition(item.getCurrentY());
                    } else {
                        if (getY2Axis() != null) {
                            if (getY2Axis().isVisible()) {
                                valPos = getY2Axis().getDisplayPosition(item.getCurrentY());
                            } else {
                                continue;
                            }
                        } else {
                            throw new NullPointerException("Y2 axis is not defined.");
                        }
                    }

                    if (Double.isNaN(categoryPos) || Double.isNaN(valPos)) {
                        continue;
                    }
                    final double bottom = Math.min(valPos, zeroPos);
                    final double top = Math.max(valPos, zeroPos);
                    bottomPos = bottom;
                    if (orientation == Orientation.VERTICAL) {
                        bar.resizeRelocate(categoryPos + barOffset + (barWidth + getBarGap()) * index, bottom, barWidth,
                                top - bottom);
                    } else {
                        // noinspection SuspiciousNameCombination
                        bar.resizeRelocate(bottom, categoryPos + barOffset + (barWidth + getBarGap()) * index,
                                top - bottom, barWidth);
                    }

                    index++;
                }
            }
            catIndex++;
        }
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
                legenditem.getSymbol().getStyleClass().addAll("chart-bar", "series" + seriesIndex, "bar-legend-symbol",
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

    private void updateMap(Series<X, Y> series, Data<X, Y> item) {
        final String category = (orientation == Orientation.VERTICAL) ? (String) item.getXValue()
                : (String) item.getYValue();
        Map<String, Data<X, Y>> categoryMap = seriesCategoryMap.get(series);
        if (categoryMap != null) {
            categoryMap.remove(category);
            if (categoryMap.isEmpty())
                seriesCategoryMap.remove(series);
        }
        if (seriesCategoryMap.isEmpty() && categoryAxis.isAutoRanging())
            categoryAxis.getCategories().clear();
    }

    private void processDataRemove(final Series<X, Y> series, final Data<X, Y> item) {
        Node bar = item.getNode();
        getPlotChildren().remove(bar);
        updateMap(series, item);
    }

    @Override
    void dataBeingRemovedIsAdded(Data<X, Y> item, Series<X, Y> series) {
        if (dataRemoveTimeline != null) {
            dataRemoveTimeline.setOnFinished(null);
            dataRemoveTimeline.stop();
        }
        processDataRemove(series, item);
        item.setSeries(null);
        removeDataItemFromDisplay(series, item);
        restoreDataValues(item);
        XYValueMap.clear();
    }

    private void restoreDataValues(Data item) {
        Double value = XYValueMap.get(item);
        if (value != null) {
            // Restoring original X/Y values
            if (orientation.equals(Orientation.VERTICAL)) {
                item.setYValue(value);
                item.setCurrentY(value);
            } else {
                item.setXValue(value);
                item.setCurrentX(value);

            }
        }
    }

    @Override
    void seriesBeingRemovedIsAdded(Series<X, Y> series) {
        boolean lastSeries = pt.getChildren().size() == 1;
        if (pt != null) {
            if (!pt.getChildren().isEmpty()) {
                for (Animation a : pt.getChildren()) {
                    a.setOnFinished(null);
                }
            }
            for (Data<X, Y> item : series.getData()) {
                processDataRemove(series, item);
                if (!lastSeries) {
                    restoreDataValues(item);
                }
            }
            XYValueMap.clear();
            pt.setOnFinished(null);
            pt.getChildren().clear();
            pt.stop();
            removeSeriesFromDisplay(series);
        }
    }

    private void updateDefaultColorIndex(final Series<X, Y> series) {
        int clearIndex = seriesColorMap.get(series);
        for (Data<X, Y> d : series.getData()) {
            final Node bar = d.getNode();
            if (bar != null) {
                bar.getStyleClass().remove(DEFAULT_COLOR + clearIndex);
            }
        }
    }

    private Node createBar(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {
        Node bar = item.getNode();
        if (bar == null) {
            bar = new StackPane();
            bar.setAccessibleRole(AccessibleRole.TEXT);
            bar.setAccessibleRoleDescription("Bar");
            bar.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
            item.setNode(bar);
        }
        bar.getStyleClass().addAll("chart-bar", "series" + seriesIndex, "data" + itemIndex,
                series.defaultColorStyleClass);
        return bar;
    }

    private Data<X, Y> getDataItem(Series<X, Y> series, int seriesIndex, int itemIndex, String category) {
        Map<String, Data<X, Y>> catmap = seriesCategoryMap.get(series);
        return (catmap != null) ? catmap.get(category) : null;
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
