package org.jevis.jeconfig.plugin.dashboard.widget;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Helper {
    private static final Logger logger = LogManager.getLogger(Helper.class);

    public static  double convertToPercent(double value, double maximum,double minimum, int decimalPlaces) {
        BigDecimal bd;
        if (maximum > 0 && maximum>minimum) {
            try{

                bd = new BigDecimal((value-minimum) / (maximum-minimum) * 100).setScale(decimalPlaces, RoundingMode.HALF_DOWN);
                return bd.doubleValue();
            }
            catch (Exception e){
                logger.error("Failed to convert value to percent", e);
                return 0;
            }
        } else return 0;


    }

}
