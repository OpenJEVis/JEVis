package org.jevis.jecc.plugin.action.ui.tab;


import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.action.data.ActionData;
import org.jevis.jecc.plugin.action.data.ActionPlanData;
import org.jevis.jecc.plugin.action.ui.CheckBoxData;
import org.jevis.jecc.plugin.action.ui.NumerFormating;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.Optional;

public class GeneralTab extends Tab {
    private static final Logger logger = LogManager.getLogger(GeneralTab.class);
    private final DatePicker f_plannedDate = new DatePicker();
    private final DatePicker f_doneDate = new DatePicker();
    //private ActionData names = new ActionData();
    private final Label l_Note = new Label();
    private final Label l_Description = new Label();
    private final Label l_ActionNr = new Label();
    private final Label l_Investment = new Label(I18n.getInstance().getString("actionform.editor.tab.general.investment"));
    private final Label l_changeKost = new Label(I18n.getInstance().getString("actionform.editor.tab.general.yearsaving"));
    private final Label l_Responsible = new Label();
    private final Label l_NoteBewertet = new Label();
    private final Label l_Attachment = new Label();
    private final Label l_Title = new Label();
    private final Label l_NoteEnergiefluss = new Label();
    private final Label l_doneDate = new Label();
    private final Label l_plannedDate = new Label();
    private final Label l_statusTags = new Label();
    private final Label l_fieldTags = new Label();
    private final Region col3Spacer = new Region();
    private final TextField f_savingYear = new TextField();
    private final TextField f_Investment = new TextField();
    private final TextField f_ActionNr = new TextField();
    private final TextField f_Title = new TextField();
    private final TextField f_Responsible = new TextField();
    private final TextArea f_Description = new TextArea();
    private final TextArea f_NoteBewertet = new TextArea();
    private final ComboBox<String> f_statusTags;
    private final CheckComboBox<String> f_fieldTags;
    //private JFXCheckComboBox f_fieldTags2;
    private final TextField f_Attachment = new TextField();
    private final TextArea f_Note = new TextArea();
    private final TextArea f_NoteEnergiefluss = new TextArea();
    private final Label l_seu = new Label(I18n.getInstance().getString("actionform.editor.tab.general.seu"));
    private final Label l_FromUser = new Label(I18n.getInstance().getString("plugin.action.fromuser"));
    private final TextField f_FromUser = new TextField();
    private final Label l_CreateDate = new Label(I18n.getInstance().getString("plugin.action.created"));  //
    private final DatePicker f_CreateDate = new DatePicker();
    private final Label l_distributor = new Label(I18n.getInstance().getString("plugin.action.distributor"));
    private final TextField f_distributor = new TextField();
    private ComboBox<String> f_sueTags = new ComboBox<>();

    {
        f_NoteBewertet.setWrapText(true);
        f_Description.setWrapText(true);
        f_Note.setWrapText(true);
        f_NoteEnergiefluss.setWrapText(true);
    }

