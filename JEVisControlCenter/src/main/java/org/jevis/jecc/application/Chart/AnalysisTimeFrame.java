package org.jevis.jecc.application.Chart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.application.Chart.data.ChartData;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.application.Chart.data.DataModel;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.jevis.jecc.application.Chart.TimeFrame.PREVIEW;

public class AnalysisTimeFrame {
    private static final Logger logger = LogManager.getLogger(AnalysisTimeFrame.class);
    private final JEVisDataSource ds;
    private final DateHelper dateHelper = new DateHelper();
    private WorkDays workDays;
    private TimeFrame timeFrame = TimeFrame.TODAY;
    private ChartPlugin chartPlugin;
    private String name = "";
    private DateTime start = DateTime.now();
    private DateTime end = DateTime.now();
    private JEVisObject currentAnalysis;
    private long id = -1;
    private DataModel dataModel;

    public AnalysisTimeFrame(JEVisDataSource ds, JEVisObject currentAnalysis, TimeFrame timeFrame) {
        this.ds = ds;
        this.timeFrame = timeFrame;
        if (currentAnalysis != null) {
            this.workDays = new WorkDays(currentAnalysis);
        } else {
            workDays = new WorkDays(null);
        }

        this.dateHelper.setWorkDays(workDays);

        updateDates();
    }

    public AnalysisTimeFrame(JEVisDataSource ds, ChartPlugin chartPlugin) {
        this.ds = ds;
        this.chartPlugin = chartPlugin;
        if (chartPlugin != null) {
            this.dataModel = chartPlugin.getDataModel();

            if (chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
                this.workDays = new WorkDays(chartPlugin.getDataSettings().getCurrentAnalysis());
            } else {
                try {
                    workDays = new WorkDays(ds.getCurrentUser().getUserObject());
                } catch (Exception e) {
                    logger.error("Could not get User object", e);
                    workDays = new WorkDays(null);
                }
            }
        } else {
            workDays = new WorkDays(null);
        }

        this.dateHelper.setWorkDays(workDays);
    }

    public AnalysisTimeFrame(JEVisDataSource ds, ChartPlugin chartPlugin, TimeFrame timeFrame) {
        this(ds, chartPlugin, timeFrame, null);

    }

