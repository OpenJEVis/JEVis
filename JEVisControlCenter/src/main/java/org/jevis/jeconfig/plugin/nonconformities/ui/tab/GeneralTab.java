package org.jevis.jeconfig.plugin.nonconformities.ui.tab;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.NotificationPane;
import org.jevis.jeconfig.plugin.nonconformities.data.Nonconformities;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.joda.time.*;

import java.time.LocalDate;

public class GeneralTab extends Tab {
    private final JFXDatePicker f_plannedDate = new JFXDatePicker();
    private final JFXDatePicker f_doneDate = new JFXDatePicker();

    private final JFXDatePicker f_createDate = new JFXDatePicker();

    private JFXComboBox<String> f_mediaTags;

    private TextArea f_Description = new TextArea();
    private TextArea f_Cause = new TextArea();
    private JFXDatePicker f_CreateDate = new JFXDatePicker();
    private JFXTextField f_ActionNr = new JFXTextField();
    private JFXTextField f_Title = new JFXTextField();
    private JFXTextField f_Creator = new JFXTextField();

    private JFXTextField f_Attachment = new JFXTextField();

    private JFXTextField f_Responsible = new JFXTextField();

    private TextArea f_ImmediateMeasures = new TextArea();

    private TextArea f_CorrectiveActions = new TextArea();

    private Label l_Description = new Label();
    private Label l_ActionNr = new Label();
    private Label l_Responsible = new Label();
    private Label l_NoteBewertet = new Label();
    private Label l_Attachment = new Label();
    private Label l_Title = new Label();
    private Label l_NoteEnergiefluss = new Label();
    private Label l_doneDate = new Label();
    private Label l_plannedDate = new Label();
    private Label l_Cause = new Label();
    private Label l_ImmediateMeasures = new Label();
    private Label l_CorrectiveActions = new Label();
    private Label l_Creator = new Label();
    private Label l_CreateDate = new Label();
    private Label l_Medium = new Label();
    private Label l_mediaTags = new Label();

    public GeneralTab(String s) {
        super(s);
    }

//    public GeneralTab(String s, Node node) {
//        super(s, node);
//    }

    @Override
    public void initTab(NonconformityData nonconformityData) {

        GridPane gridPane = new GridPane();
        notificationPane.setContent(gridPane);
        gridPane.setPadding(new Insets(20));

        Nonconformities nonconformities = nonconformityData.getNonconformities();


        //ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(10);
        gridPane.setHgap(15);
        //gridPane.gridLinesVisibleProperty().set(true);




        Region col3Spacer = new Region();

        col3Spacer.setMinWidth(25);




        add(gridPane, 1, 1, 1, 1, Priority.NEVER, l_ActionNr);
        add(gridPane, 1, 2, 1, 1, Priority.NEVER, l_Responsible);
        add(gridPane,1 , 3, 1, 1, Priority.NEVER, l_plannedDate);
        add(gridPane,1,4,1,1,Priority.NEVER,l_CreateDate);


        add(gridPane, 2, 1, 1, 1, Priority.SOMETIMES, f_ActionNr);
        add(gridPane, 2, 2, 1, 1, Priority.SOMETIMES, f_Responsible);
        add(gridPane, 2, 3, 1, 1, Priority.SOMETIMES, f_plannedDate);
        add(gridPane, 2, 4, 1, 1, Priority.SOMETIMES, f_createDate);


        add(gridPane, 3, 1, 1, 1, Priority.NEVER, l_Title);
        add(gridPane, 3, 2, 1, 1, Priority.NEVER, l_Creator);
        add(gridPane, 3, 3, 1, 1, Priority.NEVER, l_doneDate);
        add(gridPane, 3, 4, 1, 1, Priority.NEVER, l_mediaTags);

        add(gridPane, 4, 1, 1, 1, Priority.NEVER, f_Title);
        add(gridPane, 4, 2, 1, 1, Priority.NEVER, f_Creator);
        add(gridPane, 4, 3, 1, 1, Priority.SOMETIMES, f_doneDate);
        add(gridPane, 4, 4, 1, 1, Priority.SOMETIMES, f_mediaTags);

        add(gridPane, 1, 5, 2, 1, Priority.SOMETIMES, l_Description);
        add(gridPane, 1, 6, 2, 1, Priority.SOMETIMES, f_Description);

        add(gridPane,3,5,2,1,Priority.SOMETIMES,l_Cause);
        add(gridPane,3,6,2,1,Priority.SOMETIMES,f_Cause);

        add(gridPane, 1, 7, 2, 1, Priority.SOMETIMES, l_ImmediateMeasures);
        add(gridPane, 1, 8, 2, 1, Priority.SOMETIMES, f_ImmediateMeasures);

        add(gridPane, 3, 7, 2, 1, Priority.SOMETIMES, l_CorrectiveActions);
        add(gridPane, 3, 8, 2, 1, Priority.SOMETIMES, f_CorrectiveActions);



        //gridPane.add(col3Spacer, 3, 1);


        l_Attachment.setVisible(false);
        f_Attachment.setVisible(false);

        int textFieldHeight = 200;
        int textFieldWeight = 400;

        f_Description.setMaxHeight(textFieldHeight);
        f_Description.setPrefWidth(textFieldWeight);

        f_Cause.setMaxHeight(textFieldHeight);
        f_Cause.setPrefWidth(textFieldWeight);


        l_Description.setPadding(new Insets(15, 0, 0, 0));
        l_NoteBewertet.setPadding(new Insets(15, 0, 0, 0));
        l_NoteEnergiefluss.setPadding(new Insets(15, 0, 0, 0));



        f_ActionNr.setEditable(false);


        //scrollPane.setContent(gridPane);
        this.setContent(notificationPane);








    }

