package org.jevis.jeconfig.plugin.dashboard;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.widget.TimeFrameWidget;
import org.jevis.jeconfig.plugin.dashboard.widget.ValueWidget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.joda.time.Interval;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the periodic data-update lifecycle for all dashboard widgets.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Holding the single background {@link Timer} (daemon) used for all dashboard updates</li>
 *   <li>Creating and cancelling {@link TimerTask} instances that drive widget refresh cycles</li>
 *   <li>Tracking whether the update loop is currently active via {@link #isUpdateRunning()}</li>
 *   <li>Building per-widget {@link javafx.concurrent.Task} instances submitted to the status bar</li>
 * </ul>
 *
 * <p>This class is package-private and used exclusively by {@link DashboardControl}.
 * {@code DashboardControl} must call {@link #stopAllUpdates()} before the dashboard is
 * closed or switched.
 */
class DashboardUpdateScheduler {

    private static final Logger logger = LogManager.getLogger(DashboardUpdateScheduler.class);

    private final DashboardControl control;
    private final Timer updateTimer = new Timer("dashboard-update-timer", true);
    private final Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");

    private TimerTask updateTask;
    private boolean isUpdateRunning = false;

    /**
     * Creates a new scheduler bound to the given {@link DashboardControl}.
     *
     * @param control the owning DashboardControl; must not be {@code null}
     */
    DashboardUpdateScheduler(DashboardControl control) {
        this.control = control;
    }

    /**
     * Returns {@code true} if a periodic update loop is currently scheduled.
     */
    boolean isUpdateRunning() {
        return isUpdateRunning;
    }

    /**
     * Sets the update-running flag directly (used by slideshow control).
     */
    void setUpdateRunning(boolean running) {
        this.isUpdateRunning = running;
    }

    /**
     * Stops any running update tasks and cancels the scheduled {@link TimerTask}.
     * Safe to call multiple times.
     */
    void stopAllUpdates() {
        try {
            logger.debug("stopAllUpdates: {}", JEConfig.getStatusBar().getTaskList().size());
            JEConfig.getStatusBar().stopTasks(DashBordPlugIn.class.getName());
            if (this.updateTask != null) {
                this.updateTask.cancel();
            }
            this.isUpdateRunning = false;
        } catch (Exception ex) {
            logger.error("Error while stopping running task", ex);
        }
    }

    /**
     * Starts widget data updates, either as a one-shot execution or as a repeating scheduler.
     *
     * <p>Any previously scheduled update task is cancelled before the new one is created.
     * The {@link TimerTask} wraps the entire update body in a {@code catch(Throwable)} guard
     * so that an exception in one update cycle cannot kill the timer daemon thread.
     *
     * @param runOnce {@code true} to execute the update once immediately;
     *                {@code false} to schedule it at the rate configured in
     *                {@link org.jevis.jeconfig.plugin.dashboard.config2.DashboardPojo#getUpdateRate()}
     */
    void runDataUpdateTasks(boolean runOnce) {
        logger.debug("Restart Update Tasks: daemon");
        logger.debug("Update Interval: {}", control.getInterval());

        if (this.updateTask != null) {
            this.updateTask.cancel();
        }

        final AtomicInteger count = new AtomicInteger(0);

        updateTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("Starting Updates");
                    count.set(count.get() + 1);
                    JEConfig.getStatusBar().startProgressJob(
                            "Dashboard",
                            control.getWidgetList().stream().filter(w -> !w.isStatic()).count(),
                            I18n.getInstance().getString("plugin.dashboard.message.startupdate"));

                    try {
                        List<Widget> objects = new ArrayList<>();
                        List<ValueWidget> valueWidgets = new ArrayList<>();
                        List<TimeFrameWidget> timeFrameWidgets = new ArrayList<>();

                        for (Widget widget : control.getWidgetList()) {
                            if (widget instanceof TimeFrameWidget) {
                                timeFrameWidgets.add((TimeFrameWidget) widget);
                            } else if (widget instanceof ValueWidget && !valueWidgets.contains(widget)) {
                                valueWidgets.add((ValueWidget) widget);
                            } else if (!widget.isStatic()) {
                                if (objects.contains(widget)) {
                                    logger.warn("    --- warning duplicate widget update: {}-{}",
                                            widget.getConfig().getTitle(), widget.getConfig().getType());
                                } else {
                                    objects.add(widget);
                                    Task<Object> updateTask = createWidgetUpdateTask(widget, control.getInterval());
                                    JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), updateTask, widgetTaskIcon, true);
                                }
                            }
                        }

                        valueWidgets.sort((o1, o2) -> {
                            try {
                                boolean o1DependsOnO2 = o1.getDependentWidgets().contains(o2);
                                boolean o2DependsOnO1 = o2.getDependentWidgets().contains(o1);
                                if (o1DependsOnO2 && !o2DependsOnO1) return -1;
                                if (!o1DependsOnO2 && o2DependsOnO1) return 1;
                                return 0;
                            } catch (Exception e) {
                                logger.error(e);
                                return 0;
                            }
                        });

                        for (ValueWidget valueWidget : valueWidgets) {
                            Task<Object> updateTask = createWidgetUpdateTask(valueWidget, control.getInterval());
                            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), updateTask, widgetTaskIcon, true);
                        }

                        for (TimeFrameWidget timeFrameWidget : timeFrameWidgets) {
                            Task<Object> updateTask = createWidgetUpdateTask(timeFrameWidget, control.getInterval());
                            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), updateTask, widgetTaskIcon, true);
                        }

                    } catch (Exception ex) {
                        logger.error("Error while adding widgets", ex);
                    }
                    logger.info("Done task #{}", count.get());
                } catch (Throwable t) {
                    logger.error("Uncaught error in dashboard update timer — scheduler thread protected", t);
                }
            }
        };

        if (runOnce) {
            control.getDashBordPlugIn().getDashBoardToolbar().setUpdateRunning(false);
            this.updateTimer.schedule(updateTask, 0);
        } else {
            logger.info("Start updateData scheduler: {} sec, time: {}",
                    control.getActiveDashboard().getUpdateRate(), new Date());
            this.updateTimer.scheduleAtFixedRate(updateTask, 1000, control.getActiveDashboard().getUpdateRate() * 1000L);
            this.isUpdateRunning = true;
        }
    }

    /**
     * Creates a {@link javafx.concurrent.Task} that calls {@link Widget#updateData(Interval)}
     * on the given widget and reports progress to the status bar.
     *
     * @param widget   the widget to update; must not be {@code null}
     * @param interval the active time interval; must not be {@code null}
     * @return a ready-to-submit JavaFX Task
     */
    private Task<Object> createWidgetUpdateTask(Widget widget, Interval interval) {
        return new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                try {
                    logger.debug("createWidgetUpdateTask: '{}'  - Interval: {}", widget.getConfig().getTitle(), interval);
                    Platform.runLater(() -> this.updateTitle(
                            I18n.getInstance().getString("plugin.dashboard.message.updatingwidget")
                                    + " [" + widget.typeID() + widget.getConfig().getUuid() + "] "
                                    + widget.getConfig().getTitle() + "'"));
                    if (!widget.isStatic()) {
                        widget.updateData(interval);
                        logger.debug("updateData done: '{}:{}'", widget.getConfig().getTitle(), widget.getConfig().getUuid());
                    }
                    this.succeeded();
                    logger.debug("task done: {}:{}", widget.getConfig().getTitle(), widget.getConfig().getUuid());
                } catch (Exception ex) {
                    this.failed();
                    logger.error("Widget update error: [{}]", widget.getConfig().getUuid(), ex);
                } finally {
                    this.done();
                    JEConfig.getStatusBar().progressProgressJob("Dashboard", 1,
                            I18n.getInstance().getString("plugin.dashboard.message.finishedwidget")
                                    + " " + widget.getConfig().getUuid());
                }
                return null;
            }
        };
    }
}
