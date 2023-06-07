package org.jevis.jecc.application.jevistree.methods;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class AutoLimitSetting {

    private DateTime startDate;
    private DateTime endDate;
    private boolean minIsZero = true;
    private BigDecimal limit1MinSub = BigDecimal.valueOf(0);
    private BigDecimal limit1MaxAdd = BigDecimal.valueOf(15);
    private BigDecimal limit1MinTimesXLimit2Min = BigDecimal.valueOf(2);
    private BigDecimal limit1MaxTimesXLimit2Max = BigDecimal.valueOf(2);

    public AutoLimitSetting() {
    }

    public AutoLimitSetting(DateTime startDate, DateTime endDate, boolean minIsZero,
                            BigDecimal limit1MinSub, BigDecimal limit1MaxAdd,
                            BigDecimal limit1MinTimesXLimit2Min,
                            BigDecimal limit1MaxTimesXLimit2Max) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.minIsZero = minIsZero;
        this.limit1MinSub = limit1MinSub;
        this.limit1MaxAdd = limit1MaxAdd;
        this.limit1MinTimesXLimit2Min = limit1MinTimesXLimit2Min;
        this.limit1MaxTimesXLimit2Max = limit1MaxTimesXLimit2Max;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isMinIsZero() {
        return minIsZero;
    }

    public void setMinIsZero(boolean minIsZero) {
        this.minIsZero = minIsZero;
    }

    public BigDecimal getLimit1MinSub() {
        return limit1MinSub;
    }

    public void setLimit1MinSub(BigDecimal limit1MinSub) {
        this.limit1MinSub = limit1MinSub;
    }

    public BigDecimal getLimit1MaxAdd() {
        return limit1MaxAdd;
    }

    public void setLimit1MaxAdd(BigDecimal limit1MaxAdd) {
        this.limit1MaxAdd = limit1MaxAdd;
    }

    public BigDecimal getLimit1MinTimesXLimit2Min() {
        return limit1MinTimesXLimit2Min;
    }

    public void setLimit1MinTimesXLimit2Min(BigDecimal limit1MinTimesXLimit2Min) {
        this.limit1MinTimesXLimit2Min = limit1MinTimesXLimit2Min;
    }

    public BigDecimal getLimit1MaxTimesXLimit2Max() {
        return limit1MaxTimesXLimit2Max;
    }

    public void setLimit1MaxTimesXLimit2Max(BigDecimal limit1MaxTimesXLimit2Max) {
        this.limit1MaxTimesXLimit2Max = limit1MaxTimesXLimit2Max;
    }
}
