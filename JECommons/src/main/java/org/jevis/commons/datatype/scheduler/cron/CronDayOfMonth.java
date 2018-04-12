/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.datatype.scheduler.cron;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisException;

/**
 *
 * @author Artur Iablokov
 */
public class CronDayOfMonth {

    private static final String NODATA = "";
    private boolean allDays;
    private boolean lastDay;
    private List<Integer> days = new ArrayList<>();
    private String orig; 

    /**
     * parses day of month value 
     * @param d
     * @throws InvalidParameterException 
     */
    public void init(String d) throws InvalidParameterException{

        orig = d.replaceAll(" ", "");
        if (orig.equalsIgnoreCase(NODATA)) {
            //Logger.getLogger(ValueRange.class.getName()).log(Level.SEVERE, "Value Range parameter is wrong. Valid values examples: 2 / 10-12 / 4-6, 8-11 / 2, 4-6 / 3 -- all ");
            allDays = true;
        } else if (orig.equals("*")) {
            allDays = true;
        } else if (d.equals("LAST")) {
            lastDay = true;
        } else if (orig.contains(",")) {
            //1,3,5-6 or 5-6, 10-12 or 1,3,8
            String[] splStr = orig.split(",");

            for (String st : splStr) {
                machined(st);
            } //for
            // 5 or 4-100     
        } else {
            machined(orig);
        }
    }
    /**
     * gets day of month value
     * @return 
     */
    public String getDayOfMonthValue() {
        return orig;
    }
    
    /**
     * gets true if the "all days" is enabled, otherwise false. 
     * @return 
     */
    public boolean isAllDays() {
        return allDays;
    }
    
    /**
     * gets true if the "last day" is enabled, otherwise false. 
     * @return 
     */
    public boolean isLastDay() {
        return lastDay;
    }
    
    /**
     * gets true if day is enabled, otherwise false
     * @param day
     * @return 
     */
    public boolean isDayEnabled(int day) {
        return days.contains(day);
    }

//    public List<Integer> getDays() {
//        return days;
//    }

    private void machined(String st) throws InvalidParameterException {
        //TODO test output
        //System.out.println("Interval string is: " + st);
        if (isInterval(st)) {
            // check that str whit MINUS is valid
            String[] spStr = st.split("-");
            List<Integer> li = new ArrayList<>();

            //check that each string elem is int                    
            //make int array or list from string array
            if (addValidInteger(li, spStr)) {
                //check that elemnt 0 < than elem 1,  2 < 3 etc.
                // and add elements to selected items list
                addIntervalElements(li);
            }
        } else if (isValidDay(st)) {
            int tmp = Integer.parseInt(st);
            days.add(tmp);
        } else if (st.equals("LAST")){
            lastDay = true;
        }else {
            throw new InvalidParameterException("day of month value is wrong");
            //TODO logger interval is wrong -> STOP
        }
    }

    private boolean isInterval(String str) {
        return str.matches("(\\d+-\\d+){1}|([A-Z]{1,3}-[A-Z]{1,3}){1}");
    }

    private boolean addValidInteger(List<Integer> li, String[] spStr) {

        //check that each string elem is int                    
        //make int array or list from string array
        boolean add = false;

        for (int j = 0; j < spStr.length; j++) {
            String tmp = spStr[j];
            if (isValidDay(tmp)) {
                //TODO test output
                add = li.add(new Integer(tmp));
            } else { // parameter is not int -> break
                li = null; // 
                j = spStr.length; // no more
                add = false;
            }
        }

        return add;
    }

    private void addIntervalElements(List<Integer> li) {
        //check that elemnt 0 < than elem 1,  2 < 3 etc.
        // and add elements to _interval
        for (int j = 0; j < li.size(); j = j + 2) {
            int start = li.get(j);
            int end = li.get(j + 1);
            if (start <= end) {
                while (start <= end) {
                    days.add(start);
                    start++;
                }
            } else {
                days = null;
                j = li.size();
            }
        }
    }

    private static boolean isValidDay(String str) {

        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;

        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }

        int v = Integer.parseInt(str);
        return !(v < 0 || v > 31);
    }

    
}
