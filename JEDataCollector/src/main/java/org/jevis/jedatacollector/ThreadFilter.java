/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author bf
 */
public class ThreadFilter extends Filter {

    private String _value;

    public ThreadFilter(String key) {
        _value = key;
    }

    @Override
    public int decide(LoggingEvent le) {
        String mdc = (String) le.getMDC(Launcher.KEY);
//        System.out.println("MDC: " + mdc);
//        System.out.println("key: " + _value);
        if (mdc.equals(_value)) {
            return Filter.ACCEPT;
        } else {
            return Filter.DENY;
        }
//        return Filter.ACCEPT;
    }
}
