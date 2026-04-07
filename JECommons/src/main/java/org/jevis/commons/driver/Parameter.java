package org.jevis.commons.driver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Parameter implements VarFiller.VarFunction {
    @Expose
    @SerializedName("format")
    private String format;
    @Expose
    @SerializedName("Parameter")
    private VarFiller.Variable variable;

    @Expose
    @SerializedName("Timezone")
    private String timezone = DateTimeZone.UTC.getID();

    @JsonIgnore
    private DateTime lastTS;

    @JsonIgnore
    private DateTime currentTS;


    @Override
    public String getVarValue() {
        if (getVariable().equals(VarFiller.Variable.LAST_TS)) {
            return getLasTs();

        } else if (getVariable().equals(VarFiller.Variable.CURRENT_TS)) {
            return getCurrentTs();
        } else if (getVariable().equals(VarFiller.Variable.CURRENT_TS2)) {
            return getCurrentTs();
        }
        return null;
    }

    private String format(String format, DateTime dateTime) {
        try {
            if (format != null) {
                if (format.equals("UNIX")) {
                    return Long.toString(dateTime.getMillis() / 1000L);
                } else {
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
                    return fmt.print(dateTime.withZone(DateTimeZone.forID(timezone)));
                }


            } else {
                return dateTime.toString();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String getCurrentTs() {
        return format(getFormat(), getCurrentTS());
    }

    private String getLasTs() {
        if (lastTS != null) {
            System.out.println(getLastTS());
            System.out.println(timezone);
            System.out.println(getLastTS().withZone(DateTimeZone.forID(timezone)));


            return format(getFormat(), getLastTS());
        } else {
            return format(getFormat(), new DateTime(1980, 1, 1, 1, 1));
        }
    }


    @Override
    public String toString() {
        return "HTTPParameter{" + "format=" + format + ", variable=" + variable + ", lastTS=" + lastTS + ", currentTS=" + currentTS + '}';
    }

    public DateTime getLastTS() {
        return lastTS;
    }

    public void setLastTS(DateTime lastTS) {
        this.lastTS = lastTS;
    }

    public DateTime getCurrentTS() {
        return currentTS;
    }

    public void setCurrentTS(DateTime currentTS) {
        this.currentTS = currentTS;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public VarFiller.Variable getVariable() {
        return variable;
    }

    public void setVariable(VarFiller.Variable variable) {
        this.variable = variable;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
