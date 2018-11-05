package org.jevis.ws.sql;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisConstants;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.rest.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JEVisClassHelper {


    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisClassHelper.class);

    /**
     * Find all JEViClasses which inhered from the given class into the list.
     *
     * @param classname
     * @param list
     */
    public static void findHeir(String classname, List<String> list) {
        JsonJEVisClass jclass = Config.getClassCache().get(classname);
        if (jclass == null) {
            return;
        }

        for (JsonClassRelationship rel : jclass.getRelationships()) {
            if (rel.getType() == JEVisConstants.ClassRelationship.INHERIT
                    && rel.getEnd().equals(classname)) {
                list.add(rel.getStart());
                findHeir(rel.getStart(), list);
            }
        }

    }

    public static JsonType getType(String classname, String typename) {
        JsonJEVisClass jclass = Config.getClassCache().get(classname);
        if (jclass == null) {
            return null;
        }

        for (JsonType type : jclass.getTypes()) {
            if (type.getName().equals(typename)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Travers the heritage graph of the class class an add the types to its children.
     *
     * @param classMap
     * @param superClass
     */
    public static void addHeirs(Map<String, JsonJEVisClass> classMap, JsonJEVisClass superClass) {
        for (JsonClassRelationship rel : superClass.getRelationships()) {
            if (rel.getType() == JEVisConstants.ClassRelationship.INHERIT
                    && rel.getEnd().equals(superClass.getName())) {

                JsonJEVisClass subClass = classMap.get(rel.getStart());
                addHeirs(classMap, subClass);

                if (superClass.getTypes() == null) {
                    superClass.setTypes(new ArrayList<JsonType>());
                }
                for (JsonType type : superClass.getTypes()) {
                    try {
                        if (!subClass.getTypes().contains(type)) {
                            JsonType clone = cloneType(type);
//                            clone.setInherited(true);
                            subClass.getTypes().add(clone);
                        } else {
//                            logger.info("Waring, redudant type in "+subClass.getName()+ ": "+type.getName());
                        }
                    } catch (Exception ex) {
//                        logger.warn(ex);
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static JsonType cloneType(JsonType type) {
        JsonType clone = new JsonType();
        clone.setInherited(type.getInherited());
        clone.setDescription(type.getDescription());
        clone.setGUIPosition(type.getGUIPosition());
        clone.setJevisclass(type.getJevisClass());
        clone.setGuiType(type.getGuiType());
        clone.setName(type.getName());
        clone.setPrimitiveType(type.getPrimitiveType());
        clone.setValidity(type.getValidity());
        return clone;
    }

    /**
     * Goes thru the jevis classes and complete all inhered types and relationships
     *
     * @param classMap
     */
    public static void completeClasses(Map<String, JsonJEVisClass> classMap) {
        Map<String, JsonClassRelationship> clRelationships = new HashMap<>();
        //Add cross relationships
        for (Map.Entry<String, JsonJEVisClass> jc : classMap.entrySet()) {
            try {
                //Add one side missing relationships
                for (JsonClassRelationship rel : jc.getValue().getRelationships()) {
                    try {
                        String relKey = rel.getStart() + ":" + rel.getEnd() + ":" + rel.getType();
                        clRelationships.put(relKey, rel);
                    } catch (Exception ex) {
                        logger.error("Error while listing classes relationships[" + jc.getKey() + "]", ex);
                    }
                }

            } catch (Exception ex) {
                logger.error("Error while listing classes[" + jc.getKey() + "]", ex);
            }
        }

        for (Map.Entry<String, JsonClassRelationship> rel : clRelationships.entrySet()) {
            try {
                JsonJEVisClass startClass = classMap.get(rel.getValue().getStart());
                JsonJEVisClass endClass = classMap.get(rel.getValue().getEnd());

                if (!startClass.getRelationships().contains(rel.getValue())) {
                    startClass.getRelationships().add(rel.getValue());
                }

                if (!endClass.getRelationships().contains(rel.getValue())) {
                    endClass.getRelationships().add(rel.getValue());
                }
            } catch (Exception ex) {
                logger.error("Error while mapping class relationships[{}]", rel.getKey(), ex);
            }
        }

        for (Map.Entry<String, JsonJEVisClass> jc : classMap.entrySet()) {
            for (JsonClassRelationship rel : jc.getValue().getRelationships()) {
                try {
                    if (rel.getType() == JEVisConstants.ClassRelationship.INHERIT) {
                        JsonJEVisClass superClass = classMap.get(rel.getEnd());
                        addHeirs(classMap, superClass);
                    }
                } catch (Exception ex) {
                    logger.error("Error while listing classes relationships[" + jc.getKey() + "]", ex);
                }
            }

        }

    }

}
