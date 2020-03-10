package org.jevis.jeconfig.application.Chart;

import org.joda.time.DateTime;

public class AnalysisTimeFrame {

    private TimeFrame timeFrame = TimeFrame.TODAY;

    private long id = 0l;
    private String name = "";
    private DateTime start = DateTime.now();
    private DateTime end = DateTime.now();

    public AnalysisTimeFrame() {
    }

    public AnalysisTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public AnalysisTimeFrame(TimeFrame timeFrame, long id, String name) {
        this.timeFrame = timeFrame;
        this.id = id;
        this.name = name;
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
                return TimeFrame.PREVIEW;
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
}