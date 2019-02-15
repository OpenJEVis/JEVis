/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm.deprecated.limitalarm;

import org.jevis.api.JEVisException;

/**
 *
 * @author ai
 */
public interface ILimitAlarm {
    
    /**
     * Sets the parameters for the Alarm object from the JEVis class
     * (Static / Dynamic Limit Alarm).
     * 
     */
    void init();
    
    /**
     * Checks the data since the last checking in accord specific
     * conditions for a given alarm, and writes the result to the log.
     * 
     * @throws JEVisException 
     */
    void checkAlarm() throws JEVisException;
}
