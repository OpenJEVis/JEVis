package org.jevis.jeconfig.application.Chart;

public class AnalysisTimeFrame {

    private TimeFrame timeFrame = TimeFrame.TODAY;

    private long id = 0l;

    public AnalysisTimeFrame() {
    }

    public AnalysisTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public AnalysisTimeFrame(TimeFrame timeFrame, long id) {
        this.timeFrame = timeFrame;
        this.id = id;
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


}
