/**
 * Copyright (C) 2009 - 2013 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEAPI-SQL is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-SQL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-SQL. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-SQL is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.ws.sql.tables;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;

/**
 *
 * @author flo
 */
public class TypeTable {

    public final static String TABLE = "type";
    public final static String COLUMN_CLASS = "jevisclass";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_DISPLAY_TYPE = "displaytype";
    public final static String COLUMN_PRIMITIV_TYPE = "primitivtype";
//    public final static String COLUMN_INTERPRETATION="interpretation";//TODO: find a better name...
    public final static String COLUMN_DEFAULT_UNIT = "defaultunit";
    public final static String COLUMN_GUI_WEIGHT = "guiposition";
    public final static String COLUMN_DESCRIPTION = "description";
    public final static String COLUMN_VALIDITY = "validity";
    public final static String COLUMN_VALUE = "value";
    public final static String COLUMN_ALT_SYMBOL = "altsymbol";
    public final static String COLUMN_INHERITEDT = "inheritedt";

    private Logger logger = LogManager.getLogger(TypeTable.class);
    private final SQLDataSource _connection;

    public TypeTable(SQLDataSource ds) throws JEVisException {
        _connection = ds;
    }

    public boolean update(JsonType type, String jclass, String originalName) {
        try {
            String sql = "REPLACE into " + TABLE
                    + " ("
                    + COLUMN_DESCRIPTION + "," + COLUMN_NAME + "," + COLUMN_CLASS + "," + COLUMN_DISPLAY_TYPE + ","
                    + COLUMN_PRIMITIV_TYPE + "," + COLUMN_DEFAULT_UNIT + "," + COLUMN_GUI_WEIGHT + ","
                    + COLUMN_VALIDITY + "," + COLUMN_ALT_SYMBOL + ","
                    + COLUMN_INHERITEDT
                    + " ) VALUES ("
                    + "?,?,?,?,?,?,?,?,?,?" //11
                    + ")";
//                    + " where " + COLUMN_NAME + "=? and " + COLUMN_CLASS + "=?";

            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);

            ps.setString(1, type.getDescription());
            ps.setString(2, type.getName());

            ps.setString(3, jclass);
            ps.setString(4, type.getGuiType());

            ps.setInt(5, type.getPrimitiveType());
            ps.setNull(6, java.sql.Types.VARCHAR);

            //TODO, do we still want units for types
//            if (type.getUnit() != null) {
//                ps.setString(6, type.getUnit().toString());
//            } else {
//                ps.setNull(6, java.sql.Types.VARCHAR);
//            }
            ps.setInt(7, type.getGUIPosition());
            ps.setInt(8, type.getValidity());
            //TODO, to we still want altSybol?
            ps.setString(9, "");

            ps.setBoolean(10, type.getInherited());
            _connection.addQuery("Type.update()", ps.toString());
            logger.trace("SQL: {}", ps);
            System.out.println("type update ps: " + ps);
            int res = ps.executeUpdate();

            try {
                if (!type.getName().equals(originalName)) {
                    //------------------ Update exintings Attributes

                    //TODo
                    String updateObject = "update " + AttributeTable.TABLE + "," + ObjectTable.TABLE
                            + " set " + AttributeTable.TABLE + "." + AttributeTable.COLUMN_NAME + "=?"
                            + " where " + AttributeTable.TABLE + "." + AttributeTable.COLUMN_NAME + "=?"
                            + " and " + AttributeTable.TABLE + "." + AttributeTable.COLUMN_OBJECT + "=" + ObjectTable.TABLE + "." + ObjectTable.COLUMN_ID
                            + " and " + ObjectTable.TABLE + "." + ObjectTable.COLUMN_CLASS + "=?";

                    PreparedStatement ps2 = _connection.getConnection().prepareStatement(updateObject);
                    
                    ps2.setString(1, type.getName());
                    ps2.setString(2, originalName);
                    ps2.setString(3, jclass);

                    System.out.println("SQL updateAttriutesaftertype: \n"+ps2);
                    int res2 = ps2.executeUpdate();
                    if (res2 > 0) {
//                        System.out.println("updated " + res2 + " JEVisAttributes");
                    }

                }
            } catch (SQLException sex) {
                System.out.println("update attribute because of type: " + sex);
            }
            System.out.println("return true");
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
        }
        System.out.println("return false");
        return false;

    }

    public boolean delete(String jclass, String type) {
        String sql = "delete from " + TABLE
                + " where " + COLUMN_CLASS + "=?"
                + " and " + COLUMN_NAME + "=?";
        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setString(1, jclass);
            ps.setString(2, type);

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Type.delete()", ps.toString());
            if (ps.executeUpdate() == 1) {
//                System.out.println("true");
                return true;
            } else {
//                System.out.println("false");
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
                    /*ignored*/ }
            }
        }
    }

    public boolean delete(String jclass, JsonType type) throws JEVisException {
        return TypeTable.this.delete(jclass, type.getName());
    }

    public boolean insert(String jclass, JsonType newType) {
        try {
            String sql = "insert into " + TABLE
                    + "(" + COLUMN_NAME + "," + COLUMN_CLASS + ")"
                    + " values(?,?)";

            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);
            ps.setString(1, newType.getName());
            ps.setString(2, jclass);

            logger.trace("SQL: {}", ps);
            _connection.addQuery("Type.insert()", ps.toString());
            int count = ps.executeUpdate();

            if (count > 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }

    /**
     *
     * @param jclass
     * @param object
     * @return
     * @throws SQLException
     * @throws UnsupportedEncodingException
     * @throws org.jevis.api.JEVisException
     */
    public List<JsonType> getAll(JEVisClass jclass) throws SQLException, UnsupportedEncodingException, JEVisException {
        return getAll(jclass.getName());
    }

    public List<JsonType> getAll(String jclass) throws SQLException, UnsupportedEncodingException, JEVisException {
        List<JsonType> types = new ArrayList<>();

        String sql = "select * from " + TABLE
                + " where " + COLUMN_CLASS + "=?";

        PreparedStatement ps = _connection.getConnection().prepareStatement(sql);
        ps.setString(1, jclass);

        logger.trace("SQL: {}", ps);
        _connection.addQuery("Type.getAll()", ps.toString());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            types.add(SQLtoJsonFactory.buildType(rs));
        }

        logger.trace("Type Count1: {}", types.size());
        return types;
    }

    //TODO: this function is to muche like getALL(JEVisClass), may mearge into one.
    public List<JsonType> getAll() throws SQLException, UnsupportedEncodingException, JEVisException {
        List<JsonType> types = new ArrayList<>();

        String sql = "select * from " + TABLE;

        PreparedStatement ps = _connection.getConnection().prepareStatement(sql);

        logger.trace("SQL: {}", ps);
        _connection.addQuery("Type.getAll(String)", ps.toString());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            types.add(SQLtoJsonFactory.buildType(rs));
        }

        return types;
    }
}
