package org.jevis.jeconfig.plugin.nonconformities.ui.tab;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesController;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityPlan;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralTab extends Tab {

    private static final Logger logger = LogManager.getLogger(GeneralTab.class);
    private final JFXDatePicker f_deadlineDate = new JFXDatePicker();
    private final JFXDatePicker f_doneDate = new JFXDatePicker();

    private final JFXDatePicker f_createDate = new JFXDatePicker();

    private JFXComboBox<String> f_mediaTags;
    private JFXComboBox<String> f_SEU;

    private TextArea f_Description = new TextArea();
    private TextArea f_Cause = new TextArea();
    private JFXDatePicker f_CreateDate = new JFXDatePicker();
    private JFXTextField f_Nr = new JFXTextField();
    private JFXTextField f_Title = new JFXTextField();
    private JFXTextField f_action = new JFXTextField();
    private JFXTextField f_Creator = new JFXTextField();

    private JFXTextField f_Attachment = new JFXTextField();

    private JFXTextField f_Responsible = new JFXTextField();

    private TextArea f_ImmediateMeasures = new TextArea();

    private TextArea f_CorrectiveActions = new TextArea();

    private CheckComboBox<String> f_fieldTags;

    private Label l_Description = new Label();
    private Label l_SEU = new Label();
    private Label l_Nr = new Label();
    private Label l_Responsible = new Label();
    private Label l_NoteBewertet = new Label();
    private Label l_Attachment = new Label();
    private Label l_Title = new Label();
    private Label l_NoteEnergiefluss = new Label();
    private Label l_doneDate = new Label();
    private Label l_deadLine = new Label();
    private Label l_Cause = new Label();
    private Label l_ImmediateMeasures = new Label();
    private Label l_CorrectiveActions = new Label();
    private Label l_Creator = new Label();
    private Label l_CreateDate = new Label();
    private Label l_Medium = new Label();
    private Label l_mediaTags = new Label();

    private Label l_fieldTags = new Label();

    private Label l_action = new Label();

    public GeneralTab(String s) {
        super(s);
    }

//    public GeneralTab(String s, Node node) {
//        super(s, node);
//    }

    @Override
    public void initTab(NonconformityData nonconformityData) {

        addTabEvent(f_ImmediateMeasures);
        f_ImmediateMeasures.setWrapText(true);

        addTabEvent(f_Cause);
        f_Cause.setWrapText(true);

        addTabEvent(f_Description);
        f_Description.setWrapText(true);

        addTabEvent(f_CorrectiveActions);
        f_CorrectiveActions.setWrapText(true);


        GridPane gridPane = new GridPane();
        notificationPane.setContent(gridPane);
        gridPane.setPadding(new Insets(20));

        NonconformityPlan nonconformityPlan = nonconformityData.getNonconformityPlan();


        //ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(10);
        gridPane.setHgap(15);
        //gridPane.gridLinesVisibleProperty().set(true);




        Region col3Spacer = new Region();

        col3Spacer.setMinWidth(25);




        add(gridPane, 1, 1, 1, 1, Priority.NEVER, l_Nr);
        add(gridPane, 1, 2, 1, 1, Priority.NEVER, l_Title);
        add(gridPane, 1, 3, 1, 1, Priority.NEVER, l_Responsible);
        add(gridPane, 1, 4, 1, 1, Priority.NEVER, l_CreateDate);
        add(gridPane, 1, 5, 1, 1, Priority.NEVER, l_deadLine);
        add(gridPane, 1, 6, 1, 1, Priority.NEVER, l_doneDate);




        add(gridPane, 2, 1, 1, 1, Priority.SOMETIMES, f_Nr);
        add(gridPane, 2, 2, 1, 1, Priority.SOMETIMES, f_Title);
        add(gridPane, 2, 3, 1, 1, Priority.SOMETIMES, f_Responsible);
        add(gridPane, 2, 4, 1, 1, Priority.SOMETIMES, f_createDate);
        add(gridPane, 2, 5, 1, 1, Priority.SOMETIMES, f_deadlineDate);
        add(gridPane, 2, 6, 1, 1, Priority.SOMETIMES, f_doneDate);


        add(gridPane, 3, 1, 1, 1, Priority.NEVER, l_mediaTags);
        add(gridPane, 3, 2, 1, 1, Priority.NEVER, l_fieldTags);
        add(gridPane, 3, 3, 1, 1, Priority.NEVER, l_SEU);
        add(gridPane, 3, 4, 1, 1, Priority.NEVER, l_Creator);
        add(gridPane, 3, 5, 1, 1, Priority.SOMETIMES, l_action);

        add(gridPane, 4, 1, 1, 1, Priority.NEVER, f_mediaTags);
        add(gridPane, 4, 2, 1, 1, Priority.NEVER, f_fieldTags);
        add(gridPane, 4, 3, 1, 1, Priority.NEVER, f_SEU);
        add(gridPane, 4, 4, 1, 1, Priority.NEVER, f_Creator);
        add(gridPane, 4, 5, 1, 1, Priority.SOMETIMES, f_action);


        add(gridPane, 1, 7, 2, 1, Priority.SOMETIMES, l_Description);
        add(gridPane, 1, 8, 2, 1, Priority.SOMETIMES, f_Description);

        add(gridPane,3,7,2,1,Priority.SOMETIMES,l_Cause);
        add(gridPane,3,8,2,1,Priority.SOMETIMES,f_Cause);

        add(gridPane, 1, 9, 2, 1, Priority.SOMETIMES, l_ImmediateMeasures);
        add(gridPane, 1, 10, 2, 1, Priority.SOMETIMES, f_ImmediateMeasures);

        add(gridPane, 3, 9, 2, 1, Priority.SOMETIMES, l_CorrectiveActions);
        add(gridPane, 3, 10, 2, 1, Priority.SOMETIMES, f_CorrectiveActions);



        //gridPane.add(col3Spacer, 3, 1);


        l_Attachment.setVisible(false);
        f_Attachment.setVisible(false);

        int textFieldHeight = 200;
        int textFieldWeight = 400;

        f_Description.setMaxHeight(textFieldHeight);
        f_Description.setPrefWidth(textFieldWeight);

        f_Cause.setMaxHeight(textFieldHeight);
        f_Cause.setPrefWidth(textFieldWeight);

        l_Cause.setPadding(new Insets(15, 0, 0, 0));
        l_Description.setPadding(new Insets(15, 0, 0, 0));
        l_NoteBewertet.setPadding(new Insets(15, 0, 0, 0));
        l_NoteEnergiefluss.setPadding(new Insets(15, 0, 0, 0));



        f_Nr.setEditable(JEConfig.getExpert());


        //scrollPane.setContent(gridPane);
        this.setContent(notificationPane);








    }

    private void addTabEvent(TextArea f_Cause) {
        f_Cause.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.TAB) {
                    Node node = (Node) keyEvent.getSource();
                    if (node instanceof TextArea) {
                        TextAreaSkin skin = (TextAreaSkin) ((TextArea) node).getSkin();
                        skin.getBehavior().traverseNext();
                    }
                }
            }
        });
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
        f_mediaTags = new JFXComboBox<>(data.getNonconformityPlan().getMediumTags());
        f_fieldTags = new CheckComboBox<>(data.getNonconformityPlan().getFieldsTags());
        f_action.textProperty().bindBidirectional(data.actionProperty());
        f_SEU = new JFXComboBox<>(data.getNonconformityPlan().getSignificantEnergyUseTags());
        f_SEU.valueProperty().bindBidirectional(data.seuProperty());

        f_mediaTags.valueProperty().bindBidirectional(data.mediumProperty());

        data.getFieldTags().forEach(s -> {
            f_fieldTags.getCheckModel().check(s);
        });
        f_fieldTags.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                while (change.next()) {
                    data.getFieldTags().clear();
                    data.getFieldTags().addAll(change.getList());
                }
            }
        });

        data.fieldTagsProperty().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                while (change.next()) {
                    System.out.println(change.getList());
                }
            }
        });

        f_Nr.textProperty().addListener((observableValue, s, t1) -> {
            try {
                Pattern pattern = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(t1);
                while (matcher.find()) {
                    int i = Integer.valueOf(matcher.group());
                    data.nrProperty().set(i);
                    break;
                }
            } catch (NumberFormatException numberFormatException) {
                logger.error(numberFormatException);
            }
        });


        f_Title.setMaxWidth(200);
        f_Nr.setMaxWidth(200);
        f_deadlineDate.setMaxWidth(200);
        f_doneDate.setMaxWidth(200);
        f_createDate.setMaxWidth(200);
        f_Responsible.setMaxWidth(200);
        f_Creator.setMaxWidth(200);
        f_mediaTags.setMaxWidth(200);
        f_fieldTags.setMaxWidth(200);
        f_fieldTags.setMinWidth(200);
        f_action.setMaxWidth(200);
        f_SEU.minWidth(200);
        f_SEU.setMaxWidth(200);

        l_SEU.setText(fake.seuProperty().getName());
        l_action.setText(fake.actionProperty().getName());
        l_fieldTags.setText(fake.fieldTagsProperty().getName());
        l_CreateDate.setText(fake.createDateProperty().getName());
        l_doneDate.setText(fake.doneDateProperty().getName());
        l_mediaTags.setText(fake.mediumProperty().getName());
        l_deadLine.setText(fake.deadLineProperty().getName());
        l_Title.setText(fake.titleProperty().getName());
        l_Description.setText(fake.descriptionProperty().getName());
        l_Cause.setText(fake.causeProperty().getName());
        l_Nr.setText(fake.nrProperty().getName());
        l_Attachment.setText(fake.attachmentProperty().getName());
        l_Responsible.setText(fake.responsiblePersonProperty().getName());
        l_ImmediateMeasures.setText(fake.immediateMeasuresProperty().getName());
        l_CorrectiveActions.setText(fake.correctiveActionsProperty().getName());
        l_Creator.setText(fake.creatorProperty().getName());
        l_Title.setWrapText(true);


        data.fieldTagsProperty().get().stream();
        if (data.deadLineProperty().isNotNull().get()) {
            f_deadlineDate.setValue(LocalDate.of(data.getDeadLine().getYear(),data.getDeadLine().getMonthOfYear(),data.getDeadLine().getDayOfMonth()));
        }
        if (data.doneDateProperty().isNotNull().get()) {
            f_doneDate.setValue(LocalDate.of(data.getDoneDate().getYear(),data.getDoneDate().getMonthOfYear(),data.getDoneDate().getDayOfMonth()));
        }

        if (data.createDateProperty().isNotNull().get()) {
            f_createDate.setValue(LocalDate.of(data.getCreateDate().getYear(),data.getCreateDate().getMonthOfYear(),data.getCreateDate().getDayOfMonth()));
        }


        f_deadlineDate.valueProperty().addListener((observableValue, localDate, newValue) -> {
            data.deadLineProperty().set(new DateTime(newValue.getYear(),newValue.getMonthValue(),newValue.getDayOfMonth(),0,0));
        });


        f_doneDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

        });

        f_createDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.createDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

        });

        f_Nr.setText(data.getPrefix() + data.nrProperty().get());

    }


    public JFXTextField getF_action() {
        return f_action;
    }

    public TextArea getF_ImmediateMeasures() {
        return f_ImmediateMeasures;
    }


    public JFXDatePicker getF_doneDate() {
        return f_doneDate;
    }
}
