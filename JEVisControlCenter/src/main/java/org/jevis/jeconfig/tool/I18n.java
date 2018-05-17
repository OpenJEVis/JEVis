package org.jevis.jeconfig.tool;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class I18n {

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

    public void loadBundel(Locale local){
        this.locale=local;
        bundle = ResourceBundle.getBundle("JEVisCC", local);
    }

    /**
     * Formate the string unsing MessagePattern
     *
     * @see java.text.MessageFormat
     * @param key
     * @param arguments
     * @return
     */
    public String getString(String key, Object... arguments){
        try{
            return MessageFormat.format(bundle.getString(key),arguments);
        }catch (NullPointerException np){
            System.out.println("Missing translation ["+locale.getISO3Country()+"] Key: "+key);
            return "*"+key+"*";
        }
    }

    public String getString(String key){
        try{
            return bundle.getString(key);
        }catch (NullPointerException np){
            System.out.println("Missing translation ["+locale.getISO3Country()+"] Key: "+key);
            return "*"+key+"*";
        }
    }

}
