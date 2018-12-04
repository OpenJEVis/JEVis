package org.jevis.jeconfig.plugin.graph;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import jfxtras.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.Chart.AnalysisTimeFrame;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.CustomPeriodObject;
import org.jevis.application.Chart.data.DateHelper;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.plugin.graph.view.ToolBarView;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class LoadAnalysisDialog extends Dialog<ButtonType> {
    private static final Logger logger = LogManager.getLogger(LoadAnalysisDialog.class);
    private final ToolBarView toolBarView;
    private GraphDataModel graphDataModel;
    private JFXDatePicker pickerDateStart = new JFXDatePicker();
    private JFXTimePicker pickerTimeStart = new JFXTimePicker();
    private JFXDatePicker pickerDateEnd = new JFXDatePicker();
    private JFXTimePicker pickerTimeEnd = new JFXTimePicker();
    private jfxtras.scene.control.ListView<JEVisObject> analysisListView = new ListView<>();
    private JEVisDataSource ds;
    private DateHelper dateHelper = new DateHelper();
    private ComboBox<String> comboBoxPresetDates;
    private Boolean[] programmaticallySetPresetDate = new Boolean[4];
    private ComboBox<String> aggregationBox;
    private ComboBox<String> mathBox;

    public LoadAnalysisDialog(JEVisDataSource ds, GraphDataModel data, ToolBarView toolBarView) {
        this.graphDataModel = data;
        this.toolBarView = toolBarView;
        this.ds = ds;
        for (int i = 0; i < 4; i++) {
            programmaticallySetPresetDate[i] = false;
        }

        initialize();
    }

    private void initialize() {

        //graphDataModel.updateListAnalyses();

        analysisListView.setItems(graphDataModel.getObservableListAnalyses());

        analysisListView.setCellFactory(param -> new ListCell<JEVisObject>() {
            @Override
            protected void updateItem(JEVisObject obj, boolean empty) {
                super.updateItem(obj, empty);
                if (empty || obj == null || obj.getName() == null) {
                    setText("");
                } else {
                    if (!graphDataModel.getMultipleDirectories())
                        setText(obj.getName());
                    else {
                        try {
                            int indexOfObj = graphDataModel.getObservableListAnalyses().indexOf(obj);
                            String prefix = graphDataModel.getListBuildingsParentOrganisations().get(indexOfObj).getName()
                                    + " / "
                                    + graphDataModel.getListAnalysesParentBuildings().get(indexOfObj).getName();
                            setText(prefix + " / " + obj.getName());
                        } catch (Exception e) {
                        }
                    }
                }

            }
        });

        if (!analysisListView.getItems().isEmpty()) graphDataModel.updateWorkDaysFirstRun();

        //checkForCustomizedWorkdayTimeFrame();

        if (graphDataModel.getCurrentAnalysis() != null && graphDataModel.getCurrentAnalysis().getName() != null
                && !graphDataModel.getCurrentAnalysis().getName().equals(""))
            analysisListView.getSelectionModel().select(graphDataModel.getCurrentAnalysis());

        final Callback<DatePicker, DateCell> dayCellFactory = getAllowedTimeFrameForDataRows();

//        Label individualText = new Label(I18n.getInstance().getString("plugin.graph.changedate.individual"));
        Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
        pickerDateStart.setPrefWidth(120d);
        pickerDateStart.setDayCellFactory(dayCellFactory);
        pickerTimeStart.setPrefWidth(100d);
        pickerTimeStart.setMaxWidth(100d);
        pickerTimeStart.setIs24HourView(true);
        pickerTimeStart.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

        Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        pickerDateEnd.setPrefWidth(120d);
        pickerDateEnd.setDayCellFactory(dayCellFactory);
        pickerTimeEnd.setPrefWidth(100d);
        pickerTimeEnd.setMaxWidth(100d);
        pickerTimeEnd.setIs24HourView(true);
        pickerTimeEnd.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

        ObservableList<String> presetDateEntries = FXCollections.observableArrayList();
        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
        final String today = I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
        final String yesterday = I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");
        final String customStartEnd = I18n.getInstance().getString("plugin.graph.changedate.buttoncustomstartend");

        presetDateEntries.addAll(custom, today, yesterday, last7Days, lastWeek, last30Days, lastMonth, customStartEnd);
        comboBoxPresetDates = new ComboBox(presetDateEntries);

        ComboBox<String> comboBoxCustomPeriods = getCustomPeriodsComboBox();

        Label standardSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.standard"));
        Label customSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.custom"));
        Label labelAggregation = new Label(I18n.getInstance().getString("plugin.graph.interval.label"));
        Label labelMath = new Label(I18n.getInstance().getString("plugin.graph.manipulation.label"));
        final ButtonType newGraph = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.new"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType loadGraph = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.load"), ButtonBar.ButtonData.NO);
        final Label timeRange = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.timerange"));

        analysisListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {

                graphDataModel.setCurrentAnalysis(newValue);
                AnalysisTimeFrame oldTimeFrame = graphDataModel.getAnalysisTimeFrame();
                AggregationPeriod oldAggregation = AggregationPeriod.parseAggregation(aggregationBox.valueProperty().toString());

                graphDataModel.setAggregationPeriod(AggregationPeriod.NONE);
                AnalysisTimeFrame preview = new AnalysisTimeFrame();
                preview.setTimeFrame(AnalysisTimeFrame.TimeFrame.preview);
                graphDataModel.setAnalysisTimeFrame(preview);

                toolBarView.select(newValue);

                graphDataModel.setAggregationPeriod(oldAggregation);
                graphDataModel.setAnalysisTimeFrame(oldTimeFrame);
            }
        });

        if (graphDataModel.getAnalysisTimeFrame() == null) {
            /**
             * fallback to last 7days as initial value for timeframe
             */
            comboBoxPresetDates.getSelectionModel().select(3);
            applySelectedDatePresetToDataModel(3);
        } else {
            switch (graphDataModel.getAnalysisTimeFrame().getTimeFrame()) {
                case custom:
                    comboBoxPresetDates.getSelectionModel().select(0);
                    applySelectedDatePresetToDataModel(0);
                    break;
                case today:
                    comboBoxPresetDates.getSelectionModel().select(1);
                    applySelectedDatePresetToDataModel(1);
                    break;
                case yesterday:
                    comboBoxPresetDates.getSelectionModel().select(2);
                    applySelectedDatePresetToDataModel(2);
                    break;
                case last7Days:
                    comboBoxPresetDates.getSelectionModel().select(3);
                    applySelectedDatePresetToDataModel(3);
                    break;
                case lastWeek:
                    comboBoxPresetDates.getSelectionModel().select(4);
                    applySelectedDatePresetToDataModel(4);
                    break;
                case last30Days:
                    comboBoxPresetDates.getSelectionModel().select(5);
                    applySelectedDatePresetToDataModel(5);
                    break;
                case lastMonth:
                    comboBoxPresetDates.getSelectionModel().select(6);
                    applySelectedDatePresetToDataModel(6);
                    break;
                case customStartEnd:
                    comboBoxPresetDates.getSelectionModel().select(7);
                    break;
            }
        }

        for (int i = 0; i < 4; i++) {
            programmaticallySetPresetDate[i] = false;
        }

        comboBoxPresetDates.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                applySelectedDatePresetToDataModel(newValue.intValue());
            }
        });

        setupPickerListener();

        this.setTitle(I18n.getInstance().getString("plugin.graph.analysis.dialog.title"));
        this.setResizable(true);

        this.getDialogPane().getButtonTypes().addAll(newGraph, loadGraph);

        Region freeSpace = new Region();
        freeSpace.setPrefWidth(40);

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);
        GridPane.setFillWidth(freeSpace, true);

        /** Column 0 */
        gridLayout.add(analysisListView, 0, 0, 1, 16);

        /** Column 1 **/
        gridLayout.add(freeSpace, 1, 0, 1, 16);

        /** column 2**/
        gridLayout.add(timeRange, 2, 0, 2, 1);
        gridLayout.add(startText, 2, 1);
        gridLayout.add(endText, 2, 3);

        /** Column 3 **/
        gridLayout.add(pickerDateStart, 3, 1);
        gridLayout.add(pickerDateEnd, 3, 3); // column=1 row=0


        /** Column 4 **/
        gridLayout.add(pickerTimeStart, 4, 1);
        gridLayout.add(pickerTimeEnd, 4, 3);

        /** Column 2 - 4 **/
        gridLayout.add(standardSelectionsLabel, 2, 5, 3, 1);
        GridPane.setFillWidth(comboBoxPresetDates, true);
        comboBoxPresetDates.setMaxWidth(200);
        gridLayout.add(comboBoxPresetDates, 2, 6, 3, 1);

        gridLayout.add(customSelectionsLabel, 2, 8, 3, 1);
        GridPane.setFillWidth(comboBoxCustomPeriods, true);
        comboBoxCustomPeriods.setMaxWidth(200);
        gridLayout.add(comboBoxCustomPeriods, 2, 9, 3, 1);

        gridLayout.add(labelAggregation, 2, 11, 3, 1);
        aggregationBox = getAggregationBox();
        GridPane.setFillWidth(aggregationBox, true);
        aggregationBox.setMaxWidth(200);
        gridLayout.add(aggregationBox, 2, 12, 3, 1);

        gridLayout.add(labelMath, 2, 14, 3, 1);
        mathBox = getMathBox();
        GridPane.setFillWidth(mathBox, true);
        mathBox.setMaxWidth(200);
        gridLayout.add(mathBox, 2, 15, 3, 1);

        GridPane.setFillWidth(analysisListView, true);
        GridPane.setFillHeight(analysisListView, true);
        analysisListView.setMinWidth(600d);
        GridPane.setHgrow(analysisListView, Priority.ALWAYS);

        this.getDialogPane().setContent(gridLayout);

        double maxScreenWidth = Screen.getPrimary().getBounds().getWidth();
        this.getDialogPane().setPrefWidth(maxScreenWidth - 250);

    }

    private ComboBox<String> getAggregationBox() {
        List<String> aggList = new ArrayList<>();

        String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");
        String keyHourly = I18n.getInstance().getString("plugin.graph.interval.hourly");
        String keyDaily = I18n.getInstance().getString("plugin.graph.interval.daily");
        String keyWeekly = I18n.getInstance().getString("plugin.graph.interval.weekly");
        String keyMonthly = I18n.getInstance().getString("plugin.graph.interval.monthly");
        String keyQuarterly = I18n.getInstance().getString("plugin.graph.interval.quarterly");
        String keyYearly = I18n.getInstance().getString("plugin.graph.interval.yearly");


        aggList.add(keyPreset);
        aggList.add(keyHourly);
        aggList.add(keyDaily);
        aggList.add(keyWeekly);
        aggList.add(keyMonthly);
        aggList.add(keyQuarterly);
        aggList.add(keyYearly);

        ComboBox<String> aggregate = new ComboBox<>();
        aggregate.setItems(FXCollections.observableArrayList(aggList));
        aggregate.getSelectionModel().selectFirst();

        if (!graphDataModel.getSelectedData().isEmpty()) {
            for (ChartDataModel chartDataModel : graphDataModel.getSelectedData()) {
                switch (chartDataModel.getAggregationPeriod()) {
                    case NONE:
                        aggregate.valueProperty().setValue(keyPreset);
                        break;
                    case HOURLY:
                        aggregate.valueProperty().setValue(keyHourly);
                        break;
                    case DAILY:
                        aggregate.valueProperty().setValue(keyDaily);
                        break;
                    case WEEKLY:
                        aggregate.valueProperty().setValue(keyWeekly);
                        break;
                    case MONTHLY:
                        aggregate.valueProperty().setValue(keyMonthly);
                        break;
                    case QUARTERLY:
                        aggregate.valueProperty().setValue(keyQuarterly);
                        break;
                    case YEARLY:
                        aggregate.valueProperty().setValue(keyYearly);
                        break;
                }
                break;
            }
        }

        aggregate.valueProperty().addListener((observable, oldValue, newValue) -> {

            graphDataModel.getSelectedData().forEach(data -> {
                if (newValue.equals(keyPreset)) {
                    data.setAggregationPeriod(AggregationPeriod.NONE);
                } else if (newValue.equals(keyHourly)) {
                    data.setAggregationPeriod(AggregationPeriod.HOURLY);
                    mathBox.getSelectionModel().select(1);
                } else if (newValue.equals(keyDaily)) {
                    data.setAggregationPeriod(AggregationPeriod.DAILY);
                    mathBox.getSelectionModel().select(1);
                } else if (newValue.equals(keyWeekly)) {
                    data.setAggregationPeriod(AggregationPeriod.WEEKLY);
                    mathBox.getSelectionModel().select(1);
                } else if (newValue.equals(keyMonthly)) {
                    data.setAggregationPeriod(AggregationPeriod.MONTHLY);
                    mathBox.getSelectionModel().select(1);
                } else if (newValue.equals(keyQuarterly)) {
                    data.setAggregationPeriod(AggregationPeriod.QUARTERLY);
                    mathBox.getSelectionModel().select(1);
                } else if (newValue.equals(keyYearly)) {
                    data.setAggregationPeriod(AggregationPeriod.YEARLY);
                    mathBox.getSelectionModel().select(1);
                }
            });

        });
        return aggregate;
    }

    private ComboBox<String> getMathBox() {
        List<String> mathList = new ArrayList<>();

        String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");

        String keyTotal = I18n.getInstance().getString("plugin.graph.manipulation.total");
        String keyRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.runningmean");
        String keyCentricRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean");
        String keySortedMin = I18n.getInstance().getString("plugin.graph.manipulation.sortedmin");
        String keySortedMax = I18n.getInstance().getString("plugin.graph.manipulation.sortedmax");

        mathList.add(keyPreset);
        mathList.add(keyTotal);
        mathList.add(keyRunningMean);
        mathList.add(keyCentricRunningMean);
        mathList.add(keySortedMin);
        mathList.add(keySortedMax);

        ComboBox<String> math = new ComboBox<>();
        math.setItems(FXCollections.observableArrayList(mathList));
        math.getSelectionModel().selectFirst();

        if (!graphDataModel.getSelectedData().isEmpty()) {
            for (ChartDataModel chartDataModel : graphDataModel.getSelectedData()) {
                switch (chartDataModel.getManipulationMode()) {
                    case NONE:
                        math.valueProperty().setValue(keyPreset);
                        break;
                    case TOTAL:
                        math.valueProperty().setValue(keyPreset);
                        break;
                    case RUNNING_MEAN:
                        math.valueProperty().setValue(keyRunningMean);
                        aggregationBox.getSelectionModel().select(0);
                        break;
                    case CENTRIC_RUNNING_MEAN:
                        math.valueProperty().setValue(keyCentricRunningMean);
                        aggregationBox.getSelectionModel().select(0);
                        break;
                    case SORTED_MAX:
                        math.valueProperty().setValue(keySortedMax);
                        aggregationBox.getSelectionModel().select(0);
                        break;
                    case SORTED_MIN:
                        math.valueProperty().setValue(keySortedMin);
                        aggregationBox.getSelectionModel().select(0);
                        break;
                }
                break;
            }
        }

        math.valueProperty().addListener((observable, oldValue, newValue) -> {

            graphDataModel.getSelectedData().forEach(data -> {
                if (newValue.equals(keyPreset)) {
                    data.setManipulationMode(ManipulationMode.NONE);
                } else if (newValue.equals(keyTotal)) {
                    data.setManipulationMode(ManipulationMode.TOTAL);
                } else if (newValue.equals(keyRunningMean)) {
                    data.setManipulationMode(ManipulationMode.RUNNING_MEAN);
                    aggregationBox.getSelectionModel().select(0);
                } else if (newValue.equals(keyCentricRunningMean)) {
                    data.setManipulationMode(ManipulationMode.CENTRIC_RUNNING_MEAN);
                    aggregationBox.getSelectionModel().select(0);
                } else if (newValue.equals(keySortedMax)) {
                    data.setManipulationMode(ManipulationMode.SORTED_MAX);
                    aggregationBox.getSelectionModel().select(0);
                } else if (newValue.equals(keySortedMin)) {
                    data.setManipulationMode(ManipulationMode.SORTED_MIN);
                    aggregationBox.getSelectionModel().select(0);
                }

            });

        });
        return math;
    }

    private void applySelectedDatePresetToDataModel(Integer newValue) {
        switch (newValue) {
            //Custom
            case 0:
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.custom));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = false;
                }

                setPicker(null, null);
                break;
            //today
            case 1:
                dateHelper.setType(DateHelper.TransformType.TODAY);
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.today));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //yesterday
            case 2:
                dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.yesterday));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 7 days
            case 3:
                dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.last7Days));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last Week
            case 4:
                dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.lastWeek));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            //last 30 days
            case 5:
                dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.last30Days));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case 6:
                //last Month
                dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.lastMonth));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                break;
            case 7:
                Long id = graphDataModel.getAnalysisTimeFrame().getId();
                graphDataModel.setAnalysisTimeFrame(new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.customStartEnd, id));
                for (int i = 0; i < 4; i++) {
                    programmaticallySetPresetDate[i] = true;
                }
                break;
            default:
                break;
        }
    }

    private Callback<DatePicker, DateCell> getAllowedTimeFrameForDataRows() {
        return new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        LocalDate min = null;
                        LocalDate max = null;
                        for (ChartDataModel mdl : graphDataModel.getSelectedData()) {
                            if (!mdl.getSelectedcharts().isEmpty()) {
                                JEVisAttribute att = mdl.getAttribute();

                                LocalDate min_check = LocalDate.of(
                                        att.getTimestampFromFirstSample().getYear(),
                                        att.getTimestampFromFirstSample().getMonthOfYear(),
                                        att.getTimestampFromFirstSample().getDayOfMonth());

                                LocalDate max_check = LocalDate.of(
                                        att.getTimestampFromLastSample().getYear(),
                                        att.getTimestampFromLastSample().getMonthOfYear(),
                                        att.getTimestampFromLastSample().getDayOfMonth());

                                if (min == null || min_check.isBefore(min)) min = min_check;
                                if (max == null || max_check.isAfter(max)) max = max_check;
                            }
                        }

                        if (min != null && item.isBefore(min)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (max != null && item.isAfter(max)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
    }

    private void checkForCustomizedWorkdayTimeFrame() {
        if (graphDataModel.getWorkdayStart() != null && graphDataModel.getWorkdayEnd() != null) {
            dateHelper.setStartTime(graphDataModel.getWorkdayStart());
            dateHelper.setEndTime(graphDataModel.getWorkdayEnd());
            if (graphDataModel.getWorkdayEnd().isAfter(graphDataModel.getWorkdayStart())) {
                setSelectedStart(new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        graphDataModel.getWorkdayStart().getHour(), graphDataModel.getWorkdayStart().getMinute(), 0).minusDays(7));
                setSelectedEnd(new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        graphDataModel.getWorkdayEnd().getHour(), graphDataModel.getWorkdayEnd().getMinute(), 59, 999));
            } else {
                setSelectedStart(new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        graphDataModel.getWorkdayStart().getHour(), graphDataModel.getWorkdayStart().getMinute(), 0).minusDays(8));
                setSelectedEnd(new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        graphDataModel.getWorkdayEnd().getHour(), graphDataModel.getWorkdayEnd().getMinute(), 59, 999));
            }
        }
    }

    private void setupPickerListener() {
        pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateStart.valueProperty().getValue();
                LocalTime lt = pickerTimeStart.valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedStart(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[0]) comboBoxPresetDates.getSelectionModel().select(0);
                programmaticallySetPresetDate[0] = false;
            }
        });

        pickerTimeStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateStart.valueProperty().getValue();
                LocalTime lt = pickerTimeStart.valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedStart(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[1]) comboBoxPresetDates.getSelectionModel().select(0);
                programmaticallySetPresetDate[1] = false;
            }
        });

        pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateEnd.valueProperty().getValue();
                LocalTime lt = pickerTimeEnd.valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedEnd(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[2]) comboBoxPresetDates.getSelectionModel().select(0);
                programmaticallySetPresetDate[2] = false;
            }
        });

        pickerTimeEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateEnd.valueProperty().getValue();
                LocalTime lt = pickerTimeEnd.valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedEnd(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (!programmaticallySetPresetDate[3]) comboBoxPresetDates.getSelectionModel().select(0);
                programmaticallySetPresetDate[3] = false;
            }
        });
    }


    private ComboBox<String> getCustomPeriodsComboBox() {

        ObservableList<String> customPeriods = FXCollections.observableArrayList();
        List<JEVisObject> listCalendarDirectories = null;
        List<JEVisObject> listCustomPeriods = null;
        List<CustomPeriodObject> listCustomPeriodObjects = null;

        try {
            try {
                JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                listCalendarDirectories = ds.getObjects(calendarDirectoryClass, false);
            } catch (JEVisException e) {
                logger.error("Error: could not get calendar directories", e);
            }
            if (listCalendarDirectories.isEmpty()) {
                List<JEVisObject> listBuildings = new ArrayList<>();
                try {
                    JEVisClass building = ds.getJEVisClass("Building");
                    listBuildings = ds.getObjects(building, false);

                    if (!listBuildings.isEmpty()) {
                        JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                        JEVisObject calendarDirectory = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.calendardir.defaultname"), calendarDirectoryClass);
                        calendarDirectory.commit();
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

        customPeriods.add(I18n.getInstance().getString("plugin.graph.dialog.loadnew.none"));

        for (JEVisObject obj : listCustomPeriods) {
            if (obj != null) {
                if (listCustomPeriodObjects == null) listCustomPeriodObjects = new ArrayList<>();
                CustomPeriodObject cpo = new CustomPeriodObject(obj, new ObjectHandler(ds));
                if (cpo.isVisible()) {
                    listCustomPeriodObjects.add(cpo);
                    customPeriods.add(cpo.getObject().getName());
                }
            }
        }

        ComboBox tempBox = new ComboBox<>(customPeriods);
        tempBox.getSelectionModel().select(0);

        if (customPeriods.size() > 1) {

            List<CustomPeriodObject> finalListCustomPeriodObjects = listCustomPeriodObjects;
            tempBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue == null || newValue != oldValue) {
                    if (newValue.intValue() > 0) {
                        for (CustomPeriodObject cpo : finalListCustomPeriodObjects) {
                            if (finalListCustomPeriodObjects.indexOf(cpo) + 1 == newValue.intValue()) {
                                dateHelper.setCustomPeriodObject(cpo);
                                dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                                dateHelper.setStartTime(graphDataModel.getWorkdayStart());
                                dateHelper.setEndTime(graphDataModel.getWorkdayEnd());

                                graphDataModel.getAnalysisTimeFrame().setId(cpo.getObject().getID());

                                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                                setSelectedStart(dateHelper.getStartDate());
                                setSelectedEnd(dateHelper.getEndDate());
                            }
                        }
                        comboBoxPresetDates.getSelectionModel().select(7);
                    }
                }
            });
        } else {
            tempBox.setDisable(true);
        }

        return tempBox;
    }

    private void setPicker(DateTime start, DateTime end) {
        //setSelectedStart(start);
        //setSelectedEnd(end);
        if (start != null && end != null) {
            pickerDateStart.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
            pickerDateEnd.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
            pickerTimeStart.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));
            pickerTimeEnd.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute()));
        } else {
            DateTime startFromModel = null;
            DateTime endFromModel = null;
            for (ChartDataModel mdl : graphDataModel.getSelectedData()) {
                if (startFromModel == null) startFromModel = mdl.getSelectedStart();
                else if (mdl.getSelectedStart() != null && mdl.getSelectedStart().isBefore(startFromModel))
                    startFromModel = mdl.getSelectedStart();

                if (endFromModel == null) endFromModel = mdl.getSelectedEnd();
                else if (mdl.getSelectedEnd() != null && mdl.getSelectedEnd().isAfter(endFromModel))
                    endFromModel = mdl.getSelectedEnd();
            }

            pickerDateStart.valueProperty().setValue(LocalDate.of(startFromModel.getYear(), startFromModel.getMonthOfYear(), startFromModel.getDayOfMonth()));
            pickerDateEnd.valueProperty().setValue(LocalDate.of(endFromModel.getYear(), endFromModel.getMonthOfYear(), endFromModel.getDayOfMonth()));
            pickerTimeStart.valueProperty().setValue(LocalTime.of(startFromModel.getHourOfDay(), startFromModel.getMinuteOfHour(), startFromModel.getSecondOfMinute()));
            pickerTimeEnd.valueProperty().setValue(LocalTime.of(endFromModel.getHourOfDay(), endFromModel.getMinuteOfHour(), endFromModel.getSecondOfMinute()));
        }
    }

    public void setSelectedStart(DateTime selectedStart) {

        graphDataModel.getSelectedData().forEach(dataModel -> {
            dataModel.setSelectedStart(selectedStart);
            dataModel.setSomethingChanged(true);
        });
    }

    public void setSelectedEnd(DateTime selectedEnd) {

        graphDataModel.getSelectedData().forEach(dataModel -> {
            dataModel.setSelectedEnd(selectedEnd);
            dataModel.setSomethingChanged(true);
        });
    }
}
