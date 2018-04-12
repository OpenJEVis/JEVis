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
package org.jevis.commons.driver;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.jevis.api.JEVisObject;

/**
 *
 * @author broder
 */
public class DriverLoggerFactory {

    static String KEY = "process-id";

    public Logger getLogger(Logger logger, JEVisObject dataSource) {
        String loggerFileName = dataSource.getName().replace(" ", "_") + "_ID(" + dataSource.getID() + ").log";
        String loggerName = dataSource.getID().toString();
        MDC.put(KEY, "" + loggerName);

        FileAppender appender = new FileAppender();
        appender.setLayout(new PatternLayout("[%d{dd MMM yyyy HH:mm:ss}][%c{2}]: %-10m%n"));
        appender.setFile(loggerFileName);
        appender.setAppend(true);
        appender.setImmediateFlush(true);
        appender.activateOptions();
        appender.setName(loggerName);
        ThreadFilter threadFilter = new ThreadFilter(loggerName);
        appender.addFilter(threadFilter);
        logger.setAdditivity(false);    //<--do not use default root logger
        logger.addAppender(appender);
        return logger;
    }

    private class ThreadFilter extends Filter {

        private final String _value;

        public ThreadFilter(String key) {
            _value = key;
        }

        @Override
        public int decide(LoggingEvent le) {
            String mdc = (String) le.getMDC(KEY);
//        System.out.println("MDC: " + mdc);
//        System.out.println("key: " + _value);
            if (mdc.equals(_value)) {
                return Filter.ACCEPT;
            } else {
                return Filter.DENY;
            }
//        return Filter.ACCEPT;
        }
    }
}
