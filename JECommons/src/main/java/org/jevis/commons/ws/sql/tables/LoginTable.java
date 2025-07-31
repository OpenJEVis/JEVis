/*
  Copyright (C) 2009 - 2016 Envidatec GmbH <info@envidatec.com>

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
package org.jevis.commons.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisExceptionCodes;
import org.jevis.commons.ws.sql.JEVisUserSQL;
import org.jevis.commons.ws.sql.PasswordHash;
import org.jevis.commons.ws.sql.SQLDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class LoginTable {

    public final static String TABLE = "login";
    public final static String COLUMN_OBJECT = "object";
    public final static String COLUMN_LOGIN = "login";
    public final static String COLUMN_PASSWORD = "password";
    public final static String COLUMN_ENABLED = "enabled";
    public final static String COLUMN_SYS_ADMIN = "sysadmin";
    public final static String COLUMN_FIRST_NAME = "firstname";
    public final static String COLUMN_LAST_NAME = "lastname";
    public final static String COLUMN_ENTRA_ID = "entraid";
    private static final Logger logger = LogManager.getLogger(LoginTable.class);
    private final SQLDataSource _connection;

    public LoginTable(SQLDataSource ds) {
        _connection = ds;
    }

    public JEVisUserSQL loginUser(String name, String pw) throws JEVisException {
        logger.debug("Login {} {}", name, pw);
        String sqlUser = String.format("select * from %s where %s=? and %s =? limit 1", TABLE, COLUMN_LOGIN, COLUMN_ENABLED);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlUser)) {
            ps.setString(1, name);
            ps.setBoolean(2, true);

            logger.debug("SQL: {}", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    if (PasswordHash.validatePassword(pw, rs.getString(COLUMN_PASSWORD))) {
                        logger.debug("Login OK");
                        return new JEVisUserSQL(_connection, rs.getString(COLUMN_LOGIN), rs.getLong(COLUMN_OBJECT), rs.getBoolean(COLUMN_SYS_ADMIN), rs.getBoolean(COLUMN_ENABLED));

                    } else {
                        logger.debug("Login NOK");
                        throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
                    }
                } catch (Exception ex) {
                    logger.debug("Login NOK");
                    throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
                }
            }
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        }
    }

/*
    public Map<String, JEVisUserSQL> getAccounts() throws JEVisException, SQLException {
        logger.debug("getAccounts ");
        Map<String, JEVisUserSQL> acounts = new ConcurrentHashMap<>();
        String sqlUser = String.format("select * from %s ", TABLE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlUser)) {
            logger.debug("SQL: {}", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    acounts.put(rs.getString(COLUMN_LOGIN).toLowerCase(),
                            new JEVisUserSQL(_connection, rs.getString(COLUMN_LOGIN),
                                    rs.getLong(COLUMN_OBJECT), rs.getBoolean(COLUMN_SYS_ADMIN),
                                    rs.getBoolean(COLUMN_ENABLED), rs.getString(COLUMN_PASSWORD), ""));

                } catch (Exception ex) {
                    logger.error("Login NOK");
                }
            }
        }
        return acounts;
    }

 */


    /**
     * ------------------------------------------
     * Fast Login below
     */

    /*
    public Map<String, JEVisUserSQL> getAllUserOLd() throws JEVisException {
        logger.debug("Get all Logins ");
        String sqlALlUser = "select `t`.`object` AS `object`,`t`.`login` AS `login`,max(`t`.`password`) AS `password`,max(`t`.`enabled`) AS `enabled`,max(`t`.`sysadmin`) AS `sysadmin` from (select `o`.`id` AS `object`,`o`.`name` AS `login`,coalesce((case when (`s`.`attribute` = 'Password') then `s`.`value` end),0) AS `password`,coalesce((case when (`s`.`attribute` = 'Enabled') then `s`.`value` end),0) AS `enabled`,coalesce((case when (`s`.`attribute` = 'Sys Admin') then `s`.`value` end),0) AS `sysadmin` from (`jevis`.`sample` `s` left join (`jevis`.`attribute` `a` left join `jevis`.`object` `o` on((`o`.`id` = `a`.`object`))) on(((`o`.`id` = `s`.`object`) and (`a`.`name` = `s`.`attribute`) and (`a`.`maxts` = `s`.`timestamp`)))) where (`o`.`type` = 'User')) `t` group by `t`.`object`";
        Map<String, JEVisUserSQL> users = new ConcurrentHashMap<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlALlUser)) {
            logger.debug("SQL: {}", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    if (rs.getBoolean(COLUMN_ENABLED)) {
                        users.put(rs.getString(COLUMN_LOGIN).toLowerCase(Locale.ROOT),
                                new JEVisUserSQL(_connection, rs.getString(COLUMN_LOGIN),
                                        rs.getLong(COLUMN_OBJECT), rs.getBoolean(COLUMN_SYS_ADMIN),
                                        rs.getBoolean(COLUMN_ENABLED), rs.getString(COLUMN_PASSWORD), ""));
                    }

                } catch (Exception ex) {
                    logger.debug("Login NOK");
                    throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        }
        return users;
    }

     */
    public Map<String, JEVisUserSQL> getAllUser() throws JEVisException {
        logger.debug("Get all Logins ");
        String sqlALlUser = "SELECT \n" +
                "    o.id AS object,\n" +
                "    o.name AS name,\n" +
                "    \n" +
                "    MAX(CASE WHEN s.attribute = 'Password' THEN s.value END) AS password,\n" +
                "    MAX(CASE WHEN s.attribute = 'Enabled' THEN s.value END) AS enabled,\n" +
                "    MAX(CASE WHEN s.attribute = 'Entra ID Group' THEN s.value END) AS entraid,\n" +
                "    MAX(CASE WHEN s.attribute = 'Sys Admin' THEN s.value END) AS sysadmin\t\n" +
                "\n" +
                "FROM object o\n" +
                "JOIN (\n" +
                "    SELECT s1.*\n" +
                "    FROM sample s1\n" +
                "    JOIN (\n" +
                "        SELECT object, attribute, MAX(timestamp) AS max_timestamp\n" +
                "        FROM sample\n" +
                "        GROUP BY object, attribute\n" +
                "    ) latest\n" +
                "    ON s1.object = latest.object\n" +
                "       AND s1.attribute = latest.attribute\n" +
                "       AND s1.timestamp = latest.max_timestamp\n" +
                ") s ON o.id = s.object\n" +
                "where o.type=\"User\" \n" +
                "GROUP BY o.id, o.name\n" +
                "ORDER BY o.id;";

        Map<String, JEVisUserSQL> users = new ConcurrentHashMap<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlALlUser)) {
            logger.debug("SQL: {}", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    String key = rs.getString("name").toLowerCase(Locale.ROOT);

                    users.put(key, new JEVisUserSQL(_connection, key,
                            rs.getLong("object"), rs.getBoolean(COLUMN_SYS_ADMIN),
                            rs.getBoolean(COLUMN_ENABLED), rs.getString(COLUMN_PASSWORD), rs.getString(COLUMN_ENTRA_ID)));


                } catch (Exception ex) {
                    logger.debug("Login NOK");
                    //  throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        }
        return users;
    }

    public Map<String, JEVisUserSQL> getAllRoleAsUser() throws JEVisException {
        logger.debug("Get all Roles as User");
        String sqlALlUser = "select * from object where type=\"User Role\";";
        Map<String, JEVisUserSQL> users = new ConcurrentHashMap<>();

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlALlUser)) {
            logger.debug("SQL: {}", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    if (rs.getBoolean(COLUMN_ENABLED)) {
                        users.put(rs.getString("name"),
                                new JEVisUserSQL(_connection, rs.getString("name"),
                                        rs.getLong("id"),
                                        false,
                                        true,
                                        "", ""));
                    }

                } catch (Exception ex) {
                    logger.debug("Login NOK");
                    throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex);
            throw new JEVisException("User does not exist or password was wrong", JEVisExceptionCodes.UNAUTHORIZED);
        }
        return users;
    }


}
