package org.jevis.application.Chart.data;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.object.plugin.TargetHelper;

import static org.jevis.application.Chart.data.CustomPeriodObject.AttributeName.*;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class CustomPeriodObject {
    private JEVisObject object;
    private JEVisObject periodDirectoryObject;
    private SampleHandler sampleHandler;
    private ObjectHandler objectHandler;

    private Boolean visible;

    private String startReferencePoint;
    private Long startYears;
    private Long startMonths;
    private Long startWeeks;
    private Long startDays;
    private Long startHours;
    private Long startMinutes;
    private JEVisObject startReferenceObject;
    private Long startInterval;

    private String endReferencePoint;
    private Long endYears;
    private Long endMonths;
    private Long endWeeks;
    private Long endDays;
    private Long endHours;
    private Long endMinutes;
    private JEVisObject endReferenceObject;
    private Long endInterval;

    public CustomPeriodObject(JEVisObject periodObject, ObjectHandler objectHandler) {
        this.object = periodObject;
        this.objectHandler = objectHandler;
        this.periodDirectoryObject = objectHandler.getFirstParent(periodObject);
        this.sampleHandler = new SampleHandler();
    }

    public JEVisObject getObject() {
        return object;
    }

    public ObjectHandler getObjectHandler() {
        return objectHandler;
    }

    public Boolean isVisible() {
        if (visible == null)
            visible = sampleHandler.getLastSample(getObject(), VISIBILITY.getAttributeName(), false);
        return visible;
    }

    public String getStartReferencePoint() {
        if (startReferencePoint == null)
            startReferencePoint = sampleHandler.getLastSample(getObject(), START_REFERENCE_POINT.getAttributeName(), "");
        return startReferencePoint;
    }

    public Long getStartYears() {
        if (startYears == null)
            startYears = sampleHandler.getLastSample(getObject(), START_YEARS.getAttributeName(), 0L);
        return startYears;
    }

    public Long getStartMonths() {
        if (startMonths == null)
            startMonths = sampleHandler.getLastSample(getObject(), START_MONTHS.getAttributeName(), 0L);
        return startMonths;
    }

    public Long getStartWeeks() {
        if (startWeeks == null)
            startWeeks = sampleHandler.getLastSample(getObject(), START_WEEKS.getAttributeName(), 0L);
        return startWeeks;
    }

    public Long getStartDays() {
        if (startDays == null)
            startDays = sampleHandler.getLastSample(getObject(), START_DAYS.getAttributeName(), 0L);
        return startDays;
    }

    public Long getStartHours() {
        if (startHours == null)
            startHours = sampleHandler.getLastSample(getObject(), START_HOURS.getAttributeName(), 0L);
        return startHours;
    }

    public Long getStartMinutes() {
        if (startMinutes == null)
            startMinutes = sampleHandler.getLastSample(getObject(), START_MINUTES.getAttributeName(), 0L);
        return startMinutes;
    }

    public JEVisObject getStartReferenceObject() {
        if (startReferenceObject == null) {
            try {
                JEVisAttribute att = getObject().getAttribute(START_REFERENCE_OBJECT.getAttributeName());
                TargetHelper th = new TargetHelper(getObject().getDataSource(), att);
                startReferenceObject = th.getObject();
            } catch (Exception e) {
            }
        }
        return startReferenceObject;
    }

    public Long getStartInterval() {
        if (startInterval == null)
            startInterval = sampleHandler.getLastSample(getObject(), START_INTERVAL.getAttributeName(), 0L);
        return startInterval;
    }

    public String getEndReferencePoint() {
        if (endReferencePoint == null)
            endReferencePoint = sampleHandler.getLastSample(getObject(), END_REFERENCE_POINT.getAttributeName(), "");
        return endReferencePoint;
    }

    public Long getEndYears() {
        if (endYears == null)
            endYears = sampleHandler.getLastSample(getObject(), END_YEARS.getAttributeName(), 0L);
        return endYears;
    }

    public Long getEndMonths() {
        if (endMonths == null)
            endMonths = sampleHandler.getLastSample(getObject(), END_MONTHS.getAttributeName(), 0L);
        return endMonths;
    }

    public Long getEndWeeks() {
        if (endWeeks == null)
            endWeeks = sampleHandler.getLastSample(getObject(), END_WEEKS.getAttributeName(), 0L);
        return endWeeks;
    }

    public Long getEndDays() {
        if (endDays == null)
            endDays = sampleHandler.getLastSample(getObject(), END_DAYS.getAttributeName(), 0L);
        return endDays;
    }

    public Long getEndHours() {
        if (endHours == null)
            endHours = sampleHandler.getLastSample(getObject(), END_HOURS.getAttributeName(), 0L);
        return endHours;
    }

    public Long getEndMinutes() {
        if (endMinutes == null)
            endMinutes = sampleHandler.getLastSample(getObject(), END_MINUTES.getAttributeName(), 0L);
        return endMinutes;
    }

    public JEVisObject getEndReferenceObject() {
        if (endReferenceObject == null) {
            try {
                JEVisAttribute att = getObject().getAttribute(END_REFERENCE_OBJECT.getAttributeName());
                TargetHelper th = new TargetHelper(getObject().getDataSource(), att);
                endReferenceObject = th.getObject();
            } catch (Exception e) {
            }
        }
        return endReferenceObject;
    }

    public Long getEndInterval() {
        if (endInterval == null)
            endInterval = sampleHandler.getLastSample(getObject(), END_INTERVAL.getAttributeName(), 0L);
        return endInterval;
    }

    public enum AttributeName {

        VISIBILITY("Visible"),
        START_REFERENCE_POINT("Start Reference Point"),
        START_YEARS("Start Years"),
        START_MONTHS("Start Months"),
        START_WEEKS("Start Weeks"),
        START_DAYS("Start Days"),
        START_HOURS("Start Hours"),
        START_MINUTES("Start Minutes"),
        START_REFERENCE_OBJECT("Start Reference Object"),
        START_INTERVAL("Start Interval"),
        END_REFERENCE_POINT("End Reference Point"),
        END_YEARS("End Years"),
        END_MONTHS("End Months"),
        END_WEEKS("End Weeks"),
        END_DAYS("End Days"),
        END_HOURS("End Hours"),
        END_MINUTES("End Minutes"),
        END_REFERENCE_OBJECT("End Reference Object"),
        END_INTERVAL("End Interval");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