    @Override
    public void updateView(NonconformityData data) {

        NonconformityData fake = new NonconformityData();
        f_Description.textProperty().bindBidirectional(data.descriptionProperty());
        f_Title.textProperty().bindBidirectional(data.titleProperty());
        f_Cause.textProperty().bindBidirectional(data.causeProperty());
        f_Responsible.textProperty().bindBidirectional(data.responsiblePersonProperty());
        f_CorrectiveActions.textProperty().bindBidirectional(data.correctiveActionsProperty());
        f_ImmediateMeasures.textProperty().bindBidirectional(data.immediateMeasuresProperty());
        f_Creator.textProperty().bindBidirectional(data.creatorProperty());
        f_mediaTags = new JFXComboBox<>(data.getNonconformities().getMediumTags());
        f_mediaTags.valueProperty().bindBidirectional(data.mediumProperty());


        f_Title.setMaxWidth(200);
        f_ActionNr.setMaxWidth(200);
        f_plannedDate.setMaxWidth(200);
        f_doneDate.setMaxWidth(200);
        f_createDate.setMaxWidth(200);
        f_Responsible.setMaxWidth(200);
        f_Creator.setMaxWidth(200);
        f_mediaTags.setMaxWidth(200);


        l_CreateDate.setText(fake.createDateProperty().getName());
        l_doneDate.setText(fake.doneDateProperty().getName());
        l_mediaTags.setText(fake.mediumProperty().getName());
        l_plannedDate.setText(fake.plannedDateProperty().getName());
        l_Title.setText(fake.titleProperty().getName());
        l_Description.setText(fake.descriptionProperty().getName());
        l_Cause.setText(fake.causeProperty().getName());
        l_ActionNr.setText(fake.nrProperty().getName());
        l_Attachment.setText(fake.attachmentProperty().getName());
        l_Responsible.setText(fake.responsiblePersonProperty().getName());
        l_ImmediateMeasures.setText(fake.immediateMeasuresProperty().getName());
        l_CorrectiveActions.setText(fake.correctiveActionsProperty().getName());
        l_Creator.setText(fake.creatorProperty().getName());
        l_Title.setWrapText(true);
        if (data.plannedDateProperty().isNotNull().get()) {
            f_plannedDate.setValue(LocalDate.of(data.getPlannedDate().getYear(),data.getPlannedDate().getMonthOfYear(),data.getPlannedDate().getDayOfMonth()));
        }
        if (data.doneDateProperty().isNotNull().get()) {
            f_doneDate.setValue(LocalDate.of(data.getDoneDate().getYear(),data.getDoneDate().getMonthOfYear(),data.getDoneDate().getDayOfMonth()));
        }

        if (data.createDateProperty().isNotNull().get()) {
            f_createDate.setValue(LocalDate.of(data.getCreateDate().getYear(),data.getCreateDate().getMonthOfYear(),data.getCreateDate().getDayOfMonth()));
        }


        f_plannedDate.valueProperty().addListener((observableValue, localDate, newValue) -> {
            data.plannedDateProperty().set(new DateTime(newValue.getYear(),newValue.getMonthValue(),newValue.getDayOfMonth(),0,0));
        });


        f_doneDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

        });

        f_createDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.createDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

        });

        f_ActionNr.setText(data.getPrefix() + data.nrProperty().get());

    }
}
