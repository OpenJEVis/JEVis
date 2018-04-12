/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAlarm.
 *
 * JEAlarm is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAlarm is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAlarm. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAlarm is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jealarm;

/**
 * This Class holds the configuration of an Alarm
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Alarm {

    private String _subject;
    private String[] _datapoint;
    private String[] _recipient;
    private int _ignore;
    private int _timelimit;
    private String _messag;
    private String _greeting;
    private boolean _ignoreFalse;
    private String[] _bcc;

    /**
     * Create an new alarm configuration
     *
     * @param subject
     * @param datapoint
     * @param recipient
     * @param bcc
     * @param timelimit
     * @param ignorelimit
     * @param greeting
     * @param message
     * @param ignoreFalse
     */
    public Alarm(String subject, String[] datapoint, String[] recipient, String[] bcc, int timelimit, int ignorelimit, String greeting, String message, boolean ignoreFalse) {
        _subject = subject;
        _datapoint = datapoint;
        _recipient = recipient;
        _bcc = bcc;
        _timelimit = timelimit;
        _messag = message;
        _greeting = greeting;
        _ignore = ignorelimit;
        _ignoreFalse = ignoreFalse;

    }

    /**
     * returns the blind copy recipients
     *
     * @return
     */
    public String[] getBcc() {
        return _bcc;
    }

    /**
     * returns if an emty mail should be send if no alarm accourd
     *
     * @return
     */
    public boolean isIgnoreFalse() {
        return _ignoreFalse;
    }

    /**
     * Returns the Datatime limit of alarms who should be ignored
     *
     * @return
     */
    public int getIgnoreOld() {
        return _ignore;
    }

    /**
     * Returns the greeting message for the mail
     *
     * @return
     */
    public String getGreeting() {
        return _greeting;
    }

    /**
     * returns the subject for the mail
     *
     * @return
     */
    public String getSubject() {
        return _subject;
    }

    /**
     * Returns an array of datapoints to check
     *
     * @return
     */
    public String[] getDatapoint() {
        return _datapoint;
    }

    /**
     * returns an list of recipient for the mail
     *
     * @return
     */
    public String[] getRecipient() {
        return _recipient;
    }

    /**
     * returns the timelimit in hours
     *
     * @return
     */
    public int getTimeLimit() {
        return _timelimit;
    }

    /**
     * returns the body message for the mail
     *
     * @return
     */
    public String getMessage() {
        return _messag;
    }

}
