package org.jevis.jeconfig.plugin.graph.data;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.object.plugin.TargetHelper;

import static org.jevis.jeconfig.plugin.graph.data.CustomPeriodObject.AttributeName.*;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class CustomPeriodObject {
    private JEVisObject object;
    private JEVisObject periodDirectoryObject;
    private SampleHandler sampleHandler;
    private ObjectHandler objectHandler;

    private String startReferencePoint;
    private Long startMinusYears;
    private Long startMinusMonths;
    private Long startMinusDays;
    private Long startMinusHours;
    private Long startMinusMinutes;
    private JEVisObject startReferenceObject;

    private String endReferencePoint;
    private Long endMinusYears;
    private Long endMinusMonths;
    private Long endMinusDays;
    private Long endMinuasHours;
    private Long endMinusMinutes;
    private JEVisObject endReferenceObject;

    public CustomPeriodObject(JEVisObject periodObject, ObjectHandler objectHandler) {
        this.object = periodObject;
        this.objectHandler = objectHandler;
        this.periodDirectoryObject = objectHandler.getFirstParent(periodObject);
        this.sampleHandler = new SampleHandler();
    }

    public JEVisObject getObject() {
        return object;
    }

    public String getStartReferencePoint() {
        if (startReferencePoint == null)
            startReferencePoint = sampleHandler.getLastSample(getObject(), START_REFERENCE_POINT.getAttributeName(), null);
        return startReferencePoint;
    }

    public Long getStartMinusYears() {
        if (startMinusYears == null)
            startMinusYears = sampleHandler.getLastSample(getObject(), START_MINUS_YEARS.getAttributeName(), 0L);
        return startMinusYears;
    }

    public Long getStartMinusMonths() {
        if (startMinusMonths == null)
            startMinusMonths = sampleHandler.getLastSample(getObject(), START_MINUS_MONTHS.getAttributeName(), 0L);
        return startMinusMonths;
    }

    public Long getStartMinusDays() {
        if (startMinusDays == null)
            startMinusDays = sampleHandler.getLastSample(getObject(), START_MINUS_DAYS.getAttributeName(), 0L);
        return startMinusDays;
    }

    public Long getStartMinusHours() {
        if (startMinusHours == null)
            startMinusHours = sampleHandler.getLastSample(getObject(), START_MINUS_HOURS.getAttributeName(), 0L);
        return startMinusHours;
    }

    public Long getStartMinusMinutes() {
        if (startMinusMinutes == null)
            startMinusMinutes = sampleHandler.getLastSample(getObject(), START_MINUS_MINUTES.getAttributeName(), 0L);
        return startMinusMinutes;
    }

    public JEVisObject getStartReferenceObject() {
        if (startReferenceObject == null) {
            try {
                JEVisAttribute attStartReferenceObject = sampleHandler.getLastSample(getObject(), START_REFERENCE_OBJECT.getAttributeName(), null);
                if (attStartReferenceObject != null) {
                    TargetHelper th = new TargetHelper(getObject().getDataSource(), attStartReferenceObject);
                    startReferenceObject = th.getObject();
                }

            } catch (Exception e) {
            }
        }
        return startReferenceObject;
    }

    public String getEndReferencePoint() {
        if (endReferencePoint == null)
            endReferencePoint = sampleHandler.getLastSample(getObject(), END_REFERENCE_POINT.getAttributeName(), null);
        return endReferencePoint;
    }

    public Long getEndMinusYears() {
        if (endMinusYears == null)
            endMinusYears = sampleHandler.getLastSample(getObject(), END_MINUS_YEARS.getAttributeName(), 0L);
        return endMinusYears;
    }

    public Long getEndMinusMonths() {
        if (endMinusMonths == null)
            endMinusMonths = sampleHandler.getLastSample(getObject(), END_MINUS_MONTHS.getAttributeName(), 0L);
        return endMinusMonths;
    }

    public Long getEndMinusDays() {
        if (endMinusDays == null)
            endMinusDays = sampleHandler.getLastSample(getObject(), END_MINUS_DAYS.getAttributeName(), 0L);
        return endMinusDays;
    }

    public Long getEndMinuasHours() {
        if (endMinuasHours == null)
            endMinuasHours = sampleHandler.getLastSample(getObject(), END_MINUS_HOURS.getAttributeName(), 0L);
        return endMinuasHours;
    }

    public Long getEndMinusMinutes() {
        if (endMinusMinutes == null)
            endMinusMinutes = sampleHandler.getLastSample(getObject(), END_MINUS_MINUTES.getAttributeName(), 0L);
        return endMinusMinutes;
    }

    public JEVisObject getEndReferenceObject() {
        if (endReferenceObject == null) {
            try {
                JEVisAttribute attStartReferenceObject = sampleHandler.getLastSample(getObject(), END_REFERENCE_OBJECT.getAttributeName(), null);
                if (attStartReferenceObject != null) {
                    TargetHelper th = new TargetHelper(getObject().getDataSource(), attStartReferenceObject);
                    endReferenceObject = th.getObject();
                }

            } catch (Exception e) {
            }
        }
        return endReferenceObject;
    }

    public enum AttributeName {

        START_REFERENCE_POINT("Start Reference Point"),
        START_MINUS_YEARS("Start Minus Years"),
        START_MINUS_MONTHS("Start Minus Months"),
        START_MINUS_DAYS("Start Minus Days"),
        START_MINUS_HOURS("Start Minus Hours"),
        START_MINUS_MINUTES("Start Minus Minutes"),
        START_REFERENCE_OBJECT("Start Reference Object"),
        END_REFERENCE_POINT("End Reference Point"),
        END_MINUS_YEARS("End Minus Years"),
        END_MINUS_MONTHS("End Minus Months"),
        END_MINUS_DAYS("End Minus Days"),
        END_MINUS_HOURS("End Minus Hours"),
        END_MINUS_MINUTES("End Minus Minutes"),
        END_REFERENCE_OBJECT("End Reference Object");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
