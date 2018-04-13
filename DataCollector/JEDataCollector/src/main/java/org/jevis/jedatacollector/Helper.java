/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import org.apache.commons.cli.Option;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.commons.cli.JEVisCommandLine;

/**
 * @deprecated not used (AI 13.03.2018)
 * @author bf
 */
public class Helper {

    //Command line Parameter
    public static String SINGLE = "single";
    public static String DRY = "dry";
    public static String SERVICE = "service";
    public static String OUTPUT = "output";
    public static String QUERY_SERVER = "query-server";
    public static String QUERY_USER = "query-user";
    public static String QUERY_PASS = "query-pass";
    public static String DATA_SOURCE = "data-source";
    public static String DATA_POINT = "data-point";
    public static String EQUIPMENT = "equipment";
    public static String FROM = "from";
    public static String UNTIL = "until";
    public static String PROTOCOL = "protocol";
    public static String CSV = "csv";
    public static String CONNETION_FILE = "connection";
    public static String PARSING_FILE = "parsing";
    public static String OUTPUT_FILE = "outputPath";
    public static String OUTPUT_ONLINE = "outputDp";

    public static void initializeCommandLine(String[] args) {
        JEVisCommandLine cmd = JEVisCommandLine.getInstance();
        //Execution Control
        cmd.addOption(new Option(DRY, false, "starts a dry run -> fetching data without DB import"));
        cmd.addOption(new Option(OUTPUT, true, "the outputfile is saved under this path"));
        cmd.addOption(new Option(SERVICE, false, "start as service"));
        //Fetch Job Parameters
        cmd.addOption(new Option("qs", QUERY_SERVER, true, "Defines the server url for the device request"));
        cmd.addOption(new Option("qu", QUERY_USER, true, "Defines a user for authentication"));
        cmd.addOption(new Option("qp", QUERY_PASS, true, "Defines a password for authentication"));
        cmd.addOption(new Option("ds", DATA_SOURCE, true, "Forces a specific data source"));
        cmd.addOption(new Option("dp", DATA_POINT, true, "Forces a specific data point"));
        cmd.addOption(new Option("e", EQUIPMENT, true, "Forces a specific equipment"));
        cmd.addOption(new Option(FROM, true, "Forces the \"from\" timestamp in UTC format"));
        cmd.addOption(new Option(UNTIL, true, "Forces the \"until\" timestamp in UTC format"));
        cmd.addOption(new Option(PROTOCOL, true, "Forces the protocol type"));
        cmd.addOption(new Option(CSV, true, "Forces the CSV format"));
        cmd.addOption(new Option(OUTPUT_FILE, true, "Saves the output under the given path"));
        cmd.addOption(new Option(OUTPUT_ONLINE, true, "Saves the output under the given online node in the jevis system"));
        cmd.addOption(new Option(CONNETION_FILE, true, "Path of the connection file"));
        cmd.addOption(new Option(PARSING_FILE, true, "Path of the parsing file"));

        //Create Options
        cmd.parse(args);
    }

    public static void initializeLogger(Level debugLevel) {
//        PropertyConfigurator.configure("log4j.properties");
        Logger.getRootLogger().setLevel(debugLevel);
    }
}
