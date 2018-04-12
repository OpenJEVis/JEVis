/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JsonFactory {

//    public static JEVisObject buildObject(JEVisDataSource ds, JsonObject ob){
//
//    }
    /**
     *
     * @param obj
     * @param attributes
     * @param children
     * @param allSample
     * @return
     * @throws JEVisException
     */
    public static JsonObject buildObject(JEVisObject obj, boolean attributes, boolean children, boolean allSample) throws JEVisException {
        JsonObject json = new JsonObject();
        json.setName(obj.getName());
        json.setId(obj.getID());
        json.setJevisClass(obj.getJEVisClass().getName());

        if (!obj.getParents().isEmpty()) {
            json.setParent(22l);
        }

        if (attributes) {
            for (JEVisAttribute att : obj.getAttributes()) {
                json.getAttributes().add(JsonFactory.buildAttribute(att, allSample));
            }
        }
        if (children) {
            for (JEVisObject child : obj.getChildren()) {
                json.getChildren().add(JsonFactory.buildObject(child, attributes, children, allSample));
            }
        }

//        List<JsonRelationship> rels = new LinkedList<JsonRelationship>();
//        for (JEVisRelationship rel : obj.getRelationships()) {
//            rels.add(JsonFactory.buildRelationship(rel));
//        }
//        json.setRelations(rels);
        return json;
    }

    public static JsonRelationship buildRelationship(JEVisRelationship rel) throws JEVisException {
        JsonRelationship json = new JsonRelationship();
        json.setFrom(rel.getStartObject().getID().toString());
        json.setTo(rel.getEndObject().getID().toString());
        json.setType(rel.getType());//or as String lile Link
        return json;
    }

    public static JsonJEVisClass buildJEVisClass(JEVisClass jclass) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(jclass.getName());
        if (jclass.getInheritance() != null) {
            json.setInheritance(jclass.getInheritance().getName());
        } else {
            json.setInheritance("null");
        }

        json.setUnique(jclass.isUnique());
        json.setDescription(jclass.getDescription());
        return json;
    }

    public static JsonJEVisClass buildJEVisClassComplete(JEVisClass jclass) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(jclass.getName());
        if (jclass.getInheritance() != null) {
            json.setInheritance(jclass.getInheritance().getName());
        } else {
            json.setInheritance("null");
        }

        json.setUnique(jclass.isUnique());
        json.setDescription(jclass.getDescription());
        List<JsonType> types = new ArrayList<>();
        for (JEVisType type : jclass.getTypes()) {
            if (jclass.getInheritance() != null) {
                if (jclass.getInheritance().getTypes().contains(type)) {
                    System.out.println("Dont export inherit class");
                    continue;
                }
            }

            types.add(buildType(type));

        }

        json.setTypes(types);

        List<JsonRelationship> rels = new ArrayList<>();
        for (JEVisClassRelationship rel : jclass.getRelationships()) {
            rels.add(new JsonRelationship(rel));
        }

        json.setRelationships(rels);
        return json;
    }

    /**
     * @TODO: combine this with buildJEVisClassComplete via an attribute to
     * switch mode
     * @param jclass
     * @return
     * @throws JEVisException
     */
    public static JsonJEVisClass buildJEVisClassWithType(JEVisClass jclass) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(jclass.getName());
        if (jclass.getInheritance() != null) {
            json.setInheritance(jclass.getInheritance().getName());
        } else {
            json.setInheritance("null");
        }

        json.setUnique(jclass.isUnique());
        json.setDescription(jclass.getDescription());
        List<JsonType> types = new ArrayList<>();
        for (JEVisType type : jclass.getTypes()) {
            if (jclass.getInheritance() != null) {
                if (jclass.getInheritance().getTypes().contains(type)) {
                    System.out.println("Dont export inherit class");
                    continue;
                }
            }

            types.add(buildType(type));

        }

        json.setTypes(types);

        return json;
    }

    public static JsonAttribute buildAttribute(JEVisAttribute att, boolean allSamples) throws JEVisException {
        JsonAttribute json = new JsonAttribute();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

        json.setName(att.getName());
        if (att.hasSample()) {
            json.setFirstTS(fmt.print(att.getTimestampFromFirstSample()));
            json.setLastTS(fmt.print(att.getTimestampFromLastSample()));
            json.setLastvalue(att.getLatestSample().getValueAsString());

            if (allSamples) {
                json.setSamples(new ArrayList<JsonSample>());

                for (JEVisSample samp : att.getAllSamples()) {
                    json.getSamples().add(JsonFactory.buildSample(samp));
                }

            }

        }
        json.setSamplecount(att.getSampleCount());
        json.setPeriod("P15m");
        json.setObject(att.getObject().getID());

        return json;

    }

    public static JsonSample buildSample(JEVisSample sample) throws JEVisException {
        JsonSample json = new JsonSample();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        json.setTs(fmt.print(sample.getTimestamp()));
        json.setValue(sample.getValue().toString());
        json.setNote(sample.getNote());
        return json;
    }

    public static JsonType buildType(JEVisType type) throws JEVisException {
        JsonType json = new JsonType();
        json.setDescription(type.getDescription());
        json.setGUIDisplayType(type.getGUIDisplayType());
        json.setPrimitiveType(type.getPrimitiveType());
        json.setName(type.getName());
        json.setValidity("" + type.getValidity());
        return json;
    }

//    public static JsonUnit buildUnit(JEVisUnit unit) throws JEVisException {
//        JsonUnit json = new JsonUnit();
//        json.set
//        return json;
//    }
}
