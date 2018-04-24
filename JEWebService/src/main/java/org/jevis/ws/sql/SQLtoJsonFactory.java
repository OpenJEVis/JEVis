/*
  Copyright (C) 2013 - 2018 Envidatec GmbH <info@envidatec.com>

  This file is part of JEWebService.

  JEWebService is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JEWebService is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  JEWebService. If not, see <http://www.gnu.org/licenses/>.

  JEWebService is part of the OpenJEVis project, further project information
  are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.ws.sql;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisConstants;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.*;
import org.jevis.ws.sql.tables.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Factory can convert JEAPI interfaces into a JSON representation
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SQLtoJsonFactory {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SQLtoJsonFactory.class);
    /**
     * Default date format for attribute dates
     */
    private static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    /**
     * default date format for JEVIsSamples Timestamps
     */
    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();

    private static final Gson gson = new Gson();

//    public static JsonAttribute buildAttribute(ResultSet rs) throws JEVisException, SQLException {
//        JsonAttribute jatt = new JsonAttribute();
//
//        Long sampleCount = rs.getLong(AttributeTable.COLUMN_COUNT);
//
//        if (sampleCount > 0) {
//            jatt.setBegins(attDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MIN_TS))));
//            jatt.setEnds(attDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MAX_TS))));
//            jatt.setSampleCount(sampleCount);
//        }
//
//        String name = rs.getString(AttributeTable.COLUMN_NAME);
//        Long objectID = rs.getLong(AttributeTable.COLUMN_OBJECT);
//        String imputSRate = rs.getString(AttributeTable.COLUMN_INPUT_RATE);
//        String displayRate = rs.getString(AttributeTable.COLUMN_DISPLAY_RATE);
//
//        jatt.setInputSampleRate(imputSRate);
//        jatt.setDisplaySampleRate(displayRate);
//        jatt.setType(name);
//
//        try {
//            JEVisUnitImp imputUnit = new JEVisUnitImp(gson.fromJson(rs.getString(AttributeTable.COLUMN_INPUT_UNIT), JsonUnit.class));
//            jatt.setInputUnit(JsonFactory.buildUnit(imputUnit));
//        } catch (Exception ex) {
//
//        }
//
//        try {
//            JEVisUnitImp displayUnit = new JEVisUnitImp(gson.fromJson(rs.getString(AttributeTable.COLUMN_DISPLAY_UNIT), JsonUnit.class));
//            jatt.setDisplayUnit(JsonFactory.buildUnit(displayUnit));
//        } catch (Exception ex) {
//
//        }
//
//        return jatt;
//    }
public static JsonAttribute buildAttributeThisLastValue(ResultSet rs) throws SQLException {
        JsonAttribute jatt = new JsonAttribute();

        Long sampleCount = rs.getLong(AttributeTable.COLUMN_COUNT);

        if (sampleCount > 0) {
            jatt.setBegins(attDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MIN_TS))));
            jatt.setEnds(attDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MAX_TS))));
            jatt.setSampleCount(sampleCount);
        }

        String name = rs.getString(AttributeTable.COLUMN_NAME);
        Long objectID = rs.getLong(AttributeTable.COLUMN_OBJECT);
        String imputSRate = rs.getString(AttributeTable.COLUMN_INPUT_RATE);
        String displayRate = rs.getString(AttributeTable.COLUMN_DISPLAY_RATE);

        jatt.setInputSampleRate(imputSRate);
        jatt.setDisplaySampleRate(displayRate);
        jatt.setType(name);

        try {
            JEVisUnitImp imputUnit = new JEVisUnitImp(gson.fromJson(rs.getString(AttributeTable.COLUMN_INPUT_UNIT), JsonUnit.class));
            jatt.setInputUnit(JsonFactory.buildUnit(imputUnit));
        } catch (Exception ex) {

        }

        try {
            JEVisUnitImp displayUnit = new JEVisUnitImp(gson.fromJson(rs.getString(AttributeTable.COLUMN_DISPLAY_UNIT), JsonUnit.class));
            jatt.setDisplayUnit(JsonFactory.buildUnit(displayUnit));
        } catch (Exception ex) {

        }

        try {
            jatt.setPrimitiveType(rs.getInt(TypeTable.COLUMN_PRIMITIV_TYPE));
            //TODO getLastSample bease on priType
//            jatt.setLatestValue(rs.getString(SampleTable.COLUMN_VALUE));
            if (rs.getString(SampleTable.COLUMN_VALUE) != null) {
                JsonSample sample = new JsonSample();
                sample.setNote(rs.getString(SampleTable.COLUMN_NOTE));
                sample.setTs(sampleDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MAX_TS))));
                
                if(jatt.getPrimitiveType()==JEVisConstants.PrimitiveType.BOOLEAN){
                    sample.setValue(""+rs.getBoolean(SampleTable.COLUMN_VALUE));
                }else{
                    sample.setValue(rs.getString(SampleTable.COLUMN_VALUE));
                }
                
                
                jatt.setLatestValue(sample);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jatt;
    }

    public static JsonClassRelationship buildClassRelationship(ResultSet rs) throws SQLException {

        JsonClassRelationship json = new JsonClassRelationship();
        json.setStart(rs.getString(ClassRelationTable.COLUMN_START));
        json.setEnd(rs.getString(ClassRelationTable.COLUMN_END));
        json.setType(rs.getInt(ClassRelationTable.COLUMN_TYPE));

        return json;
    }

    //used by the android app?
