package org.jevis.jeconfig.tool;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.jevis.commons.ws.json.JsonI18n;
import org.jevis.jeapi.ws.JEVisDataSourceWS;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Prototype for the JEVis localisation
 */
public class I18n {

    private static I18n i18n;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private ResourceBundle bundle;
    private JEVisDataSourceWS ws;
    private List<JsonI18n> i18nfiles;

    public static String CLASS_DESCRIPTION="cdescr";
    public static String CLASS_NAME="cname";
    public static String TYPE_DESCRIPTION="tdescr";
    public static String TYPE_NAME="tname";



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
     * Add an webservice as an additional translation source.
     *
     * @todo replace this prototype with an the JEDataSource interface
     * @param ws
     */
    public void setDataSource(JEVisDataSourceWS ws){
        this.ws=ws;
    }


    public String getString(String type, String key,Object... arguments ){
        if(ws==null){
            return "*"+key+"*";
        }
        if(i18nfiles==null){
            i18nfiles=ws.getTranslation(locale);
        }


        for(JsonI18n translation:i18nfiles){
            if(translation.getType().equalsIgnoreCase(type) && translation.getKey().equalsIgnoreCase(key)){
                if(arguments==null){
                    return translation.getValue();
                }else{
                    return MessageFormat.format(translation.getValue(),arguments);
                }
            }
        }
        return  "*"+key+"*";
    }

    /**
     * Format the string using MessagePattern
     *
     * @see java.text.MessageFormat
     * @param key
     * @param arguments
     * @return
     */
    public String getString(String key, Object... arguments){
        try{
            return MessageFormat.format(bundle.getString(key),arguments);
        }catch (NullPointerException| java.util.MissingResourceException np){
            System.out.println("Missing translation ["+locale.getISO3Country()+"] Key: "+key);
            return "*"+key+"*";
        }
    }

    public String getString(String key){
        try{
            return bundle.getString(key);
        }catch (NullPointerException| java.util.MissingResourceException np){
            System.out.println("Missing translation ["+locale.getISO3Country()+"] Key: "+key);
            return "*"+key+"*";
        }
    }

}
