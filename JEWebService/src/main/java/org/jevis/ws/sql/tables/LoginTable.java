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
package org.jevis.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisExceptionCodes;
import org.jevis.ws.sql.JEVisUserNew;
import org.jevis.ws.sql.PasswordHash;
import org.jevis.ws.sql.SQLDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
    private static final Logger logger = LogManager.getLogger(LoginTable.class);
    private SQLDataSource _connection;

    public LoginTable(SQLDataSource ds) {
        _connection = ds;
    }

    public JEVisUserNew loginUser(String name, String pw) throws JEVisException {
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
                        return new JEVisUserNew(_connection, rs.getString(COLUMN_LOGIN), rs.getLong(COLUMN_OBJECT), rs.getBoolean(COLUMN_SYS_ADMIN), rs.getBoolean(COLUMN_ENABLED));

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


    public Map<String, JEVisUserNew> getAccounts()throws JEVisException,SQLException{
        logger.debug("getAccounts ");
        Map<String, JEVisUserNew> acounts = new ConcurrentHashMap<>();
        String sqlUser = String.format("select * from %s ", TABLE);

        try (PreparedStatement ps = _connection.getConnection().prepareStatement(sqlUser)) {
            logger.debug("SQL: {}", ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    acounts.put(rs.getString(COLUMN_LOGIN).toLowerCase(),
                            new JEVisUserNew(_connection, rs.getString(COLUMN_LOGIN),
                                    rs.getLong(COLUMN_OBJECT), rs.getBoolean(COLUMN_SYS_ADMIN),
                                    rs.getBoolean(COLUMN_ENABLED),rs.getString(COLUMN_PASSWORD)));

                } catch (Exception ex) {
                    logger.error("Login NOK");
                }
            }
        }
        return acounts;
    }

}
