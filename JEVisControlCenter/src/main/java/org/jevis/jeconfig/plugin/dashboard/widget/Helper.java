package org.jevis.jeconfig.plugin.dashboard.widget;

import org.jevis.api.JEVisException;
import org.joda.time.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Helper {
    public static  double convertToPercent(double value, double maximum,double minimum, int decimalPlaces) {
        BigDecimal bd;
        if (maximum > 0 && maximum>minimum) {
            try{

                bd = new BigDecimal((value-minimum) / (maximum-minimum) * 100).setScale(decimalPlaces, RoundingMode.HALF_DOWN);
                return bd.doubleValue();
            }
            catch (Exception e){
                e.printStackTrace();
                return 0;
            }
        } else return 0;


    }

}
