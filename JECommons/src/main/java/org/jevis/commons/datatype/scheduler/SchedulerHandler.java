/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.datatype.scheduler;

import org.jevis.commons.datatype.scheduler.json.JsonScheduler;
import org.jevis.commons.datatype.scheduler.json.JsonSchedulerRule;
import org.jevis.commons.datatype.scheduler.cron.CronRule;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTimeZone;

/**
 * Scheduler Handler - Serialization and deserialization
 *
 * @author Artur Iablokov
 */
public class SchedulerHandler {
    
    /**
     * builds default (empty) scheduler
     * @return 
     */
    public static CronScheduler BuildDefaultScheduler(){
        return JsonToJavaClass(buildDefaultJson());
    }
    /**
     * builds scheduler fron JSON string
     * @param json
     * @return
     * @throws IOException 
     */
    public static CronScheduler BuildScheduler(String json) throws IOException{
        return JsonToJavaClass(stringToJson(json));
    }
    /**
     * builds scheduler from JSON file
     * @param jfile
     * @return
     * @throws IOException 
     */
    public static CronScheduler BuildScheduler(File jfile) throws IOException{
        return JsonToJavaClass(fileToJson(jfile));
    }
    /**
     * serialize scheduler and gets string
     * @param scheduler
     * @return 
     */
    public static String SerializeScheduler(CronScheduler scheduler){
        return jsonToString(JavaClassToJson(scheduler));
    }
    /**
     * serialize scheduler and store in file
     * @param scheduler
     * @param path 
     */
    public static void SerializeScheduler(CronScheduler scheduler, String path){
        jsonToFile(path, JavaClassToJson(scheduler));
    }

    private static JsonScheduler JavaClassToJson(CronScheduler scheduler) {

        JsonScheduler json = new JsonScheduler();
        JsonSchedulerRule jr;
        List<JsonSchedulerRule> jrules = new ArrayList<>();
        List<SchedulerRule> rules = scheduler.getAllRules();
        json.setTimezone(scheduler.getDatetTimeZone().getID());

        for (SchedulerRule rule : rules) {
            jr = new JsonSchedulerRule();
            jr.setMonths(rule.getMonthArray());
            jr.setDayOfMonth(rule.getDayOfMonth());
            jr.setDayOfWeek(rule.getDayOfWeekArray());
            jr.setStartTimeHours(rule.getStartHour());
            jr.setStartTimeMinutes(rule.getStartMinute());
            jr.setEndTimeHours(rule.getEndHour());
            jr.setEndTimeMinutes(rule.getEndMinute());
            jrules.add(jr);
        }

        json.setScheduler(jrules);
        return json;
    }

    private static CronScheduler JsonToJavaClass(JsonScheduler json) {

        CronScheduler scheduler = new CronScheduler();
        CronRule rule;
        List<CronRule> rules = new ArrayList<>();
        List<JsonSchedulerRule> jrules = json.getScheduler();
        scheduler.setDatetTimeZone(DateTimeZone.forID(json.getTimezone()));
        
        for (JsonSchedulerRule jr : jrules) {
            scheduler.addRule(JsonRuleToCronRule(jr));
        }
        return scheduler;
    }

    private static CronRule JsonRuleToCronRule(JsonSchedulerRule jr) {
        CronRule rule = new CronRule();
        for (int mon : jr.getMonths()) {
            rule.setMonth(Month.of(mon), true);
        }
        rule.setDayOfMonths(jr.getDayOfMonth());
        for (int dow : jr.getDayOfWeek()) {
            rule.setDayOfWeek(DayOfWeek.of(dow), true);
        }
        rule.setStartHour(jr.getStartTimeHours());
        rule.setStartMinute(jr.getStartTimeMinutes());
        rule.setEndHour(jr.getEndTimeHours());
        rule.setEndMinute(jr.getEndTimeMinutes());
        return rule;
    }

    private static String jsonToString(JsonScheduler json) {
        ObjectMapper objectMapper = new ObjectMapper();
        String str = null;
        try {
            str = objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(SchedulerHandler.class.getName()).log(Level.SEVERE, "Scheduler JSON import to string failed", ex);
        }
        return str;
    }

    private static void jsonToFile(String path, JsonScheduler json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(path), json);
        } catch (IOException ex) {
            Logger.getLogger(SchedulerHandler.class.getName()).log(Level.SEVERE, "Scheduler JSON import to file failed", ex);
        }
    }

    /**
     * Build default (empty) scheduler handler
     *
     * @return
     */
    private static JsonScheduler buildDefaultJson() {
        return new JsonScheduler();
    }

    /**
     * Build scheduler handler from json string
     *
     * @param json
     * @return
     * @throws IOException
     */
    private static JsonScheduler stringToJson(String json) throws IOException {
        return inputToJson(json);
    }

    /**
     * Build scheduler handler from json file
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static JsonScheduler fileToJson(File file) throws IOException {
        return inputToJson(file);
    }

    private static JsonScheduler inputToJson(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonScheduler jsonObj = null;

        if (obj != null) {
            if (obj instanceof String) {
                String json = (String) obj;
                jsonObj = objectMapper.readValue(json, JsonScheduler.class);

            } else if (obj instanceof File) {
                File jsonFile = (File) obj;
                jsonObj = objectMapper.readValue(jsonFile, JsonScheduler.class);
            }

            return jsonObj;
        }
        throw new IllegalArgumentException("argument is null or json corrupt");
    }
}
