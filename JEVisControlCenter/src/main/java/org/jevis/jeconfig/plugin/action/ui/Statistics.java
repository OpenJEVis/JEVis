package org.jevis.jeconfig.plugin.action.ui;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.Medium;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Statistics {
    private static final Logger logger = LogManager.getLogger(Statistics.class);
    private final DoubleProperty sumSinceImplementation = new SimpleDoubleProperty(0.0);
    private final DoubleProperty sumConsumptionSinceImplementation = new SimpleDoubleProperty(0.0);
    private final StringProperty textSumSinceImplementation = new SimpleStringProperty();
    private final StringProperty textSumSinceImplementationGross = new SimpleStringProperty();
    private final StringProperty textSumConsumptionSinceImplementation = new SimpleStringProperty();
    private final ActionPlanData actionPlan;
    private final SimpleObjectProperty<DateFilter> dateFilter;
    private final DoubleProperty sumInvestProperty = new SimpleDoubleProperty(0.0);
    private final StringProperty sumInvestStrProperty = new SimpleStringProperty();
    private final DoubleProperty sumSavingsProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty sumSavingsGross = new SimpleDoubleProperty(0.0);
    private final StringProperty sumSavingsStrProperty = new SimpleStringProperty();
    private final StringProperty sumSavingsCombinedProperty = new SimpleStringProperty();
    private final DoubleProperty sumNPVResultProperty = new SimpleDoubleProperty(0.0);
    private final StringProperty sumNPVResultStrProperty = new SimpleStringProperty();
    private final StringProperty sumCO2 = new SimpleStringProperty();
    private final ObservableMap<String, StringProperty> sumNPVResultPerMediumStrList = FXCollections.observableHashMap();
    private final StringProperty sumStrProperty = new SimpleStringProperty();
    private final StringProperty sumSavingsByMedium = new SimpleStringProperty();
    private final StringProperty statusStrProperty = new SimpleStringProperty("\n\n\n\n");
    private final ObservableMap<String, StringProperty> statusMap = FXCollections.observableHashMap();
    private ObservableList<ActionData> data;

    public Statistics(ActionPlanData actionPlan, SimpleObjectProperty<DateFilter> dateFilter) {
        this.actionPlan = actionPlan;
        this.dateFilter = dateFilter;
        dateFilter.addListener((observable, oldValue, newValue) -> {
            update();
        });

        actionPlan.getMediumTags().forEach(s -> {
            sumNPVResultPerMediumStrList.put(s, new SimpleStringProperty());
        });

    }

    public String getTextSumConsumptionSinceImplementation() {
        return textSumConsumptionSinceImplementation.get();
    }

    public StringProperty textSumConsumptionSinceImplementationProperty() {
        return textSumConsumptionSinceImplementation;
    }

    public void setData(ObservableList<ActionData> data) {
        this.data = data;

        data.addListener((ListChangeListener<? super ActionData>) observable -> {
            while (observable.next()) {
            }
            update();

        });
    }

    public void update() {
        logger.debug("Update Statistics for plan: {}", actionPlan.getName());
        try {
            updateSumSinceImplementation();
            updateSumConsumptionSinceImplementation();
            updateSums();
        } catch (Exception ex) {
            logger.error("Error while update Statistics", ex);
        }
    }

    private void updateSums() {
        double sumInvest = 0;
        double sumSavings = 0.0;
        double sumImprovement = 0.0;
        double sumSavingsGrossD = 0.0;
        Map<String, Double> nvpresultMap = new HashMap<>();
        Double zero = new Double(0.0);
        actionPlan.getMediumTags().forEach(s -> {
            nvpresultMap.put(s, zero);
        });

        for (ActionData o : data) {
            try {
                sumInvest += o.npv.get().getInvestment();
                sumSavings += o.npv.get().einsparung.get();
                sumImprovement += o.getConsumption().diff.get();
                if (o.npv.get().einsparung.get() > 0) sumSavingsGrossD += o.npv.get().einsparung.get();


                if (!o.mediaTags.get().isEmpty() && !nvpresultMap.isEmpty()) {
                    try {
                        double sumMedium = nvpresultMap.get(o.mediaTags.get()) + o.getConsumption().diff.get();
                        nvpresultMap.put(o.mediaTags.get(), sumMedium);
                    } catch (Exception ex) {
                        logger.error("Error in Sum by Medium: medium: {} , value: {}", o.mediaTags, o.getConsumption().diff);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        sumInvestProperty.set(sumInvest);
        sumInvestStrProperty.set(NumerFormating.getInstance().getCurrencyFormat().format(sumInvest));
        sumSavingsProperty.set(sumSavings);
        sumSavingsStrProperty.set(NumerFormating.getInstance().getCurrencyFormat().format(sumSavings));
        sumSavingsGross.set(sumSavingsGrossD);
        sumNPVResultProperty.set(sumImprovement);
        sumNPVResultStrProperty.set(NumerFormating.getInstance().getDoubleFormate().format(sumImprovement) + " kWh");


        sumNPVResultPerMediumStrList.forEach((s, stringProperty) -> {
            stringProperty.setValue("");
        });


        String mediumStrg = "";
        if (!nvpresultMap.isEmpty()) {
            int maxLength = nvpresultMap.keySet().stream().max(Comparator.comparingInt(String::length)).get().length();
            int maxValueLength = String.format("%s kWh", nvpresultMap.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).get()).length();

            for (Map.Entry<String, Double> entry : nvpresultMap.entrySet()) {
                String s = entry.getKey();
                try {
                    Double doubleValue = entry.getValue();
                    String name = String.format("%-" + (maxLength + 1) + "s", s + ":");
                    String valueStrg = String.format("%s kWh", NumerFormating.getInstance().getDoubleFormate().format(doubleValue));
                    String full = String.format("%s %" + maxValueLength + "s", name, valueStrg);
                    if (doubleValue.equals(zero)) {
                        sumNPVResultPerMediumStrList.get(s).setValue("");
                    } else {
                        sumNPVResultPerMediumStrList.get(s).setValue(full);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }


        sumSavingsByMedium.set(mediumStrg);

        String statusStr = "";

        statusMap.forEach((s, stringProperty) -> {
            stringProperty.setValue(s + ": " + 0);
        });
        for (String s : actionPlan.getStatustags()) {
            int amount = data.stream().filter(actionData -> actionData.statusTags.get().equals(s)).collect(Collectors.toList()).size();
            statusStr += s + ": " + amount + "\n";
            getStatusAmount(s).setValue(s + ": " + amount);
        }
        statusStrProperty.set(statusStr);

    }

    public StringProperty getMediumSum(String medium) {
        if (!sumNPVResultPerMediumStrList.containsKey(medium)) {
            sumNPVResultPerMediumStrList.put(medium, new SimpleStringProperty(String.format("%s %s\n", medium, NumerFormating.getInstance().getDoubleFormate().format(0.0) + " kWh")));
        }
        return sumNPVResultPerMediumStrList.get(medium);
    }

    public StringProperty getStatusAmount(String status) {
        if (!statusMap.containsKey(status)) {
            statusMap.put(status, new SimpleStringProperty(status + ": 0"));
        }
        return statusMap.get(status);
    }

    public Map<String, StringProperty> getNVPResultForMedium() {
        return sumNPVResultPerMediumStrList;
    }

    public double getSumConsumptionSinceImplementation() {
        return sumConsumptionSinceImplementation.get();
    }

    public DoubleProperty sumConsumptionSinceImplementationProperty() {
        return sumConsumptionSinceImplementation;
    }

    private void updateSumSinceImplementation() {
        logger.debug("------------------------\nCalculate € Sum"); // actionData.npv.get().einsparung.get()
        DoubleProperty sum = new SimpleDoubleProperty(0);
        data.forEach(actionData -> {
            if (actionData.doneDate.get() != null && actionData.doneDate.get().isAfter(dateFilter.get().getFromDate())) {
                int daysRunning = Days.daysBetween(actionData.doneDate.get().withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays();
                sum.set(sum.get() + ((daysRunning) * (actionData.npv.get().einsparung.get() / 365)));

                logger.debug("Action Nr: " + actionData.nr.get() + " DoneDate: " + actionData.doneDate.get() + " Until: " + DateTime.now() + " Days: " + daysRunning + " Value: " + actionData.consumption.get().diff.get());
                logger.debug("Sum: " + ((daysRunning) * (actionData.npv.get().einsparung.get() / 365)) + "= " + daysRunning + "*(" + actionData.npv.get().einsparung.get() + "/365)");

            }
        });
        logger.debug("Total Sum: " + sum.get());
        sumConsumptionSinceImplementation.setValue(sum.get());
        textSumConsumptionSinceImplementation.set(I18n.getInstance().getString("plugin.action.statistics.saveSinceConsumptionImp")
                + ":\t" + NumerFormating.getInstance().getCurrencyFormat().format(sum.get()));
    }

    private void updateSumConsumptionSinceImplementation() {
        logger.debug("------------------------\nCalculate kwh Sum");
        DoubleProperty sumNet = new SimpleDoubleProperty(0);
        DoubleProperty sumGross = new SimpleDoubleProperty(0);
        DoubleProperty sumCO2Net = new SimpleDoubleProperty(0);
        DoubleProperty sumCO2Gross = new SimpleDoubleProperty(0);
        data.forEach(actionData -> {
            if (actionData.doneDate.get() != null && actionData.doneDate.get().isAfter(dateFilter.get().getFromDate())) {
                int daysRunning = Days.daysBetween(actionData.doneDate.get().withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays();
                double co2Value = 0;

                ObservableList<Medium> d1 = actionPlan.getMedium();
                String d2 = actionData.mediaTags.get();

                Optional<Medium> medium = actionPlan.getMedium().stream().filter(m -> m.getId().equals(actionData.mediaTags.get())).findFirst();
                if (medium.isPresent()) {
                    co2Value = medium.get().getCo2();
                }


                double net = ((daysRunning) * (actionData.consumption.get().diff.get() / 365));
                sumNet.set(sumNet.get() + net);
                sumCO2Net.set(sumCO2Net.get() + (co2Value * (net / 1000))); //kWh->mWh


                if (actionData.consumption.get().diff.get() > 0) {
                    double gross = ((daysRunning) * (actionData.consumption.get().diff.get() / 365));
                    sumGross.set(sumGross.get() + gross);
                    sumCO2Gross.set(sumCO2Gross.get() + (co2Value * (gross / 1000)));//kWh->mWh
                }


                logger.debug("Action Nr: " + actionData.nr.get() + " DoneDate: " + actionData.doneDate.get() + " Until: " + DateTime.now() + " Days: " + daysRunning + " Value: " + actionData.consumption.get().diff.get());
                logger.debug("Sum: " + ((daysRunning) * (actionData.consumption.get().diff.get() / 365)) + "= " + daysRunning + "*(" + actionData.consumption.get().diff.get() + "/365)");

            }
        });


        logger.debug("Total Sum: " + sumNet.get());
        sumSinceImplementation.setValue(sumNet.get());
        textSumSinceImplementation.set(I18n.getInstance().getString("plugin.action.statistics.saveSinceImp")
                + ":\t" + NumerFormating.getInstance().getDoubleFormate().format(sumNet.get()) + " kWh");
        textSumSinceImplementationGross.set(I18n.getInstance().getString("plugin.action.statistics.saveGrossSinceImp")
                + ":\t" + NumerFormating.getInstance().getDoubleFormate().format(sumGross.get()) + " kWh");
        sumCO2.set(I18n.getInstance().getString("plugin.action.statistics.saveCO2Net")
                + ":\t" + NumerFormating.getInstance().getDoubleFormate().format(sumCO2Net.get()) + " Tonnen");


    }

    public StringProperty getSumCO2Net() {
        return sumCO2;
    }

    public double getSumSavingsProperty() {
        return sumSavingsProperty.get();
    }

    public String getTextSumSinceImplementationGross() {
        return textSumSinceImplementationGross.get();
    }

    public StringProperty textSumSinceImplementationGrossProperty() {
        return textSumSinceImplementationGross;
    }

    public double getSumSavingsGross() {
        return sumSavingsGross.get();
    }

    public DoubleProperty sumSavingsGrossProperty() {
        return sumSavingsGross;
    }

    public DoubleProperty sumSavingsPropertyProperty() {
        return sumSavingsProperty;
    }

    public String getSumSavingsStrProperty() {
        return sumSavingsStrProperty.get();
    }

    public StringProperty sumSavingsStrPropertyProperty() {
        return sumSavingsStrProperty;
    }

    public double getSumInvestProperty() {
        return sumInvestProperty.get();
    }

    public String getSumSavingsByMedium() {
        return sumSavingsByMedium.get();
    }

    public StringProperty sumSavingsByMediumProperty() {
        return sumSavingsByMedium;
    }

    public DoubleProperty sumInvestPropertyProperty() {
        return sumInvestProperty;
    }

    public String getSumInvestStrProperty() {
        return sumInvestStrProperty.get();
    }

    public StringProperty sumInvestStrPropertyProperty() {
        return sumInvestStrProperty;
    }

    public String getTextSumSinceImplementation() {
        return textSumSinceImplementation.get();
    }

    public String getStatusStrProperty() {
        return statusStrProperty.get();
    }

    public StringProperty statusStrPropertyProperty() {
        return statusStrProperty;
    }

    public StringProperty textSumSinceImplementationProperty() {
        return textSumSinceImplementation;
    }

    public String getSumSavingsCombinedProperty() {
        return sumSavingsCombinedProperty.get();
    }

    public StringProperty sumSavingsCombinedPropertyProperty() {
        return sumSavingsCombinedProperty;
    }

    public double getSumSinceImplementation() {
        return sumSinceImplementation.get();
    }

    public DoubleProperty sumSinceImplementationProperty() {
        return sumSinceImplementation;
    }

    public double getSumNPVResultProperty() {
        return sumNPVResultProperty.get();
    }

    public DoubleProperty sumNPVResultPropertyProperty() {
        return sumNPVResultProperty;
    }

    public String getSumNPVResultStrProperty() {
        return sumNPVResultStrProperty.get();
    }

    public StringProperty sumNPVResultStrPropertyProperty() {
        return sumNPVResultStrProperty;
    }
}
