/*
 * Copyright (C) 2016 Artur Iablokov
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Artur Iablokov
 */
public class WDParserHelper {

    private static final String TABLETAG = "table";
    private static final String THEADTAG = "thead";
    private static final String TBODYTAG = "tbody";
    private static final String THTAG = "th";
    private static final String TDTAG = "td";
    private static final String TRTAG = "tr";
    private static final String H4TAG = "h4";
    private static final int FIRSTCOLUMN = 0;
    //regex to parse date and time
    private static final Map<String, String> REGEX;
    private static final Logger log = LogManager.getLogger(JEVisDWDWDParser.class);

    static {
        HashMap<String, String> tMap = new HashMap<String, String>();
        tMap.put("Date", "(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d(?:,)");
        tMap.put("Time", "(0[0-9]|[1][0-9]|2[0-3])[- :]([0-5][0-9])");
        REGEX = Collections.unmodifiableMap(tMap);
    }

    /**
     * parses the specific DWD table with the weather data
     *
     * @param doc        html document
     * @param city       city name / table row
     * @param columnName data option / table column
     * @return value specific value
     */
    public static String parseTable(Document doc, String city, String columnName) {
        String value = null;
        Element table = doc.select(TABLETAG).first();
        Element thead = table.select(THEADTAG).first();
        Element tbody = table.select(TBODYTAG).first();

        Integer columnNameIndex = null;

        Elements headRows = thead.select(TRTAG);
        for (Element row : headRows) {
            Elements cols = row.select(THTAG);
            for (int i = 0; i < cols.size(); i++) {
                Element col = cols.get(i);
                if (columnName.equals(col.text()) || columnName.equals(getAlternativeColumnName(col.text()))) {
                    columnNameIndex = i;
                    break;
                }
            }
        }

        if (columnNameIndex != null) {

            Elements bodyRows = tbody.select(TRTAG);
            for (Element row : bodyRows) { //first row is the col names so skip it.
                Elements cols = row.select(TDTAG);

                if (cols.get(FIRSTCOLUMN).text().equals(city) | StringUtils.containsIgnoreCase(cols.get(0).text(), city)) {
                    value = cols.get(columnNameIndex).text();
                } else if (checkAlternativeCityNames(city, cols)) {
                    value = cols.get(columnNameIndex).text();
                }
            }
        }
        return value;
    }

    private static String getAlternativeColumnName(String text) {
        return DWDWDDataPoint.ALTERNATIVE_WEATHEROPTION_NAMES.get(text);
    }

    /**
     * parses the header of DWD table
     *
     * @param doc      html document
     * @param regexkey key to regex map
     * @return value (Date or Time)
     */
    public static String parseHeader(Document doc, String regexkey) {
        String value = null;
        String header = doc.select(H4TAG).text();
        String regex = REGEX.get(regexkey);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(header);
        if (m.find()) {
            value = m.group(0);
            if (regexkey.equalsIgnoreCase(DWDWDService.DATE)) {
                value = value.substring(0, 10);
            }
            if (value == null) {
                log.fatal("{0} value is null", regexkey);
            }
        } else {
            log.fatal(regexkey + " value not found");
        }
        return value;
    }

    private static boolean checkAlternativeCityNames(String city, Elements cols) {
        boolean answer = cols.get(FIRSTCOLUMN).text().equals(DWDWDDataPoint.ALTERNATIVE_CITY_NAME.get(city));
        return answer;
    }

}
