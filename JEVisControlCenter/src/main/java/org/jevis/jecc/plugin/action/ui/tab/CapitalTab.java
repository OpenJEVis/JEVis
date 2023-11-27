package org.jevis.jecc.plugin.action.ui.tab;


import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.action.data.ActionData;
import org.jevis.jecc.plugin.action.ui.NPVTableView;
import org.jevis.jecc.plugin.action.ui.NumerFormating;

import java.text.DecimalFormat;

public class CapitalTab extends Tab {
    private static final Logger logger = LogManager.getLogger(CapitalTab.class);
    Label l_investition = new Label("Investition");
    TextField f_investition = new TextField();
    Label l_investitionUnit = new Label("€");
    Label l_einsparrung = new Label(I18n.getInstance().getString("actionform.action.nvp.annualSavings"));//"Jährliche Einsparung");
    TextField f_einsparrung = new TextField();
    Label l_einsparrungUnit = new Label("€");
    Label l_zinssatz = new Label(I18n.getInstance().getString("actionform.action.nvp.interestRate"));//"Zinssatz");
    TextField f_zinssatz = new TextField("");
    Label l_proZent = new Label("%");
    Label l_years = new Label("Jahr(e)");
    Label l_kapitalwert = new Label(I18n.getInstance().getString("actionform.action.nvp"));//"Kapitalwert");
    TextField f_kapitalwert = new TextField("");
    Label l_kapitalwertrate = new Label(I18n.getInstance().getString("actionform.action.nvp.rate"));//"Kapitalwertrate");
    TextField f_kapitalwertrate = new TextField("");
    Label l_period = new Label(I18n.getInstance().getString("actionform.action.nvp.label.term"));//"Laufzeit");
    Label l_periodOverX = new Label(I18n.getInstance().getString("actionform.action.nvp.label.termOverX"));//"Laufzeit über X");
    Label l_overRuntime = new Label(I18n.getInstance().getString("actionform.action.nvp.label.overRuntime"));//"Amortisation über die Laufzeit");
    Label l_over = new Label(I18n.getInstance().getString("actionform.action.nvp.label.amortization"));//"Amortisation über");
    Label l_infation = new Label(I18n.getInstance().getString("actionform.action.nvp.annualIncrease"));
    Label l_infationUnit = new Label("%");
    TextField f_infation = new TextField();
    Label l_yearCost = new Label(I18n.getInstance().getString("actionform.action.nvp.annualCost"));
    Label l_yearCostUnit = new Label("€");
    TextField f_runningCost = new TextField();
    TextField f_kapitalwertOverX = new TextField();
    TextField f_kapitalrateOverX = new TextField();

    Label l_yearsOverX = new Label("Jahr(e)");

