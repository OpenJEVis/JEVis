package org.jevis.jeconfig.plugin.object.extension.calculation;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

public class Stats {

    public String name = "";
    public DateTime fromDate;
    public DateTime untilDate;
    public int sampleCount;

    public JEVisObject object;

    public Stats(JEVisObject obj, String name, DateTime fromDate, DateTime untilDate, int sampleCount) {
        this.object = obj;
        this.name = name;
        this.fromDate = fromDate;
        this.untilDate = untilDate;
        this.sampleCount = sampleCount;
    }

    public String getName() {
        return name;
    }

    public JEVisObject getObject() {
        return object;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public DateTime getUntilDate() {
        return untilDate;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public String getIdentifier() {
        try {
            return object.getAttribute("Identifier").getLatestSample().getValueAsString();
        } catch (Exception ex) {
        }
        return "";
    }

    public String getTargetName() {
        try {
            JEVisAttribute att = object.getAttribute("Input Data");
            TargetHelper targetHelper = new TargetHelper(att.getDataSource(), att);
            if (targetHelper.getObject().get(0).getJEVisClassName().equals("Clean Data")) {
                return targetHelper.getObject().get(0).getParents().get(0).getName();
            } else {
                return targetHelper.getObject().get(0).getName();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public String getTargetPeriod() {
        try {
            JEVisAttribute att = object.getAttribute("Input Data");
            TargetHelper targetHelper = new TargetHelper(att.getDataSource(), att);
            for (JEVisObject jeVisObject : targetHelper.getObject().get(0).getChildren()) {
                try {
                    if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                        return jeVisObject.getAttribute("Period").getLatestSample().getValueAsString();
                    }
                } catch (Exception ex) {
                }
            }

        } catch (Exception ex) {
        }
        return "";
    }

    public String getInputDataType() {
        try {
            JEVisAttribute att = object.getAttribute("Input Data Type");
            return att.getLatestSample().getValueAsString();

        } catch (Exception ex) {
        }
        return "";
    }
}
