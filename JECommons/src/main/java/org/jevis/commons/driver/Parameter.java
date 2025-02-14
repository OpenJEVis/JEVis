package org.jevis.commons.driver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Parameter implements VarFiller.VarFunction {
    @Expose
    @SerializedName("format")
    private final StringProperty format = new SimpleStringProperty();
    @Expose
    @SerializedName("Parameter")
    private final ObjectProperty<VarFiller.Variable> variable = new SimpleObjectProperty<>();

    @Expose
    @SerializedName("Timezone")
    private final StringProperty timezone = new SimpleStringProperty(DateTimeZone.UTC.getID());

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
                    return fmt.print(dateTime.withZone(DateTimeZone.forID(timezone.get())));
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
            System.out.println(timezone.get());
            System.out.println(getLastTS().withZone(DateTimeZone.forID(timezone.get())));


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
        return format.get();
    }

    public void setFormat(String format) {
        this.format.set(format);
    }

    public StringProperty formatProperty() {
        return format;
    }

    public VarFiller.Variable getVariable() {
        return variable.get();
    }

    public void setVariable(VarFiller.Variable variable) {
        this.variable.set(variable);
    }

    public ObjectProperty<VarFiller.Variable> variableProperty() {
        return variable;
    }

    public String getTimezone() {
        return timezone.get();
    }

    public void setTimezone(String timezone) {
        this.timezone.set(timezone);
    }

    public StringProperty timezoneProperty() {
        return timezone;
    }
}
