/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class BasicEnPIs {

    List<Double> listTotalEnPIs = new ArrayList<>();
    private Double totalConsumptionJanuary = 0.0;
    private Double totalConsumptionFebruary = 0.0;
    private Double totalConsumptionMarch = 0.0;
    private Double totalConsumptionApril = 0.0;
    private Double totalConsumptionMay = 0.0;
    private Double totalConsumptionJune = 0.0;
    private Double totalConsumptionJuly = 0.0;
    private Double totalConsumptionAugust = 0.0;
    private Double totalConsumptionSeptember = 0.0;
    private Double totalConsumptionOctober = 0.0;
    private Double totalConsumptionNovember = 0.0;
    private Double totalConsumptionDecember = 0.0;
    private Double totalProductionJanuary = 0.0;
    private Double totalProductionFebruary = 0.0;
    private Double totalProductionMarch = 0.0;
    private Double totalProductionApril = 0.0;
    private Double totalProductionMay = 0.0;
    private Double totalProductionJune = 0.0;
    private Double totalProductionJuly = 0.0;
    private Double totalProductionAugust = 0.0;
    private Double totalProductionSeptember = 0.0;
    private Double totalProductionOctober = 0.0;
    private Double totalProductionNovember = 0.0;
    private Double totalProductionDecember = 0.0;
    private Double totalEnPIJanuary = 0.0;
    private Double totalEnPIFebruary = 0.0;
    private Double totalEnPIMarch = 0.0;
    private Double totalEnPIApril = 0.0;
    private Double totalEnPIMay = 0.0;
    private Double totalEnPIJune = 0.0;
    private Double totalEnPIJuly = 0.0;
    private Double totalEnPIAugust = 0.0;
    private Double totalEnPISeptember = 0.0;
    private Double totalEnPIOctober = 0.0;
    private Double totalEnPINovember = 0.0;
    private Double totalEnPIDecember = 0.0;
    private long year = 0L;

    private Double totalConsumption = 0.0;
    private Double totalProduction = 0.0;
    private Double totalEnPIYearRound = 0.0;

    public BasicEnPIs(Long year, List<EnergySource> listSources, List<Produce> listProduction) throws Exception {

        this.year = year;

        for (EnergySource es : listSources) {
            for (EnergyConsumption ec : es.getEnergyconsumptions()) {
                if (year == ec.getYear()) {
                    this.totalConsumptionJanuary += ec.getJanuary();
                    this.totalConsumptionFebruary += ec.getFebruary();
                    this.totalConsumptionMarch += ec.getMarch();
                    this.totalConsumptionApril += ec.getApril();
                    this.totalConsumptionMay += ec.getMay();
                    this.totalConsumptionJune += ec.getJune();
                    this.totalConsumptionJuly += ec.getJuly();
                    this.totalConsumptionAugust += ec.getAugust();
                    this.totalConsumptionSeptember += ec.getSeptember();
                    this.totalConsumptionOctober += ec.getOctober();
                    this.totalConsumptionNovember += ec.getNovember();
                    this.totalConsumptionDecember += ec.getDecember();
                    this.totalConsumption += ec.getSum();
                }
            }
        }

        for (Produce p : listProduction) {
            if (year == p.getYear()) {
                this.totalProductionJanuary += p.getJanuary();
                this.totalProductionFebruary += p.getFebruary();
                this.totalProductionMarch += p.getMarch();
                this.totalProductionApril += p.getApril();
                this.totalProductionMay += p.getMay();
                this.totalProductionJune += p.getJune();
                this.totalProductionJuly += p.getJuly();
                this.totalProductionAugust += p.getAugust();
                this.totalProductionSeptember += p.getSeptember();
                this.totalProductionOctober += p.getOctober();
                this.totalProductionNovember += p.getNovember();
                this.totalProductionDecember += p.getDecember();
                this.totalProduction += p.getSum();
            }
        }

        this.totalEnPIJanuary = divZeroFix(this.totalConsumptionJanuary, this.totalProductionJanuary);
        this.totalEnPIFebruary = divZeroFix(this.totalConsumptionFebruary, this.totalProductionFebruary);
        this.totalEnPIMarch = divZeroFix(this.totalConsumptionMarch, this.totalProductionMarch);
        this.totalEnPIApril = divZeroFix(this.totalConsumptionApril, this.totalProductionApril);
        this.totalEnPIMay = divZeroFix(this.totalConsumptionMay, this.totalProductionMay);
        this.totalEnPIJune = divZeroFix(this.totalConsumptionJune, this.totalProductionJune);
        this.totalEnPIJuly = divZeroFix(this.totalConsumptionJuly, this.totalProductionJuly);
        this.totalEnPIAugust = divZeroFix(this.totalConsumptionAugust, this.totalProductionAugust);
        this.totalEnPISeptember = divZeroFix(this.totalConsumptionSeptember, this.totalProductionSeptember);
        this.totalEnPIOctober = divZeroFix(this.totalConsumptionOctober, this.totalProductionOctober);
        this.totalEnPINovember = divZeroFix(this.totalConsumptionNovember, this.totalProductionNovember);
        this.totalEnPIDecember = divZeroFix(this.totalConsumptionDecember, this.totalProductionDecember);
        this.totalEnPIYearRound = divZeroFix(this.totalConsumption, this.totalProduction);

        listTotalEnPIs = Arrays.asList(this.totalEnPIYearRound, this.totalEnPIJanuary, this.totalEnPIFebruary, this.totalEnPIMarch, this.totalEnPIApril, this.totalEnPIMay, this.totalEnPIJune,
                this.totalEnPIJuly, this.totalEnPIAugust, this.totalEnPISeptember, this.totalEnPIOctober, this.totalEnPINovember, this.totalEnPIDecember);

    }

    private Double divZeroFix(Double dividend, Double divisor) {
        Double d = 0.0;

        d = dividend / divisor;

        if (d.isNaN() || d.isInfinite()) {
            return 0.0;
        } else {
            return d;
        }
    }

    @Override
    public String toString() {
        return "BasicEnPIs[" + "totalEnPIJanuary=" + totalEnPIJanuary + ", totalEnPIFebruary=" + totalEnPIFebruary + ", totalEnPIMarch=" + totalEnPIMarch
                + ", totalEnPIApril=" + totalEnPIApril + ", totalEnPIMay=" + totalEnPIMay + ", totalEnPIJune=" + totalEnPIJune + ", totalEnPIJuly=" + totalEnPIJuly
                + ", totalEnPIAugust=" + totalEnPIAugust + ", totalEnPISeptember=" + totalEnPISeptember + ", totalEnPIOctober=" + totalEnPIOctober + ", totalEnPINovember="
                + totalEnPINovember + ", totalEnPIDecember=" + totalEnPIDecember + ']';
    }

    public Double getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(Double totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public Double getTotalProduction() {
        return totalProduction;
    }

    public void setTotalProduction(Double totalProduction) {
        this.totalProduction = totalProduction;
    }

    public double getTotalConsumptionJanuary() {
        return totalConsumptionJanuary;
    }

    public void setTotalConsumptionJanuary(Double totalConsumptionJanuary) {
        this.totalConsumptionJanuary = totalConsumptionJanuary;
    }

    public Double getTotalEnPIYearRound() {
        return totalEnPIYearRound;
    }

    public void setTotalEnPIYearRound(Double totalEnPIYearRound) {
        this.totalEnPIYearRound = totalEnPIYearRound;
    }

    public Double getTotalConsumptionFebruary() {
        return totalConsumptionFebruary;
    }

    public void setTotalConsumptionFebruary(Double totalConsumptionFebruary) {
        this.totalConsumptionFebruary = totalConsumptionFebruary;
    }

    public Double getTotalConsumptionMarch() {
        return totalConsumptionMarch;
    }

    public void setTotalConsumptionMarch(Double totalConsumptionMarch) {
        this.totalConsumptionMarch = totalConsumptionMarch;
    }

    public Double getTotalConsumptionApril() {
        return totalConsumptionApril;
    }

    public void setTotalConsumptionApril(Double totalConsumptionApril) {
        this.totalConsumptionApril = totalConsumptionApril;
    }

    public long getYear() {
        return year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    public Double getTotalConsumptionMay() {
        return totalConsumptionMay;
    }

    public void setTotalConsumptionMay(Double totalConsumptionMay) {
        this.totalConsumptionMay = totalConsumptionMay;
    }

    public Double getTotalConsumptionJune() {
        return totalConsumptionJune;
    }

    public void setTotalConsumptionJune(Double totalConsumptionJune) {
        this.totalConsumptionJune = totalConsumptionJune;
    }

    public Double getTotalConsumptionJuly() {
        return totalConsumptionJuly;
    }

    public void setTotalConsumptionJuly(Double totalConsumptionJuly) {
        this.totalConsumptionJuly = totalConsumptionJuly;
    }

    public Double getTotalConsumptionAugust() {
        return totalConsumptionAugust;
    }

    public void setTotalConsumptionAugust(Double totalConsumptionAugust) {
        this.totalConsumptionAugust = totalConsumptionAugust;
    }

    public Double getTotalConsumptionSeptember() {
        return totalConsumptionSeptember;
    }

    public void setTotalConsumptionSeptember(Double totalConsumptionSeptember) {
        this.totalConsumptionSeptember = totalConsumptionSeptember;
    }

    public Double getTotalConsumptionOctober() {
        return totalConsumptionOctober;
    }

    public void setTotalConsumptionOctober(Double totalConsumptionOctober) {
        this.totalConsumptionOctober = totalConsumptionOctober;
    }

    public Double getTotalConsumptionNovember() {
        return totalConsumptionNovember;
    }

    public void setTotalConsumptionNovember(Double totalConsumptionNovember) {
        this.totalConsumptionNovember = totalConsumptionNovember;
    }

    public Double getTotalConsumptionDecember() {
        return totalConsumptionDecember;
    }

    public void setTotalConsumptionDecember(Double totalConsumptionDecember) {
        this.totalConsumptionDecember = totalConsumptionDecember;
    }

    public Double getTotalProductionJanuary() {
        return totalProductionJanuary;
    }

    public void setTotalProductionJanuary(Double totalProductionJanuary) {
        this.totalProductionJanuary = totalProductionJanuary;
    }

    public Double getTotalProductionFebruary() {
        return totalProductionFebruary;
    }

    public void setTotalProductionFebruary(Double totalProductionFebruary) {
        this.totalProductionFebruary = totalProductionFebruary;
    }

    public Double getTotalProductionMarch() {
        return totalProductionMarch;
    }

    public void setTotalProductionMarch(Double totalProductionMarch) {
        this.totalProductionMarch = totalProductionMarch;
    }

    public Double getTotalProductionApril() {
        return totalProductionApril;
    }

    public void setTotalProductionApril(Double totalProductionApril) {
        this.totalProductionApril = totalProductionApril;
    }

    public Double getTotalProductionMay() {
        return totalProductionMay;
    }

    public void setTotalProductionMay(Double totalProductionMay) {
        this.totalProductionMay = totalProductionMay;
    }

    public Double getTotalProductionJune() {
        return totalProductionJune;
    }

    public void setTotalProductionJune(Double totalProductionJune) {
        this.totalProductionJune = totalProductionJune;
    }

    public Double getTotalProductionJuly() {
        return totalProductionJuly;
    }

    public void setTotalProductionJuly(Double totalProductionJuly) {
        this.totalProductionJuly = totalProductionJuly;
    }

    public Double getTotalProductionAugust() {
        return totalProductionAugust;
    }

    public void setTotalProductionAugust(Double totalProductionAugust) {
        this.totalProductionAugust = totalProductionAugust;
    }

    public Double getTotalProductionSeptember() {
        return totalProductionSeptember;
    }

    public void setTotalProductionSeptember(Double totalProductionSeptember) {
        this.totalProductionSeptember = totalProductionSeptember;
    }

    public Double getTotalProductionOctober() {
        return totalProductionOctober;
    }

    public void setTotalProductionOctober(Double totalProductionOctober) {
        this.totalProductionOctober = totalProductionOctober;
    }

    public Double getTotalProductionNovember() {
        return totalProductionNovember;
    }

    public void setTotalProductionNovember(Double totalProductionNovember) {
        this.totalProductionNovember = totalProductionNovember;
    }

    public Double getTotalProductionDecember() {
        return totalProductionDecember;
    }

    public void setTotalProductionDecember(Double totalProductionDecember) {
        this.totalProductionDecember = totalProductionDecember;
    }

    public Double getTotalEnPIJanuary() {
        return totalEnPIJanuary;
    }

    public void setTotalEnPIJanuary(Double totalEnPIJanuary) {
        this.totalEnPIJanuary = totalEnPIJanuary;
    }

    public Double getTotalEnPIFebruary() {
        return totalEnPIFebruary;
    }

    public void setTotalEnPIFebruary(Double totalEnPIFebruary) {
        this.totalEnPIFebruary = totalEnPIFebruary;
    }

    public Double getTotalEnPIMarch() {
        return totalEnPIMarch;
    }

    public void setTotalEnPIMarch(Double totalEnPIMarch) {
        this.totalEnPIMarch = totalEnPIMarch;
    }

    public Double getTotalEnPIApril() {
        return totalEnPIApril;
    }

    public void setTotalEnPIApril(Double totalEnPIApril) {
        this.totalEnPIApril = totalEnPIApril;
    }

    public Double getTotalEnPIMay() {
        return totalEnPIMay;
    }

    public void setTotalEnPIMay(Double totalEnPIMay) {
        this.totalEnPIMay = totalEnPIMay;
    }

    public Double getTotalEnPIJune() {
        return totalEnPIJune;
    }

    public void setTotalEnPIJune(Double totalEnPIJune) {
        this.totalEnPIJune = totalEnPIJune;
    }

    public Double getTotalEnPIJuly() {
        return totalEnPIJuly;
    }

    public void setTotalEnPIJuly(Double totalEnPIJuly) {
        this.totalEnPIJuly = totalEnPIJuly;
    }

    public Double getTotalEnPIAugust() {
        return totalEnPIAugust;
    }

    public void setTotalEnPIAugust(Double totalEnPIAugust) {
        this.totalEnPIAugust = totalEnPIAugust;
    }

    public Double getTotalEnPISeptember() {
        return totalEnPISeptember;
    }

    public void setTotalEnPISeptember(Double totalEnPISeptember) {
        this.totalEnPISeptember = totalEnPISeptember;
    }

    public Double getTotalEnPIOctober() {
        return totalEnPIOctober;
    }

    public void setTotalEnPIOctober(Double totalEnPIOctober) {
        this.totalEnPIOctober = totalEnPIOctober;
    }

    public Double getTotalEnPINovember() {
        return totalEnPINovember;
    }

    public void setTotalEnPINovember(Double totalEnPINovember) {
        this.totalEnPINovember = totalEnPINovember;
    }

    public Double getTotalEnPIDecember() {
        return totalEnPIDecember;
    }

    public void setTotalEnPIDecember(Double totalEnPIDecember) {
        this.totalEnPIDecember = totalEnPIDecember;
    }

    public List<Double> getListTotalEnPIs() {
        return listTotalEnPIs;
    }

    public void setListTotalEnPIs(List<Double> listTotalEnPIs) {
        this.listTotalEnPIs = listTotalEnPIs;
    }

}
