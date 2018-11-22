/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.cli;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author bf
 */
public class JEVisCommandLine {
    private static final Logger logger = LogManager.getLogger(JEVisCommandLine.class);

    private static JEVisCommandLine _instance = null;
    private Options _options;
    private CommandLineParser _parser;
    private CommandLine _cmd;
    private String[] _args;
    //the default options
    private static String SERVER = "jevis-server";
    private static String PORT = "jevis-port";
    private static String USER = "jevis-user";
    private static String PASSWORD = "jevis-pass";
    private static String CONFIG = "jevis-config";
    private static String DRIVER_FOLDER = "driver-folder";
    private static String HELP = "help";
    private static String DEBUG = "debug";
    private static String SINGLE = "single";
    private static String DAEMONCYLCE = "daemon-cycle";
    private static String DAEMON = "daemon";

    private boolean _isUsed = false;

    private JEVisCommandLine() {
        _options = new Options();
        _parser = new GnuParser();
        setDefaultOptions();
    }

    public static JEVisCommandLine getInstance() {
        if (_instance == null) {
            _instance = new JEVisCommandLine();
        }
        return _instance;
    }

    private void setDefaultOptions() {
        _options.addOption("js", SERVER, true, "The server for the connection");
        _options.addOption("jp", PORT, true, "The port for the connection, default is 80");
        _options.addOption("ju", USER, true, "The user for the connection");
        _options.addOption("jpwd", PASSWORD, true, "The password for the connection");
        _options.addOption("jc", CONFIG, true, "Path to the JEVis Config File");
        _options.addOption("h", HELP, false, "Show the help list");
        _options.addOption("d", DEBUG, true, "Sets the debug level (INFO, WARN, ALL)");
        _options.addOption("df", DRIVER_FOLDER, true, "Sets the root folder for the driver structure");
        _options.addOption("si", SINGLE, true, "set a single equipment to start");
        _options.addOption("de", DAEMON, false, "Start in service(daemon) mode");
        _options.addOption("dec", DAEMONCYLCE, false, "service(daemon) mode sleep time between run cycles in milliseconds. Default is 3600000ms");

    }

    public void addOption(Option option) {
        _options.addOption(option);
    }

    public void addOptions(List<Option> options) {
        for (Option o : options) {
            _options.addOption(o);
        }
    }

    public void parse(String[] args) {
        try {
            _cmd = _parser.parse(_options, args);
        } catch (ParseException ex) {
            logger.error(ex);
            showHelp();
            System.exit(0);
        }
        //if the help option is set
        if (_cmd.hasOption("help")) {
            showHelp();
            System.exit(0);
        }
    }

    public String getValue(String optionParameter) {
        return _cmd.getOptionValue(optionParameter);
    }

    public String getServer() {
        return _cmd.getOptionValue(SERVER);
    }

    public int getPort() {
        return Integer.parseInt(_cmd.getOptionValue(PORT, "80"));
    }

    public String getUser() {
        return _cmd.getOptionValue(USER);
    }

    public String getPassword() {
        return _cmd.getOptionValue(PASSWORD);
    }

    public String getConfigPath() {
        return _cmd.getOptionValue(CONFIG);
    }

    public String getDriverFolder() {
        return _cmd.getOptionValue(DRIVER_FOLDER);
    }

    public boolean isService() {
        return _cmd.hasOption(DAEMON);
    }

    public Long getCycleSleepTime() {
        try {
            return Long.parseLong(_cmd.getOptionValue(DAEMONCYLCE, "3600000"));
        } catch (Exception px) {
            return 3600000L;
        }

    }

    private void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataLogger", _options);
    }

    public Level getDebugLevel() {
        String optionValue = _cmd.getOptionValue("debug", "WARN");
        return Level.toLevel(optionValue.toUpperCase());
    }

    public void setIsUsed(boolean isUsed) {
        _isUsed = isUsed;
    }

    public boolean isUsed() {
        return _isUsed;
    }

    public Long getSingleObject() {
        if (_cmd.hasOption(SINGLE)) {
            String optionValue = _cmd.getOptionValue(SINGLE);
            return Long.parseLong(_cmd.getOptionValue(SINGLE));
        } else {
            return null;
        }
    }

    public boolean isDevDriverForced() {
        return _cmd.hasOption("dev");
    }
}
