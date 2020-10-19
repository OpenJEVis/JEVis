package org.jevis.jeconfig.application.application;

import org.apache.commons.beanutils.locale.LocaleBeanUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.commons.ws.json.Json18nEnum;
import org.jevis.commons.ws.json.JsonI18nClass;
import org.jevis.commons.ws.json.JsonI18nType;
import org.jevis.jeapi.ws.JEVisDataSourceWS;

import java.util.*;


/**
 * Prototype for the JEVis localisation.
 * <p>
 * JEApplication will now have the JEAPI-WS as an dependency. If the I18n is part of the API we can remove this dependency.
 */
public class I18nWS {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(I18nWS.class);
    private static I18nWS i18n;
    private static JEVisDataSourceWS ds;
    private Locale locale = LocaleBeanUtils.getDefaultLocale();
    private static List<JsonI18nClass> i18nfiles;


    public I18nWS() {

    }

    public static void setLocale(Locale locale) {
        getInstance().locale = locale;
    }

    public static synchronized I18nWS getInstance() {
        if (i18n == null)
            i18n = new I18nWS();
        return i18n;
    }

    /**
     * Add an webservice as an additional translation source.
     *
     * @param ws
     * @todo replace this prototype with an the JEDataSource interface
     */
    public static void setDataSource(JEVisDataSourceWS ws) {
        ds = ws;
        i18nfiles = ws.getTranslation();
    }

    public String getClassName(String className) {
        JsonI18nClass json = getJsonClass(className);

        if (json.getNames().containsKey(locale.getLanguage())) {
            return json.getNames().get(locale.getLanguage());
        } else {
            logger.warn("Class name not found: {}", className);
            return className;
        }
    }

    public Map<String, Json18nEnum> getEnum(String className, String typeName) {
        try {
            JsonI18nClass json = getJsonClass(className);
            for (JsonI18nType type : json.getTypes()) {
                if (type.getEnums() != null && type.getType().equalsIgnoreCase(typeName)
                        && !type.getEnums().isEmpty()) {
                    return type.getEnums();
                }
            }

            return new HashMap<>();
        } catch (Exception ex) {
            logger.error("Error while loading Enum for type: {}:{} -> {}", className, typeName, ex.getMessage(), ex);
            return new HashMap<>();
        }
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public String getTypeName(String jevisClass, String typeName) {
        try {
            JsonI18nClass json = getJsonClass(jevisClass);

            String translatedString = "";
            boolean found = false;
            for (JsonI18nType type : json.getTypes()) {
                if (type.getType().equalsIgnoreCase(typeName)) {
                    if (type.getNames().containsKey(locale.getLanguage())) {
                        translatedString = type.getNames().get(locale.getLanguage());
                        found = true;
                    }
                }
            }
            if (!found) {
                JEVisClass inheritanceClass = ds.getJEVisClass(jevisClass).getInheritance();
                List<JsonI18nClass> possibleParents = new ArrayList<>();
                getPossibleParents(possibleParents, inheritanceClass);

                for (JsonI18nClass jsonI18nClass : possibleParents) {
                    for (JsonI18nType type : jsonI18nClass.getTypes()) {
                        if (type.getType().equalsIgnoreCase(typeName)) {
                            if (type.getNames().containsKey(locale.getLanguage())) {
                                translatedString = type.getNames().get(locale.getLanguage());
                                found = true;
                            }
                        }
                    }
                }
            }

            if (!found) {
                logger.warn("Type name not found: {}-{}", jevisClass, typeName);
                translatedString = typeName;
            }

            return translatedString;
        } catch (Exception ex) {
            logger.error("Error while loading Type name: {}.{}: {}", jevisClass, typeName, ex);
        }

        return typeName;
    }

    private void getPossibleParents(List<JsonI18nClass> possibleParents, JEVisClass inheritanceClass) {

        JsonI18nClass parentJson = null;
        try {
            parentJson = getJsonClass(inheritanceClass.getName());
        } catch (JEVisException e) {
            logger.error(e);
        }
        if (parentJson != null) {
            possibleParents.add(parentJson);
            JEVisClass parentInheritanceClass = null;
            try {
                parentInheritanceClass = ds.getJEVisClass(parentJson.getJevisclass()).getInheritance();
            } catch (JEVisException e) {
                logger.error(e);
            }
            if (parentInheritanceClass != null) {
                getPossibleParents(possibleParents, parentInheritanceClass);
            }
        }
    }

    public String getAttributeName(JEVisAttribute attribute) throws JEVisException {
        if (attribute == null) {
            logger.error("Null Attribute");
            return "-";
        }
        return getTypeName(attribute.getType());
    }

    public String getTypeName(JEVisType type) throws JEVisException {
        if (type == null) {
            logger.error("Null Type");
            return "-";
        }
        return getTypeName(type.getJEVisClassName(), type.getName());
    }

    public String getClassName(JEVisClass jclass) throws JEVisException {
        if (jclass == null) {
            logger.error("Null JEVisClass");
            return "-";
        }
        return getClassName(jclass.getName());
    }


    public String getClassDescription(String className) {
        JsonI18nClass json = getJsonClass(className);

        if (json.getDescriptions().containsKey(locale.getLanguage())) {
            return json.getDescriptions().get(locale.getLanguage());
        } else {
            logger.warn("Class description not found: {}", className);
            return "";
        }
    }

    public String getTypeDescription(JEVisType type) {
        try {
            return getTypeDescription(type.getJEVisClassName(), type.getName());
        } catch (Exception ex) {
            return "";
        }
    }

    public String getAttributeDescription(JEVisAttribute att) {
        try {
            return getTypeDescription(att.getType());
        } catch (Exception ex) {
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
                    logger.warn("Type description not found: {}-{}", jevisClass, typeName);
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

        //null handling
        JsonI18nClass fallback = new JsonI18nClass();
        fallback.setJevisclass(classname);
        return fallback;
    }


}
