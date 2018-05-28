package org.jevis.application.application;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.jevis.commons.ws.json.JsonI18n;
import org.jevis.jeapi.ws.JEVisDataSourceWS;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;


/**
 * Prototype for the JEVis localisation.
 *
 * JEApplication will now have the JEAPI-WS as an dependency. If the I18n is part of the API we can remove this dependency.
 *
 */
public class I18nWS {

    private static I18nWS i18n;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private JEVisDataSourceWS ws;
    private List<JsonI18n> i18nfiles;

    public static String CLASS_DESCRIPTION="cdescr";
    public static String CLASS_NAME="cname";
    public static String TYPE_DESCRIPTION="tdescr";
    public static String TYPE_NAME="tname";



    public static synchronized I18nWS getInstance() {
        if (i18n == null)
            i18n = new I18nWS();
        return i18n;
    }

    public I18nWS() {

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


}
