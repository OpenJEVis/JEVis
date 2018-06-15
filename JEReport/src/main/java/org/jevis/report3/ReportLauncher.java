/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.google.inject.Injector;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.JEVisCommandLine;
import org.jevis.commons.cli.JEVisServerConnectionCLI;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.policy.ReportPolicy;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author broder
 */
public class ReportLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ReportLauncher.class);
    public static JEVisDataSource jevisDataSource;
    private static Injector injector;
    private boolean singleMode;

    public static void main(String[] args) throws JEVisException {
        //parse Commandline
        JEVisCommandLine cmd = JEVisCommandLine.getInstance();
        cmd.parse(args);

        //start report
//        Injector injector = Guice.createInjector(new ReportLauncherInjector());
        ReportLauncher launcher = new ReportLauncher();
        launcher.run();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void setInjector(Injector inj) {
        injector = inj;
    }

    private void run() throws JEVisException {
        logger.info("connect to jevis server");
        establishConnection();

        //createObjects();

        JEVisCommandLine cmd = JEVisCommandLine.getInstance();
        List<JEVisObject> reportObjects = new ArrayList<>();
        if (cmd.getSingleObject() != null) {
            Long singleObject = cmd.getSingleObject();
            JEVisObject reportbject = jevisDataSource.getObject(singleObject);
            singleMode = reportObjects.add(reportbject);
        } else {
            JEVisClass reportClass = jevisDataSource.getJEVisClass(ReportAttributes.NAME);
            reportObjects = jevisDataSource.getObjects(reportClass, true);
        }

        //execute the report objects
        logger.info("nr of reports {}", reportObjects.size());
        for (JEVisObject reportObject : reportObjects) {
            try {
                logger.info("---------------------------------------------------------------------");
                logger.info("current report object: " + reportObject.getName() + " with id: " + reportObject.getID());
                //check if the report is enabled
                ReportPolicy reportPolicy = new ReportPolicy(); //Todo inject in constructor
                Boolean reportEnabled = reportPolicy.isReportEnabled(reportObject);
                if (!reportEnabled & !singleMode) {
                    logger.info("Report is not enabled");
                    continue;
                }

                ReportExecutor executor = ReportExecutorFactory.getReportExecutor(reportObject);
                executor.executeReport();
            } catch (Exception e) {
                logger.error("Error while creating report", e);
                e.printStackTrace();
            }
        }
    }

    public static JEVisDataSource getDataSource() {
        return jevisDataSource;
    }

    public boolean establishConnection() {
        JEVisCommandLine cmd = JEVisCommandLine.getInstance();
        String configFile = cmd.getConfigPath();
        logger.info("Path to Config File: " + configFile);
        JEVisServerConnectionCLI con = new JEVisServerConnectionCLI(configFile);
        try {
//            jevisDataSource = new JEVisDataSourceSQL("openjevis.org", "13306", "jevis", "jevis", "jevistest");
//            return jevisDataSource.connect("", "");
            jevisDataSource = new JEVisDataSourceWS("http://" + con.getDb() + ":" + con.getPort());
            jevisDataSource.connect(con.getJevisUser(), con.getJevisPW());
//            jevisDataSource = new JEVisDataSourceSQL(con.getDb(), con.getPort(), con.getSchema(), con.getUser(), con.getPw());
//            return jevisDataSource.connect(con.getJevisUser(), con.getJevisPW());
        } catch (JEVisException ex) {
            logger.error("No Connection", ex);
        }
        return false;
    }

    private void createObjects() {
        try {
            Path path = Paths.get("");
            List<String> lines = Files.readAllLines(path,StandardCharsets.ISO_8859_1);
            int curLine = 0;
            List<String> dps = new ArrayList<>();
            for (String line : lines) {
                if (curLine == 5){
                    String[] splitted = line.split(";");
                    for(int i = 2;i<splitted.length;i++){
                        String mapping = splitted[i];
                        createMapping(mapping);
                    }
                    break;
                }
                curLine++;
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ReportLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    private void createMapping(String mapping) {
        try {
            System.out.println(mapping);
            JEVisObject object = jevisDataSource.getObject(7458l);
            JEVisClass jeVisClass = jevisDataSource.getJEVisClass("Data");
            JEVisObject buildObject = object.buildObject(mapping, jeVisClass);
            JEVisObject channel = jevisDataSource.getObject(7457l);
            JEVisClass jeVisClass2 = jevisDataSource.getJEVisClass("CSV Data Point");
            JEVisObject buildObject1 = channel.buildObject(mapping, jeVisClass2);
            buildObject1.getAttribute("Mapping Identifier").buildSample(new DateTime(), mapping).commit();
            buildObject1.getAttribute("Target").buildSample(new DateTime(), buildObject.getID()).commit();
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ReportLauncher.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }
}
