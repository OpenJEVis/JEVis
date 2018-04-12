package org.jevis.commons.datatype;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Month;
import org.jevis.commons.datatype.scheduler.SchedulerHandler;
import org.jevis.commons.datatype.scheduler.cron.CronRule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.datatype.scheduler.handler.SchedulerContainsHandler;

/**
 *
 * @author Artur Iablokov
 */
public class ScheduleTest {

    //private ScheduleHandler handler;
    private org.joda.time.format.DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private DateTime dt;
    private CronScheduler otherScheduler;
    private CronScheduler scheduler = SchedulerHandler.BuildDefaultScheduler();
    private SchedulerContainsHandler contHandler = new SchedulerContainsHandler();
    String jsonString;

    @Before
    public void setUp() {

        scheduler.setDatetTimeZone(DateTimeZone.forID("Europe/Berlin"));
        SchedulerRule r = new CronRule();
        r.setMonth(Month.SEPTEMBER, true);
        r.setDayOfMonths("*");
        r.setDayOfWeek(DayOfWeek.MONDAY, true);
        r.setDayOfWeek(DayOfWeek.TUESDAY, true);
        r.setDayOfWeek(DayOfWeek.WEDNESDAY, true);
        r.setDayOfWeek(DayOfWeek.THURSDAY, true);
        r.setDayOfWeek(DayOfWeek.FRIDAY, true);
        r.setStartHour("10");
        r.setStartMinute("00");
        r.setEndHour("16");
        r.setEndMinute("00");
        scheduler.addRule(r);

        dt = dtf.parseDateTime("2017-09-06 13:32:12").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
    }

    @Test
    public void posTest() {
        boolean b = contHandler.contains(dt, scheduler);
        assertTrue(b == true);
    }

    @Test
    public void posTest2() throws IOException {
        String jString = SchedulerHandler.SerializeScheduler(scheduler);
        otherScheduler = SchedulerHandler.BuildScheduler(jString);
        boolean b = contHandler.contains(dt, scheduler);
        assertTrue(b == true);
    }

    @Test
    public void negTest() {
        DateTime ndt = dt.plusHours(2).plusMinutes(30);
        boolean b = contHandler.contains(ndt, scheduler);
        assertTrue(b == false);
    }

    @Test
    public void posTest3() {
        DateTime ndt = dt.plusHours(2).plusMinutes(30);
        scheduler.getAllRules().get(0).setEndMinute("15");
        boolean b = contHandler.contains(ndt, scheduler);
        assertTrue(b == true);
    }

    @Test(expected = NumberFormatException.class)
    public void invalidValueForConatains() {
        DateTime ndt = dt.plusHours(2).plusMinutes(30);
        scheduler.getAllRules().get(0).setStartHour("*");
        boolean b = contHandler.contains(ndt, scheduler);
        assertTrue(b == false);
    }

    @Test
    public void neg2Test() {
        scheduler.getAllRules().get(0).setDayOfWeek(DayOfWeek.WEDNESDAY, false);
        boolean b = contHandler.contains(dt, scheduler);
        assertTrue(b == false);
    }

    @Test
    public void neg3Test() {
        scheduler.getAllRules().get(0).setDayOfMonths("1,2,3,4,5,7,8,9,10");
        scheduler.getAllRules().get(0).setDayOfWeek(DayOfWeek.SUNDAY, true);
        boolean b = contHandler.contains(dt, scheduler);
        assertTrue(b == false);
        DateTime ndt = dtf.parseDateTime("2017-09-10 13:32:12").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void monthTest() {
        scheduler.getAllRules().get(0).setMonth(Month.SEPTEMBER, false);
        scheduler.getAllRules().get(0).setMonth(Month.AUGUST, true);
        boolean b = contHandler.contains(dt, scheduler);
        assertTrue(b == false);
    }

    @Test
    public void twoSchedulers() {
        scheduler.getAllRules().get(0).setMonth(Month.SEPTEMBER, false);
        scheduler.getAllRules().get(0).setMonth(Month.AUGUST, true);
        CronRule r2 = new CronRule();
        r2.setMonth(Month.SEPTEMBER, true);
        r2.setDayOfWeek(DayOfWeek.WEDNESDAY, true);
        r2.setDayOfMonths("*");
        r2.setStartHour("13");
        r2.setStartMinute("00");
        r2.setEndHour("14");
        r2.setEndMinute("00");
        scheduler.addRule(r2);
        boolean b = contHandler.contains(dt, scheduler);
        assertTrue(b == true);
    }
}
