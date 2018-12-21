/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig;

import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.OptionFactory;
import org.jevis.jeconfig.application.ParameterHelper;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

/**
 * This class holds the configutraion for the JEConfig.
 *
 * TODO: replace this with the JEVisOption
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Configuration {

    private static final Logger logger = LogManager.getLogger(Configuration.class);
    private static final JEVisOption JECONFIG = OptionFactory.BuildOption(null, "jeconfig", "", "JEConfig option group");
    private static final JEVisOption URL_WELCOME = OptionFactory.BuildOption(JECONFIG, "welcomeurl", "", "URL of the welcome screen");

    private JEVisOption options;
    private List<JEVisOption> configuration;

    private String _loginIcon = "/icons/logo_coffee_klein.png";//"/icons/kaust.jpg";//"/icons/openjevislogo_simple2.png";
    private String _welcomeURL = "http://coffee-project.eu/";//"http://www.kaust.edu.sa/research-technology-park.html";//http://openjevis.org/projects/openjevis/wiki/JEConfig3#JEConfig-Version-3";
    private String _watermark = "/icons/logo_JEVis_OPEN_Ohne_Schatten_long_v0_10.png";
    //    private final String _defaultServerURL = "user:password@server:3306/jevis";
    private File lastPath = null;
    private File lastFile = null;
    private Locale appLoccale = Locale.getDefault();

    public Locale getLocale() {
        return appLoccale;
    }

    public void setLocale(Locale locale) {
        appLoccale = locale;
    }

    public URI getWelcomeURL() throws URISyntaxException {
        return new URI(_welcomeURL);
    }

    public File getLastFile() {
        return lastFile;
    }

    public void setLastFile(File file) {
        this.lastFile = file;
    }

    public File getLastPath() {
        return lastPath;
    }

    public void setLastPath(File path) {
        this.lastPath = path;
    }

    /**
     * Returns if the Sever URL should be visible in the LoginDialog
     *
     * @return
     */
    public boolean getShowServer() {
        return false;
    }

    /**
     * Returns the default Server URL for the Login dialog
     *
     * @return
     */
    public String getDefaultServer() {
        //"jevis:Taexu3Eesesieth3eid1@lthneo.kaust.edu.sa:3306/jevis";//user:password@server:3306/jevis";
        return "jevis:jevistest@coffee-project.eu:13306/jevis";
    }

    /**
     * returns if SSL is enabled for the DS connection
     *
     * @return
     */
    public boolean getEnabledSSL() {
        return false;
    }

    public String getLoginIcon() {
        return _loginIcon;
    }

    public enum COLORS {

        BACKGROUND
    }

    public String getWatermark() {
        return _watermark;
    }

    public void parseParameters(Application.Parameters args) {

        configuration = ParameterHelper.ParseJEVisConfiguration(args);
        for (JEVisOption opt : configuration) {
            if (opt.equals(JECONFIG)) {
                logger.info("Found " + JECONFIG.getKey());
                options = opt;
            }
        }

        if (options != null) {
            if (options.hasOption(URL_WELCOME.getKey())) {
                JEVisOption opt = options.getOption(URL_WELCOME.getKey());
                if (opt.getValue().equals("off")) {
                    _welcomeURL = "";
                } else {
                    _welcomeURL = opt.getValue();
                }
            } else {
                _welcomeURL = "http://openjevis.org/";
            }
        } else {
            _welcomeURL = "http://openjevis.org/";

        }
//        logger.info("Welcome URL: " + _welcomeURL);

//        for (Map.Entry<String, String> entry : args.getNamed().entrySet()) {
//            logger.info(entry.getKey() + " : " + entry.getValue());
//        }
        if (args.getNamed().containsKey("loginbanner")) {
//            logger.info("LoginIcon: " + args.getNamed().get("loginbanner"));
            _loginIcon = args.getNamed().get("loginbanner");
        }

//        if (args.getNamed().containsKey("welcomeurl")) {
////            logger.info("welcomeurl: " + args.getNamed().get("welcomeurl"));
//            _welcomeURL = args.getNamed().get("welcomeurl");
//        }
        if (args.getNamed().containsKey("watermark")) {
//            logger.info("watermark: " + args.getNamed().get("watermark"));
            _watermark = args.getNamed().get("watermark");
        }

    }

}
