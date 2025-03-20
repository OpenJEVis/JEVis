/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.config;

import org.jevis.api.JEVisOption;

/**
 * Static references to common options for the JEVisConfigurtaion
 *
 * @author Florian Simon
 */
public interface CommonOptions {

    /**
     * JEVisOptions related to the JEVisDataSource
     */
    interface DataSource {

        /**
         * Group of all datasource options. Will also be used as prefix for
         * parameters
         */
//        public final String GROUP = "datasource";
        JEVisOption DataSource = OptionFactory.BuildOption(null, "datasource", "", "JEVis datasource option group");

        JEVisOption HOST = OptionFactory.BuildOption(DataSource, "host", "openjevis.org", "Hostname URL of the data source");
        JEVisOption PORT = OptionFactory.BuildOption(DataSource, "port", "3306", "Port of the data source");
        JEVisOption CLASS = OptionFactory.BuildOption(DataSource, "class", "org.jevis.api.sql.JEVisDataSourceSQL", "Classname of the data source. e.g. 'org.jevis.api.sql.JEVisDataSourceSQL'");
        JEVisOption SCHEMA = OptionFactory.BuildOption(DataSource, "schema", "jevis", "MySQL Schema of the data source");
        JEVisOption USERNAME = OptionFactory.BuildOption(DataSource, "username", "jevis", "Username to the data source.");
        JEVisOption PASSWORD = OptionFactory.BuildOption(DataSource, "password", "jevistest", "Password to the data source");
        JEVisOption LOCALE = OptionFactory.BuildOption(DataSource, "locale", "en", "Locale for i18n");
        JEVisOption SSLTRUST = OptionFactory.BuildOption(DataSource, "ssltrust", "system", "SSL certificates trust mode. ALLWAY if you wish to trust signed ssl certificates");
        JEVisOption CONNECTION = OptionFactory.BuildOption(DataSource, "connection", "jevistest", "Password to the data source");

    }

    /**
     * JEVisOptions related to the JavaFX bases login GUI.
     */
    interface FXLogin {

        /**
         * Group of all avaFX bases login GUI options. Will also be used as
         * prefix for parameters
         */
        JEVisOption FXLogin = OptionFactory.BuildOption(null, "fxlogin", "", "FXLogin option group");

        /**
         * Logo displayed into login form.
         */
        JEVisOption URL_LOGO = OptionFactory.BuildOption(FXLogin, "logo", "", "URL for the Logo fof the FXLogin");
        JEVisOption URL_CSS = OptionFactory.BuildOption(FXLogin, "css", "", "URL for the CSS customisation of the FXLogin");
        JEVisOption URL_REGISTER = OptionFactory.BuildOption(FXLogin, "register", "", "URL of the registration link, set 'off' if it should be hidden ");

    }

}
