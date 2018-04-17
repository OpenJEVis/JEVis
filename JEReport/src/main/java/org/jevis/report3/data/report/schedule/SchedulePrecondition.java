package org.jevis.report3.data.report.schedule;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.List;
import javax.inject.Inject;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.data.report.Precondition;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author broder
 */
public class SchedulePrecondition implements Precondition {

    private final SampleHandler sampleHandler;
    private final JEVisIntervalParser intervalParser;

    @Inject
    public SchedulePrecondition(SampleHandler sampleHandler, JEVisIntervalParser intervalParser) {
        this.sampleHandler = sampleHandler;
        this.intervalParser = intervalParser;
    }

    @Override
    public boolean isPreconditionReached(JEVisObject reportObject) {
        List<JEVisSample> allSamples = sampleHandler.getAllSamples(reportObject, "Interval");
        intervalParser.parseSamples(allSamples);
        Interval interval = JEVisIntervalParser.getInterval();

        if (interval.getEnd() != null) {
            return interval.getEnd().isBefore(new DateTime());
        }
        return false;
    }
}
