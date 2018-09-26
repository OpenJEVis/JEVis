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
package org.jevis.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RelationshipTable {

    public final static String TABLE = "relationship";
    public final static String COLUMN_START = "startobject";
    public final static String COLUMN_END = "endobject";
    public final static String COLUMN_TYPE = "relationtype";
    private SQLDataSource _connection;
    private static final Logger logger = LogManager.getLogger(RelationshipTable.class);

    public RelationshipTable(SQLDataSource ds) {
        _connection = ds;
    }

    public List<JsonRelationship> selectByType(int type) {
        String sql = "select * from " + TABLE
                + " where " + COLUMN_TYPE + "=?";

        PreparedStatement ps = null;
        List<JsonRelationship> relations = new LinkedList<>();
        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setInt(1, type);

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.byTypeg(int)", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                relations.add(SQLtoJsonFactory.buildRelationship(rs));
            }

        } catch (Exception ex) {
            logger.error("Error while selecting relationships from DB: {}", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return relations;
    }

    /**
     * @param id jevisObject id
     * @return
     */
    public List<JsonRelationship> selectForObject(long id) {
        String sql = "select * from " + TABLE
                + " where " + COLUMN_START + "=? "
                + " or " + COLUMN_END + "=?";

        PreparedStatement ps = null;
        List<JsonRelationship> relations = new LinkedList<>();

        try {
            ps = _connection.getConnection().prepareStatement(sql);

            ps.setLong(1, id);
            ps.setLong(2, id);

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.selectForObject(long)", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                relations.add(SQLtoJsonFactory.buildRelationship(rs));
            }

        } catch (Exception ex) {
            logger.error("Error while selecting relationships from DB: {}", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return relations;
    }

    //todo: implemet the return save and performant

    /**
     * @param start
     * @param end
     * @param type
     * @return
     * @throws JEVisException
     */
    public JsonRelationship insert(long start, long end, int type) throws JEVisException {

        String sql = "insert into " + TABLE
                + " (" + COLUMN_START + "," + COLUMN_END + "," + COLUMN_TYPE + ")"
                + " values (?,?,?)";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setLong(1, start);
            ps.setLong(2, end);
            ps.setInt(3, type);
            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.insert()", ps.toString());
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

        } catch (Exception ex) {
            logger.error("Error while inserting relationship into DB: {}", ex.getMessage());
            throw new JEVisException("Could not create the relationship", 1964824, ex);
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

    public boolean delete(JsonRelationship rel) {
        return delete(rel.getFrom(), rel.getTo(), rel.getType());
    }


    public boolean delete(long start, long end, int type) {

        String sql = "delete from " + TABLE
                + " where " + COLUMN_START + "=?"
                + " and " + COLUMN_END + "=?"
                + " and " + COLUMN_TYPE + "=?";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setLong(1, start);
            ps.setLong(2, end);
            ps.setInt(3, type);
            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.delete()", ps.toString());
            int count = ps.executeUpdate();
            if (count == 1) {
                return true;
            } else {
                return true;//delete is allways true
            }

        } catch (Exception ex) {
            logger.error("Error while deleting relationship from DB: {}", ex);
            logger.error(ex);
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

    public boolean deleteAll(long id) {
        return deleteAll(new LinkedList<Long>(Arrays.asList(id)));
    }

    public boolean deleteAll(List<Long> ids) {
        //      logger.info("delete rel for: " + Arrays.toString(ids.toArray()));
        //TODO make it save with a prepared or so
        PreparedStatement ps = null;

        try {
            String in = " IN(";
            for (int i = 0; i < ids.size(); i++) {
                in += ids.get(i);
                if (i != ids.size() - 1) {
                    in += ",";
                }
            }
            in += ")";

            String sql = "delete from " + TABLE
                    + " where " + COLUMN_START + in
                    + " or " + COLUMN_END + in;
            logger.trace("SQL: {}", ps);
            ps = _connection.getConnection().prepareStatement(sql);

            _connection.addQuery("Relationship.deleteAll()", ps.toString());
            int count = ps.executeUpdate();

            return count == 1;

        } catch (Exception ex) {
            logger.error("Error while deleting relationship from DB: {}", ex.getMessage());
            logger.error(ex);
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

    public List<JsonRelationship> getAllForObject(long object) {
        logger.trace("getSingel Relationship");
        PreparedStatement ps = null;
        List<JsonRelationship> relations = new LinkedList<>();

        try {
            String sql = "select * from " + TABLE
                    + " where " + COLUMN_END + "=?"
                    + " or " + COLUMN_START + "=?";

            ps = _connection.getConnection().prepareStatement(sql);
            ps.setLong(1, object);
            ps.setLong(2, object);
            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.getAllForObject()", ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                relations.add(SQLtoJsonFactory.buildRelationship(rs));
            }

        } catch (Exception ex) {
            logger.error("Error while selecting relationships from DB: {}", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return relations;
    }


    public List<JsonRelationship> getAll() {
        logger.trace("getAll Relationship");
        PreparedStatement ps = null;
        List<JsonRelationship> relations = new LinkedList<>();

        try {
            String sql = "select * from " + TABLE;

            ps = _connection.getConnection().prepareStatement(sql);

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.getAll()", ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                relations.add(SQLtoJsonFactory.buildRelationship(rs));
            }

        } catch (Exception ex) {
            logger.error("Error while selecting relationships from DB: {}", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return relations;
    }

    public List<JsonRelationship> getAll(List<Integer> types) {

        PreparedStatement ps = null;
        List<JsonRelationship> relations = new LinkedList<>();

        try {
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

            String sql = "select * from " + TABLE
                    + " where " + COLUMN_TYPE + in;

            ps = _connection.getConnection().prepareStatement(sql);
            int pos = 0;
            for (int type : types) {
                ps.setInt(++pos, type);
            }

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Relationship.getAll(List<int>)", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                relations.add(SQLtoJsonFactory.buildRelationship(rs));
            }

        } catch (Exception ex) {
            logger.error("Error while selecting relationships from DB: {}", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }
        }
        return relations;
    }
}
