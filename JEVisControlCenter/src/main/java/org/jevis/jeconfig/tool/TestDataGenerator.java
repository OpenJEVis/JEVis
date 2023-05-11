package org.jevis.jeconfig.tool;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal tool to generate simple test data for testing.
 */
public class TestDataGenerator {

    /**
     * Copy all double values to the same attribute but with an year offset
     *
     * @param jeVisAttribute
     * @param fromYear
     * @param toYear
     */
    public static void copyYearToNext(JEVisAttribute jeVisAttribute, int fromYear, int toYear) {
        try {
            List<JEVisSample> sampleList = jeVisAttribute.getSamples(
                    new DateTime(fromYear, 1, 1, 1, 1, 1),
                    new DateTime(fromYear, 12, 31, 1, 1, 1));
            List<JEVisSample> newSamples = new ArrayList<>();
            sampleList.forEach(jeVisSample -> {
                try {
                    newSamples.add(jeVisAttribute.buildSample(jeVisSample.getTimestamp().plusYears(toYear - fromYear), jeVisSample.getValueAsDouble()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            jeVisAttribute.addSamples(newSamples);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
