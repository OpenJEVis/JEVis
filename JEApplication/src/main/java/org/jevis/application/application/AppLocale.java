package org.jevis.application.application;

import java.util.Locale;

/**
 * Global locale for the JEVis applications
 */
public class AppLocale {


    private static AppLocale appLocale;
    private Locale locale;
    public static String BUNDEL_ID= "jeapplication";


    public static synchronized AppLocale getInstance() {
        if (appLocale == null)
            appLocale = new AppLocale();
        return appLocale;
    }

    public AppLocale() {
        locale=Locale.getDefault();
    }

    public void setLocale(Locale locale){
        this.locale=locale;
    }

    public Locale getLocale(){
        return this.locale;
    }

}
