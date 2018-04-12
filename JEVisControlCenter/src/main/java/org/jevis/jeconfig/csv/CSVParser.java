/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVParser {

    private int _maxColumnCount = 0;
    private boolean _isAllwaysSameColumnCount = true;
    private List<CSVLine> rows;

    public CSVParser(File file, String enclosed, String seperator, int header) {
        List<String> lines = readfile(file);
        List<CSVLine> csvLines = parseLines(lines, enclosed, seperator, header);
        rows = csvLines;
    }

    public List<CSVLine> getRows() {
        return rows;
    }

    private List<CSVLine> parseLines(List<String> list, String enclosed, String seperator, int header) {
        List<CSVLine> cslines = new ArrayList<>();
        int count = 0;
//        System.out.println("Split column by: " + seperator + " text by: " + enclosed);
        for (String line : list) {
            count++;
            if (count < header) {
                continue;
            }

            if (line.isEmpty()) {
                continue;
            }

            CSVLine csvline = new CSVLine(line, enclosed, seperator, count);
            cslines.add(csvline);

            if (_maxColumnCount != 0 && csvline.getColoumCount() != 0) {
                if (_maxColumnCount != csvline.getColoumCount()) {
                    _isAllwaysSameColumnCount = false;
                }
            }
            if (_maxColumnCount < csvline.getColoumCount()) {
                _maxColumnCount = csvline.getColoumCount();
            }
        }
        return cslines;
    }

    public boolean isColoumCountAllwaysSame() {
        return _isAllwaysSameColumnCount;
    }

    public int getColumnCount() {
        return _maxColumnCount;
    }

    private List<String> readfile(File csvFile) {
        BufferedReader br = null;
        List<String> lines = new ArrayList<>();
        try {

            String line = "";
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

}
