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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility class that converts JDBC {@link java.sql.ResultSet} rows into the
 * JSON DTO objects used by the JEWebService REST layer.
 * <p>
 * All methods are static; this class cannot be instantiated.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SQLtoJsonFactory {

    /**
     * default date format for JEVIsSamples Timestamps
     */
    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();
    public static final DateTimeFormatter deleteDTF = ISODateTimeFormat.date();
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SQLtoJsonFactory.class);
    /**
     * Default date format for attribute dates
     */
    private static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    //    private static final Gson gson = new Gson();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Builds a {@link JsonAttribute} from the current row of a result set that
     * joins the {@code attribute}, {@code sample} (latest value), and
     * {@code object} (class name) tables.
     *
     * @param rs the current result set row
     * @return the populated {@link JsonAttribute}, or {@code null} if the
     * class/type combination is unknown
     * @throws SQLException if a column cannot be read
     */
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
            //TODO getLastSample based on primitive type
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
     * Builds a {@link JsonObject} from the current row of an {@code object}
     * table result set, including i18n metadata if present.
     *
     * @param rs the current result set row
     * @return the populated {@link JsonObject}
     * @throws SQLException if a column cannot be read
     */
    public static JsonObject buildObject(ResultSet rs) throws SQLException {
        JsonObject json = new JsonObject();
        json.setName(rs.getString(ObjectTable.COLUMN_NAME));
        json.setId(rs.getLong(ObjectTable.COLUMN_ID));
        json.setJevisClass(rs.getString(ObjectTable.COLUMN_CLASS));
        json.setisPublic(rs.getBoolean(ObjectTable.COLUMN_PUBLIC));
        Timestamp deletets = rs.getTimestamp(ObjectTable.COLUMN_DELETE);
        if (deletets != null) {
            json.setDeleteTS(rs.getString(ObjectTable.COLUMN_DELETE));
            //json.setDeleteTS(sampleDTF.print(new DateTime(deletets)));
        }


        String i18njsonString = rs.getString(ObjectTable.COLUMN_I18N);
        if (i18njsonString != null && !i18njsonString.isEmpty()) {
            try {
                JsonI18n[] jsons = objectMapper.readValue(i18njsonString, JsonI18n[].class);
                json.setI18n(Arrays.asList(jsons));
            } catch (Exception ex) {
                logger.error(ex);
            }

        }

        return json;
    }


    /**
     * Builds a {@link JsonRelationship} from the current row of a
     * {@code relationship} table result set.
     *
     * @param rs the current result set row
     * @return the populated {@link JsonRelationship}
     * @throws SQLException if a column cannot be read
     */
    public static JsonRelationship buildRelationship(ResultSet rs) throws SQLException {
        JsonRelationship json = new JsonRelationship();
        json.setFrom(rs.getLong(RelationshipTable.COLUMN_START));
        json.setTo(rs.getLong(RelationshipTable.COLUMN_END));
        json.setType(rs.getInt(RelationshipTable.COLUMN_TYPE));

        return json;
    }


    /**
     * Distributes a flat list of {@link JsonType} objects into their owning
     * {@link JsonJEVisClass} entries in the given map, creating the type list
     * for each class if it does not yet exist.
     *
     * @param classes a mutable class map (name → class) to populate
     * @param types   the type definitions to distribute
     */
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

    /**
     * Converts a list of {@link JsonJEVisClass} objects into a map keyed by
     * class name using Guava's {@code Maps.uniqueIndex}.
     *
     * @param classes the list of class definitions
     * @return an immutable map from class name to class definition
     */
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
     * Builds a {@link JsonSample} from the current row of a {@code sample}
     * table result set. Timestamps are formatted as ISO-8601 strings.
     *
     * @param rs the current result set row
     * @return the populated {@link JsonSample}
     * @throws SQLException if a column cannot be read
     */
    public static JsonSample buildSample(ResultSet rs) throws SQLException {
        JsonSample json = new JsonSample();
        json.setNote(rs.getString(SampleTable.COLUMN_NOTE));
        json.setTs(sampleDTF.print(new DateTime(rs.getTimestamp(SampleTable.COLUMN_TIMESTAMP))));
        json.setValue(rs.getString(SampleTable.COLUMN_VALUE));

        return json;
    }
}
