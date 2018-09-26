/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class DataRowReader {
    private static final Logger logger = LogManager.getLogger(DataRowReader.class);

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

                JEVisSample sample = new VirtualSample(currentdate, value);
                jevisSamples.add(sample);
                line = br.readLine();
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
        return jevisSamples;
    }
}
