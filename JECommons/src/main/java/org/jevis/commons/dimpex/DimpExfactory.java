package org.jevis.commons.dimpex;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.ws.json.JsonFactory;

import java.util.ArrayList;
import java.util.UUID;

public class DimpExfactory {

    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(DimpExfactory.class);

    public enum SampleMode {
        CONFIGURATION, ALL, NONE
    }

    public static DimpexSample buildSample(JEVisSample sample) throws JEVisException {
        DimpexSample dsample = new DimpexSample();
        dsample.setTimestamp(JsonFactory.sampleDTF.print(sample.getTimestamp()));
        dsample.setValue(sample.getValueAsString());
        dsample.setNote(sample.getNote());


        return dsample;
    }

    public static DimpexAttribute buildAttribute(JEVisAttribute att, SampleMode smode) throws JEVisException {
        DimpexAttribute dAtt = new DimpexAttribute();
        dAtt.setName(att.getName());
        dAtt.setDisplayRate(att.getDisplaySampleRate().toString());
        dAtt.setInputRate(att.getDisplaySampleRate().toString());
        dAtt.setInputUnit(JsonFactory.buildUnit(att.getInputUnit()));
        dAtt.setDisplayUnit(JsonFactory.buildUnit(att.getDisplayUnit()));

        if (smode == SampleMode.ALL) {
            dAtt.setSampleID(UUID.randomUUID().toString());
            //TODO: create an file with samples with the UUID. File as Zip archiv?
        }
        if (smode == SampleMode.CONFIGURATION) {
            try {
                dAtt.getSamples().add(JsonFactory.buildSample(att.getLatestSample(), att.getPrimitiveType()));
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return dAtt;
    }

    public static DimpexObject build(JEVisObject obj, boolean includeChildren, SampleMode smode) throws JEVisException {
        DimpexObject newObj = new DimpexObject();
        newObj.setName(obj.getName());
        newObj.setJclass(obj.getJEVisClassName());
        newObj.setUid(UUID.randomUUID().toString());
        newObj.setChildren(new ArrayList<>());
        newObj.setPublic(obj.isPublic());

        if (includeChildren) {
            try {
                for (JEVisObject child : obj.getChildren()) {
                    newObj.getChildren().add(build(child, includeChildren, smode));
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        if (smode != SampleMode.NONE) {
            try {
                for (JEVisAttribute att : obj.getAttributes()) {
                    newObj.getAttributes().add(buildAttribute(att, smode));
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }


        return newObj;
    }
}
