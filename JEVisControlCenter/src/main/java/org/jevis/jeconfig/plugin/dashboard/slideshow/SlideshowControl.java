package org.jevis.jeconfig.plugin.dashboard.slideshow;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowControl {

    private static final Logger logger = LogManager.getLogger(SlideshowControl.class);
    private final DashboardControl dashboardControl;
    private Timer mainLoopTimer = null;
    private List<Period> timers = new ArrayList<>();
    private List<JEVisObject> dashboards = new ArrayList<>();
    private boolean isAutoplay = false;
    private int lastTimerPos = -1;
    private TimerTask mainLoop = null;


    public SlideshowControl(JEVisObject collectionObj, DashboardControl dashboardControl) throws Exception {
        this.dashboardControl = dashboardControl;
        JEVisDataSource ds = collectionObj.getDataSource();

        String timerStrg = collectionObj.getAttribute("Timer").getLatestSample().getValueAsString();
        for (String s : timerStrg.split(",")) {
            try {
                Period period = Period.parse(s.trim());
                timers.add(period);
            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        }

        try {
            isAutoplay = collectionObj.getAttribute("Autostart").getLatestSample().getValueAsBoolean();
        } catch (Exception ex) {
            isAutoplay = false;
        }

        String dashbaordStrg = collectionObj.getAttribute("Dashboards").getLatestSample().getValueAsString();

        for (String s : dashbaordStrg.split(",")) {
            try {
                dashboards.add(ds.getObject(Long.parseLong(s.trim())));
            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        }
    }

    public JEVisObject getFirstDashboard() {
        if (dashboards.isEmpty()) return null;

        return dashboards.get(0);
    }

    public void start() {
        mainLoopTimer = new Timer(true);
        mainLoop = new TimerTask() {
            boolean isCanceled = false;

            @Override
            public void run() {
                for (JEVisObject dashboard : dashboards) {
                    try {
                        if (!isCanceled) {
                            Platform.runLater(() -> dashboardControl.selectDashboard(dashboard));
                        } else {
                            break;
                        }
                        Thread.sleep(getNextTimer().toStandardDuration().getMillis());

                    } catch (Exception ex) {
                        logger.error(ex, ex);
                    }
                }
            }

            @Override
            public boolean cancel() {
                isCanceled = true;
                return super.cancel();
            }
        };
        dashboardControl.setUpdateRunning(true);
        mainLoopTimer.scheduleAtFixedRate(mainLoop, 0, 1);
    }

    public boolean isAutoplay() {
        return isAutoplay;
    }


    public void stop() {
        try {
            mainLoop.cancel();
            mainLoopTimer.cancel();
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
        dashboardControl.setUpdateRunning(false);
    }

    private Period getNextTimer() {
        lastTimerPos++;

        if (lastTimerPos >= timers.size()) lastTimerPos = 0;

        return timers.get(lastTimerPos);
    }

}
