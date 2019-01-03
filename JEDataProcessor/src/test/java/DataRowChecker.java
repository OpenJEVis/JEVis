import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jedataprocessor.data.CleanDataObject;
import org.jevis.jedataprocessor.data.CleanDataObjectOffline;
import org.jevis.jedataprocessor.util.DataRowReader;
import org.joda.time.DateTime;
import org.junit.Assert;

import java.util.List;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author broder
 */
public class DataRowChecker {
    private static final Logger logger = LogManager.getLogger(DataRowChecker.class);

    public void validate(String pathToInputFile, String pathToCleanConfigFile, String pathToRealOutput, String pathToCorrectOutput) {
        this.run(pathToInputFile, pathToCleanConfigFile, pathToRealOutput);

        List<JEVisSample> realSamples = getSamplesFromFile(pathToRealOutput);
        List<JEVisSample> correctSamples = getSamplesFromFile(pathToCorrectOutput);

        Assert.assertEquals("same amount of samples", correctSamples.size(), realSamples.size());
        for (int i = 0; i < realSamples.size(); i++) {
            JEVisSample realSample = realSamples.get(i);
            JEVisSample correctSample = correctSamples.get(i);

            try {
                Double realValue = realSample.getValueAsDouble();
                Double correctValue = correctSample.getValueAsDouble();
                Assert.assertEquals("same value", correctValue, realValue);

                DateTime realDateTime = realSample.getTimestamp();
                DateTime correctDateTime = correctSample.getTimestamp();
                Assert.assertEquals("same timestamp", realDateTime, correctDateTime);
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }

//        try {
//            Files.delete(Paths.get(pathToRealOutput));
//        } catch (IOException ex) {
//            Logger.getLogger(AlignmentTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void run(String pathToInputFile, String pathToCleanConfigFile, String pathToRealOutput) {
        CleanDataObject calcAttribute = new CleanDataObjectOffline(pathToInputFile, pathToCleanConfigFile, pathToRealOutput);
        //ProcessManager processManager = new ProcessManager(calcAttribute, new ObjectHandler());
        try {
            //  processManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<JEVisSample> getSamplesFromFile(String pathToOutput) {
        DataRowReader reader = new DataRowReader();
        return reader.getSamplesFromFile(pathToOutput);
    }

}
