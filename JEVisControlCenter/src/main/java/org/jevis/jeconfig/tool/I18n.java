package org.jevis.jeconfig.tool;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
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

    public String getString(String key){
        try{
            return bundle.getString(key);
        }catch (NullPointerException np){
            return "*"+key+"*";
        }
    }

}
