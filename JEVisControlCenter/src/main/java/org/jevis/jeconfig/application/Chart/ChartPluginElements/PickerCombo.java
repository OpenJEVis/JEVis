package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.api.JEVisAttribute;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.List;

public class PickerCombo {
    private SimpleBooleanProperty changed = new SimpleBooleanProperty(false);
    private Boolean programmaticallyChanged = Boolean.FALSE;

    private ComboBox<TimeFrame> presetDateBox = new ComboBox<>();
    private JFXDatePicker startDatePicker = new JFXDatePicker();
    private JFXDatePicker endDatePicker = new JFXDatePicker();
    private JFXTimePicker startTimePicker = new JFXTimePicker();
    private JFXTimePicker endTimePicker = new JFXTimePicker();

    private GraphDataModel graphDataModel;
    private List<ChartDataModel> chartDataModels;

    private Boolean[] programmaticallySetPresetDate = new Boolean[4];
    private final ChangeListener<LocalTime> startTimeChangeListener = new ChangeListener<LocalTime>() {
        @Override
        public void changed(ObservableValue<? extends LocalTime> observable, LocalTime oldValue, LocalTime newValue) {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = startDatePicker.valueProperty().getValue();
                LocalTime lt = startTimePicker.valueProperty().getValue();
                if (ld != null && lt != null) {
                    PickerCombo.this.setSelectedStart(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[1]) presetDateBox.getSelectionModel().select(TimeFrame.CUSTOM);
                programmaticallySetPresetDate[1] = false;
            }
        }
    };
    private DateHelper dateHelper;
    private LocalDate minDate;
    private LocalDate maxDate;
    private final ChangeListener<LocalTime> endTimeChangeListener = new ChangeListener<LocalTime>() {
        @Override
        public void changed(ObservableValue<? extends LocalTime> observable, LocalTime oldValue, LocalTime newValue) {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = endDatePicker.valueProperty().getValue();
                LocalTime lt = endTimePicker.valueProperty().getValue();
                if (ld != null && lt != null) {
                    PickerCombo.this.setSelectedEnd(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[3]) presetDateBox.getSelectionModel().select(TimeFrame.CUSTOM);
                programmaticallySetPresetDate[3] = false;
            }
        }
    };
    private ChangeListener<LocalDate> startDateChangeListener = new ChangeListener<LocalDate>() {
        @Override
        public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = startDatePicker.valueProperty().getValue();
                LocalTime lt = startTimePicker.valueProperty().getValue();
                if (ld != null && lt != null) {
                    PickerCombo.this.setSelectedStart(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[0]) presetDateBox.getSelectionModel().select(TimeFrame.CUSTOM);
                programmaticallySetPresetDate[0] = false;
            }
        }
    };
    private boolean activeDateListener;
    private ChangeListener<LocalDate> startDateUpdateListener = new ChangeListener<LocalDate>() {
        @Override
        public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
            graphDataModel.update();
        }
    };
    private ChangeListener<LocalDate> endDateUpdateListener = new ChangeListener<LocalDate>() {
        @Override
        public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
            graphDataModel.update();
        }
    };
    private ChangeListener<TimeFrame> timeFrameChangeListener = new ChangeListener<TimeFrame>() {
        @Override
        public void changed(ObservableValue<? extends TimeFrame> observable, TimeFrame oldValue, TimeFrame newValue) {

            startDatePicker.valueProperty().removeListener(startDateUpdateListener);
            endDatePicker.valueProperty().removeListener(endDateUpdateListener);
            graphDataModel.update();
            startDatePicker.valueProperty().addListener(startDateUpdateListener);
            endDatePicker.valueProperty().addListener(endDateUpdateListener);
        }
    };
    private ChangeListener<LocalDate> endDateChangeListener = new ChangeListener<LocalDate>() {
        @Override
        public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = endDatePicker.valueProperty().getValue();
                LocalTime lt = endTimePicker.valueProperty().getValue();
                if (ld != null && lt != null) {
                    PickerCombo.this.setSelectedEnd(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[2]) presetDateBox.getSelectionModel().select(TimeFrame.CUSTOM);
                programmaticallySetPresetDate[2] = false;
            }
        }
    };
    private Boolean isGlobalTimeFrame;
    private final ChangeListener<TimeFrame> standardPresetDateBoxListener = new ChangeListener<TimeFrame>() {
        @Override
        public void changed(ObservableValue<? extends TimeFrame> observable, TimeFrame oldValue, TimeFrame newValue) {
            if (newValue != oldValue) {
                PickerCombo.this.stopDateListener();
                boolean b = PickerCombo.this.hasActiveDateListener();
                if (b) PickerCombo.this.stopUpdateListener();
                PickerCombo.this.applySelectedDatePresetToDataModel(newValue);
                PickerCombo.this.startDateListener();
                if (b) PickerCombo.this.startUpdateListener();
            }
//            else if (newValue == TimeFrame.CUSTOM){
//                applySelectedDatePresetToDataModel(newValue);
//            }
        }
    };

    public PickerCombo(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModels, Boolean isGlobalTimeFrame) {
        this.graphDataModel = graphDataModel;
        this.chartDataModels = chartDataModels;
        this.isGlobalTimeFrame = isGlobalTimeFrame;
        this.dateHelper = new DateHelper();
        for (int i = 0; i < 4; i++) {
            programmaticallySetPresetDate[i] = false;
        }

        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
        final String today = I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
        final String yesterday = I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");
        final String thisYear = I18n.getInstance().getString("plugin.graph.changedate.buttonthisyear");
        final String lastYear = I18n.getInstance().getString("plugin.graph.changedate.buttonlastyear");
        final String customStartEnd = I18n.getInstance().getString("plugin.graph.changedate.buttoncustomstartend");

        presetDateBox.setItems(FXCollections.observableArrayList(TimeFrame.values()));
        presetDateBox.getItems().remove(TimeFrame.PREVIEW);

        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> cellFactory = new Callback<javafx.scene.control.ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(javafx.scene.control.ListView<TimeFrame> param) {
                return new ListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame timeFrame, boolean empty) {
                        super.updateItem(timeFrame, empty);
                        if (empty || timeFrame == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (timeFrame) {
                                case CUSTOM:
                                    text = custom;
                                    break;
                                case TODAY:
                                    text = today;
                                    break;
                                case YESTERDAY:
                                    text = yesterday;
                                    break;
                                case LAST_7_DAYS:
                                    text = last7Days;
                                    break;
                                case LAST_WEEK:
                                    text = lastWeek;
                                    break;
                                case LAST_30_DAYS:
                                    text = last30Days;
                                    break;
                                case LAST_MONTH:
                                    text = lastMonth;
                                    break;
                                case THIS_YEAR:
                                    text = thisYear;
                                    break;
                                case LAST_YEAR:
                                    text = lastYear;
                                    break;
                                case CUSTOM_START_END:
                                    text = customStartEnd;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        presetDateBox.setCellFactory(cellFactory);
        presetDateBox.setButtonCell(cellFactory.call(null));

        startDatePicker.setPrefWidth(120d);
        endDatePicker.setPrefWidth(120d);

        startTimePicker.setPrefWidth(100d);
        startTimePicker.setMaxWidth(100d);
        startTimePicker.setIs24HourView(true);
        startTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        endTimePicker.setPrefWidth(100d);
        endTimePicker.setMaxWidth(100d);
        endTimePicker.setIs24HourView(true);
        endTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        startStandardPresetDateBoxListener();

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            if (graphDataModel != null && !graphDataModel.getCharts().isEmpty()) {
                graphDataModel.getCharts().forEach(chartSettings -> {
                    for (ChartDataModel model : chartDataModels) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            presetDateBox.getSelectionModel().select(chartSettings.getAnalysisTimeFrame().getTimeFrame());

                            DateTime start = model.getSelectedStart();
                            startDatePicker.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));

                            startTimePicker.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));

                            DateTime end = model.getSelectedEnd();
                            endDatePicker.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));

                            endTimePicker.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute(), 999999999));
                        }
                    }
                });

            }
        } else {
            if (graphDataModel.isglobalAnalysisTimeFrame()) {
                presetDateBox.getSelectionModel().select(graphDataModel.getGlobalAnalysisTimeFrame().getTimeFrame());

            } else {
                graphDataModel.getCharts().forEach(chartSettings -> {
                    for (ChartDataModel model : graphDataModel.getSelectedData()) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            presetDateBox.getSelectionModel().select(chartSettings.getAnalysisTimeFrame().getTimeFrame());

                            DateTime start = model.getSelectedStart();
                            startDatePicker.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));

                            startTimePicker.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));

                            DateTime end = model.getSelectedEnd();
                            endDatePicker.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));

                            endTimePicker.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute(), 999999999));
                        }
                    }
                });
            }
        }

        startDateListener();
    }

    private void startStandardPresetDateBoxListener() {
        presetDateBox.getSelectionModel().selectedItemProperty().addListener(standardPresetDateBoxListener);
    }

    private void stopStandardPresetDateBoxListener() {
        presetDateBox.getSelectionModel().selectedItemProperty().removeListener(standardPresetDateBoxListener);
    }

    public void startDateListener() {
        startDatePicker.valueProperty().addListener(startDateChangeListener);
        endDatePicker.valueProperty().addListener(endDateChangeListener);
        startTimePicker.valueProperty().addListener(startTimeChangeListener);
        endTimePicker.valueProperty().addListener(endTimeChangeListener);
    }

    public void stopDateListener() {
        startDatePicker.valueProperty().removeListener(startDateChangeListener);
        endDatePicker.valueProperty().removeListener(endDateChangeListener);
        startTimePicker.valueProperty().removeListener(startTimeChangeListener);
        endTimePicker.valueProperty().removeListener(endTimeChangeListener);

    }

    public void startUpdateListener() {
        hasActiveDateListener(true);
        presetDateBox.getSelectionModel().selectedItemProperty().addListener(timeFrameChangeListener);
        startDatePicker.valueProperty().addListener(startDateUpdateListener);
        endDatePicker.valueProperty().addListener(endDateUpdateListener);
    }

    public void stopUpdateListener() {
        hasActiveDateListener(false);
        presetDateBox.getSelectionModel().selectedItemProperty().removeListener(timeFrameChangeListener);
        startDatePicker.valueProperty().removeListener(startDateUpdateListener);
        endDatePicker.valueProperty().removeListener(endDateUpdateListener);
    }

    private void hasActiveDateListener(boolean b) {
        activeDateListener = b;
    }

    private boolean hasActiveDateListener() {
        return activeDateListener;
    }

    private void updateMinMax() {
        minDate = null;
        maxDate = null;

        if (chartDataModels == null) {
            for (ChartDataModel mdl : graphDataModel.getSelectedData()) {
                if (!mdl.getSelectedcharts().isEmpty()) {
                    JEVisAttribute att = mdl.getAttribute();
                    setMinMax(att);
                }
            }
        } else {
            for (ChartDataModel model : chartDataModels) {
                JEVisAttribute att = model.getAttribute();
                setMinMax(att);
            }
        }
    }

    private void setMinMax(JEVisAttribute att) {
        DateTime timeStampFromFirstSample = att.getTimestampFromFirstSample();
        DateTime timeStampFromLastSample = att.getTimestampFromLastSample();

        LocalDate min_check = LocalDate.of(
                timeStampFromFirstSample.getYear(),
                timeStampFromFirstSample.getMonthOfYear(),
                timeStampFromFirstSample.getDayOfMonth());

        LocalDate max_check = LocalDate.of(
                timeStampFromLastSample.getYear(),
                timeStampFromLastSample.getMonthOfYear(),
                timeStampFromLastSample.getDayOfMonth());

        if (minDate == null || min_check.isBefore(minDate)) minDate = min_check;
        if (maxDate == null || max_check.isAfter(maxDate)) maxDate = max_check;
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


    public GraphDataModel getGraphDataModel() {
        return graphDataModel;
    }

    public void setGraphDataModel(GraphDataModel graphDataModel) {
        this.graphDataModel = graphDataModel;
    }

    public List<ChartDataModel> getChartDataModels() {
        return chartDataModels;
    }

    public void setChartDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
    }

    public boolean isChanged() {
        return changed.get();
    }

    public void setChanged(boolean changed) {
        this.changed.set(changed);
    }

    public SimpleBooleanProperty changedProperty() {
        return changed;
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

    public ComboBox<TimeFrame> getPresetDateBox() {
        return presetDateBox;
    }

    private void setSelectedStart(DateTime selectedStart) {
        if (chartDataModels == null || chartDataModels.isEmpty()) {
            graphDataModel.getSelectedData().forEach(dataModel -> {
                dataModel.setSelectedStart(selectedStart);
                dataModel.setSomethingChanged(true);

            });
        } else {
            chartDataModels.forEach(dataModel -> {
                dataModel.setSelectedStart(selectedStart);
                dataModel.setSomethingChanged(true);
            });
        }
    }

    private void setSelectedEnd(DateTime selectedEnd) {
        if (chartDataModels == null || chartDataModels.isEmpty()) {
            graphDataModel.getSelectedData().forEach(dataModel -> {
                dataModel.setSelectedEnd(selectedEnd);
                dataModel.setSomethingChanged(true);
            });
        } else {
            chartDataModels.forEach(model -> {
                model.setSelectedEnd(selectedEnd);
                model.setSomethingChanged(true);
            });
        }

    }

    private void applySelectedDatePresetToDataModel(TimeFrame newValue) {

        WorkDays wd = new WorkDays(graphDataModel.getCurrentAnalysis());
        if (wd.getWorkdayStart() != null) dateHelper.setStartTime(wd.getWorkdayStart());
        if (wd.getWorkdayEnd() != null) dateHelper.setEndTime(wd.getWorkdayEnd());

        graphDataModel.isGlobalAnalysisTimeFrame(isGlobalTimeFrame);

        if (newValue != TimeFrame.PREVIEW && newValue != TimeFrame.CUSTOM) {
            if (chartDataModels == null) {
                graphDataModel.setAnalysisTimeFrameForAllModels(new AnalysisTimeFrame(newValue));
            } else {
                dateHelper.setMinMaxForDateHelper(chartDataModels);
                graphDataModel.setAnalysisTimeFrameForModels(chartDataModels, dateHelper, new AnalysisTimeFrame(newValue));
            }
        }

        switch (newValue) {
            //Custom
            case CUSTOM:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = false;
                }
                DateTime start = null;
                DateTime end = null;
                if (chartDataModels == null) {
                    for (ChartDataModel model : graphDataModel.getSelectedData()) {

                        start = model.getSelectedStart();
                        end = model.getSelectedEnd();

                        if (start != null && end != null) break;
                    }
                } else {
                    for (ChartDataModel model : chartDataModels) {

                        start = model.getSelectedStart();
                        end = model.getSelectedEnd();

                        if (start != null && end != null) break;
                    }
                }

//                setPicker(start, end);

                break;
            //today
            case TODAY:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.TODAY);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //yesterday
            case YESTERDAY:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 7 days
            case LAST_7_DAYS:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last Week
            case LAST_WEEK:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 30 days
            case LAST_30_DAYS:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case LAST_MONTH:
                //last Month
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case THIS_YEAR:
                //this Year
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.THISYEAR);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case LAST_YEAR:
                //last Year
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case CUSTOM_START_END:
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                break;
            default:
                break;
        }
    }


    private void setPicker(DateTime start, DateTime end) {

        if (start != null && end != null) {
            startDatePicker.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
            endDatePicker.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
            startTimePicker.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));
            endTimePicker.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute()));
        }
    }
}
