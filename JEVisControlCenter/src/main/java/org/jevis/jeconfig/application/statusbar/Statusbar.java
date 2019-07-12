/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.statusbar;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.CommonOptions;
import org.jevis.jeconfig.application.resource.ResourceLoader;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Status bar with user and connection infos.
 *
 * @author Florian Simon
 */
public class Statusbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(Statusbar.class);

    private final int ICON_SIZE = 20;
    private final int WAIT_TIME = 3000;//60000;//MSEC
    private final int RETRY_COUNT = 720;//count
    public BooleanProperty connectedProperty = new SimpleBooleanProperty(true);
    private Label userName = new Label("");
    private Label onlineInfo = new Label("Online");
    private HBox conBox = new HBox();
    private ImageView connectIcon = ResourceLoader.getImage("network-connected.png", this.ICON_SIZE, this.ICON_SIZE);
    private ImageView notConnectIcon = ResourceLoader.getImage("network-disconnected.png", this.ICON_SIZE, this.ICON_SIZE);
    private JEVisDataSource _ds;
    private String lastUsername = "";
    private String lastPassword = "";
    private Tooltip tt = new Tooltip("Warning:\nConnection to server lost. Trying to reconnect...  ");
    private int retryCount = 0;

    public Statusbar(JEVisDataSource ds) {
        super();
        this._ds = ds;

        HBox root = new HBox();

        root.setSpacing(10);
        root.setAlignment(Pos.CENTER_LEFT);

        ImageView userIcon = ResourceLoader.getImage("user.png", this.ICON_SIZE, this.ICON_SIZE);

//        Label userLabel = new Label("User:");
        ImageView notification = ResourceLoader.getImage("note_3.png", this.ICON_SIZE, this.ICON_SIZE);

        this.conBox.getChildren().setAll(this.connectIcon);

        Pane spacer = new Pane();
        spacer.setMaxWidth(Integer.MAX_VALUE);

        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(this.onlineInfo, Priority.NEVER);
        HBox.setHgrow(this.userName, Priority.NEVER);

        //TODO implement notification
        root.getChildren().addAll(userIcon, this.userName, spacer, this.conBox, this.onlineInfo);

        String sinfo = "";

        for (JEVisOption opt : ds.getConfiguration()) {
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                for (JEVisOption dsOption : opt.getOptions()) {
                    sinfo += dsOption.getKey() + ": " + dsOption.getValue() + "\n";
                }
            }
        }


        NumberFormat numberFormate = DecimalFormat.getNumberInstance(java.util.Locale.getDefault());
        double memNumber = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);

        sinfo += "\nMemory usage: " + numberFormate.format(memNumber) + " mb";

        Tooltip serverTip = new Tooltip("Connection Info:\n"
                + sinfo);
        this.onlineInfo.setTooltip(serverTip);

        HBox.setHgrow(root, Priority.ALWAYS);
        getItems().add(root);

        setBar();
    }

    private void setBar() {
        String name = "";
        String lastName = "";
        String userAccount = "";
        try {
            name = this._ds.getCurrentUser().getFirstName();
            lastName = this._ds.getCurrentUser().getLastName();
            this.lastUsername = this._ds.getCurrentUser().getAccountName();
        } catch (Exception ex) {
            logger.fatal("Could not fetch Username", ex);
        }

        if (name.isEmpty() && lastName.isEmpty()) {
            this.userName.setText(userAccount);
        } else {
            this.userName.setText(name + " " + lastName);
        }

        Thread checkOnline = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(Statusbar.this.WAIT_TIME);
//                        System.gc();

                        if (Statusbar.this._ds.isConnectionAlive()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
//                                    logger.info("still online");
                                    Statusbar.this.onlineInfo.setText("Online");
                                    Statusbar.this.onlineInfo.setTextFill(Color.BLACK);
                                    Statusbar.this.conBox.getChildren().setAll(Statusbar.this.connectIcon);

                                    if (Statusbar.this.tt.isShowing()) {
                                        Statusbar.this.tt.hide();
                                    }
                                    Statusbar.this.connectedProperty.setValue(Boolean.TRUE);
                                }
                            });

                        } else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
//                                    logger.info("whaa were are offline");
                                    Statusbar.this.onlineInfo.setText("Offline");
                                    Statusbar.this.onlineInfo.setTextFill(Color.web("#D62748"));//red
                                    Statusbar.this.conBox.getChildren().setAll(Statusbar.this.notConnectIcon);

//                                    onlineInfo.setTooltip(tt);
                                    final Point2D nodeCoord = Statusbar.this.onlineInfo.localToScene(0.0, 0.0);
                                    if (!Statusbar.this.tt.isShowing()) {
                                        Statusbar.this.tt.show(Statusbar.this.onlineInfo, nodeCoord.getX(), nodeCoord.getY());
                                    }
                                    Statusbar.this.connectedProperty.setValue(Boolean.FALSE);
//                                    reconnect();
                                }
                            });

                        }
                    }

                    //TODO checlk is alive
                } catch (InterruptedException ex) {
                    logger.fatal(ex);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }

            }

        };
        checkOnline.setDaemon(true);
        checkOnline.start();
    }

    private void reconnect() {
        Thread reConn = new Thread() {

            @Override
            public void run() {
                try {
                    if (Statusbar.this.retryCount < Statusbar.this.RETRY_COUNT) {
                        logger.info("try Reconnect");
                        Statusbar.this._ds.reconnect();
                        ++Statusbar.this.retryCount;
                    } else {
                        logger.error("No Connection Possible .. giving up");
                    }
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
            }
        };
        reConn.setDaemon(true);
        reConn.start();
    }

// TODO implement status bar for JEVis applications
}
