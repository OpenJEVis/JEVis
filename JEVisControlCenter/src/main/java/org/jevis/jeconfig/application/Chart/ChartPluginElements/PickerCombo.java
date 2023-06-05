package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXTimePicker;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.DataModel;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;

import static org.jevis.jeconfig.application.Chart.TimeFrame.CUSTOM_START_END;

public class PickerCombo {
    private static final Logger logger = LogManager.getLogger(PickerCombo.class);
    private final JEVisDataSource ds;
    private final PresetDateBox presetDateBox;
    private final MFXDatePicker startDatePicker = new MFXDatePicker();
    private final MFXDatePicker endDatePicker = new MFXDatePicker();
    private final JFXTimePicker startTimePicker = new JFXTimePicker();
    private final JFXTimePicker endTimePicker = new JFXTimePicker();
    private final DataSettings dataSettings;
    private final ChartPlugin chartPlugin;

    private final DataModel dataModel;

    private final DateHelper dateHelper;
    private LocalDate minDate;
    private LocalDate maxDate;
    private boolean isUpdating = false;

    public PickerCombo(JEVisDataSource ds, ChartPlugin chartPlugin, boolean withCustom) {
        this.ds = ds;
        this.dataModel = chartPlugin.getDataModel();
        this.dataSettings = chartPlugin.getDataSettings();
        this.chartPlugin = chartPlugin;
        this.presetDateBox = new PresetDateBox(ds, chartPlugin);

        initialize(withCustom);

        this.dateHelper = new DateHelper();

        startDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.startdate")));
        endDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.enddate")));

        startDatePicker.setPrefWidth(120d);
        endDatePicker.setPrefWidth(120d);

        startTimePicker.setPrefWidth(100d);
        startTimePicker.setMaxWidth(100d);
        startTimePicker.set24HourView(true);
        startTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        endTimePicker.setPrefWidth(100d);
        endTimePicker.setMaxWidth(100d);
        endTimePicker.set24HourView(true);
        endTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
    }

    public void initialize(boolean withCustom) {
        isUpdating = true;

        this.presetDateBox.isWithCustom(ds, withCustom);

        if (dataModel != null && !dataModel.getChartModels().isEmpty()) {

            presetDateBox.getItems().stream().filter(timeFrame -> timeFrame.getTimeFrame() == dataSettings.getAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == dataSettings.getAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.selectItem(timeFrame));

            DateTime start = dataSettings.getAnalysisTimeFrame().getStart();
            DateTime end = dataSettings.getAnalysisTimeFrame().getEnd();

            setPicker(start, end);
        }

        isUpdating = false;
    }


    public void addListener() {
        presetDateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && !isUpdating) {
                if (newValue.getTimeFrame() != TimeFrame.CUSTOM) {
                    dataSettings.setAnalysisTimeFrame(newValue);
                    chartPlugin.update();
                }
            }
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && !isUpdating) {
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM);
                DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                        startTimePicker.getValue().getHour(), startTimePicker.getValue().getMinute(), startTimePicker.getValue().getSecond());
                analysisTimeFrame.setStart(startDate);
                analysisTimeFrame.setEnd(dataSettings.getAnalysisTimeFrame().getEnd());

                dataSettings.setAnalysisTimeFrame(analysisTimeFrame);
                chartPlugin.update();
            }
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && !isUpdating) {
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM);
                DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                        endTimePicker.getValue().getHour(), endTimePicker.getValue().getMinute(), endTimePicker.getValue().getSecond());
                analysisTimeFrame.setStart(dataSettings.getAnalysisTimeFrame().getStart());
                analysisTimeFrame.setEnd(endDate);

                dataSettings.setAnalysisTimeFrame(analysisTimeFrame);
                chartPlugin.update();
            }
        });
    }


    private void updateMinMax() {
        minDate = null;
        maxDate = null;

        dataModel.getChartModels().forEach(chart -> chart.getChartData().forEach(chartData -> {
            ChartDataRow chartDataRow = new ChartDataRow(ds, chartData);
            JEVisAttribute att = chartDataRow.getAttribute();
            setMinMax(att);
            if (chartDataRow.hasForecastData()) {
                JEVisAttribute forecastDataAttribute = chartDataRow.getForecastDataAttribute();
                setMinMax(forecastDataAttribute);
            }
        }));
    }

    private void setMinMax(JEVisAttribute att) {
        DateTime timeStampFromFirstSample = att.getTimestampFromFirstSample();
        DateTime timeStampFromLastSample = att.getTimestampFromLastSample();

        LocalDate min_check = null;
        if (timeStampFromFirstSample != null) {
            min_check = LocalDate.of(
                    timeStampFromFirstSample.getYear(),
                    timeStampFromFirstSample.getMonthOfYear(),
                    timeStampFromFirstSample.getDayOfMonth());
        }

        LocalDate max_check = null;
        if (timeStampFromLastSample != null) {
            max_check = LocalDate.of(
                    timeStampFromLastSample.getYear(),
                    timeStampFromLastSample.getMonthOfYear(),
                    timeStampFromLastSample.getDayOfMonth());
        }


        if (min_check != null && (minDate == null || min_check.isBefore(minDate))) {
            minDate = min_check;
        }

        if (max_check != null && (maxDate == null || max_check.isAfter(maxDate))) {
            maxDate = max_check;
        }

    }

    public void updateCellFactory() {
        Callback<DatePicker, DateCell> dateCellCallback = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        updateMinMax();

                        if (minDate != null && item.isBefore(minDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (maxDate != null && item.isAfter(maxDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };

        //TODO JFX17
        // startDatePicker.setDayCellFactory(dateCellCallback);
        // endDatePicker.setDayCellFactory(dateCellCallback);
    }


    public DataModel getDataModel() {
        return dataModel;
    }

    public MFXDatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public MFXDatePicker getEndDatePicker() {
        return endDatePicker;
    }

    public JFXTimePicker getStartTimePicker() {
        return startTimePicker;
    }

    public JFXTimePicker getEndTimePicker() {
        return endTimePicker;
    }

    public PresetDateBox getPresetDateBox() {
        return presetDateBox;
    }

    public void setPicker(DateTime start, DateTime end) {

        if (start != null && end != null) {
            startDatePicker.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
            endDatePicker.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
            startTimePicker.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));
            endTimePicker.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute()));
        }
    }
}
