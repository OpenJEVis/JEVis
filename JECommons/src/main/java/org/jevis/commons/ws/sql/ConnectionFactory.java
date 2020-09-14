/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.ws.sql;


import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author fs
 */
public class ConnectionFactory {

    private static ConnectionFactory instance;
    private BasicDataSource ds;

    public ConnectionFactory() {

    }

    public void registerMySQLDriver(String host, String port, String schema, String dbUser, String dbPW) {

        if (ds == null) {
            String conSring = "jdbc:mysql://" + host + ":" + port + "/" + schema + "?"
                    + "characterEncoding=UTF-8&useUnicode=true&character_set_client=UTF-8&character_set_connection=UTF-8&character_set_results=UTF-8";
//                    + "characterEncoding=UTF-8&amp;useUnicode=true";
//                    + "user=" + dbUser + "&password=" + dbPW;

            ds = new BasicDataSource();
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
            ds.setUrl(conSring);
            ds.setUsername(dbUser);
            ds.setPassword(dbPW);
            ds.setMaxTotal(100);
            ds.setMinIdle(5);
            ds.setMaxIdle(10);


        }

    }

    public Connection getConnection() throws SQLException {
        if (ds != null) {
            return ds.getConnection();
        } else {
            throw new SQLException("No database driver registered");
        }

    }

    public static ConnectionFactory getInstance() {
        if (ConnectionFactory.instance == null) {
            ConnectionFactory.instance = new ConnectionFactory();
        }
        return ConnectionFactory.instance;
    }

}
