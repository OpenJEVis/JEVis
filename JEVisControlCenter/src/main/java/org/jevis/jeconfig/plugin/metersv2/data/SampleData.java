package org.jevis.jeconfig.plugin.metersv2.data;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;

import java.util.Optional;

public class SampleData {
    private JEVisAttribute jeVisAttribute;
    private Optional<JEVisSample> optionalJEVisSample;

    public SampleData(JEVisAttribute jeVisAttribute) {
        this.jeVisAttribute = jeVisAttribute;
        this.optionalJEVisSample = Optional.ofNullable(jeVisAttribute.getLatestSample());
    }

    public JEVisAttribute getJeVisAttribute() {
        return jeVisAttribute;
    }

    public Optional<JEVisSample> getOptionalJEVisSample() {
        return optionalJEVisSample;
    }
}
