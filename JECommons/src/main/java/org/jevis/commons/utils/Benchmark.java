/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.utils;

import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Benchmark {

    private DateTime _start;
    private DateTime _lastPrint;

    public Benchmark() {
        _start = DateTime.now();
        _lastPrint = DateTime.now();
    }

    public void reset() {
        _start = DateTime.now();
        _lastPrint = DateTime.now();
    }

    public long timeSinceStart() {
        return DateTime.now().getMillis() - _start.getMillis();
    }

    public void printBechmark(String text) {
        String unit = "s";
        long milis = DateTime.now().getMillis() - _start.getMillis();
        long secound = (milis / 1000);
        if (secound == 0 && milis > 0) {
            unit = "ms";
        } else {
            milis = secound;
        }

//        System.out.printf("[  |%4d|%n  %s  ] for - %s", milis, unit, text);
        System.out.printf("[%4d %s] %s\n", milis, unit, text);
//        System.out.println("[" + (milis) + unit + "] for - " + text);
    }

    public void printBenchmarkDetail(String text) {

        String unit = "s";
        String unitPrint = "s";
        long milisSStart = DateTime.now().getMillis() - _start.getMillis();
        long milisSPrint = DateTime.now().getMillis() - _lastPrint.getMillis();

        long secound = (milisSStart / 1000);
        if (secound == 0 && milisSStart > 0) {
            unit = "ms";
        } else {
            milisSStart = secound;
        }

        long secoundPrint = (milisSPrint / 1000);
        if (secoundPrint == 0 && milisSPrint > 0) {
            unitPrint = "ms";
        } else {
            milisSPrint = secoundPrint;
        }

        System.out.printf("[%4d %s | %4d %s] %s\n", milisSPrint, unitPrint, milisSStart, unit, text);
        _lastPrint = DateTime.now();

    }

}
