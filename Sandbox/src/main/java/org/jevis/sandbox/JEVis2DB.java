package org.jevis.sandbox;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLtoJsonFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JEVis2DB {


    private static final Logger logger = LogManager.getLogger(JEVis2DB.class);
    private BasicDataSource ds;
    private String getDataSQL = "select * from REGISTRY_DATA where REGISTRY_ID=*";

    public JEVis2DB(BasicDataSource ds) {

        registerMySQLDriver("localhost", "3306", "JEVIS", "jevis", "pw");


    }

    public void export() {
        PreparedStatement ps = null;
        List<JsonObject> objects = new ArrayList<>();

        try {
            ps = getConnection().prepareStatement(getDataSQL);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {


                    objects.add(SQLtoJsonFactory.buildObject(rs));
                } catch (Exception ex) {
                    logger.error("Cound not load Object: " + ex.getMessage());
                }
            }

        } catch (SQLException ex) {
            logger.error("Error while selecting Object: {} ", ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex);
            logger.error("Error while selecting Object: {} ", ex);
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

    public void registerMySQLDriver(String host, String port, String schema, String dbUser, String dbPW) {


        if (ds == null) {
            String conSring = "jdbc:mysql://" + host + ":" + port + "/" + schema + "?"
                    + "characterEncoding=UTF-8&useUnicode=true&character_set_client=UTF-8&character_set_connection=UTF-8&character_set_results=UTF-8";
//                    + "characterEncoding=UTF-8&amp;useUnicode=true";
//                    + "user=" + dbUser + "&password=" + dbPW;

            ds = new BasicDataSource();
            ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setUrl(conSring);
            ds.setUsername(dbUser);
            ds.setPassword(dbPW);
            ds.setMaxTotal(100);
        }

    }

    public Connection getConnection() throws SQLException {
        if (ds != null) {
            return ds.getConnection();
        } else {
            throw new SQLException("No database driver registered");
        }

    }
}
