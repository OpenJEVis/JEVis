/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEWebService.
 * <p>
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.ws.json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This Factory can convert JEAPI interfaces into a JSON representation
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JsonFactory {

    private static final Logger logger = LogManager.getLogger(JsonFactory.class);
    /**
     * Default date format for attribute dates
     */
    private static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    /**
     * default date format for JEVIsSamples Timestamps
     */
    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();

    /**
     * Build a JSON representation of a JEVisAttribute list
     *
     * @param atts
     * @return
     * @throws JEVisException
     */
    public static List<JsonAttribute> buildAttributes(List<JEVisAttribute> atts) throws JEVisException {
        List<JsonAttribute> jAtts = new ArrayList<JsonAttribute>();
        for (JEVisAttribute att : atts) {
            jAtts.add(buildAttribute(att));
        }

        return jAtts;
    }

    public static JsonUnit buildUnit(JEVisUnit unit) {
        JsonUnit json = new JsonUnit();
        try {
            json.setPrefix(UnitManager.getInstance().getPrefixName(unit.getPrefix(), Locale.getDefault()));
            json.setFormula(unit.getFormula());
            json.setLabel(unit.getLabel());
        } catch (Exception ex) {
            logger.error("Error while building JSON for unit: " + ex);
            json.setPrefix(UnitManager.PrefixName.NONE);
            json.setFormula(javax.measure.unit.Unit.ONE.toString());
            json.setLabel("Unknown");
        }
        return json;
    }

    /**
     * Build a JSON representation of a JEVisAttribute
     *
     * @param att
     * @return
     * @throws JEVisException
     */
    public static JsonAttribute buildAttribute(JEVisAttribute att) throws JEVisException {
        JsonAttribute jatt = new JsonAttribute();

        if (att.hasSample()) {
            jatt.setBegins(attDTF.print(att.getTimestampFromFirstSample()));
            jatt.setEnds(attDTF.print(att.getTimestampFromLastSample()));
            jatt.setSampleCount(att.getSampleCount());
        }

        jatt.setInputSampleRate(att.getInputSampleRate().toString());
        jatt.setDisplaySampleRate(att.getDisplaySampleRate().toString());

        jatt.setType(att.getName());
        if (att.getInputUnit() != null && !att.getInputUnit().toString().isEmpty()) {
            jatt.setInputUnit(JsonFactory.buildUnit(att.getInputUnit()));
        }

        if (att.getDisplayUnit() != null && !att.getDisplayUnit().toString().isEmpty()) {
            jatt.setDisplayUnit(JsonFactory.buildUnit(att.getDisplayUnit()));
        }

        return jatt;
    }

    /**
     * Build a JSON representation of a JEVisRelationship list
     *
     * @param objs
     * @return
     */
    public static List<JsonRelationship> buildRelationship(List<JEVisRelationship> objs) {
        List<JsonRelationship> jRels = new ArrayList<JsonRelationship>();
        for (JEVisRelationship rel : objs) {
            try {
                JsonRelationship json = new JsonRelationship();
                json.setFrom(rel.getStartID());
                json.setTo(rel.getEndID());
                json.setType(rel.getType());
                jRels.add(json);
            } catch (Exception ex) {
                logger.error("Error while building JSON: " + ex);
            }
        }

        return jRels;
    }

    /**
     * Build a JSON representation of a JEVisClass
     *
     * @param objs
     * @return
     */
    public static List<JsonClassRelationship> buildClassRelationships(List<JEVisClassRelationship> objs) {
        List<JsonClassRelationship> jRels = new ArrayList<JsonClassRelationship>();
        for (JEVisClassRelationship rel : objs) {
            try {
                jRels.add(buildClassRelationship(rel));
//                JsonClassRelationship json = new JsonClassRelationship();
//                json.setStart(rel.getStart().getName());
//                json.setEnd(rel.getEnd().getName());
//                json.setType(rel.getType());
//                jRels.add(json);
            } catch (Exception ex) {
                logger.error("Error while building JSON: " + ex);
            }
        }

        return jRels;
    }

    public static JsonClassRelationship buildClassRelationship(JEVisClassRelationship rel) throws JEVisException {

        JsonClassRelationship json = new JsonClassRelationship();
        json.setStart(rel.getStartName());
        json.setEnd(rel.getEndName());
        json.setType(rel.getType());

        return json;
    }

    /**
     * Build a JSON representation of a JEVisObject list
     *
     * @param objs
     * @return
     */
    public static List<JsonObject> buildObject(List<JEVisObject> objs, boolean includeRelationships) {
        List<JsonObject> jObjects = new ArrayList<JsonObject>();

        for (JEVisObject obj : objs) {
            try {
                jObjects.add(buildObject(obj, includeRelationships));
            } catch (Exception ex) {
                logger.error(ex);
            }

        }

        return jObjects;
    }

    public static List<JsonObject> buildDetailedObject(List<JEVisObject> objs) throws JEVisException {
        List<JsonObject> jObjects = new ArrayList<JsonObject>();
        for (JEVisObject obj : objs) {
            jObjects.add(buildDetailedObject(obj));

        }

        return jObjects;
    }

    public static JsonObject buildDetailedObject(JEVisObject obj) throws JEVisException {
        JsonObject json = new JsonObject();
        json.setName(obj.getName());
        json.setId(obj.getID());
        json.setJevisClass(obj.getJEVisClass().getName());
        json.setRelationships(JsonFactory.buildRelationship(obj.getRelationships()));
        List<JsonAttribute> attributes = new ArrayList<JsonAttribute>();
        for (JEVisAttribute att : obj.getAttributes()) {
            attributes.add(buildAttribute(att));
        }
        json.setAttributes(attributes);

        List<JsonObject> children = new ArrayList<JsonObject>();
        for (JEVisObject child : obj.getChildren()) {
            children.add(buildDetailedObject(child));
        }
        json.setObjects(children);

        return json;
    }

    /**
     * Build a JSON representation of a JEVisObject
     *
     * @param obj
     * @param includeRelationships
     * @return
     * @throws JEVisException
     */
    public static JsonObject buildObject(JEVisObject obj, boolean includeRelationships) throws JEVisException {
        logger.trace("build Json includeRelationships: {} obj: {}", includeRelationships, obj);
        JsonObject json = new JsonObject();
        json.setName(obj.getName());
        json.setId(obj.getID());
        json.setJevisClass(obj.getJEVisClassName());
        json.setisPublic(obj.isPublic());
        if (obj.getID() < 10) {
            logger.info("----------- " + obj.isPublic());
        }


        if (!obj.getParents().isEmpty()) {
            json.setParent(obj.getParents().get(0).getID());
        }

        if (includeRelationships) {
            json.setRelationships(JsonFactory.buildRelationship(obj.getRelationships()));

        }
        return json;
    }

    /**
     * Build a JSON representation of a JEVisRelationship
     *
     * @param rel
     * @return
     * @throws JEVisException
     */
    public static JsonRelationship buildRelationship(JEVisRelationship rel) throws JEVisException {
        JsonRelationship json = new JsonRelationship();
        json.setFrom(rel.getStartID());
        json.setTo(rel.getEndID());
        json.setType(rel.getType());//or as String lile Link
        return json;
    }

    /**
     * Build a JSON representation of a JEVisClass list
     *
     * @param classes
     * @return
     * @throws JEVisException
     */
    public static List<JsonJEVisClass> buildJEVisClass(List<JEVisClass> classes) throws JEVisException {
        List<JsonJEVisClass> jclasses = new ArrayList<JsonJEVisClass>();

        for (JEVisClass jc : classes) {
            jclasses.add(buildJEVisClass(jc));
        }

        return jclasses;
    }

    /**
     * Builds a JSON representation of a JEVisClass
     *
     * @param jclass
     * @return
     * @throws JEVisException
     */
    public static JsonJEVisClass buildJEVisClass(JEVisClass jclass) throws JEVisException {
        JsonJEVisClass json = new JsonJEVisClass();
        json.setName(jclass.getName());

        json.setUnique(jclass.isUnique());
        json.setDescription(jclass.getDescription());

        json.setRelationships(buildClassRelationships(jclass.getRelationships()));

        return json;
    }

    /**
     * Build a JSON representation of a JEVIsSample
     *
     * @param sample
     * @return
     * @throws JEVisException
     */
    public static JsonSample buildSample(JEVisSample sample, int primitiveType) throws JEVisException {
        JsonSample json = new JsonSample();

        json.setTs(sampleDTF.print(sample.getTimestamp()));

        //TODO: handle other types appropriately
        // format sample-value according to the primitive type
//        int primitiveType = sample.getAttribute().getType().getPrimitiveType();
        if (primitiveType == JEVisConstants.PrimitiveType.FILE) {
            json.setValue(sample.getValueAsFile().getFilename());
        } else {
            json.setValue(sample.getValueAsString());
        }

        if (sample.getNote() != null && !sample.getNote().isEmpty()) {
            json.setNote(sample.getNote());
        }

        return json;
    }

    /**
     * Build a list of JSON representation of list of JEVisTypes
     *
     * @param types
     * @return
     */
    public static List<JsonType> buildTypes(List<JEVisType> types) {
        List<JsonType> jtypes = new ArrayList<JsonType>();

        for (JEVisType type : types) {
            try {
                jtypes.add(buildType(type));
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }

        return jtypes;
    }

    /**
     * Build a JSON representation of a JEVisType
     *
     * @param type
     * @return
     * @throws JEVisException
     */
    public static JsonType buildType(JEVisType type) throws JEVisException {
        JsonType json = new JsonType();
        json.setDescription(type.getDescription());
        json.setGuiType(type.getGUIDisplayType());
        json.setPrimitiveType(type.getPrimitiveType());
        json.setName(type.getName());
        json.setValidity(type.getValidity());
        json.setInherited(type.isInherited());
        return json;
    }
}
