package org.jevis.ws.sql;

import org.jevis.api.JEVisConstants;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.rest.Config;

import java.util.List;

public class JEVisClassHelper {


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

}
