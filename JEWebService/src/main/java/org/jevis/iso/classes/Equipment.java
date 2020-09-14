/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Equipment {

    public static final String AttEnergySource = "Energy Source";
    public static final String AttMeasuringPoint = "Measuring Point";
    private long ID;
    private String name;
    private JsonObject object;
    private SQLDataSource ds;

    private Double DailyOperatingHours;
    private Long EnergySource;
    private String Manufacturer;
    private Long MeasuringPoint;
    private Double NominalPower;
    private Double WeightingFactor;
    private Double WorkWeeks;
    private Long WorkingDays;

    private Double yearlyconsumption;
    private Double shareoftotalconsumption;
    private Double co2emissions;
    private Double co2shareoftotal;

    Equipment(SQLDataSource ds, JsonObject input) throws Exception {
        final String AttDailyOperatingHours = "Daily Operating Hours";
        final String AttManufacturer = "Manufacturer";
        final String AttNominalPower = "Nominal Power";
        final String AttWeightingFactor = "Weighting Factor";
        final String AttWorkWeeks = "Work Weeks";
        final String AttWorkingDays = "Working Days";
        ID = 0L;
        name = "";
        DailyOperatingHours = 0.0;
        EnergySource = 0L;
        Manufacturer = "";
        MeasuringPoint = 0L;
        NominalPower = 0.0;
        WeightingFactor = 0.0;
        WorkWeeks = 0.0;
        WorkingDays = 0L;
        yearlyconsumption = 0.0;
        shareoftotalconsumption = 0.0;
        co2emissions = 0.0;
        co2shareoftotal = 0.0;
        this.ID = input.getId();
        this.name = input.getName();
        this.object = input;
        this.ds = ds;

        List<JsonAttribute> listAttributes = new ArrayList<>();

        listAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listAttributes) {
            String name = att.getType();

            switch (name) {
                case AttDailyOperatingHours:
                    if (getValueString(att, "") != "") {
                        this.setDailyOperatingHours(Double.parseDouble(getValueString(att, "")));
                    } else this.setDailyOperatingHours(0.0);
                    break;
                case AttEnergySource:
                    if (getValueString(att, "") != "") {
                        this.setEnergySource(Long.parseLong(getValueString(att, "")));
                    } else this.setEnergySource(0L);
                    break;
                case AttManufacturer:
                    this.setManufacturer(getValueString(att, ""));
                    break;
                case AttMeasuringPoint:
                    if (getValueString(att, "") != "") {
                        this.setMeasuringPoint(Long.parseLong(getValueString(att, "")));
                    } else this.setMeasuringPoint(0L);
                    break;
                case AttNominalPower:
                    if (getValueString(att, "") != "") {
                        this.setNominalPower(Double.parseDouble(getValueString(att, "")));
                    } else this.setNominalPower(0.0);
                    break;
                case AttWeightingFactor:
                    if (getValueString(att, "") != "") {
                        this.setWeightingFactor(Double.parseDouble(getValueString(att, "")));
                    } else this.setWeightingFactor(0.0);
                    break;
                case AttWorkWeeks:
                    if (getValueString(att, "") != "") {
                        this.setWorkWeeks(Double.parseDouble(getValueString(att, "")));
                    } else this.setWorkWeeks(0.0);
                    break;
                case AttWorkingDays:
                    if (getValueString(att, "") != "") {
                        this.setWorkingDays(Long.parseLong(getValueString(att, "")));
                    } else this.setWorkingDays(0L);
                    break;
                default:
                    break;
            }
        }

    }

    public Equipment() {
        final String AttDailyOperatingHours = "Daily Operating Hours";
        final String AttManufacturer = "Manufacturer";
        final String AttNominalPower = "Nominal Power";
        final String AttWeightingFactor = "Weighting Factor";
        final String AttWorkWeeks = "Work Weeks";
        final String AttWorkingDays = "Working Days";
        ID = 0L;
        name = "";
        DailyOperatingHours = 0.0;
        EnergySource = 0L;
        Manufacturer = "";
        MeasuringPoint = 0L;
        NominalPower = 0.0;
        WeightingFactor = 0.0;
        WorkWeeks = 0.0;
        WorkingDays = 0L;
        yearlyconsumption = 0.0;
        shareoftotalconsumption = 0.0;
        co2emissions = 0.0;
        co2shareoftotal = 0.0;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public Double getYearlyconsumption() throws JEVisException {

        if (this.MeasuringPoint != null && !this.MeasuringPoint.equals("") && this.MeasuringPoint != 0L) {

            if (getDs().getAttribute(MeasuringPoint, org.jevis.iso.classes.MeasuringPoint.AttMonitoringID).getSampleCount() > 0) {
                this.yearlyconsumption = DailyOperatingHours * WorkingDays * WorkWeeks * WeightingFactor * NominalPower / 1000;

            } else {
                //implement getting consumption from linked element
                DateTime from = new DateTime(DateTime.now().year().get() - 1, 1, 1, 0, 0);
                DateTime to = new DateTime(DateTime.now().year().get(), 1, 1, 0, 0);
                Long l = MeasuringPoint;
                JsonObject mp = getDs().getObject(l);
                JsonAttribute monitoringAtt = getDs().getAttribute(mp.getId(), org.jevis.iso.classes.MeasuringPoint.AttMonitoringID);
                retrieveYearlyConsumption(monitoringAtt, from, to);

            }
        } else {
            this.yearlyconsumption = DailyOperatingHours * WorkingDays * WorkWeeks * WeightingFactor * NominalPower / 1000;
        }
        return yearlyconsumption;
    }

    private void retrieveYearlyConsumption(JsonAttribute monitoringAtttribute, DateTime from, DateTime to) throws JEVisException {
        if (monitoringAtttribute.getSampleCount() > 0) {
            Long monitoringID = Long.parseLong(getValueString(monitoringAtttribute, ""));
            List<JsonSample> listSamples = getDs().getSamples(monitoringID, "Value", from, to, Long.MAX_VALUE);
            for (JsonSample sample : listSamples) {
                this.yearlyconsumption = Double.parseDouble(sample.getValue());
            }
        }
    }

    public Double getYearlyconsumption(Integer year) throws JEVisException {

        if (this.MeasuringPoint != null && !this.MeasuringPoint.equals("") && this.MeasuringPoint != 0L) {

            if (!(getDs().getAttribute(MeasuringPoint, org.jevis.iso.classes.MeasuringPoint.AttMonitoringID).getSampleCount() > 0)) {
                this.yearlyconsumption = DailyOperatingHours * WorkingDays * WorkWeeks * WeightingFactor * NominalPower / 1000;

            } else {
                //implement getting consumption from linked element
                DateTime from = new DateTime(year, 1, 1, 0, 0);
                DateTime to = new DateTime(year + 1, 1, 1, 0, 0);
                Long l = MeasuringPoint;
                JsonObject mp = getDs().getObject(l);
                JsonAttribute monitoringAtt = getDs().getAttribute(mp.getId(), org.jevis.iso.classes.MeasuringPoint.AttMonitoringID);
                retrieveYearlyConsumption(monitoringAtt, from, to);

            }
        } else {
            this.yearlyconsumption = DailyOperatingHours * WorkingDays * WorkWeeks * WeightingFactor * NominalPower / 1000;
        }
        return yearlyconsumption;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Double getShareoftotalconsumption() {
        return shareoftotalconsumption;
    }

    public void setShareoftotalconsumption(Double shareoftotalconsumption) {
        this.shareoftotalconsumption = shareoftotalconsumption;
    }

    public Double getCo2emissions() {
        return co2emissions;
    }

    public void setCo2emissions(Double co2emissions) {
        this.co2emissions = co2emissions;
    }

    public Double getCo2shareoftotal() {
        return co2shareoftotal;
    }

    public void setCo2shareoftotal(Double co2shareoftotal) {
        this.co2shareoftotal = co2shareoftotal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public Double getDailyOperatingHours() {
        return DailyOperatingHours;
    }

    public void setDailyOperatingHours(Double DailyOperatingHours) {
        this.DailyOperatingHours = DailyOperatingHours;
    }

    public Long getEnergySource() {
        return EnergySource;
    }

    public void setEnergySource(Long EnergySource) {
        this.EnergySource = EnergySource;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String Manufacturer) {
        this.Manufacturer = Manufacturer;
    }

    public Long getMeasuringPoint() {
        return MeasuringPoint;
    }

    public void setMeasuringPoint(Long MeasuringPoint) {
        this.MeasuringPoint = MeasuringPoint;
    }

    public Double getNominalPower() {
        return NominalPower;
    }

    public void setNominalPower(Double NominalPower) {
        this.NominalPower = NominalPower;
    }

    public Double getWeightingFactor() {
        return WeightingFactor;
    }

    public void setWeightingFactor(Double WeightingFactor) {
        this.WeightingFactor = WeightingFactor;
    }

    public Double getWorkWeeks() {
        return WorkWeeks;
    }

    public void setWorkWeeks(Double WorkWeeks) {
        this.WorkWeeks = WorkWeeks;
    }

    public Long getWorkingDays() {
        return WorkingDays;
    }

    public void setWorkingDays(Long WorkingDays) {
        this.WorkingDays = WorkingDays;
    }

    public String getAttMeasuringPoint() {
        return AttMeasuringPoint;
    }

}
