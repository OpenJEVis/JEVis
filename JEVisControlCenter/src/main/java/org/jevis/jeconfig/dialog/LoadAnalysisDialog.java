package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import jfxtras.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class LoadAnalysisDialog {
    private static final Logger logger = LogManager.getLogger(LoadAnalysisDialog.class);
    private final ObjectRelations objectRelations;
    private Response response = Response.CANCEL;
    private Stage stage;
    private AnalysisDataModel analysisDataModel;
    private PickerCombo pickerCombo;
    private ComboBox<TimeFrame> presetDateBox;
    private JFXDatePicker pickerDateStart;
    private JFXTimePicker pickerTimeStart;
    private JFXDatePicker pickerDateEnd;
    private JFXTimePicker pickerTimeEnd;
    private FilteredList<JEVisObject> filteredData;
    private TextField filterInput = new TextField();
    private ListView<JEVisObject> analysisListView;
    private JEVisDataSource ds;
    private DateHelper dateHelper = new DateHelper();
    private ComboBox<AggregationPeriod> aggregationBox;
    private DisabledItemsComboBox<ManipulationMode> mathBox;
    private List<CustomPeriodObject> finalListCustomPeriodObjects;
    private ComboBox<String> comboBoxCustomPeriods;
    private Button loadButton;
    private Button newButton;
    private CheckBox drawOptimization;


    public LoadAnalysisDialog(JEVisDataSource ds, AnalysisDataModel data) {
        this.analysisDataModel = data;
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
    }

    public Response show() {

        response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(JEConfig.getStage());
        stage.setTitle(I18n.getInstance().getString("plugin.graph.analysis.dialog.title"));
        stage.setResizable(true);
//        double maxScreenWidth = Screen.getPrimary().getBounds().getWidth();
//        stage.setWidth(maxScreenWidth - 250);

        filteredData = new FilteredList<>(analysisDataModel.getObservableListAnalyses(), s -> true);

        filterInput.textProperty().addListener(obs -> {
            String filter = filterInput.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = (objectRelations.getObjectPath(s) + s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (objectRelations.getObjectPath(s) + s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
        });

        analysisListView = new ListView<>(filteredData);
        analysisListView.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) ->
                        Platform.runLater(() ->
                                analysisListView.scrollTo(analysisListView.getSelectionModel().getSelectedIndex())));

        analysisListView.setCellFactory(param -> new ListCell<JEVisObject>() {

            @Override
            protected void updateItem(JEVisObject obj, boolean empty) {
                super.updateItem(obj, empty);
                if (empty || obj == null || obj.getName() == null) {
                    setText("");
                } else {
                    if (!analysisDataModel.getMultipleDirectories())
                        setText(obj.getName());
                    else {
                        String prefix = objectRelations.getObjectPath(obj);

                        setText(prefix + obj.getName());
                    }
                }
            }
        });

        if (!analysisListView.getItems().isEmpty()) {
            dateHelper.setStartTime(analysisDataModel.getWorkdayStart());
            dateHelper.setEndTime(analysisDataModel.getWorkdayEnd());
        }

        if (analysisDataModel.getCurrentAnalysis() != null && analysisDataModel.getCurrentAnalysis().getName() != null
                && !analysisDataModel.getCurrentAnalysis().getName().equals(""))
            analysisListView.getSelectionModel().select(analysisDataModel.getCurrentAnalysis());

        updateGridLayout();

        stage.showAndWait();

        return response;
    }

    private void addListener() {
        analysisListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {

                analysisDataModel.setCurrentAnalysis(newValue);
                pickerCombo.updateCellFactory();
                analysisDataModel.setAggregationPeriod(AggregationPeriod.NONE);
                analysisDataModel.setManipulationMode(ManipulationMode.NONE);
                analysisDataModel.resetToolbarSettings();
                AnalysisTimeFrame preview = new AnalysisTimeFrame(TimeFrame.PREVIEW);
                analysisDataModel.setAnalysisTimeFrameForAllModels(preview);
            }
        });

        presetDateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                switch (newValue) {
                    //Custom
                    case CUSTOM:
                        break;
                    //today
                    case TODAY:
                        dateHelper.setType(DateHelper.TransformType.TODAY);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.TODAY));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    //yesterday
                    case YESTERDAY:
                        dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.YESTERDAY));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    //last 7 days
                    case LAST_7_DAYS:
                        dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.LAST_7_DAYS));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    //this Week
                    case THIS_WEEK:
                        dateHelper.setType(DateHelper.TransformType.THISWEEK);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.THIS_WEEK));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    //last Week
                    case LAST_WEEK:
                        dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.LAST_WEEK));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    //last 30 days
                    case LAST_30_DAYS:
                        dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.LAST_30_DAYS));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    case THIS_MONTH:
                        //last Month
                        dateHelper.setType(DateHelper.TransformType.THISMONTH);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.THIS_MONTH));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    case LAST_MONTH:
                        //last Month
                        dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.LAST_MONTH));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    case THIS_YEAR:
                        //this Year
                        dateHelper.setType(DateHelper.TransformType.THISYEAR);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.THIS_YEAR));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    case LAST_YEAR:
                        //last Year
                        dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                        analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(new AnalysisTimeFrame(TimeFrame.LAST_YEAR));
                        analysisDataModel.getGlobalAnalysisTimeFrame().setStart(dateHelper.getStartDate());
                        analysisDataModel.getGlobalAnalysisTimeFrame().setEnd(dateHelper.getEndDate());
                        updateGridLayout();
                        break;
                    case CUSTOM_START_END:
                        break;
                    default:
                        break;
                }
            }
        });

        pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = analysisDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = analysisDataModel.getGlobalAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime startDate = null;
                if (start != null) {
                    startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            start.getHourOfDay(), start.getMinuteOfHour(), 0, 0);
                } else {
                    startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            now.getHourOfDay(), now.getMinuteOfHour(), 0, 0);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                analysisTimeFrame.setStart(startDate);
                if (end != null) {
                    analysisTimeFrame.setEnd(end);
                } else {
                    analysisTimeFrame.setEnd(now);
                }
                analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(analysisTimeFrame);
                updateGridLayout();
            }
        });

        pickerTimeStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = analysisDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = analysisDataModel.getGlobalAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime startDate = null;
                if (start != null) {
                    startDate = new DateTime(
                            start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 0, 0);
                } else {
                    startDate = new DateTime(
                            now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 0, 0);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                analysisTimeFrame.setStart(startDate);
                if (end != null) {
                    analysisTimeFrame.setEnd(end);
                } else {
                    analysisTimeFrame.setEnd(now);
                }
                analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(analysisTimeFrame);
                updateGridLayout();
            }
        });

        pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = analysisDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = analysisDataModel.getGlobalAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime endDate = null;
                if (end != null) {
                    endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            end.getHourOfDay(), end.getMinuteOfHour(), 59, 999);
                } else {
                    endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            now.getHourOfDay(), now.getMinuteOfHour(), 59, 999);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                if (start != null) {
                    analysisTimeFrame.setStart(start);
                } else {
                    analysisTimeFrame.setStart(now);
                }
                analysisTimeFrame.setEnd(endDate);
                analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(analysisTimeFrame);
                updateGridLayout();
            }
        });

        pickerTimeEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = analysisDataModel.getGlobalAnalysisTimeFrame().getStart();
                DateTime end = analysisDataModel.getGlobalAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime endDate = null;
                if (end != null) {
                    endDate = new DateTime(
                            end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 59, 999);
                } else {
                    endDate = new DateTime(
                            now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 59, 999);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                if (start != null) {
                    analysisTimeFrame.setStart(start);
                } else {
                    analysisTimeFrame.setStart(now);
                }
                analysisTimeFrame.setEnd(endDate);
                analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(analysisTimeFrame);
                updateGridLayout();
            }
        });

        mathBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                analysisDataModel.setManipulationMode(newValue);
                updateGridLayout();
            }
        });

        aggregationBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                analysisDataModel.setAggregationPeriod(newValue);
                updateGridLayout();
            }
        });

        loadButton.setOnAction(event -> {
            response = Response.LOAD;

            if (analysisListView.getSelectedItem() != null) {
                analysisDataModel.setCurrentAnalysis(analysisListView.getSelectedItem());
            }
            analysisDataModel.setAggregationPeriod(aggregationBox.getSelectionModel().getSelectedItem());
            analysisDataModel.setManipulationMode(mathBox.getSelectionModel().getSelectedItem());
            AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(presetDateBox.getSelectionModel().getSelectedItem());
            if (presetDateBox.getSelectionModel().getSelectedItem().equals(TimeFrame.CUSTOM_START_END)) {
                for (CustomPeriodObject cpo : finalListCustomPeriodObjects) {
                    if (finalListCustomPeriodObjects.indexOf(cpo) + 1 == comboBoxCustomPeriods.getSelectionModel().getSelectedIndex()) {
                        analysisTimeFrame.setId(cpo.getObject().getID());
                    }
                }
            } else if (presetDateBox.getSelectionModel().getSelectedItem().equals(TimeFrame.CUSTOM)) {
                DateTime start = new DateTime(pickerDateStart.getValue().getYear(), pickerDateStart.getValue().getMonthValue(), pickerDateStart.getValue().getDayOfMonth(),
                        pickerTimeStart.getValue().getHour(), pickerTimeStart.getValue().getMinute(), pickerTimeStart.getValue().getSecond());
                DateTime end = new DateTime(pickerDateEnd.getValue().getYear(), pickerDateEnd.getValue().getMonthValue(), pickerDateEnd.getValue().getDayOfMonth(),
                        pickerTimeEnd.getValue().getHour(), pickerTimeEnd.getValue().getMinute(), pickerTimeEnd.getValue().getSecond());
                analysisTimeFrame.setStart(start);
                analysisTimeFrame.setEnd(end);
            }
            analysisDataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

            stage.close();
        });

        newButton.setOnAction(event -> {
            response = Response.NEW;

            stage.close();

        });

        drawOptimization.setOnAction(event -> {
            HiddenConfig.CHART_PRECISION_ON = drawOptimization.isSelected();
        });
    }

    private DisabledItemsComboBox<ManipulationMode> getMathBox() {

        final String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");

        final String keyRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.runningmean");
        final String keyCentricRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean");
        final String keySortedMin = I18n.getInstance().getString("plugin.graph.manipulation.sortedmin");
        final String keySortedMax = I18n.getInstance().getString("plugin.graph.manipulation.sortedmax");
        final String keyCumulate = I18n.getInstance().getString("plugin.graph.manipulation.cumulate");

        DisabledItemsComboBox<ManipulationMode> math = new DisabledItemsComboBox<>();
        List<ManipulationMode> customList = new ArrayList<>();
        customList.add(ManipulationMode.NONE);
        customList.add(ManipulationMode.RUNNING_MEAN);
        customList.add(ManipulationMode.CENTRIC_RUNNING_MEAN);
        customList.add(ManipulationMode.SORTED_MIN);
        customList.add(ManipulationMode.SORTED_MAX);
        customList.add(ManipulationMode.CUMULATE);

        math.setItems(FXCollections.observableArrayList(customList));
        math.getSelectionModel().selectFirst();

        Callback<javafx.scene.control.ListView<ManipulationMode>, ListCell<ManipulationMode>> cellFactory = new Callback<javafx.scene.control.ListView<ManipulationMode>, ListCell<ManipulationMode>>() {
            @Override
            public ListCell<ManipulationMode> call(javafx.scene.control.ListView<ManipulationMode> param) {
                return new ListCell<ManipulationMode>() {
                    @Override
                    protected void updateItem(ManipulationMode manipulationMode, boolean empty) {
                        super.updateItem(manipulationMode, empty);
                        if (empty || manipulationMode == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (manipulationMode) {
                                case NONE:
                                    text = keyPreset;
                                    break;
                                case RUNNING_MEAN:
                                    text = keyRunningMean;
                                    break;
                                case CENTRIC_RUNNING_MEAN:
                                    text = keyCentricRunningMean;
                                    break;
                                case SORTED_MIN:
                                    text = keySortedMin;
                                    break;
                                case SORTED_MAX:
                                    text = keySortedMax;
                                    break;
                                case CUMULATE:
                                    text = keyCumulate;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        math.setCellFactory(cellFactory);
        math.setButtonCell(cellFactory.call(null));

        math.getSelectionModel().select(analysisDataModel.getManipulationMode());

        return math;
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

        ComboBox<String> tempBox = new ComboBox<>(customPeriods);

        if (analysisDataModel.getGlobalAnalysisTimeFrame().getTimeFrame().equals(TimeFrame.CUSTOM_START_END)) {
            for (CustomPeriodObject cpo : listCustomPeriodObjects) {
                if (cpo.getObject().getID().equals(analysisDataModel.getGlobalAnalysisTimeFrame().getId())) {
                    tempBox.getSelectionModel().select(listCustomPeriodObjects.indexOf(cpo) + 1);
                }
            }
        } else {
            tempBox.getSelectionModel().select(0);
        }

        if (customPeriods.size() > 1) {

            finalListCustomPeriodObjects = listCustomPeriodObjects;
            tempBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    if (newValue.intValue() > 0) {
                        for (CustomPeriodObject cpo : finalListCustomPeriodObjects) {
                            if (finalListCustomPeriodObjects.indexOf(cpo) + 1 == newValue.intValue()) {
                                dateHelper.setCustomPeriodObject(cpo);
                                dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                                dateHelper.setStartTime(analysisDataModel.getWorkdayStart());
                                dateHelper.setEndTime(analysisDataModel.getWorkdayEnd());

                                AnalysisTimeFrame newTimeFrame = new AnalysisTimeFrame();
                                newTimeFrame.setTimeFrame(TimeFrame.CUSTOM_START_END);
                                newTimeFrame.setId(cpo.getObject().getID());
                                newTimeFrame.setStart(dateHelper.getStartDate());
                                newTimeFrame.setEnd(dateHelper.getEndDate());

                                analysisDataModel.setGlobalAnalysisTimeFrameNOEVENT(newTimeFrame);
                                updateGridLayout();
                            }
                        }
                    }
                }
            });
        } else {
            tempBox.setDisable(true);
        }

        return tempBox;
    }

    public Response getResponse() {
        return response;
    }

    private void updateGridLayout() {
        Platform.runLater(() -> {

            pickerCombo = new PickerCombo(analysisDataModel, null);
            pickerCombo.updateCellFactory();
            presetDateBox = pickerCombo.getPresetDateBox();
            pickerDateStart = pickerCombo.getStartDatePicker();
            pickerTimeStart = pickerCombo.getStartTimePicker();
            pickerDateEnd = pickerCombo.getEndDatePicker();
            pickerTimeEnd = pickerCombo.getEndTimePicker();
            filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

            Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
            Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
            Label standardSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.standard"));
            Label customSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.custom"));
            Label labelAggregation = new Label(I18n.getInstance().getString("plugin.graph.interval.label"));
            Label labelMath = new Label(I18n.getInstance().getString("plugin.graph.manipulation.label"));
            final Label timeRange = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.timerange"));

            Region freeSpace = new Region();
            freeSpace.setPrefWidth(40);

            GridPane gridLayout = new GridPane();
            gridLayout.setPadding(new Insets(10, 10, 10, 10));
            gridLayout.setVgap(10);
//            GridPane.setFillWidth(freeSpace, true);

            /** Column 0 */
            gridLayout.add(filterInput, 0, 0, 1, 1);
            gridLayout.add(analysisListView, 0, 1, 1, 12);

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
            GridPane.setFillWidth(presetDateBox, true);
            presetDateBox.setMaxWidth(200);
            gridLayout.add(presetDateBox, 2, 6, 3, 1);

            gridLayout.add(customSelectionsLabel, 2, 7, 3, 1);
            comboBoxCustomPeriods = getCustomPeriodsComboBox();
            GridPane.setFillWidth(comboBoxCustomPeriods, true);
            comboBoxCustomPeriods.setMaxWidth(200);
            gridLayout.add(comboBoxCustomPeriods, 2, 8, 3, 1);

            gridLayout.add(labelAggregation, 2, 9, 3, 1);
            aggregationBox = new AggregationBox(analysisDataModel, null);
            GridPane.setFillWidth(aggregationBox, true);
            aggregationBox.setMaxWidth(200);
            gridLayout.add(aggregationBox, 2, 10, 3, 1);

            gridLayout.add(labelMath, 2, 11, 3, 1);
            mathBox = getMathBox();
            GridPane.setFillWidth(mathBox, true);
            mathBox.setMaxWidth(200);
            gridLayout.add(mathBox, 2, 12, 3, 1);

            GridPane.setFillWidth(analysisListView, true);
            GridPane.setFillHeight(analysisListView, true);
            if (!analysisDataModel.getMultipleDirectories()) analysisListView.setMinWidth(600d);
            else analysisListView.setMinWidth(900d);
//            analysisListView.setMaxWidth(600d);
            GridPane.setHgrow(analysisListView, Priority.ALWAYS);
            GridPane.setVgrow(analysisListView, Priority.ALWAYS);
            GridPane.setFillHeight(analysisListView, true);

            HBox buttonBox = new HBox(10);
            Region spacer = new Region();
            loadButton = new Button(I18n.getInstance().getString("plugin.graph.analysis.load"));
            newButton = new Button(I18n.getInstance().getString("plugin.graph.analysis.new"));
            drawOptimization = new CheckBox(I18n.getInstance().getString("plugin.graph.analysis.drawopt"));
            drawOptimization.setSelected(HiddenConfig.CHART_PRECISION_ON);

            loadButton.setDefaultButton(true);

            HBox.setHgrow(loadButton, Priority.NEVER);
            HBox.setHgrow(newButton, Priority.NEVER);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox.setMargin(loadButton, new Insets(10));
            HBox.setMargin(newButton, new Insets(10));
            HBox.setMargin(drawOptimization, new Insets(10));

            buttonBox.getChildren().setAll(drawOptimization, spacer, loadButton, newButton);
            VBox root = new VBox();
            Separator sep = new Separator(Orientation.HORIZONTAL);

            root.getChildren().setAll(gridLayout, sep, buttonBox);

            VBox.setVgrow(analysisListView, Priority.ALWAYS);
            VBox.setVgrow(gridLayout, Priority.ALWAYS);
            VBox.setVgrow(sep, Priority.NEVER);
            VBox.setVgrow(buttonBox, Priority.NEVER);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            addListener();

        });
    }
}
