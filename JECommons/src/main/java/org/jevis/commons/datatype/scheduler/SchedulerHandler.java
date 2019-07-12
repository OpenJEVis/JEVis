//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jevis.commons.datatype.scheduler.cron.CronRule;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.json.JsonScheduler;
import org.jevis.commons.json.JsonSchedulerRule;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchedulerHandler {
    public SchedulerHandler() {
    }

    public static CronScheduler BuildDefaultScheduler() {
        return JsonToJavaClass(buildDefaultJson());
    }

    public static CronScheduler BuildScheduler(String json) throws IOException {
        return JsonToJavaClass(stringToJson(json));
    }

    public static CronScheduler BuildScheduler(File jfile) throws IOException {
        return JsonToJavaClass(fileToJson(jfile));
    }

    public static String SerializeScheduler(CronScheduler scheduler) {
        return jsonToString(JavaClassToJson(scheduler));
    }

    public static void SerializeScheduler(CronScheduler scheduler, String path) {
        jsonToFile(path, JavaClassToJson(scheduler));
    }

    private static JsonScheduler JavaClassToJson(CronScheduler scheduler) {
        JsonScheduler json = new JsonScheduler();
        List<JsonSchedulerRule> jrules = new ArrayList();
        List<SchedulerRule> rules = scheduler.getAllRules();
        json.setTimezone(scheduler.getDatetTimeZone().getID());

        for (SchedulerRule rule : rules) {
            JsonSchedulerRule jr = new JsonSchedulerRule();
            jr.setMonths(rule.getMonthArray());
            jr.setDayOfMonth(rule.getDayOfMonth());
            jr.setDayOfWeek(rule.getDayOfWeekArray());
            jr.setStartTimeHours(rule.getStartHour());
            jr.setStartTimeMinutes(rule.getStartMinute());
            jr.setEndTimeHours(rule.getEndHour());
            jr.setEndTimeMinutes(rule.getEndMinute());
            jrules.add(jr);
        }

        json.setRules(jrules);
        return json;
    }

    private static CronScheduler JsonToJavaClass(JsonScheduler json) {
        CronScheduler scheduler = new CronScheduler();
        new ArrayList();
        List<JsonSchedulerRule> jrules = json.getRules();
        scheduler.setDatetTimeZone(DateTimeZone.forID(json.getTimezone()));

        for (JsonSchedulerRule jr : jrules) {
            scheduler.addRule(JsonRuleToCronRule(jr));
        }

        return scheduler;
    }

    private static Integer[] stringToIntList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            List<Integer> integers = new ArrayList<>();
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");

            for (String str : tempList) {
                integers.add(Integer.parseInt(str));
            }

            Integer[] output = new Integer[integers.size()];
            output = integers.toArray(output);
            return output;
        } else return new Integer[0];
    }

    private static CronRule JsonRuleToCronRule(JsonSchedulerRule jr) {
        CronRule rule = new CronRule();
        Integer[] monthArray = stringToIntList(jr.getMonths());
        int lengthMonthArray = monthArray.length;

        int i;
        int dow;
        for (i = 0; i < lengthMonthArray; ++i) {
            dow = monthArray[i];
            rule.setMonth(Month.of(dow), true);
        }

        rule.setDayOfMonths(jr.getDayOfMonth());
        Integer[] dayArray = stringToIntList(jr.getDayOfWeek());
        int lengthDayArray = dayArray.length;

        for (i = 0; i < lengthDayArray; ++i) {
            dow = dayArray[i];
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
        } catch (JsonProcessingException var4) {
            Logger.getLogger(SchedulerHandler.class.getName()).log(Level.SEVERE, "Scheduler JSON import to string failed", var4);
        }

        return str;
    }

    private static void jsonToFile(String path, JsonScheduler json) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(path), json);
        } catch (IOException var4) {
            Logger.getLogger(SchedulerHandler.class.getName()).log(Level.SEVERE, "Scheduler JSON import to file failed", var4);
        }

    }

    private static JsonScheduler buildDefaultJson() {
        return new JsonScheduler();
    }

    private static JsonScheduler stringToJson(String json) throws IOException {
        return inputToJson(json);
    }

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
        } else {
            throw new IllegalArgumentException("argument is null or json corrupt");
        }
    }
}
