package org.jevis.mscons;

import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Messlokation {
    private String messlokationNumber;
    private DateTime fromDateTime;

    private DateTime untilDateTime;
    List<MsconsSample> sampleList = new ArrayList<>();

    public Messlokation(String messlokationNumber) {
        this.messlokationNumber = messlokationNumber;
    }

    public String getMesslokationNumber() {
        return messlokationNumber;
    }

    public void setMesslokationNumber(String messlokationNumber) {
        this.messlokationNumber = messlokationNumber;
    }

    public DateTime getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(DateTime fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public DateTime getUntilDateTime() {
        return untilDateTime;
    }

    public void setUntilDateTime(DateTime untilDateTime) {
        this.untilDateTime = untilDateTime;
    }

    public List<MsconsSample> getSampleList() {
        return sampleList;
    }

    public void setSampleList(List<MsconsSample> sampleList) {
        this.sampleList = sampleList;
    }



    @Override
    public String toString() {
        return "Messlokation{" +
                "messlokationNumber='" + messlokationNumber + '\'' +
                ", fromDateTime=" + fromDateTime +
                ", untilDateTime=" + untilDateTime +
                '}';
    }

    enum StartEnd {
        Start,END
    }
}