    public CapitalTab(ActionData data) {
        super(I18n.getInstance().getString("actionform.editor.tab.capital"));
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25, 12, 12, 12));
        gridPane.setVgap(10);
        gridPane.setHgap(15);

        f_zinssatz.setPrefWidth(50);
        NPVTableView npvTableView = new NPVTableView(data.npv.get().npvYears);

        Label l_einzahlungGesamt = new Label("Einsparung");
        l_einzahlungGesamt.setAlignment(Pos.CENTER);
        TextField f_einzahlungGesamt = new TextField();


        Label l_auszahlungGesamt = new Label("Investition");
        l_auszahlungGesamt.setAlignment(Pos.CENTER);
        TextField f_auszahlungGesamt = new TextField();
        Label l_nettoGesamt = new Label("Netto");
        l_nettoGesamt.setAlignment(Pos.CENTER);
        TextField f_nettoGesamt = new TextField();
        Label l_gesamt = new Label("Gesamt");
        logger.debug("-------------");
        logger.debug("data.npv.get().overXYear: " + data.npv.get().amoutYear.get());

        ChoiceBox<Integer> f_period = new ChoiceBox<>();
        f_period.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15); // only 15 years by lore
        f_period.valueProperty().addListener((observableValue, integer, t1) -> {
            data.npv.get().amoutYear.set(t1.intValue());
        });
        f_period.valueProperty().set(data.npv.get().amoutYear.get());

        ChoiceBox<Integer> f_amortizedDuration = new ChoiceBox<>();
        f_amortizedDuration.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        f_amortizedDuration.valueProperty().addListener((observableValue, integer, t1) -> {
            data.npv.get().overXYear.set(t1.intValue());
        });
        f_amortizedDuration.valueProperty().set(data.npv.get().overXYear.get());

        GridPane.setHalignment(l_einzahlungGesamt, HPos.RIGHT);
        GridPane.setHalignment(l_auszahlungGesamt, HPos.RIGHT);
        GridPane.setHalignment(l_overRuntime, HPos.RIGHT);
        GridPane.setHalignment(l_periodOverX, HPos.RIGHT);
        GridPane.setHalignment(l_nettoGesamt, HPos.RIGHT);
        l_overRuntime.setTextAlignment(TextAlignment.RIGHT);
        l_periodOverX.setTextAlignment(TextAlignment.RIGHT);
        l_einzahlungGesamt.setTextAlignment(TextAlignment.RIGHT);
        l_auszahlungGesamt.setTextAlignment(TextAlignment.RIGHT);
        l_nettoGesamt.setTextAlignment(TextAlignment.RIGHT);

        double currencyPrefWidth = 120;
        f_kapitalwert.setPrefWidth(currencyPrefWidth);
        f_kapitalwertrate.setPrefWidth(currencyPrefWidth);
        f_einzahlungGesamt.setPrefWidth(currencyPrefWidth);
        f_auszahlungGesamt.setPrefWidth(currencyPrefWidth);
        f_nettoGesamt.setPrefWidth(currencyPrefWidth);

        f_kapitalwert.setEditable(false);
        f_kapitalwertrate.setEditable(false);
        f_nettoGesamt.setEditable(false);
        f_einzahlungGesamt.setEditable(false);
        f_auszahlungGesamt.setEditable(false);


        NumberStringConverter nsc = NumerFormating.getInstance().getCurrencyConverter();
        NumberStringConverter nscNoUnit = NumerFormating.getInstance().getDoubleConverter();

        data.npv.get().amoutYear.addListener(observable -> {
            logger.debug("Debug: new Years amount: " + observable);
        });
        data.npv.get().overXYear.addListener((observable, oldValue, newValue) -> updateRunOverXText(data.npv.get().overXYear.intValue()));
        data.npv.get().amoutYear.addListener((observable, oldValue, newValue) -> updateRunOverXText(data.npv.get().overXYear.intValue()));
        updateRunOverXText(data.npv.get().overXYear.intValue());

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMinimumFractionDigits(4);

        f_zinssatz.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
        Bindings.bindBidirectional(f_zinssatz.textProperty(), data.npv.get().interestRate, new NumberStringConverter());
        f_investition.setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_investition.textProperty(), data.npv.get().investment, nscNoUnit);
        f_infation.setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_infation.textProperty(), data.npv.get().inflation, nscNoUnit);
        f_einsparrung.setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_einsparrung.textProperty(), data.npv.get().einsparung, nscNoUnit);
        f_runningCost.setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_runningCost.textProperty(), data.npv.get().runningCost, nscNoUnit);

        Bindings.bindBidirectional(f_einzahlungGesamt.textProperty(), data.npv.get().sumEinzahlung, nsc);
        Bindings.bindBidirectional(f_auszahlungGesamt.textProperty(), data.npv.get().sumAuszahlungProperty(), nsc);
        Bindings.bindBidirectional(f_kapitalwert.textProperty(), data.npv.get().npvResult, nsc);
        Bindings.bindBidirectional(f_kapitalwertrate.textProperty(), data.npv.get().piResult, decimalFormat);
        Bindings.bindBidirectional(f_nettoGesamt.textProperty(), data.npv.get().sumNetto, nsc);
        Bindings.bindBidirectional(f_kapitalwertOverX.textProperty(), data.npv.get().npvResultOverX, nsc);
        Bindings.bindBidirectional(f_kapitalrateOverX.textProperty(), data.npv.get().piResultOverX, decimalFormat);


        f_zinssatz.setAlignment(Pos.CENTER_RIGHT);
        f_kapitalwert.setAlignment(Pos.CENTER_RIGHT);
        f_auszahlungGesamt.setAlignment(Pos.CENTER_RIGHT);
        f_einzahlungGesamt.setAlignment(Pos.CENTER_RIGHT);
        f_kapitalwertrate.setAlignment(Pos.CENTER_RIGHT);
        f_nettoGesamt.setAlignment(Pos.CENTER_RIGHT);
        f_investition.setAlignment(Pos.CENTER_RIGHT);
        f_einsparrung.setAlignment(Pos.CENTER_RIGHT);
        f_kapitalwertOverX.setAlignment(Pos.CENTER_RIGHT);
        f_kapitalrateOverX.setAlignment(Pos.CENTER_RIGHT);
        f_runningCost.setAlignment(Pos.CENTER_RIGHT);
        f_infation.setAlignment(Pos.CENTER_RIGHT);


        HBox peridBox = new HBox(f_period, l_years, l_over, f_amortizedDuration, l_yearsOverX);
        peridBox.setSpacing(15);
        peridBox.setAlignment(Pos.CENTER_LEFT);

        GridPane topPane = new GridPane();
        topPane.setHgap(10);
        topPane.setVgap(6);

        double fieldWidth = 125;
        topPane.getColumnConstraints().addAll(
                new ColumnConstraints(100, 150, 400),
                new ColumnConstraints(100, fieldWidth, 200),
                new ColumnConstraints(20, 20, 50),
                new ColumnConstraints(20, 20, 20), // Spacer
                new ColumnConstraints(100, 170, 400),
                new ColumnConstraints(100, fieldWidth, 200),
                new ColumnConstraints(20, 20, 50)
        );


        topPane.addRow(0, l_investition, f_investition, l_investitionUnit);
        topPane.addRow(1, l_einsparrung, f_einsparrung, l_einsparrungUnit, new Region(), l_yearCost, f_runningCost, l_yearCostUnit);
        topPane.addRow(2, l_zinssatz, f_zinssatz, l_proZent, new Region(), l_infation, f_infation, l_infationUnit);
        topPane.add(l_period, 0, 4);
        topPane.add(peridBox, 1, 4, 6, 1);
        GridPane bottomPane = new GridPane();
        bottomPane.setHgap(10);
        bottomPane.setVgap(6);
        bottomPane.addRow(0, new Region(), l_overRuntime, l_periodOverX);
        bottomPane.addRow(1, l_kapitalwert, f_kapitalwert, f_kapitalwertOverX);
        bottomPane.addRow(3, l_kapitalwertrate, f_kapitalwertrate, f_kapitalrateOverX);

        GridPane sumPane = new GridPane();
        sumPane.setHgap(12);
        sumPane.setVgap(6);
        sumPane.addRow(0, new Region(), l_einzahlungGesamt, l_auszahlungGesamt, l_nettoGesamt);
        sumPane.addRow(1, l_gesamt, f_einzahlungGesamt, f_auszahlungGesamt, f_nettoGesamt);

        int toalColums = 6;
        gridPane.add(topPane, 0, 0, 5, 1);
        gridPane.add(new Separator(), 0, 1, toalColums, 1);
        gridPane.add(npvTableView, 0, 2, toalColums, 1);
        gridPane.add(new Separator(), 0, 3, toalColums, 1);
        gridPane.add(sumPane, 0, 4, 4, 1);

        gridPane.add(new Separator(), 0, 6, toalColums, 1);
        gridPane.add(bottomPane, 0, 7, 4, 1);

        data.npv.get().update();
        data.npv.get().updateResults();

        setContent(gridPane);
    }

    private void updateRunOverXText(int value) {
        l_periodOverX.setText(String.format(I18n.getInstance().getString("actionform.editor.tab.captial.amortoveryear"), value));
    }
}
