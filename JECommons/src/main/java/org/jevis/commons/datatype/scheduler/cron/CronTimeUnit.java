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
 *
 * @author Artur Iablokov
 */
public abstract class CronTimeUnit {
    
    public static final String NODATA = "";
    /**
     * hh:mm = false, cron format = true
     */
    protected boolean isAlias = false;
    /**
     * if cronformat "'*' / denominator "
     */
    protected int denominator;
    
    public abstract String getValue();
    
    public boolean isAlias(){
        return isAlias;
    }
    
    public int getDenominator(){
        return denominator;
    }
    
    protected boolean isNumeratorValid(String num) {
        return num.equals("*");
    }

    protected boolean isDenominatorValid(String denom, int lim) {
        int den;
        try{
            den = Integer.parseInt(denom);
        } catch (NumberFormatException e){
            return false;
        }
        return (den>=0 && den<=lim);
    }
    
    protected boolean isValueValid(String t, int lim){
        return isDenominatorValid(t, lim);
    }
    
}
