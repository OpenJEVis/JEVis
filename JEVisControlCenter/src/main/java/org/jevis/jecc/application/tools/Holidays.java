package org.jevis.jecc.application.tools;

import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameter;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.control.CalendarRow;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Holidays {
    private static final Logger logger = LogManager.getLogger(WorkDays.class);
    private static final HashMap<JEVisObject, HolidayManager> siteHolidayManagerMap = new HashMap<>();
    private static final HashMap<JEVisObject, HolidayManager> customHolidayManagerMap = new HashMap<>();
    private static JEVisDataSource ds;
    private static JEVisClass siteClass;
    private static HolidayManager defaultHolidayManager;
    private static String stateCode = "";

    public Holidays() {
    }

    public static void setDataSource(JEVisDataSource ds) {
        Holidays.ds = ds;
        if (ds != null) {
            try {
                siteClass = ds.getJEVisClass("Building");
            } catch (Exception e) {
                logger.fatal("Could not get JEVisClass for Building");
            }
        }

        Locale locale = I18n.getInstance().getLocale();
        try {
            ManagerParameter managerParameter = ManagerParameters.create(locale);
            defaultHolidayManager = HolidayManager.getInstance(managerParameter);
        } catch (Exception e) {
            defaultHolidayManager = HolidayManager.getInstance(ManagerParameters.create(I18n.getInstance().getDefaultBundle().getLocale()));
            logger.error("Could not identify {}, creating fallback default holiday manager {}", locale, I18n.getInstance().getDefaultBundle().getLocale());
        }
        if (ds != null) {
            try {
                List<JEVisObject> sites = ds.getObjects(siteClass, false);
                for (JEVisObject site : sites) {
                    JEVisAttribute holidaysAtt = site.getAttribute("Holidays");
                    if (holidaysAtt.hasSample()) {
                        try {
                            CalendarRow calendarRow = new CalendarRow(holidaysAtt.getLatestSample().getValueAsString());
                            siteHolidayManagerMap.put(site, HolidayManager.getInstance(ManagerParameters.create(calendarRow.getCountryCode())));
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
                            customHolidayManagerMap.put(site, HolidayManager.getInstance(ManagerParameters.create(url)));
                        } catch (Exception e) {
                            logger.error("Could not create custom holiday manager");
                        }
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

    public static HolidayManager getSiteHolidayManager(JEVisObject site) {
        if (site != null) {
            return siteHolidayManagerMap.get(site);
        } else return defaultHolidayManager;
    }

    public static HolidayManager getCustomHolidayManager(JEVisObject site) {
        if (site != null) {
            return customHolidayManagerMap.get(site);
        } else return defaultHolidayManager;
    }
}