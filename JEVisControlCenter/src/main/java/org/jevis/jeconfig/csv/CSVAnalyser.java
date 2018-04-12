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
import java.util.HashMap;
import java.util.List;

/**
 * An very very very simple check to see what are the most likely seperator or
 * enclosed chars
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVAnalyser {

    private String seperator = "";
    private String enclosed = "";
    public List<String> lines;

    public CSVAnalyser(File file) {

        readfile(file);
        countChars();
    }

    public String getSeperator() {
        return seperator;
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
//        System.out.println("startIN: " + startLine);
//        System.out.println("endLine" + endLine);
//        System.out.println("total: " + lines.size());

        for (String line : lines) {
//            System.out.print("check line: " + line);
            if (inLine < startLine || inLine > endLine) {
                inLine++;
                continue;
            }

            if (line.isEmpty()) {
//                System.out.println("is emty");
                inLine++;
                continue;
            }
//            System.out.println("<-");
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

        int simicolon = cheackChar(lineCounter, ';');
        int comma = cheackChar(lineCounter, ',');
        int tab = cheackChar(lineCounter, '\t');
        int space = cheackChar(lineCounter, ' ');

//        System.out.println("simicolons: " + simicolon);
        if (simicolon == -2) {
            seperator = ";";
        } else if (comma == -2) {
            seperator = ",";
        } else if (tab == -2) {
            seperator = "\t";
        } else if (space == -2) {
            seperator = " ";
        }

        int bestMatch = 0;
        if (!seperator.equals("")) {
            if (simicolon != -1 && simicolon > 0) {
                bestMatch = simicolon;
                seperator = ";";
            } else if (comma != -1 && comma > 0 && comma > bestMatch) {
                bestMatch = comma;
                seperator = ",";
            } else if (tab != -1 && tab > 0 && tab > bestMatch) {
                bestMatch = tab;
                seperator = "\t";
            } else if (space != -1 && space > 0 && space > bestMatch) {
                bestMatch = space;
                seperator = " ";
            }
        }

        int enclosed1 = cheackChar(lineCounter, '"');
        int enclosed2 = cheackChar(lineCounter, '\'');
        if (enclosed1 == -2) {
            enclosed = "\"";
        } else if (enclosed2 == -2) {
            enclosed = "'";
        }

    }

    private int cheackChar(List<HashMap> lineCounter, Character toCount) {
//        System.out.println("check for: (" + toCount + ")");
//        System.out.println("lines: " + lineCounter.size());
        int max = 0;
        int averange = 0;
        int min = 1000;
        int lines = 0;
        int total = 0;
        for (HashMap<Character, Integer> map : lineCounter) {
            lines++;
            int count = 0;
            if (map.containsKey(toCount)) {
                count = map.get(toCount);;
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

    private void readfile(File csvFile) {
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
