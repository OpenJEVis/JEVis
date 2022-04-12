package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.List;

import static org.jevis.jeconfig.application.Chart.TimeFrame.CUSTOM_START_END;

public class PickerCombo {
    private static final Logger logger = LogManager.getLogger(PickerCombo.class);
    private JEVisDataSource ds;
    private final PresetDateBox presetDateBox = new PresetDateBox();
    private final JFXDatePicker startDatePicker = new JFXDatePicker();
    private final JFXDatePicker endDatePicker = new JFXDatePicker();
    private final JFXTimePicker startTimePicker = new JFXTimePicker();
    private final JFXTimePicker endTimePicker = new JFXTimePicker();

    private AnalysisDataModel analysisDataModel;
    private List<ChartDataRow> chartDataRows;


    private final DateHelper dateHelper;
    private LocalDate minDate;
    private LocalDate maxDate;
    private boolean isUpdating = false;

    public PickerCombo(AnalysisDataModel analysisDataModel, List<ChartDataRow> chartDataRows, boolean withCustom) {

        initialize(analysisDataModel, chartDataRows, withCustom);

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

    public void initialize(AnalysisDataModel analysisDataModel, List<ChartDataRow> chartDataRows, boolean withCustom) {
        isUpdating = true;
        this.analysisDataModel = analysisDataModel;
        JEVisObject obj = null;
        try {
            for (ChartDataRow chartDataRow : analysisDataModel.getSelectedData()) {
                this.ds = chartDataRow.getObject().getDataSource();
                obj = chartDataRow.getObject();
                break;
            }
        } catch (Exception e) {
            try {
                for (ChartDataRow chartDataRow : chartDataRows) {
                    this.ds = chartDataRow.getObject().getDataSource();
                    obj = chartDataRow.getObject();
                    break;
                }
            } catch (Exception e1) {
                logger.error("Could not find data source", e1);
            }
        }
        this.chartDataRows = chartDataRows;
        this.presetDateBox.isWithCustom(obj, withCustom);

        if (chartDataRows != null && !chartDataRows.isEmpty()) {
            if (analysisDataModel != null && !analysisDataModel.getCharts().getListSettings().isEmpty()) {
                analysisDataModel.getCharts().getListSettings().forEach(chartSettings -> {
                    for (ChartDataRow model : chartDataRows) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            presetDateBox.getItems().stream().filter(timeFrame -> timeFrame.getTimeFrame() == chartSettings.getAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == chartSettings.getAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.getSelectionModel().select(timeFrame));

                            DateTime start = model.getSelectedStart();
                            DateTime end = model.getSelectedEnd();

                            setPicker(start, end);
                        }
                    }
                });

            }
        } else {
            if (analysisDataModel.isGlobalAnalysisTimeFrame()) {
                presetDateBox.getItems().stream().filter(timeFrame -> timeFrame.getTimeFrame() == analysisDataModel.getGlobalAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == analysisDataModel.getGlobalAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.getSelectionModel().select(timeFrame));

                DateTime start = analysisDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = analysisDataModel.getGlobalAnalysisTimeFrame().getEnd();
                setPicker(start, end);

            } else {
                analysisDataModel.getCharts().getListSettings().forEach(chartSettings -> {
                    for (ChartDataRow model : analysisDataModel.getSelectedData()) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            presetDateBox.getItems().stream().filter(timeFrame -> timeFrame.getTimeFrame() == chartSettings.getAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == chartSettings.getAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.getSelectionModel().select(timeFrame));

                            DateTime start = model.getSelectedStart();
                            DateTime end = model.getSelectedEnd();

                            setPicker(start, end);
                        }
                    }
                });
            }
        }

        isUpdating = false;
    }


    public void addListener() {
        presetDateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && !isUpdating) {
                if (chartDataRows == null && analysisDataModel != null) {
                    if (newValue.getTimeFrame() != TimeFrame.CUSTOM) {
                        analysisDataModel.setAnalysisTimeFrameForAllModels(newValue);
                    }
                } else if (analysisDataModel != null && chartDataRows != null) {
                    if (newValue.getTimeFrame() != TimeFrame.CUSTOM) {
                        analysisDataModel.setAnalysisTimeFrameForModels(chartDataRows, new DateHelper(), newValue);
                    }
                }
            }
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && !isUpdating) {
                if (chartDataRows == null && analysisDataModel != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            startTimePicker.getValue().getHour(), startTimePicker.getValue().getMinute(), startTimePicker.getValue().getSecond());
                    analysisTimeFrame.setStart(startDate);
                    analysisTimeFrame.setEnd(analysisDataModel.getGlobalAnalysisTimeFrame().getEnd());

                    analysisDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                } else if (analysisDataModel != null && chartDataRows != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            startTimePicker.getValue().getHour(), startTimePicker.getValue().getMinute(), startTimePicker.getValue().getSecond());
                    analysisTimeFrame.setStart(startDate);
                    analysisTimeFrame.setEnd(analysisDataModel.getGlobalAnalysisTimeFrame().getEnd());

                    analysisDataModel.setAnalysisTimeFrameForModels(chartDataRows, new DateHelper(), analysisTimeFrame);

                }
            }
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && !isUpdating) {
                if (chartDataRows == null && analysisDataModel != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            endTimePicker.getValue().getHour(), endTimePicker.getValue().getMinute(), endTimePicker.getValue().getSecond());
                    analysisTimeFrame.setStart(analysisDataModel.getGlobalAnalysisTimeFrame().getStart());
                    analysisTimeFrame.setEnd(endDate);

                    analysisDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                } else if (analysisDataModel != null && chartDataRows != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            endTimePicker.getValue().getHour(), endTimePicker.getValue().getMinute(), endTimePicker.getValue().getSecond());
                    analysisTimeFrame.setStart(analysisDataModel.getGlobalAnalysisTimeFrame().getStart());
                    analysisTimeFrame.setEnd(endDate);

                    analysisDataModel.setAnalysisTimeFrameForModels(chartDataRows, new DateHelper(), analysisTimeFrame);

                }
            }
        });
    }


    private void updateMinMax() {
        minDate = null;
        maxDate = null;

        if (chartDataRows == null) {
            for (ChartDataRow mdl : analysisDataModel.getSelectedData()) {
                if (!mdl.getSelectedcharts().isEmpty()) {
                    JEVisAttribute att = mdl.getAttribute();
                    setMinMax(att);
                    if (mdl.hasForecastData()) {
                        JEVisAttribute forecastDataAttribute = mdl.getForecastDataAttribute();
                        setMinMax(forecastDataAttribute);
                    }
                }
            }
        } else {
            for (ChartDataRow model : chartDataRows) {
                JEVisAttribute att = model.getAttribute();
                setMinMax(att);
                if (model.hasForecastData()) {
                    JEVisAttribute forecastDataAttribute = model.getForecastDataAttribute();
                    setMinMax(forecastDataAttribute);
                }
            }
        }
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

        startDatePicker.setDayCellFactory(dateCellCallback);
        endDatePicker.setDayCellFactory(dateCellCallback);
    }


    public AnalysisDataModel getAnalysisDataModel() {
        return analysisDataModel;
    }

    public void setAnalysisDataModel(AnalysisDataModel analysisDataModel) {
        this.analysisDataModel = analysisDataModel;
    }

    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    public void setChartDataRows(List<ChartDataRow> chartDataRows) {
        this.chartDataRows = chartDataRows;
    }

    public JFXDatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public JFXDatePicker getEndDatePicker() {
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

    private void setSelectedStart(DateTime selectedStart) {
        if (chartDataRows == null || chartDataRows.isEmpty()) {
            analysisDataModel.getSelectedData().forEach(dataModel -> {
                dataModel.setSelectedStart(selectedStart);
                dataModel.setSomethingChanged(true);

            });
        } else {
            chartDataRows.forEach(dataModel -> {
                dataModel.setSelectedStart(selectedStart);
                dataModel.setSomethingChanged(true);
            });
        }
    }

    private void setSelectedEnd(DateTime selectedEnd) {
        if (chartDataRows == null || chartDataRows.isEmpty()) {
            analysisDataModel.getSelectedData().forEach(dataModel -> {
                dataModel.setSelectedEnd(selectedEnd);
                dataModel.setSomethingChanged(true);
            });
        } else {
            chartDataRows.forEach(model -> {
                model.setSelectedEnd(selectedEnd);
                model.setSomethingChanged(true);
            });
        }

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
