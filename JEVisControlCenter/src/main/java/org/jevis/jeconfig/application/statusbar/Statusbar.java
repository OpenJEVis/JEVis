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

import com.jfoenix.controls.JFXPopup;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.TaskProgressView;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.dialog.HiddenConfig;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
    private Label versionLabel = new Label(I18n.getInstance().getString("statusbar.version"));
    private HBox conBox = new HBox();
    private ImageView connectIcon = ResourceLoader.getImage("network-connected.png", this.ICON_SIZE, this.ICON_SIZE);
    private ImageView notConnectIcon = ResourceLoader.getImage("network-disconnected.png", this.ICON_SIZE, this.ICON_SIZE);
    private JEVisDataSource _ds;
    private Tooltip tt = new Tooltip("Warning:\nConnection to server lost. Trying to reconnect...  ");
    private int retryCount = 0;
    private ProgressBar progressBar = new ProgressBar();
    private HBox progressbox = new HBox();
    private Label messageBox = new Label();
    private TaskProgressView taskProgressView = new TaskProgressView();
    private JFXPopup popup = new JFXPopup();
    private Button showTaskViewButton = new Button("", JEConfig.getImage("TaskList.png", 15, 15));
    private Map<String, Image> imageList = new HashMap<>();
    private boolean hideTaskList = false;
    private Label titleLabel = new Label(I18n.getInstance().getString("statusbar.taskmon.title"));
    private Region spacer = new Region();
    private ExecutorService executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);
    private HashMap<Task, String> taskList = new HashMap<>();

    private class Job {
        public double total = 0;
        public double done = 0;

        public Job(double total, double done) {
            this.total = total;
            this.done = done;
        }
    }

    private HashMap<String, Job> jobList = new HashMap<>();

    public Statusbar() {
        super();
        BorderPane anchorPane = new BorderPane(taskProgressView);
        ToggleButton hideButton = new ToggleButton("", JEConfig.getImage("Hide.png", 12, 12));
        HBox hBox = new HBox(titleLabel, spacer, hideButton);
        hBox.setPadding(new Insets(8));
        hBox.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        anchorPane.setTop(hBox);

        popup.setPopupContent(anchorPane);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT);
        showTaskViewButton.setStyle("-fx-padding: 0;");
        showTaskViewButton.setOnAction(event -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                popup.show(onlineInfo);
            }

        });
        taskProgressView.setGraphicFactory(new Callback() {
            @Override
            public Object call(Object param) {

                if (param != null) {
                    if (imageList.containsKey(param.toString())) {
                        ImageView imageView = new ImageView(imageList.get(param.toString()));
                        imageView.setPreserveRatio(true);
                        imageView.fitHeightProperty().set(25);
                        //imageView.setFitHeight(100);
                        //imageView.setFitWidth(100);
                        //imageView.fitWidthProperty().set(25);
                        return imageView;
                    }
                }
                return null;
            }
        });
        taskProgressView.setPrefHeight(300);
        taskProgressView.getTasks().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                while (c.next()) {

                    if (c.getRemovedSize() == taskProgressView.getTasks().size()) {
                        try {
                            Platform.runLater(() -> popup.hide());
                        } catch (Exception ex) {
                        }
                    }
                    if (c.wasAdded()) {
                        try {
                            Platform.runLater(() -> {
                                if (!hideTaskList && !popup.isShowing()) {
                                    popup.show(onlineInfo);
                                }
                            });

                        } catch (Exception ex) {
                        }
                    }
                }

            }
        });

        hideButton.setSelected(hideTaskList);
        hideButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                hideTaskList = newValue;
                if (newValue) {
                    popup.hide();
                }
            }
        });

    }

    /**
     * Display an message in the shared status bar.
     *
     * @param jobID
     * @param totalJobs
     * @param message
     */
    public void startProgressJob(String jobID, double totalJobs, String message) {
        jobList.put(jobID, new Job(totalJobs, 0));
        setProgressBar(totalJobs, 0, message);
        Platform.runLater(() -> messageBox.setText(message));
    }


    /**
     * NOTE: Add the task monitor support.
     *
     * @param owner
     * @param futureTask
     * @param image
     * @param autoStart
     */
    public void addTask(String owner, FutureTask futureTask, Image image, boolean autoStart) {
        logger.debug("Starting new FutureTask: {}", futureTask);

        if (autoStart) executor.execute(futureTask);
    }

    /**
     * Add an new task to the process monitor.
     *
     * @param owner     id of the function to start the task, used to stop the task if needed
     * @param task      task
     * @param image     image for this task in the process monitor
     * @param autoStart if ture the shared executor will start the task.
     */
    public void addTask(String owner, Task task, Image image, boolean autoStart) {
        logger.debug("Starting new Task: {}", task);
        imageList.put(task.toString(), image);
        task.stateProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.equals(Worker.State.CANCELLED) || newValue.equals(Worker.State.FAILED) || newValue.equals(Worker.State.SUCCEEDED)) {
                    taskList.remove(task);
                    imageList.remove(task.toString());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        Platform.runLater(() -> {
            taskProgressView.getTasks().add(task);
            taskList.put(task, owner);
        });

        if (autoStart) executor.submit(task);
    }

    /**
     * Top all running and queued tasks for the owner
     *
     * @param owner
     */
    public void stopTasks(String owner) {

        taskList.forEach((task, s) -> {
            if (s.equals(owner)) {
                try {
                    task.cancel(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    /**
     * Increase the job done list by the given amount. (its not total)
     *
     * @param jobID
     * @param jobsDone new finished jobs
     * @param message
     */
    public void progressProgressJob(String jobID, double jobsDone, String message) {
        Job job = jobList.get(jobID);
        if (job != null) {
            job.done += jobsDone;

            setProgressBar(job.total, job.done, message);
            Platform.runLater(() -> messageBox.setText(message));
        }
    }

    public double getProgress(String jobID) {
        Job job = jobList.get(jobID);
        if (job != null) {
            return job.done;
        }
        return 0;
    }

    public void finishProgressJob(String jobID, String message) {
        Job job = jobList.get(jobID);
        if (job != null) {
            job.done = job.total;
            setProgressBar(job.total, job.total, message);
            Platform.runLater(() -> messageBox.setText(""));
        } else {
            setProgressBar(1, 1, message);
        }
    }


    public void setProgressBar(double totalJobs, double doneJobs, String message) {
        if (totalJobs < 0) {
            Platform.runLater(() -> {
                progressBar.setProgress(-1);
                messageBox.setText("");
            });
            return;
        }

        double procent = (((100 / totalJobs) * doneJobs) / 100);
        Platform.runLater(() -> {
            progressBar.setProgress(procent);
            if (doneJobs >= totalJobs) {
                progressBar.setProgress(0);
                //progressbox.setVisible(false);
                //messageBox.setVisible(false);
            } else {
                // progressbox.setVisible(true);
                // messageBox.setVisible(true);
            }

        });

    }

    public void initView() {
        HBox root = new HBox();

        root.setSpacing(10);
        root.setAlignment(Pos.CENTER_LEFT);

        ImageView userIcon = ResourceLoader.getImage("user.png", this.ICON_SIZE, this.ICON_SIZE);

//        Label userLabel = new Label("User:");
        ImageView notification = ResourceLoader.getImage("note_3.png", this.ICON_SIZE, this.ICON_SIZE);

        this.conBox.getChildren().setAll(this.connectIcon);

        Label versionNumber = new Label(JEConfig.class.getPackage().getImplementationVersion());
        versionNumber.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                try {
                    Tooltip tooltip = new Tooltip(((new Date()).getTime() - JEConfig.startDate.getTime()) + "ms");
                    versionNumber.setTooltip(tooltip);
                } catch (Exception ex) {
                }
            }
        });

        Pane spacer = new Pane();
        spacer.setMaxWidth(50);
        Pane spacer2 = new Pane();
        spacer.setMaxWidth(100);
        Region spacerLeft = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(this.messageBox, Priority.ALWAYS);
        HBox.setHgrow(this.onlineInfo, Priority.NEVER);
        HBox.setHgrow(this.userName, Priority.NEVER);

        Label loadStatus = new Label(" " + I18n.getInstance().getString("statusbar.loading"));

        Separator sep1 = new Separator(Orientation.VERTICAL);

        progressbox.setAlignment(Pos.CENTER_RIGHT);
        progressbox.setSpacing(8);
        progressbox.getChildren().setAll(messageBox, sep1, loadStatus, progressBar, showTaskViewButton);
        //TODO implement notification
        root.getChildren().setAll(userIcon, this.userName, spacerLeft, progressbox, spacer, versionLabel, versionNumber, spacer2, this.conBox, this.onlineInfo);

        String sinfo = "";

        for (JEVisOption opt : _ds.getConfiguration()) {
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

    public void setDataSource(JEVisDataSource ds) {
        this._ds = ds;
    }

    private void setBar() {
        String name = "";
        String lastName = "";
        String userAccount = "";
        try {
            name = this._ds.getCurrentUser().getFirstName();
            lastName = this._ds.getCurrentUser().getLastName();
            userAccount = this._ds.getCurrentUser().getAccountName();
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

    public HashMap<Task, String> getTaskList() {
        return taskList;
    }

    // TODO implement status bar for JEVis applications
}
