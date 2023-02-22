package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.FileData;
import org.jevis.jeconfig.plugin.action.data.NPVYearData;
import org.jevis.jeconfig.tool.ScreenSize;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ActionForm extends Dialog {

    private final JFXDatePicker f_plannedDate = new JFXDatePicker();
    private final JFXDatePicker f_doneDate = new JFXDatePicker();

    private JFXTextField f_isNewEnPI = new JFXTextField();


    //private CheckComboBox<String> f_statusTags;
    private ComboBox<String> f_statusTags;
    private CheckComboBox<String> f_fieldTags;
    private ComboBox<String> f_mediaTags;
    private JFXTextField f_FromUser = new JFXTextField();
    private JFXTextField f_toUser = new JFXTextField();
    private TextArea f_Note = new TextArea();
    private TextArea f_Description = new TextArea();
    private JFXDatePicker f_CreateDate = new JFXDatePicker();
    private JFXTextField f_ActionNr = new JFXTextField();
    private JFXTextField f_Title = new JFXTextField();
    private TextArea f_NoteEnergiefluss = new TextArea();
    private JFXTextField f_Attachment = new JFXTextField();
    private TextArea f_NoteBewertet = new TextArea();
    private JFXTextField f_Responsible = new JFXTextField();
    private TabPane tabPane = new TabPane();
    private Tab basicTab = new Tab(I18n.getInstance().getString("actionform.editor.tab.general"));
    private Tab detailTab = new Tab(I18n.getInstance().getString("actionform.editor.tab.deteils"));
    private Tab attachmentTab = new Tab(I18n.getInstance().getString("actionform.editor.tab.attachment"));
    private Tab capitalTab = new Tab(I18n.getInstance().getString("actionform.editor.tab.capital"));
    private Tab checkListTab = new Tab(I18n.getInstance().getString("actionform.editor.tab.checklist"));

    private JFXTextField f_Investment = new JFXTextField();
    private JFXTextField f_savingYear = new JFXTextField();
    private JFXTextField f_enpiAfter = new JFXTextField();
    private JFXTextField f_enpiBefore = new JFXTextField();
    private JFXTextField f_enpiDiff = new JFXTextField();
    private JFXTextField f_distributor = new JFXTextField();

    private JFXTextField f_energyBefore = new JFXTextField();
    private JFXTextField f_energyAfter = new JFXTextField();
    private JFXTextField f_energyChange = new JFXTextField();

    private CheckBox f_isTargetReached = new CheckBox();
    private TextArea f_correctionIfNeeded = new TextArea("Korrekturmaßnahmen");
    private TextArea f_nextActionIfNeeded = new TextArea("Folgemaßnahmen");
    private TextArea f_alternativAction = new TextArea("Alternativmaßnahmen");

    private TextFieldWithUnit f_consumptionBefore = new TextFieldWithUnit();
    private TextFieldWithUnit f_consumptionAfter = new TextFieldWithUnit();
    private TextFieldWithUnit f_consumptionDiff = new TextFieldWithUnit();

    private ComboBox<JEVisObject> f_Enpi = new ComboBox();
    private ActionPlanData actionPlan;

    private CheckBox f_isNeedProcessDocument = new CheckBox();
    private CheckBox f_isNeedWorkInstruction = new CheckBox();
    private CheckBox f_isNeedTestInstruction = new CheckBox();
    private CheckBox f_isNeedDrawing = new CheckBox();
    private CheckBox f_isNeedOther = new CheckBox();
    private CheckBox f_IsNeedAdditionalMeters = new CheckBox();
    private CheckBox f_IsAffectsOtherProcess = new CheckBox();
    private CheckBox f_IsConsumptionDocumented = new CheckBox();
    private CheckBox f_isNeedCorrection = new CheckBox();
    private CheckBox f_isNeedAdditionalAction = new CheckBox();
    private ActionData names = new ActionData();


    public ActionForm(ActionPlanData actionPlan, ActionData action) {
        super();
        this.initOwner(JEConfig.getStage());
        this.actionPlan = actionPlan;


        setTitle(I18n.getInstance().getString("actionform.editor.title"));
        setHeaderText(null);
        setResizable(true);
        this.getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1000));
        this.getDialogPane().setPrefHeight(ScreenSize.fitScreenHeight(950));

        widthProperty().addListener((observable, oldValue, newValue) -> tabPane.setPrefWidth(newValue.doubleValue() - 50));


        tabPane.setPrefWidth(ScreenSize.fitScreenWidth(1050));

        f_Enpi = new ComboBox(actionPlan.getEnpis());
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> enpiCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        };
        f_Enpi.setCellFactory(enpiCellFactory);
        f_Enpi.setButtonCell(enpiCellFactory.call(null));

        f_statusTags = new ComboBox<>(actionPlan.getStatustags());
        //f_statusTags = new CheckComboBox<>(actionPlan.getStatustags());
        f_fieldTags = new CheckComboBox<>(actionPlan.getFieldsTags());
        f_mediaTags = new ComboBox<>(actionPlan.getMediumTags());

        f_Title.widthProperty().addListener((observable, oldValue, newValue) -> {
            f_statusTags.setPrefWidth(newValue.doubleValue());
            f_fieldTags.setPrefWidth(newValue.doubleValue());
            f_mediaTags.setPrefWidth(newValue.doubleValue());
            f_Enpi.setPrefWidth(newValue.doubleValue());
        });

        basicTab.setClosable(false);
        detailTab.setClosable(false);
        attachmentTab.setClosable(false);
        capitalTab.setClosable(false);
        //capitalTab.setDisable(true);
        checkListTab.setClosable(false);

        tabPane.getTabs().addAll(basicTab, detailTab, capitalTab, checkListTab, attachmentTab);
        getDialogPane().setContent(tabPane);

        setData(action);
    }

    private void setData(ActionData data) {
        initGeneralTab(data);
        initDetailsTab(data);
        initAttachmentTab(data);
        initCapitalValueTab(data);
        initCheckListTab(data);
        updateView(data);

    }

    private void initCapitalValueTab(ActionData data) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25, 12, 12, 12));
        gridPane.setVgap(10);
        gridPane.setHgap(15);

        Label l_investition = new Label("Investition");
        JFXTextField f_investition = new JFXTextField();
        Label l_investitionUnit = new Label("€");
        Label l_einsparrung = new Label("Jährliche Einsparung");
        JFXTextField f_einsparrung = new JFXTextField();
        Label l_einsparrungUnit = new Label("€");

        Label l_zinssatz = new Label("Zinssatz");
        JFXTextField f_zinssatz = new JFXTextField("");
        f_zinssatz.setPrefWidth(50);
        Label l_proZent = new Label("%");
        Label l_years = new Label("Jahr(e)");
        Label l_kapitalwert = new Label("Kapitalwert");
        JFXTextField f_kapitalwert = new JFXTextField("");
        Label l_kapitalwertrate = new Label("Kapitalwertrate");
        JFXTextField f_kapitalwertrate = new JFXTextField("");
        Label l_period = new Label("Laufzeit");
        Label l_periodOverX = new Label("Laufzeit über X");
        Label l_overRuntime = new Label("Amortisation über die Laufzeit");
        Label l_over = new Label("Amortisation über");
        Label l_infation = new Label("Jährliche Preissteigerung");
        Label l_infationUnit = new Label("%");
        JFXTextField f_infation = new JFXTextField();
        Label l_yearCost = new Label("Jährliche Betriebskosten");
        Label l_yearCostUnit = new Label("€");
        JFXTextField f_runningCost = new JFXTextField();


        Label l_yearsOverX = new Label("Jahr(e)");
        JFXTextField f_kapitalwertOverX = new JFXTextField();
        JFXTextField f_kapitalrateOverX = new JFXTextField();
        NPVTableView npvTableView = new NPVTableView(data.npv.get().npvYears);

        Label l_einzahlungGesamt = new Label("Einsparung");
        l_einzahlungGesamt.setAlignment(Pos.CENTER);
        JFXTextField f_einzahlungGesamt = new JFXTextField();


        Label l_auszahlungGesamt = new Label("Investition");
        l_auszahlungGesamt.setAlignment(Pos.CENTER);
        JFXTextField f_auszahlungGesamt = new JFXTextField();
        Label l_nettoGesamt = new Label("Netto");
        l_nettoGesamt.setAlignment(Pos.CENTER);
        JFXTextField f_nettoGesamt = new JFXTextField();
        Label l_gesamt = new Label("Gesamt");

        ChoiceBox<Integer> f_period = new ChoiceBox<>();
        f_period.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15); // only 15 years by lore
        f_period.setValue(data.npv.get().npvYears.size());

        ChoiceBox<Integer> f_amortizedDuration = new ChoiceBox<>();
        f_amortizedDuration.valueProperty().bindBidirectional(data.npv.get().overXYear.asObject());
        f_amortizedDuration.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

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


        f_period.setOnAction(event -> {
            int diff = f_period.getValue() - data.npv.get().npvYears.size();
            if (diff > 0) {
                for (int i = data.npv.get().npvYears.size(); i <= f_period.getValue(); i++) {
                    NPVYearData npvYearData = new NPVYearData();
                    npvYearData.year.set(i);
                    npvYearData.setNpvData(data.npv.get());
                    npvYearData.setDeposit(data.npv.get().einsparung.get());
                    data.npv.get().npvYears.add(npvYearData);
                }

            } else if (diff < 0) {
                data.npv.get().npvYears.remove(f_period.getValue() + 1, data.npv.get().npvYears.size());
            }

            data.npv.get().npvYears.forEach(NPVYearData::updateSums);
            data.npv.get().updateResults();
        });


        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(2);
        doubleFormat.setMaximumFractionDigits(2);
        doubleFormat.setGroupingUsed(true);

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));

        NumberStringConverter nsc = new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return currencyFormat.format(value);
            }
        };

        NumberStringConverter nscNoUnit = new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return doubleFormat.format(value);
            }
        };

        l_periodOverX.setText("Amortisation über " + data.npv.get().overXYear.get() + " Jahre");
        data.npv.get().overXYear.addListener(observable -> l_periodOverX.setText("Amortisation über " + observable.toString() + " Year"));
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMinimumFractionDigits(4);

        Bindings.bindBidirectional(f_zinssatz.textProperty(), data.npv.get().interestRate, new NumberStringConverter());
        Bindings.bindBidirectional(f_kapitalwert.textProperty(), data.npv.get().npvResult, nsc);
        Bindings.bindBidirectional(f_auszahlungGesamt.textProperty(), data.npv.get().sumAuszahlungProperty(), nsc);
        Bindings.bindBidirectional(f_einzahlungGesamt.textProperty(), data.npv.get().sumEinzahlung, nsc);
        Bindings.bindBidirectional(f_kapitalwertrate.textProperty(), data.npv.get().piResult, decimalFormat);
        Bindings.bindBidirectional(f_nettoGesamt.textProperty(), data.npv.get().sumNetto, nsc);
        Bindings.bindBidirectional(f_investition.textProperty(), data.npv.get().investition, nscNoUnit);
        Bindings.bindBidirectional(f_einsparrung.textProperty(), data.npv.get().einsparung, nscNoUnit);
        Bindings.bindBidirectional(f_kapitalwertOverX.textProperty(), data.npv.get().npvResultOverX, nsc);
        Bindings.bindBidirectional(f_kapitalrateOverX.textProperty(), data.npv.get().piResultOverX, decimalFormat);
        Bindings.bindBidirectional(f_infation.textProperty(), data.npv.get().inflation, nscNoUnit);
        Bindings.bindBidirectional(f_runningCost.textProperty(), data.npv.get().runningCost, nscNoUnit);

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

        data.npv.get().updateResults();

        capitalTab.setContent(gridPane);
    }

    private void formateCurrencyInGrid(JFXTextField node) {
        GridPane.setHalignment(node, HPos.RIGHT);
        node.setPrefWidth(100);

    }

    private void initGeneralTab(ActionData data) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(10);
        gridPane.setHgap(15);
        //gridPane.gridLinesVisibleProperty().set(true);

        Label l_Note = new Label();
        Label l_Description = new Label();
        Label l_ActionNr = new Label();
        Label l_Investment = new Label("Investment:");
        Label l_changeKost = new Label("Änderung Kosten/Jahr:");
        Label l_Responsible = new Label();
        Label l_NoteBewertet = new Label();
        Label l_Attachment = new Label();
        Label l_Title = new Label();
        Label l_NoteEnergiefluss = new Label();
        Label l_doneDate = new Label();
        Label l_plannedDate = new Label();
        Label l_mediaTags = new Label();
        Label l_statusTags = new Label();
        Label l_fieldTags = new Label();
        Region col3Spacer = new Region();
        Label l_Enpi = new Label("EnPI");
        col3Spacer.setMinWidth(25);

        f_savingYear.setTextFormatter(new TextFormatter(new UnitDoubleConverter()));
        JFXTextField l_savingsUnitLabel = new JFXTextField("€");
        l_savingsUnitLabel.setEditable(false);
        l_savingsUnitLabel.setPrefWidth(25);
        HBox savingsBox = new HBox(f_savingYear, l_savingsUnitLabel);
        HBox.setHgrow(f_savingYear, Priority.ALWAYS);
        f_savingYear.setAlignment(Pos.BASELINE_RIGHT);

        f_Investment.setTextFormatter(new TextFormatter(new UnitDoubleConverter()));
        JFXTextField l_investmentUnitLabel = new JFXTextField("€");
        l_investmentUnitLabel.setEditable(false);
        l_investmentUnitLabel.setPrefWidth(25);
        HBox investBox = new HBox(f_Investment, l_investmentUnitLabel);
        HBox.setHgrow(f_Investment, Priority.ALWAYS);
        f_Investment.setAlignment(Pos.BASELINE_RIGHT);


        add(gridPane, 1, 1, 1, 1, Priority.NEVER, l_ActionNr);
        add(gridPane, 1, 2, 1, 1, Priority.NEVER, l_Title);
        add(gridPane, 1, 3, 1, 1, Priority.NEVER, l_Responsible);
        add(gridPane, 1, 4, 1, 1, Priority.NEVER, l_plannedDate);
        add(gridPane, 1, 5, 1, 1, Priority.NEVER, l_doneDate);

        add(gridPane, 2, 1, 1, 1, Priority.SOMETIMES, f_ActionNr);
        add(gridPane, 2, 2, 1, 1, Priority.SOMETIMES, f_Title);
        add(gridPane, 2, 3, 1, 1, Priority.SOMETIMES, f_Responsible);
        add(gridPane, 2, 4, 1, 1, Priority.SOMETIMES, f_plannedDate);
        add(gridPane, 2, 5, 1, 1, Priority.SOMETIMES, f_doneDate);


        add(gridPane, 1, 8, 2, 1, Priority.SOMETIMES, l_Description);
        add(gridPane, 1, 9, 2, 1, Priority.SOMETIMES, f_Description);
        add(gridPane, 1, 10, 2, 1, Priority.SOMETIMES, l_NoteBewertet);
        add(gridPane, 1, 11, 2, 1, Priority.SOMETIMES, f_NoteBewertet);

        //Spacer column
        gridPane.add(col3Spacer, 3, 1);

        add(gridPane, 4, 1, 1, 1, Priority.SOMETIMES, l_statusTags);
        add(gridPane, 4, 2, 1, 1, Priority.SOMETIMES, l_fieldTags);
        add(gridPane, 4, 3, 1, 1, Priority.SOMETIMES, l_mediaTags);
        add(gridPane, 4, 4, 1, 1, Priority.SOMETIMES, l_Enpi);
        add(gridPane, 4, 5, 1, 1, Priority.SOMETIMES, l_Investment);
        add(gridPane, 4, 6, 1, 1, Priority.SOMETIMES, l_changeKost);
        add(gridPane, 4, 7, 1, 1, Priority.SOMETIMES, l_Attachment);

        add(gridPane, 5, 1, 1, 1, Priority.SOMETIMES, f_statusTags);
        add(gridPane, 5, 2, 1, 1, Priority.SOMETIMES, f_fieldTags);
        add(gridPane, 5, 3, 1, 1, Priority.SOMETIMES, f_mediaTags);
        add(gridPane, 5, 4, 1, 1, Priority.SOMETIMES, f_Enpi);
        add(gridPane, 5, 5, 1, 1, Priority.SOMETIMES, investBox);//f_Investment
        add(gridPane, 5, 6, 1, 1, Priority.SOMETIMES, savingsBox);//f_savings
        add(gridPane, 5, 7, 1, 1, Priority.SOMETIMES, f_Attachment);

        add(gridPane, 4, 8, 2, 1, Priority.SOMETIMES, l_NoteEnergiefluss);
        add(gridPane, 4, 9, 2, 1, Priority.SOMETIMES, f_NoteEnergiefluss);
        add(gridPane, 4, 10, 2, 1, Priority.SOMETIMES, l_Note);
        add(gridPane, 4, 11, 2, 1, Priority.SOMETIMES, f_Note);

        l_Attachment.setVisible(false);
        f_Attachment.setVisible(false);

        int textFieldHeight = 200;
        int textFieldWeight = 400;

        f_Description.setMaxHeight(textFieldHeight);
        f_Note.setMaxHeight(textFieldHeight);
        f_NoteBewertet.setMaxHeight(textFieldHeight);
        f_NoteEnergiefluss.setMaxHeight(textFieldHeight);
        f_Description.setPrefWidth(textFieldWeight);
        f_Note.setPrefWidth(textFieldWeight);
        f_NoteBewertet.setPrefWidth(textFieldWeight);
        f_NoteEnergiefluss.setPrefWidth(textFieldWeight);

        l_Description.setPadding(new Insets(15, 0, 0, 0));
        l_Note.setPadding(new Insets(15, 0, 0, 0));
        l_NoteBewertet.setPadding(new Insets(15, 0, 0, 0));
        l_NoteEnergiefluss.setPadding(new Insets(15, 0, 0, 0));

        f_ActionNr.setEditable(false);

        GridPane.setHgrow(f_statusTags, Priority.ALWAYS);

        scrollPane.setContent(gridPane);
        basicTab.setContent(scrollPane);

        l_doneDate.setText(names.doneDateProperty().getName());
        l_plannedDate.setText(names.plannedDateProperty().getName());
        l_Note.setText(names.noteProperty().getName());
        l_Description.setText(names.desciptionProperty().getName());
        System.out.println("nr later:" + names.nrProperty().getName());
        l_ActionNr.setText(names.nrProperty().getName());
        l_Attachment.setText(names.attachmentProperty().getName());
        l_statusTags.setText(names.statusTagsProperty().getName());
        l_fieldTags.setText(names.fieldTagsProperty().getName());
        l_mediaTags.setText(names.mediaTagsProperty().getName());
        l_Responsible.setText(names.responsibleProperty().getName());

        l_Title.setText(names.noteBetroffenerProzessProperty().getName());
        l_NoteBewertet.setText(names.noteBewertetProperty().getName());
        l_NoteEnergiefluss.setText(names.noteEnergieflussProperty().getName());

        l_Title.setWrapText(true);
        l_NoteBewertet.setWrapText(true);
        l_NoteEnergiefluss.setWrapText(true);

    }


    private void add(GridPane pane, int column, int row, int colspan, int rowspan, Priority priority, Node node) {
        pane.add(node, column, row, colspan, rowspan);
        GridPane.setHgrow(node, priority);
    }

    private void initAttachmentTab(ActionData data) {
        AnchorPane anchorPane = new AnchorPane();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(8));
        gridPane.setHgap(15);
        anchorPane.getChildren().add(gridPane);
        AnchorPane.setTopAnchor(gridPane, 0.0);
        AnchorPane.setLeftAnchor(gridPane, 0.0);
        AnchorPane.setRightAnchor(gridPane, 0.0);
        AnchorPane.setBottomAnchor(gridPane, 0.0);
        attachmentTab.setContent(anchorPane);


        //ToggleButton renameFileButton = new ToggleButton("", JEConfig.getSVGImage(Icon.R, iconSize, iconSize));
        ObservableList<FileData> fileData = FXCollections.observableArrayList();
        FileTableView fileTableView = new FileTableView(fileData);
        double iconSize = 12;
        Button addFileButton = new Button("", JEConfig.getSVGImage(Icon.PLUS, iconSize, 18));
        Button downloadFileButton = new Button("", JEConfig.getSVGImage(Icon.EXPORT, iconSize, 18));
        Button deleteFileButton = new Button("", JEConfig.getSVGImage(Icon.DELETE, iconSize, 18));


        addFileButton.setDisable(true);
        try {
            addFileButton.setDisable(!data.getObject().getDataSource().getCurrentUser().canCreate(data.getObject().getID()));
        } catch (Exception ex) {

        }

        deleteFileButton.setOnAction(event -> {
            fileTableView.deleteSelectedFile();
        });

        downloadFileButton.setOnAction(event -> fileTableView.saveSelectedFile());

        addFileButton.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(JEConfig.getStage());
                if (file != null) {
                    System.out.println("Create file under: " + data.getObject().toString());
                    JEVisClass fileClass = data.getObject().getDataSource().getJEVisClass(JC.File.name);

                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    DateTime changeDate = new DateTime(Long.valueOf(attr.lastModifiedTime().toMillis()), DateTimeZone.getDefault());

                    JEVisFileImp jFile = new JEVisFileImp(file.getName(), file);
                    JEVisObject newFileObj = data.getObject().buildObject(file.getName(), fileClass);
                    newFileObj.commit();
                    JEVisAttribute fileAtt = newFileObj.getAttribute(JC.File.a_File);
                    fileAtt.buildSample(new DateTime(), jFile).commit();
                    FileData newFile = new FileData(newFileObj);
                    fileData.add(newFile);

                }

            } catch (Exception xe) {
                xe.printStackTrace();
            }
        });

        try {
            JEVisClass fileClass = data.getObject().getDataSource().getJEVisClass(JC.File.name);
            List<JEVisObject> files = data.getObject().getChildren(fileClass, true);
            files.forEach(jeVisObject -> {
                fileData.add(new FileData(jeVisObject));
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //fileData.add(new FileData("Checkliste.pdf", new DateTime(), "Florian Simon"));

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().setAll(addFileButton, downloadFileButton, deleteFileButton);
        //ToolBar toolBar = new ToolBar(newButton, addFileButton, removeFileButton);
        // HBox toolbar = new HBox(addFileButton, removeFileButton);
        //toolBar.getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));

        gridPane.add(toolBar, 0, 0, 1, 1);
        gridPane.add(fileTableView, 0, 1, 1, 1);
        GridPane.setHgrow(fileTableView, Priority.ALWAYS);
        GridPane.setVgrow(fileTableView, Priority.ALWAYS);

    }

    private void initDetailsTab(ActionData data) {
        GridPane gridPane = new GridPane();
        //gridPane.setPrefWidth(1000);
        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(15);
        gridPane.setHgap(15);

        scrollPane.setContent(gridPane);
        detailTab.setContent(scrollPane);

        Label l_CreateDate = new Label(names.createDateProperty().getName());
        Label l_FromUser = new Label(names.fromUserProperty().getName());
        Label l_distributor = new Label(names.distributorProperty().getName());
        Label l_enpiAfter = new Label(names.enpiAfterProperty().getName());
        Label l_enpiBefore = new Label(names.enpiBeforeProperty().getName());
        Label l_enpiChange = new Label(names.enpiChangeProperty().getName());
        Label l_correctionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.correction"));
        Label l_nextActionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.followupaction"));
        Label l_alternativAction = new Label(I18n.getInstance().getString("plugin.action.alternativaction"));
        Label l_energyBefore = new Label("Verbrauch (Referenz)");
        Label l_energyAfter = new Label("Verbrauch (Ist)");
        Label l_energyChange = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));

        Button beforeDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
        Button afterDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
        Button diffDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
        diffDateButton.setVisible(false);

        JFXButton buttonOpenAnalysisBefore = new JFXButton("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
        Button buttonOpenAnalysisafter = new Button("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
        Button buttonOpenAnalysisaDiff = new Button("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));


        f_enpiAfter.setEditable(false);
        f_enpiBefore.setEditable(false);
        f_enpiDiff.setEditable(false);

        Region spacerEnpiBefore = new Region();
        Region spacerEnpiAfter = new Region();
        Region spacerEnpiDiff = new Region();
        HBox.setHgrow(spacerEnpiBefore, Priority.ALWAYS);
        HBox.setHgrow(spacerEnpiAfter, Priority.ALWAYS);
        HBox.setHgrow(spacerEnpiDiff, Priority.ALWAYS);


        HBox box_EnpiAfter = new HBox(buttonOpenAnalysisBefore, afterDateButton, spacerEnpiBefore, f_enpiAfter);
        HBox box_EnpiBefore = new HBox(buttonOpenAnalysisafter, beforeDateButton, spacerEnpiBefore, f_enpiBefore);
        HBox box_EnpiDiff = new HBox(buttonOpenAnalysisaDiff, diffDateButton, spacerEnpiBefore, f_enpiDiff);
        box_EnpiAfter.setSpacing(8);
        box_EnpiBefore.setSpacing(8);
        box_EnpiDiff.setSpacing(8);
        HBox.setHgrow(f_enpiAfter, Priority.SOMETIMES);
        HBox.setHgrow(f_enpiBefore, Priority.SOMETIMES);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        beforeDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    new DateTime(2022, 8, 1, 0, 0),
                    new DateTime(2022, 8, 1, 0, 0));

            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                data.enpiBeforeProperty().set(fmt.print(timeRangeDialog.getFromDate()) + "&" + fmt.print(timeRangeDialog.getUntilDate()));
            }
        });
        afterDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    new DateTime(2022, 8, 1, 0, 0),
                    new DateTime(2022, 8, 1, 0, 0));
            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                data.enpiAfterProperty().set(fmt.print(timeRangeDialog.getFromDate()) + "&" + fmt.print(timeRangeDialog.getUntilDate()));
            }

        });


        buttonOpenAnalysisBefore.setOnAction(event -> {
            try {
                Long enpiData = Long.parseLong(data.enpilinksProperty().get().replace(";", ""));
                JEVisAttribute attribute = data.getObject().getDataSource().getObject(enpiData).getAttribute("Value");

                /**
                 AnalysisRequest analysisRequest = new AnalysisRequest(attribute.getObject(),
                 AggregationPeriod.NONE,
                 ManipulationMode.NONE,
                 startDateFromSampleRate, timestampFromLastSample);
                 analysisRequest.setAttribute(attribute);
                 JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest)
                 */
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });


        data.enpilinksProperty().addListener(observable -> {
            System.out.println("Enpi link changed");

        });

        f_correctionIfNeeded.setPrefWidth(400);
        f_nextActionIfNeeded.setPrefWidth(400);
        f_alternativAction.setPrefWidth(400);
        f_energyAfter.setAlignment(Pos.CENTER_RIGHT);
        f_energyBefore.setAlignment(Pos.CENTER_RIGHT);
        f_energyChange.setAlignment(Pos.CENTER_RIGHT);
        f_enpiAfter.setAlignment(Pos.CENTER_RIGHT);
        f_enpiBefore.setAlignment(Pos.CENTER_RIGHT);
        f_enpiDiff.setAlignment(Pos.CENTER_RIGHT);

        gridPane.addRow(0, l_enpiBefore, box_EnpiBefore, new Region(), l_energyBefore, f_consumptionBefore);
        gridPane.addRow(1, l_enpiAfter, box_EnpiAfter, new Region(), l_energyAfter, f_consumptionAfter);
        gridPane.addRow(2, l_enpiChange, box_EnpiDiff, new Region(), l_energyChange, f_consumptionDiff);
        gridPane.addRow(3, new Region());

        System.out.println("l_FromUser: " + l_FromUser.textProperty().get());
        gridPane.addRow(4, l_FromUser, f_FromUser, new Region(), l_CreateDate, f_CreateDate);
        gridPane.addRow(5, l_distributor, f_distributor);

        gridPane.addRow(6, l_correctionIfNeeded, new Region(), new Region(), l_nextActionIfNeeded, new Region());
        gridPane.addRow(7, f_correctionIfNeeded, new Region(), new Region(), f_nextActionIfNeeded, new Region());
        // gridPane.addRow(8, l_alternativAction);
        // gridPane.addRow(9, f_alternativAction, new Region(), new Region(), new Region(), new Region());

        GridPane.setColumnSpan(l_correctionIfNeeded, 2);
        GridPane.setColumnSpan(l_nextActionIfNeeded, 2);
        GridPane.setColumnSpan(f_correctionIfNeeded, 2);
        GridPane.setColumnSpan(f_nextActionIfNeeded, 2);
        GridPane.setColumnSpan(l_alternativAction, 2);
        GridPane.setColumnSpan(f_alternativAction, 2);

    }

    private void initCheckListTab(ActionData data) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(15);
        gridPane.setHgap(15);

        Label l_isNeedProcessDocument = new Label(names.isNeedProcessDocumentProperty().getName());
        Label l_isNeedWorkInstruction = new Label(names.isNeedWorkInstructionProperty().getName());
        Label l_isNeedTestInstruction = new Label(names.isNeedTestInstructionProperty().getName());
        Label l_isNeedDrawing = new Label(names.isNeedDrawingProperty().getName());
        Label l_isNeedOther = new Label(names.isNeedOtherProperty().getName());
        Label l_isNeedAdditionalAction = new Label(names.isNeedAdditionalActionProperty().getName());
        Label l_isAffectsOtherProcess = new Label(names.isAffectsOtherProcessProperty().getName());
        Label l_isConsumptionDocumented = new Label(names.isConsumptionDocumentedProperty().getName());
        Label l_isTargetReached = new Label(names.isTargetReachedProperty().getName());
        Label l_isNewEnPI = new Label(names.isNewEnPIProperty().getName());
        Label l_isNeedDocumentCorrection = new Label(names.isNeedDocumentCorrectionProperty().getName());
        Label l_isNeedCorrection = new Label(names.isNeedCorrectionProperty().getName());
        Label l_IsNeedAdditionalMeters = new Label(names.isNeedAdditionalMetersProperty().getName());

        Label l_titleDocument = new Label(I18n.getInstance().getString("plugin.action.needdocchange.title"));


        l_isNeedProcessDocument.setText("Prozessanweisung");
        l_isNeedWorkInstruction.setText("Arbeitsanweisung");
        l_isNeedTestInstruction.setText("Prüfanweisung");
        l_isNeedDrawing.setText("Zeichnungen");
        l_isNeedOther.setText("Sonstige");


        HBox q1 = new HBox(f_IsNeedAdditionalMeters, l_IsNeedAdditionalMeters);
        HBox q2 = new HBox(f_IsAffectsOtherProcess, l_isAffectsOtherProcess);
        HBox q3 = new HBox(f_isTargetReached, l_isTargetReached);
        HBox q4 = new HBox(f_isNeedCorrection, l_isNeedCorrection);
        HBox q5 = new HBox(f_isNeedAdditionalAction, l_isNeedAdditionalAction);
        HBox qDoc1 = new HBox(f_isNeedProcessDocument, l_isNeedProcessDocument);
        HBox qDoc2 = new HBox(f_isNeedWorkInstruction, l_isNeedWorkInstruction);
        HBox qDoc3 = new HBox(f_isNeedTestInstruction, l_isNeedTestInstruction);
        HBox qDoc4 = new HBox(f_isNeedDrawing, l_isNeedDrawing);
        HBox qDoc5 = new HBox(f_isNeedOther, l_isNeedOther);

        qDoc1.setPadding(new Insets(0, 0, 0, 20));
        qDoc2.setPadding(new Insets(0, 0, 0, 20));
        qDoc3.setPadding(new Insets(0, 0, 0, 20));
        qDoc4.setPadding(new Insets(0, 0, 0, 20));
        qDoc5.setPadding(new Insets(0, 0, 0, 20));

        q1.setPadding(new Insets(0, 0, 0, 20));
        q2.setPadding(new Insets(0, 0, 0, 20));
        q3.setPadding(new Insets(0, 0, 0, 20));
        q4.setPadding(new Insets(0, 0, 0, 20));
        q5.setPadding(new Insets(0, 0, 0, 20));

        int row = 0;

        gridPane.add(new Label(I18n.getInstance().getString("plugin.action.dependencies.title")), 0, ++row, 2, 1);
        gridPane.add(q1, 0, ++row, 2, 1);
        gridPane.add(q2, 0, ++row, 2, 1);
        //gridPane.add(q3, 0, ++row, 2, 1);
        //gridPane.add(q4, 0, ++row, 2, 1);
        gridPane.add(q5, 0, ++row, 2, 1);

        row = 9;
        gridPane.add(l_titleDocument, 0, ++row, 2, 1);
        gridPane.add(qDoc1, 0, ++row, 2, 1);
        gridPane.add(qDoc2, 0, ++row, 2, 1);
        gridPane.add(qDoc3, 0, ++row, 2, 1);
        gridPane.add(qDoc4, 0, ++row, 2, 1);
        gridPane.add(qDoc5, 0, ++row, 2, 1);


        checkListTab.setContent(gridPane);
    }

    private void updateEnpi() {
        try {
            DecimalFormat f = new DecimalFormat("#0.00");

            JEVisObject dataObj = actionPlan.getObject().getDataSource().getObject(9610l);
            JEVisObject calcObj = actionPlan.getObject().getDataSource().getObject(9629l);

            JEVisUnit unit = dataObj.getAttribute("Value").getDisplayUnit();
            double before = calcEnpi(dataObj, calcObj, unit,
                    new DateTime(2022, 8, 8, 0, 0), new DateTime(2022, 8, 31, 0, 0));

            double after = calcEnpi(dataObj, calcObj, unit,
                    new DateTime(2022, 12, 1, 0, 0), new DateTime(2023, 1, 5, 0, 0));

            double diff = before - after;

            f_enpiBefore.setText(f.format(before) + " " + unit.toString());
            f_enpiBefore.setTooltip(new Tooltip(before + " " + unit.toString()));

            f_enpiAfter.setText(f.format(after) + " " + unit.toString());
            f_enpiAfter.setTooltip(new Tooltip(after + " " + unit.toString()));

            f_enpiDiff.setText(f.format(diff) + " " + unit.toString());
            f_enpiDiff.setTooltip(new Tooltip(diff + " " + unit.toString()));


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Double calcEnpi(JEVisObject dataObj, JEVisObject calcObj, JEVisUnit unit, DateTime from, DateTime until) {
        try {
            System.out.println("ENPI changed: " + dataObj);

            ChartDataRow chartDataRow = new ChartDataRow(actionPlan.getObject().getDataSource());
            chartDataRow.setId(dataObj.getID());
            chartDataRow.setEnPI(true);
            chartDataRow.setCalculationObject(calcObj);
            chartDataRow.setSelectedStart(from);
            chartDataRow.setSelectedEnd(until);
            chartDataRow.setAggregationPeriod(AggregationPeriod.NONE);
            chartDataRow.setManipulationMode(ManipulationMode.NONE);
            chartDataRow.setAbsolute(true);
            // JEVisUnit unit = unit;//dataObj.getAttribute("Value").getDisplayUnit();
            chartDataRow.setUnit(unit);
            System.out.println("Get Data");
            List<JEVisSample> samples = chartDataRow.getSamples();

            for (JEVisSample jeVisSample : samples) {
                try {
                    System.out.println("Sample: " + jeVisSample);
                    return jeVisSample.getValueAsDouble();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Double(0);
    }


    private void updateView(ActionData data) {
        //Gson gson = CustomAdapter.createDefaultBuilder().create();
        // System.out.println("Jsons--------\n" + gson.toJson(data));


        f_distributor.textProperty().bindBidirectional(data.distributorProperty());
        f_FromUser.textProperty().bindBidirectional(data.fromUserProperty());
        f_Note.textProperty().bindBidirectional(data.noteProperty());
        f_Description.textProperty().bindBidirectional(data.desciptionProperty());
        f_Title.textProperty().bindBidirectional(data.titleProperty());
        f_NoteEnergiefluss.textProperty().bindBidirectional(data.noteEnergieflussProperty());
        f_NoteBewertet.textProperty().bindBidirectional(data.noteBewertetProperty());
        f_isNewEnPI.textProperty().bindBidirectional(data.isNewEnPIProperty());
        f_Responsible.textProperty().bindBidirectional(data.responsibleProperty());

        f_Investment.textProperty().bindBidirectional(data.investmentProperty());
        f_savingYear.textProperty().bindBidirectional(data.DELETEsavingyearProperty());
        f_enpiAfter.textProperty().bindBidirectional(data.enpiAfterProperty());
        f_enpiBefore.textProperty().bindBidirectional(data.enpiBeforeProperty());
        f_statusTags.getSelectionModel().select(data.statusTagsProperty().getValue());
        f_statusTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Status change: " + newValue + " = " + ActionPlanData.STATUS_DONE + " = " + newValue.equals(ActionPlanData.STATUS_DONE));
                data.statusTagsProperty().set(newValue);
                if (newValue.equals(ActionPlanData.STATUS_DONE)) {
                    data.doneDateProperty().set(new DateTime());
                }
            }
        });

        f_mediaTags.getSelectionModel().select(data.mediaTagsProperty().getValue());
        f_mediaTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                data.mediaTagsProperty().set(newValue);
            }
        });


        for (String s : data.fieldTagsProperty().getValue().split(";")) {
            f_fieldTags.getCheckModel().check(s);
        }
        f_fieldTags.checkModelProperty().addListener((observable, oldValue, newValue) -> {
            data.fieldTagsProperty().set(ActionPlanData.listToString(f_fieldTags.getCheckModel().getCheckedItems()));
        });

        DateTime start = data.createDateProperty().get();
        f_CreateDate.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        f_CreateDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });

        data.doneDateProperty().addListener((observable, oldValue, newValue) -> {
            f_doneDate.setValue(LocalDate.of(newValue.getYear(), newValue.getMonthOfYear(), newValue.getDayOfMonth()));
        });

        f_doneDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });

        try {
            f_Enpi.valueProperty().set(data.getObject().getDataSource().getObject(new Long(data.enpilinksProperty().get())));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        f_Enpi.valueProperty().addListener(new ChangeListener<JEVisObject>() {
            @Override
            public void changed(ObservableValue<? extends JEVisObject> observable, JEVisObject oldValue, JEVisObject newValue) {
                data.enpilinksProperty().set(newValue.getID().toString());
                updateEnpi();
            }
        });

        if (data.doneDateProperty().getValue() != null) {
            DateTime end = data.doneDateProperty().get();
            f_doneDate.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
        }


        DateTime plan = data.plannedDateProperty().get();
        f_plannedDate.valueProperty().setValue(LocalDate.of(plan.getYear(), plan.getMonthOfYear(), plan.getDayOfMonth()));
        f_plannedDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });

        f_ActionNr.setText(data.nrProperty().get() + "");

        f_IsNeedAdditionalMeters.selectedProperty().bindBidirectional(data.isNeedAdditionalMetersProperty());
        f_isNeedAdditionalAction.selectedProperty().bindBidirectional((data.isNeedAdditionalActionProperty()));
        f_IsAffectsOtherProcess.selectedProperty().bindBidirectional((data.isAffectsOtherProcessProperty()));
        f_isNeedCorrection.selectedProperty().bindBidirectional((data.isNeedCorrectionProperty()));
        f_isNeedOther.selectedProperty().bindBidirectional((data.isNeedOtherProperty()));
        f_isTargetReached.selectedProperty().bindBidirectional((data.isTargetReachedProperty()));
        f_IsConsumptionDocumented.selectedProperty().bindBidirectional((data.isConsumptionDocumentedProperty()));

        f_CreateDate.setOnAction(event -> {
            //data.createDateProperty().set(fCreateDate.getText());
        });

        f_ActionNr.setOnAction(event -> {
            // data.actionNrProperty().set(Integer.parseInt(fActionNr.getText()));
        });

        f_consumptionAfter.getUnitField().textProperty().bindBidirectional(data.consumptionUnit);
        f_consumptionBefore.getUnitField().textProperty().bindBidirectional(data.consumptionUnit);
        f_consumptionDiff.getUnitField().textProperty().bindBidirectional(data.consumptionUnit);

        updateEnpi();
    }


}
