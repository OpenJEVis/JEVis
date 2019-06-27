package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
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

import static org.jevis.jeconfig.application.Chart.TimeFrame.CUSTOM_START_END;
import static org.jevis.jeconfig.application.Chart.TimeFrame.PREVIEW;

public class PickerCombo {

    private ComboBox<TimeFrame> presetDateBox = new ComboBox<>();
    private JFXDatePicker startDatePicker = new JFXDatePicker();
    private JFXDatePicker endDatePicker = new JFXDatePicker();
    private JFXTimePicker startTimePicker = new JFXTimePicker();
    private JFXTimePicker endTimePicker = new JFXTimePicker();

    private GraphDataModel graphDataModel;
    private List<ChartDataModel> chartDataModels;


    private DateHelper dateHelper;
    private LocalDate minDate;
    private LocalDate maxDate;

    public PickerCombo(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModels) {

        this.graphDataModel = graphDataModel;
        this.chartDataModels = chartDataModels;

        this.dateHelper = new DateHelper();

        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
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

        presetDateBox.setItems(FXCollections.observableArrayList(TimeFrame.values()));

        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> cellFactory = new Callback<javafx.scene.control.ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(javafx.scene.control.ListView<TimeFrame> param) {
                return new ListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame timeFrame, boolean empty) {
                        super.updateItem(timeFrame, empty);
                        setText(null);
                        setGraphic(null);

                        if (timeFrame != null) {
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
                                case THIS_WEEK:
                                    text = thisWeek;
                                    break;
                                case LAST_WEEK:
                                    text = lastWeek;
                                    break;
                                case LAST_30_DAYS:
                                    text = last30Days;
                                    break;
                                case THIS_MONTH:
                                    text = thisMonth;
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
                                case PREVIEW:
                                    text = preview;
                                    break;
                            }
                            setText(text);
                            if (timeFrame == CUSTOM_START_END || timeFrame == PREVIEW) {
                                setTextFill(Color.LIGHTGRAY);
                                setDisable(true);
                            }
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
        startTimePicker.setConverter(new

                LocalTimeStringConverter(FormatStyle.SHORT));

        endTimePicker.setPrefWidth(100d);
        endTimePicker.setMaxWidth(100d);
        endTimePicker.set24HourView(true);
        endTimePicker.setConverter(new

                LocalTimeStringConverter(FormatStyle.SHORT));

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            if (graphDataModel != null && !graphDataModel.getCharts().isEmpty()) {
                graphDataModel.getCharts().forEach(chartSettings -> {
                    for (ChartDataModel model : chartDataModels) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            presetDateBox.getSelectionModel().select(chartSettings.getAnalysisTimeFrame().getTimeFrame());

                            DateTime start = model.getSelectedStart();
                            DateTime end = model.getSelectedEnd();

                            setPicker(start, end);
                        }
                    }
                });

            }
        } else {
            if (graphDataModel.isglobalAnalysisTimeFrame()) {
                presetDateBox.getSelectionModel().select(graphDataModel.getGlobalAnalysisTimeFrame().getTimeFrame());

                DateTime start = graphDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = graphDataModel.getGlobalAnalysisTimeFrame().getEnd();
                setPicker(start, end);

            } else {
                graphDataModel.getCharts().forEach(chartSettings -> {
                    for (ChartDataModel model : graphDataModel.getSelectedData()) {
                        if (model.getSelectedcharts().contains(chartSettings.getId())) {
                            presetDateBox.getSelectionModel().select(chartSettings.getAnalysisTimeFrame().getTimeFrame());

                            DateTime start = model.getSelectedStart();
                            DateTime end = model.getSelectedEnd();

                            setPicker(start, end);
                        }
                    }
                });
            }
        }

    }

    public void addListener() {
        presetDateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (chartDataModels == null && graphDataModel != null) {
                    if (newValue != TimeFrame.CUSTOM && newValue != CUSTOM_START_END) {
                        graphDataModel.setAnalysisTimeFrameForAllModels(new AnalysisTimeFrame(newValue));
                    }
                } else if (graphDataModel != null && chartDataModels != null) {
                    if (newValue != TimeFrame.CUSTOM && newValue != CUSTOM_START_END) {
                        graphDataModel.setAnalysisTimeFrameForModels(chartDataModels, new DateHelper(), new AnalysisTimeFrame(newValue));
                    }
                }
            }
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (chartDataModels == null && graphDataModel != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getStart().getHourOfDay(), graphDataModel.getGlobalAnalysisTimeFrame().getStart().getMinuteOfHour(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getStart().getSecondOfMinute());
                    analysisTimeFrame.setStart(startDate);
                    analysisTimeFrame.setEnd(graphDataModel.getGlobalAnalysisTimeFrame().getEnd());

                    graphDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                } else if (graphDataModel != null && chartDataModels != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getStart().getHourOfDay(), graphDataModel.getGlobalAnalysisTimeFrame().getStart().getMinuteOfHour(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getStart().getSecondOfMinute());
                    analysisTimeFrame.setStart(startDate);
                    analysisTimeFrame.setEnd(graphDataModel.getGlobalAnalysisTimeFrame().getEnd());

                    graphDataModel.setAnalysisTimeFrameForModels(chartDataModels, new DateHelper(), analysisTimeFrame);

                }
            }
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (chartDataModels == null && graphDataModel != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getEnd().getHourOfDay(), graphDataModel.getGlobalAnalysisTimeFrame().getEnd().getMinuteOfHour(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getEnd().getSecondOfMinute());
                    analysisTimeFrame.setStart(graphDataModel.getGlobalAnalysisTimeFrame().getStart());
                    analysisTimeFrame.setEnd(endDate);

                    graphDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                } else if (graphDataModel != null && chartDataModels != null) {
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    DateTime endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getEnd().getHourOfDay(), graphDataModel.getGlobalAnalysisTimeFrame().getEnd().getMinuteOfHour(),
                            graphDataModel.getGlobalAnalysisTimeFrame().getEnd().getSecondOfMinute());
                    analysisTimeFrame.setStart(graphDataModel.getGlobalAnalysisTimeFrame().getStart());
                    analysisTimeFrame.setEnd(endDate);

                    graphDataModel.setAnalysisTimeFrameForModels(chartDataModels, new DateHelper(), analysisTimeFrame);

                }
            }
        });
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
        JEVisObject forCustomTime = graphDataModel.getCurrentAnalysis();
        if (forCustomTime != null) {
            WorkDays wd = new WorkDays(graphDataModel.getCurrentAnalysis());
            if (wd.getWorkdayStart() != null && wd.getWorkdayEnd() != null) {
                dateHelper.setStartTime(wd.getWorkdayStart());
                dateHelper.setEndTime(wd.getWorkdayEnd());
            }
        } else if (!graphDataModel.getObservableListAnalyses().isEmpty()) {
            WorkDays wd = new WorkDays(graphDataModel.getObservableListAnalyses().get(0));
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
                break;
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
