/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier;

//import org.apache.log4j.Appender;
//import org.apache.log4j.FileAppender;
//import org.apache.log4j.PatternLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Level;
//import org.jevis.commons.cli.JEVisCommandLine;

/**
 *
 * @author gf
 */
public class Main {

//    private Logger logger = Logger.getRootLogger();
    public static void main(String[] args) {
        JENotifierOption option = new JENotifierOption();

        PropertyConfigurator.configure("log4j.properties");
//        JENotifierHelper.initializeLogger(Level.FATAL);
//        Appender appender = Logger.getRootLogger().getAppender("FILE");
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "-------Start JENotifier-------");
//        Helper.initializeLogger(JEVisCommandLine.getInstance().getDebugLevel());

        option.runJENotifier(args);

        Logger.getLogger(Main.class.getName()).log(Level.INFO, "-------End JENotifier-------");
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
