package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.EndDatePicker;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.EndTimePicker;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.StartDatePicker;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.StartTimePicker;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PresetDateBox extends ComboBox<TimeFrame> {
    private List<ChartDataModel> chartDataModel;
    private StartDatePicker pickerDateStart;
    private StartTimePicker pickerTimeStart;
    private EndDatePicker pickerDateEnd;
    private EndTimePicker pickerTimeEnd;
    private DateHelper dateHelper;
    private GraphDataModel graphDataModel;
    private Boolean[] programmaticallySetPresetDate;


    public PresetDateBox() {
        super();
    }

    public void initialize(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModel, DateHelper dateHelper, StartDatePicker pickerDateStart,
                           StartTimePicker pickerTimeStart, EndDatePicker pickerDateEnd, EndTimePicker pickerTimeEnd, Boolean[] programmaticallySetPresetDate) {
        this.chartDataModel = chartDataModel;
        this.pickerDateStart = pickerDateStart;
        this.pickerTimeStart = pickerTimeStart;
        this.pickerDateEnd = pickerDateEnd;
        this.pickerTimeEnd = pickerTimeEnd;
        this.graphDataModel = graphDataModel;
        this.dateHelper = dateHelper;
        this.programmaticallySetPresetDate = programmaticallySetPresetDate;

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


        setItems(FXCollections.observableArrayList(TimeFrame.values()));
        getItems().remove(TimeFrame.PREVIEW);

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
        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        getSelectionModel().selectFirst();

        getSelectionModel().select(graphDataModel.getAnalysisTimeFrame().getTimeFrame());
        applySelectedDatePresetToDataModel(getSelectionModel().getSelectedItem());

        valueProperty().addListener((observable, oldValue, newValue) -> {
            applySelectedDatePresetToDataModel(newValue);
        });
    }

    private void applySelectedDatePresetToDataModel(TimeFrame newValue) {
        switch (newValue) {
            //Custom
            case CUSTOM:

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = false;
                }

                DateTime start = null;
                DateTime end = null;
                for (ChartDataModel model : graphDataModel.getSelectedData()) {

                    if (chartDataModel == null) {
                        start = model.getSelectedStart();
                        end = model.getSelectedEnd();

                        if (start != null && end != null) break;
                    }
                }

                setPicker(start, end);
                break;
            //today
            case TODAY:
                dateHelper.setType(DateHelper.TransformType.TODAY);
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //yesterday
            case YESTERDAY:
                dateHelper.setType(DateHelper.TransformType.YESTERDAY);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 7 days
            case LAST_7_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST7DAYS);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last Week
            case LAST_WEEK:
                dateHelper.setType(DateHelper.TransformType.LASTWEEK);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 30 days
            case LAST_30_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST30DAYS);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case LAST_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.LASTMONTH);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case THIS_YEAR:
                //this Year
                dateHelper.setType(DateHelper.TransformType.THISYEAR);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case LAST_YEAR:
                //last Year
                dateHelper.setType(DateHelper.TransformType.LASTYEAR);

                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
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
            pickerDateStart.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
            pickerDateEnd.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
            pickerTimeStart.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));
            pickerTimeEnd.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute()));
        }
    }
}
