package org.jevis.jeconfig.plugin.action.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.commons.i18n.I18n;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class NPVData {

    @Expose
    @SerializedName("Interest Rate")
    public final SimpleDoubleProperty interestRate = new SimpleDoubleProperty(10);
    @Expose
    @SerializedName("Investment")
    public final SimpleDoubleProperty investment = new SimpleDoubleProperty("Investment"
            , I18n.getInstance().getString("plugin.action.npv.invest"), 10000);
    @Expose
    @SerializedName("Saving")
    public final SimpleDoubleProperty einsparung = new SimpleDoubleProperty("Saving"
            , I18n.getInstance().getString("plugin.action.npv.saving"), 1000);
    @Expose
    @SerializedName("Running Cost")
    public final SimpleDoubleProperty runningCost = new SimpleDoubleProperty(500);
    @Expose
    @SerializedName("Inflation")
    public final SimpleDoubleProperty inflation = new SimpleDoubleProperty(3.1);
    public final ObservableList<NPVYearData> npvYears = FXCollections.observableArrayList();

    @Expose
    @SerializedName("Years")
    public final SimpleIntegerProperty amoutYear = new SimpleIntegerProperty(10);
    @Expose
    @SerializedName("Duration")
    public final SimpleIntegerProperty overXYear = new SimpleIntegerProperty(3);
    @Expose
    @SerializedName("Result")
    public final SimpleDoubleProperty npvResult = new SimpleDoubleProperty(0d);
    @Expose
    @SerializedName("Profitability Index")
    public final SimpleDoubleProperty piResult = new SimpleDoubleProperty(0d);
    @Expose
    @SerializedName("Result over X")
    public final SimpleDoubleProperty npvResultOverX = new SimpleDoubleProperty(0d);
    @Expose
    @SerializedName("Profitability Index over X")
    public final SimpleDoubleProperty piResultOverX = new SimpleDoubleProperty(0d);
    @Expose
    @SerializedName("Sum Investment")
    public final SimpleDoubleProperty sumEinzahlung = new SimpleDoubleProperty(0d);
    @Expose
    @SerializedName("Sum Payout")
    public final SimpleDoubleProperty sumAuszahlung = new SimpleDoubleProperty(0d);
    @Expose
    @SerializedName("Sum Net")
    public final SimpleDoubleProperty sumNetto = new SimpleDoubleProperty(0d);

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private boolean initDone = false;

    public NPVData() {
    }

    public void update() {
        int amountToCreate = amoutYear.get();
        npvYears.clear();
        //System.out.println("Update NPV: " + amountToCreate);
        List<NPVYearData> tmp = new ArrayList<>();
        for (int i = 1; i <= amountToCreate; i++) {
            // System.out.println("Add: " + i);
            tmp.add(new NPVYearData(i, this));
        }
        npvYears.addAll(tmp);

        if (initDone = false) {
            initDone = true;

            amoutYear.addListener((observable, oldValue, newValue) -> {
                //  System.out.println("amoutYear.listener: " + newValue);
                update();
                npvYears.forEach(NPVYearData::updateSums);
            });

            ChangeListener<Number> changeListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    npvYears.forEach(NPVYearData::updateSums);
                    System.out.println("Value changed: " + newValue);
                    //updateResults();
                }
            };

            interestRate.addListener(changeListener);
            investment.addListener(changeListener);
            einsparung.addListener(changeListener);
            runningCost.addListener(changeListener);
            inflation.addListener(changeListener);
        }


    }


    public void updateResults() {
        //System.out.println("######################");
        // System.out.println("Update NPV sums");

        for (NPVYearData npvYearData : npvYears) {
            npvYearData.setInvestment(runningCost.doubleValue());
        }

        double sum = 0;
        double einsparrungSum = 0;
        double sumBetriebskosten = 0;
        double nettoSum = 0;
        double sumOverX = 0;
        double initalInvestment = investment.get();

        sumBetriebskosten += initalInvestment;
        //einzahlung = 0;

        /*
        System.out.println("Einmalig Anschaffung: " + investition.get());
        System.out.println("Einmalig invest:      " + einsparung.get());
        System.out.println("Einmalig runningCost: " + runningCost.get());
        System.out.println("--");

         */

        int year = 0;
        for (NPVYearData npvYearData : npvYears) {
            year++;
            //npvYearData.setDeposit(this.einsparung.get());
            //neu mit teuerung
            npvYearData.setDeposit(round(einsparung.get() * Math.pow((1 + (inflation.get() / 100)), year), 2));

            //neu mit teuerung
            //npvYearData.setInvestment(this.runningCost.get());
            npvYearData.setInvestment(round(runningCost.get() * Math.pow((1 + (inflation.get() / 100)), year), 2));

            npvYearData.netamount.set(npvYearData.getDeposit() - npvYearData.getInvestment());
            npvYearData.setDiscountedCashFlow(round(npvYearData.netamount.get() / Math.pow((1 + (interestRate.get() / 100)), year), 2));


            einsparrungSum += npvYearData.getDeposit();
            sumBetriebskosten += npvYearData.getInvestment();
            nettoSum += npvYearData.getNetamount();

            sum += npvYearData.netamount.get() / Math.pow((1 + (interestRate.get() / 100)), npvYearData.getYear());

            if (year <= overXYear.get()) {
                sumOverX += npvYearData.netamount.get() / Math.pow((1 + (interestRate.get() / 100)), npvYearData.getYear());
            }

        }
/*
        System.out.println("Investtion:    " + (initalInvestment * -1));
        System.out.println("Sum            " + sum);
        System.out.println("sumEinzahlung: " + einsparrungSum);
        System.out.println("sumAuszahlung: " + sumBetriebskosten);
        System.out.println("sumNetto:      " + nettoSum);

        System.out.println("hmmm: " + (1895.3933847042238 / 10000.0));
        System.out.println("hmm2: " + (sum / initalInvestment));

 */

        npvResult.set(round(((initalInvestment * -1) + sum), 2));
        piResult.set((sum / initalInvestment));
        sumEinzahlung.setValue(round(einsparrungSum, 2));
        sumAuszahlung.setValue(round(sumBetriebskosten, 2));
        sumNetto.setValue(round(sumEinzahlung.get() - sumAuszahlung.getValue(), 2));
        //sumNetto.setValue(round(nettoSum, 2));

        npvResultOverX.set(round(((initalInvestment * -1) + sumOverX), 2));
        piResultOverX.set((sumOverX / initalInvestment));

        /*
        System.out.println("new Sum:       " + sumAuszahlung.getValue());
        System.out.println("########################");

         */

    }

    public double getInvestment() {
        return investment.get();
    }

    public SimpleDoubleProperty investmentProperty() {
        return investment;
    }

    private double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }

    public double getSumAuszahlung() {
        return sumAuszahlung.get();
    }

    public SimpleDoubleProperty sumAuszahlungProperty() {
        return sumAuszahlung;
    }

    public double getSumEinzahlung() {
        return sumEinzahlung.get();
    }

    public void setSumEinzahlung(double sumEinzahlung) {
        this.sumEinzahlung.set(sumEinzahlung);
    }

    public SimpleDoubleProperty sumEinzahlungProperty() {
        return sumEinzahlung;
    }
}
