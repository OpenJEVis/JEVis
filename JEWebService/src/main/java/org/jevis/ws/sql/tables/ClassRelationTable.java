/*
  Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>

  This file is part of JEAPI-SQL.

  JEAPI-SQL is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JEAPI-SQL is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  JEAPI-SQL. If not, see <http://www.gnu.org/licenses/>.

  JEAPI-SQL is part of the OpenJEVis project, further project information are
  published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class ClassRelationTable {

    public final static String TABLE = "classrelationship";
    public final static String COLUMN_START = "startclass";
//    public final static String COLUMN_INHERITANCE="inheritance";
    public final static String COLUMN_END = "endclass";
    public final static String COLUMN_TYPE = "type";
    private SQLDataSource _connection;
    private static final Logger logger = LogManager.getLogger(SQLDataSource.class);

    public ClassRelationTable(SQLDataSource ds) {
        this._connection = ds;
    }

    public boolean delete(JEVisClassRelationship rel) throws JEVisException {
        return delete(rel.getStartName(), rel.getEndName(), rel.getType());
    }

    /**
     *
     * @param start
     * @param end
     * @param type
     * @param rel
     * @return
     * @throws JEVisException
     */
    public boolean delete(String start, String end, int type) throws JEVisException {
        String sql = "delete from " + TABLE
                + " where " + COLUMN_START + "=?"
                + " and " + COLUMN_END + "=?"
                + " and " + COLUMN_TYPE + "=?";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, end);
            ps.setInt(3, type);

            logger.trace("Delete {}", ps);
            _connection.addQuery("ClassRel.delete()", ps.toString());
            int count = ps.executeUpdate();
            if (count == 1) {
                return true;
            } else {
                return true;//delete is allways true
            }

        } catch (Exception ex) {
            throw new JEVisException("Could not delete new ClassRelationship", 578246, ex);
        }
        /*ignored*/

    }

    /**
     *
     * @param start
     * @param end
     * @param type
     * @return
     * @throws JEVisException
     */
    public JsonClassRelationship insert(String start, String end, int type) throws JEVisException {
        String sql = "insert into " + TABLE + " (" + COLUMN_START + "," + COLUMN_END + "," + COLUMN_TYPE + ") "
                + " values(?,?,?)";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ps.setInt(3, type);

            logger.trace("SQL: {}", ps);
            _connection.addQuery("ClassRel.insert()", ps.toString());
            int count = ps.executeUpdate();
            if (count > 0) {
                JsonClassRelationship jcr = new JsonClassRelationship();
                jcr.setStart(start);
                jcr.setEnd(end);
                jcr.setType(type);
                return jcr;
            } else {
                throw new JEVisException("Could not insert new ClassRelationship", 578245);
            }

        } catch (Exception ex) {
            throw new JEVisException("Could not insert new ClassRelationship", 578246, ex);
        }
        /*ignored*/
    }

    /**
     *
     * @param jclass
     * @return
     * @throws JEVisException
     */
    public List<JsonClassRelationship> get(String jclass) throws JEVisException {
        logger.debug("get({})", jclass);
        List<JsonClassRelationship> relations = new ArrayList<>();
        //saver this will exclude not existion classes
        String sql = "select distinct " + TABLE + ".* from " + TABLE
                + " left join " + ClassTable.TABLE + " c1 on " + TABLE + "." + COLUMN_START + "=c1." + ClassTable.COLUMN_NAME
                + " left join " + ClassTable.TABLE + " c2 on " + TABLE + "." + COLUMN_END + "=c2." + ClassTable.COLUMN_NAME
                + " where (" + COLUMN_START + "=?" + " or " + COLUMN_END + "=? )"
                + " and c1." + ClassTable.COLUMN_NAME + " is not null "
                + " and c2." + ClassTable.COLUMN_NAME + " is not null ";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            ps.setString(1, jclass);
            ps.setString(2, jclass);

            _connection.addQuery("ClassRel.get(String)", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildClassRelationship(rs));
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }

        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while fetching ClassRelationship", 7390562, ex);
        }
        /*ignored*/
        return relations;
    }

    public List<JsonClassRelationship> get() throws JEVisException {
        logger.debug("get all");
        List<JsonClassRelationship> relations = new ArrayList<>();
//        String sql = "select * from " + TABLE
//                + " where " + COLUMN_START + "=?"
//                + " or " + COLUMN_END + "=?";

        //saver this will exclude not existion classes
        String sql = "select distinct " + TABLE + ".* from " + TABLE
                + " left join " + ClassTable.TABLE + " c1 on " + TABLE + "." + COLUMN_START + "=c1." + ClassTable.COLUMN_NAME
                + " left join " + ClassTable.TABLE + " c2 on " + TABLE + "." + COLUMN_END + "=c2." + ClassTable.COLUMN_NAME
                + " where "
                + " c1." + ClassTable.COLUMN_NAME + " is not null "
                + " and c2." + ClassTable.COLUMN_NAME + " is not null ";

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sql)) {

            _connection.addQuery("ClassRel.get()", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    relations.add(SQLtoJsonFactory.buildClassRelationship(rs));
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }

        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("Error while fetching ClassRelationship", 7390562, ex);
        }
        /*ignored*/
        return relations;
    }
}
