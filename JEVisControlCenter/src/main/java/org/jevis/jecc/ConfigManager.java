/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc;

import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ConfigManager {

    private static ConfigManager instance;
    private static Stage primaryStage;
    private static JEVisDataSource ds;

    public synchronized static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public JEVisDataSource getDataSource() {
        return ds;
    }

    public void setDataSource(JEVisDataSource newds) {
        ConfigManager.ds = newds;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        ConfigManager.primaryStage = primaryStage;
    }
}
