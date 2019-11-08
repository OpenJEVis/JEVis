/**
 * Copyright (C) 2009 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-SQL.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class ObjectTable {

    public final static String TABLE = "object";
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_CLASS = "type";
    public final static String COLUMN_PUBLIC = "public";
    public final static String COLUMN_LINK = "link";
    public final static String COLUMN_DELETE = "deletets";
    public final static String COLUMN_GROUP = "groupid";//remove ID from name
    private static final Logger logger = LogManager.getLogger(ObjectTable.class);
    private SQLDataSource _connection;

    public ObjectTable(SQLDataSource ds) {
        _connection = ds;
    }

    /**
     * Debug the connection settings
     */
    public void debugConnection() {
        String charVarsSQL = "show variables like 'char%'";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = _connection.getConnection().prepareStatement(charVarsSQL);

            logger.debug("SQL: {}", ps.toString());

            rs = ps.executeQuery();

            while (rs.next()) {
                logger.error("-- {}: {}", rs.getString(1), rs.getString(2));
            }

        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    logger.debug("Error while closing DB connection: {}. ", ex);
                }
            }
        }
    }

    /**
     * @param name
     * @param jclass
     * @param parent
     * @return
     * @throws JEVisException
     */
    public JsonObject insertObject(String name, String jclass, long parent, boolean isPublic) throws JEVisException {
        String sql = String.format("insert into %s(%s,%s, %s) values(?,?,?)", TABLE, COLUMN_NAME, COLUMN_CLASS, COLUMN_PUBLIC);


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, jclass);
            ps.setBoolean(3, isPublic);

            logger.trace("SQL: {}", ps);
            int count = ps.executeUpdate();
            if (count == 1) {
                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {

                    for (JsonRelationship rel : _connection.getRelationshipTable().selectForObject(parent)) {

                        if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && rel.getFrom() == parent) {
                            _connection.getRelationshipTable().insert(rs.getLong(1), rel.getTo(), JEVisConstants.ObjectRelationship.OWNER);
                        }
                    }

                    int relType = JEVisConstants.ObjectRelationship.PARENT;//not very save
                    _connection.getRelationshipTable().insert(rs.getLong(1), parent, relType);

                    return getObject(rs.getLong(1));
                } else {
                    throw new JEVisException("Error selecting inserted object", 234235);
                }
            } else {
                throw new JEVisException("Error while inserting object", 234236);
            }
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("Error while inserting object", 234237);
        }


    }

    public JsonObject updateObject(long id, String newname, boolean ispublic) throws JEVisException {
        String sql = String.format("update %s set %s=?,%s=? where %s=?", TABLE, COLUMN_NAME, COLUMN_PUBLIC, COLUMN_ID);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newname);
            ps.setBoolean(2, ispublic);
            ps.setLong(3, id);

            logger.trace("SQL: {}", ps);
            int count = ps.executeUpdate();
            if (count == 1) {
                return getObject(id);
            } else {
                throw new JEVisException("Error while updating object", 234236);//ToDo real number
            }
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("Error while updating object", 234234, ex);//ToDo real number
        }


    }
    
    public JsonObject getObject(Long id) throws JEVisException {
        logger.trace("getObject: {} ", id);

        String sql = String.format("select o.* from %s o where  o.%s=? and o.%s is null limit 1 ", TABLE, COLUMN_ID, COLUMN_DELETE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);

            logger.trace("getObject.sql: {} ", ps);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject obj = SQLtoJsonFactory.buildObject(rs);
                List<JsonRelationship> relationships = _connection.getRelationships(obj.getId());
                relationships.forEach(jsonRelationship -> {
                    if (jsonRelationship.getType() == JEVisConstants.ObjectRelationship.PARENT && jsonRelationship.getFrom() == obj.getId()) {
                        //child -> parent
                        obj.setParent(jsonRelationship.getTo());
                    }
                });

                return obj;
            }
        } catch (SQLException ex) {
            logger.error(ex);

        }
        return null;
    }


    public List<JsonObject> getAllPublicObjects() throws JEVisException {
        logger.trace("getPublicObjects");

        String sql = String.format("select * from %s where %s is null and %s=1", TABLE, COLUMN_DELETE, COLUMN_PUBLIC);


        List<JsonObject> objects = new ArrayList<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    objects.add(SQLtoJsonFactory.buildObject(rs));
                } catch (Exception ex) {
                    logger.error("Cound not load Object: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return objects;
    }

    public List<JsonObject> getAllObjects() throws JEVisException {

        String sql = String.format("select * from %s where %s is null", TABLE, COLUMN_DELETE);

        List<JsonObject> objects = new ArrayList<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    objects.add(SQLtoJsonFactory.buildObject(rs));
                } catch (Exception ex) {
                    logger.error("Could not load Object: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return objects;
    }

    public Map<Long,JsonObject> getGroupObjects() throws JEVisException {

        String sql = String.format("select * from %s where %s is null and %s=?", TABLE, COLUMN_DELETE,COLUMN_CLASS);

        Map<Long,JsonObject> objects = new ConcurrentHashMap<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setString(1,"Group");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    JsonObject object = SQLtoJsonFactory.buildObject(rs);
                    objects.put(object.getId(),object);
                } catch (Exception ex) {
                    logger.error("Could not load UserGroup: {}" ,ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return objects;
    }

    public void getAllChildren(List<JsonObject> objs, JsonObject parentObj) {

        List<JsonRelationship> allObjects = _connection.getRelationships(JEVisConstants.ObjectRelationship.PARENT);

        for (JsonRelationship ob : allObjects) {

            try {
                //child -> parent
                if (ob.getTo() == parentObj.getId()) {
                    JsonObject child = _connection.getObject(ob.getFrom());

                    if (child != null) {
                        objs.add(child);
                        getAllChildren(objs, child);
                    }

                }
            } catch (Exception ex) {
            }
        }

    }

    public boolean deleteObject(JsonObject obj) {
        String sql = String.format("update %s set %s=? where %s IN(", TABLE, COLUMN_DELETE, COLUMN_ID);

        List<JsonObject> children = new ArrayList<>();
        children.add(obj);
        getAllChildren(children, obj);

        boolean first = true;
        for (JsonObject ch : children) {
            if (first) {
                sql += "?";
                first = false;
            } else {
                sql += ",?";
            }

        }

        sql += ")";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            Calendar now = new GregorianCalendar();
            ps.setTimestamp(1, new Timestamp(now.getTimeInMillis()));

            int raw = 2;
            for (JsonObject ch : children) {
                ps.setLong(raw, ch.getId());
                raw++;
            }

            logger.trace("SQL: {}", ps);
            int count = ps.executeUpdate();
            if (count > 0) {
                List<Long> ids = new ArrayList<>();
                for (JsonObject o : children) {
                    ids.add(o.getId());
                }
                _connection.getRelationshipTable().deleteAll(ids);

                return true;
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return false;

    }

}
