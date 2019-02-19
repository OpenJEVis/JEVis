//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.cron;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class CronDayOfMonth {
    private static final String NODATA = "";
    private boolean allDays;
    private boolean lastDay;
    private List<Integer> days = new ArrayList();
    private String orig;

    public CronDayOfMonth() {
    }

    private static boolean isValidDay(String str) {
        int length = str.length();
        if (length == 0) {
            return false;
        } else {
            for (int i = 0; i < length; ++i) {
                char c = str.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }

            int v = Integer.parseInt(str);
            return v >= 0 && v <= 31;
        }
    }

    public void init(String d) throws InvalidParameterException {
        this.orig = d.replaceAll(" ", "");
        if (this.orig.equalsIgnoreCase("")) {
            this.allDays = true;
        } else if (this.orig.equals("*")) {
            this.allDays = true;
        } else if (d.equals("LAST")) {
            this.lastDay = true;
        } else if (this.orig.contains(",")) {
            String[] splStr = this.orig.split(",");
            String[] var3 = splStr;
            int var4 = splStr.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String st = var3[var5];
                this.machined(st);
            }
        } else {
            this.machined(this.orig);
        }

    }

    public String getDayOfMonthValue() {
        return this.orig;
    }

    public boolean isAllDays() {
        return this.allDays;
    }

    public boolean isLastDay() {
        return this.lastDay;
    }

    public boolean isDayEnabled(int day) {
        return this.days.contains(day);
    }

    private void machined(String st) throws InvalidParameterException {
        if (this.isInterval(st)) {
            String[] spStr = st.split("-");
            List<Integer> li = new ArrayList();
            if (this.addValidInteger(li, spStr)) {
                this.addIntervalElements(li);
            }
        } else if (isValidDay(st)) {
            int tmp = Integer.parseInt(st);
            this.days.add(tmp);
        } else {
            if (!st.equals("LAST")) {
                throw new InvalidParameterException("day of month value is wrong");
            }

            this.lastDay = true;
        }

    }

    private boolean isInterval(String str) {
        return str.matches("(\\d+-\\d+){1}|([A-Z]{1,3}-[A-Z]{1,3}){1}");
    }

    private boolean addValidInteger(List<Integer> li, String[] spStr) {
        boolean add = false;

        for (int j = 0; j < spStr.length; ++j) {
            String tmp = spStr[j];
            if (isValidDay(tmp)) {
                add = li.add(new Integer(tmp));
            } else {
                li = null;
                j = spStr.length;
                add = false;
            }
        }

        return add;
    }

    private void addIntervalElements(List<Integer> li) {
        for (int j = 0; j < li.size(); j += 2) {
            int start = li.get(j);
            int end = li.get(j + 1);
            if (start <= end) {
                while (start <= end) {
                    this.days.add(start);
                    ++start;
                }
            } else {
                this.days = null;
                j = li.size();
            }
        }

    }
}
