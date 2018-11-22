/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm.usageperiod;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

/**
 * period is an integer value
 *
 * first 23 bits are hours - 0 bit = 00 h, 1 bit = 01 h,.... 23 bit = 23 h next
 * 7 bits (24-30 bits) are week days - 24 bit = Mo, 25 bit = Tu, .. 30 bit = Su
 * last bit (31 bit) = 0
 *
 * example: each Saturday and Sunday from 00:00 until 9:59 Bin form:
 * 01100000000000000000001111111111 Dez from: 1610613759
 *
 * tipp: use microsoft windows(R) calculator
 *
 * @author ai
 */
public class UsagePeriod {
    private static final Logger logger = LogManager.getLogger(UsagePeriod.class);

    private final int NODATA = 0; 
    private int _times;
    private int _days;
    private final int _type;

    public UsagePeriod(int type) {
        _type = type;
    }

    public int getType() {
        return _type;
    }

    public void setPeriod(JEVisObject alarmObj, String attrName) {
        _times = getPeriod(alarmObj, attrName);
        int period = _times;
        logger.info("hours bitmap: " + Integer.toBinaryString(_times));
        _days = ((period >> 24) & 0xFF);
        _days = _days * 2; // shift of 1 to the left (days = 1,2,..7 - check bit 1 not 0)
        logger.info("days bitmap: " + Integer.toBinaryString(_days));
    }
    
    private int getPeriod(JEVisObject alarmObj, String attrName) {
        try {
            int period;
            JEVisAttribute att = alarmObj.getAttribute(attrName);

            if (att == null) {
                logger.error("Attribute is null");
                return NODATA;
            }
            if (!att.hasSample()) {
                logger.error("Attribute has no samples");
                return NODATA;
            }

            JEVisSample lastS = att.getLatestSample();
            long val;
            try {
                val = lastS.getValueAsLong();
                return safeLongToInt(val);
            } catch (JEVisException ex) {
                logger.info("period is empty. defualt value added", ex);
                return NODATA;
            }
        } catch (Exception ex) {
            logger.error("Failed to get the value for the " + attrName, ex);
        }
        logger.error("Get attribute value failed");
        throw new NullPointerException();
    }

    public boolean isDayInPeriod(int val) {
        return isValueSelected(val, _days);
    }

    public boolean isTimeInPeriod(int val) {
        return isValueSelected(val, _times);
    }

    public boolean isValueSelected(int val, int n) {
        int tmp = n;
        int bitmask = 1;
        bitmask = bitmask << (val);
        System.err.println(Integer.toBinaryString(bitmask));
        return ((tmp & bitmask) != 0);
        //System.err.println(Integer.toBinaryString());
    }

    private int safeLongToInt(long val) {
        if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(val + " cannot be cast to int; long is to big");
        }
        return (int) val;
    }
}