//    public static JsonObject buildDetailedObject(JEVisObject obj) throws JEVisException {
//        JsonObject json = new JsonObject();
//        json.setName(obj.getName());
//        json.setId(obj.getID());
//        json.setJevisClass(obj.getJEVisClass().getName());
//        json.setRelationships(JsonSQLFactory.buildRelationship(obj.getRelationships()));
//        List<JsonAttribute> attributes = new ArrayList<JsonAttribute>();
//        for (JEVisAttribute att : obj.getAttributes()) {
//            attributes.add(buildAttribute(att));
//        }
//        json.setAttributes(attributes);
//
//        List<JsonObject> children = new ArrayList<JsonObject>();
//        for (JEVisObject child : obj.getChildren()) {
//            children.add(buildDetailedObject(child));
//        }
//        json.setObjects(children);
//
//        return json;
//    }
    /**
     * Build a JSON representation of a JEVisObject
     *
     * @param obj
     * @param includeRelationships
     * @return
     */
    public static JsonObject buildObject(ResultSet rs) throws SQLException {
        JsonObject json = new JsonObject();
        json.setName(rs.getString(ObjectTable.COLUMN_NAME));
        json.setId(rs.getLong(ObjectTable.COLUMN_ID));
        json.setJevisClass(rs.getString(ObjectTable.COLUMN_CLASS));
        json.setisPublic(rs.getBoolean(ObjectTable.COLUMN_PUBLIC));
        return json;
    }

    /**
     * Build a JSON representation of a JEVisRelationship
     *
     * @param rel
     * @return
     */
    public static JsonRelationship buildRelationship(ResultSet rs) throws SQLException {
        JsonRelationship json = new JsonRelationship();
        json.setFrom(rs.getLong(RelationshipTable.COLUMN_START));
        json.setTo(rs.getLong(RelationshipTable.COLUMN_END));
        json.setType(rs.getInt(RelationshipTable.COLUMN_TYPE));//or as String lile Link
        return json;
    }

    /**
     * Builds a JSON representation of a JEVisClass
     *
     * @param jclass
     * @return
     */
    public static JsonJEVisClass buildJEVisClass(ResultSet rs) throws SQLException {
        JsonJEVisClass json = new JsonJEVisClass();

        json.setName(rs.getString(ClassTable.COLUMN_NAME));
        json.setDescription(rs.getString(ClassTable.COLUMN_DESCRIPTION));
        json.setUnique(rs.getBoolean(ClassTable.COLUMN_UNIQUE));
//        json.setRelationships(buildClassRelationships(jclass.getRelationships()));

        return json;
    }

    public static void addTypesToClasses(Map<String, JsonJEVisClass> classes, List<JsonType> types) {
        for (JsonType t : types) {
            try {
                JsonJEVisClass jc = classes.get(t.getJevisClass());

                if (jc.getTypes() == null) {
                    jc.setTypes(new ArrayList<>());
                }
                jc.getTypes().add(t);
            } catch (Exception ex) {
                
            }

        }
    }

    public static Map<String, JsonJEVisClass> toMap(List<JsonJEVisClass> classes) {
        return Maps.uniqueIndex(classes, new Function<JsonJEVisClass, String>() {
            @Override
            public String apply(JsonJEVisClass f) {
                return f.getName();
            }
        });
    }

    public static void addRelationhipsToClasses(Map<String, JsonJEVisClass> classes, List<JsonClassRelationship> classRels) {
        for (JsonClassRelationship t : classRels) {
            try {
                JsonJEVisClass toJC = classes.get(t.getEnd());
                JsonJEVisClass fromJC = classes.get(t.getStart());

                if (toJC.getRelationships() == null) {
                    toJC.setRelationships(new ArrayList<>());
                }
                if (fromJC.getRelationships() == null) {
                    fromJC.setRelationships(new ArrayList<>());
                }

                toJC.getRelationships().add(t);
                fromJC.getRelationships().add(t);
            } catch (NullPointerException np) {
                np.printStackTrace();
            }

        }
    }

    /**
     * Build a JSON representation of a JEVIsSample
     *
     * @param sample
     * @return
     */
    public static JsonSample buildSample(ResultSet rs) throws SQLException {
        JsonSample json = new JsonSample();
        json.setNote(rs.getString(SampleTable.COLUMN_NOTE));
        json.setTs(sampleDTF.print(new DateTime(rs.getTimestamp(SampleTable.COLUMN_TIMESTAMP))));
        json.setValue(rs.getString(SampleTable.COLUMN_VALUE));

        return json;
    }

    /**
     * Build a JSON representation of a JEVisType
     *
     * @param type
     * @return
     */
    public static JsonType buildType(ResultSet rs) throws SQLException {
        JsonType json = new JsonType();
        json.setDescription(rs.getString(TypeTable.COLUMN_DESCRIPTION));
        json.setGuiType(rs.getString(TypeTable.COLUMN_DISPLAY_TYPE));
        json.setPrimitiveType(rs.getInt(TypeTable.COLUMN_PRIMITIV_TYPE));
        json.setName(rs.getString(TypeTable.COLUMN_NAME));
        json.setValidity(rs.getInt(TypeTable.COLUMN_VALIDITY));
        json.setInherited(rs.getBoolean(TypeTable.COLUMN_INHERITEDT));
        json.setJevisclass(rs.getString(TypeTable.COLUMN_CLASS));

//        String unitString = rs.getString(TypeTable.COLUMN_DEFAULT_UNIT);
        return json;
    }
}
