/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.jevis.jenotifier.mode.Service;
import org.jevis.jenotifier.mode.Single;
import org.jevis.jenotifier.mode.SingleAll;

/**
 * @author gf
 */
public class JENotifierOption {
    private static final Logger logger = LogManager.getLogger(JENotifierOption.class);

    public void runJENotifier(String[] commands) {

        JENotifierConfig config = new JENotifierConfig();
        config.parser(commands);

//        JENotifierHelper.initializeLogger(Level.INFO);
//        logger.info(config.getDebugLevel());

        if (config.getMode().equalsIgnoreCase("single")) {
            logger.info("-------Operation Mode: Single-------");
            Single single = new Single(config);
            single.start();
        } else if (config.getMode().equalsIgnoreCase("singleAll")) {
            logger.info("-------Operation Mode: SingleAll-------");
            SingleAll singleall = new SingleAll(config);
            singleall.start();
        } else if (config.getMode().equalsIgnoreCase("Service")) {
            logger.info("-------Operation Mode: Service-------");
            Service service = new Service(config);
            service.start();
        } else {
            logger.info("-------The Operation Mode: " + config.getMode() + " doesn't exist!-------");
        }

    }
}
