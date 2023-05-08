package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.converter.NumberStringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class TableSumPanel extends GridPane {
    ActionData fakeNames = new ActionData();
    JFXTextField f_sumInvestment = new JFXTextField();
    JFXTextField f_sumSavingsYear = new JFXTextField();
    JFXTextField f_sumSavingEnergy = new JFXTextField();
    Label l_sumLabel = new Label(I18n.getInstance().getString("plugin.action.sumtable.total"));
    Label l_sumInvestment = new Label(fakeNames.npv.get().investment.getName());
    Label l_sumSavingsYear = new Label(fakeNames.npv.get().einsparung.getName());
    Label l_sumSavingEnergy = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));
    ObservableList<ActionData> data;
    NumberFormat currencyFormat = NumerFormating.getInstance().getCurrencyFormat();
    NumberStringConverter nsc = new NumberStringConverter() {
        @Override
        public String toString(Number value) {
            return currencyFormat.format(value);
        }
    };
    UnitDoubleConverter unitDoubleConverter = new UnitDoubleConverter();
    private Map<String, JFXTextField> columns = new HashMap<>();
    private ActionPlanData actionPlan;

    public TableSumPanel(ActionPlanData actionPlan, ObservableList<ActionData> data) {
        this.data = data;
        this.actionPlan = actionPlan;
        System.out.println("Create new SumTable for: " + actionPlan.getName());
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(20, 10, 10, 20));

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));


        f_sumInvestment.setAlignment(Pos.CENTER_RIGHT);
        f_sumSavingsYear.setAlignment(Pos.CENTER_RIGHT);
        f_sumSavingEnergy.setAlignment(Pos.CENTER_RIGHT);

        // addRow(0, new Region(), l_sumInvestment, l_sumSavingsYear);
        // addRow(1, l_sumLabel, f_sumInvestment, f_sumSavingsYear);


        actionPlan.getMediumTags().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasReplaced()) {
                    update();
                }
                // updateLayout();
                // updateData();
            }

        });
        data.addListener((ListChangeListener<? super ActionData>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasReplaced()) {
                    try {
                        update();
                        // updateLayout();
                        // updateData();
                    } catch (Exception ex) {
                    }
                }
            }
        });


    }

    private void update() {
        getChildren().clear();
        getColumnConstraints().clear();

        add(new Region(), 0, 0);
        add(l_sumLabel, 0, 1);
        Separator sep2 = new Separator(Orientation.VERTICAL);
        add(sep2, 1, 0, 1, 2);

        double sumInvest = 0;
        double sumEinsparrung = 0;
        for (ActionData actionData : data) {
            sumInvest += actionData.npv.get().getInvestment();
            sumEinsparrung += actionData.npv.get().einsparung.get();

        }

        Label headerInvest = new Label(fakeNames.npv.get().investment.getName());
        Label valueInvest = new Label(nsc.toString(sumInvest));
        add(headerInvest, 2, 0);
        add(valueInvest, 2, 1);
        Separator sep3 = new Separator(Orientation.VERTICAL);
        add(sep3, 3, 0, 1, 2);

        Label headerSaving = new Label(fakeNames.npv.get().investment.getName());
        Label valueSaving = new Label(nsc.toString(sumEinsparrung));
        add(headerSaving, 4, 0);
        add(valueSaving, 4, 1);
        Separator sep4 = new Separator(Orientation.VERTICAL);
        add(sep4, 5, 0, 1, 2);


        int col = 5;
        for (String medium : actionPlan.getMediumTags()) {
            col++;
            //double sum = data.stream().filter(actionData -> actionData.mediaTagsProperty().get().equals(medium)).reduce(0,Double::sum);
            double sum = data.stream().map(actionData -> actionData.enpi.get().diff.get()).reduce(0.0, Double::sum);
            Label header = new Label(medium);
            Label value = new Label();
            value.setPrefWidth(45);
            value.setText(sum + " kWh");
            value.setAlignment(Pos.CENTER);
            add(header, col, 0);
            add(value, col, 1);
            GridPane.setFillWidth(header, false);
            GridPane.setFillWidth(value, false);
            GridPane.setHalignment(header, HPos.CENTER);
            GridPane.setHalignment(value, HPos.CENTER);

            Separator sep = new Separator(Orientation.VERTICAL);
            col++;
            add(sep, col, 0, 1, 2);
            header.widthProperty().addListener((observable, oldValue, newValue) -> System.out.println("W: " + newValue));

        }

        for (String status : actionPlan.getStatustags()) {
            col++;
            int sum = data.stream().filter(actionData -> actionData.statusTags.get().equals(status)).collect(Collectors.toList()).size();
            Label header = new Label(status);
            Label value = new Label(sum + "");
            value.setPrefWidth(45);
            add(header, col, 0);
            add(value, col, 1);
            GridPane.setFillWidth(header, false);
            GridPane.setFillWidth(value, false);
            GridPane.setHalignment(header, HPos.CENTER);
            GridPane.setHalignment(value, HPos.CENTER);

            Separator sep = new Separator(Orientation.VERTICAL);
            col++;
            add(sep, col, 0, 1, 2);
        }


    }

    private void updateLayout() {
        //System.out.println("UpdateLayout");
        columns.clear();
        getChildren().clear();
        getColumnConstraints().clear();

        this.getColumnConstraints().add(0, new ColumnConstraints(100, 150, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(1, new ColumnConstraints(100, 175, 300, Priority.NEVER, HPos.RIGHT, true));

        addRow(0, new Region(), l_sumSavingsYear); //l_sumInvestment
        addRow(1, l_sumLabel, f_sumSavingsYear); //f_sumInvestment
        //updateLayout();


        //this.getColumnConstraints().clear();
        ObservableList<String> mediums = actionPlan.getMediumTags();

        for (String s : mediums) {
            if (!columns.containsKey(s)) {
                Label label = new Label(s);
                JFXTextField field = new JFXTextField();
                field.setAlignment(Pos.CENTER_RIGHT);
                int newColumn = columns.size() + 2;
                columns.put(s, field);
                this.getColumnConstraints().add(newColumn,
                        new ColumnConstraints(100, 100, 300, Priority.NEVER, HPos.RIGHT, true));//160
                add(label, newColumn, 0);
                add(field, newColumn, 1);
            }
        }

        for (String s : actionPlan.getStatustags()) {
            if (!columns.containsKey(s)) {
                Label label = new Label(s);
                JFXTextField field = new JFXTextField();
                field.setAlignment(Pos.CENTER_RIGHT);
                int newColumn = columns.size() + 2;
                columns.put(s, field);
                this.getColumnConstraints().add(newColumn,
                        new ColumnConstraints(100, 100, 300, Priority.NEVER, HPos.RIGHT, true));//170
                add(label, newColumn, 0);
                add(field, newColumn, 1);
            }
        }

    }


    private void updateData() {
        //System.out.println("UpdateSumTable:" + actionPlan + " data:" + data.size());

        double sumInvest = 0;
        double sumEinsparrung = 0;
        for (ActionData actionData : data) {
            sumInvest += actionData.npv.get().getInvestment();
            sumEinsparrung += actionData.npv.get().einsparung.get();

        }
        f_sumInvestment.setText(nsc.toString(sumInvest));
        f_sumSavingsYear.setText(nsc.toString(sumEinsparrung));
        f_sumSavingEnergy.setText("");


        if (actionPlan != null) {
            Map<String, DoubleProperty> mediumSum = new HashMap<>();
            columns.forEach((s, jfxTextField) -> {
                mediumSum.put(s, new SimpleDoubleProperty(0));
            });

            data.forEach(actionData -> {
                //System.out.println("------------");
                //System.out.println("mediumSum: ad: " + actionData + "   isIn: " + mediumSum.containsKey(actionData.mediaTagsProperty().get()));
                if (mediumSum.containsKey(actionData.mediaTagsProperty().get())) {

                    DoubleProperty value = mediumSum.get(actionData.mediaTagsProperty().get());
                    if (!actionData.consumption.get().diffProperty().getValue().isNaN()) {
                        // System.out.println("Add: " + actionData.consumption.get().diffProperty().get());
                        value.setValue(value.get() + actionData.consumption.get().diffProperty().get());
                    }
                    // System.out.println("new sum: " + value.get());


                }
            });
            /*
            columns.forEach((s, jfxTextField) -> {
                jfxTextField.setText(DoubleConverter.getInstance().getDoubleConverter().toString(mediumSum.get(s).get()) + " kWh");
                mediumSum.put(s, new SimpleDoubleProperty(0));
            });

             */


            /* Staus sum */
            Map<String, DoubleProperty> statusMap = new HashMap<>();
            actionPlan.getStatustags().forEach(s -> {
                statusMap.put(s, new SimpleDoubleProperty(0));
            });
            /*
            columns.forEach((s, jfxTextField) -> {

            });

             */

            data.forEach(actionData -> {
                //System.out.println("Entry: " + actionData.statusTagsProperty().get() + "  in: " + statusMap.keySet());
                if (statusMap.containsKey(actionData.statusTagsProperty().get())) {
                    DoubleProperty value = statusMap.get(actionData.statusTagsProperty().get());
                    value.setValue(value.get() + 1);
                }
            });

            /*
            columns.forEach((s, jfxTextField) -> {
                //System.out.println("Reset field: " + s);
                jfxTextField.setText("");
            });
             */

            mediumSum.forEach((s, doubleProperty) -> {
                //System.out.println("Fill GUI: " + s + "  double: " + doubleProperty + " field: " + columns.get(s));
                JFXTextField jfxTextField = columns.get(s);

                Platform.runLater(() -> {
                    jfxTextField.setText(NumerFormating.getInstance().getDoubleConverter().toString(doubleProperty.get()) + " kWh");
                });
            });

            statusMap.forEach((s, doubleProperty) -> {
                //System.out.println("Fill GUIs: " + s + "  double: " + doubleProperty + " field: " + columns.get(s));
                JFXTextField jfxTextField = columns.get(s);
                if (jfxTextField != null) {
                    Platform.runLater(() -> {
                        jfxTextField.setText(NumerFormating.getInstance().getDoubleConverter().toString(doubleProperty.get()));
                    });

                } else {
                    // System.out.println("Missing  textField in sum: " + s);
                }

                //System.out.println("set status: " + s + "=" + doubleProperty.get());
            });


        }


    }

}
