package org.jevis.jeconfig.tool;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


/**
 * Prototype for the JEVis localisation
 */
public class I18n {

    private static final Logger logger = LogManager.getLogger(I18n.class);
    private static I18n i18n;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private ResourceBundle bundle;


    public static synchronized I18n getInstance() {
        if (i18n == null)
            i18n = new I18n();
        return i18n;
    }

    public I18n() {

    }

    public ResourceBundle getBundle() {
        return this.bundle;
    }

    public void loadBundel(Locale local) {
        this.locale = local;
        this.bundle = ResourceBundle.getBundle("JEVisCC", local);
        try {
            System.out.println("file to read: " + "JEVisCC_" + local.getLanguage() + ".properties");
//            InputStream test = getClass().getResourceAsStream("JEVisCC_de.properties");
//            test.read();
//            URL url = ResourceBundle.class.getResource("1472562626_unknown.png");
//            System.out.println(url.getFile());
//            Class cls = Class.forName("ResourceBundle");
//
//            // returns the ClassLoader object associated with this Class
//            ClassLoader cLoader = cls.getClassLoader();
            InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("JEVisCC_" + local.getLanguage() + ".properties");
            Reader reader = new InputStreamReader(stream, "UTF-8");
            this.bundle = new PropertyResourceBundle(reader);


//            Reader reader = new BufferedReader(new InputStreamReader(
//                    getClass().getResourceAsStream("JEVisCC_" + local.getLanguage() + ".properties"), "utf-8"));
//            PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);
//            this.bundle = propertyResourceBundle;
        } catch (Exception e) {
            e.printStackTrace();
        }
        ;

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
            logger.info("Missing translation [" + this.locale.getISO3Country() + "] Key: " + key);
            return "*" + key + "*";
        }
    }

    public String getString(String key) {
        try {
            return this.bundle.getString(key);
        } catch (NullPointerException | java.util.MissingResourceException np) {
            logger.info("Missing translation [" + this.locale.getISO3Country() + "] Key: " + key);
            return "*" + key + "*";
        }
    }

}
