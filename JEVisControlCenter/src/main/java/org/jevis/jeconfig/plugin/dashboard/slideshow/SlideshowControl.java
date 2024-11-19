package org.jevis.jeconfig.plugin.dashboard.slideshow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowControl {

    private static final Logger logger = LogManager.getLogger(SlideshowControl.class);
    private List<Timer> timers = new ArrayList<>();
    private List<JEVisObject> dashboards = new ArrayList<>();
    private boolean isAutoplay = false;


    public SlideshowControl(JEVisObject collectionObj) throws Exception{

        JEVisDataSource ds = collectionObj.getDataSource();
        String timerStrg = collectionObj.getAttribute("Timer").getLatestSample().getValueAsString();
        for (String s : timerStrg.split(",")) {
            try{
                Period period = Period.parse(s.trim());
            }catch (Exception ex){
                logger.error(ex,ex);
            }
        }

        try {
            isAutoplay = collectionObj.getAttribute("Autostart").getLatestSample().getValueAsBoolean();
        }catch (Exception ex){
            isAutoplay = false;
        }

        //TargetHelper targetHelper = new TargetHelper(ds,collectionObj.getAttribute("Dashboards"));

        String dashbaordStrg =collectionObj.getAttribute("Dashboards").getLatestSample().getValueAsString();

        for (String s : dashbaordStrg.split(",")) {
            try{
                dashboards.add(ds.getObject(Long.parseLong(s.trim())));
            }catch (Exception ex){
                logger.error(ex,ex);
            }
        }
    }

    public void start(){


    }

    public boolean isAutoplay(){
        return isAutoplay;
    }




    private TimerTask createTimer(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

            }
        };

        return task;
    }

}
