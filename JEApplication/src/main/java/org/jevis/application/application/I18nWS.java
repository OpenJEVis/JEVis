package org.jevis.application.application;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.commons.ws.json.JsonI18nClass;
import org.jevis.commons.ws.json.JsonI18nType;
import org.jevis.jeapi.ws.JEVisDataSourceWS;

import java.util.List;
import java.util.Locale;


/**
 * Prototype for the JEVis localisation.
 * <p>
 * JEApplication will now have the JEAPI-WS as an dependency. If the I18n is part of the API we can remove this dependency.
 */
public class I18nWS {

    private static I18nWS i18n;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private JEVisDataSourceWS ws;
    private List<JsonI18nClass> i18nfiles;


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
     * @param ws
     * @todo replace this prototype with an the JEDataSource interface
     */
    public void setDataSource(JEVisDataSourceWS ws) {
        this.ws = ws;
    }

    public String getClassName(String className) {
        JsonI18nClass json = getJsonClass(className);

        if (json.getNames().containsKey(locale.getLanguage())) {
            return json.getNames().get(locale.getLanguage());
        } else {
            System.out.println("Class name not found: " + className);
            return className;
        }
    }

    public String getTypeName(String jevisClass, String typeName) {
        JsonI18nClass json = getJsonClass(jevisClass);

        for (JsonI18nType type : json.getTypes()) {
            if (type.getType().equalsIgnoreCase(typeName)) {
                if (type.getNames().containsKey(locale.getLanguage())) {
                    return type.getNames().get(locale.getLanguage());
                } else {
                    System.out.println("Type name not found: " + jevisClass + " | " + typeName);
                    return typeName;
                }
            }
        }

        return typeName;
    }

    public String getAttributeName(JEVisAttribute attribute) throws JEVisException {
        return getTypeName(attribute.getType());
    }

    public String getTypeName(JEVisType type) throws JEVisException {
        return getTypeName(type.getJEVisClassName(), type.getName());
    }

    public String getClassName(JEVisClass jclass) throws JEVisException {
        return getClassName(jclass.getName());
    }


    public String getClassDescription(String className) {
        JsonI18nClass json = getJsonClass(className);

        if (json.getDescriptions().containsKey(locale.getLanguage())) {
            return json.getDescriptions().get(locale.getLanguage());
        } else {
            System.out.println("Class description not found: " + className);
            return "";
        }
    }

    public String getTypeDescription(String jevisClass, String typeName) {

        JsonI18nClass json = getJsonClass(jevisClass);

        for (JsonI18nType type : json.getTypes()) {
            if (type.getType().equalsIgnoreCase(typeName)) {
                if (type.getDescriptions().containsKey(locale.getLanguage())) {
                    return type.getDescriptions().get(locale.getLanguage());
                } else {
                    System.out.println("Type description not found: " + jevisClass + " | " + typeName);
                    return "";
                }
            }
        }

        return "";
    }


    private JsonI18nClass getJsonClass(String classname) {
        for (JsonI18nClass jclass : i18nfiles) {
            if (jclass.getJevisclass().equals(classname)) {
                return jclass;
            }
        }
        JsonI18nClass fallback = new JsonI18nClass();
        fallback.setJevisclass(classname);
        return fallback;
    }


}
