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

import org.jevis.commons.datatype.scheduler.cron.CronTimeUnit;
import java.security.InvalidParameterException;

/**
 *
 * @author Artur Iablokov
 */
public class CronMinute extends CronTimeUnit {
    
    private static final int LIMIT = 59;
    private String min;
    
    protected void setValue(String str) throws InvalidParameterException {
        
        String tmp = str.replaceAll(" ", "");
        if (tmp.equalsIgnoreCase(NODATA)) {
            tmp = "*";
            isAlias = true;
            denominator = 0; 
        } else if (tmp.equals("*")) {
            isAlias = true;
            denominator = 0; 
        } else if (tmp.contains("/")) {
            String[] splStr = tmp.split("/");
            if (splStr.length == 2 && isNumeratorValid(splStr[0]) && isDenominatorValid(splStr[1], LIMIT)) {
                isAlias = true;
                denominator = Integer.parseInt(splStr[1]); 
            }
        } else {
            if(!isValueValid(tmp, LIMIT)){
                throw new InvalidParameterException("time parameter "+ tmp +" is wrong");    
            }
        }
        min = tmp;
    }
    
    @Override
    public String getValue(){
        return min;
    }
    
}
