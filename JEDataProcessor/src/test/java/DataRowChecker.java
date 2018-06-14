
import org.jevis.jecalc.workflow.ProcessManager;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanDataAttributeOffline;
import org.jevis.jecalc.util.DataRowReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.junit.Assert;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author broder
 */
public class DataRowChecker {

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
                Logger.getLogger(DataRowChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//        try {
//            Files.delete(Paths.get(pathToRealOutput));
//        } catch (IOException ex) {
//            Logger.getLogger(AlignmentTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void run(String pathToInputFile, String pathToCleanConfigFile, String pathToRealOutput) {
        CleanDataAttribute calcAttribute = new CleanDataAttributeOffline(pathToInputFile, pathToCleanConfigFile, pathToRealOutput);
        ProcessManager processManager = new ProcessManager(calcAttribute);
        processManager.start();
    }

    private List<JEVisSample> getSamplesFromFile(String pathToOutput) {
        DataRowReader reader = new DataRowReader();
        return reader.getSamplesFromFile(pathToOutput);
    }

}
