/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;

/**
 *
 * @author broder
 */
public abstract class SampleAdjuster implements SampleGenerator {

    protected SampleGenerator sampleGenerator;

    public SampleAdjuster(SampleGenerator sampleGenerator) {
        this.sampleGenerator = sampleGenerator;
    }
}
