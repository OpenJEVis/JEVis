package org.jevis.jecc.application.Chart.Charts;

import com.ibm.icu.text.NumberFormat;
import de.focus_shift.jollyday.core.Holiday;
import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.MatrixPane;
import eu.hansolo.fx.charts.data.MatrixChartItem;
import eu.hansolo.fx.charts.series.MatrixItemSeries;
import eu.hansolo.fx.charts.tools.Helper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.application.Chart.ChartElements.TableEntry;
import org.jevis.jecc.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.application.tools.ColorHelper;
import org.jevis.jecc.application.tools.Holidays;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class HeatMapChart implements Chart {
    private static final Logger logger = LogManager.getLogger(HeatMapChart.class);
    private final WorkDays workDays;
    private final List<ChartDataRow> chartDataRows = new ArrayList<>();
    private final ChartModel chartModel;
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private final Color backgroundColor;
    private final Color fontColor;
    private final JEVisDataSource ds;
    private final Map<MatrixXY, Double> matrixData = new HashMap<>();
    private final org.jevis.jecc.application.Chart.ChartType chartType = org.jevis.jecc.application.Chart.ChartType.HEAT_MAP;
    private final long SECOND_MILLIS = 1000;
    private final long MINUTE_MILLIS = SECOND_MILLIS * 60L;
    private final long QUARTER_HOUR_MILLIS = MINUTE_MILLIS * 15L;
    private final long HOUR_MILLIS = MINUTE_MILLIS * 60L;
    private final long DAY_MILLIS = HOUR_MILLIS * 24L;
    private final long WEEK_MILLIS = DAY_MILLIS * 7L;
    private final long MIN_MONTH_MILLIS = DAY_MILLIS * 28L;
    private final long MAX_MONTH_MILLIS = DAY_MILLIS * 31L;
    private final long MIN_YEAR_MILLIS = DAY_MILLIS * 365L;
    private final long MAX_YEAR_MILLIS = DAY_MILLIS * 366L;
    private Long ROWS;
    private Long COLS;
    private Region chartRegion;
    private String X_FORMAT;
    private String Y_FORMAT;
    private String Y2_FORMAT;
    private double maxValue;
    private String unit;
    private List<DateTime> xAxisList;
    private List<DateTime> yAxisList;
    private Period period;

    public HeatMapChart(JEVisDataSource ds, ChartModel chartModel) {
        this(ds, chartModel, new ArrayList<>(), null, null);
    }

    public HeatMapChart(JEVisDataSource ds, ChartModel chartModel, List<ChartDataRow> chartDataRows, Color backgroundColor, Color fontColor) {
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;

        this.ds = ds;
        this.chartModel = chartModel;
        this.ROWS = 24L;
        this.COLS = 4L;

        if (chartDataRows.isEmpty()) {
            chartModel.getChartData().stream().map(chartData -> new ChartDataRow(ds, chartData)).forEach(this.chartDataRows::add);
        } else this.chartDataRows.addAll(chartDataRows);

        this.workDays = new WorkDays(this.chartDataRows.get(0).getObject());

        init();
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
        return chartModel.getChartName();
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return chartModel.getChartId();
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
    public void setChart(de.gsi.chart.Chart chart) {
        if (chart == null) {
            chartRegion = null;
        }
    }

    @Override
    public org.jevis.jecc.application.Chart.ChartType getChartType() {
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
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }

    @Override
    public List<XYChartSerie> getXyChartSerieList() {
        return null;
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
    public void setPeriod(Period period) {
        this.period = period;
    }

    private void init() {
        List<MatrixChartItem> matrixData1 = new ArrayList<>();

        ChartDataRow chartDataRow = chartDataRows.get(0);
        unit = UnitManager.getInstance().format(chartDataRow.getUnit());
        Interval interval = new Interval(chartDataRow.getSelectedStart(), chartDataRow.getSelectedEnd());
        Period inputSampleRate = chartDataRow.getPeriod();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(chartModel.getMinFractionDigits());
        numberFormat.setMaximumFractionDigits(chartModel.getMaxFractionDigits());

        AggregationPeriod selectedAggregation = chartDataRow.getAggregationPeriod();
        ManipulationMode selectedManipulationMode = chartDataRow.getManipulationMode();

        StringBuilder errorMsg = new StringBuilder();
        if (selectedManipulationMode != ManipulationMode.NONE) {
            errorMsg.append(I18n.getInstance().getString("plugin.chart.heatmap.warning.manipulation"));
            errorMsg.append("\n");
        }

        if (selectedAggregation != AggregationPeriod.NONE) {
            errorMsg.append(I18n.getInstance().getString("plugin.chart.heatmap.warning.aggregation"));
        }

        if (!errorMsg.toString().isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING, errorMsg.toString(), ButtonType.OK);
            Platform.runLater(() -> {
                warning.show();
                warning.getDialogPane().toFront();
            });
        }

        HeatMapXY heatMapXY = getHeatMapXY(interval, inputSampleRate);
        COLS = heatMapXY.getX();
        ROWS = heatMapXY.getY();
        X_FORMAT = heatMapXY.getX_FORMAT();
        Y_FORMAT = heatMapXY.getY_FORMAT();
        Y2_FORMAT = heatMapXY.getY2_FORMAT();
        chartDataRow.setAggregationPeriod(heatMapXY.getAggregationPeriod());
        chartDataRow.setManipulationMode(ManipulationMode.NONE);

        List<JEVisSample> samples = chartDataRow.getSamples();
        DateTime currentTS = null;

        if (samples.size() > 1) {
            try {
                inputSampleRate = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                currentTS = samples.get(0).getTimestamp();

            } catch (JEVisException e) {
                logger.error("Error while getting input sample rate", e);
            }
        } else {
            logger.warn("Only got {} samples, fallback to default", samples.size());
            inputSampleRate = chartDataRow.getPeriod();
            currentTS = chartDataRow.getSelectedStart();
        }

        HashMap<DateTime, JEVisSample> sampleHashMap = new HashMap<>();
        samples.forEach(jeVisSample -> {
            try {
                sampleHashMap.put(jeVisSample.getTimestamp(), jeVisSample);
            } catch (JEVisException e) {
                logger.error("Error while getting sample timestamp of sample {}", jeVisSample, e);
            }
        });

        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;
        yAxisList = new ArrayList<>();
        xAxisList = new ArrayList<>();

        boolean isCustomStart = false;
        if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
            LocalTime of = LocalTime.of(chartDataRow.getSelectedStart().getHourOfDay(), chartDataRow.getSelectedStart().getMinuteOfHour());

            if (workDays.getWorkdayStart().equals(of)) {
                isCustomStart = true;
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
                    logger.error("Error while processing sample {}", sampleHashMap.get(currentTS), e);
                }

                currentTS = currentTS.plus(inputSampleRate);
            }
        }

        DateTime lastTs = chartDataRow.getSelectedEnd();
        yAxisList.removeAll(yAxisList.stream().filter(dateTime -> dateTime.isAfter(lastTs)).collect(Collectors.toList()));
        ROWS = (long) yAxisList.size();

        this.maxValue = maxValue;

        MatrixItemSeries<MatrixChartItem> matrixItemSeries1 = new MatrixItemSeries<>(matrixData1, ChartType.MATRIX_HEATMAP);

        MatrixPane<MatrixChartItem> matrixHeatMap = new MatrixPane<>(matrixItemSeries1);
        matrixHeatMap.setMaxHeight(8192);
        matrixHeatMap.setColorMapping(chartModel.getColorMapping());
        matrixHeatMap.getMatrix().setUseSpacer(false);
        matrixHeatMap.getMatrix().setColsAndRows(COLS.intValue(), ROWS.intValue());

        GridPane leftAxis = new GridPane();
        leftAxis.setPadding(new Insets(4));

        GridPane rightAxis = new GridPane();
        rightAxis.setPadding(new Insets(4));

        JEVisObject site = null;
        try {
            site = CommonMethods.getFirstParentalObjectOfClass(chartDataRow.getObject(), "Building");
        } catch (Exception e) {
            logger.error("Could not get site object", e);
        }

        int row = 0;
        for (DateTime dateTime : yAxisList) {
            Label tsLeft = new Label();
            if (!Y_FORMAT.isEmpty()) {
                tsLeft.setText(dateTime.toString(Y_FORMAT));
            }

            if (fontColor != null) {
                tsLeft.setTextFill(fontColor);
                tsLeft.setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(fontColor) + "!important;");
            }

            Label tsRight = new Label();
            if (!Y2_FORMAT.isEmpty()) {
                tsRight.setText(dateTime.toString(Y2_FORMAT));
            }
            if (fontColor != null) {
                tsRight.setTextFill(fontColor);
                tsRight.setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(fontColor) + "!important;");
            }

            String toolTipString = "";
            Calendar dtToCal = dateTime.toCalendar(I18n.getInstance().getLocale());

            if (Holidays.getDefaultHolidayManager().isHoliday(dtToCal)
                    || (Holidays.getSiteHolidayManager(site) != null && Holidays.getSiteHolidayManager(site).isHoliday(dtToCal, Holidays.getStateCode()))
                    || (Holidays.getCustomHolidayManager(site) != null && Holidays.getCustomHolidayManager(site).isHoliday(dtToCal))) {
                LocalDate localDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());

                Set<Holiday> holidays = Holidays.getDefaultHolidayManager().getHolidays(localDate, localDate, "");
                if (Holidays.getSiteHolidayManager(site) != null) {
                    holidays.addAll(Holidays.getSiteHolidayManager(site).getHolidays(localDate, localDate, Holidays.getStateCode()));
                }

                if (Holidays.getCustomHolidayManager(site) != null) {
                    holidays.addAll(Holidays.getCustomHolidayManager(site).getHolidays(localDate, localDate, Holidays.getStateCode()));
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
                tsRight.setStyle("-fx-text-fill: red !important;");
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
        Label titleLabel = new Label(chartModel.getChartName());
        if (fontColor != null) {
            titleLabel.setTextFill(fontColor);
            titleLabel.setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(fontColor) + "!important;");
        }
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
            if (fontColor != null) {
                label.setTextFill(fontColor);
                label.setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(fontColor) + "!important;");
            }

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

    private HeatMapXY getHeatMapXY(Interval interval, Period inputSampleRate) {
        long y;
        String y_Format = "HH";
        String y2_Format = "EEEE";
        long x;
        String x_Format = "mm";
        AggregationPeriod aggregationPeriod;
        Long intervalMillis = interval.getEndMillis() - interval.getStartMillis();
        PeriodComparator periodComparator = new PeriodComparator();
        Period fifteenMinutes = Period.minutes(15);
        int greaterFifteenMinutes = periodComparator.compare(inputSampleRate, fifteenMinutes);

        //basic settings
        x = 24 * 4;
        x_Format = "HH:mm";
        aggregationPeriod = AggregationPeriod.NONE;
        y = Math.round((double) (intervalMillis / DAY_MILLIS));
        y_Format = "yyyy-MM-dd";

        if (greaterFifteenMinutes < 0) {
            aggregationPeriod = AggregationPeriod.QUARTER_HOURLY;

        } else {
            if (inputSampleRate.equals(fifteenMinutes)) {
                // first do nothing
            } else if (inputSampleRate.equals(Period.hours(1))) {
                x = 24;
            } else if (inputSampleRate.equals(Period.days(1))) {
                x = 31;
                x_Format = "dd";
                y = Math.round((double) (intervalMillis / MIN_MONTH_MILLIS));
                y_Format = "yyyy-MM";
                y2_Format = "";
            } else if (inputSampleRate.equals(Period.weeks(1))) {
                x = 1;
                x_Format = "";
                y = Math.round((double) (intervalMillis / WEEK_MILLIS));
                y_Format = "ww";
                y2_Format = "";
            } else if (inputSampleRate.equals(Period.months(1))) {
                x = 1;
                x_Format = "";
                y = Math.round((double) (intervalMillis / MIN_MONTH_MILLIS));
                y_Format = "yyyy-MM";
                y2_Format = "";
            }
        }

        y++;

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
