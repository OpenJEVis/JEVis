package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.jevis.jeconfig.application.Chart.TimeFrame.CUSTOM_START_END;

public class PickerCombo {
    private static final Logger logger = LogManager.getLogger(PickerCombo.class);
    private JEVisDataSource ds;
    private ComboBox<AnalysisTimeFrame> presetDateBox = new ComboBox<>();
    private JFXDatePicker startDatePicker = new JFXDatePicker();
    private JFXDatePicker endDatePicker = new JFXDatePicker();
    private JFXTimePicker startTimePicker = new JFXTimePicker();
    private JFXTimePicker endTimePicker = new JFXTimePicker();

    private AnalysisDataModel analysisDataModel;
    private List<ChartDataModel> chartDataModels;


    private DateHelper dateHelper;
    private LocalDate minDate;
    private LocalDate maxDate;

    public PickerCombo(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, boolean withCustom) {

        this.analysisDataModel = analysisDataModel;
        try {
            for (ChartDataModel chartDataModel : analysisDataModel.getSelectedData()) {
                this.ds = chartDataModel.getObject().getDataSource();
                break;
            }
        } catch (Exception e) {
            try {
                for (ChartDataModel chartDataModel : chartDataModels) {
                    this.ds = chartDataModel.getObject().getDataSource();
                    break;
                }
            } catch (Exception e1) {
                logger.error("Could not find data source", e1);
            }
        }
        this.chartDataModels = chartDataModels;

        this.dateHelper = new DateHelper();

        this.presetDateBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) this.presetDateBox.getSkin();
                if (skin != null) {
                    ListView<?> popupContent = (ListView<?>) skin.getPopupContent();
                    if (popupContent != null) {
                        popupContent.scrollTo(this.presetDateBox.getSelectionModel().getSelectedIndex());
                    }
                }
            });
        });

        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
        final String current = I18n.getInstance().getString("plugin.graph.changedate.buttoncurrent");
        final String today = I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
        final String yesterday = I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String thisWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonthisweek");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String thisMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonthismonth");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");
        final String thisYear = I18n.getInstance().getString("plugin.graph.changedate.buttonthisyear");
        final String lastYear = I18n.getInstance().getString("plugin.graph.changedate.buttonlastyear");
        final String customStartEnd = I18n.getInstance().getString("plugin.graph.changedate.buttoncustomstartend");
        final String preview = I18n.getInstance().getString("plugin.graph.changedate.preview");

        List<AnalysisTimeFrame> analysisTimeFrameList = new ArrayList<>();
        for (TimeFrame timeFrame : TimeFrame.values()) {
            AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(timeFrame);

            if (!withCustom || timeFrame != CUSTOM_START_END) {
                analysisTimeFrameList.add(analysisTimeFrame);
            }
        }

        if (withCustom) {
            analysisTimeFrameList.addAll(getCustomPeriods());
        }

        presetDateBox.setItems(FXCollections.observableArrayList(analysisTimeFrameList));

        Callback<ListView<AnalysisTimeFrame>, ListCell<AnalysisTimeFrame>> cellFactory = new Callback<javafx.scene.control.ListView<AnalysisTimeFrame>, ListCell<AnalysisTimeFrame>>() {
            @Override
            public ListCell<AnalysisTimeFrame> call(javafx.scene.control.ListView<AnalysisTimeFrame> param) {
                return new ListCell<AnalysisTimeFrame>() {
                    @Override
                    protected void updateItem(AnalysisTimeFrame analysisTimeFrame, boolean empty) {
                        super.updateItem(analysisTimeFrame, empty);
                        setText(null);
                        setGraphic(null);

                        if (analysisTimeFrame != null && !empty) {
                            String text = "";
                            switch (analysisTimeFrame.getTimeFrame()) {
                                case CUSTOM:
                                    text = custom;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case CURRENT:
                                    text = current;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case TODAY:
                                    text = today;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case YESTERDAY:
                                    text = yesterday;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case LAST_7_DAYS:
                                    text = last7Days;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case THIS_WEEK:
                                    text = thisWeek;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case LAST_WEEK:
                                    text = lastWeek;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case LAST_30_DAYS:
                                    text = last30Days;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case THIS_MONTH:
                                    text = thisMonth;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case LAST_MONTH:
                                    text = lastMonth;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case THIS_YEAR:
                                    text = thisYear;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case LAST_YEAR:
                                    text = lastYear;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case CUSTOM_START_END:
                                    if (!withCustom) {
                                        text = customStartEnd;
                                        setTextFill(Color.LIGHTGRAY);
                                        setDisable(true);
                                    } else {
                                        text = analysisTimeFrame.getName();
                                        setTextFill(Color.BLACK);
                                        setDisable(false);
                                    }
                                    break;
                                case PREVIEW:
                                    text = preview;
                                    setTextFill(Color.LIGHTGRAY);
                                    setDisable(true);
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
        startTimePicker.set24HourView(true);
        startTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        endTimePicker.setPrefWidth(100d);
        endTimePicker.setMaxWidth(100d);
        endTimePicker.set24HourView(true);
        endTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            if (analysisDataModel != null && !analysisDataModel.getCharts().getListSettings().isEmpty()) {
                analysisDataModel.getCharts().getListSettings().forEach(chartSettings -> {
                    for (ChartDataModel model : chartDataModels) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            analysisTimeFrameList.stream().filter(timeFrame -> timeFrame.getTimeFrame() == chartSettings.getAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == chartSettings.getAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.getSelectionModel().select(timeFrame));

                            DateTime start = model.getSelectedStart();
                            DateTime end = model.getSelectedEnd();

                            setPicker(start, end);
                        }
                    }
                });

            }
        } else {
            if (analysisDataModel.isglobalAnalysisTimeFrame()) {
                analysisTimeFrameList.stream().filter(timeFrame -> timeFrame.getTimeFrame() == analysisDataModel.getGlobalAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == analysisDataModel.getGlobalAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.getSelectionModel().select(timeFrame));

                DateTime start = analysisDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = analysisDataModel.getGlobalAnalysisTimeFrame().getEnd();
                setPicker(start, end);

            } else {
                analysisDataModel.getCharts().getListSettings().forEach(chartSettings -> {
                    for (ChartDataModel model : analysisDataModel.getSelectedData()) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            analysisTimeFrameList.stream().filter(timeFrame -> timeFrame.getTimeFrame() == chartSettings.getAnalysisTimeFrame().getTimeFrame()).filter(timeFrame -> timeFrame.getTimeFrame() != CUSTOM_START_END || timeFrame.getId() == chartSettings.getAnalysisTimeFrame().getId()).findFirst().ifPresent(timeFrame -> presetDateBox.getSelectionModel().select(timeFrame));

                            DateTime start = model.getSelectedStart();
                            DateTime end = model.getSelectedEnd();

                            setPicker(start, end);
                        }
                    }
                });
            }
        }

    }

    private Collection<? extends AnalysisTimeFrame> getCustomPeriods() {

        List<JEVisObject> listCalendarDirectories = new ArrayList<>();
        List<JEVisObject> listCustomPeriods = new ArrayList<>();
        List<CustomPeriodObject> listCustomPeriodObjects = new ArrayList<>();
        List<AnalysisTimeFrame> customPeriods = new ArrayList<>();
        DateHelper dateHelper = new DateHelper();

        if (analysisDataModel != null) {
            dateHelper.setStartTime(analysisDataModel.getWorkdayStart());
            dateHelper.setEndTime(analysisDataModel.getWorkdayEnd());
        }

        try {
            try {
                JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                listCalendarDirectories = ds.getObjects(calendarDirectoryClass, false);
            } catch (JEVisException e) {
                logger.error("Error: could not get calendar directories", e);
            }
            if (Objects.requireNonNull(listCalendarDirectories).isEmpty()) {
                List<JEVisObject> listBuildings = new ArrayList<>();
                try {
                    JEVisClass building = ds.getJEVisClass("Building");
                    listBuildings = ds.getObjects(building, false);

                    if (!listBuildings.isEmpty()) {
                        JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                        if (ds.getCurrentUser().canCreate(listBuildings.get(0).getID())) {

                            JEVisObject calendarDirectory = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.calendardir.defaultname"), calendarDirectoryClass);
                            calendarDirectory.commit();
                        }
                    }
                } catch (JEVisException e) {
                    logger.error("Error: could not create new calendar directory", e);
                }

            }
            try {
                listCustomPeriods = ds.getObjects(ds.getJEVisClass("Custom Period"), false);
            } catch (JEVisException e) {
                logger.error("Error: could not get custom period", e);
            }
        } catch (Exception e) {
        }

        for (JEVisObject obj : listCustomPeriods) {
            if (obj != null) {
                CustomPeriodObject cpo = new CustomPeriodObject(obj, new ObjectHandler(ds));
                if (cpo.isVisible()) {
                    listCustomPeriodObjects.add(cpo);
                }
            }
        }

        if (listCustomPeriodObjects.size() > 1) {

            for (CustomPeriodObject cpo : listCustomPeriodObjects) {

                dateHelper.setCustomPeriodObject(cpo);
                dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                dateHelper.setStartTime(analysisDataModel.getWorkdayStart());
                dateHelper.setEndTime(analysisDataModel.getWorkdayEnd());

                AnalysisTimeFrame newTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM_START_END, cpo.getObject().getID(), cpo.getObject().getName());
                newTimeFrame.setStart(dateHelper.getStartDate());
                newTimeFrame.setEnd(dateHelper.getEndDate());

                customPeriods.add(newTimeFrame);
            }
        }

        return customPeriods;
    }

    public void addListener() {
        presetDateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (chartDataModels == null && analysisDataModel != null) {
                    if (newValue.getTimeFrame() != TimeFrame.CUSTOM) {
                        analysisDataModel.setAnalysisTimeFrameForAllModels(newValue);
                    }
                } else if (analysisDataModel != null && chartDataModels != null) {
                    if (newValue.getTimeFrame() != TimeFrame.CUSTOM) {
                        analysisDataModel.setAnalysisTimeFrameForModels(chartDataModels, new DateHelper(), newValue);
                    }
                }
            }
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (chartDataModels == null && analysisDataModel != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getStart().getHourOfDay(), analysisDataModel.getGlobalAnalysisTimeFrame().getStart().getMinuteOfHour(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getStart().getSecondOfMinute());
                    analysisTimeFrame.setStart(startDate);
                    analysisTimeFrame.setEnd(analysisDataModel.getGlobalAnalysisTimeFrame().getEnd());

                    analysisDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                } else if (analysisDataModel != null && chartDataModels != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getStart().getHourOfDay(), analysisDataModel.getGlobalAnalysisTimeFrame().getStart().getMinuteOfHour(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getStart().getSecondOfMinute());
                    analysisTimeFrame.setStart(startDate);
                    analysisTimeFrame.setEnd(analysisDataModel.getGlobalAnalysisTimeFrame().getEnd());

                    analysisDataModel.setAnalysisTimeFrameForModels(chartDataModels, new DateHelper(), analysisTimeFrame);

                }
            }
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (chartDataModels == null && analysisDataModel != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getEnd().getHourOfDay(), analysisDataModel.getGlobalAnalysisTimeFrame().getEnd().getMinuteOfHour(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getEnd().getSecondOfMinute());
                    analysisTimeFrame.setStart(analysisDataModel.getGlobalAnalysisTimeFrame().getStart());
                    analysisTimeFrame.setEnd(endDate);

                    analysisDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                } else if (analysisDataModel != null && chartDataModels != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getEnd().getHourOfDay(), analysisDataModel.getGlobalAnalysisTimeFrame().getEnd().getMinuteOfHour(),
                            analysisDataModel.getGlobalAnalysisTimeFrame().getEnd().getSecondOfMinute());
                    analysisTimeFrame.setStart(analysisDataModel.getGlobalAnalysisTimeFrame().getStart());
                    analysisTimeFrame.setEnd(endDate);

                    analysisDataModel.setAnalysisTimeFrameForModels(chartDataModels, new DateHelper(), analysisTimeFrame);

                }
            }
        });
    }


    private void updateMinMax() {
        minDate = null;
        maxDate = null;

        if (chartDataModels == null) {
            for (ChartDataModel mdl : analysisDataModel.getSelectedData()) {
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
            for (ChartDataModel model : chartDataModels) {
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

    public List<ChartDataModel> getChartDataModels() {
        return chartDataModels;
    }

    public void setChartDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
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

    public ComboBox<AnalysisTimeFrame> getPresetDateBox() {
        return presetDateBox;
    }

    private void setSelectedStart(DateTime selectedStart) {
        if (chartDataModels == null || chartDataModels.isEmpty()) {
            analysisDataModel.getSelectedData().forEach(dataModel -> {
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
            analysisDataModel.getSelectedData().forEach(dataModel -> {
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
        JEVisObject forCustomTime = analysisDataModel.getCurrentAnalysis();
        if (forCustomTime != null) {
            WorkDays wd = new WorkDays(analysisDataModel.getCurrentAnalysis());
            wd.setEnabled(analysisDataModel.isCustomWorkDay());
            if (wd.getWorkdayStart() != null && wd.getWorkdayEnd() != null) {
                dateHelper.setStartTime(wd.getWorkdayStart());
                dateHelper.setEndTime(wd.getWorkdayEnd());
            }
        } else if (!analysisDataModel.getObservableListAnalyses().isEmpty()) {
            WorkDays wd = new WorkDays(analysisDataModel.getObservableListAnalyses().get(0));
            wd.setEnabled(analysisDataModel.isCustomWorkDay());
            if (wd.getWorkdayStart() != null && wd.getWorkdayEnd() != null) {
                dateHelper.setStartTime(wd.getWorkdayStart());
                dateHelper.setEndTime(wd.getWorkdayEnd());
            }
        }


//        if (newValue != TimeFrame.PREVIEW) {
//            if (chartDataModels == null) {
//                graphDataModel.isGlobalAnalysisTimeFrame(true);
//                graphDataModel.setAnalysisTimeFrameForAllModels(new AnalysisTimeFrame(newValue));
//            } else {
//                dateHelper.setMinMaxForDateHelper(chartDataModels);
//                graphDataModel.setAnalysisTimeFrameForModels(chartDataModels, dateHelper, new AnalysisTimeFrame(newValue));
//            }
//        }

        switch (newValue) {
            //Custom
            case CUSTOM:
                break;
            //current
            case CURRENT:
                dateHelper.setType(DateHelper.TransformType.CURRENT);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //today
            case TODAY:
                dateHelper.setType(DateHelper.TransformType.TODAY);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //yesterday
            case YESTERDAY:
                dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 7 days
            case LAST_7_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //this Week
            case THIS_WEEK:
                dateHelper.setType(DateHelper.TransformType.THISWEEK);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last Week
            case LAST_WEEK:
                dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 30 days
            case LAST_30_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case THIS_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.THISMONTH);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case LAST_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case THIS_YEAR:
                //this Year
                dateHelper.setType(DateHelper.TransformType.THISYEAR);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case LAST_YEAR:
                //last Year
                dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case CUSTOM_START_END:
            default:
                break;
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
