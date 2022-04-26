/**
 * Copyright (C) 2009 - 2013 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEWebService.
 * <p>
 * JEAPI-SQL is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-SQL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-SQL. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-SQL is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.ws.sql.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.SQLtoJsonFactory;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.measure.unit.Unit;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author florian.simon@envidatec.com
 */
public class AttributeTable {

    public final static String TABLE = "attribute";
    public final static String COLUMN_OBJECT = "object";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_MIN_TS = "mints";
    public final static String COLUMN_MAX_TS = "maxts";
    public final static String COLUMN_COUNT = "samplecount";
    public final static String COLUMN_INPUT_UNIT = "inputunit";
    public final static String COLUMN_INPUT_RATE = "inputrate";
    public final static String COLUMN_DISPLAY_UNIT = "displayunit";
    public final static String COLUMN_DISPLAY_RATE = "displayrate";
    private static final Logger logger = LogManager.getLogger(AttributeTable.class);
    private final SQLDataSource ds;

    public AttributeTable(SQLDataSource ds) {
        this.ds = ds;
    }


    /**
     * @param object
     * @return
     * @throws JEVisException
     */
    public List<JsonAttribute> getAttributes(long object) throws JEVisException {
        logger.trace("getAttributes ");
        List<JsonAttribute> attributes = new ArrayList<>();

        String sql = "select o.type,a.*,s.* FROM attribute a left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts ) left join object o on (o.id=a.object) where a.object=?;";


        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);

