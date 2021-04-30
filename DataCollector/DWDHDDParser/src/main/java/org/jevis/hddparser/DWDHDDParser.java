/*
 * Copyright (C) 2016 AI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jevis.hddparser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.driver.Converter;
import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.inputHandler.GenericConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Gerrit Schutz
 */
public class DWDHDDParser {

    private static final String CHARSET = "UTF-8";
    private static final Logger log = LogManager.getLogger(DWDHDDParser.class);
    private final List<Result> _results = new ArrayList<>();
    private List<DWDHDDDataPoint> _dataPoints;

    /**
     * sets Data Points to parser
     *
     * @param dataPoints list of data points
     */
    public void setDataPoints(List<DWDHDDDataPoint> dataPoints) {
        _dataPoints = dataPoints;
    }

    /**
     * get result
     *
     * @return results
     */
    public List<Result> getResult() {
        return _results;
    }

    public void parse(List<InputStream> input) {
        if (input != null) {
            log.info("Inputstream elements in list: {}", input.size());
        }

        Converter converter = new GenericConverter();

        for (InputStream inputStream : input) {

            converter.convertInput(inputStream, Charset.forName(CHARSET));
            String[] stringArrayInput = (String[]) converter.getConvertedInput(String[].class);

            String evalDateString = stringArrayInput[2];
            String[] dateLine = evalDateString.split(" ", -1);

            DateTime start = DateTime.parse(dateLine[1], DateTimeFormat.forPattern("dd-MMM-yyyy").withLocale(Locale.ENGLISH));
            String[] dateHeaderLine = stringArrayInput[3].split(";", -1);

            for (int row = 4; row < stringArrayInput.length; row++) {
                String[] line = stringArrayInput[row].split(";", -1);
                String stationName = line[3].trim();

                for (int col = 4; col < line.length; col++) {
                    String dateString = dateHeaderLine[col].trim();
                    int day = Integer.parseInt(dateString);
                    String valueString = line[col].trim();
                    double value = Double.parseDouble(valueString);
                    DateTime dt = new DateTime(start.getYear(), start.getMonthOfYear(), day, 0, 0, 0);
                    for (DWDHDDDataPoint dp : _dataPoints) {
                        if (dp.getStation().equals(stationName)) {
                            _results.add(new Result(dp.getTarget(), value, dt));
                            break;
                        }
                    }
                }
            }
        }
    }

}