    public AnalysisTimeFrame(JEVisDataSource ds, ChartPlugin chartPlugin, TimeFrame timeFrame, JEVisObject customPeriodObject) {
        this(ds, chartPlugin);
        this.timeFrame = timeFrame;

        if (customPeriodObject != null) {
            this.id = customPeriodObject.getID();
            this.name = customPeriodObject.getLocalName(I18n.getInstance().getLocale().getLanguage());
        }

        updateDates();
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeFrame parseTimeFrameFromString(String s) {
        switch (s) {
            case "custom":
                return TimeFrame.CUSTOM;
            case "today":
                return TimeFrame.TODAY;
            case "last7Days":
                return TimeFrame.LAST_7_DAYS;
            case "last30Days":
                return TimeFrame.LAST_30_DAYS;
            case "yesterday":
                return TimeFrame.YESTERDAY;
            case "lastWeek":
                return TimeFrame.LAST_WEEK;
            case "lastMonth":
                return TimeFrame.LAST_MONTH;
            case "thisYear":
                return TimeFrame.THIS_YEAR;
            case "lastYear":
                return TimeFrame.LAST_YEAR;
            case "customStartEnd":
                return TimeFrame.CUSTOM_START_END;
            case "preview":
                return PREVIEW;
        }
        return TimeFrame.TODAY;
    }

    @Override
    public boolean equals(Object analysisTimeFrame) {
        if (analysisTimeFrame instanceof AnalysisTimeFrame) {
            if (this.getTimeFrame() != TimeFrame.CUSTOM_START_END && ((AnalysisTimeFrame) analysisTimeFrame).getTimeFrame() != TimeFrame.CUSTOM_START_END) {
                return this.getTimeFrame().equals(((AnalysisTimeFrame) analysisTimeFrame).getTimeFrame());
            } else {
                return this.getId() == ((AnalysisTimeFrame) analysisTimeFrame).getId();
            }
        } else return false;
    }

    public void updateDates() {

        if (chartPlugin != null && chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
            dateHelper.setWorkDays(new WorkDays(chartPlugin.getDataSettings().getCurrentAnalysis()));
        } else if (currentAnalysis != null) {
            dateHelper.setWorkDays(new WorkDays(currentAnalysis));
        }

        switch (getTimeFrame()) {
            //Custom
            case CUSTOM:
//                    if (analysisTimeFrame.getStart() != null && analysisTimeFrame.getEnd() != null) {
//                        for (ChartDataRow model : chartDataRows) {
//                            setChartDataModelStartAndEnd(model, analysisTimeFrame.getStart(), analysisTimeFrame.getEnd());
//                        }
//                    }
                break;
            //today
            case TODAY:
                dateHelper.setType(DateHelper.TransformType.TODAY);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            //yesterday
            case YESTERDAY:
                dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            //last 7 days
            case LAST_7_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            //last Week
            case THIS_WEEK:
                dateHelper.setType(DateHelper.TransformType.THISWEEK);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            //last Week
            case LAST_WEEK:
                dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            //last 30 days
            case LAST_30_DAYS:
                dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            case THIS_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.THISMONTH);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            case LAST_MONTH:
                //last Month
                dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            case THIS_YEAR:
                //last Month
                dateHelper.setType(DateHelper.TransformType.THISYEAR);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            case LAST_YEAR:
                //last Month
                dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            case THE_YEAR_BEFORE_LAST:
                //last Month
                dateHelper.setType(DateHelper.TransformType.THEYEARBEFORELAST);
                setStart(dateHelper.getStartDate());
                setEnd(dateHelper.getEndDate());
                break;
            case CUSTOM_START_END:
                if (getId() != -1) {
                    try {
                        dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                        CustomPeriodObject cpo = new CustomPeriodObject(ds.getObject(getId()), new ObjectHandler(ds));
                        dateHelper.setCustomPeriodObject(cpo);

                        setStart(dateHelper.getStartDate());
                        setEnd(dateHelper.getEndDate());
                    } catch (Exception e) {
                        logger.error("Error getting custom period object: " + e);
                    }
                }
                break;
            case PREVIEW:
                if (dataModel != null) {
                    checkForPreviewData();
                } else {
                    DateTime now = DateTime.now();
                    setEnd(now);
                    setStart(now.minusDays(1));
                }
                break;
        }
    }

    private void checkForPreviewData() {
        try {

            AtomicReference<DateTime> start = new AtomicReference<>(null);
            AtomicReference<DateTime> end = new AtomicReference<>(DateTime.now());

            for (ChartModel chartModel : dataModel.getChartModels()) {
                for (ChartData chartData : chartModel.getChartData()) {
                    JEVisObject object = ds.getObject(chartData.getId());
                    if (object != null) {
                        JEVisAttribute valueAtt = object.getAttribute(chartData.getAttributeString());
                        if (valueAtt != null && valueAtt.getTimestampOfLastSample() != null) {
                            if (valueAtt.getTimestampOfLastSample().isBefore(end.get())) {
                                end.set(valueAtt.getTimestampOfLastSample());
                            }

                            DateTime newStart = CommonMethods.getStartDateFromSampleRate(valueAtt);
                            if (start.get() == null || start.get().isAfter(newStart)) {
                                start.set(newStart);
                            }
                        }
                    }
                }
            }

            if (start.get() == null) {
                start.set(end.get().minusDays(1));
            }
            setStart(start.get());
            setEnd(end.get());

        } catch (Exception e) {
            logger.error("Error: " + e);
        }
    }

    public LocalDate getLocalEndDate() {
        return LocalDate.of(getEnd().getYear(), getEnd().getMonthOfYear(), getEnd().getDayOfMonth());
    }

    public LocalDate getLocalStartDate() {
        return LocalDate.of(getStart().getYear(), getStart().getMonthOfYear(), getStart().getDayOfMonth());
    }

    public LocalTime getLocalStartTime() {
        return LocalTime.of(getStart().getHourOfDay(), getStart().getMinuteOfHour(), getStart().getSecondOfMinute(), 0);
    }

    public LocalTime getLocalEndTime() {
        return LocalTime.of(getEnd().getHourOfDay(), getEnd().getMinuteOfHour(), getEnd().getSecondOfMinute(), 999999999);
    }
}