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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;

/**
 * ChartPanManager manages drag gestures on an {@link XYChart} by translating them to panning
 * actions on the chart's axes.
 *
 * @author Jason Winnebeck
 */
public class ChartPanManager {
    /**
     * The default mouse filter for the {@link ChartPanManager} filters events unless only primary
     * mouse button (usually left) is depressed.
     */
    public static final EventHandler<MouseEvent> DEFAULT_FILTER = ChartZoomManager.DEFAULT_FILTER;

    private final EventHandlerManager handlerManager;

    private final ValueAxis<?> xAxis;
    private final ValueAxis<?> y1Axis;
    private final ValueAxis<?> y2Axis;
    private final XYChartInfo chartInfo;

    private AxisConstraint panMode = AxisConstraint.None;
    private AxisConstraintStrategy axisConstraintStrategy = AxisConstraintStrategies.getDefault();

    private EventHandler<? super MouseEvent> mouseFilter = DEFAULT_FILTER;

    private boolean dragging = false;

    private boolean wasXAnimated;
    private boolean wasY1Animated;
    private boolean wasY2Animated;

    private double lastX;
    private double lastY1;
    private double lastY2;

    public ChartPanManager(MultiAxisChart<?, ?> chart) {
        handlerManager = new EventHandlerManager(chart);
        xAxis = (ValueAxis<?>) chart.getXAxis();
        y1Axis = (ValueAxis<?>) chart.getY1Axis();
        y2Axis = (ValueAxis<?>) chart.getY2Axis();
        chartInfo = new XYChartInfo(chart, chart);

        handlerManager.addEventHandler(false, MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (passesFilter(mouseEvent))
                    startDrag(mouseEvent);
            }
        });

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                drag(mouseEvent);
            }
        });

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                release();
            }
        });
    }

    /**
     * Returns the current strategy in use.
     *
     * @see #setAxisConstraintStrategy(AxisConstraintStrategy)
     */
    public AxisConstraintStrategy getAxisConstraintStrategy() {
        return axisConstraintStrategy;
    }

    /**
     * Sets the {@link AxisConstraintStrategy} to use, which determines which axis is allowed for panning. The default
     * implementation is {@link AxisConstraintStrategies#getDefault()}.
     *
     * @see AxisConstraintStrategies
     */
    public void setAxisConstraintStrategy(AxisConstraintStrategy axisConstraintStrategy) {
        this.axisConstraintStrategy = axisConstraintStrategy;
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
     * Sets the mouse filter for starting the pan action. If the filter consumes the event with
     * {@link Event#consume()}, then the event is ignored. If the filter is null, all events are
     * passed through. The default filter is {@link #DEFAULT_FILTER}.
     */
    public void setMouseFilter(EventHandler<? super MouseEvent> mouseFilter) {
        this.mouseFilter = mouseFilter;
    }

    public void start() {
        handlerManager.addAllHandlers();
    }

    public void stop() {
        handlerManager.removeAllHandlers();
        release();
    }

    private boolean passesFilter(MouseEvent event) {
        if (mouseFilter != null) {
            MouseEvent cloned = (MouseEvent) event.clone();
            mouseFilter.handle(cloned);
            return !cloned.isConsumed();
        }

        return true;
    }

    private void startDrag(MouseEvent event) {
        DefaultChartInputContext context = new DefaultChartInputContext(chartInfo, event.getX(), event.getY());
        panMode = axisConstraintStrategy.getConstraint(context);

        if (panMode != AxisConstraint.None) {
            lastX = event.getX();
            lastY1 = event.getY();
            lastY2 = event.getY();

            wasXAnimated = xAxis.getAnimated();
            wasY1Animated = y1Axis.getAnimated();
            wasY2Animated = y2Axis.getAnimated();

            xAxis.setAnimated(false);
            xAxis.setAutoRanging(false);
            y1Axis.setAnimated(false);
            y1Axis.setAutoRanging(false);
            y2Axis.setAnimated(false);
            y2Axis.setAutoRanging(false);

            dragging = true;
        }
    }

    private void drag(MouseEvent event) {
        if (!dragging)
            return;

        if (panMode == AxisConstraint.Both || panMode == AxisConstraint.Horizontal) {
            double dX = (event.getX() - lastX) / -xAxis.getScale();
            lastX = event.getX();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(xAxis.getLowerBound() + dX);
            xAxis.setUpperBound(xAxis.getUpperBound() + dX);
        }

        if (panMode == AxisConstraint.Both || panMode == AxisConstraint.Vertical) {
            double dY1 = (event.getY() - lastY1) / -y1Axis.getScale();
            double dY2 = (event.getY() - lastY2) / -y2Axis.getScale();
            lastY1 = event.getY();
            y1Axis.setAutoRanging(false);
            y1Axis.setLowerBound(y1Axis.getLowerBound() + dY1);
            y1Axis.setUpperBound(y1Axis.getUpperBound() + dY1);
            lastY2 = event.getY();
            y2Axis.setAutoRanging(false);
            y2Axis.setLowerBound(y2Axis.getLowerBound() + dY2);
            y2Axis.setUpperBound(y2Axis.getUpperBound() + dY2);
        }
    }

    private void release() {
        if (!dragging)
            return;

        dragging = false;

        xAxis.setAnimated(wasXAnimated);
        y1Axis.setAnimated(wasY1Animated);
        y2Axis.setAnimated(wasY2Animated);
    }
}
