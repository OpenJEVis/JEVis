/*
 * Copyright 2013 Jason Winnebeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jevis.jeconfig.application.Chart.Zoom;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jevis.jeconfig.application.Chart.ChartElements.DateAxis;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.Charts.jfx.Axis;
import org.jevis.jeconfig.application.Chart.Charts.jfx.ValueAxis;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

/**
 * ChartZoomManager manages a zooming selection rectangle and the bounds of the graph. It can be
 * enabled via {@link #start()} and disabled via {@link #stop()}. The normal usage is to create a
 * StackPane with two children, an XYChart type and a Rectangle. The Rectangle should start out
 * invisible and have mouseTransparent set to true. If it has a stroke, it should be of INSIDE
 * type to be pixel perfect.
 * <p>
 * You can also use {@link JFXChartUtil#setupZooming(MultiAxisChart)} for a default solution.
 * <p>
 * Six types of zooming are supported. All are enabled by default. The drag zooming can be disabled
 * with the {@link #setMouseFilter} set to a mouse filter that allows nothing. Mouse wheel zooming
 * can be disabled via the {@link #setMouseWheelZoomAllowed} method.
 * <ol>
 * <li>Free-form zooming in the plot area on both axes</li>
 * <li>X-axis only zooming by dragging in the x-axis</li>
 * <li>Y-axis only zooming by dragging in the y-axis</li>
 * <li>Free-form zooming by the mouse wheel. The location of the cursor is taken as the zoom
 * focus point</li>
 * <li>X-axis only zooming by mouse wheel; cursor used as focus point</li>
 * <li>Y-axis only zooming by mouse wheel; cursor used as focus point</li>
 * </ol>
 * <p>
 * A lot of code in ChartZoomManager currently assumes there are no scale or rotate
 * transforms between the chartPane and the axes and plot area. However, all translation transforms,
 * layoutX/Y changes, padding, margin, and setTranslate issues should be OK. This might be improved
 * later, for example JavaFX 8 is rumored to allow transform multiplication, which could solve this.
 * <p>
 * Example FXML to create the components used by this class:
 * <pre>
 * &lt;StackPane fx:id="chartPane" alignment="CENTER"&gt;
 * &lt;LineChart fx:id="chart" animated="false" legendVisible="false"&gt;
 * &lt;xAxis&gt;
 * &lt;NumberAxis animated="false" side="BOTTOM" /&gt;
 * &lt;/xAxis&gt;
 * &lt;y1Axis&gt;
 * &lt;NumberAxis animated="false" side="LEFT" /&gt;
 * &lt;/y1Axis&gt;
 * &lt;/LineChart&gt;
 * &lt;Rectangle fx:id="selectRect" fill="DODGERBLUE" height="0.0" mouseTransparent="true"
 * opacity="0.3" stroke="#002966" strokeType="INSIDE" strokeWidth="3.0" width="0.0"
 * x="0.0" y="0.0" StackPane.alignment="TOP_LEFT" /&gt;
 * &lt;/StackPane&gt;</pre>
 * <p>
 * Example Java code in bound controller class:
 * <pre>
 * ChartZoomManager zoomManager = new ChartZoomManager( chartPane, selectRect, chart );
 * zoomManager.start();</pre>
 *
 * @author Jason Winnebeck
 */
