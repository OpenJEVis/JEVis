package org.jevis.jeconfig.application.Chart.Charts;

import de.jollyday.Holiday;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.tools.Holidays;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class HeatMapChart implements Chart {

    private final Integer chartId;
    private final WorkDays workDays;
    private final ColorMapping colorMapping;
    private List<ChartDataModel> chartDataModels;
    private String chartTitle;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Long ROWS;
    private Long COLS;

    private Region chartRegion;
    private String X_FORMAT;
    private String Y_FORMAT;
    private String Y2_FORMAT;
    private double maxValue;
    private Map<MatrixXY, Double> matrixData = new HashMap<>();
    private String unit;
    private org.jevis.jeconfig.application.Chart.ChartType chartType = org.jevis.jeconfig.application.Chart.ChartType.HEAT_MAP;
    private List<DateTime> xAxisList;
    private List<DateTime> yAxisList;

    public HeatMapChart(List<ChartDataModel> chartDataModels, ChartSetting chartSetting) {
        this.chartDataModels = chartDataModels;
        this.chartId = chartSetting.getId();
        this.chartTitle = chartSetting.getName();
        this.colorMapping = chartSetting.getColorMapping();
        this.ROWS = 24L;
        this.COLS = 4L;
        this.workDays = new WorkDays(chartDataModels.get(0).getObject());

        init();
    }

    private void init() {
        List<MatrixChartItem> matrixData1 = new ArrayList<>();

        ChartDataModel chartDataModel = chartDataModels.get(0);
        unit = UnitManager.getInstance().format(chartDataModel.getUnit());
        Period period = new Period(chartDataModel.getSelectedStart(), chartDataModel.getSelectedEnd());
        Period inputSampleRate = chartDataModel.getAttribute().getInputSampleRate();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        HeatMapXY heatMapXY = getHeatMapXY(period, inputSampleRate);
        COLS = heatMapXY.getX();
        ROWS = heatMapXY.getY();
        X_FORMAT = heatMapXY.getX_FORMAT();
        Y_FORMAT = heatMapXY.getY_FORMAT();
        Y2_FORMAT = heatMapXY.getY2_FORMAT();
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

        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;
        yAxisList = new ArrayList<>();
        xAxisList = new ArrayList<>();

        boolean isCustomStart = false;
        if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {

            LocalTime of = null;
            try {
                of = LocalTime.of(samples.get(0).getTimestamp().getHourOfDay(), samples.get(0).getTimestamp().getMinuteOfHour());

                if (workDays.getWorkdayStart().equals(of)) {
                    isCustomStart = true;
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        for (int y = 0; y < ROWS; y++) {
            int xCell = 0;

            if (!isCustomStart) {
                yAxisList.add(currentTS);
            } else {
                DateTime helpDate = new DateTime(currentTS.getMillis());
                for (int x = 0; x < COLS; x++) {
                    if (helpDate.getHourOfDay() == 0) {
                        yAxisList.add(helpDate);
                        break;
                    } else {
                        helpDate = helpDate.plus(inputSampleRate);
                    }
                }
            }

            for (int x = 0; x < COLS; x++) {
                if (xAxisList.size() < COLS) {
                    xAxisList.add(currentTS);
                }

                try {
                    JEVisSample jeVisSample = sampleHashMap.get(currentTS);

                    if (jeVisSample != null) {
                        Double valueAsDouble = jeVisSample.getValueAsDouble();
                        minValue = Math.min(minValue, valueAsDouble);
                        maxValue = Math.max(maxValue, valueAsDouble);
                        matrixData1.add(new MatrixChartItem(xCell, y, valueAsDouble));
                        matrixData.put(new MatrixXY(xCell, y), valueAsDouble);
                        xCell++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                currentTS = currentTS.plus(inputSampleRate);
            }
        }

        try {
            DateTime lastTs = samples.get(samples.size() - 1).getTimestamp();
            yAxisList.removeAll(yAxisList.stream().filter(dateTime -> dateTime.isAfter(lastTs)).collect(Collectors.toList()));
            ROWS = (long) yAxisList.size();
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        this.maxValue = maxValue;

        MatrixItemSeries<MatrixChartItem> matrixItemSeries1 = new MatrixItemSeries<>(matrixData1, ChartType.MATRIX_HEATMAP);

        MatrixPane<MatrixChartItem> matrixHeatMap = new MatrixPane<>(matrixItemSeries1);
        matrixHeatMap.setMaxHeight(8192);
        matrixHeatMap.setColorMapping(colorMapping);
        matrixHeatMap.getMatrix().setUseSpacer(false);
        matrixHeatMap.getMatrix().setColsAndRows(COLS.intValue(), ROWS.intValue());

        GridPane leftAxis = new GridPane();
        leftAxis.setPadding(new Insets(4));

        GridPane rightAxis = new GridPane();
        rightAxis.setPadding(new Insets(4));

        int row = 0;
        for (DateTime dateTime : yAxisList) {
            Label tsLeft = new Label(dateTime.toString(Y_FORMAT));

            Label tsRight = new Label(dateTime.toString(Y2_FORMAT));
            String toolTipString = "";
            if (Holidays.getDefaultHolidayManager().isHoliday(dateTime.toCalendar(I18n.getInstance().getLocale()))
                    || (Holidays.getSiteHolidayManager() != null && Holidays.getSiteHolidayManager().isHoliday(dateTime.toCalendar(I18n.getInstance().getLocale()), Holidays.getStateCode()))
                    || (Holidays.getCustomHolidayManager() != null && Holidays.getCustomHolidayManager().isHoliday(dateTime.toCalendar(I18n.getInstance().getLocale())))) {
                LocalDate localDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());

                Set<Holiday> holidays = Holidays.getDefaultHolidayManager().getHolidays(localDate, localDate, "");
                if (Holidays.getSiteHolidayManager() != null) {
                    holidays.addAll(Holidays.getSiteHolidayManager().getHolidays(localDate, localDate, Holidays.getStateCode()));
                }

                if (Holidays.getCustomHolidayManager() != null) {
                    holidays.addAll(Holidays.getCustomHolidayManager().getHolidays(localDate, localDate, Holidays.getStateCode()));
                }

                for (Holiday holiday : holidays) {
                    toolTipString = holiday.getDescription(I18n.getInstance().getLocale());
                }
            }
            if (dateTime.getDayOfWeek() == 6 || dateTime.getDayOfWeek() == 7 || !toolTipString.equals("")) {
                if (dateTime.getDayOfWeek() == 6 || dateTime.getDayOfWeek() == 7) {
                    if (toolTipString.equals("")) {
                        toolTipString = DateTimeFormat.forPattern("EEEE").print(dateTime);
                    } else {
                        toolTipString += ", " + dateTime.toString("dddd");
                    }
                }
                tsRight.setTextFill(Color.RED);
                if (!toolTipString.equals("")) {
                    Tooltip tooltip = new Tooltip(toolTipString);
                    Tooltip.install(tsRight, tooltip);
                }
            }

            leftAxis.add(tsLeft, 0, row);
            rightAxis.add(tsRight, 0, row);
            GridPane.setFillWidth(tsLeft, true);
            GridPane.setFillWidth(tsRight, true);
            row++;
            if (yAxisList.indexOf(dateTime) < yAxisList.size() - 1) {
                Region leftSeparator = new Region();
                leftSeparator.setPrefHeight(0.05);
                leftAxis.add(leftSeparator, 0, row);

                Region rightSeparator = new Region();
                rightSeparator.setPrefHeight(0.05);
                rightAxis.add(rightSeparator, 0, row);
                row++;
            }
        }

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        Label titleLabel = new Label(chartTitle);
        titleLabel.getStyleClass().setAll("chart-title");
        titleBox.setPadding(new Insets(8));
        titleLabel.setAlignment(Pos.CENTER);
        titleBox.getChildren().setAll(titleLabel);

        HBox spHor = new HBox();
        spHor.getChildren().setAll(leftAxis, matrixHeatMap, rightAxis);
        HBox.setHgrow(matrixHeatMap, Priority.ALWAYS);

        GridPane bottomAxis = new GridPane();
        bottomAxis.setHgap(0);
        bottomAxis.setMinHeight(30d);

        HBox legend = new HBox();
        legend.setPadding(new Insets(8));
        legend.setSpacing(4);
        legend.setAlignment(Pos.CENTER);

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

        ScrollPane sp = new ScrollPane(spVer);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setFitToWidth(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        setRegion(sp);
    }

    public final Double getValueAt(final LinearGradient GRADIENT, Color color) {
        List<Stop> stops = GRADIENT.getStops();
        Stop foundStop = null;

        for (Stop stop : stops) {
            if (stop.getColor().equals(color)) {
                foundStop = stop;
                break;
            }
        }

        if (foundStop != null) {
            return foundStop.getOffset();
        } else return null;
    }

    @Override
    public String getChartName() {
        return chartTitle;
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return chartId;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {

    }

    @Override
    public void updateTableZoom(double lowerBound, double upperBound) {

    }

    @Override
    public void applyColors() {

    }

    @Override
    public de.gsi.chart.XYChart getChart() {
        return null;
    }

    @Override
    public org.jevis.jeconfig.application.Chart.ChartType getChartType() {
        return chartType;
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
    public List<ChartDataModel> getChartDataModels() {
        return chartDataModels;
    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return null;
    }

    private HeatMapXY getHeatMapXY(Period period, Period inputSampleRate) {
        long y;
        String y_Format = "HH";
        String y2_Format = "EEEE";
        long x;
        String x_Format = "mm";
        AggregationPeriod aggregationPeriod;

        if (period.getYears() > 1 || period.getMonths() >= 12 || period.getWeeks() >= 52 || period.getDays() >= 365) {
            int years = period.getYears();
            int months = period.getMonths();
            Period newPeriod = period.minusYears(years).minusMonths(months);
            y = newPeriod.toStandardDays().getDays();
            y += (years * 365.25) + (months * 31);
            y_Format = "yyyy-MM";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 31;
                x_Format = "dd";
                aggregationPeriod = AggregationPeriod.DAILY;
            } else {
                x = 31;
                x_Format = "dd";
                aggregationPeriod = AggregationPeriod.DAILY;
            }
        } else if (period.getYears() == 1 || period.getMonths() >= 12 || period.getWeeks() >= 52 || period.getDays() >= 365) {
            int months = period.getMonths();
            Period newPeriod = period.minusYears(1).minusMonths(months);
            y = newPeriod.toStandardDays().getDays();
            y += months * 31;
            y_Format = "yyyy-MM";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 31;
                x_Format = "dd";
                aggregationPeriod = AggregationPeriod.DAILY;
            } else {
                x = 31;
                x_Format = "dd";
                aggregationPeriod = AggregationPeriod.DAILY;
            }
        } else if (period.getMonths() > 1 || period.getWeeks() >= 4 || period.getDays() >= 31) {
            int months = period.getMonths();
            Period newPeriod = period.minusMonths(months);
            y = newPeriod.toStandardDays().getDays() + 1;
            y += months * 31;
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
        } else if (period.getMonths() == 1 || period.getWeeks() >= 4 || period.getDays() >= 31) {
            Period newPeriod = period.minusMonths(1);
            y = newPeriod.toStandardDays().getDays() * 7 + 1;
            y += 4 * 7;
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
        } else if (period.getWeeks() > 1 || period.getDays() >= 7) {
            y = period.getWeeks() * 7 + 1;
            y_Format = "MM-dd";
            if (inputSampleRate.equals(Period.minutes(15))) {
                x = 24 * 4;
                x_Format = "HH:mm";
                aggregationPeriod = AggregationPeriod.NONE;
            } else {
                x = 24;
                x_Format = "HH";
                aggregationPeriod = AggregationPeriod.HOURLY;
            }
        } else if (period.getWeeks() == 1 || period.getDays() >= 7) {
            y = period.toStandardDays().getDays() + 1;
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
            y = period.getDays() + 1;
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

        return new HeatMapXY(x, x_Format, y, y_Format, y2_Format, aggregationPeriod);
    }

    public double getMaxValue() {
        return maxValue;
    }

    public Long getROWS() {
        return ROWS;
    }

    public Long getCOLS() {
        return COLS;
    }

    public Map<MatrixXY, Double> getMatrixData() {
        return matrixData;
    }

    public String getUnit() {
        return unit;
    }

    public List<DateTime> getxAxisList() {
        return xAxisList;
    }

    public String getX_FORMAT() {
        return X_FORMAT;
    }

    public String getY_FORMAT() {
        return Y_FORMAT;
    }

    public String getY2_FORMAT() {
        return Y2_FORMAT;
    }

    public List<DateTime> getyAxisList() {
        return yAxisList;
    }
}
