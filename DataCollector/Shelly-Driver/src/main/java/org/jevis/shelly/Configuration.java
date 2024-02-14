package org.jevis.shelly;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    public String server = "";
    public String deviceid = "";
    public String apikey = "";

    public String jevisServer = "";
    public String jevisUser = "";
    public String jevisPassword = "";

    public String jsonNode = "total_act";

    public Map<String, Long> mapping = new HashMap<>();

    public int total_act_power_id = 0;

    public Configuration(String file) throws ConfigurationException {
        File cfile = new File(file);
        XMLConfiguration config = new XMLConfiguration(cfile);
        System.out.println("File: " + config + " read: " + cfile.canRead());

        server = getParameter(config, "shelly.cloud", "https://shelly-83-eu.shelly.cloud");
        deviceid = getParameter(config, "shelly.device", "");
        apikey = getParameter(config, "shelly.apikey", "");
        jsonNode = getParameter(config, "shelly.jsonnode", "total_act");

        jevisServer = getParameter(config, "jevis.server", "");
        jevisUser = getParameter(config, "jevis.user", "");
        jevisPassword = getParameter(config, "jevis.password", "");
        jevisPassword = getParameter(config, "jevis.password", "");

        List<HierarchicalConfiguration> dps = config.configurationsAt("mapping.dp");
        for (HierarchicalConfiguration dp : dps) {
            try {
                System.out.println(dp.getString("jevis") + " " + dp.getString("json"));
                mapping.put(dp.getString("json"), dp.getLong("jevis"));
            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        }

        System.out.printf("Server: %s\nDevice: %s\napikey: %s\njevis: %s\nuser: %s\npw: %s%n",
                server, deviceid, apikey, jevisServer, jevisUser, jevisPassword);

    }


    public static String getParameter(XMLConfiguration config, String key, String defaultValue) {
        try {
            return config.getString(key);
        } catch (NullPointerException nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }
}
