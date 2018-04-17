/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.jevis.jenotifier.mode.Service;
import org.jevis.jenotifier.mode.Single;
import org.jevis.jenotifier.mode.SingleAll;

/**
 *
 * @author gf
 */
public class JENotifierOption {

    public void runJENotifier(String[] commands) {

        JENotifierConfig config = new JENotifierConfig();
        config.parser(commands);

//        JENotifierHelper.initializeLogger(Level.INFO);
//        System.out.println(config.getDebugLevel());

        if (config.getMode().equalsIgnoreCase("single")) {
            Logger.getLogger(JENotifierOption.class.getName()).log(Level.INFO, "-------Operation Mode: Single-------");
            Single single = new Single(config);
            single.start();
        } else if (config.getMode().equalsIgnoreCase("singleAll")) {
            Logger.getLogger(JENotifierOption.class.getName()).log(Level.INFO, "-------Operation Mode: SingleAll-------");
            SingleAll singleall = new SingleAll(config);
            singleall.start();
        } else if (config.getMode().equalsIgnoreCase("Service")) {
            Logger.getLogger(JENotifierOption.class.getName()).log(Level.INFO, "-------Operation Mode: Service-------");
            Service service = new Service(config);
            service.start();
        } else {
            Logger.getLogger(JENotifierOption.class.getName()).log(Level.INFO, "-------The Operation Mode: " + config.getMode() + " doesn't exist!-------");
        }

    }
}
