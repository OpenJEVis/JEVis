package org.jevis.jeconfig.plugin.action.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class NPVData {

    private static final Logger logger = LogManager.getLogger(NPVData.class);
    @Expose
    @SerializedName("Interest Rate")
    public final SimpleDoubleProperty interestRate = new SimpleDoubleProperty(10);
    @Expose
    @SerializedName("Investment")
    public final SimpleDoubleProperty investment = new SimpleDoubleProperty("Investment"
            , I18n.getInstance().getString("plugin.action.npv.invest"), 0);
    @Expose
    @SerializedName("Saving")
    public final SimpleDoubleProperty einsparung = new SimpleDoubleProperty("Saving"
            , I18n.getInstance().getString("plugin.action.npv.saving"), 0);
    @Expose
    @SerializedName("Running Cost")
    public final SimpleDoubleProperty runningCost = new SimpleDoubleProperty(0);
    @Expose
    @SerializedName("Inflation")
    public final SimpleDoubleProperty inflation = new SimpleDoubleProperty(3.1);
    public final ObservableList<NPVYearData> npvYears = FXCollections.observableArrayList();

    @Expose
    @SerializedName("Years")
    public final SimpleIntegerProperty amoutYear = new SimpleIntegerProperty(0);
    @Expose
    @SerializedName("Duration")
    public final SimpleIntegerProperty overXYear = new SimpleIntegerProperty(0);
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

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private boolean initDone = false;

    public NPVData() {
    }

    public void update() {
        logger.info("NVP Update");
        int amountToCreate = amoutYear.get();
        npvYears.clear();
        //System.out.println("Update NPV: " + amountToCreate);
        List<NPVYearData> tmp = new ArrayList<>();
        for (int i = 1; i <= amountToCreate; i++) {
            // System.out.println("Add: " + i);
            tmp.add(new NPVYearData(i, this));
        }
        npvYears.addAll(tmp);

        if (!initDone) {
            initDone = true;

            ChangeListener<Number> changeListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    //npvYears.forEach(NPVYearData::updateSums);
                    updateResults();
                }
            };

            interestRate.addListener(changeListener);
            investment.addListener(changeListener);
            einsparung.addListener(changeListener);
            runningCost.addListener(changeListener);
            inflation.addListener(changeListener);

            /*
            npvYears.addListener(new ListChangeListener<NPVYearData>() {
                @Override
                public void onChanged(Change<? extends NPVYearData> c) {
                    npvYears.forEach(NPVYearData::updateSums);
                }
            });

             */
            amoutYear.addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    logger.debug("NPV Data: Amount Year changed: " + newValue);
                    npvYears.clear();
                    List<NPVYearData> tmp = new ArrayList<>();
                    for (int i = 1; i <= newValue.intValue(); i++) {
                        // System.out.println("Add: " + i);
                        tmp.add(new NPVYearData(i, NPVData.this));
                    }
                    npvYears.addAll(tmp);
                    //npvYears.forEach(NPVYearData::updateSums);
                    updateResults();
                }
            });

        }


    }


    public void updateResults() {
        //System.out.println("######################");

        double sum = 0;
        double einsparrungSum = 0;
        double sumBetriebskosten = 0;
        double nettoSum = 0;
        double sumOverX = 0;
        double initalInvestment = investment.get();

        sumBetriebskosten += initalInvestment;


        int year = 0;
        for (NPVYearData npvYearData : npvYears) {
            year++;
            //neu mit teuerung
            npvYearData.setDeposit(round(einsparung.get() * Math.pow((1 + (inflation.get() / 100)), year), 2));

            //neu mit teuerung
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
        npvResult.set(round(((initalInvestment * -1) + sum), 2));
        Double piR = (sum / initalInvestment);
        piResult.set(piR.isNaN() ? 0 : piR.doubleValue());
        Double piRoverX = (sum / initalInvestment);
        piResultOverX.set(piRoverX.isNaN() ? 0 : piRoverX.doubleValue());

        sumEinzahlung.setValue(round(einsparrungSum, 2));
        sumAuszahlung.setValue(round(sumBetriebskosten, 2));
        sumNetto.setValue(round(sumEinzahlung.get() - sumAuszahlung.getValue(), 2));

        npvResultOverX.set(round(((initalInvestment * -1) + sumOverX), 2));


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

    public double getInterestRate() {
        return interestRate.get();
    }

    public SimpleDoubleProperty interestRateProperty() {
        return interestRate;
    }

    public double getEinsparung() {
        return einsparung.get();
    }

    public SimpleDoubleProperty einsparungProperty() {
        return einsparung;
    }

    public double getRunningCost() {
        return runningCost.get();
    }

    public SimpleDoubleProperty runningCostProperty() {
        return runningCost;
    }

    public double getInflation() {
        return inflation.get();
    }

    public SimpleDoubleProperty inflationProperty() {
        return inflation;
    }

    public ObservableList<NPVYearData> getNpvYears() {
        return npvYears;
    }

    public int getAmoutYear() {
        return amoutYear.get();
    }

    public SimpleIntegerProperty amoutYearProperty() {
        return amoutYear;
    }

    public int getOverXYear() {
        return overXYear.get();
    }

    public SimpleIntegerProperty overXYearProperty() {
        return overXYear;
    }

    public double getNpvResult() {
        return npvResult.get();
    }

    public SimpleDoubleProperty npvResultProperty() {
        return npvResult;
    }

    public double getPiResult() {
        return piResult.get();
    }

    public SimpleDoubleProperty piResultProperty() {
        return piResult;
    }

    public double getNpvResultOverX() {
        return npvResultOverX.get();
    }

    public SimpleDoubleProperty npvResultOverXProperty() {
        return npvResultOverX;
    }

    public double getPiResultOverX() {
        return piResultOverX.get();
    }

    public SimpleDoubleProperty piResultOverXProperty() {
        return piResultOverX;
    }

    public double getSumNetto() {
        return sumNetto.get();
    }

    public SimpleDoubleProperty sumNettoProperty() {
        return sumNetto;
    }
}
