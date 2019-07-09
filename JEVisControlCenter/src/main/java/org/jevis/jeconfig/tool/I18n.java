package org.jevis.jeconfig.tool;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Prototype for the JEVis localisation
 */
public class I18n {

    private static final Logger logger = LogManager.getLogger(I18n.class);
    private static I18n i18n;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private ResourceBundle bundle;
    private ResourceBundle defaultBundle;


    public static synchronized I18n getInstance() {
        if (i18n == null)
            i18n = new I18n();
        return i18n;
    }

    public I18n() {
        defaultBundle = ResourceBundle.getBundle("JEVisCC", Locale.ENGLISH);
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void loadBundle(Locale local) {
        this.locale = local;
        bundle = ResourceBundle.getBundle("JEVisCC", local);
    }

    public Locale getLocale() {
        return this.locale;
    }


    /**
     * Format the string using MessagePattern
     *
     * @param key
     * @param arguments
     * @return
     * @see java.text.MessageFormat
     */
    public String getString(String key, Object... arguments) {
        try {
            return String.format(bundle.getString(key), arguments);
        } catch (NullPointerException | java.util.MissingResourceException np) {
            logger.info("Missing translation [" + locale.getISO3Country() + "] Key: " + key);
            try {
                return String.format(defaultBundle.getString(key), arguments);
            } catch (NullPointerException | java.util.MissingResourceException np1) {
                logger.info("Missing translation [" + locale.getISO3Country() + "] Key: " + key);
                return "*" + key + "*";
            }
        }
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (NullPointerException | java.util.MissingResourceException np) {
            logger.info("Missing translation [" + locale.getISO3Country() + "] Key: " + key);
            try {
                return defaultBundle.getString(key);
            } catch (NullPointerException | java.util.MissingResourceException np2) {
                logger.info("Missing translation [" + locale.getISO3Country() + "] Key: " + key);
                return "*" + key + "*";
            }
        }
    }
}

