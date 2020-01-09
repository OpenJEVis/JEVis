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
package org.jevis.jeconfig;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Temporary solution of an global toolbar.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GlobalToolBar {

    private static final Logger logger = LogManager.getLogger(GlobalToolBar.class);
    private static final String STANDARD_BUTTON_STYLE = "-fx-background-color: transparent;-fx-background-insets: 0 0 0;";
    private static final String HOVERED_BUTTON_STYLE = "-fx-background-insets: 1 1 1;";
    private final PluginManager pm;

    public GlobalToolBar(PluginManager pm) {
        this.pm = pm;
    }

    public static void addEventHandler(PluginManager pm, ToggleButton button, final int command) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                logger.info("send command  " + command);
                pm.getSelectedPlugin().handleRequest(command);
            }
        });

    }



    public static void BuildEventhandler(Plugin plugin, ButtonBase button, final int command) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                plugin.handleRequest(command);

            }
        });

    }

    public static void changeBackgroundOnHoverUsingBinding(Node node) {
        node.styleProperty().bind(
                Bindings
                        .when(node.hoverProperty())
                        .then(
                                new SimpleStringProperty(HOVERED_BUTTON_STYLE))
                        .otherwise(
                                new SimpleStringProperty(STANDARD_BUTTON_STYLE)));
    }
}
