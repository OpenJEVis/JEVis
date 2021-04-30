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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;

/**
 * @author Artur Iablokov
 */
public class DWDWDService {

    public static final String DATE = "Date";
    public static final String TIME = "Time";
    private static final String PATTERN = "dd.MM.yyyy HH:mm";
    private static final String WHITESPACE = " ";
    private static final String NOTEXIST = "---";
    private static final Logger log = LogManager.getLogger(DWDWDService.class);
    private final Document _doc;

    public DWDWDService(Document d) {
        _doc = d;
    }

    /**
     * processes the string received from the table parser
     *
     * @param city   city name / table row
     * @param option data option / table column
     * @return specific value
     */
    public String getTableValue(String option, String city) {

        String answer = WDParserHelper.parseTable(_doc, city, option);
        if (StringUtils.containsIgnoreCase(answer, NOTEXIST)) {
            return null;
        }

        return answer;
    }

    /**
     * processes the string received from the header parser
     *
     * @return datetime value
     */
    public DateTime getTableDateTime() {
        DateTime datetime = null;

        String dt = WDParserHelper.parseHeader(_doc, DATE);
        dt = dt + WHITESPACE + WDParserHelper.parseHeader(_doc, TIME);
        datetime = buildDateTime(dt);

        return datetime;
    }

    /**
     * build DateTime Object from a String according to pattern
     *
     * @param dt String - date and time
     * @return datetime
     */
    private DateTime buildDateTime(String dt) {
        DateTime datetime = null;
        org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern(PATTERN);
        try {
            datetime = formatter.parseDateTime(dt);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            log.fatal("DateTime isnt parseble");
        }

        return datetime;
    }
}
