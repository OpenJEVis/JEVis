/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Benchmark {

    private static final Logger logger = LogManager.getLogger(Benchmark.class);
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
        long seconds = (milis / 1000);
        if (seconds == 0 && milis > 0) {
            unit = "ms";
        } else {
            milis = seconds;
        }

        logger.info("[{} {}] {}", milis, unit, text);
    }

    public void printBenchmarkDetail(String text) {

        String unit = "s";
        String unitPrint = "s";
        long milisSStart = DateTime.now().getMillis() - _start.getMillis();
        long milisSPrint = DateTime.now().getMillis() - _lastPrint.getMillis();

        long seconds = (milisSStart / 1000);
        if (seconds == 0 && milisSStart > 0) {
            unit = "ms";
        } else {
            milisSStart = seconds;
        }

        long secondPrint = (milisSPrint / 1000);
        if (secondPrint == 0 && milisSPrint > 0) {
            unitPrint = "ms";
        } else {
            milisSPrint = secondPrint;
        }

        logger.info("[{} {} | {} {}] {}", milisSPrint, unitPrint, milisSStart, unit, text);
        _lastPrint = DateTime.now();

    }

}
