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

    public enum TimeFrame {
        custom("custom"),
        today("today"),
        last7Days("last7Days"),
        last30Days("last30Days"),
        yesterday("yesterday"),
        lastWeek("lastWeek"),
        lastMonth("lastMonth"),
        customStartEnd("customStartEnd");

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
