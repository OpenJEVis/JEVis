package org.jevis.jeconfig.tool;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Prototype for the JEVis localisation
 */
public class I18n {

    private static final Logger logger = LogManager.getLogger(I18n.class);
    private static I18n i18n;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private PropertyResourceBundle bundle;
    private ResourceBundle defaultBundle;
    private Map<Locale, ResourceBundle> allBundles = new HashMap<>();

    public static synchronized I18n getInstance() {
        if (i18n == null)
            i18n = new I18n();
        return i18n;
    }

    public I18n() {
        this.defaultBundle = ResourceBundle.getBundle("JEVisCC", Locale.ENGLISH);
    }

    public ResourceBundle getBundle() {
        return this.bundle;
    }

    public void loadAndSelectBundles(List<Locale> availableLang, Locale local) {
        this.locale = local;
//        bundle = ResourceBundle.getBundle("JEVisCC", local);

        for (Locale locale : availableLang) {
            try {
                String s = "JEVisCC_" + locale.getLanguage() + ".properties";

                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(s);
                if (resourceAsStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
                    Reader reader = new BufferedReader(inputStreamReader);
                    if (locale.equals(local)) {
                        this.bundle = new PropertyResourceBundle(reader);
                        this.allBundles.put(locale, this.bundle);
                    } else {
                        this.allBundles.put(locale, new PropertyResourceBundle(reader));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("Unsupported encoding exception. Error while reading resource file.", e);
            } catch (IOException e) {
                logger.error("IO exception. Error while reading resource file.", e);
            }
        }
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
            return String.format(this.bundle.getString(key), arguments);
        } catch (NullPointerException | java.util.MissingResourceException np) {
            logger.info("Missing translation [" + this.locale.getLanguage() + "] Key: " + key);
            try {
                return String.format(this.defaultBundle.getString(key), arguments);
            } catch (NullPointerException | java.util.MissingResourceException np1) {
                logger.info("Missing translation [" + this.locale.getLanguage() + "] Key: " + key);
                return "*" + key + "*";
            }
        }
    }

    public String getString(String key) {
        try {
            return this.bundle.getString(key);
        } catch (NullPointerException | java.util.MissingResourceException np) {
            logger.info("Missing translation [" + this.locale.getLanguage() + "] Key: " + key + "");
            try {
                return this.defaultBundle.getString(key);
            } catch (NullPointerException | java.util.MissingResourceException np2) {
                logger.info("Missing translation [" + this.locale.getLanguage() + "] Key: " + key);
                return "*" + key + "*";
            }
        }
    }

    public Map<Locale, ResourceBundle> getAllBundles() {
        return allBundles;
    }
}

