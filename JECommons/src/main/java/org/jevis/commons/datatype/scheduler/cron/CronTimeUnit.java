//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.cron;

public abstract class CronTimeUnit {
    public static final String NODATA = "";
    protected boolean isAlias = false;
    protected int denominator;

    public CronTimeUnit() {
    }

    public abstract String getValue();

    public boolean isAlias() {
        return this.isAlias;
    }

    public int getDenominator() {
        return this.denominator;
    }

    protected boolean isNumeratorValid(String num) {
        return num.equals("*");
    }

    protected boolean isDenominatorValid(String denom, int lim) {
        int den;
        try {
            den = Integer.parseInt(denom);
        } catch (NumberFormatException var5) {
            return false;
        }

        return den >= 0 && den <= lim;
    }

    protected boolean isValueValid(String t, int lim) {
        return this.isDenominatorValid(t, lim);
    }
}
