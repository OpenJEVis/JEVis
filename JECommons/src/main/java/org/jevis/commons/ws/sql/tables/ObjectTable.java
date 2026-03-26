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
package org.jevis.commons.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.SQLtoJsonFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC accessor for the {@code object} table.
 * <p>
 * Provides CRUD operations for JEVis objects and helpers for recursive
 * subtree traversal.
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class ObjectTable {

    /**
     * Name of the database table managed by this class.
     */
    public final static String TABLE = "object";
    /** Column: object ID (primary key). */
    public final static String COLUMN_ID = "id";
    /** Column: display name. */
    public final static String COLUMN_NAME = "name";
    /** Column: JEVis class name. */
    public final static String COLUMN_CLASS = "type";
    /** Column: public-visibility flag. */
    public final static String COLUMN_PUBLIC = "public";
    /** Column: link target (unused in current schema). */
    public final static String COLUMN_LINK = "link";
    /** Column: soft-delete timestamp. */
    public final static String COLUMN_DELETE = "deletets";
    /** Column: i18n JSON string. */
    public final static String COLUMN_I18N = "i18n";
    /**
     * Column: group ID.
     */
    public final static String COLUMN_GROUP = "groupid";
    private static final Logger logger = LogManager.getLogger(ObjectTable.class);
    private final SQLDataSource _connection;

    /**
     * Creates an {@code ObjectTable} accessor backed by the given data source.
     *
     * @param ds the per-request SQL data source
     */
    public ObjectTable(SQLDataSource ds) {
        _connection = ds;
    }

    /**
     * Logs the current character-set configuration of the database connection
     * at DEBUG level. Intended for connection debugging only.
     */
    public void debugConnection() {
        String charVarsSQL = "show variables like 'char%'";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = _connection.getConnection().prepareStatement(charVarsSQL);

            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps.toString());
            }

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
     * Inserts a new object into the database, creates a PARENT relationship to
     * the given parent, and copies all OWNER relationships from the parent.
     *
     * @param name     the display name for the new object
     * @param jclass   the JEVis class name
     * @param parent   the ID of the parent object
     * @param isPublic whether the object is publicly visible
     * @param i18n     i18n JSON string, may be {@code null}
     * @return the newly created {@link JsonObject}
     * @throws JEVisException if the insert or relationship creation fails
     */
    public JsonObject insertObject(String name, String jclass, long parent, boolean isPublic, String i18n) throws JEVisException {
        String sql = String.format("insert into %s(%s, %s, %s, %s) values(?,?,?,?)", TABLE, COLUMN_NAME, COLUMN_CLASS, COLUMN_PUBLIC, COLUMN_I18N);


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, jclass);
            ps.setBoolean(3, isPublic);
            ps.setString(4, i18n);

            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
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

    /**
     * Updates the name, public flag, and i18n metadata of an existing object.
     *
     * @param id       the object ID to update
     * @param newname  the new display name
     * @param ispublic the new public-visibility flag
     * @param i18n     the new i18n JSON string
     * @return the updated {@link JsonObject}
     * @throws JEVisException if the update fails
     */
    public JsonObject updateObject(long id, String newname, boolean ispublic, String i18n) throws JEVisException {
        String sql = String.format("update %s set %s=?,%s=?,%s=? where %s=?", TABLE, COLUMN_NAME, COLUMN_PUBLIC, COLUMN_I18N, COLUMN_ID);
        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newname);
            ps.setBoolean(2, ispublic);
            ps.setString(3, i18n);
            ps.setLong(4, id);

            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
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

    /**
     * Retrieves a single object by ID from the database and resolves its parent
     * relationship.
     *
     * @param id the object ID
     * @return the matching {@link JsonObject}, or {@code null} if not found
     * @throws JEVisException if a database error occurs
     */
    public JsonObject getObject(Long id) throws JEVisException {
        logger.trace("getObject: {} ", id);

        String sql = String.format("select o.* from %s o where  o.%s=? limit 1 ", TABLE, COLUMN_ID);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);

            logger.trace("getObject.sql: {} ", ps);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject obj = SQLtoJsonFactory.buildObject(rs);
                List<JsonRelationship> relationships = _connection.getParentRelationships(obj.getId());
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


    /**
     * Returns all public objects in the database.
     *
     * @return a list of publicly-visible {@link JsonObject} instances
     * @throws JEVisException if the query fails
     */
    public List<JsonObject> getAllPublicObjects() throws JEVisException {
        logger.trace("getPublicObjects");

        String sql = String.format("select * from %s where %s=1", TABLE, COLUMN_PUBLIC);


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

    /**
     * Returns all objects in the database (including soft-deleted ones).
     *
     * @return a list of all {@link JsonObject} instances
     * @throws JEVisException if the query fails
     */
    public List<JsonObject> getAllObjects() throws JEVisException {

        String sql = String.format("select * from %s", TABLE);

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

    /**
     * Returns all objects that have been soft-deleted (i.e., have a non-null
     * {@code deletets} column).
     *
     * @return a list of deleted {@link JsonObject} instances
     * @throws JEVisException if the query fails
     */
    public List<JsonObject> getAllDeletedObjects() throws JEVisException {

        String sql = String.format("select * from %s where %s is not null", TABLE, COLUMN_DELETE);

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


    /**
     * Returns all objects whose JEVis class is {@code "Group"}, keyed by ID.
     *
     * @return a map from object ID to {@link JsonObject} for all group objects
     * @throws JEVisException if the query fails
     */
    public Map<Long, JsonObject> getGroupObjects() throws JEVisException {

        String sql = String.format("select * from %s where %s is null and %s=?", TABLE, COLUMN_DELETE, COLUMN_CLASS);

        Map<Long, JsonObject> objects = new ConcurrentHashMap<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "Group");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    JsonObject object = SQLtoJsonFactory.buildObject(rs);
                    objects.put(object.getId(), object);
                } catch (Exception ex) {
                    logger.error("Could not load UserGroup: {}", ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return objects;
    }

    /**
     * Recursively collects all descendant objects of {@code parentObj} into
     * {@code objs}.
     *
     * <p><b>Performance:</b> Builds a {@code childrenByParent} lookup map once
     * from the full set of PARENT-type relationships before recursing, so that
     * each recursive step is O(1) instead of re-scanning the relationship list.
     * This eliminates the O(n × depth) query pattern present in naïve
     * implementations.
     *
     * @param objs      the accumulator list; descendants are appended here
     * @param parentObj the root of the subtree to collect
     */
    public void getAllChildren(List<JsonObject> objs, JsonObject parentObj) {
        List<JsonRelationship> allParentRels = _connection.getRelationships(JEVisConstants.ObjectRelationship.PARENT);

        // Build a parent-ID → [child-IDs] map once; O(n) where n = total relationships
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (JsonRelationship rel : allParentRels) {
            childrenByParent.computeIfAbsent(rel.getTo(), k -> new ArrayList<>()).add(rel.getFrom());
        }

        collectChildren(objs, parentObj.getId(), childrenByParent);
    }

    /**
     * Internal recursive helper for {@link #getAllChildren}. Uses the
     * pre-built map for O(1) child lookups per node.
     */
    private void collectChildren(List<JsonObject> objs, long parentId, Map<Long, List<Long>> childrenByParent) {
        List<Long> childIds = childrenByParent.get(parentId);
        if (childIds == null) return;
        for (long childId : childIds) {
            try {
                JsonObject child = _connection.getObject(childId);
                if (child != null) {
                    objs.add(child);
                    collectChildren(objs, childId, childrenByParent);
                }
            } catch (Exception ex) {
                logger.trace("Could not load child {}: {}", childId, ex.getMessage());
            }
        }
    }

    /**
     * Permanently deletes the given object and all its descendants from the
     * database, including their samples and relationships.
     *
     * @param obj the object to delete
     * @return {@code true} if at least one row was deleted
     */
    public boolean deleteObjectFromDB(JsonObject obj) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteObjectFromDB: " + obj);
        }
        String sql = String.format("delete from %s where %s IN(", TABLE, COLUMN_ID);
        List<JsonObject> children = new ArrayList<>();
        children.add(obj);
        getAllChildren(children, obj);

        boolean first = true;
        for (JsonObject ch : children) {
            logger.trace("JsonObject: " + ch);
            if (first) {
                sql += "?";
                first = false;
            } else {
                sql += ",?";
            }

        }

        sql += ")";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            int raw = 1;
            for (JsonObject ch : children) {
                ps.setLong(raw, ch.getId());
                raw++;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            int count = ps.executeUpdate();
            if (count > 0) {
                List<Long> ids = new ArrayList<>();
                for (JsonObject o : children) {
                    ids.add(o.getId());
                }
                _connection.getRelationshipTable().deleteAll(ids);
                _connection.getSampleTable().deleteAllSamples(ids);
                return true;
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return false;
    }

    /**
     * Soft-deletes the given object and all its descendants by setting their
     * {@code deletets} column to the current timestamp and converting their
     * PARENT relationships to DELETED_PARENT.
     *
     * @param obj the object to mark as deleted
     * @return {@code true} if at least one row was updated
     */
    public boolean markObjectAsDeleted(JsonObject obj) {

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
                /* remove parent relationship and add delete relationship*/
                _connection.getRelationshipTable().changeType(obj.getId(), JEVisConstants.ObjectRelationship.PARENT, JEVisConstants.ObjectRelationship.DELETED_PARENT);

                return true;
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return false;

    }

    /**
     * Restores a previously soft-deleted object (and all its descendants) by
     * clearing their {@code deletets} column.
     *
     * @param obj the object to restore
     * @return {@code true} if at least one row was updated
     */
    public boolean restoreObjectAsDeleted(JsonObject obj) {

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
            ps.setNull(1, Types.TIMESTAMP);

            int raw = 2;
            for (JsonObject ch : children) {
                ps.setLong(raw, ch.getId());
                raw++;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            int count = ps.executeUpdate();
            if (count > 0) {
                return true;
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return false;

    }

}
