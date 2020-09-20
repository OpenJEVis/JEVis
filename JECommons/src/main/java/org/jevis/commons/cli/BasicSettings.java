/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Artur Iablokov
 */
public class BasicSettings {
    
    private static final String DESC_H = "Print help";
    @Parameter(names = {"--help", "-h"}, help = true, description = DESC_H)
    public boolean help;

    private static final String DESC_C = "Configuration File";
    @Parameter(names = {"--config", "-c"}, required = true, description = DESC_C, converter = CliFileConverter.class)
    public File config;
    
    private static final String DESC_UC = "Use cache";
    @Parameter(names = {"--usecache", "-uc"}, description = DESC_UC)
    public boolean cache = true;

//    private static final String DESC_LF = "Set an logfile";
//    @Parameter(names = {"-lf", "--logfile"}, required = true, description = DESC_LF)
//    public String logfile;
//
//    private static final String DESC_LL = "Set an log level";
//    @Parameter(names = {"-ll", "--loglevel"}, required = true, description = DESC_LL)
//    public Integer loglevel;

    private static final String DESC_O = "Dynamic Options '-o <list>'";
    @DynamicParameter(names = {"-o", "--option"}, required = false, description = DESC_O)
    public Map<String, String> options = new HashMap<>();

    
    public static final String SINGLE = "single";
    public static final String COMPLETE = "complete";
    public static final String SERVICE = "service";
    private static final String DESC_MODE = "Configure the service mode (" + SINGLE + ", " + SERVICE + ", " + COMPLETE + ")";
    @Parameter(names = {"--servicemode", "-sm"}, required = false,  description = DESC_MODE, validateWith = CliServiceModeValidator.class)
    public String servicemode = COMPLETE;

    private static final String DESC_JEVISID = "JEVis Object ID";
    @Parameter(names = {"--jevisid", "-jid"}, required = false, description = DESC_JEVISID)
    public String jevisid;


    private static final String CYCLE_TIME = "Cycle Time";
    @Parameter(names = {"--cycle-time", "-ct"}, required = false, description = CYCLE_TIME)
    public Integer cycle_time;

    private static final String EMERGENCY_CONFIG = "Emergency Config";
    @Parameter(names = {"--emergency-config", "-ec"}, required = false, description = EMERGENCY_CONFIG)
    public String emergency_Config;
}