            logger.debug("SQL {}", ps);
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                try {
                    JsonAttribute att = SQLtoJsonFactory.buildAttributeThisLastValue(rs);
                    if (att != null) {
                        attributes.add(att);
                    }

                } catch (Exception ex) {
                    logger.trace(ex, ex);
                }

            }

        } catch (SQLException ex) {
            logger.error(ex, ex);
        }


        return attributes;
    }

    public JsonAttribute getAttribute(long object, String name) {
        logger.trace("getAttribute:  {}, {}", object, name);

        JsonAttribute attribute = null;

        String sql = "select o.type,a.*,s.* " +
                "FROM attribute a left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts ) " +
                "left join object o on (o.id=a.object) where a.object=? and a.name=?;";


        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setString(2, name);

            logger.debug("SQL {}", ps);
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                try {
                    attribute = SQLtoJsonFactory.buildAttributeThisLastValue(rs);

                } catch (Exception ex) {
                    logger.trace(ex, ex);
                }

            }

        } catch (SQLException ex) {
            logger.error(ex, ex);
        }


        return attribute;
    }


    public List<JsonAttribute> getAllAttributes() throws JEVisException {
        logger.trace("getAllAttributes ");
        List<JsonAttribute> attributes = new ArrayList<>();

        String sql = "select o.type,a.*,s.* FROM attribute a left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts ) left join object o on (o.id=a.object)";

        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql)) {

            logger.debug("SQL {}", ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    JsonAttribute att = SQLtoJsonFactory.buildAttributeThisLastValue(rs);
                    if (att != null) {
                        attributes.add(att);
                    }

                } catch (Exception ex) {
                    logger.trace(ex, ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex, ex);
        }


        return attributes;
    }

    /**
     * Prototype for an DataProcessor JobListener
     * Does not work because of the Monthly Row -> Monthly Clean problem
     *
     * @return
     * @throws JEVisException
     */
    public List<JsonObject> getDataProcessorTodoList() throws JEVisException {
        logger.trace("getAllAttributes ");
        Map<Long, JsonAttribute> aMap = new HashMap<>();
        List<JsonObject> objects = new ArrayList<>();

        String sql = "select o.type,a.*,s.* FROM attribute a left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts ) left join object o on (o.id=a.object) where a.name=\"Value\";";

        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql)) {

            logger.debug("SQL {}", ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    JsonAttribute att = SQLtoJsonFactory.buildAttributeThisLastValue(rs);
                    if (att != null) {
                        aMap.put(att.getObjectID(), att);
                    }

                } catch (Exception ex) {
                    logger.trace(ex, ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex, ex);
        }

        /** todo: the get(Type) filter does not work, fix to remove manual filter here*/
        List<JsonRelationship> allRelationships = ds.getRelationshipTable().getAll();
        List<JsonRelationship> relationships = new ArrayList<>();
        allRelationships.forEach(jsonRelationship -> {
            if (jsonRelationship.getType() == JEVisConstants.ObjectRelationship.PARENT) {
                relationships.add(jsonRelationship);
            }
        });


        DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();

        int count = 0;
        for (JsonRelationship rel : relationships) {
            if (aMap.get(rel.getFrom()) != null && aMap.get(rel.getTo()) != null) {
                JsonAttribute rowDR = aMap.get(rel.getFrom());
                JsonAttribute cleanDR = aMap.get(rel.getTo());
                boolean isNewer = false;
                if (rowDR.getEnds() != null && !rowDR.getEnds().isEmpty() && cleanDR.getEnds() != null && !cleanDR.getEnds().isEmpty()) {
                    if (sampleDTF.parseDateTime(rowDR.getEnds()).isAfter(sampleDTF.parseDateTime(cleanDR.getEnds()))) {
                        isNewer = true;
                        logger.trace("-------- is newer");
                        count++;
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.setId(cleanDR.getObjectID());
                        objects.add(jsonObject);
                    }
                }

                logger.trace("Row: [{}] {} clean: [{}] {} = {}", rowDR.getObjectID(), rowDR.getEnds(), cleanDR.getObjectID(), cleanDR.getEnds(), isNewer);
            }
        }
        logger.trace("Total amount of to calc objects: {}/{}", count, aMap.size());


        return objects;
    }


    public void delete(long objectID) {
        logger.debug("delete attributes: {}", objectID);
        String sqlUpdate = String.format("delete from %s where %s=? ", TABLE, COLUMN_OBJECT);

        try (PreparedStatement psUpdate = ds.getConnection().prepareStatement(sqlUpdate)) {
            psUpdate.setLong(1, objectID);


        } catch (SQLException ex) {
            logger.error(ex, ex);
        }


    }

    /**
     * Update the min/max/count if samples changed for an attribute
     * TODO: this could be an trigger in mysql direct
     *
     * @param objectID
     * @param attribute
     */
    public void updateMinMaxTS(long objectID, String attribute) {
        logger.debug("updateMinMaxTS: {}:{}", objectID, attribute);
        /* with count
         String selectSQL = String.format("select min(%s) as min,max(%s) as max,count(%s) as count " +
                        "from %s where object=? and attribute=?  ",
                SampleTable.COLUMN_TIMESTAMP, SampleTable.COLUMN_TIMESTAMP, SampleTable.COLUMN_TIMESTAMP, SampleTable.TABLE);

        String sqlUpdate = String.format("insert into %s ( %s,%s,%s,%s,%s) " +
                        "VALUES (?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE  %s=VALUES(%s), %s=VALUES(%s), %s=VALUES(%s)",
                TABLE, COLUMN_MIN_TS, COLUMN_MAX_TS, COLUMN_COUNT, COLUMN_OBJECT, COLUMN_NAME,
                COLUMN_MIN_TS, COLUMN_MIN_TS, COLUMN_MAX_TS, COLUMN_MAX_TS, COLUMN_COUNT, COLUMN_COUNT);

         */

        String selectSQL = String.format("select min(%s) as min,max(%s) as max " +
                        "from %s where object=? and attribute=?  ",
                SampleTable.COLUMN_TIMESTAMP, SampleTable.COLUMN_TIMESTAMP, SampleTable.TABLE);


        String sqlUpdate = String.format("insert into %s ( %s,%s,%s,%s,%s) " +
                        "VALUES (?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE  %s=VALUES(%s), %s=VALUES(%s), %s=VALUES(%s)",
                TABLE, COLUMN_MIN_TS, COLUMN_MAX_TS, COLUMN_COUNT, COLUMN_OBJECT, COLUMN_NAME,
                COLUMN_MIN_TS, COLUMN_MIN_TS, COLUMN_MAX_TS, COLUMN_MAX_TS, COLUMN_COUNT, COLUMN_COUNT);

        //logger.error("SqlS1: {}", selectSQL);
        //logger.error("SqlS2: {}", sqlUpdate);

        try (PreparedStatement ps = ds.getConnection().prepareStatement(selectSQL)) {


            ps.setLong(1, objectID);
            ps.setString(2, attribute);
            logger.debug("SQL {}", ps);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp mindate = rs.getTimestamp("min");
                Timestamp maxdate = rs.getTimestamp("max");
                //long count = rs.getLong("count"); // tmp disabled for performance
                long count = 0;
                if (mindate != null) {//tmp performance workaround.
                    count = 1;
                }
                logger.debug("count: {},min: {}", count, mindate);

                try (PreparedStatement psUpdate = ds.getConnection().prepareStatement(sqlUpdate)) {
                    logger.debug("SQL2.update: {}", psUpdate);
                    /** values */
                    if (mindate != null) {
                        psUpdate.setTimestamp(1, mindate);
                        psUpdate.setTimestamp(2, maxdate);

                    } else {
                        psUpdate.setNull(1, Types.TIMESTAMP);
                        psUpdate.setNull(2, Types.TIMESTAMP);
                    }


                    psUpdate.setLong(3, count);
                    psUpdate.setLong(4, objectID);
                    psUpdate.setString(5, attribute);

                    logger.debug("SQL: {}", psUpdate);
                    psUpdate.executeUpdate();

                } catch (SQLException ex) {
                    logger.error(ex, ex);
                }

            }

        } catch (SQLException ex) {
            logger.error(ex, ex);
        }


    }


    /**
     * @param att
     * @throws JEVisException
     */
    public void updateAttribute(long object, JsonAttribute att) throws JEVisException {

        String sql = String.format("insert into %s( %s,%s,%s,%s,%s,%s) Values ( ?,?,?,?,?,?)  ON DUPLICATE KEY UPDATE %s=?,%s=?,%s=?,%s=?",
                TABLE, COLUMN_DISPLAY_UNIT, COLUMN_INPUT_UNIT, COLUMN_DISPLAY_RATE, COLUMN_INPUT_RATE, COLUMN_OBJECT, COLUMN_NAME, COLUMN_DISPLAY_UNIT, COLUMN_INPUT_UNIT, COLUMN_DISPLAY_RATE, COLUMN_INPUT_RATE);

        try (PreparedStatement ps = ds.getConnection().prepareStatement(sql)) {
//            Gson gson = new Gson();
            JEVisUnit fallbackUnit = new JEVisUnitImp(Unit.ONE);

            if (att.getDisplayUnit() != null && att.getDisplayUnit() != null) {
                ps.setString(1, ds.getObjectMapper().writeValueAsString(att.getDisplayUnit()));
                ps.setString(7, ds.getObjectMapper().writeValueAsString(att.getDisplayUnit()));
            } else {
                ps.setString(1, fallbackUnit.toJSON());
                ps.setString(7, fallbackUnit.toJSON());
            }

            if (att.getInputUnit() != null && att.getInputUnit() != null) {
                ps.setString(2, ds.getObjectMapper().writeValueAsString(att.getInputUnit()));
                ps.setString(8, ds.getObjectMapper().writeValueAsString(att.getInputUnit()));

            } else {
                ps.setString(2, fallbackUnit.toJSON());
                ps.setString(8, fallbackUnit.toJSON());
            }

            if (att.getDisplaySampleRate() != null) {
                ps.setString(3, att.getDisplaySampleRate());
                ps.setString(9, att.getDisplaySampleRate());
            } else {
                ps.setString(3, new Period().toString());
                ps.setString(9, new Period().toString());
            }

            if (att.getInputSampleRate() != null) {
                ps.setString(4, att.getInputSampleRate());
                ps.setString(10, att.getInputSampleRate());
            } else {
                ps.setString(4, new Period().toString());
                ps.setString(10, new Period().toString());
            }

            ps.setLong(5, object);

            if (att.getType() != null && !att.getType().isEmpty()) {
                ps.setString(6, att.getType());
            } else {
                throw new JEVisException("Missing attribute name", 326543);
            }

            logger.debug("SQL: {}", ps);

            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.error("SQLException.", ex);
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException.", e);
        }


    }
}
