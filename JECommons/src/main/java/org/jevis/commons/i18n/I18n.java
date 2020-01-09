package org.jevis.commons.i18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Locale.GERMANY;
import static java.util.Locale.UK;


/**
 * Prototype for the JEVis localisation
 */
public class I18n {

    private static final Logger logger = LogManager.getLogger(I18n.class);
    private static I18n i18n;
    private Locale locale = Locale.getDefault();
    private PropertyResourceBundle bundle;
    private ResourceBundle defaultBundle;
    private Map<Locale, PropertyResourceBundle> allBundles = new HashMap<>();
    private List<Locale> availableLang = new ArrayList<>();

    public static synchronized I18n getInstance() {
        if (i18n == null)
            i18n = new I18n();
        return i18n;
    }

    public I18n() {
        this.defaultBundle = ResourceBundle.getBundle("JEVis", Locale.ENGLISH);
        availableLang.add(UK);
        availableLang.add(GERMANY);
        availableLang.add(Locale.forLanguageTag("ru"));
        availableLang.add(Locale.forLanguageTag("uk"));
        availableLang.add(Locale.forLanguageTag("th"));

        for (Locale locale : availableLang) {
            try {
                String s = "JEVis_" + locale.getLanguage() + ".properties";

                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(s);
                if (resourceAsStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
                    Reader reader = new BufferedReader(inputStreamReader);
                    this.allBundles.put(locale, new PropertyResourceBundle(reader));
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("Unsupported encoding exception. Error while reading resource file.", e);
            } catch (IOException e) {
                logger.error("IO exception. Error while reading resource file.", e);
            }
        }
    }

    public List<Locale> getAvailableLang() {
        return availableLang;
    }

    public ResourceBundle getBundle() {
        return this.bundle;
    }

    public void selectBundle(Locale local) {
        this.locale = local;
        this.bundle = allBundles.get(local);
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

    public Map<Locale, PropertyResourceBundle> getAllBundles() {
        return allBundles;
    }
}

