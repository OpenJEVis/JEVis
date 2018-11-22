package org.jevis.application.Chart;

public class AnalysisTimeFrame {

    private TimeFrame timeFrame = TimeFrame.last7Days;

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
                return TimeFrame.custom;
            case "today":
                return TimeFrame.today;
            case "last7Days":
                return TimeFrame.last7Days;
            case "last30Days":
                return TimeFrame.last30Days;
            case "yesterday":
                return TimeFrame.yesterday;
            case "lastWeek":
                return TimeFrame.lastWeek;
            case "lastMonth":
                return TimeFrame.lastMonth;
            case "customStartEnd":
                return TimeFrame.customStartEnd;
            case "preview":
                return TimeFrame.preview;
        }
        return TimeFrame.last7Days;
    }

    public enum TimeFrame {
        custom("custom"),
        today("today"),
        last7Days("last7Days"),
        last30Days("last30Days"),
        yesterday("yesterday"),
        lastWeek("lastWeek"),
        lastMonth("lastMonth"),
        customStartEnd("customStartEnd"),
        preview("preview");

        private final String name;

        TimeFrame(String s) {
            this.name = s;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}
