package org.jevis.jeconfig.plugin.action.ui.tab;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.FreeObject;
import org.jevis.jeconfig.plugin.action.ui.DoubleConverter;
import org.joda.time.DateTime;

import java.time.LocalDate;

public class GeneralTab extends Tab {

    private final JFXDatePicker f_plannedDate = new JFXDatePicker();
    private final JFXDatePicker f_doneDate = new JFXDatePicker();
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
    private JFXTextField f_savingYear = new JFXTextField();
    private JFXTextField f_Investment = new JFXTextField();
    private JFXTextField f_ActionNr = new JFXTextField();
    private JFXTextField f_Title = new JFXTextField();
    private JFXTextField f_Responsible = new JFXTextField();
    private TextArea f_Description = new TextArea();
    private TextArea f_NoteBewertet = new TextArea();
    private JFXComboBox<String> f_statusTags;
    private CheckComboBox<String> f_fieldTags;
    private JFXComboBox<String> f_mediaTags;
    private JFXComboBox<JEVisObject> f_Enpi;
    private JFXTextField f_Attachment = new JFXTextField();
    private TextArea f_Note = new TextArea();
    private TextArea f_NoteEnergiefluss = new TextArea();
    private ActionData names = new ActionData();

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
        //gridPane.gridLinesVisibleProperty().set(true);


        col3Spacer.setMinWidth(25);

        // f_savingYear.textProperty().bindBidirectional(data.DELETEsavingyearProperty());
        f_ActionNr.setText(data.nrProperty().get() + "");
        // f_Investment.textProperty().bindBidirectional(data.investmentProperty());

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

        f_Enpi = new JFXComboBox(data.getActionPlan().getEnpis());
        f_Enpi.setCellFactory(enpiCellFactory);
        f_Enpi.setButtonCell(enpiCellFactory.call(null));
        f_statusTags = new JFXComboBox<>(actionPlan.getStatustags());
        f_fieldTags = new CheckComboBox<>(actionPlan.getFieldsTags());
        f_mediaTags = new JFXComboBox<>(actionPlan.getMediumTags());
        //f_mediaTags.setCellFactory();

        f_Title.widthProperty().addListener((observable, oldValue, newValue) -> {
            f_statusTags.setPrefWidth(newValue.doubleValue());
            f_fieldTags.setPrefWidth(newValue.doubleValue());
            f_mediaTags.setPrefWidth(newValue.doubleValue());
            f_Enpi.setPrefWidth(newValue.doubleValue());
        });
        f_Note.textProperty().bindBidirectional(data.noteProperty());
        f_Description.textProperty().bindBidirectional(data.desciptionProperty());
        f_Title.textProperty().bindBidirectional(data.titleProperty());
        f_NoteEnergiefluss.textProperty().bindBidirectional(data.noteEnergieflussProperty());
        f_NoteBewertet.textProperty().bindBidirectional(data.noteBewertetProperty());
        f_Responsible.textProperty().bindBidirectional(data.responsibleProperty());
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


        data.doneDateProperty().addListener((observable, oldValue, newValue) -> {
            f_doneDate.setValue(LocalDate.of(newValue.getYear(), newValue.getMonthOfYear(), newValue.getDayOfMonth()));
        });

        f_doneDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });

        try {
            JEVisObject obj = FreeObject.getInstance();
            System.out.println("data.enpilinksProperty().get(): " + data.enpiProperty().get().jevisLinkProperty().get());
            if (!data.enpiProperty().get().jevisLinkProperty().get().isEmpty() && !data.enpiProperty().get().jevisLinkProperty().get().equals(FreeObject.getInstance().getID())) {
                try {
                    obj = data.getObject().getDataSource().getObject(new Long(data.enpiProperty().get().jevisLinkProperty().get()));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            //System.out.println("Select Object; " + obj);
            // f_Enpi.valueProperty().set(obj);
            f_Enpi.getSelectionModel().select(obj);
            f_Enpi.getSelectionModel().selectLast();

            System.out.println("EnPI selected1: " + f_Enpi.getSelectionModel().getSelectedItem());
            System.out.println("EnPI selected2: " + obj);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        f_Enpi.valueProperty().addListener(new ChangeListener<JEVisObject>() {
            @Override
            public void changed(ObservableValue<? extends JEVisObject> observable, JEVisObject oldValue, JEVisObject newValue) {
                data.enpiProperty().get().jevisLinkProperty().set(newValue.getID().toString());
                /** TODO update enpi data **/
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

        Bindings.bindBidirectional(f_Investment.textProperty(), data.npv.get().investment, DoubleConverter.getInstance().getDoubleConverter());
        Bindings.bindBidirectional(f_savingYear.textProperty(), data.npv.get().einsparung, DoubleConverter.getInstance().getDoubleConverter());

        // f_savingYear.setTextFormatter(new TextFormatter(new UnitDoubleConverter()));
        JFXTextField l_savingsUnitLabel = new JFXTextField("€");
        l_savingsUnitLabel.setEditable(false);
        l_savingsUnitLabel.setPrefWidth(25);
        HBox savingsBox = new HBox(f_savingYear, l_savingsUnitLabel);
        HBox.setHgrow(f_savingYear, Priority.ALWAYS);
        f_savingYear.setAlignment(Pos.BASELINE_RIGHT);


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

        setContent(scrollPane);
    }

    private void add(GridPane pane, int column, int row, int colspan, int rowspan, Priority priority, Node node) {
        pane.add(node, column, row, colspan, rowspan);
        GridPane.setHgrow(node, priority);
    }
}
