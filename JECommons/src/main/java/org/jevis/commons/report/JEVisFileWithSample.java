package org.jevis.commons.report;

import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;

public class JEVisFileWithSample {
    private JEVisSample jeVisSample;
    private JEVisFile jeVisFile;

    public JEVisFileWithSample(JEVisSample jeVisSample, JEVisFile jeVisFile) {
        this.jeVisSample = jeVisSample;
        this.jeVisFile = jeVisFile;
    }

    public JEVisSample getJeVisSample() {
        return jeVisSample;
    }

    public void setJeVisSample(JEVisSample jeVisSample) {
        this.jeVisSample = jeVisSample;
    }

    public JEVisFile getJeVisFile() {
        return jeVisFile;
    }

    public void setJeVisFile(JEVisFile jeVisFile) {
        this.jeVisFile = jeVisFile;
    }
}
