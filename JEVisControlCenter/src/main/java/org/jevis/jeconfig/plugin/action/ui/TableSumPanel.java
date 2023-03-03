package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.converter.NumberStringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;


public class TableSumPanel extends GridPane {

    JFXTextField f_sumInvestment = new JFXTextField();
    JFXTextField f_sumSavingsYear = new JFXTextField();
    JFXTextField f_sumSavingEnergy = new JFXTextField();

    ObservableList<ActionData> data;
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    NumberStringConverter nsc = new NumberStringConverter() {
        @Override
        public String toString(Number value) {
            return currencyFormat.format(value);
        }
    };

    public TableSumPanel(ObservableList<ActionData> data) {
        this.data = data;
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(20, 10, 10, 20));

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));

        ActionData fakeNames = new ActionData();

        Label l_sumLabel = new Label(I18n.getInstance().getString("plugin.action.sumtable.total"));
        Label l_sumInvestment = new Label(fakeNames.npv.get().investment.getName());
        Label l_sumSavingsYear = new Label(fakeNames.npv.get().einsparung.getName());
        Label l_sumSavingEnergy = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));


        f_sumInvestment.setAlignment(Pos.CENTER_RIGHT);
        f_sumSavingsYear.setAlignment(Pos.CENTER_RIGHT);
        f_sumSavingEnergy.setAlignment(Pos.CENTER_RIGHT);

        addRow(0, new Region(), l_sumInvestment, l_sumSavingsYear, l_sumSavingEnergy);
        addRow(1, l_sumLabel, f_sumInvestment, f_sumSavingsYear, f_sumSavingEnergy);


        this.getColumnConstraints().add(0, new ColumnConstraints(50, 60, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(1, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(2, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(3, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(4, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));

        data.addListener((ListChangeListener<? super ActionData>) c -> {
            while (c.next()) {
                updateData();
            }
        });

    }

    private void updateData() {
        double sumInvest = 0;
        double sumEinsparrung = 0;
        for (ActionData actionData : data) {
            sumInvest += actionData.npv.get().getInvestment();
            sumEinsparrung += actionData.npv.get().einsparung.get();

        }

        f_sumInvestment.setText(nsc.toString(sumInvest));
        f_sumSavingsYear.setText(nsc.toString(sumEinsparrung));
        f_sumSavingEnergy.setText("");

    }

}
