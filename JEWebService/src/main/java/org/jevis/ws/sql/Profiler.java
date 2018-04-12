/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.ws.sql;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;

/**
 *
 * @author fs
 */
public class Profiler {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Profiler.class);

    private class Event {

        DateTime time = new DateTime();
        String name = "";
        String message = "";

        public Event(String name, String message) {
            this.name = name;
            this.message = message;
        }

    }

    private String user = "unkown";
    List<Event> events = new ArrayList<>();
    List<String> sql = new ArrayList<>();

    
    
    public void setUser(String user) {
        this.user = user;
    }

    public void addEvent(String name, String message) {
        events.add(new Event(name, message));
    }
    
    public void setSQLList(List<String> list){
        sql=list;
    }

    public void printLog() {
        try {
            logger.info("--------------------------------------------");
            String header = String.format("[ %s | %s ] for user %s", "Total(ms)", "Realtiv(ms)", user);
            logger.info("{}", header);
            Event prevEvent = null;

            DateTime startTime = null;
            for (Event ev : events) {
                long time = 0;
                long timeTotal = 0;
                if (prevEvent != null) {
                    time = ev.time.getMillis() - prevEvent.time.getMillis();
                    timeTotal = ev.time.getMillis() - startTime.getMillis();
                } else {
                    startTime = ev.time;
                }

                String promt = String.format("[ %1$9s | %2$11s ]", timeTotal, time);
                logger.info("{} {}: {}", promt, ev.name, ev.message);
                prevEvent = ev;

            }
            logger.info("Sql requests: {} ", sql.size());
            int counter =0;
            for(String s:sql){
                counter++;
                logger.info("[{}] {}",counter,s);
            }
            
            //;)
            logger.info("");
            logger.info("");
            logger.info("");
            logger.info("");
            
        } catch (Exception ex) {

        }
        //TODO: print amount of used sql querrys
    }

    
    
}
