package org.jevis.application.application;

import java.util.Locale;

/**
 * Global locale for the JEVis applications
 */
public class AppLocale {


    public static String BUNDEL_ID = "jeapplication";
    private static AppLocale appLocale;
    private Locale locale;


    public AppLocale() {
        locale = Locale.getDefault();
    }

    public static synchronized AppLocale getInstance() {
        if (appLocale == null)
            appLocale = new AppLocale();
        return appLocale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
