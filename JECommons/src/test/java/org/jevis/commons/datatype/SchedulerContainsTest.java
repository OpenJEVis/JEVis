/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.datatype;

import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.Month;
import org.jevis.commons.datatype.scheduler.SchedulerHandler;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.jevis.commons.datatype.scheduler.cron.CronRule;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.datatype.scheduler.handler.SchedulerContainsHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jsc
 */
public class SchedulerContainsTest {

    private CronScheduler scheduler = SchedulerHandler.BuildDefaultScheduler();
    private DateTime dt;
    private org.joda.time.format.DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private SchedulerContainsHandler contHandler = new SchedulerContainsHandler();

    @Before
    public void setUp() {
        scheduler.setDatetTimeZone(DateTimeZone.forID("Europe/Berlin"));
        SchedulerRule r = new CronRule();
        r.setMonth(Month.SEPTEMBER, true);
        r.setMonth(Month.APRIL, true);
        r.setMonth(Month.MAY, true);
        r.setDayOfMonths("1, LAST");
        r.setDayOfWeek(DayOfWeek.MONDAY, true);
        r.setDayOfWeek(DayOfWeek.TUESDAY, true);
        r.setDayOfWeek(DayOfWeek.WEDNESDAY, true);
        r.setDayOfWeek(DayOfWeek.THURSDAY, true);
        r.setDayOfWeek(DayOfWeek.FRIDAY, true);
        r.setStartHour("8");
        r.setStartMinute("50");
        r.setEndHour("18");
        r.setEndMinute("18");
        scheduler.addRule(r);

        SchedulerRule r3 = new CronRule();
        r3.setMonth(Month.SEPTEMBER, true);
        r3.setMonth(Month.APRIL, true);
        r3.setMonth(Month.MAY, true);
        r3.setDayOfMonths("1, LAST");
        r3.setDayOfWeek(DayOfWeek.SATURDAY, true);
        r3.setStartHour("16");
        r3.setStartMinute("30");
        r3.setEndHour("17");
        r3.setEndMinute("00");
        scheduler.addRule(r3);

        SchedulerRule r2 = new CronRule();
        r2.setMonth(Month.SEPTEMBER, true);
        r2.setMonth(Month.APRIL, true);
        r2.setMonth(Month.MAY, true);
        r2.setDayOfMonths("1, LAST");
        r2.setDayOfWeek(DayOfWeek.SUNDAY, true);
        r2.setStartHour("22");
        r2.setStartMinute("20");
        r2.setEndHour("23");
        r2.setEndMinute("59");
        scheduler.addRule(r2);

        SchedulerRule r22 = new CronRule();
        r22.setMonth(Month.APRIL, true);
        r22.setMonth(Month.MAY, true);
        r22.setMonth(Month.SEPTEMBER, true);
        r22.setDayOfMonths("1, LAST");
        r22.setDayOfWeek(DayOfWeek.MONDAY, true);
        r22.setStartHour("00");
        r22.setStartMinute("00");
        r22.setEndHour("03");
        r22.setEndMinute("15");
        scheduler.addRule(r22);

        //dt = dtf.parseDateTime("2017-09-06 13:32:12").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
    }

    //# 1. Tag des Monats ist Freitag
    @Test
    public void test1() {
        DateTime ndt = dtf.parseDateTime("2017-09-01 08:49:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    @Test
    public void test2() {
        DateTime ndt = dtf.parseDateTime("2017-09-01 08:50:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test3() {
        DateTime ndt = dtf.parseDateTime("2017-09-01 18:18:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test4() {
        DateTime ndt = dtf.parseDateTime("2017-09-01 18:19:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    //# Letzter Tag des Monats ist Sonntag
    @Test
    public void test5() {
        DateTime ndt = dtf.parseDateTime("2017-09-30 16:29:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    @Test
    public void test6() {
        DateTime ndt = dtf.parseDateTime("2017-09-30 16:30:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test7() {
        DateTime ndt = dtf.parseDateTime("2017-09-30 17:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test8() {
        DateTime ndt = dtf.parseDateTime("2017-09-30 17:01:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    //#Letzter Tag Sonntag, erster Tag Montag. Übergang Nacht
    @Test
    public void test9() {
        DateTime ndt = dtf.parseDateTime("2017-04-30 22:19:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    @Test
    public void test10() {
        DateTime ndt = dtf.parseDateTime("2017-04-30 22:20:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test11() {
        DateTime ndt = dtf.parseDateTime("2017-04-30 23:59:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test12() {
        DateTime ndt = dtf.parseDateTime("2017-05-01 00:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test13() {
        DateTime ndt = dtf.parseDateTime("2017-05-01 03:15:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == true);
    }

    @Test
    public void test14() {
        DateTime ndt = dtf.parseDateTime("2017-05-01 03:16:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    //# Nicht erster oder letzter Tag
    @Test
    public void test15() {
        DateTime ndt = dtf.parseDateTime("2017-09-10 12:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        boolean bb = contHandler.contains(ndt, scheduler);
        assertTrue(bb == false);
    }

    @Test
    public void robustTest1() {
        DateTime ndt = dtf.parseDateTime("2018-01-01 12:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        CronScheduler s = SchedulerHandler.BuildDefaultScheduler();
        SchedulerContainsHandler h = new SchedulerContainsHandler();
        boolean bb = h.contains(ndt, s);
        assertTrue(bb == false);
    }

    @Test
    public void robustTest2() {
        DateTime ndt = dtf.parseDateTime("2018-01-01 12:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        CronScheduler s = SchedulerHandler.BuildDefaultScheduler();
        SchedulerRule r = new CronRule();
        s.addRule(r);
        SchedulerContainsHandler h = new SchedulerContainsHandler();
        boolean bb = h.contains(ndt, s);
        assertTrue(bb == false);
    }

    @Test (expected = InvalidParameterException.class)
    public void robustTest3() {
        DateTime ndt = dtf.parseDateTime("2018-01-01 12:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        CronScheduler s = SchedulerHandler.BuildDefaultScheduler();
        SchedulerRule r = new CronRule();
        r.setStartHour("ABC");
        s.addRule(r);
        SchedulerContainsHandler h = new SchedulerContainsHandler();
        boolean bb = h.contains(ndt, s);
        assertTrue(bb == false);
    }
    
    @Test (expected = InvalidParameterException.class)
    public void robustTest4() {
        DateTime ndt = dtf.parseDateTime("2018-01-01 12:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        CronScheduler s = SchedulerHandler.BuildDefaultScheduler();
        SchedulerRule r = new CronRule();
        r.setAllMonths(true);
        r.setDayOfMonths("SOME WRONG STRING");
        s.addRule(r);
        SchedulerContainsHandler h = new SchedulerContainsHandler();
        boolean bb = h.contains(ndt, s);
        assertTrue(bb == false);
    }
    
    @Test (expected = InvalidParameterException.class)
    public void robustTest5() {
        DateTime ndt = dtf.parseDateTime("2018-01-01 12:00:00").withZoneRetainFields(DateTimeZone.forID("Europe/Berlin"));
        CronScheduler s = SchedulerHandler.BuildDefaultScheduler();
        SchedulerRule r = new CronRule();
        r.setStartHour("22");
        r.setStartMinute("20");
        r.setEndHour("23");
        r.setEndMinute("60");
        s.addRule(r);
        SchedulerContainsHandler h = new SchedulerContainsHandler();
        boolean bb = h.contains(ndt, s);
        assertTrue(bb == false);
    }

}
