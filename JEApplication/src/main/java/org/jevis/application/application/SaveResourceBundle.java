package org.jevis.application.application;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SaveResourceBundle {

    private final ResourceBundle rbFallback;
    private ResourceBundle rb;
    private final Locale locale;

    public SaveResourceBundle(String bundel, Locale locale) {
        this.locale = locale;
        this.rbFallback = ResourceBundle.getBundle(bundel, Locale.US);
        try {
            this.rb = ResourceBundle.getBundle(bundel, locale);
        }catch (Exception np) {

        }


    }

    public String getString(String key){
        return getString(key,null);
    }

    public String getString(String key, Object... arguments){
        ResourceBundle bundle = rbFallback;
        if(rb!=null) {
            bundle=rb;
        }

        try {
            return MessageFormat.format(bundle.getString(key), arguments);
        }catch (NullPointerException np){
            System.out.println("Missing translation ["+locale.getISO3Country()+"] Key: "+key);
            try{
                return MessageFormat.format(rbFallback.getString(key), arguments);
            }catch (Exception ex){
                return "*"+key+"*";
            }
        }
    }
}
