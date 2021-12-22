package org.jevis.jestatus;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;

public class ServiceRuntime {
    private String result = "";

    public ServiceRuntime(JEVisAttribute statusAttribute) {
        List<JEVisSample> allSamples = statusAttribute.getAllSamples();
        List<Runtime> runtimeList = new ArrayList<>();

        JEVisSample lastSample = null;
        for (JEVisSample sample : allSamples) {
            if (lastSample == null) {
                lastSample = sample;
                continue;
            }

            try {
                long currentStatus = sample.getValueAsLong();
                long lastStatus = lastSample.getValueAsLong();
                if (currentStatus == 1 && lastStatus == 2) {
                    runtimeList.add(new Runtime(lastSample.getTimestamp(), sample.getTimestamp()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastSample = sample;
        }

        if (runtimeList.isEmpty()) return;

        long averageRuntime = 0L;

        for (Runtime runtime : runtimeList) {
            averageRuntime += runtime.getRuntime();
        }
        averageRuntime = averageRuntime / runtimeList.size();
        long seconds = averageRuntime / 1000;
        double minutes = seconds / 60d;
        double hours = minutes / 60;
        double days = hours / 24;

        Period p = new Period(averageRuntime);
        result = p.toString(PeriodFormat.wordBased());
    }

    public String getResult() {
        return result;
    }

    static class Runtime {
        private Long runtime = 0L;

        public Runtime(DateTime start, DateTime end) {
            runtime = end.getMillis() - start.getMillis();
        }

        public Long getRuntime() {
            return runtime;
        }
    }
}
