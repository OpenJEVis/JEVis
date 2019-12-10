package org.jevis.jeconfig.application.tools;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.application.control.CalendarRow;
import org.jevis.jeconfig.tool.I18n;

import java.io.File;
import java.net.URL;
import java.util.List;

public class Holidays {
    private static final Logger logger = LogManager.getLogger(WorkDays.class);
    private static JEVisDataSource ds;
    private static JEVisClass siteClass;
    private static HolidayManager defaultHolidayManager;
    private static HolidayManager siteHolidayManager;
    private static HolidayManager customHolidayManager;
    private static String stateCode = "";

    public Holidays() {
    }

    public static void setDataSource(JEVisDataSource ds) {
        Holidays.ds = ds;
        if (ds != null) {
            try {
                siteClass = ds.getJEVisClass("Building");
            } catch (JEVisException e) {
                logger.fatal("Could not get JEVisClass for Building");
            }
        }

        defaultHolidayManager = HolidayManager.getInstance(ManagerParameters.create(I18n.getInstance().getLocale()));
        if (ds != null) {
            try {
                List<JEVisObject> sites = ds.getObjects(siteClass, false);
                for (JEVisObject site : sites) {
                    JEVisAttribute holidaysAtt = site.getAttribute("Holidays");
                    if (holidaysAtt.hasSample()) {
                        try {
                            CalendarRow calendarRow = new CalendarRow(holidaysAtt.getLatestSample().getValueAsString());
                            siteHolidayManager = HolidayManager.getInstance(ManagerParameters.create(calendarRow.getCountryCode()));
                            stateCode = calendarRow.getStateCode();
                        } catch (Exception e) {
                            logger.error("Could not create site holiday manager");
                        }
                    }

                    JEVisAttribute customHolidaysAtt = site.getAttribute("Custom Holidays");
                    if (customHolidaysAtt.hasSample()) {
                        try {
                            JEVisFile jeVisFile = customHolidaysAtt.getLatestSample().getValueAsFile();
                            String property = System.getProperty("java.io.tmpdir");
                            File file = new File(property + "/" + jeVisFile.getFilename());
                            file.deleteOnExit();
                            jeVisFile.saveToFile(file);

                            URL url = new URL("file:" + file);
                            customHolidayManager = HolidayManager.getInstance(ManagerParameters.create(url));
                        } catch (Exception e) {
                            logger.error("Could not create custom holiday manager");
                        }
                    }

                    if (siteHolidayManager != null || customHolidayManager != null) {
                        break;
                    }
                }
            } catch (Exception e) {
                logger.fatal("Could not create any custom holiday manager");
            }
        }
    }

    public static String getStateCode() {
        return stateCode;
    }

    public static HolidayManager getDefaultHolidayManager() {
        return defaultHolidayManager;
    }

    public static HolidayManager getSiteHolidayManager() {
        return siteHolidayManager;
    }

    public static HolidayManager getCustomHolidayManager() {
        return customHolidayManager;
    }
}
