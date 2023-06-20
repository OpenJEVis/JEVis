package org.jevis.commons.driver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Parameter implements VarFiller.VarFunction {
    @Expose
    @SerializedName("format")
    private StringProperty format = new SimpleStringProperty();
    @Expose
    @SerializedName("Parameter")
    private ObjectProperty<VarFiller.Variable> variable = new SimpleObjectProperty<>();
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
        }
        return null;
    }

    private String format(String format, DateTime dateTime) {
        try {
            if (format != null) {
                DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
                return fmt.print(dateTime);
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
            return format(getFormat(), getLastTS());
        } else {
            return format(getFormat(), new DateTime(1980, 01, 01, 01, 01));
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

    public StringProperty formatProperty() {
        return format;
    }

    public void setFormat(String format) {
        this.format.set(format);
    }

    public VarFiller.Variable getVariable() {
        return variable.get();
    }

    public ObjectProperty<VarFiller.Variable> variableProperty() {
        return variable;
    }

    public void setVariable(VarFiller.Variable variable) {
        this.variable.set(variable);
    }
}
