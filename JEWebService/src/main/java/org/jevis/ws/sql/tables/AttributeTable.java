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
package org.jevis.ws.sql.tables;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;
import org.joda.time.Period;

import javax.measure.unit.Unit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private final SQLDataSource _connection;

    public AttributeTable(SQLDataSource ds) {
        _connection = ds;
    }

    //TODO: try-catch-finally
    public void insert(JEVisType type, JEVisObject obj) {
        String sql = String.format("insert into %s (%s,%s,%s,%s) values(?,?,?,?)",
                TABLE, COLUMN_OBJECT, COLUMN_NAME, COLUMN_DISPLAY_UNIT, COLUMN_INPUT_UNIT);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            try {
                ps.setLong(1, obj.getID());
                ps.setString(2, type.getName());

                String unitJSON = "";
                try {
                    unitJSON = type.getUnit().toJSON();
                } catch (Exception ex) {

                }

                //DisplayUnit
                ps.setString(3, unitJSON);
                ps.setString(4, unitJSON);

                int count = ps.executeUpdate();
            } catch (JEVisException jex) {
                logger.error(jex);
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }


    }

    //TODO: try-catch-finally
    public void insert(JEVisAttribute att) throws JEVisException {
        insert(att.getType(), att.getObject());
    }

    /**
     * @param object
     * @return
     * @throws JEVisException
     */
    public List<JsonAttribute> getAttributes(long object) throws JEVisException {
        logger.trace("getAttributes ");
        List<JsonAttribute> attributes = new ArrayList<>();

        String sql = String.format("select o.type,a.*,s.* " +
                "FROM attribute a " +
                "left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts ) " +
                "left join object o on (o.id=a.object) where a.object=?;");


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);

            logger.trace("SQL {}", ps);
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                try {
                    JsonAttribute att = SQLtoJsonFactory.buildAttributeThisLastValue(rs);
                    if (att != null) {
                        attributes.add(SQLtoJsonFactory.buildAttributeThisLastValue(rs));
                    }

                } catch (Exception ex) {
                    logger.trace(ex);
                }

            }

        } catch (SQLException ex) {
            logger.error(ex);
        }


        return attributes;
    }

    public List<JsonAttribute> getAllAttributes() throws JEVisException {
        logger.trace("getAllAttributes ");
        List<JsonAttribute> attributes = new ArrayList<>();

        String sql = String.format("select o.type,a.*,s.* " +
                "FROM attribute a " +
                "left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts ) " +
                "left join object o on (o.id=a.object)");

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            logger.trace("SQL {}", ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    JsonAttribute att = SQLtoJsonFactory.buildAttributeThisLastValue(rs);
                    if (att != null) {
                        attributes.add(SQLtoJsonFactory.buildAttributeThisLastValue(rs));
                    }

                } catch (Exception ex) {
                    logger.trace(ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }


        return attributes;
    }


    /**
     * Remove attribute if row. Happens if all samples are deleted.
     *
     * @param objectID
     * @param attribute
     */
    public void deleteMinMaxTS(long objectID, String attribute) {
        String sql = String.format("delete from %s where %s=?, and %s=?", TABLE, COLUMN_OBJECT, COLUMN_NAME);


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            ps.setLong(1, objectID);
            ps.setString(2, attribute);


            logger.debug("SQL: {}", ps);
            ps.executeUpdate();

        } catch (SQLException ex) {
            logger.error(ex);
        }

    }


    public void updateMinMaxTS(long objectID, String attribute) {

        try {
            if (!_connection.getSampleTable().hasSamples(objectID, attribute)) {
                deleteMinMaxTS(objectID, attribute);
            }//else continue
        } catch (Exception ex) {
            logger.error("Error while checking if attribute has data", ex);
        }

        String sql = String.format("insert into %s( %s,%s,%s,%s,%s) " +
                        "select min(%s), max(%s), count(*), object, attribute  " +
                        "from sample where object=? and attribute=?  " +
                        "ON DUPLICATE KEY UPDATE   %s=(select min(%s) from %s where %s=?  and %s=?),   %s=(select max(%s) " +
                        "from %s where %s=?  and %s=?), %s=(select count(*) from %s where %s=?  and %s=?)",
                TABLE, COLUMN_MIN_TS, COLUMN_MAX_TS, COLUMN_COUNT, COLUMN_OBJECT, COLUMN_NAME,
                SampleTable.COLUMN_TIMESTAMP, SampleTable.COLUMN_TIMESTAMP, COLUMN_MIN_TS,
                SampleTable.COLUMN_TIMESTAMP, SampleTable.TABLE, SampleTable.COLUMN_OBJECT,
                SampleTable.COLUMN_ATTRIBUTE, COLUMN_MAX_TS, SampleTable.COLUMN_TIMESTAMP,
                SampleTable.TABLE, SampleTable.COLUMN_OBJECT, SampleTable.COLUMN_ATTRIBUTE,
                COLUMN_COUNT, SampleTable.TABLE, SampleTable.COLUMN_OBJECT, SampleTable.COLUMN_ATTRIBUTE);


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            //insert
            ps.setLong(1, objectID);
            ps.setString(2, attribute);

            //select min
            ps.setLong(3, objectID);
            ps.setString(4, attribute);

            //select max
            ps.setLong(5, objectID);
            ps.setString(6, attribute);

            //select count
            ps.setLong(7, objectID);
            ps.setString(8, attribute);

            logger.debug("SQL: {}", ps);
            ps.executeUpdate();

        } catch (SQLException ex) {
            logger.error(ex);
        }

    }

    /**
     * @param att
     * @throws JEVisException
     * @TODO: this could be a SQL trigger but my fu**** trigger wont work
     */
    public void updateAttribute(long object, JsonAttribute att) throws JEVisException {

        String sql = String.format("insert into %s( %s,%s,%s,%s,%s,%s) Values ( ?,?,?,?,?,?)  ON DUPLICATE KEY UPDATE %s=?,%s=?,%s=?,%s=?", TABLE, COLUMN_DISPLAY_UNIT, COLUMN_INPUT_UNIT, COLUMN_DISPLAY_RATE, COLUMN_INPUT_RATE, COLUMN_OBJECT, COLUMN_NAME, COLUMN_DISPLAY_UNIT, COLUMN_INPUT_UNIT, COLUMN_DISPLAY_RATE, COLUMN_INPUT_RATE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            Gson gson = new Gson();
            JEVisUnit fallbackUnit = new JEVisUnitImp(Unit.ONE);

            if (att.getDisplayUnit() != null && att.getDisplayUnit() != null) {
                ps.setString(1, gson.toJson(att.getDisplayUnit(), JsonUnit.class));
                ps.setString(7, gson.toJson(att.getDisplayUnit(), JsonUnit.class));
            } else {
                ps.setString(1, fallbackUnit.toJSON());
                ps.setString(7, fallbackUnit.toJSON());
            }

            if (att.getInputUnit() != null && att.getInputUnit() != null) {
                ps.setString(2, gson.toJson(att.getInputUnit(), JsonUnit.class));
                ps.setString(8, gson.toJson(att.getInputUnit(), JsonUnit.class));

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
            logger.error(ex);
        }


    }
}
