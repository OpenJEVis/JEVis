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
package org.jevis.wdparser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Artur Iablokov
 */
public class DWDWDParser {

    private static final String CHARSET = "UTF-8";
    private static final Logger log = LogManager.getLogger(DWDWDParser.class);
    private final List<Result> _results = new ArrayList<>();
    private DWDWDService _dwdService;
    private List<DWDWDDataPoint> _dataPoints;

    /**
     * sets Data Points to parser
     *
     * @param dataPoints list of data points
     */
    public void setDataPoints(List<DWDWDDataPoint> dataPoints) {
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

    /**
     * It manages the process - parses files(inputstream)
     * in accordance with the requirements of each data points.
     *
     * @param input
     */
    public void parse(List<InputStream> input) {
        if (input != null) {
            log.info("Inputstream elements in list: {0}", input.size());
        }

        for (InputStream in : input) {
            Document doc = null;
            try {
                doc = Jsoup.parse(in, CHARSET, "www.dwd.de");
            } catch (IOException ex) {
                log.fatal("Inputstream isnt parsable", ex);
            }
            _dwdService = new DWDWDService(doc);
            DateTime dateTime = _dwdService.getTableDateTime();

            _dataPoints.stream().forEach((dp) -> {
                setResult(dateTime, dp);
            });

        }
    }

    /**
     * sets result to result list for each data point
     *
     * @param dt timestamp
     * @param dp data point
     */
    private void setResult(DateTime dt, DWDWDDataPoint dp) {
        for (Map.Entry<String, String> entry : dp.getTargetsMap().entrySet()) {
            String target = entry.getKey();
            String valueString = _dwdService.getTableValue(entry.getValue(), dp.getCity());
            Double value = null;
            if (valueString != null) {


                if (valueString.matches(".*\\d+.*")) {
                    try {
                        value = new Double(valueString);
                    } catch (NullPointerException | NumberFormatException e) {
                        _results.add(new Result(target, valueString, dt));
                        log.debug("Result: value {0} from {1} added to id {2} " + dp.getCity(), new Object[]{valueString, dt, target});
                    }
                    _results.add(new Result(target, value, dt));
                    log.debug("Result: value {0} from {1} added to id {2} " + dp.getCity(), new Object[]{value, dt, target});
                } else {
                    _results.add(new Result(target, valueString, dt));
                    log.debug("Result: value {0} from {1} added to id {2} " + dp.getCity(), new Object[]{valueString, dt, target});
                }
            }
        }
    }
}
