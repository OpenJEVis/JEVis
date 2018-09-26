package org.jevis.application.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SaveResourceBundle {
    private static final Logger logger = LogManager.getLogger(SaveResourceBundle.class);
    private final ResourceBundle rbFallback;
    private final Locale locale;
    private ResourceBundle rb;

    public SaveResourceBundle(String bundel, Locale locale) {
        this.locale = locale;
        this.rbFallback = ResourceBundle.getBundle(bundel, Locale.US);
        try {
            this.rb = ResourceBundle.getBundle(bundel, locale);
        } catch (Exception np) {

        }


    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, Object... arguments) {
        ResourceBundle bundle = rbFallback;
        if (rb != null) {
            bundle = rb;
        }

        try {
            return MessageFormat.format(bundle.getString(key), arguments);
        } catch (NullPointerException np) {
            logger.info("Missing translation [" + locale.getISO3Country() + "] Key: " + key);
            try {
                return MessageFormat.format(rbFallback.getString(key), arguments);
            } catch (Exception ex) {
                return "*" + key + "*";
            }
        }
    }
}
