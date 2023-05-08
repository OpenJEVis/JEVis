package org.jevis.jeconfig.plugin.action.data;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class NPVYearData {

    public final SimpleIntegerProperty year = new SimpleIntegerProperty(0);
    /**
     * Positiver betrag
     **/
    public final SimpleDoubleProperty deposit = new SimpleDoubleProperty(1000.00d);

    public final SimpleDoubleProperty investment = new SimpleDoubleProperty(0.00d);
    public final SimpleDoubleProperty netamount = new SimpleDoubleProperty(0d);
    public final SimpleDoubleProperty discountedCashFlow = new SimpleDoubleProperty(0d);

    private NPVData npvData;


    public NPVYearData() {
        super();
        addListeners();
        updateSums();
    }

    public NPVYearData(int year, NPVData npvData) {
        super();
        this.year.set(year);
        this.npvData = npvData;
        // addListeners();
        // updateSums();
    }

    private void addListeners() {
        System.out.println("Add NPVYearData. add Listeners");
        ChangeListener<Number> listener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //  updateSums();
            }
        };
        //deposit.addListener(listener);
        //investment.addListener(listener);
    }

    public void setNpvData(NPVData npvData) {
        this.npvData = npvData;
    }

    public void updateSums() {
        try {
            //System.out.println("---- NPVYearData Update sums");
            netamount.setValue(round(deposit.get() - investment.getValue(), 2));
            discountedCashFlow.setValue(round(deposit.get() - investment.getValue(), 2));
            discountedCashFlow.setValue(round(netamount.get() / Math.pow((1 + (npvData.interestRate.get() / 100)), getYear()), 2));
            npvData.updateResults();

        } catch (Exception ex) {

        }
    }

    private double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }


    public int getYear() {
        return year.get();
    }

    public void setYear(int year) {
        this.year.set(year);
    }

    public SimpleIntegerProperty yearProperty() {
        return year;
    }

    public double getDeposit() {
        return deposit.get();
    }

    public void setDeposit(double deposit) {
        this.deposit.set(deposit);
    }

    public SimpleDoubleProperty depositProperty() {
        return deposit;
    }

    public double getInvestment() {
        return investment.get();
    }

    public void setInvestment(double investment) {
        this.investment.set(investment);
    }

    public SimpleDoubleProperty investmentProperty() {
        return investment;
    }

    public double getNetamount() {
        return netamount.get();
    }

    public void setNetamount(double netamount) {
        this.netamount.set(netamount);
    }

    public SimpleDoubleProperty netamountProperty() {
        return netamount;
    }

    public double getDiscountedCashFlow() {
        return discountedCashFlow.get();
    }

    public void setDiscountedCashFlow(double discountedCashFlow) {
        this.discountedCashFlow.set(discountedCashFlow);
    }

    public SimpleDoubleProperty discountedCashFlowProperty() {
        return discountedCashFlow;
    }
}
