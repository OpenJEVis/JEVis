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
    private final ResourceBundle defaultBundle;
    private final Map<Locale, PropertyResourceBundle> allBundles = new HashMap<>();
    private final List<Locale> availableLang = new ArrayList<>();

    public static synchronized I18n getInstance() {
        if (i18n == null)
            i18n = new I18n();
        return i18n;
    }

    public I18n() {
        this.defaultBundle = ResourceBundle.getBundle("JEVis", Locale.ENGLISH);
        availableLang.add(UK);
        availableLang.add(GERMANY);
        availableLang.add(new Locale("ru", "RU"));
        availableLang.add(new Locale("uk", "UA"));
        availableLang.add(new Locale("th", "TH"));
        availableLang.add(new Locale("ar", "DZ"));

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

        //set en as default
        selectBundle(Locale.getDefault());
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
    public Map<String,String> getTranslationMap(String key){
        Map<String, String> translation = new HashMap<>();
        for (Locale language:availableLang) {
            translation.put(language.getLanguage(), retrieveString(key, language,allBundles.get(language)));
        }
        return translation;

    }

    public String getString(String key) {
        return retrieveString(key, this.locale,this.bundle);
    }
    private String retrieveString(String key, Locale language, PropertyResourceBundle resourceBundle){
        try {
            return resourceBundle.getString(key);
        } catch (NullPointerException | java.util.MissingResourceException np) {
            logger.info("Missing translation [" + language.getLanguage() + "] Key: " + key + "");
            try {
                return this.defaultBundle.getString(key);
            } catch (NullPointerException | java.util.MissingResourceException np2) {
                logger.info("Missing translation [" + language.getLanguage() + "] Key: " + key);
                return "*" + key + "*";
            }
        }
    }

    public Map<Locale, PropertyResourceBundle> getAllBundles() {
        return allBundles;
    }

    public ResourceBundle getDefaultBundle() {
        return defaultBundle;
    }
}

