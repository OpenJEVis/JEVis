/*
  Copyright (C) 2013 - 2015 Envidatec GmbH <info@envidatec.com>

  This file is part of JEAPI-SQL.

  JEAPI-SQL is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JEAPI-SQL is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  JEAPI-SQL. If not, see <http://www.gnu.org/licenses/>.
 */
package org.jevis.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisExceptionCodes;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class ClassTable {

    public final static String TABLE = "objectclass";
    public final static String COLUMN_NAME = "name";
//    public final static String COLUMN_INHERIT = "inheritance";
    public final static String COLUMN_DESCRIPTION = "description";
    public final static String COLUMN_ICON = "icon";//-->editinal table for files ?
    public final static String COLUMN_UNIQUE = "isunique";
    private SQLDataSource _connection;
//    private final SimpleClassCache _cache = SimpleClassCache.getInstance();
//    private Map<String, JEVisClass> _cach = new HashMap<String, JEVisClass>();
    Logger logger = LogManager.getLogger(ClassTable.class);

    public ClassTable(SQLDataSource ds) {
        _connection = ds;
    }

    public void getHeirNames(List<String> all, String jclass) throws JEVisException {

        List<JsonClassRelationship> rels = _connection.getClassRelationshipTable().get(jclass);
        for (JsonClassRelationship rel : rels) {
            if (rel.getType() == JEVisConstants.ClassRelationship.INHERIT && rel.getEnd().equals(jclass)) {
                //sub to suber
                all.add(rel.getStart());
                getHeirNames(all, rel.getStart());
            }
        }

    }

    /**
     * Delete the class and everything belonging to it.
     *
     * TODO: make it more pretty and secure TODO: rollback if once of the 3 is
     * failing 1. collect all class hiers 2. delete all types for the class 3.
     * delete all relationshipts for the class 4. delete classes
     *
     * @param jclass
     * @return
     * @throws JEVisException
     */
    public boolean delete(String jclass) throws JEVisException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        List<String> className = new ArrayList<>();
        className.add(jclass);
        getHeirNames(className, jclass);
        System.out.println("Delete Classes: " + Arrays.toString(className.toArray()));

        boolean typeDelte = false;
        boolean classDelete = false;

        //First delete all types
        try {
            String sqlDelTypes = "delete from " + TypeTable.TABLE + " where " + TypeTable.COLUMN_CLASS + " IN (";

            boolean first = true;
            for (String s : className) {
                if (!first) {
                    sqlDelTypes += ",";
                } else {
                    first = false;
                }
                sqlDelTypes += "?";
            }
            sqlDelTypes += ")";

            ps = _connection.getConnection().prepareStatement(sqlDelTypes);
//            Array array = _connection.getConnection().createArrayOf("VARCHAR", className.toArray());//java.sql.SQLFeatureNotSupportedException
//            ps.setArray(1, array);
            int count = 0;
            for (String s : className) {
                count++;//starts with 1
                ps.setString(count, s);
            }
            logger.error("SQL delete types: {}", ps);
            _connection.addQuery("Class.delete()", ps.toString());
            int restult = ps.executeUpdate();

            if (restult == 1) {
                typeDelte = true;
            }

        } catch (Exception ex) {
            throw new JEVisException("Error while deleting Class: " + ex, 2342763);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    logger.warn("cound not close Prepared statement: {}", ex);
                }
            }
        }

        //delete relationships
        try {
            String sqlDeleteRels = "delete from " + ClassRelationTable.TABLE + " where " + ClassRelationTable.COLUMN_START + " IN (";

            boolean first = true;
            for (String s : className) {
                if (!first) {
                    sqlDeleteRels += ",";
                } else {
                    first = false;
                }
                sqlDeleteRels += "?";
            }
            sqlDeleteRels += ")";

            sqlDeleteRels += " or " + ClassRelationTable.COLUMN_END + " IN (";

            boolean first2 = true;
            for (String s : className) {
                if (!first2) {
                    sqlDeleteRels += ",";
                } else {
                    first2 = false;
                }
                sqlDeleteRels += "?";
            }
            sqlDeleteRels += ")";

            ps = _connection.getConnection().prepareStatement(sqlDeleteRels);
//            Array array = _connection.getConnection().createArrayOf("VARCHAR", className.toArray());//java.sql.SQLFeatureNotSupportedException
//            ps.setArray(1, array);
            int count = 0;
            for (int i = 0; i < 2; i++) {

                for (String s : className) {
                    count++;//starts with 1
                    ps.setString(count, s);
                }
            }
            logger.error("SQL delete types: {}", ps);
            _connection.addQuery("Class.delete()", ps.toString());
            int restult = ps.executeUpdate();

            if (restult == 1) {
                typeDelte = true;
            }

        } catch (Exception ex) {
            throw new JEVisException("Error while deleting Class: " + ex, 2342763);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    logger.warn("cound not close Prepared statement: {}", ex);
                }
            }
        }

        //Delete all classes
        try {
            String sqlDelClass = "delete from " + TABLE + " where " + COLUMN_NAME + " IN (";
            boolean first = true;
            for (String s : className) {
                if (!first) {
                    sqlDelClass += ",";
                } else {
                    first = false;
                }
                sqlDelClass += "?";
            }
            sqlDelClass += ")";

            ps = _connection.getConnection().prepareStatement(sqlDelClass);
//            Array array = _connection.getConnection().createArrayOf("VARCHAR", className.toArray());
//            ps.setArray(1, array);
            int count = 0;
            for (String s : className) {
                count++;//starts with 1
                ps.setString(count, s);
            }
            logger.error("SQL delete classes: {}", ps);
            int result = ps.executeUpdate();

            if (result == 1) {
                classDelete = true;
            }

        } catch (Exception ex) {
            throw new JEVisException("Error while deleting Class: " + ex, 2342763);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    logger.warn("cound not close Prepared statement: {}", ex);
                }
            }
        }

        return classDelete && typeDelte;

    }

    public boolean insert(String name, String description, boolean isunique) throws JEVisException {
        String sql = "insert into " + TABLE
                + "(" + COLUMN_NAME + "," + COLUMN_DESCRIPTION + "," + COLUMN_UNIQUE + ")"
                + " values(?,?,?)";

        PreparedStatement ps = null;

        try {

            ps = _connection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBoolean(3, isunique);

            int value = ps.executeUpdate();
            logger.trace("SQL: {}", ps);
            _connection.addQuery("Class.insert()", ps.toString());
            if (value == 1) {
                getObjectClass(name);
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {

            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
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

    public boolean updateClass(String oldname, JsonJEVisClass jclass) {
        try {
            System.out.println("UpdateClass: "+oldname+" jclass.dis: "+jclass.getDescription());
            String sql = "update " + TABLE
                    + " set " + COLUMN_DESCRIPTION + "=?," + COLUMN_UNIQUE + "=?," + COLUMN_NAME + "=?"
                    + " where " + COLUMN_NAME + "=?";// + COLUMN_ICON + "=?"

            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);

            int i = 1;
            ps.setString(i++, jclass.getDescription());
            ps.setBoolean(i++, jclass.getUnique());
            ps.setString(i++, jclass.getName());
            ps.setString(i++, oldname);

            logger.error("SQL: {}", ps);

            _connection.addQuery("Class.update()", ps.toString());
            int res = ps.executeUpdate();

            //Check if the name changed, if yes we have to change all existing JEVisObjects.....do we want that?
            if (res == 1) {
                return true;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        return false;
    }

    public boolean updateClassIcon(String jclass, BufferedImage icon) {
        try {
            System.out.println("---------update ClassIcon ------------");
            String sql = "update " + TABLE
                    + " set " + COLUMN_ICON + "=?"
                    + " where " + COLUMN_NAME + "=?";

            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(icon, "gif", os);//better png or not?
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            ps.setBinaryStream(1, is);
            ps.setString(2, jclass);

            logger.error("SQL: {}", ps);

            _connection.addQuery("Class.updateIcon()", ps.toString());
            int res = ps.executeUpdate();
            System.out.println("sqlreturn: " + res);
            if (res == 1) {
                return true;
            }
        } catch (SQLException sex) {
            logger.error("sql errror", sex);
        } catch (Exception ex) {
            logger.error(ex);
        }

        return false;
    }

    private static java.awt.image.BufferedImage convertToBufferedImage(ImageIcon icon) {
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        // paint the Icon to the BufferedImage.
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return bi;

    }

    private byte[] getIconBytes(ImageIcon icon) throws Exception {
        BufferedImage img = convertToBufferedImage(icon);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        ImageIO.write(img, "jpg", baos);
        baos.flush();

        return baos.toByteArray();
    }

    public BufferedImage getClassIcon(String name) {

//        System.out.println("getObjectClass() " + name);
        JsonJEVisClass jClass = null;

        String sql = "select " + COLUMN_ICON + " from " + TABLE
                + " where  " + COLUMN_NAME + "=?"
                + " limit 1 ";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setString(1, name);
            logger.trace("SQL: {}", ps);

            _connection.addQuery("Class.getClassIcon(String)", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                byte[] bytes = rs.getBytes(ClassTable.COLUMN_ICON);
                if (bytes != null && bytes.length > 0) {
                    return ImageIO.read(new ByteArrayInputStream(bytes));
                }
            }

        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    logger.debug("Error while closing DB connection: {}. ", ex);
                }
            }
        }

        return null;

    }

    public JsonJEVisClass getObjectClass(String name) throws JEVisException {

//        System.out.println("getObjectClass() " + name);
        JsonJEVisClass jClass = null;

        String sql = "select * from " + TABLE
                + " where  " + COLUMN_NAME + "=?"
                + " limit 1 ";

        PreparedStatement ps = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            ps.setString(1, name);
            logger.trace("SQL: {}", ps);

            _connection.addQuery("Class.getObjectClass(String)", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                jClass = SQLtoJsonFactory.buildJEVisClass(rs);
            }

        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    logger.debug("Error while closing DB connection: {}. ", ex);
                }
            }
        }

        return jClass;
    }

    public List<JsonJEVisClass> getAllObjectClasses() throws JEVisException {
        List<JsonJEVisClass> jClasses = new LinkedList<>();

        String sql = "select * from " + TABLE;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = _connection.getConnection().prepareStatement(sql);
            logger.trace("SQL: {}", ps);

            _connection.addQuery("Class.getAll()", ps.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                jClasses.add(SQLtoJsonFactory.buildJEVisClass(rs));
            }

//            System.out.println("getAllObjectClasses()----> end");
        } catch (Exception ex) {
            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    logger.debug("Error while closing DB connection: {}. ", ex);
                }
            }
        }

        return jClasses;
    }
}
