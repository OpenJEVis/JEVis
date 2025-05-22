/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier;

//import org.apache.log4j.Appender;
//import org.apache.log4j.FileAppender;
//import org.apache.log4j.PatternLayout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.jevis.commons.cli.JEVisCommandLine;

/**
 * @author gf
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        JENotifierOption option = new JENotifierOption();

//        JENotifierHelper.initializeLogger(Level.FATAL);
//        Appender appender = Logger.getRootLogger().getAppender("FILE");
        logger.info("-------Start JENotifier-------");
//        Helper.initializeLogger(JEVisCommandLine.getInstance().getDebugLevel());

        option.runJENotifier(args);

        logger.info("-------End JENotifier-------");
    }
//    private void initNewAppender(String NameForAppender, String Name4LogFile) {
////        logger = Logger.getLogger(NameForAppender); //NOT DEFAULT BY "logger = Logger.getLogger(TestJob.class);"
//
//        FileAppender appender = new FileAppender();
//        appender.setLayout(new PatternLayout("[%d{dd MMM yyyy HH:mm:ss}][%c{2}]: %-10m%n"));
//        appender.setFile(Name4LogFile);
//        appender.setAppend(true);
//        appender.setImmediateFlush(true);
//        appender.activateOptions();
//        appender.setName(NameForAppender);
//        appender.addFilter(new ThreadFilter(NameForAppender));
//        logger.setAdditivity(false);    //<--do not use default root logger
//        logger.addAppender(appender);
//    }
}
