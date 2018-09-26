/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevCalendar;
import envidatec.jevis.capi.data.TimeSet;
import envidatec.jevis.capi.nodes.NodeManager;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



/**
 *
 * @author broder
 */
public class Property {

    private JevCalendar nextCreationDate;  //heißt im Config noch nextCreationDate
    private JevCalendar endCreationDate;
    private JevCalendar startRecord;
    private String schedule;
    private int period;
    private TimeSet timeSet;
    private TimeSet oldTimeSet;
    private TimeSet oldOldTimeSet;
    private TimeSet lastYearTimeSet;
    private TimeSet oldLastYearTimeSet;
    private String emails;
    private boolean dynamic;
    private String _host;
    private int _port;
    private String _user;
    private String _pass;
    private String _subject;
    private static final Logger _logger = Logger.getLogger(ReportHandler.class.getName());

    public Property(RegTreeNode reportNode) {
        NodeManager nm = NodeManager.getInstance();

        Long test = reportNode.getPropertyNode("Schedule").getLinkID();
        schedule = nm.getDefinitionNode(test).getName();

        period = Integer.valueOf(reportNode.getPropertyNode("Period").getCurrentValue().getVal().toString());

        _host = reportNode.getPropertyNode("Host").getCurrentValue().getVal().toString();
        _port = Integer.valueOf(reportNode.getPropertyNode("Port").getCurrentValue().getVal().toString());
        _user = reportNode.getPropertyNode("User").getCurrentValue().getVal().toString();
        _pass = reportNode.getPropertyNode("Password").getCurrentValue().getVal().toString();
        _subject = reportNode.getPropertyNode("Subject").getCurrentValue().getVal().toString();

        String creationDate = reportNode.getPropertyNode("Next Creation Date").getCurrentValue().getVal().toString();  //UTC!!!!
        nextCreationDate = setPropertyDate(creationDate);
        nextCreationDate = setRightTimezone(nextCreationDate);


        String startRecordDate = reportNode.getPropertyNode("Start record").getCurrentValue().getVal().toString();
//        logger.info("startrecoddate " + startRecordDate);
        //sollte in die setTimeSet methode
        JevCalendar startTimeStamp = setPropertyDate(startRecordDate);
        startRecord = startTimeStamp.clone();
        startRecord = setRightTimezone(startRecord);
//        logger.info("startrecod " + startRecord);

        endCreationDate = startTimeStamp.clone();
        endCreationDate.add(getScheduleAsInt(), period);
//        logger.info("ENDCREATIONDATE " + endCreationDate);
        endCreationDate = setRightTimezone(endCreationDate);

        setTimeSet();

        setTimeSetOld(startTimeStamp);
        setLastYearTimeSet(startTimeStamp);
        setTimeSetOldOld(startTimeStamp);
        setOldLastYearTimeSet(startTimeStamp);

        dynamic = false;
        if (reportNode.getPropertyNode("dynamic").getCurrentValue().getVal().toString().equals("1")) {
            dynamic = true;
        }



        emails = reportNode.getPropertyNode("E-mail list").getCurrentValue().getVal().toString();


        _logger.log(Level.FINEST, "Schedule " + schedule);
        _logger.log(Level.FINEST, "Period " + period);
        _logger.log(Level.FINEST, "StartRecordDate " + startRecordDate);
        _logger.log(Level.FINEST, "NextCreationData " + endCreationDate);
//        logger.info("PopertyEND");
    }

    private JevCalendar setPropertyDate(String date) {
        String tmp = date.replace("-", " ");
        String tmp2 = tmp.replace(":", " ");
        String datearray[] = tmp2.split(" ");

        JevCalendar tempCal = new JevCalendar(Integer.parseInt(datearray[0]), Integer.parseInt(datearray[1]), Integer.parseInt(datearray[2]), Integer.parseInt(datearray[3]), Integer.parseInt(datearray[4]));

//        logger.info("TEMPCAL " + tempCal);
//        logger.info("zeitzone " + tempCal.getTimeZone().getDisplayName());
        return tempCal;
    }

    private JevCalendar setRightTimezone(JevCalendar tempCal) {
        JevCalendar returnCal = new JevCalendar(new Date(tempCal.getTimeInMillis()
                - Calendar.getInstance().getTimeZone().getOffset(tempCal.clone().getTimeInMillis())));
//        JevCalendar tmpCal = new JevCalendar(Integer.parseInt(datearray[0]), Integer.parseInt(datearray[1]), Integer.parseInt(datearray[2]), Integer.parseInt(datearray[3]) - 2, Integer.parseInt(datearray[4]), TimeZone.getTimeZone(Locale.getDefault().getDisplayCountry()));
//        logger.info("returncal zeitzone " + returnCal.getTimeZone().getDisplayName());
        return returnCal;
    }