public class ChartZoomManager {
    /**
     * The default mouse filter for the {@link ChartZoomManager} filters events unless only primary
     * mouse button (usually left) is depressed.
     */
    public static final EventHandler<MouseEvent> DEFAULT_FILTER = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            //The ChartPanManager uses this reference, so if behavior changes, copy to users first.
            if (mouseEvent.getButton() != MouseButton.PRIMARY)
                mouseEvent.consume();
        }
    };

    private final SimpleDoubleProperty rectX = new SimpleDoubleProperty();
    private final SimpleDoubleProperty rectY = new SimpleDoubleProperty();
    private final SimpleBooleanProperty selecting = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty zoomFinished = new SimpleBooleanProperty(false);

    private final DoubleProperty zoomDurationMillis = new SimpleDoubleProperty(750.0);
    private final BooleanProperty zoomAnimated = new SimpleBooleanProperty(true);
    private final BooleanProperty mouseWheelZoomAllowed = new SimpleBooleanProperty(true);
    private final EventHandlerManager handlerManager;
    private final Rectangle selectRect;
    private final ValueAxis<?> xAxis;
    private final ValueAxis<?> y1Axis;
    private final ValueAxis<?> y2Axis;
    private final XYChartInfo chartInfo;
    private final Timeline zoomAnimation = new Timeline();
    private final StackPane chartPane;
    private AxisConstraint zoomMode = AxisConstraint.None;
    private AxisConstraintStrategy axisConstraintStrategy = AxisConstraintStrategies.getIgnoreOutsideChart();
    private AxisConstraintStrategy mouseWheelAxisConstraintStrategy = AxisConstraintStrategies.getDefault();
    private EventHandler<? super MouseEvent> mouseFilter = DEFAULT_FILTER;
    private Rectangle2D zoomWindow1;
    private final String maxStr = I18n.getInstance().getString("plugin.graph.table.max") + " ";
    private final String minStr = I18n.getInstance().getString("plugin.graph.table.min") + " ";
    private Label startLabel = new Label();
    private Label endLabel = new Label();

    /**
     * Construct a new ChartZoomManager. See {@link ChartZoomManager} documentation for normal usage.
     *
     * @param chartPane  A Pane which is the ancestor of all arguments
     * @param selectRect A Rectangle whose layoutX/Y makes it line up with the chart
     * @param chart      Chart to manage, where both X and Y axis are a {@link ValueAxis}.
     */
    public ChartZoomManager(StackPane chartPane, Rectangle selectRect, MultiAxisChart<?, ?> chart) {
        this.selectRect = selectRect;
        this.chartPane = chartPane;
        this.chartPane.getChildren().addAll(startLabel, endLabel);
        StackPane.setAlignment(startLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(endLabel, Pos.TOP_RIGHT);

        DropShadow dropShadowLeftToRight = new DropShadow();
        dropShadowLeftToRight.setRadius(5.0);
        dropShadowLeftToRight.setOffsetX(3.0);
        dropShadowLeftToRight.setOffsetY(3.0);
        dropShadowLeftToRight.setColor(Color.BLACK);
        startLabel.setBackground(new Background(new BackgroundFill(Color.HOTPINK, new CornerRadii(0, 0, 5, 0, false), Insets.EMPTY)));
        startLabel.setPadding(new Insets(3));
        startLabel.setStyle("-fx-font-weight: bold;");
        startLabel.setEffect(dropShadowLeftToRight);

        DropShadow dropShadowRightToLeft = new DropShadow();
        dropShadowRightToLeft.setRadius(5.0);
        dropShadowRightToLeft.setOffsetX(-3.0);
        dropShadowRightToLeft.setOffsetY(3.0);
        dropShadowRightToLeft.setColor(Color.BLACK);
        endLabel.setBackground(new Background(new BackgroundFill(Color.HOTPINK, new CornerRadii(0, 0, 0, 5, false), Insets.EMPTY)));
        endLabel.setPadding(new Insets(3));
        endLabel.setStyle("-fx-font-weight: bold;");
        endLabel.setEffect(dropShadowRightToLeft);

        if (chart.getXAxis() instanceof ValueAxis) xAxis = (ValueAxis<?>) chart.getXAxis();
        else {
            /**
             * TODO what to do with category
             */
            Axis a = new DateAxis();
            xAxis = (ValueAxis<?>) a;
        }
        this.y1Axis = (ValueAxis<?>) chart.getY1Axis();
        this.y2Axis = (ValueAxis<?>) chart.getY2Axis();
        chartInfo = new XYChartInfo(chart, chartPane);

        handlerManager = new EventHandlerManager(chartPane);

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (passesFilter(mouseEvent))
                    onMousePressed(mouseEvent);
            }
        });

        handlerManager.addEventHandler(false, MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (passesFilter(mouseEvent))
                    onDragStart();
            }
        });

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //Don't check filter here, we're either already started, or not
                onMouseDragged(mouseEvent);
            }
        });

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //Don't check filter here, we're either already started, or not
                onMouseReleased();
            }
        });

        handlerManager.addEventHandler(false, ScrollEvent.ANY, new MouseWheelZoomHandler());
    }

    public Double getXAxisLowerBound() {
        return zoomWindow1.getMinX();
    }

    public Double getXAxisUpperBound() {
        return zoomWindow1.getMaxX();
    }

    public Double getY1AxisLowerBound() {
        return y1Axis.getLowerBound();
    }

    public Double getY1AxisUpperBound() {
        return y1Axis.getUpperBound();
    }

    public Double getY2AxisLowerBound() {
        return y2Axis.getLowerBound();
    }

    public Double getY2AxisUpperBound() {
        return y2Axis.getUpperBound();
    }

    public boolean isZoomFinished() {
        return zoomFinished.get();
    }

    public SimpleBooleanProperty zoomFinishedProperty() {
        return zoomFinished;
    }

    private static double getBalance(double val, double min, double max) {
        if (val <= min)
            return 0.0;
        else if (val >= max)
            return 1.0;

        return (val - min) / (max - min);
    }

    /**
     * Returns the current strategy in use for mouse drag events.
     *
     * @see #setAxisConstraintStrategy(AxisConstraintStrategy)
     */
    public AxisConstraintStrategy getAxisConstraintStrategy() {
        return axisConstraintStrategy;
    }

    /**
     * Sets the {@link AxisConstraintStrategy} to use for mouse drag events, which determines which axis is allowed for
     * zooming. The default implementation is {@link AxisConstraintStrategies#getIgnoreOutsideChart()}.
     *
     * @see AxisConstraintStrategies
     */
    public void setAxisConstraintStrategy(AxisConstraintStrategy axisConstraintStrategy) {
        this.axisConstraintStrategy = axisConstraintStrategy;
    }

    /**
     * Returns the current strategy in use for mouse wheel events.
     *
     * @see #setMouseWheelAxisConstraintStrategy(AxisConstraintStrategy)
     */
    public AxisConstraintStrategy getMouseWheelAxisConstraintStrategy() {
        return mouseWheelAxisConstraintStrategy;
    }

    /**
     * Sets the {@link AxisConstraintStrategy} to use for mouse wheel events, which determines which axis is allowed for
     * zooming. The default implementation is {@link AxisConstraintStrategies#getDefault()}.
     *
     * @see AxisConstraintStrategies
     */
    public void setMouseWheelAxisConstraintStrategy(AxisConstraintStrategy mouseWheelAxisConstraintStrategy) {
        this.mouseWheelAxisConstraintStrategy = mouseWheelAxisConstraintStrategy;
    }

    /**
     * If true, animates the zoom.
     */
    public boolean isZoomAnimated() {
        return zoomAnimated.get();
    }

    /**
     * If true, animates the zoom.
     */
    public void setZoomAnimated(boolean zoomAnimated) {
        this.zoomAnimated.set(zoomAnimated);
    }

    /**
     * If true, animates the zoom.
     */
    public BooleanProperty zoomAnimatedProperty() {
        return zoomAnimated;
    }

    /**
     * Returns the number of milliseconds the zoom animation takes.
     */
    public double getZoomDurationMillis() {
        return zoomDurationMillis.get();
    }

    /**
     * Sets the number of milliseconds the zoom animation takes.
     */
    public void setZoomDurationMillis(double zoomDurationMillis) {
        this.zoomDurationMillis.set(zoomDurationMillis);
    }

    /**
     * Returns the number of milliseconds the zoom animation takes.
     */
    public DoubleProperty zoomDurationMillisProperty() {
        return zoomDurationMillis;
    }

    /**
     * If true, allow zooming via mouse wheel.
     */
    public boolean isMouseWheelZoomAllowed() {
        return mouseWheelZoomAllowed.get();
    }

    /**
     * If true, allow zooming via mouse wheel.
     */
    public void setMouseWheelZoomAllowed(boolean allowed) {
        mouseWheelZoomAllowed.set(allowed);
    }

    /**
     * If true, allow zooming via mouse wheel.
     */
    public BooleanProperty mouseWheelZoomAllowedProperty() {
        return mouseWheelZoomAllowed;
    }

    /**
     * Returns the mouse filter.
     *
     * @see #setMouseFilter(EventHandler)
     */
    public EventHandler<? super MouseEvent> getMouseFilter() {
        return mouseFilter;
    }

    /**
     * Sets the mouse filter for starting the zoom action. If the filter consumes the event with
     * {@link Event#consume()}, then the event is ignored. If the filter is null, all events are
     * passed through. The default filter is {@link #DEFAULT_FILTER}.
     */
    public void setMouseFilter(EventHandler<? super MouseEvent> mouseFilter) {
        this.mouseFilter = mouseFilter;
    }

    /**
     * Start managing zoom management by adding event handlers and bindings as appropriate.
     */
    public void start() {
        handlerManager.addAllHandlers();

        selectRect.widthProperty().bind(rectX.subtract(selectRect.translateXProperty()));
        selectRect.heightProperty().bind(rectY.subtract(selectRect.translateYProperty()));
        selectRect.visibleProperty().bind(selecting);
        startLabel.visibleProperty().bind(selecting);
        endLabel.visibleProperty().bind(selecting);
    }

    /**
     * Stop managing zoom management by removing all event handlers and bindings, and hiding the
     * rectangle.
     */
    public void stop() {
        handlerManager.removeAllHandlers();
        selecting.set(false);
        selectRect.widthProperty().unbind();
        selectRect.heightProperty().unbind();
        selectRect.visibleProperty().unbind();
        startLabel.visibleProperty().unbind();
        endLabel.visibleProperty().unbind();
    }

    private boolean passesFilter(MouseEvent event) {
        if (mouseFilter != null) {
            MouseEvent cloned = (MouseEvent) event.clone();
            mouseFilter.handle(cloned);
            return !cloned.isConsumed();
        }

        return true;
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        Rectangle2D plotArea = chartInfo.getPlotArea();
        DefaultChartInputContext context = new DefaultChartInputContext(chartInfo, x, y);
        zoomMode = axisConstraintStrategy.getConstraint(context);

        if (zoomMode == AxisConstraint.Both) {
            selectRect.setTranslateX(x);
            selectRect.setTranslateY(y);
            rectX.set(x);
            rectY.set(y);
        } else if (zoomMode == AxisConstraint.Horizontal) {
            selectRect.setTranslateX(x);
            selectRect.setTranslateY(plotArea.getMinY());
            rectX.set(x);
            rectY.set(plotArea.getMaxY());

        } else if (zoomMode == AxisConstraint.Vertical) {
            selectRect.setTranslateX(plotArea.getMinX());
            selectRect.setTranslateY(y);
            rectX.set(plotArea.getMaxX());
            rectY.set(y);
        }

        Point2D dataCoordinatesY1 = chartInfo.getDataCoordinatesY1(x, y);
        startLabel.setText(minStr + new DateTime((long) dataCoordinatesY1.getX()).toString("yyyy-MM-dd HH:mm"));
    }


    private void onDragStart() {
        //Don't actually start the selecting process until it's officially a drag
        //But, we saved the original coordinates from where we started.
        if (zoomMode != AxisConstraint.None)
            selecting.set(true);
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if (!selecting.get())
            return;

        Rectangle2D plotArea = chartInfo.getPlotArea();

        if (zoomMode == AxisConstraint.Both || zoomMode == AxisConstraint.Horizontal) {
            double x = mouseEvent.getX();
            //Clamp to the selection start
            x = Math.max(x, selectRect.getTranslateX());
            //Clamp to plot area
            x = Math.min(x, plotArea.getMaxX());
            rectX.set(x);
        }

        if (zoomMode == AxisConstraint.Both || zoomMode == AxisConstraint.Vertical) {
            double y = mouseEvent.getY();
            //Clamp to the selection start
            y = Math.max(y, selectRect.getTranslateY());
            //Clamp to plot area
            y = Math.min(y, plotArea.getMaxY());
            rectY.set(y);
        }

        Point2D dataCoordinatesY1 = chartInfo.getDataCoordinatesY1(rectX.get(), rectY.get());
        endLabel.setText(maxStr + new DateTime((long) dataCoordinatesY1.getX()).toString("yyyy-MM-dd HH:mm"));
    }

    private void onMouseReleased() {
        if (!selecting.get())
            return;

        //Prevent a silly zoom... I'm still undecided about && vs ||
        if (selectRect.getWidth() == 0.0 ||
                selectRect.getHeight() == 0.0) {
            selecting.set(false);
            return;
        }

        zoomWindow1 = chartInfo.getDataCoordinatesY1(
                selectRect.getTranslateX(), selectRect.getTranslateY(),
                rectX.get(), rectY.get()
        );

        Rectangle2D zoomWindow2 = chartInfo.getDataCoordinatesY2(
                selectRect.getTranslateX(), selectRect.getTranslateY(),
                rectX.get(), rectY.get()
        );

        xAxis.setAutoRanging(false);
        y1Axis.setAutoRanging(false);
        y2Axis.setAutoRanging(false);
        if (zoomAnimated.get()) {
            zoomAnimation.stop();
            zoomAnimation.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(xAxis.lowerBoundProperty(), xAxis.getLowerBound()),
                            new KeyValue(xAxis.upperBoundProperty(), xAxis.getUpperBound()),
                            new KeyValue(y1Axis.lowerBoundProperty(), y1Axis.getLowerBound()),
                            new KeyValue(y1Axis.upperBoundProperty(), y1Axis.getUpperBound()),
                            new KeyValue(y2Axis.lowerBoundProperty(), y2Axis.getLowerBound()),
                            new KeyValue(y2Axis.upperBoundProperty(), y2Axis.getUpperBound())
                    ),
                    new KeyFrame(Duration.millis(zoomDurationMillis.get()),
                            new KeyValue(xAxis.lowerBoundProperty(), zoomWindow1.getMinX()),
                            new KeyValue(xAxis.upperBoundProperty(), zoomWindow1.getMaxX()),
                            new KeyValue(y1Axis.lowerBoundProperty(), zoomWindow1.getMinY()),
                            new KeyValue(y1Axis.upperBoundProperty(), zoomWindow1.getMaxY()),
                            new KeyValue(y2Axis.lowerBoundProperty(), zoomWindow2.getMinY()),
                            new KeyValue(y2Axis.upperBoundProperty(), zoomWindow2.getMaxY())
                    )
            );
            zoomAnimation.play();
        } else {
            zoomAnimation.stop();
            xAxis.setLowerBound(zoomWindow1.getMinX());
            xAxis.setUpperBound(zoomWindow1.getMaxX());
            y1Axis.setLowerBound(zoomWindow1.getMinY());
            y1Axis.setUpperBound(zoomWindow1.getMaxY());
            y2Axis.setLowerBound(zoomWindow2.getMinY());
            y2Axis.setUpperBound(zoomWindow2.getMaxY());
        }

        selecting.set(false);
        zoomFinished.setValue(true);

    }

    private class MouseWheelZoomHandler implements EventHandler<ScrollEvent> {
        private boolean ignoring = false;

        @Override
        public void handle(ScrollEvent event) {
            EventType<? extends Event> eventType = event.getEventType();
            if (eventType == ScrollEvent.SCROLL_STARTED) {
                //mouse wheel events never send SCROLL_STARTED
                ignoring = true;
            } else if (eventType == ScrollEvent.SCROLL_FINISHED) {
                //end non-mouse wheel event
                ignoring = false;

            } else if (eventType == ScrollEvent.SCROLL &&
                    //If we are allowing mouse wheel zooming
                    mouseWheelZoomAllowed.get() &&
                    //If we aren't between SCROLL_STARTED and SCROLL_FINISHED
                    !ignoring &&
                    //inertia from non-wheel gestures might have touch count of 0
                    !event.isInertia() &&
                    //Only care about vertical wheel events
                    event.getDeltaY() != 0 &&
                    //mouse wheel always has touch count of 0
                    event.getTouchCount() == 0) {

                //Find out which axes to zoom based on the strategy
                double eventX = event.getX();
                double eventY = event.getY();
                DefaultChartInputContext context = new DefaultChartInputContext(chartInfo, eventX, eventY);
                AxisConstraint zoomMode = mouseWheelAxisConstraintStrategy.getConstraint(context);

                if (zoomMode == AxisConstraint.None)
                    return;

                //If we are are doing a zoom animation, stop it. Also of note is that we don't zoom the
                //mouse wheel zooming. Because the mouse wheel can "fly" and generate a lot of events,
                //animation doesn't work well. Plus, as the mouse wheel changes the view a small amount in
                //a predictable way, it "looks like" an animation when you roll it.
                //We might experiment with mouse wheel zoom animation in the future, though.
                zoomAnimation.stop();

                //At this point we are a mouse wheel event, based on everything I've read
                Point2D dataCoordsY1 = chartInfo.getDataCoordinatesY1(eventX, eventY);
                Point2D dataCoordsY2 = chartInfo.getDataCoordinatesY2(eventX, eventY);

                //Determine the proportion of change to the lower and upper bounds based on how far the
                //cursor is along the axis.
                double xZoomBalance = getBalance(dataCoordsY1.getX(),
                        xAxis.getLowerBound(), xAxis.getUpperBound());
                double y1ZoomBalance = getBalance(dataCoordsY1.getY(),
                        y1Axis.getLowerBound(), y1Axis.getUpperBound());
                double y2ZoomBalance = getBalance(dataCoordsY2.getY(),
                        y2Axis.getLowerBound(), y2Axis.getUpperBound());

                //Are we zooming in or out, based on the direction of the roll
                double direction = -Math.signum(event.getDeltaY());

                //TODO: Do we need to handle "continuous" scroll wheels that don't work based on ticks?
                //If so, the 0.2 needs to be modified
                double zoomAmount = 0.2 * direction;

                if (zoomMode == AxisConstraint.Both || zoomMode == AxisConstraint.Horizontal) {
                    double xZoomDelta = (xAxis.getUpperBound() - xAxis.getLowerBound()) * zoomAmount;
                    xAxis.setAutoRanging(false);
                    xAxis.setLowerBound(xAxis.getLowerBound() - xZoomDelta * xZoomBalance);
                    xAxis.setUpperBound(xAxis.getUpperBound() + xZoomDelta * (1 - xZoomBalance));
                }

                if (zoomMode == AxisConstraint.Both || zoomMode == AxisConstraint.Vertical) {
                    double y1ZoomDelta = (y1Axis.getUpperBound() - y1Axis.getLowerBound()) * zoomAmount;
                    y1Axis.setAutoRanging(false);
                    y1Axis.setLowerBound(y1Axis.getLowerBound() - y1ZoomDelta * y1ZoomBalance);
                    y1Axis.setUpperBound(y1Axis.getUpperBound() + y1ZoomDelta * (1 - y1ZoomBalance));
                    double y2ZoomDelta = (y2Axis.getUpperBound() - y2Axis.getLowerBound()) * zoomAmount;
                    y2Axis.setAutoRanging(false);
                    y2Axis.setLowerBound(y2Axis.getLowerBound() - y2ZoomDelta * y2ZoomBalance);
                    y2Axis.setUpperBound(y2Axis.getUpperBound() + y2ZoomDelta * (1 - y2ZoomBalance));
                }
            }
        }
    }
}
