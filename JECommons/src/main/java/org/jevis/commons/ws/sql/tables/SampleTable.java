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
package org.jevis.commons.ws.sql.tables;

import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.PasswordHash;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.SQLtoJsonFactory;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
    private static final Logger logger = LogManager.getLogger(SampleTable.class);
    private final SQLDataSource _connection;

    public SampleTable(SQLDataSource ds) {
        _connection = ds;
    }

    public int insertSamples(long object, String attribute, int priType, List<JsonSample> samples) throws JEVisException {
        //Benchmark benchmark = new Benchmark();
        int perChunk = 100000;// careful, if value is bigger sql has a limit per transaction. 1mio is test only with small ints
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
        //benchmark.printBechmark("Imported: " + count + " samples");
//        samples.clear();
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
        String sql = String.format("INSERT IGNORE into %s(%s,%s,%s,%s,%s,%s,%s) VALUES",
                TABLE, COLUMN_OBJECT, COLUMN_ATTRIBUTE, COLUMN_TIMESTAMP, COLUMN_VALUE, COLUMN_MANID, COLUMN_NOTE, COLUMN_INSERT_TIMESTAMP);

        int count = 0;

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
        sqlWithUpdate += " ON DUPLICATE KEY UPDATE "
                + COLUMN_VALUE + "=VALUES(" + COLUMN_VALUE + "), "
                + COLUMN_NOTE + "=VALUES(" + COLUMN_NOTE + "), "
                + COLUMN_MANID + "=VALUES(" + COLUMN_MANID + ") ";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlWithUpdate)) {
            Calendar cal = Calendar.getInstance();//care tor TZ?
            long now = cal.getTimeInMillis();
            DoubleValidator dv = DoubleValidator.getInstance();

            int p = 0;
            for (JsonSample sample : samples) {

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
            logger.trace("SQL: {}", ps);
            count = ps.executeUpdate();

            _connection.getAttributeTable().updateMinMaxTS(object, attribute);

            return count;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("Invalid sql command. Error while inserting Sample ", 4231, ex);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new JEVisException("Invalid algorithm. Error while inserting Sample ", 4232, e);
        } catch (NumberFormatException e) {
            logger.error(e);
            throw new JEVisException("Invalid number format. Error while inserting Sample ", 4233, e);
        } catch (InvalidKeySpecException e) {
            logger.error(e);
            throw new JEVisException("Invalid key spec. Error while inserting Sample ", 4234, e);
        }

    }

    public boolean insertFile(long object, String att, DateTime from, JEVisFile file) throws JEVisException {
        String sql = String.format("INSERT IGNORE into %s(%s,%s,%s,%s,%s,%s) VALUES (?,?,?,?,?,?)",
                TABLE, COLUMN_OBJECT, COLUMN_ATTRIBUTE, COLUMN_TIMESTAMP, COLUMN_INSERT_TIMESTAMP, COLUMN_FILE_NAME, COLUMN_FILE);

        int count = 0;

        sql += " ON DUPLICATE KEY UPDATE "
                + COLUMN_FILE_NAME + "=VALUES(" + COLUMN_FILE_NAME + "), "
                + COLUMN_FILE + "=VALUES(" + COLUMN_FILE + ")";
        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            Calendar cal = Calendar.getInstance();//care tor TZ?
            long now = cal.getTimeInMillis();
            ps.setLong(1, object);
            ps.setString(2, att);
            ps.setTimestamp(3, new Timestamp(from.getMillis()));
            ps.setTimestamp(4, new Timestamp(now));

            ps.setString(5, file.getFilename());
            ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
            ps.setBlob(6, bis);

            logger.trace("SQL: {}", ps);
            count = ps.executeUpdate();

            _connection.getAttributeTable().updateMinMaxTS(object, att);

            return count > 0;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("Error while inserting Sample ", 4234, ex);
        }


    }

    public JEVisFile getFileSample(long object, String att, DateTime from) throws JEVisException {

        String sql = String.format("select %s,%s from %s where %s=? and %s=? %s=?",
                COLUMN_FILE, COLUMN_FILE_NAME, TABLE, COLUMN_OBJECT, COLUMN_ATTRIBUTE, COLUMN_TIMESTAMP);

        if (from != null) {
            sql += " " + COLUMN_TIMESTAMP + "=?";
        } else {
            sql += "order by " + COLUMN_TIMESTAMP + " limit 1";
        }

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            int pos = 1;

            ps.setLong(pos++, object);
            ps.setString(pos++, att);
            if (from != null) {
                ps.setTimestamp(pos++, new Timestamp(from.getMillis()));
            }

            logger.trace("SQL: {}", ps);
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
        } catch (SQLException ex) {
            logger.error(ex);
        }
        return null;
    }

    public List<JsonSample> getSamples(long object, String att, DateTime from, DateTime until, long limit) throws JEVisException {
        logger.debug("getSamples: {}:{} from: {} until: {}", object, att, from, until);
        List<JsonSample> samples = new ArrayList<>();

        String sql = String.format("select * from %s where %s=? and %s=?",
                TABLE, COLUMN_OBJECT, COLUMN_ATTRIBUTE);

        if (from != null) {
            sql += " and " + COLUMN_TIMESTAMP + ">=?";
        }
        if (until != null) {
            sql += " and " + COLUMN_TIMESTAMP + "<=?";
        }
        sql += " order by " + COLUMN_TIMESTAMP + " limit " + limit;

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)) {
            //ps.setFetchDirection(ResultSet.FETCH_FORWARD);
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
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    samples.add(SQLtoJsonFactory.buildSample(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return samples;

    }

    public boolean deleteAllSamples(long object, String att) {
        String sql = String.format("delete from %s where %s=? and %s=?", TABLE, COLUMN_ATTRIBUTE, COLUMN_OBJECT);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setString(1, att);
            ps.setLong(2, object);
            logger.debug("SQL: {}", ps);
            ps.executeUpdate();
            _connection.getAttributeTable().updateMinMaxTS(object, att);
            return true;
        } catch (SQLException ex) {
            logger.error(ex);
            return false;
        }

    }

    public boolean deleteAllSamples(List<Long> object) {
        String sql = String.format("delete from %s where %s=?", TABLE, COLUMN_OBJECT);

        object.forEach(aLong -> {
            try {
                try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
                    ps.setLong(1, aLong);
                    logger.debug("SQL: {}", ps);

                    ps.executeUpdate();
                } catch (SQLException ex) {
                    logger.error(ex);
                }
            } catch (Exception ex) {
                logger.error("Error while deleting Sample for {}", aLong, ex, ex);
            }
        });

        return true;


    }

    public boolean deleteSamples(long object, String att, DateTime from, DateTime until) {
        String sql = String.format("delete from %s where %s=? and %s=?", TABLE, COLUMN_ATTRIBUTE, COLUMN_OBJECT);


        if (from != null && until == null) {
            sql += " and " + COLUMN_TIMESTAMP + ">=?";
        }

        if (from != null && until != null) {
            sql += " and " + COLUMN_TIMESTAMP + ">=?"
                    + " and " + COLUMN_TIMESTAMP + "<=?";
        }

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
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

            logger.debug("SQL: {}", ps);
            ps.execute();
            _connection.getAttributeTable().updateMinMaxTS(object, att);
            return true;
        } catch (SQLException ex) {
            logger.error(ex);
            return false;
        }

    }

    public boolean deleteOldLogging() {
        String sql = String.format("delete from %s " +
                        "where attribute=\"%s\" and timestamp<=DATE(NOW()-INTERVAL 1 YEAR) " +
                        "and object in (select ID from object where object.type=\"%s\");"
                , TABLE, "Activities", "User");


        //from sample where attribute="Activities" and timestamp<=DATE(NOW()-INTERVAL 1 YEAR) and object in (select ID from object where object.type="User");

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            logger.error("SQL: {}", ps);
            ps.execute();
            //_connection.getAttributeTable().updateMinMaxTS(object, att);
            return true;
        } catch (SQLException ex) {
            logger.error(ex);
            return false;
        }

    }

    public List<JsonSample> getAll(long object, String att) throws SQLException {
        List<JsonSample> samples = new ArrayList<>();

        String sql = String.format("select * from %s where %s=? and %s=? order by %s",
                TABLE, COLUMN_OBJECT, COLUMN_ATTRIBUTE, COLUMN_TIMESTAMP);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setString(2, att);

            logger.trace("SQL: {}", ps);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    samples.add(SQLtoJsonFactory.buildSample(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }


        return samples;
    }


    public JsonSample getLatest(long object, String att) throws JEVisException {
        String sql = String.format("select * from %s where %s=? and %s=? order by %s DESC limit 1",
                TABLE, COLUMN_OBJECT, COLUMN_ATTRIBUTE, COLUMN_TIMESTAMP);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setString(2, att);

            logger.trace("SQL: {}", ps);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                return SQLtoJsonFactory.buildSample(rs);
            }
            return null;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("Error while inserting object", 234234, ex);//ToDo real number
        }

    }
}
