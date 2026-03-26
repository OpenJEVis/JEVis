/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
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
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.SQLtoJsonFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * JDBC accessor for the {@code relationship} table.
 * <p>
 * Manages the directed relationships between JEVis objects, including
 * PARENT, OWNER, MEMBER, and ROLE relationship types.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RelationshipTable {

    /**
     * Name of the database table managed by this class.
     */
    public final static String TABLE = "relationship";
    /** Column: source object ID. */
    public final static String COLUMN_START = "startobject";
    /** Column: target object ID. */
    public final static String COLUMN_END = "endobject";
    /** Column: relationship type constant. */
    public final static String COLUMN_TYPE = "relationtype";
    private static final Logger logger = LogManager.getLogger(RelationshipTable.class);
    private final SQLDataSource _connection;

    /**
     * Creates a {@code RelationshipTable} accessor backed by the given data source.
     *
     * @param ds the per-request SQL data source
     */
    public RelationshipTable(SQLDataSource ds) {
        _connection = ds;
    }

    /**
     * Returns all relationships of the given type.
     *
     * @param type the relationship type constant (see {@link JEVisConstants.ObjectRelationship})
     * @return a list of matching {@link JsonRelationship} instances
     */
    public List<JsonRelationship> selectByType(int type) {
        String sql = String.format("select * from %s where %s=?", TABLE, COLUMN_TYPE);


        List<JsonRelationship> relations = new LinkedList<>();
        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, type);

            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all relationships where the given object ID appears as either
     * the source or the target.
     *
     * @param id the JEVis object ID
     * @return a list of relationships involving that object
     */
    public List<JsonRelationship> selectForObject(long id) {
        String sql = String.format("select * from %s where %s=?  or %s=?", TABLE, COLUMN_START, COLUMN_END);

        List<JsonRelationship> relations = new LinkedList<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);

            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Inserts a new relationship row for the given start/end/type triple.
     *
     * @param start the source object ID
     * @param end   the target object ID
     * @param type  the relationship type constant
     * @return the newly created {@link JsonRelationship}
     * @throws JEVisException if the insert fails
     */
    public JsonRelationship insert(long start, long end, int type) throws JEVisException {

        String sql = String.format("insert into %s (%s,%s,%s) values (?,?,?)", TABLE, COLUMN_START, COLUMN_END, COLUMN_TYPE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, start);
            ps.setLong(2, end);
            ps.setInt(3, type);
            logger.trace("SQL: {}", ps);
            int count = ps.executeUpdate();
            if (count == 1) {
                JsonRelationship json = new JsonRelationship();
                json.setFrom(start);
                json.setTo(end);
                json.setType(type);

                return json;
            } else {
                throw new JEVisException("Could not create the relationship", 1964823);
            }
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("Could not create the relationship", 1964824, ex);
        }
    }

    /**
     * Deletes the relationship described by the given {@link JsonRelationship}.
     *
     * @param rel the relationship to delete
     * @return {@code true} if the row was deleted (always {@code true})
     */
    public boolean delete(JsonRelationship rel) {
        return delete(rel.getFrom(), rel.getTo(), rel.getType());
    }


    /**
     * Changes the type of a relationship identified by its start object and
     * old type.
     *
     * @param start   the source object ID
     * @param oldType the current relationship type constant
     * @param newType the new relationship type constant
     * @return {@code true} if the update succeeded
     */
    public boolean changeType(long start, int oldType, int newType) {
        String sql = String.format("update %s set %s=? where %s=? and %s=?", TABLE, COLUMN_TYPE, COLUMN_START, COLUMN_TYPE);
        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, newType);
            ps.setLong(2, start);
            ps.setInt(3, oldType);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.error(ex);
            return false;
        }
    }

    /**
     * Deletes a specific relationship row identified by its start/end/type triple.
     *
     * @param start the source object ID
     * @param end   the target object ID
     * @param type  the relationship type constant
     * @return {@code true} always (delete is treated as idempotent)
     */
    public boolean delete(long start, long end, int type) {

        String sql = String.format("delete from %s where %s=? and %s=? and %s=?", TABLE, COLUMN_START, COLUMN_END, COLUMN_TYPE);


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, start);
            ps.setLong(2, end);
            ps.setInt(3, type);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ps.executeUpdate();
            return true; //delete is always true
        } catch (SQLException ex) {
            logger.error(ex);
            return false;
        }
    }


    /**
     * Deletes all relationships where the source or target object ID is in the
     * given list. Used when permanently deleting a subtree of objects.
     *
     * @param ids the object IDs whose relationships should be removed
     * @return {@code true} if the statement executed without error
     */
    public boolean deleteAll(List<Long> ids) {
        String in = " IN(";
        for (int i = 0; i < ids.size(); i++) {
            in += ids.get(i);
            if (i != ids.size() - 1) {
                in += ",";
            }
        }
        in += ")";

        String sql = String.format("delete from %s where %s%s or %s%s", TABLE, COLUMN_START, in, COLUMN_END, in);


        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }

            ps.executeUpdate();
            return true;


        } catch (SQLException ex) {
            logger.error(ex);
            return false;
        }

    }

    /**
     * Returns all OWNER-type relationships where the given object is the source.
     *
     * @param object the source object ID
     * @return a list of ownership relationships
     */
    public List<JsonRelationship> getGroupOwnerObject(long object) {
        List<JsonRelationship> relations = new LinkedList<>();

        String sql = String.format("select * from %s where %s=? and %s=?", TABLE, COLUMN_START, COLUMN_TYPE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setInt(2, JEVisConstants.ObjectRelationship.OWNER);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all PARENT-type relationships where the given object is the source.
     *
     * @param object the source object ID (child side)
     * @return a list of parent relationships
     */
    public List<JsonRelationship> getParentObject(long object) {
        List<JsonRelationship> relations = new LinkedList<>();

        String sql = String.format("select * from %s where %s=? and %s=?", TABLE, COLUMN_START, COLUMN_TYPE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setInt(2, JEVisConstants.ObjectRelationship.PARENT);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all relationships of the given type that involve the given object
     * (either as source or target).
     *
     * @param object the object ID
     * @param type   the relationship type constant
     * @return a list of matching relationships
     */
    public List<JsonRelationship> getAllForObject(long object, int type) {
        List<JsonRelationship> relations = new LinkedList<>();


        String sql = String.format("select * from %s where  ( %s=? or %s=?) and %s=?", TABLE, COLUMN_END, COLUMN_START, COLUMN_TYPE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setLong(2, object);
            ps.setLong(3, type);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all relationships that involve the given object (either as source
     * or target), regardless of type.
     *
     * @param object the object ID
     * @return a list of all relationships for that object
     */
    public List<JsonRelationship> getAllForObject(long object) {
        List<JsonRelationship> relations = new LinkedList<>();


        String sql = String.format("select * from %s where %s=? or %s=?", TABLE, COLUMN_END, COLUMN_START);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, object);
            ps.setLong(2, object);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }


    /**
     * Returns every row in the relationship table. Used to populate the
     * request-scoped relationship cache.
     *
     * @return the full list of all relationships
     */
    public List<JsonRelationship> getAll() {
        List<JsonRelationship> relations = new LinkedList<>();
        String sql = String.format("select * from %s", TABLE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            logger.trace("SQL: {}", ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all role-membership relationships (ROLE_MEMBER, ROLE_READ,
     * ROLE_DELETE, ROLE_EXECUTE, ROLE_WRITE).
     *
     * @return a list of role membership relationships
     */
    public List<JsonRelationship> getAllMembershipsForRoles() {
        List<JsonRelationship> relations = new LinkedList<>();
        String memberTypes = JEVisConstants.ObjectRelationship.ROLE_MEMBER + "," +
                JEVisConstants.ObjectRelationship.ROLE_READ + "," +
                JEVisConstants.ObjectRelationship.ROLE_DELETE + "," +
                JEVisConstants.ObjectRelationship.ROLE_EXECUTE + "," +
                JEVisConstants.ObjectRelationship.ROLE_WRITE;

        String sql = String.format("select * from %s where %s in(%s)", TABLE, COLUMN_TYPE, memberTypes);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all object-level membership relationships (MEMBER_READ,
     * MEMBER_WRITE, MEMBER_DELETE, MEMBER_EXECUTE, MEMBER_CREATE).
     * Used to populate the {@link org.jevis.commons.ws.sql.CachedAccessControl}
     * group-membership map.
     *
     * @return a list of membership relationships
     */
    public List<JsonRelationship> getAllMemberships() {
        List<JsonRelationship> relations = new LinkedList<>();
        String memberTypes = JEVisConstants.ObjectRelationship.MEMBER_READ + "," +
                JEVisConstants.ObjectRelationship.MEMBER_WRITE + "," +
                JEVisConstants.ObjectRelationship.MEMBER_DELETE + "," +
                JEVisConstants.ObjectRelationship.MEMBER_EXECUTE + "," +
                JEVisConstants.ObjectRelationship.MEMBER_CREATE;

        String sql = String.format("select * from %s where %s in(%s)", TABLE, COLUMN_TYPE, memberTypes);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all role-level membership relationships (ROLE_READ,
     * ROLE_WRITE, ROLE_DELETE, ROLE_EXECUTE, ROLE_CREATE).
     *
     * @return a list of role-member relationships
     */
    public List<JsonRelationship> getAllRoleMemberships() {
        List<JsonRelationship> relations = new LinkedList<>();
        String memberTypes = JEVisConstants.ObjectRelationship.ROLE_READ + "," +
                JEVisConstants.ObjectRelationship.ROLE_WRITE + "," +
                JEVisConstants.ObjectRelationship.ROLE_DELETE + "," +
                JEVisConstants.ObjectRelationship.ROLE_EXECUTE + "," +
                JEVisConstants.ObjectRelationship.ROLE_CREATE;

        String sql = String.format("select * from %s where %s in(%s)", TABLE, COLUMN_TYPE, memberTypes);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", ps);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }

        return relations;
    }

    /**
     * Returns all relationships whose type is in the given list.
     *
     * @param types the list of relationship type constants to include
     * @return a list of matching relationships
     */
    public List<JsonRelationship> getAll(List<Integer> types) {

        List<JsonRelationship> relations = new LinkedList<>();

        String in = " IN(";
        boolean first = true;
        for (int i : types) {
            if (!first) {
                in += ",";
            }
            in += i;
            if (first) {
                first = false;
            }
        }

        in += ")";

        String sql = String.format("select * from %s where %s%s", TABLE, COLUMN_TYPE, in);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            int pos = 0;
            for (int type : types) {
                ps.setInt(++pos, type);
            }
            logger.error("SQL: {}", ps);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildRelationship(rs));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }


        return relations;
    }
}
