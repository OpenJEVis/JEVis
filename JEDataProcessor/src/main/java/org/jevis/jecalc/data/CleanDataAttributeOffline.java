/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.data;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jecalc.gap.Gap.GapStrategy;
import org.jevis.jecalc.util.DataRowReader;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author broder
 */
public class CleanDataAttributeOffline implements CleanDataAttribute {

    private final String pathToOutput;
    private DateTime date;
    private int periodOffset;
    private Period periodAlignment;
    private boolean valueIsQuantity;
    private List<JEVisSample> isConversionToDifferential;
    private List<JEVisSample> multiplier;
    private Double offset;
    private GapStrategy gapStrategy;

    private List<JEVisSample> rawSample = new ArrayList<>();
    private boolean periodAligned;
    private List<JsonGapFillingConfig> jsonGapFillingConfig;
    private List<JsonLimitsConfig> jsonLimitsConfig;
    private Boolean limitsEnabled;

    private JEVisObject object;
    private static final Logger logger = LoggerFactory.getLogger(CleanDataAttributeOffline.class);

    public CleanDataAttributeOffline(String pathToInputFile, String pathToCleanConfigFile, String pathToOutput) {
        initProperties(pathToCleanConfigFile);

        rawSample = initInputDataRow(pathToInputFile);

        try {
            DateTime firstTimestamp = rawSample.get(0).getTimestamp();
            int year = firstTimestamp.getYear();
            date = new DateTime(year, 1, 1, 0, 0);
        } catch (JEVisException ex) {
            logger.error(null, ex);
        }

        this.pathToOutput = pathToOutput;
    }

    @Override
    public DateTime getFirstDate() {
        return date;
    }

    @Override
    public DateTime getMaxEndDate() {
        if (!rawSample.isEmpty()) {
            try {
                return rawSample.get(rawSample.size() - 1).getTimestamp().plusSeconds(periodOffset).plus(periodAlignment);
            } catch (JEVisException ex) {
                logger.error(null, ex);
            }
        }
        return null;
    }

    @Override
    public Period getPeriodAlignment() {
        return periodAlignment;
    }

    @Override
    public List<JEVisSample> getRawSamples() {
        return rawSample;
    }

    @Override
    public List<JEVisSample> getConversionDifferential() {
        return isConversionToDifferential;
    }

    @Override
    public Boolean getValueIsQuantity() {
        return valueIsQuantity;
    }

    private void initProperties(String pathToCleanConfigFile) {
        Properties prop = new Properties();

        try {
            //load a properties file
            prop.load(new FileInputStream(pathToCleanConfigFile));

            String periodOffsetAsString = prop.getProperty("Period_Offset");
            periodOffset = Integer.parseInt(periodOffsetAsString);

            String periodAlignmentAsString = prop.getProperty("Period");
            periodAlignment = ISOPeriodFormat.standard().parsePeriod(periodAlignmentAsString);

            String valueIsQuantityAsString = prop.getProperty("Value_is_a_Quantity");
            valueIsQuantity = valueIsQuantityAsString.equals("true");

            String isConversionToDifferentialAsString = prop.getProperty("Conversion_to_Differential");
            //isConversionToDifferential = isConversionToDifferentialAsString.equals("true");

            String multiplierString = prop.getProperty("Multiplier");
            //multiplier = Double.parseDouble(multiplierString);

            String offsetString = prop.getProperty("Offset");
            offset = Double.parseDouble(offsetString);

            String gapModeString = prop.getProperty("Gap_Filling");
            gapStrategy = new GapStrategy(gapModeString.toUpperCase());

            String periodAlignedString = prop.getProperty("Period_Aligned");
            periodAligned = periodAlignedString.equals("true");


        } catch (IOException ex) {
            logger.error(null, ex);
        }
    }

    private List<JEVisSample> initInputDataRow(String pathToInputFile) {
        DataRowReader reader = new DataRowReader();
        return reader.getSamplesFromFile(pathToInputFile);
    }

    public String getPathToOutput() {
        return pathToOutput;
    }

    @Override
    public Integer getPeriodOffset() {
        return periodOffset;
    }

    @Override
    public Double getLastDiffValue() {
        return null;
    }

    @Override
    public List<JEVisSample> getMultiplier() {
        return multiplier;
    }

    @Override
    public Double getOffset() {
        return offset;
    }

    @Override
    public Double getLastCleanValue() {
        return null;
    }

    @Override
    public GapStrategy getGapFillingMode() {
        return gapStrategy;
    }

    @Override
    public Boolean getIsPeriodAligned() {
        return periodAligned;
    }

    @Override
    public Boolean getEnabled() {
        return true;
    }

    @Override
    public List<JsonGapFillingConfig> getGapFillingConfig() {
        return jsonGapFillingConfig;
    }

    @Override
    public List<JsonLimitsConfig> getLimitsConfig() {
        return jsonLimitsConfig;
    }

    @Override
    public Boolean getLimitsEnabled() {
        return limitsEnabled;
    }

    @Override
    public String getName() {
        return "offline mode";
    }

    @Override
    public JEVisObject getObject() {
        return object;
    }
}
