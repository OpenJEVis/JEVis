/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.csv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVParser {

    private static final Logger logger = LogManager.getLogger(CSVParser.class);
    private File file;
    private int _maxColumnCount = 0;
//    private boolean _isAlwaysSameColumnCount = true;
    private List<CSVLine> rows;
    private Charset charset;
    private String enclosed;
    private String separator;
    private int header;

    public CSVParser(File file, String enclosed, String separator, int header, Charset charset) {
        this.file = file;
        this.charset = charset;
        this.enclosed = enclosed;
        this.separator = separator;
        this.header = header;

        rows = parse();
    }

    public List<CSVLine> parse() {
        List<String> lines = readFile(file, charset);
        rows = parseLines(lines, enclosed, separator, header);
        return rows;
    }

    public List<CSVLine> getRows() {
        return rows;
    }

    private List<CSVLine> parseLines(List<String> list, String enclosed, String seperator, int header) {
        List<CSVLine> csvLines = new ArrayList<>();
        int count = -1;
        for (String line : list) {
            count++;
            if (count < header) {
                continue;
            }

            if (line.isEmpty()) {
                continue;
            }

            CSVLine csvline = new CSVLine(line, enclosed, seperator, count);
            csvLines.add(csvline);

            if (_maxColumnCount < csvline.getColumnCount()) {
                _maxColumnCount = csvline.getColumnCount();
            }
        }
        return csvLines;
    }


    public int getColumnCount() {
        return _maxColumnCount;
    }

    private List<String> readFile(File csvFile, Charset charset) {
        logger.debug("File: " + csvFile);
        BufferedReader br = null;
        List<String> lines = new ArrayList<>();
        try {

            String line = "";
            br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), charset));
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

    private List<String> readfile2(File csvFile) {
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


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getEnclosed() {
        return enclosed;
    }

    public void setEnclosed(String enclosed) {
        this.enclosed = enclosed;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }
}
