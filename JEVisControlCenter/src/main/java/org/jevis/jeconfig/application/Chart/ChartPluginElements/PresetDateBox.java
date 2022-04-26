package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.skins.JFXComboBoxListViewSkin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.jevis.jeconfig.application.Chart.TimeFrame.CUSTOM_START_END;

public class PresetDateBox extends JFXComboBox<AnalysisTimeFrame> {
    private static final Logger logger = LogManager.getLogger(PresetDateBox.class);
    private final DateHelper dateHelper = new DateHelper();
    private DateTime startDate = DateTime.now();
    private DateTime endDate = DateTime.now();

    public PresetDateBox() {
        super();

        this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                JFXComboBoxListViewSkin<?> skin = (JFXComboBoxListViewSkin<?>) this.getSkin();
                if (skin != null) {
                    ListView<?> popupContent = (ListView<?>) skin.getPopupContent();
                    if (popupContent != null) {
                        popupContent.scrollTo(this.getSelectionModel().getSelectedIndex());
                    }
                }
            });
        });

        setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.presetdate")));

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
        final String theYearBeforeLast = I18n.getInstance().getString("plugin.graph.changedate.buttontheyearbeforelast");
        final String customStartEnd = I18n.getInstance().getString("plugin.graph.changedate.buttoncustomstartend");
        final String preview = I18n.getInstance().getString("plugin.graph.changedate.preview");

        List<AnalysisTimeFrame> analysisTimeFrameList = new ArrayList<>();
        for (TimeFrame timeFrame : TimeFrame.values()) {
            AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(timeFrame);

            if (timeFrame != CUSTOM_START_END) {
                analysisTimeFrameList.add(analysisTimeFrame);
            }
        }

        setItems(FXCollections.observableArrayList(analysisTimeFrameList));

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
                                case THE_YEAR_BEFORE_LAST:
                                    text = theYearBeforeLast;
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
                                    break;
                                case CUSTOM_START_END:
                                    text = analysisTimeFrame.getName();
                                    setTextFill(Color.BLACK);
                                    setDisable(false);
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
        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));
    }

    public DateTime getStartDate() {
        updateSelection();
        return startDate;
    }

    private void updateSelection() {
        switch (getSelectionModel().getSelectedItem().getTimeFrame()) {
            //Custom
            case CUSTOM:
                break;
            //today
            case TODAY:
                dateHelper.setType(DateHelper.TransformType.TODAY);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            //yesterday
            case YESTERDAY:
                dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            //last 7 days
            case LAST_7_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            //last Week
            case THIS_WEEK:
                dateHelper.setType(DateHelper.TransformType.THISWEEK);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            //last Week
            case LAST_WEEK:
                dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            //last 30 days
            case LAST_30_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            case THIS_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.THISMONTH);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            case LAST_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            case THIS_YEAR:
                //last Month
                dateHelper.setType(DateHelper.TransformType.THISYEAR);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            case LAST_YEAR:
                //last year
                dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            case THE_YEAR_BEFORE_LAST:
                //the year before last
                dateHelper.setType(DateHelper.TransformType.THEYEARBEFORELAST);
                startDate = dateHelper.getStartDate();
                endDate = dateHelper.getEndDate();
                break;
            case CUSTOM_START_END:
                break;
            case PREVIEW:
                break;
        }
    }

    public DateTime getEndDate() {
        updateSelection();
        return endDate;
    }

    public void isWithCustom(JEVisObject object, boolean withCustom) {
        if (withCustom && object != null) {
            getItems().addAll(getCustomPeriods(object));
        } else if (object != null) {
            getItems().removeAll(getCustomPeriods(object));
        }
    }

    private Collection<? extends AnalysisTimeFrame> getCustomPeriods(JEVisObject object) {

        List<JEVisObject> listCalendarDirectories = new ArrayList<>();
        List<JEVisObject> listCustomPeriods = new ArrayList<>();
        List<CustomPeriodObject> listCustomPeriodObjects = new ArrayList<>();
        List<AnalysisTimeFrame> customPeriods = new ArrayList<>();
        JEVisDataSource ds = null;
        WorkDays workDays = null;

        if (object != null) {
            workDays = new WorkDays(object);
            dateHelper.setWorkDays(workDays);
            try {
                ds = object.getDataSource();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        if (ds == null) return null;

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
                dateHelper.setWorkDays(workDays);

                AnalysisTimeFrame newTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM_START_END, cpo.getObject().getID(), cpo.getObject().getName());
                newTimeFrame.setStart(dateHelper.getStartDate());
                newTimeFrame.setEnd(dateHelper.getEndDate());

                customPeriods.add(newTimeFrame);
            }
        }

        return customPeriods;
    }
}
