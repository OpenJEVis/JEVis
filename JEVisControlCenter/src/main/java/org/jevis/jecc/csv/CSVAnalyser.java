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
package org.jevis.jecc.csv;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An very very very simple check to see what are the most likely separator or
 * enclosed chars
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVAnalyser {

    public List<String> lines;
    private String separator = "";
    private String enclosed = "";

    public CSVAnalyser(File file) {

        readFile(file);
        countChars();
    }

    public String getSeparator() {
        return separator;
    }

    public String getEnclosed() {
        return enclosed;
    }

    private void countChars() {
        List<HashMap> lineCounter = new ArrayList<>();
        int startLine = 1;
        int endLine = lines.size();//the last line is maybe only a new line
        //we only the the last 5 to avoid the header
        if (lines.size() > 10) {
            startLine = lines.size() - 5;
        } else {
            //save fallback to take secound last, because maybe the last is only a new line
            startLine = lines.size() - 1;
        }

        int inLine = 1;
//        logger.info("startIN: " + startLine);
//        logger.info("endLine" + endLine);
//        logger.info("total: " + lines.size());

        for (String line : lines) {
//            System.out.print("check line: " + line);
            if (inLine < startLine || inLine > endLine) {
                inLine++;
                continue;
            }

            if (line.isEmpty()) {
//                logger.info("is emty");
                inLine++;
                continue;
            }
//            logger.info("<-");
            inLine++;
            HashMap<Character, Integer> map = new HashMap<Character, Integer>();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                Integer val = map.get(new Character(c));
                if (val != null) {
                    map.put(c, new Integer(val + 1));
                } else {
                    map.put(c, 1);
                }
            }
            lineCounter.add(map);
        }

        int simicolon = checkChar(lineCounter, ';');
        int comma = checkChar(lineCounter, ',');
        int tab = checkChar(lineCounter, '\t');
        int space = checkChar(lineCounter, ' ');

//        logger.info("simicolons: " + simicolon);
        if (simicolon == -2) {
            separator = ";";
        } else if (comma == -2) {
            separator = ",";
        } else if (tab == -2) {
            separator = "\t";
        } else if (space == -2) {
            separator = " ";
        }

        int bestMatch = 0;
        if (!separator.equals("")) {
            if (simicolon != -1 && simicolon > 0) {
                bestMatch = simicolon;
                separator = ";";
            } else if (comma != -1 && comma > 0 && comma > bestMatch) {
                bestMatch = comma;
                separator = ",";
            } else if (tab != -1 && tab > 0 && tab > bestMatch) {
                bestMatch = tab;
                separator = "\t";
            } else if (space != -1 && space > 0 && space > bestMatch) {
                bestMatch = space;
                separator = " ";
            }
        }

        int enclosed1 = checkChar(lineCounter, '"');
        int enclosed2 = checkChar(lineCounter, '\'');
        if (enclosed1 == -2) {
            enclosed = "\"";
        } else if (enclosed2 == -2) {
            enclosed = "'";
        }

    }

    private int checkChar(List<HashMap> lineCounter, Character toCount) {
//        logger.info("check for: (" + toCount + ")");
//        logger.info("lines: " + lineCounter.size());
        int max = 0;
        int averange = 0;
        int min = 1000;
        int lines = 0;
        int total = 0;
        for (HashMap<Character, Integer> map : lineCounter) {
            lines++;
            int count = 0;
            if (map.containsKey(toCount)) {
                count = map.get(toCount);
            }

            total += count;
            if (count > max) {
                max = count;
            }
            if (count < min) {
                min = count;
            }
        }
        if (total != 0 && lines != 0) {
            averange = total / lines;
        }

        int likly = 1;

        if (max == 0) {
            return -1;
        }

        if (min == max && max != 0) {
            return -2;
        }

        return averange;

    }

    private void readFile(File csvFile) {
        BufferedReader br = null;
        lines = new ArrayList<>();
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
    }

}
