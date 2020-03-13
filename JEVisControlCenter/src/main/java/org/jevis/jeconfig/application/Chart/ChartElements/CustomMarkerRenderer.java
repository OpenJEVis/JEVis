package org.jevis.jeconfig.application.Chart.ChartElements;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.renderer.spi.LabelledMarkerRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class CustomMarkerRenderer extends LabelledMarkerRenderer {

    private List<XYChartSerie> xyChartSerieList;

    public CustomMarkerRenderer(List<XYChartSerie> xyChartSerieList) {
        super();

        this.xyChartSerieList = xyChartSerieList;
    }

    @Override
    protected void drawVerticalLabelledMarker(GraphicsContext gc, XYChart chart, DataSet dataSet, int indexMin, int indexMax) {

        final Axis xAxis = chart.getXAxis();
        Axis yAxis = null;

        DoubleDataSet doubleDataSet = (DoubleDataSet) dataSet;

        XYChartSerie currentSerie = null;
        for (XYChartSerie serie : xyChartSerieList) {
            if (serie.getNoteDataSet().equals(doubleDataSet)) {
                currentSerie = serie;
                break;
            }
        }

        if (currentSerie == null || currentSerie.getSingleRow().getAxis() == 0) {
            yAxis = chart.getYAxis();
        } else {
            for (Axis axis : chart.getAxes()) {
                if (axis.getSide() == Side.RIGHT) {
                    yAxis = axis;
                    break;
                }
            }
        }

        gc.save();
        setGraphicsContextAttributes(gc, dataSet.getStyle());
        gc.setTextAlign(TextAlignment.LEFT);

        final double height = chart.getCanvas().getHeight();
        double lastLabel = -Double.MAX_VALUE;
        double lastFontSize = 0;
        for (int i = indexMin; i < indexMax; i++) {
            final double screenX = (int) xAxis.getDisplayPosition(dataSet.get(DataSet.DIM_X, i));
            final double screenY = (int) yAxis.getDisplayPosition(dataSet.get(DataSet.DIM_Y, i));
            final String label = dataSet.getDataLabel(i);

            final String pointStyle = dataSet.getStyle(i);

            if (pointStyle != null) {
                gc.save();
                setGraphicsContextAttributes(gc, pointStyle);
            }


            final Text text = new Text(label);
            text.setFont(Font.font("Helvetica", 12));

            final double textWidth = text.getLayoutBounds().getWidth();
            final double textHeight = text.getLayoutBounds().getHeight();

            double widthRect = textWidth + 6;
            double heightRect = textHeight;
            double xRect = screenX - widthRect / 2;
            double yRect = screenY - heightRect / 2;

            gc.setLineWidth(1.5);
            gc.strokeRect(xRect, yRect, widthRect, heightRect);
//            gc.strokeLine(screenX, 0, screenX, height);

            if (Math.abs(screenX - lastLabel) > lastFontSize && !label.isEmpty()) {
                gc.save();
                gc.setLineWidth(0.8);
                gc.setLineDashes(1.0);
//                gc.translate(Math.ceil(xPos + 3), Math.ceil(0.01 * height));
//                gc.rotate(+90);
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Helvetica", 12));
                gc.fillText(label, xRect + 3, yRect + heightRect - 3);
                gc.restore();
                lastLabel = screenX;
                lastFontSize = gc.getFont().getSize();
            }
            if (pointStyle != null) {
                gc.restore();
            }
        }
        gc.restore();
    }
}
