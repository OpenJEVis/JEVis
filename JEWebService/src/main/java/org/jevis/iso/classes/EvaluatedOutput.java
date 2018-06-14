/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EvaluatedOutput {
    long ID;
    String EnergySupplier;
    long year;
    double January;
    double February;
    double March;
    double April;
    double May;
    double June;
    double July;
    double August;
    double September;
    double October;
    double November;
    double December;
    double sum;
    String type;
    private String name;

    public EvaluatedOutput(SQLDataSource ds, JsonObject input) throws Exception {
        final String AttEnergySupplier = "Energy Supplier";
        final String AttYear = "Year";
        final String AttJanuary = "01 January";
        final String AttFebruary = "02 February";
        final String AttMarch = "03 March";
        final String AttApril = "04 April";
        final String AttMay = "05 May";
        final String AttJune = "06 June";
        final String AttJuly = "07 July";
        final String AttAugust = "08 August";
        final String AttSeptember = "09 September";
        final String AttOctober = "10 October";
        final String AttNovember = "11 November";
        final String AttDecember = "12 December";
        ID = 0L;
        name = "";
        EnergySupplier = "";
        year = 0L;
        January = 0.0;
        February = 0.0;
        March = 0.0;
        April = 0.0;
        May = 0.0;
        June = 0.0;
        July = 0.0;
        August = 0.0;
        September = 0.0;
        October = 0.0;
        November = 0.0;
        December = 0.0;
        sum = 0.0;
        type = "";
        this.ID = input.getId();
        this.name = input.getName();

        List<JsonAttribute> listAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listAttributes) {

            String name = att.getType();

            switch (name) {
                case AttEnergySupplier:
                    this.setEnergySupplier(getValueString(att, ""));
                    break;
                case AttYear:
                    if (getValueString(att, "") != "") {
                        this.setYear(Long.parseLong(getValueString(att, "")));
                    } else this.setYear(DateTime.now().getYear());
                    break;
                case AttJanuary:
                    if (getValueString(att, "") != "") {
                        this.setJanuary(Double.parseDouble(getValueString(att, "")));
                    } else this.setJanuary(0.0);
                    break;
                case AttFebruary:
                    if (getValueString(att, "") != "") {
                        this.setFebruary(Double.parseDouble(getValueString(att, "")));
                    } else this.setFebruary(0.0);
                    break;
                case AttMarch:
                    if (getValueString(att, "") != "") {
                        this.setMarch(Double.parseDouble(getValueString(att, "")));
                    } else this.setMarch(0.0);
                    break;
                case AttApril:
                    if (getValueString(att, "") != "") {
                        this.setApril(Double.parseDouble(getValueString(att, "")));
                    } else this.setApril(0.0);
                    break;
                case AttMay:
                    if (getValueString(att, "") != "") {
                        this.setMay(Double.parseDouble(getValueString(att, "")));
                    } else this.setMay(0.0);
                    break;
                case AttJune:
                    if (getValueString(att, "") != "") {
                        this.setJune(Double.parseDouble(getValueString(att, "")));
                    } else this.setJune(0.0);
                    break;
                case AttJuly:
                    if (getValueString(att, "") != "") {
                        this.setJuly(Double.parseDouble(getValueString(att, "")));
                    } else this.setJuly(0.0);
                    break;
                case AttAugust:
                    if (getValueString(att, "") != "") {
                        this.setAugust(Double.parseDouble(getValueString(att, "")));
                    } else this.setAugust(0.0);
                    break;
                case AttSeptember:
                    if (getValueString(att, "") != "") {
                        this.setSeptember(Double.parseDouble(getValueString(att, "")));
                    } else this.setSeptember(0.0);
                    break;
                case AttOctober:
                    if (getValueString(att, "") != "") {
                        this.setOctober(Double.parseDouble(getValueString(att, "")));
                    } else this.setOctober(0.0);
                    break;
                case AttNovember:
                    if (getValueString(att, "") != "") {
                        this.setNovember(Double.parseDouble(getValueString(att, "")));
                    } else this.setNovember(0.0);
                    break;
                case AttDecember:
                    if (getValueString(att, "") != "") {
                        this.setDecember(Double.parseDouble(getValueString(att, "")));
                    } else this.setDecember(0.0);
                    break;
                default:
                    break;
            }
        }

        this.buildSum();

    }

    public EvaluatedOutput() {
        final String AttEnergySupplier = "Energy Supplier";
        final String AttYear = "Year";
        final String AttJanuary = "01 January";
        final String AttFebruary = "02 February";
        final String AttMarch = "03 March";
        final String AttApril = "04 April";
        final String AttMay = "05 May";
        final String AttJune = "06 June";
        final String AttJuly = "07 July";
        final String AttAugust = "08 August";
        final String AttSeptember = "09 September";
        final String AttOctober = "10 October";
        final String AttNovember = "11 November";
        final String AttDecember = "12 December";
        ID = 0L;
        name = "";
        EnergySupplier = "";
        year = 0L;
        January = 0.0;
        February = 0.0;
        March = 0.0;
        April = 0.0;
        May = 0.0;
        June = 0.0;
        July = 0.0;
        August = 0.0;
        September = 0.0;
        October = 0.0;
        November = 0.0;
        December = 0.0;
        sum = 0.0;
        type = "";
    }

    public List<Double> getList() {
        List<Double> l = new ArrayList<>();
        l.addAll(Arrays.asList(this.January, this.February, this.March, this.April, this.May, this.June, this.July, this.August, this.September, this.October, this.November, this.December));

        return l;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getname() {
        return name;
    }

    @Override
    public String toString() {
        return "EnergyConsumption{" + "ID=" + ID + ", EnergySupplier=" + EnergySupplier + ", Year=" + year + ", January=" + January + ", February=" + February + ", March=" + March + ", April=" + April + ", May=" + May + ", June=" + June + ", July=" + July + ", August=" + August + ", September=" + September + ", October=" + October + ", November=" + November + ", December=" + December + '}';
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getEnergySupplier() {
        return EnergySupplier;
    }

    public void setEnergySupplier(String EnergySupplier) {
        this.EnergySupplier = EnergySupplier;
    }

    public long getYear() {
        return year;
    }

    public void setYear(long Year) {
        this.year = Year;
    }

    public double getJanuary() {
        return January;
    }

    public void setJanuary(double January) {
        this.January = January;
    }

    public double getFebruary() {
        return February;
    }

    public void setFebruary(double February) {
        this.February = February;
    }

    public double getMarch() {
        return March;
    }

    public void setMarch(double March) {
        this.March = March;
    }

    public double getApril() {
        return April;
    }

    public void setApril(double April) {
        this.April = April;
    }

    public double getMay() {
        return May;
    }

    public void setMay(double May) {
        this.May = May;
    }

    public double getJune() {
        return June;
    }

    public void setJune(double June) {
        this.June = June;
    }

    public double getJuly() {
        return July;
    }

    public void setJuly(double July) {
        this.July = July;
    }

    public double getAugust() {
        return August;
    }

    public void setAugust(double August) {
        this.August = August;
    }

    public double getSeptember() {
        return September;
    }

    public void setSeptember(double September) {
        this.September = September;
    }

    public double getOctober() {
        return October;
    }

    public void setOctober(double October) {
        this.October = October;
    }

    public double getNovember() {
        return November;
    }

    public void setNovember(double November) {
        this.November = November;
    }

    public double getDecember() {
        return December;
    }

    public void setDecember(double December) {
        this.December = December;
    }

    public double getSum() {
        return this.sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public void buildSum() {
        this.sum = this.January + this.February + this.March + this.April + this.May + this.June + this.July + this.August + this.September + this.October + this.November + this.December;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
