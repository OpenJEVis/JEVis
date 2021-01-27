package org.jevis.commons.report;

import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;

public class JEVisFileWithSample {
    private final JEVisSample jeVisSample;
    private final JEVisFile pdfFile;
    private final JEVisFile xlsxFile;

    public JEVisFileWithSample(JEVisSample jeVisSample, JEVisFile pdfFile, JEVisFile xlsxFile) {
        this.jeVisSample = jeVisSample;
        this.pdfFile = pdfFile;
        this.xlsxFile = xlsxFile;
    }

    public JEVisFileWithSample(JEVisSample jeVisSample, JEVisFile pdfFile) {
        this(jeVisSample, pdfFile, null);
    }

    public JEVisSample getJeVisSample() {
        return jeVisSample;
    }

    public JEVisFile getPdfFile() {
        return pdfFile;
    }

    public JEVisFile getXlsxFile() {
        return xlsxFile;
    }
}
