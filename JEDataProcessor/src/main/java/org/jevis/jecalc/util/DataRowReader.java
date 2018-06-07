/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.util;

import org.jevis.jecalc.data.CleanDataAttributeOffline;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtuelSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class DataRowReader {

    public List<JEVisSample> getSamplesFromFile(String pathToInputFile) {
        List<JEVisSample> jevisSamples = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToInputFile))) {
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                String[] delimLine = line.split(";");
                String dateAsString = delimLine[0];
                DateTime currentdate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(dateAsString);

                String valueAsString = delimLine[1];
                Double value = Double.parseDouble(valueAsString);

                JEVisSample sample = new VirtuelSample(currentdate, value);
                jevisSamples.add(sample);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CleanDataAttributeOffline.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CleanDataAttributeOffline.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jevisSamples;
    }
}
