/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.ws.sql;


import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton factory that manages a shared Apache Commons DBCP2 connection pool
 * for the JEWebService MySQL database.
 * <p>
 * The pool is created lazily on the first call to
 * {@link #registerMySQLDriver(String, String, String, String, String, String)}
 * and is then reused for all subsequent requests.
 *
 * <p>Pool settings:
 * <ul>
 *   <li>Max total connections: 100</li>
 *   <li>Min idle: 5 / Max idle: 10</li>
 *   <li>Connection validation: on borrow and while idle</li>
 * </ul>
 *
 * @author fs
 */
public class ConnectionFactory {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ConnectionFactory.class);
    private static ConnectionFactory instance;
    private BasicDataSource ds;

    /**
     * Creates a new (uninitialized) factory instance.
     * Use {@link #getInstance()} to obtain the shared singleton.
     */
    public ConnectionFactory() {

    }

    /**
     * Returns the singleton {@code ConnectionFactory} instance, creating it
     * on the first call.
     *
     * @return the shared factory instance
     */
    public static ConnectionFactory getInstance() {
        if (ConnectionFactory.instance == null) {
            ConnectionFactory.instance = new ConnectionFactory();
        }
        return ConnectionFactory.instance;
    }

    /**
     * Initializes the DBCP2 connection pool for the given MySQL endpoint.
     * This method is idempotent: repeated calls with any parameters are
     * ignored once the pool has been created.
     *
     * @param host    the MySQL host name or IP address
     * @param port    the MySQL port (typically {@code "3306"})
     * @param schema  the database/schema name
     * @param dbUser  the database user
     * @param dbPW    the database password
     * @param options additional JDBC URL options (appended with {@code &}),
     *                may be empty or {@code null}
     */
    public void registerMySQLDriver(String host, String port, String schema, String dbUser, String dbPW, String options) {

        if (ds == null) {
            String conString = "jdbc:mysql://" + host + ":" + port + "/" + schema + "?"
                    + "characterEncoding=UTF-8&useUnicode=true&character_set_client=UTF-8&character_set_connection=UTF-8&character_set_results=UTF-8";
            if (options != null && !options.isEmpty()) {
                conString += "&" + options;
            }

            ds = new BasicDataSource();
            ds.setUrl(conString);
            ds.setUsername(dbUser);
            ds.setPassword(dbPW);
            ds.setMaxTotal(100);
            ds.setMinIdle(5);
            ds.setMaxIdle(10);
            ds.setTestOnBorrow(true);
            ds.setTestWhileIdle(true);

        }

    }

    /**
     * Borrows a connection from the pool. On failure the call is retried up
     * to 5 times with a 3-second back-off between attempts.
     *
     * @return an open JDBC connection
     * @throws SQLException if no driver is registered or the pool is exhausted
     *                      after all retries
     */
    public Connection getConnection() throws SQLException {
        if (ds != null) {

            try {
                return ds.getConnection();
            } catch (SQLException ex) {
                return getConnection(0);
            }
        } else {


            throw new SQLException("No database driver registered");
        }
    }

    /**
     * Retry helper that waits 3 seconds between attempts.
     *
     * @param retry the current attempt count (0-based)
     * @return an open JDBC connection
     * @throws SQLException if the maximum number of retries (5) is exceeded
     *                      or the thread is interrupted while sleeping
     */
    public Connection getConnection(int retry) throws SQLException {
        logger.error("Retry SQL connection: {}", retry);

        if (retry < 5) {
            try {
                logger.error("Wait 3 seconds");
                Thread.sleep(3000);
                try {
                    logger.error("done wait");
                    return ds.getConnection();

                } catch (SQLException sqlex) {
                    logger.error("SQL Connection error: {}", sqlex.toString(), sqlex);
                    return getConnection(++retry);
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.error(ex);
                throw new SQLException("Thread Interrupted, No SQL Connection");
            }
        } else {
            logger.error("Max SQL retry reached stopping");
            throw new SQLException("No SQL Connection");
        }

    }

}
