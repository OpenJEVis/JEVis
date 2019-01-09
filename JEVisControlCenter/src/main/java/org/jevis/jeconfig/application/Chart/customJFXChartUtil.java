package org.jevis.jeconfig.application.Chart;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.gillius.jfxutils.JFXUtil;
import org.gillius.jfxutils.chart.ChartZoomManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

public class customJFXChartUtil extends JFXChartUtil {

    public static Region setupZooming(XYChart<?, ?> chart, EventHandler<? super MouseEvent> mouseFilter) {
        StackPane chartPane = new StackPane();
        if (chart.getParent() != null) {
            JFXUtil.replaceComponent(chart, chartPane);
        }

        Rectangle selectRect = new Rectangle(0.0D, 0.0D, 0.0D, 0.0D);
        selectRect.setFill(Color.DODGERBLUE);
        selectRect.setMouseTransparent(true);
        selectRect.setOpacity(0.3D);
        selectRect.setStroke(Color.rgb(0, 41, 102));
        selectRect.setStrokeType(StrokeType.INSIDE);
        selectRect.setStrokeWidth(3.0D);
        StackPane.setAlignment(selectRect, Pos.TOP_LEFT);
        chartPane.getChildren().addAll(chart, selectRect);
        ChartZoomManager zoomManager = new ChartZoomManager(chartPane, selectRect, chart);
        zoomManager.setMouseWheelZoomAllowed(false);
        zoomManager.setMouseFilter(mouseFilter);
        zoomManager.start();
        return chartPane;
    }
}
