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
    //    public final static String COLUMN_PERIOD = "period";//depricated
//    public final static String COLUMN_UNIT = "unit";//depricated
    public final static String COLUMN_COUNT = "samplecount";
    public final static String COLUMN_INPUT_UNIT = "inputunit";
    //    public final static String COLUMN_ALT_SYMBOL = "altsymbol";
    public final static String COLUMN_INPUT_RATE = "inputrate";
    public final static String COLUMN_DISPLAY_UNIT = "displayunit";
    public final static String COLUMN_DISPLAY_RATE = "displayrate";
    public final static String COLUMN_OPTION = "opt";//option and options are already sql keywords
    private static final Logger logger = LogManager.getLogger(AttributeTable.class);
    private final SQLDataSource _connection;

    public AttributeTable(SQLDataSource ds) {
        _connection = ds;
    }

    //TODO: try-catch-finally
    public void insert(JEVisType type, JEVisObject obj) {
//        logger.info("AttributeTable.insert");
        String sql = "insert into " + TABLE
                + " (" + COLUMN_OBJECT + "," + COLUMN_NAME
                + "," + COLUMN_DISPLAY_UNIT + "," + COLUMN_INPUT_UNIT
                + ") values(?,?,?,?)";

        try {
            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);

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

            _connection.addQuery("Attribute.insert()", ps.toString());
            int count = ps.executeUpdate();

        } catch (Exception ex) {
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

        String sql = "select o.type,a.*,s.*"
                + " FROM attribute a"
                + " left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts )"
                + " left join object o on (o.id=a.object)"
                + " where a.object=?;";


        try {
            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);
            ps.setLong(1, object);

//            logger.info("SQL: " + ps);
            logger.trace("SQL {}", ps);
            _connection.addQuery("Attribute.get(long)", ps.toString());
            //logger.info("SQL: " + ps);
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
        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while fetching Attributes ", 85675, ex);

        }
//        Collections.sort(attributes);

        return attributes;
    }

    public List<JsonAttribute> getAllAttributes() throws JEVisException {
        logger.trace("getAllAttributes ");
        List<JsonAttribute> attributes = new ArrayList<>();

        String sql = "select o.type,a.*,s.*"
                + " FROM attribute a"
                + " left join sample s on(s.object=a.object and s.attribute=a.name and s.timestamp=a.maxts )"
                + " left join object o on (o.id=a.object)";


        try {
            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);

//            logger.info("SQL: " + ps);
            logger.trace("SQL {}", ps);
            _connection.addQuery("Attribute.get(long)", ps.toString());
            //logger.info("SQL: " + ps);
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
        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while fetching Attributes ", 85675, ex);

        }
//        Collections.sort(attributes);

        return attributes;
    }


    /**
     * Remove attribute if row. Happens if all samples are deleted.
     *
     * @param objectID
     * @param attribute
     */
    public void delteMinMaxTS(long objectID, String attribute) {
        String sql = "" +
                "delete from " + TABLE
                + " where " + COLUMN_OBJECT + "=?,"
                + " and " + COLUMN_NAME + "=?";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);

            //insert
            ps.setLong(1, objectID);
            ps.setString(2, attribute);


            logger.debug("SQL: {}", ps);
            ps.executeUpdate();

        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
    }


    public void updateMinMaxTS(long objectID, String attribute) {

        try {
            if (!_connection.getSampleTable().hasSamples(objectID, attribute)) {
                delteMinMaxTS(objectID, attribute);
            }//else continue
        } catch (Exception ex) {
            logger.error("Error while checking if attribute has data", ex);
        }

        /* Possible trigger to haldle the update on the DB side but i fear the performace for an "for each"+"3 sub selects" and big inserts(100k+ rows)
        --
        DELIMITER $$

        CREATE TRIGGER `sample_after_insert` AFTER INSERT on `sample`
        for each row
        BEGIN
                insert into attribute (mints,maxts,samplecount,name,object)
                        select min(timestamp), max(timestamp), count(*),attribute,object from sample where object=new.object and attribute=new.attribute
                on DUPLICATE KEY UPDATE
                        mints=(select min(timestamp) from sample where object=new.object and attribute=new.attribute),
                        maxts=(select max(timestamp) from sample where object=new.object and attribute=new.attribute),
                        samplecount=(select count(*) from sample where object=new.object and attribute=new.attribute);
        END
        $$
        --
         */
        String sql = "insert into " + TABLE
                + "( "
                + COLUMN_MIN_TS + ","
                + COLUMN_MAX_TS + ","
                + COLUMN_COUNT + ","
                + COLUMN_OBJECT + ","
                + COLUMN_NAME
                + ")"
                + " select min(" + SampleTable.COLUMN_TIMESTAMP + "), max(" + SampleTable.COLUMN_TIMESTAMP + "), count(*), object, attribute "
                + " from sample where object=? and attribute=? "
                + " ON DUPLICATE KEY UPDATE"
                + "   " + COLUMN_MIN_TS + "=(select min(" + SampleTable.COLUMN_TIMESTAMP + ") from " + SampleTable.TABLE + " where " + SampleTable.COLUMN_OBJECT + "=?  and " + SampleTable.COLUMN_ATTRIBUTE + "=?),"
                + "   " + COLUMN_MAX_TS + "=(select max(" + SampleTable.COLUMN_TIMESTAMP + ") from " + SampleTable.TABLE + " where " + SampleTable.COLUMN_OBJECT + "=?  and " + SampleTable.COLUMN_ATTRIBUTE + "=?),"
                + "   " + COLUMN_COUNT + "=(select count(*) from " + SampleTable.TABLE + " where " + SampleTable.COLUMN_OBJECT + "=?  and " + SampleTable.COLUMN_ATTRIBUTE + "=?)";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);

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
            _connection.addQuery("Attribute.updateMinMax()", ps.toString());
            ps.executeUpdate();

        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }

    }

    /**
     * @param att
     * @throws JEVisException
     * @TODO: this could be a SQL trigger but my fu**** trigger wont work
     */
    public void updateAttribute(long object, JsonAttribute att) throws JEVisException {

        /*
        INSERT INTO AggregatedData (datenum,Timestamp)
            VALUES ("734152.979166667","2010-01-14 23:30:00.000")
            ON DUPLICATE KEY UPDATE
            Timestamp=VALUES(Timestamp)

         */
        String sql = "insert into " + TABLE
                + "( "
                + COLUMN_DISPLAY_UNIT + ","
                + COLUMN_INPUT_UNIT + ","
                + COLUMN_DISPLAY_RATE + ","
                + COLUMN_INPUT_RATE + ","
                + COLUMN_OBJECT + ","
                + COLUMN_NAME
                + ")"
                + " Values ( ?,?,?,?,?,?) "
                + " ON DUPLICATE KEY UPDATE "
                + COLUMN_DISPLAY_UNIT + "=?,"
                + COLUMN_INPUT_UNIT + "=?,"
                + COLUMN_DISPLAY_RATE + "=?,"
                + COLUMN_INPUT_RATE + "=?";

        PreparedStatement ps = null;
        Gson gson = new Gson();
        try {
            ps = _connection.getConnection().prepareStatement(sql);

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
            _connection.addQuery("Attribute.update()", ps.toString());

            ps.executeUpdate();

        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while updateing attribute ", 4233, ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }

    }
}
