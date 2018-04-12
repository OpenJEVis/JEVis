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
package org.jevis.commons.datatype.scheduler.cron;

/**
 * Represent time "HH:mm" or
 * cron hour, minute like a "*" "'*'/2"
 * @author Artur Iablokov
 */
public class  CronTime {

    private CronHour hour;
    private CronMinute min;
    /**
     * gets hour object
     * @return 
     */
    public CronHour getHour() {
        return hour;
    }
    /**
     * sets hour object
     * @param h 
     */
    public void setHour(String h) {
        this.hour = new CronHour();
        this.hour.setValue(h);
    }
    /**
     * gets minute object
     * @return 
     */
    public CronMinute getMin() {
        return min;
    }
    /**
     * sets minute object
     * @param m 
     */
    public void setMin(String m) {
        this.min = new CronMinute();
        this.min.setValue(m);
    }    
}
