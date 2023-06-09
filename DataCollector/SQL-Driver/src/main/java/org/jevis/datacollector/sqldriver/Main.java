package org.jevis.datacollector.sqldriver;

import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeapi.ws.JEVisDataSourceWS;

/**
 * Debug Class
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start Debug: FuE server, User: " + args[0] + " PW: " + args[1] + " sql Server: " + args[2] + " JEVis: " + args[3]);

        JEVisDataSource dataSource = new JEVisDataSourceWS(args[3]);
        dataSource.connect(args[0], args[1]);
        JEVisObject serverOBJ = dataSource.getObject(Long.parseLong(args[2]));
        SQLDriver driver = new SQLDriver();
        driver.initialize(serverOBJ);
        driver.run();
    }

}