    private void setTimeSet() {

        timeSet = new TimeSet(startRecord, new JevCalendar(new Date(endCreationDate.getTimeInMillis() - 1)));
        logger.info("Date from " + timeSet.getFrom());
        logger.info("Date till " + timeSet.getUntil());
//        logger.info("Timezone " + timeSet.getFrom().getTimeZone().getDisplayName());
    }

    //TODO getUntil muss um einen verringert werden?
    private void setTimeSetOld(JevCalendar start) {
        JevCalendar tempCal = start.clone();
        tempCal.add(getScheduleAsInt(), period * -1);
        tempCal = setRightTimezone(tempCal);
        JevCalendar tempUntil = new JevCalendar(new Date(startRecord.getTimeInMillis() - 1));
        oldTimeSet = new TimeSet(tempCal, tempUntil);

//        logger.info("Date from OLD " + tempCal);
//        logger.info("Date till OLD " + tempUntil);
    }

    private void setTimeSetOldOld(JevCalendar start) {
        JevCalendar tempCal = start.clone();
        tempCal.add(getScheduleAsInt(), period * -2);
        tempCal = setRightTimezone(tempCal);
        JevCalendar tempUntil = start.clone();
        tempUntil.add(getScheduleAsInt(), period * -1);
        tempUntil = setRightTimezone(tempUntil);
        JevCalendar tempTempUntil = new JevCalendar(new Date(tempUntil.getTimeInMillis() - 1));
        oldOldTimeSet = new TimeSet(tempCal, tempTempUntil);

//        logger.info("Date from OLDOLD " + tempCal);
//        logger.info("Date till OLDOLD " + tempUntil);
    }

    private void setLastYearTimeSet(JevCalendar start) {
        JevCalendar tempCal = start.clone();
        tempCal.add(Calendar.YEAR, -1);
        tempCal = setRightTimezone(tempCal);
        JevCalendar tempUntil = start.clone();
        tempUntil.add(getScheduleAsInt(), period * 1);
        tempUntil.add(Calendar.YEAR, -1);     //TODO vllt muss man hier auch noch den kompletten Calendar neu erstellen und verschieben
        tempUntil = setRightTimezone(tempUntil);
        JevCalendar tempTempUntil = new JevCalendar(new Date(tempUntil.getTimeInMillis() - 1));
        lastYearTimeSet = new TimeSet(tempCal, tempTempUntil);
//        logger.info("Date from LASTYEAR " + lastYearTimeSet.getFrom());
//        logger.info("Date till LASTYEAR " + lastYearTimeSet.getUntil());
    }

    public TimeSet getTimeSet() {
        return timeSet;
    }

    public TimeSet getOldTimeSet() {
        return oldTimeSet;
    }

    public int getScheduleAsInt() {
        if (schedule.equals("DAILY")) {
            return Calendar.DATE;
        } else if (schedule.equals("MONTHLY")) {
            return Calendar.MONTH;
        } else if (schedule.equals("WEEKLY")) {
            return Calendar.WEEK_OF_YEAR;
        } else if (schedule.equals("YEARLY")) {
            return Calendar.YEAR;
        }
        return -1;
    }

    public String getMails() {
        return emails;
    }

    public JevCalendar getStartRecord() {
        return startRecord;
    }

    public JevCalendar getNextCreationDate() {
        return nextCreationDate;
    }

    public int getPeriod() {
        return period;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _pass;
    }

    public String getSubject() {
        return _subject;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public TimeSet getLastYearTimeSet() {
        return lastYearTimeSet;
    }

    public TimeSet getOldOldTimeSet() {
        return oldOldTimeSet;
    }

    TimeSet getOldLastYearTimeSet() {
        return oldLastYearTimeSet;
    }

    private void setOldLastYearTimeSet(JevCalendar start) {
        JevCalendar tempCal = start.clone();
        tempCal.add(Calendar.YEAR, -1);
        tempCal.add(getScheduleAsInt(), period * -1);
        tempCal = setRightTimezone(tempCal);
        JevCalendar tempUntil = start.clone();
        tempUntil.add(Calendar.YEAR, -1);     //TODO vllt muss man hier auch noch den kompletten Calendar neu erstellen und verschieben
        tempUntil = setRightTimezone(tempUntil);
        JevCalendar tempTempUntil = new JevCalendar(new Date(tempUntil.getTimeInMillis() - 1));
        oldLastYearTimeSet = new TimeSet(tempCal, tempTempUntil);
//        logger.info("Date from OLDLASTYEAR " + oldLastYearTimeSet.getFrom());
//        logger.info("Date till OLDLASTYEAR " + oldLastYearTimeSet.getUntil());
    }
}
