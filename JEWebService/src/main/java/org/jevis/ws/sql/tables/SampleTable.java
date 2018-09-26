/**
 * Copyright (C) 2009 - 2013 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.ws.sql.tables;

import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.ws.sql.PasswordHash;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class SampleTable {

    public final static String TABLE = "sample";
    public final static String COLUMN_VALUE = "value";
    public final static String COLUMN_ATTRIBUTE = "attribute";
    public final static String COLUMN_TIMESTAMP = "timestamp";//rename into ts?
    public final static String COLUMN_INSERT_TIMESTAMP = "insertts";
    public final static String COLUMN_OBJECT = "object";
    public final static String COLUMN_MANID = "manid";
    public final static String COLUMN_NOTE = "note";
    public final static String COLUMN_FILE = "file";
    public final static String COLUMN_FILE_NAME = "filename";
    private SQLDataSource _connection;
    private Logger logger = LogManager.getLogger(SampleTable.class);

    public SampleTable(SQLDataSource ds) {
        _connection = ds;
    }

    public int insertSamples(long object, String attribute, int priType, List<JsonSample> samples) throws JEVisException {
        int perChunk = 100000;// care if value is bigger sql has a limit per transaktion. 1mio is teste only with small ints
        int count = 0;
        for (int i = 0; i < samples.size(); i += perChunk) {
            if ((i + perChunk) < samples.size()) {
                List<JsonSample> chunk = samples.subList(i, i + perChunk);
                count += insertSamplesChunk(object, attribute, priType, chunk);
            } else {
                List<JsonSample> chunk = samples.subList(i, samples.size());
                count += insertSamplesChunk(object, attribute, priType, chunk);
                break;
            }
        }
//        _ds.getAttributeTable().updateAttributeTS(attribute);

        return count;
    }

    /**
     * TODO: batch the insert because mysql has a limit for a request
     * "max_allowed_packet=32M"
     *
     * @param object
     * @param attribute
     * @param samples
     */
    private int insertSamplesChunk(long object, String attribute, int priType, List<JsonSample> samples) throws JEVisException {
        String sql = "INSERT IGNORE into " + TABLE
                + "(" + COLUMN_OBJECT + "," + COLUMN_ATTRIBUTE + "," + COLUMN_TIMESTAMP
                + "," + COLUMN_VALUE + "," + COLUMN_MANID + "," + COLUMN_NOTE + "," + COLUMN_INSERT_TIMESTAMP
                + ") VALUES";

//        logger.info("SQL raw: "+sql);
        PreparedStatement ps = null;
        int count = 0;

        try {
            StringBuilder build = new StringBuilder(sql);

            for (int i = 0; i < samples.size(); i++) {
                build.append("(?,?,?,?,?,?,?)");
                if (i < samples.size() - 1) {
                    build.append(", ");
                } else {
//                    build.append(";");
                }
            }

            String sqlWithUpdate = build.toString();
            sqlWithUpdate = sqlWithUpdate += " ON DUPLICATE KEY UPDATE "
                    + COLUMN_VALUE + "=VALUES(" + COLUMN_VALUE + "), "
                    + COLUMN_NOTE + "=VALUES(" + COLUMN_NOTE + "), "
                    + COLUMN_MANID + "=VALUES(" + COLUMN_MANID + ") ";

            ps = _connection.getConnection().prepareStatement(sqlWithUpdate);

            Calendar cal = Calendar.getInstance();//care tor TZ?
            long now = cal.getTimeInMillis();
            DoubleValidator dv = DoubleValidator.getInstance();

            int p = 0;
            for (int i = 0; i < samples.size(); i++) {
                JsonSample sample = samples.get(i);
                ps.setLong(++p, object);
                ps.setString(++p, attribute);
                DateTime ts = JsonFactory.sampleDTF.parseDateTime(sample.getTs());
                ps.setTimestamp(++p, new Timestamp(ts.getMillis()));
                switch (priType) {
                    case JEVisConstants.PrimitiveType.PASSWORD_PBKDF2:
                        //Passwords will be stored as Saled Hash
                        ps.setString(++p, PasswordHash.createHash(sample.getValue()));
                        break;
//                    case JEVisConstants.PrimitiveType.FILE:
//                        ps.setNull(++p, Types.VARCHAR);
//                        break;
                    case JEVisConstants.PrimitiveType.BOOLEAN:
                        if (sample.getValue().equals("1") || sample.getValue().equals("2")) {
                            ps.setBoolean(++p, "1".equals(sample.getValue()));
                        } else {
                            ps.setBoolean(++p, Boolean.valueOf(sample.getValue()));
                        }
                        break;
                    case JEVisConstants.PrimitiveType.SELECTION:
                        ps.setLong(++p, Long.valueOf(sample.getValue()));
                        break;
                    case JEVisConstants.PrimitiveType.MULTI_SELECTION:
                        ps.setString(++p, sample.getValue());
                        break;
                    case JEVisConstants.PrimitiveType.LONG:
                        ps.setLong(++p, dv.validate(sample.getValue(), Locale.US).longValue());
                        break;
                    case JEVisConstants.PrimitiveType.DOUBLE:
                        ps.setDouble(++p, dv.validate(sample.getValue(), Locale.US));
                        break;
                    default:
                        ps.setString(++p, sample.getValue());
                        break;
                }

//                ps.setString(++p, sample.getManipulation().toString());
                ps.setNull(++p, Types.INTEGER);
                ps.setString(++p, sample.getNote());
                ps.setTimestamp(++p, new Timestamp(now));

            }
//            logger.info("SamplDB.putSample SQL: \n" + ps);
            logger.error("SQL: {}", ps);
            _connection.addQuery("Sample.insert()", ps.toString());
            count = ps.executeUpdate();

            _connection.getAttributeTable().updateMinMaxTS(object, attribute);

            return count;
        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while inserting Sample ", 4234, ex);
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

    public boolean insertFile(long object, String att, DateTime from, JEVisFile file) throws JEVisException {
        String sql = "INSERT IGNORE into " + TABLE
                + "(" + COLUMN_OBJECT + "," + COLUMN_ATTRIBUTE + "," + COLUMN_TIMESTAMP + "," + COLUMN_INSERT_TIMESTAMP
                + "," + COLUMN_FILE_NAME + "," + COLUMN_FILE
                + ") VALUES (?,?,?,?,?,?)";

        PreparedStatement ps = null;
        int count = 0;

        try {

            sql = sql += " ON DUPLICATE KEY UPDATE "
                    + COLUMN_FILE_NAME + "=VALUES(" + COLUMN_FILE_NAME + "), "
                    + COLUMN_FILE + "=VALUES(" + COLUMN_FILE + ")";

            ps = _connection.getConnection().prepareStatement(sql);

            Calendar cal = Calendar.getInstance();//care tor TZ?
            long now = cal.getTimeInMillis();
            ps.setLong(1, object);
            ps.setString(2, att);
            ps.setTimestamp(3, new Timestamp(from.getMillis()));
            ps.setTimestamp(4, new Timestamp(now));

            ps.setString(5, file.getFilename());
            ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
            ps.setBlob(6, bis);

//            logger.info("SamplDB.putSample SQL: \n" + ps);
            logger.trace("SQL: {}", ps);
            _connection.addQuery("Sample.setFile()", ps.toString());
            count = ps.executeUpdate();

            _connection.getAttributeTable().updateMinMaxTS(object, att);

            return count > 0;
        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while inserting Sample ", 4234, ex);
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

    public JEVisFile getFileSample(long object, String att, DateTime from) throws JEVisException {

        String sql = "select " + COLUMN_FILE + "," + COLUMN_FILE_NAME + " from " + TABLE
                + " where " + COLUMN_OBJECT + "=?"
                + " and " + COLUMN_ATTRIBUTE + "=?"
                + " " + COLUMN_TIMESTAMP + "=?";

        PreparedStatement ps = null;

        try {

            if (from != null) {
                sql += " " + COLUMN_TIMESTAMP + "=?";
            } else {
                sql += "order by " + COLUMN_TIMESTAMP + " limit 1";
            }

            ps = _connection.getConnection().prepareStatement(sql);
            int pos = 1;

            ps.setLong(pos++, object);
            ps.setString(pos++, att);
            if (from != null) {
                ps.setTimestamp(pos++, new Timestamp(from.getMillis()));
            }

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Sample.getFile()", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getBytes(SampleTable.COLUMN_FILE) != null) {
                    JEVisFile jFile = new JEVisFileImp();

                    String _filename = rs.getString(SampleTable.COLUMN_FILE_NAME);

                    Blob fileBlob = rs.getBlob(SampleTable.COLUMN_FILE);
                    byte[] _fileBytes = fileBlob.getBytes(1, (int) fileBlob.length());
                    fileBlob.free();

                    jFile.setBytes(_fileBytes);
                    jFile.setFilename(_filename);
                    return jFile;
                }

            }

        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while select samples", 723547, ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return null;
    }

    public List<JsonSample> getSamples(long object, String att, DateTime from, DateTime until, long limit) throws JEVisException {
        List<JsonSample> samples = new ArrayList<>();

        String sql = "select * from " + TABLE
                + " where " + COLUMN_OBJECT + "=?"
                + " and " + COLUMN_ATTRIBUTE + "=?";

        if (from != null) {
            sql += " and " + COLUMN_TIMESTAMP + ">=?";
        }
        if (until != null) {
            sql += " and " + COLUMN_TIMESTAMP + "<=?";
        }
        sql += " order by " + COLUMN_TIMESTAMP + " limit " + limit;

        PreparedStatement ps = null;

        try {

            ps = _connection.getConnection().prepareStatement(sql);
            int pos = 1;

            ps.setLong(pos++, object);
            ps.setString(pos++, att);
            if (from != null) {
                ps.setTimestamp(pos++, new Timestamp(from.getMillis()));
            }
            if (until != null) {
                ps.setTimestamp(pos++, new Timestamp(until.getMillis()));
            }

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Sample.get()", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                samples.add(SQLtoJsonFactory.buildSample(rs));
            }

        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while select samples", 723547, ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return samples;
    }

    public boolean deleteAllSamples(long object, String att) {
        String sql = "delete from " + TABLE
                + " where " + COLUMN_ATTRIBUTE + "=?"
                + " and " + COLUMN_OBJECT + "=?";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setString(1, att);
            ps.setLong(2, object);
            logger.trace("SQL: {}", ps);
            _connection.addQuery("Sample.deleteAll()", ps.toString());
            if (ps.executeUpdate() > 0) {
                _connection.getAttributeTable().updateMinMaxTS(object, att);
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            logger.error(ex);
            //TODO throw JEVisExeption?!
            return false;
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

    public boolean deleteSamples(long object, String att, DateTime from, DateTime until) {
        String sql = "delete from " + TABLE
                + " where " + COLUMN_ATTRIBUTE + "=?"
                + " and " + COLUMN_OBJECT + "=?";


        if (from != null && until == null) {
            sql += " and " + COLUMN_TIMESTAMP + ">=?";
        }

        if (from != null && until != null) {
            sql += " and " + COLUMN_TIMESTAMP + ">=?"
                    + " and " + COLUMN_TIMESTAMP + "<=?";
        }

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setString(1, att);
            ps.setLong(2, object);

            int i = 3;
            if (from != null) {
                ps.setTimestamp(i, new Timestamp(from.getMillis()));
                i++;
            }
            if (until != null) {
                ps.setTimestamp(i, new Timestamp(until.getMillis()));
            }

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Sample.delete(fom,to)", ps.toString());
            if (ps.executeUpdate() > 0) {
                _connection.getAttributeTable().updateMinMaxTS(object, att);
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            logger.error(ex);
            //TODO throw JEVisExeption?!
            return false;
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

    public List<JsonSample> getAll(long object, String att) throws SQLException, JEVisException {
//        logger.info("SampleTable.getAll");
        List<JsonSample> samples = new ArrayList<>();

        String sql = "select * from " + TABLE
                + " where " + COLUMN_OBJECT + "=?"
                + " and " + COLUMN_ATTRIBUTE + "=?"
                + " order by " + COLUMN_TIMESTAMP;

        PreparedStatement ps = _connection.getConnection().prepareStatement(sql);
        ps.setLong(1, object);
        ps.setString(2, att);

        logger.trace("SQL: {}", ps);
        _connection.addQuery("Sample.getAll()", ps.toString());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            samples.add(SQLtoJsonFactory.buildSample(rs));
        }

        return samples;
    }

    public JsonSample getLatest(long object, String att) throws JEVisException {
        JEVisSample sample = null;
        PreparedStatement ps = null;
        try {
            String sql = "select * from " + TABLE
                    + " where " + COLUMN_OBJECT + "=?"
                    + " and " + COLUMN_ATTRIBUTE + "=?"
                    + " order by " + COLUMN_TIMESTAMP + " DESC"
                    + " limit 1";

            ps = _connection.getConnection().prepareStatement(sql);
            ps.setLong(1, object);
            ps.setString(2, att);
//        ps.setTimestamp(3, new Timestamp(att.getTimestampFromLastSample().getMillis()));

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Sample.getLastes()", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                return SQLtoJsonFactory.buildSample(rs);
            }
            return null;
        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while inserting object", 234234, ex);//ToDo real number
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    logger.debug("Error while closing DB connection: {}. ", ex);
                }
            }
        }
    }
}
