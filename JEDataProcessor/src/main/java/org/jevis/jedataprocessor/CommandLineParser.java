/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor;

import org.apache.commons.cli.*;

import java.util.List;

/**
 * @author broder
 */
public class CommandLineParser {

    //the default options
    private static final String CONFIG = "jevis-config";
    private static final String JEVIS_ALL = "jevis-all";
    private static final String JEVIS_SINGLE = "jevis-single";
    private static final String OFFLINE = "offline";
    private static final String CLEANING_CONFIG = "clean-config";
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String HELP = "help";
    private static final String SERVICE_MODE = "service-mode";
    private static final String CYCLE_TIME = "cycle-time";
    private static CommandLineParser _instance = null;
    private Options _options;
    private org.apache.commons.cli.CommandLineParser _parser;
    private CommandLine _cmd;

    private CommandLineParser() {
        _options = new Options();
        _parser = new GnuParser();
        setDefaultOptions();
    }

    public static CommandLineParser getInstance() {
        if (_instance == null) {
            _instance = new CommandLineParser();
        }
        return _instance;
    }

    private void setDefaultOptions() {
        _options.addOption("jc", CONFIG, true, "Path to the JEVis Config File");
        _options.addOption("h", HELP, false, "Show the help list");
        _options.addOption("ja", JEVIS_ALL, false, "Connect to the jevis system and execute for all clean data objects");
        _options.addOption("js", JEVIS_SINGLE, true, "ID for a clean data object");
        _options.addOption("off", OFFLINE, false, "Offline mode");
        _options.addOption("cc", CLEANING_CONFIG, true, "Path to cleaning config file");
        _options.addOption("in", INPUT, true, "path to input data row file");
        _options.addOption("out", OUTPUT, true, "path to output data row file");
        _options.addOption("sm", SERVICE_MODE, false, "Enable Service-Mode");
        _options.addOption("ct", CYCLE_TIME, true, "Set the cycle time for the Service");
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

    public String getConfigPath() {
        return _cmd.getOptionValue(CONFIG);
    }

    private void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataLogger", _options);
    }

    public Long getSingleObject() {
        if (_cmd.hasOption(JEVIS_SINGLE)) {
            return Long.parseLong(_cmd.getOptionValue(JEVIS_SINGLE));
        } else {
            return null;
        }
    }

    public Integer getCycleTime() {
        if (_cmd.hasOption(CYCLE_TIME)) {
            return Integer.parseInt(_cmd.getOptionValue(CYCLE_TIME));
        } else return null;
    }

    public Boolean isServiceMode() {
        return _cmd.hasOption(SERVICE_MODE);
    }

    public Boolean isAllJEvisMode() {
        return _cmd.hasOption(JEVIS_ALL);
    }

    public Boolean isSingleJEvisMode() {
        return _cmd.hasOption(JEVIS_SINGLE);
    }

    public Boolean isOfflineMode() {
        return _cmd.hasOption(OFFLINE);
    }

    public String getInputPath() {
        if (_cmd.hasOption(INPUT)) {
            return _cmd.getOptionValue(INPUT);
        } else {
            return null;
        }
    }

    public String getCleanConfigPath() {
        if (_cmd.hasOption(CLEANING_CONFIG)) {
            return _cmd.getOptionValue(CLEANING_CONFIG);
        } else {
            return null;
        }
    }

    public String getOutputPath() {
        if (_cmd.hasOption(OUTPUT)) {
            return _cmd.getOptionValue(OUTPUT);
        } else {
            return null;
        }
    }

}
