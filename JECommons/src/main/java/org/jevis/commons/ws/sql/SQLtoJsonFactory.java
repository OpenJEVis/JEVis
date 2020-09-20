/**
 * Copyright (C) 2013 - 2018 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.ws.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisConstants;
import org.jevis.commons.ws.json.*;
import org.jevis.commons.ws.sql.tables.AttributeTable;
import org.jevis.commons.ws.sql.tables.ObjectTable;
import org.jevis.commons.ws.sql.tables.RelationshipTable;
import org.jevis.commons.ws.sql.tables.SampleTable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This Factory can convert JEAPI interfaces into a JSON representation
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SQLtoJsonFactory {

    /**
     * default date format for JEVIsSamples Timestamps
     */
    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SQLtoJsonFactory.class);
    /**
     * Default date format for attribute dates
     */
    private static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    //    private static final Gson gson = new Gson();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonAttribute buildAttributeThisLastValue(ResultSet rs) throws SQLException {
        JsonType type = JEVisClassHelper.getType(rs.getString(ObjectTable.COLUMN_CLASS), rs.getString(AttributeTable.COLUMN_NAME));
        if (type == null) {
            return null;
        }
        JsonAttribute jatt = new JsonAttribute();

        Long sampleCount = rs.getLong(AttributeTable.COLUMN_COUNT);

        if (sampleCount > 0) {
            jatt.setBegins(attDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MIN_TS))));
            jatt.setEnds(attDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MAX_TS))));
            jatt.setSampleCount(sampleCount);
        }

        String name = rs.getString(AttributeTable.COLUMN_NAME);
        Long objectID = rs.getLong(AttributeTable.COLUMN_OBJECT);
        String inputSRate = rs.getString(AttributeTable.COLUMN_INPUT_RATE);
        String inputUnit = rs.getString(AttributeTable.COLUMN_INPUT_UNIT);
        String displayRate = rs.getString(AttributeTable.COLUMN_DISPLAY_RATE);
        String displayUnit = rs.getString(AttributeTable.COLUMN_DISPLAY_UNIT);

        jatt.setInputSampleRate(inputSRate);
        jatt.setDisplaySampleRate(displayRate);
        jatt.setType(name);
        jatt.setObjectID(objectID);

        jatt.setInputUnit(getUnitFromString(inputUnit));
        jatt.setDisplayUnit(getUnitFromString(displayUnit));

        try {
            jatt.setPrimitiveType(type.getPrimitiveType());
            //TODO getLastSample bease on priType
//            jatt.setLatestValue(rs.getString(SampleTable.COLUMN_VALUE));
            if (rs.getString(SampleTable.COLUMN_VALUE) != null) {
                JsonSample sample = new JsonSample();
                sample.setNote(rs.getString(SampleTable.COLUMN_NOTE));
                sample.setTs(sampleDTF.print(new DateTime(rs.getTimestamp(AttributeTable.COLUMN_MAX_TS))));

                if (jatt.getPrimitiveType() == JEVisConstants.PrimitiveType.BOOLEAN) {
                    sample.setValue("" + rs.getBoolean(SampleTable.COLUMN_VALUE));
                } else {
                    sample.setValue(rs.getString(SampleTable.COLUMN_VALUE));
                }


                jatt.setLatestValue(sample);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jatt;
    }

    private static JsonUnit getUnitFromString(String unitString) {
        JsonUnit unit = new JsonUnit();
        unit.setFormula("");
        unit.setLabel("");
        unit.setPrefix("");

        if (unitString != null) {
            //check if its a broken unit
            long count = unitString.chars().filter(ch -> ch == ',').count();
            if (count == 2) {
                try {
                    unit = objectMapper.readValue(unitString, JsonUnit.class);
                } catch (Exception ex) {
                    logger.error("Could not parse input unit {}", unitString, ex);
                }
            }
        }

        return unit;
    }


    /**
     * Build a JSON representation of a JEVisObject
     *
     * @return
     */
    public static JsonObject buildObject(ResultSet rs) throws SQLException {
        JsonObject json = new JsonObject();
        json.setName(rs.getString(ObjectTable.COLUMN_NAME));
        json.setId(rs.getLong(ObjectTable.COLUMN_ID));
        json.setJevisClass(rs.getString(ObjectTable.COLUMN_CLASS));
        json.setisPublic(rs.getBoolean(ObjectTable.COLUMN_PUBLIC));

        String i18njsonString = rs.getString(ObjectTable.COLUMN_I18N);
        if(i18njsonString!=null && !i18njsonString.isEmpty()){
            try {
                JsonI18n[] jsons = objectMapper.readValue(i18njsonString, JsonI18n[].class);
                json.setI18n(Arrays.asList(jsons));
            }catch (Exception ex){
                logger.error(ex);
            }

        }

        return json;
    }


    /**
     * Build a JSON representation of a JEVisRelationship
     *
     * @return
     */
    public static JsonRelationship buildRelationship(ResultSet rs) throws SQLException {
        JsonRelationship json = new JsonRelationship();
        json.setFrom(rs.getLong(RelationshipTable.COLUMN_START));
        json.setTo(rs.getLong(RelationshipTable.COLUMN_END));
        json.setType(rs.getInt(RelationshipTable.COLUMN_TYPE));

        return json;
    }


    public static void addTypesToClasses(Map<String, JsonJEVisClass> classes, List<JsonType> types) {
        for (JsonType t : types) {
            try {
                JsonJEVisClass jc = classes.get(t.getJevisClass());

                if (jc.getTypes() == null) {
                    jc.setTypes(new ArrayList<JsonType>());
                }
                jc.getTypes().add(t);
            } catch (Exception ex) {

            }

        }
    }

    public static Map<String, JsonJEVisClass> toMap(List<JsonJEVisClass> classes) {
        Map<String, JsonJEVisClass> map = Maps.uniqueIndex(classes, new Function<JsonJEVisClass, String>() {
            @Override
            public String apply(JsonJEVisClass f) {
                return f.getName();
            }
        });
        return map;
    }

    /**
     * Build a JSON representation of a JEVIsSample
     *
     * @return
     */
    public static JsonSample buildSample(ResultSet rs) throws SQLException {
        JsonSample json = new JsonSample();
        json.setNote(rs.getString(SampleTable.COLUMN_NOTE));
        json.setTs(sampleDTF.print(new DateTime(rs.getTimestamp(SampleTable.COLUMN_TIMESTAMP))));
        json.setValue(rs.getString(SampleTable.COLUMN_VALUE));

        return json;
    }
}