    public GeneralTab(ActionData data) {
        super(I18n.getInstance().getString("actionform.editor.tab.general"));
        ActionPlanData actionPlan = data.getActionPlan();
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(10);
        gridPane.setHgap(15);

        col3Spacer.setMinWidth(25);

        /* Readable if the workaround is not needed */
        f_ActionNr.setText(actionPlan.noPrefixProperty().get() + data.noProperty().get());
        f_ActionNr.setEditable(false);
        f_ActionNr.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.isAltDown() && mouseEvent.isControlDown()) {
                TextInputDialog textInputDialog = new TextInputDialog(data.noProperty().get() + "");
                textInputDialog.setHeaderText("Set Nr");
                textInputDialog.setContentText("Set Nr");
                Optional<String> value = textInputDialog.showAndWait();
                try {
                    data.no.set(Integer.parseInt(value.get()));
                    f_ActionNr.setText(actionPlan.noPrefixProperty().get() + data.noProperty().get());
                } catch (Exception ex) {

                }
            }
        });
        /* allow editing NR */
        // StringConverter sdfs = new IntegerStringConverter();
        //Bindings.bindBidirectional(f_ActionNr.textProperty(), data.nr, sdfs);


        f_statusTags = new ComboBox<>(actionPlan.getStatustags());
        f_fieldTags = new CheckComboBox<>(actionPlan.getFieldsTags());
        f_sueTags = new ComboBox<>(actionPlan.significantEnergyUseTags());
        //f_mediaTags.setCellFactory();

        ObservableList<CheckBoxData> f_fieldTags2Data = FXCollections.observableArrayList();
        data.getActionPlan().getFieldsTags().forEach(s -> {
            f_fieldTags2Data.add(new CheckBoxData(s, false));
        });

        f_Title.widthProperty().addListener((observable, oldValue, newValue) -> {
            f_statusTags.setPrefWidth(newValue.doubleValue());
            f_fieldTags.setPrefWidth(newValue.doubleValue());
            //  f_fieldTags2.setPrefWidth(newValue.doubleValue());
            f_sueTags.setPrefWidth(newValue.doubleValue());
            //f_mediaTags.setPrefWidth(newValue.doubleValue());
            //f_Enpi.setPrefWidth(newValue.doubleValue());
        });

        f_Note.textProperty().bindBidirectional(data.noteProperty());
        f_Description.textProperty().bindBidirectional(data.descriptionProperty());
        f_Title.textProperty().bindBidirectional(data.titleProperty());
        f_NoteEnergiefluss.textProperty().bindBidirectional(data.noteEnergieflussProperty());
        f_NoteBewertet.textProperty().bindBidirectional(data.noteBewertetProperty());
        f_Responsible.textProperty().bindBidirectional(data.responsibleProperty());
        f_statusTags.getSelectionModel().select(data.statusTagsProperty().getValue());
        f_statusTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //System.out.println("Status change: " + newValue + " = " + ActionPlanData.STATUS_DONE + " = " + newValue.equals(ActionPlanData.STATUS_DONE));
                data.statusTagsProperty().set(newValue);
                if (newValue.equals(ActionPlanData.STATUS_DONE)) {
                    data.doneDateProperty().set(new DateTime());
                }
            }
        });


        for (String s : data.fieldTagsProperty().getValue().split(";")) {
            f_fieldTags.getCheckModel().check(s);
        }
        f_fieldTags.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                while (c.next()) {
                    //do something with changes here
                    if (c.wasAdded() || c.wasRemoved()) {
                        data.fieldTagsProperty().set(ActionPlanData.listToString(f_fieldTags.getCheckModel().getCheckedItems()));
                    }
                }

            }
        });


        f_sueTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                System.out.println("Status change: " + newValue + " = " + ActionPlanData.STATUS_DONE + " = " + newValue.equals(ActionPlanData.STATUS_DONE));
                data.seuTagsProperty().set(newValue);
            }
        });


        if (data.doneDateProperty().getValue() != null) {
            DateTime end = data.doneDateProperty().get();
            f_doneDate.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
        }

        f_doneDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                data.doneDate.setValue(null);
            } else {
                data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
            }

        });


        DateTime planDate = data.plannedDateProperty().get();
        if (planDate != null) {
            f_plannedDate.valueProperty().setValue(LocalDate.of(planDate.getYear(), planDate.getMonthOfYear(), planDate.getDayOfMonth()));
        }
        f_plannedDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                data.plannedDateProperty().set(null);
            } else {
                data.plannedDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
            }
        });


        f_Investment.setTextFormatter(new TextFormatter<>(NumerFormating.getInstance().getDoubleConverter()));
        Bindings.bindBidirectional(f_Investment.textProperty(), data.npv.get().investment, NumerFormating.getInstance().getDoubleConverter());
        f_savingYear.setTextFormatter(new TextFormatter<>(NumerFormating.getInstance().getDoubleConverter()));
        Bindings.bindBidirectional(f_savingYear.textProperty(), data.npv.get().einsparung, NumerFormating.getInstance().getDoubleConverter());

        f_savingYear.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                f_savingYear.textProperty().set(NumerFormating.getInstance().getDoubleFormate().format(data.npv.get().einsparung.get()));
            }
        });

        f_Description.setWrapText(true);
        f_NoteBewertet.setWrapText(true);
        f_Note.setWrapText(true);
        f_NoteEnergiefluss.setWrapText(true);


        logger.debug("Investment: " + data.npv.get().investment.get());
        logger.debug("InvestText: " + f_Investment.textProperty().get());


        // f_savingYear.setTextFormatter(new TextFormatter(new UnitDoubleConverter()));
        TextField l_savingsUnitLabel = new TextField("€");
        l_savingsUnitLabel.setEditable(false);
        l_savingsUnitLabel.setPrefWidth(25);
        HBox savingsBox = new HBox(f_savingYear, l_savingsUnitLabel);
        HBox.setHgrow(f_savingYear, Priority.ALWAYS);
        f_savingYear.setAlignment(Pos.BASELINE_RIGHT);


        TextField l_investmentUnitLabel = new TextField("€");
        l_investmentUnitLabel.setEditable(false);
        l_investmentUnitLabel.setPrefWidth(25);
        HBox investBox = new HBox(f_Investment, l_investmentUnitLabel);
        HBox.setHgrow(f_Investment, Priority.ALWAYS);
        f_Investment.setAlignment(Pos.BASELINE_RIGHT);


        f_distributor.textProperty().bindBidirectional(data.distributorProperty());
        f_FromUser.textProperty().bindBidirectional(data.fromUserProperty());

        DateTime start = data.createDateProperty().get();
        if (start != null) {
            f_CreateDate.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        }
        f_CreateDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug("newValue: " + newValue);
            if (newValue == null) {
                data.createDateProperty().set(null);
            } else {
                data.createDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
            }

        });

        add(gridPane, 1, 1, 1, 1, Priority.NEVER, l_ActionNr);
        add(gridPane, 1, 2, 1, 1, Priority.NEVER, l_Title);
        add(gridPane, 1, 3, 1, 1, Priority.NEVER, l_Responsible);
        add(gridPane, 1, 4, 1, 1, Priority.NEVER, l_CreateDate);
        add(gridPane, 1, 5, 1, 1, Priority.NEVER, l_plannedDate);
        add(gridPane, 1, 6, 1, 1, Priority.NEVER, l_doneDate);
        // add(gridPane, 1, 7, 1, 1, Priority.NEVER, l_distributor);

        add(gridPane, 2, 1, 1, 1, Priority.SOMETIMES, f_ActionNr);
        add(gridPane, 2, 2, 1, 1, Priority.SOMETIMES, f_Title);
        add(gridPane, 2, 3, 1, 1, Priority.SOMETIMES, f_Responsible);
        add(gridPane, 2, 4, 1, 1, Priority.SOMETIMES, f_CreateDate);
        add(gridPane, 2, 5, 1, 1, Priority.SOMETIMES, f_plannedDate);
        add(gridPane, 2, 6, 1, 1, Priority.SOMETIMES, f_doneDate);
        //add(gridPane, 2, 7, 1, 1, Priority.NEVER, f_distributor);

        add(gridPane, 1, 8, 2, 1, Priority.SOMETIMES, l_Description);
        add(gridPane, 1, 9, 2, 1, Priority.SOMETIMES, f_Description);
        add(gridPane, 1, 10, 2, 1, Priority.SOMETIMES, l_NoteBewertet);
        add(gridPane, 1, 11, 2, 1, Priority.SOMETIMES, f_NoteBewertet);

        //Spacer column
        gridPane.add(col3Spacer, 3, 1);

        add(gridPane, 4, 1, 1, 1, Priority.SOMETIMES, l_statusTags);
        add(gridPane, 4, 2, 1, 1, Priority.SOMETIMES, l_fieldTags);
        add(gridPane, 4, 3, 1, 1, Priority.SOMETIMES, l_seu);
        add(gridPane, 4, 4, 1, 1, Priority.SOMETIMES, l_FromUser);
        add(gridPane, 4, 5, 1, 1, Priority.SOMETIMES, l_changeKost);
        add(gridPane, 4, 6, 1, 1, Priority.SOMETIMES, l_Investment);
        add(gridPane, 4, 7, 1, 1, Priority.SOMETIMES, l_Attachment);

        add(gridPane, 5, 1, 1, 1, Priority.SOMETIMES, f_statusTags);
        add(gridPane, 5, 2, 1, 1, Priority.SOMETIMES, f_fieldTags);//f_fieldTags2);
        add(gridPane, 5, 3, 1, 1, Priority.SOMETIMES, f_sueTags);
        add(gridPane, 5, 4, 1, 1, Priority.SOMETIMES, f_FromUser);
        add(gridPane, 5, 5, 1, 1, Priority.SOMETIMES, savingsBox);//f_Investment
        add(gridPane, 5, 6, 1, 1, Priority.SOMETIMES, investBox);//f_savings
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


        GridPane.setHgrow(f_statusTags, Priority.ALWAYS);

        scrollPane.setContent(gridPane);


        l_doneDate.setText("Abgeschlossen");
        l_plannedDate.setText(I18n.getInstance().getString("plugin.action.plandate"));
        l_Note.setText(I18n.getInstance().getString("plugin.action.note"));
        l_Description.setText(I18n.getInstance().getString("plugin.action.description"));
        l_ActionNr.setText(I18n.getInstance().getString("plugin.action.nr"));
        l_Attachment.setText("Anhang");
        l_statusTags.setText("Status");
        l_fieldTags.setText("Bereich");

        l_Responsible.setText("Verantwortlichkeit");

        l_Title.setText(I18n.getInstance().getString("plugin.action.affectedprocess"));
        l_NoteBewertet.setText(I18n.getInstance().getString("plugin.action.noteBewertet"));
        l_NoteEnergiefluss.setText(I18n.getInstance().getString("plugin.action.measureDescription"));

        l_Title.setWrapText(true);
        l_NoteBewertet.setWrapText(true);
        l_NoteEnergiefluss.setWrapText(true);

        setContent(scrollPane);

    }

    private void add(GridPane pane, int column, int row, int colspan, int rowspan, Priority priority, Node node) {
        pane.add(node, column, row, colspan, rowspan);
        GridPane.setHgrow(node, priority);
    }
}
