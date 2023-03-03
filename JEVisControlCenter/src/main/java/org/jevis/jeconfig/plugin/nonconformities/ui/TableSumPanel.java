package org.jevis.jeconfig.plugin.nonconformities.ui;

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
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;


public class TableSumPanel extends GridPane {

    JFXTextField f_SumTotal = new JFXTextField();
    JFXTextField f_SumNotCompleted = new JFXTextField();
    JFXTextField f_SumCompleted = new JFXTextField();

    ObservableList<NonconformityData> data;
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    NumberStringConverter nsc = new NumberStringConverter() {
        @Override
        public String toString(Number value) {
            return currencyFormat.format(value);
        }
    };

    public TableSumPanel(ObservableList<NonconformityData> data) {
        this.data = data;
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(20, 10, 10, 20));

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));

        ActionData fakeNames = new ActionData();

        Label l_sumLabel = new Label(I18n.getInstance().getString("plugin.action.sumtable.total"));
        Label l_sumTotal = new Label(I18n.getInstance().getString("plugin.nonconformities.sum.total"));
        Label l_sumNotCompleted = new Label(I18n.getInstance().getString("plugin.nonconformities.sum.notcompletd"));
        Label l_sumCompleted = new Label(I18n.getInstance().getString("plugin.nonconformities.sum.compled"));


        f_SumTotal.setAlignment(Pos.CENTER_RIGHT);
        f_SumNotCompleted.setAlignment(Pos.CENTER_RIGHT);
        f_SumCompleted.setAlignment(Pos.CENTER_RIGHT);

        addRow(0, new Region(), l_sumTotal, l_sumNotCompleted, l_sumCompleted);
        addRow(1, l_sumLabel, f_SumTotal, f_SumNotCompleted, f_SumCompleted);


        this.getColumnConstraints().add(0, new ColumnConstraints(50, 60, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(1, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(2, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(3, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));
        this.getColumnConstraints().add(4, new ColumnConstraints(100, 170, 300, Priority.NEVER, HPos.RIGHT, true));

        data.addListener((ListChangeListener<? super NonconformityData>) c -> {
            while (c.next()) {
                updateData();
            }
        });

    }

    private void updateData() {
        f_SumTotal.setText(String.valueOf(data.stream().count()));
        f_SumNotCompleted.setText(String.valueOf(data.stream().filter(actionData -> actionData.getDoneDate()== null).count()));
        f_SumCompleted.setText(String.valueOf(data.stream().filter(data1 -> data1.getDoneDate()!= null).count()));
    }

}
