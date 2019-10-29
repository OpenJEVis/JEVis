package org.jevis.jeconfig.application.Chart.Charts;

import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.MatrixPane;
import eu.hansolo.fx.charts.data.MatrixChartItem;
import eu.hansolo.fx.charts.series.MatrixItemSeries;
import eu.hansolo.fx.charts.tools.ColorMapping;
import eu.hansolo.fx.charts.tools.Helper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HeatMapChart implements Chart {

    private List<ChartDataModel> chartDataModels;
    private String chartTitle;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Long Y_MAX;
    private Long X_MAX;

    private Region chartRegion;
    private String X_FORMAT;
    private String Y_FORMAT;

    public HeatMapChart(List<ChartDataModel> chartDataModels, String chartTitle) {
        this.chartDataModels = chartDataModels;
        this.chartTitle = chartTitle;
        this.Y_MAX = 24L;
        this.X_MAX = 4L;

        init();
    }

    private void init() {
        List<MatrixChartItem> matrixData1 = new ArrayList<>();

        ChartDataModel chartDataModel = chartDataModels.get(0);
        String unit = UnitManager.getInstance().format(chartDataModel.getUnit());
        Period period = new Period(chartDataModel.getSelectedStart(), chartDataModel.getSelectedEnd());
        Period inputSampleRate = chartDataModel.getAttribute().getInputSampleRate();

        HeatMapXY heatMapXY = getHeatMapXY(period, inputSampleRate);
        X_MAX = heatMapXY.getX();
        Y_MAX = heatMapXY.getY();
        X_FORMAT = heatMapXY.getX_FORMAT();
        System.out.println("X: " + X_FORMAT);
        Y_FORMAT = heatMapXY.getY_FORMAT();
        System.out.println("Y: " + Y_FORMAT);
        chartDataModel.setAggregationPeriod(heatMapXY.getAggregationPeriod());

        List<JEVisSample> samples = chartDataModel.getSamples();
        try {
            inputSampleRate = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());

        } catch (JEVisException e) {
            e.printStackTrace();
        }

        HashMap<DateTime, JEVisSample> sampleHashMap = new HashMap<>();
        samples.forEach(jeVisSample -> {
            try {
                sampleHashMap.put(jeVisSample.getTimestamp(), jeVisSample);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });
        DateTime currentTS = null;
        try {
            currentTS = samples.get(0).getTimestamp();
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        int cellX = 0;
        int cellY = 0;
        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;
        List<DateTime> yAxisList = new ArrayList<>();
        List<DateTime> xAxisList = new ArrayList<>();
        for (int y = 0; y < Y_MAX; y++) {
            cellX = 0;
            yAxisList.add(currentTS);
            for (int x = 0; x < X_MAX; x++) {
                if (xAxisList.size() < X_MAX) {
                    xAxisList.add(currentTS);
                }
                try {
                    JEVisSample jeVisSample = sampleHashMap.get(currentTS);

                    if (jeVisSample != null) {
                        Double valueAsDouble = jeVisSample.getValueAsDouble();
                        minValue = Math.min(minValue, valueAsDouble);
                        maxValue = Math.max(maxValue, valueAsDouble);
                        matrixData1.add(new MatrixChartItem(cellX, cellY, valueAsDouble));
                    } else {
                        matrixData1.add(new MatrixChartItem(cellX, cellY, 0d));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentTS = currentTS.plus(inputSampleRate);
                cellX++;
            }
            cellY++;
        }

        MatrixItemSeries<MatrixChartItem> matrixItemSeries1 = new MatrixItemSeries<>(matrixData1, ChartType.MATRIX_HEATMAP);

        MatrixPane matrixHeatMap = new MatrixPane<>(matrixItemSeries1);
        matrixHeatMap.setColorMapping(ColorMapping.GREEN_YELLOW_RED);
        matrixHeatMap.getMatrix().setUseSpacer(false);
        matrixHeatMap.getMatrix().setColsAndRows(X_MAX.intValue(), Y_MAX.intValue());

        GridPane leftAxis = new GridPane();
        leftAxis.setPadding(new Insets(4));

        int row = 0;
        for (DateTime dateTime : yAxisList) {
            Label ts = new Label(dateTime.toString(Y_FORMAT));
            leftAxis.add(ts, 0, row);
            GridPane.setFillWidth(ts, true);
            row++;
            if (yAxisList.indexOf(dateTime) < yAxisList.size() - 1) {
                Region separator = new Region();
                separator.setPrefHeight(0.05);
                leftAxis.add(separator, 0, row);
                row++;
            }
        }

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        Label titleLabel = new Label(chartTitle);
        titleLabel.getStyleClass().setAll("chart-title");
        titleLabel.setAlignment(Pos.CENTER);
        titleBox.getChildren().setAll(titleLabel);

        HBox spHor = new HBox();
        spHor.getChildren().setAll(leftAxis, matrixHeatMap);
        HBox.setHgrow(matrixHeatMap, Priority.ALWAYS);


        GridPane bottomAxis = new GridPane();
        bottomAxis.setMinHeight(30d);
        Region freeSpace = new Region();
        bottomAxis.add(freeSpace, 0, 0);

        int col = 1;
//        boolean odd = true;
        for (DateTime dateTime : xAxisList) {
            Label ts = new Label(dateTime.toString(X_FORMAT));

            if ((xAxisList.indexOf(dateTime) == 0) || (xAxisList.indexOf(dateTime) + 1) % 5 == 0) {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                Rectangle rectangle = new Rectangle(2, 10);
                hBox.getChildren().addAll(rectangle);
                bottomAxis.add(hBox, col, 0);

                bottomAxis.add(ts, col, 1, 4, 1);
            } else {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                Rectangle rectangle = new Rectangle(1, 7);
                hBox.getChildren().addAll(rectangle);
                bottomAxis.add(hBox, col, 0);
            }

            if (xAxisList.indexOf(dateTime) < xAxisList.size() - 1) {
                Region separator = new Region();
                separator.setPrefWidth(0.05);
                bottomAxis.add(separator, col, 0);
            }

            col++;
        }

        HBox legend = new HBox();
        legend.setSpacing(4);
        legend.setAlignment(Pos.CENTER);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        for (int i = 0; i <= 100; i = i + 20) {
            Color color = Helper.getColorAt(matrixHeatMap.getMatrixGradient(), i / 100d);
            Rectangle rectangle = new Rectangle(16, 16, color);
            Label label = new Label();

            if (i == 0) {
                label.setText(i + "% (" + numberFormat.format(minValue) + " " + unit + ")");
            } else {
                double v = (maxValue - minValue) * (i / 100d) + minValue;
                label.setText(i + "% (" + numberFormat.format(v) + " " + unit + ")");
            }
            legend.getChildren().addAll(rectangle, label);
        }

        VBox spVer = new VBox();
        spVer.getChildren().setAll(titleBox, spHor, bottomAxis, legend);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        VBox.setVgrow(spHor, Priority.ALWAYS);

        setRegion(spVer);
    }

    @Override
    public String getChartName() {
        return null;
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {

    }

    @Override
    public void updateTableZoom(Long lowerBound, Long upperBound) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {

    }

    @Override
    public void applyColors() {

    }

    @Override
    public DateTime getValueForDisplay() {
        return null;
    }

    @Override
    public void setValueForDisplay(DateTime valueForDisplay) {

    }

    @Override
    public DateTime getNearest() {
        return null;
    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return null;
    }

    @Override
    public Region getRegion() {
        return chartRegion;
    }

    @Override
    public void setRegion(Region region) {
        this.chartRegion = region;
    }

    @Override
    public void checkForY2Axis() {

    }

    @Override
    public void initializeZoom() {

    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return null;
    }

    @Override
    public DateTime getStartDateTime() {
        return null;
    }

    @Override
    public DateTime getEndDateTime() {
        return null;
    }

    @Override
    public void updateChart() {
        init();
    }

    @Override
    public void setDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
    }

    @Override
    public void setHideShowIcons(Boolean hideShowIcons) {

    }

    @Override
    public void setChartSettings(ChartSettingsFunction function) {

    }

    @Override
    public ChartPanManager getPanner() {
        return null;
    }

    @Override
    public JFXChartUtil getJfxChartUtil() {
        return null;
    }

    private HeatMapXY getHeatMapXY(Period period, Period inputSampleRate) {
        long y;
        String y_Format = "HH";
        long x;
        String x_Format = "mm";
        AggregationPeriod aggregationPeriod;

        if (period.getYears() > 1) {
            int years = period.getYears();
            int months = period.getMonths();
            Period newPeriod = period.minusYears(years).minusMonths(months);
            y = newPeriod.toStandardDays().getDays();
            y += (years * 365.25) + (months * 30.25);
            y_Format = "yyyy-MM-dd";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getYears() == 1) {
            int months = period.getMonths();
            Period newPeriod = period.minusYears(1).minusMonths(months);
            y = newPeriod.toStandardDays().getDays();
            y += months * 30.25;
            y_Format = "MM-dd (dddd)";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getMonths() > 1) {
            int months = period.getMonths();
            Period newPeriod = period.minusMonths(months);
            y = newPeriod.toStandardDays().getDays();
            y += months * 30.25;
            y_Format = "MM-dd (dddd)";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getMonths() == 1) {
            Period newPeriod = period.minusMonths(1);
            y = newPeriod.toStandardDays().getDays() * 7;
            y += 4 * 7;
            y_Format = "MM-dd (dddd)";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getWeeks() > 1) {
            y = period.getWeeks() * 7;
            y_Format = "MM-dd (dddd)";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getWeeks() == 1) {
            y = period.toStandardDays().getDays();
            y_Format = "dd";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getDays() > 1) {
            y = period.getDays();
            y_Format = "dd";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getDays() == 1) {
            y = period.toStandardHours().getHours();
            x = 4;
            aggregationPeriod = AggregationPeriod.NONE;
        } else if (period.getHours() > 1) {
            y = period.getHours();
            y_Format = "HH";
            x = 4;
            x_Format = "mm";
            aggregationPeriod = AggregationPeriod.NONE;
        } else {
            y = 1;
            y_Format = "HH";
            x = 4;
            x_Format = "mm";
            aggregationPeriod = AggregationPeriod.NONE;
        }

        return new HeatMapXY(x, x_Format, y, y_Format, aggregationPeriod);
    }
}
