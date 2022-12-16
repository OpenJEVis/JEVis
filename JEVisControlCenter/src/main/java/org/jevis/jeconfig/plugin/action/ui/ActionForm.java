package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlan;
import org.jevis.jeconfig.plugin.action.data.FileData;
import org.jevis.jeconfig.tool.ScreenSize;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.List;

public class ActionForm extends Dialog {

    private final JFXDatePicker f_plannedDate = new JFXDatePicker();
    private final JFXDatePicker f_doneDate = new JFXDatePicker();
    private CheckBox f_IsNeedAdditionalMeters = new CheckBox();
    private CheckBox f_IsAffectsOtherProcess = new CheckBox();
    private CheckBox f_IsConsumptionDocumented = new CheckBox();
    private JFXTextField f_isNewEnPI = new JFXTextField();
    private CheckBox f_isNeedCorrection = new CheckBox();
    private CheckBox f_isNeedOther = new CheckBox();
    private CheckBox f_isNeedAdditionalAction = new CheckBox();
    //private CheckComboBox<String> f_statusTags;
    private ComboBox<String> f_statusTags;
    private CheckComboBox<String> f_fieldTags;
    private CheckComboBox<String> f_mediaTags;
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
    private JFXTextField f_Investment = new JFXTextField();
    private JFXTextField f_savingYear = new JFXTextField();
    private JFXTextField f_enpiAfter = new JFXTextField();
    private JFXTextField f_enpiBefore = new JFXTextField();
    private JFXTextField f_enpiChange = new JFXTextField();
    private JFXTextField f_distributor = new JFXTextField();
    private CheckBox f_isTargetReached = new CheckBox();
    private TextArea f_correctionIfNeeded = new TextArea("Korrekturmaßnahmen");
    private TextArea f_nextActionIfNeeded = new TextArea("Folgemaßnahmen");
    private TextArea f_alternativAction = new TextArea("Alternativmaßnahmen");
    ;

    public ActionForm(ActionPlan actionPlan) {
        super();
        this.initOwner(JEConfig.getStage());


        setTitle(I18n.getInstance().getString("actionform.editor.title"));
        setHeaderText(null);
        setResizable(true);
        this.getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1000));
        this.getDialogPane().setPrefHeight(ScreenSize.fitScreenHeight(950));

        widthProperty().addListener((observable, oldValue, newValue) -> tabPane.setPrefWidth(newValue.doubleValue() - 50));


        tabPane.setPrefWidth(ScreenSize.fitScreenWidth(1050));


        f_statusTags = new ComboBox<>(actionPlan.getStatustags());
        //f_statusTags = new CheckComboBox<>(actionPlan.getStatustags());
        f_fieldTags = new CheckComboBox<>(actionPlan.getFieldsTags());
        f_mediaTags = new CheckComboBox<>(actionPlan.getMediumTags());

        f_Title.widthProperty().addListener((observable, oldValue, newValue) -> {
            f_statusTags.setPrefWidth(newValue.doubleValue());
            f_fieldTags.setPrefWidth(newValue.doubleValue());
            f_mediaTags.setPrefWidth(newValue.doubleValue());
        });

        basicTab.setClosable(false);
        detailTab.setClosable(false);
        attachmentTab.setClosable(false);
        tabPane.getTabs().addAll(basicTab, detailTab, attachmentTab);
        getDialogPane().setContent(tabPane);
    }

    public void setData(ActionData data) {
        initLayout(data);
        initDetailsTab(data);
        initAttachmentTab(data);
        updateView(data);

    }

    private void initLayout(ActionData data) {
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
        col3Spacer.setMinWidth(25);

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


        add(gridPane, 1, 7, 2, 1, Priority.SOMETIMES, l_Description);
        add(gridPane, 1, 8, 2, 1, Priority.SOMETIMES, f_Description);
        add(gridPane, 1, 9, 2, 1, Priority.SOMETIMES, l_NoteBewertet);
        add(gridPane, 1, 10, 2, 1, Priority.SOMETIMES, f_NoteBewertet);

        //Spacer column
        gridPane.add(col3Spacer, 3, 1);

        add(gridPane, 4, 1, 1, 1, Priority.SOMETIMES, l_statusTags);
        add(gridPane, 4, 2, 1, 1, Priority.SOMETIMES, l_fieldTags);
        add(gridPane, 4, 3, 1, 1, Priority.SOMETIMES, l_mediaTags);
        add(gridPane, 4, 4, 1, 1, Priority.SOMETIMES, l_Investment);
        add(gridPane, 4, 5, 1, 1, Priority.SOMETIMES, l_changeKost);
        add(gridPane, 4, 6, 1, 1, Priority.SOMETIMES, l_Attachment);

        add(gridPane, 5, 1, 1, 1, Priority.SOMETIMES, f_statusTags);
        add(gridPane, 5, 2, 1, 1, Priority.SOMETIMES, f_fieldTags);
        add(gridPane, 5, 3, 1, 1, Priority.SOMETIMES, f_mediaTags);
        add(gridPane, 5, 4, 1, 1, Priority.SOMETIMES, f_Investment);
        add(gridPane, 5, 5, 1, 1, Priority.SOMETIMES, f_savingYear);
        add(gridPane, 5, 6, 1, 1, Priority.SOMETIMES, f_Attachment);

        add(gridPane, 4, 7, 2, 1, Priority.SOMETIMES, l_NoteEnergiefluss);
        add(gridPane, 4, 8, 2, 1, Priority.SOMETIMES, f_NoteEnergiefluss);
        add(gridPane, 4, 9, 2, 1, Priority.SOMETIMES, l_Note);
        add(gridPane, 4, 10, 2, 1, Priority.SOMETIMES, f_Note);

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

        l_doneDate.setText(data.doneDateProperty().getName());
        l_plannedDate.setText(data.plannedDateProperty().getName());
        l_Note.setText(data.noteProperty().getName());
        l_Description.setText(data.desciptionProperty().getName());
        l_ActionNr.setText(data.actionNrProperty().getName());
        l_Attachment.setText(data.attachmentProperty().getName());
        l_statusTags.setText(data.statusTagsProperty().getName());
        l_fieldTags.setText(data.fieldTagsProperty().getName());
        l_mediaTags.setText(data.mediaTagsProperty().getName());
        l_Responsible.setText(data.responsibleProperty().getName());

        l_Title.setText(data.noteBetroffenerProzessProperty().getName());
        l_NoteBewertet.setText(data.noteBewertetProperty().getName());
        l_NoteEnergiefluss.setText(data.noteEnergieflussProperty().getName());

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

        Label l_CreateDate = new Label(data.createDateProperty().getName());
        Label l_FromUser = new Label(data.fromUserProperty().getName());
        Label l_distributor = new Label(data.distributorProperty().getName());
        Label l_enpiAfter = new Label(data.enpiAfterProperty().getName());
        Label l_enpiBefore = new Label(data.enpiBeforeProperty().getName());
        Label l_enpiChange = new Label(data.enpiChangeProperty().getName());
        Label l_isNeedProcessDocument = new Label(data.isNeedProcessDocumentProperty().getName());
        Label l_isNeedWorkInstruction = new Label(data.isNeedWorkInstructionProperty().getName());
        Label l_isNeedTestInstruction = new Label(data.isNeedTestInstructionProperty().getName());
        Label l_isNeedDrawing = new Label(data.isNeedDrawingProperty().getName());
        Label l_isNeedOther = new Label(data.isNeedOtherProperty().getName());
        Label l_isNeedAdditionalAction = new Label(data.isNeedAdditionalActionProperty().getName());
        Label l_isAffectsOtherProcess = new Label(data.isAffectsOtherProcessProperty().getName());
        Label l_isConsumptionDocumented = new Label(data.isConsumptionDocumentedProperty().getName());
        Label l_isTargetReached = new Label(data.isTargetReachedProperty().getName());
        Label l_isNewEnPI = new Label(data.isNewEnPIProperty().getName());
        Label l_isNeedDocumentCorrection = new Label(data.isNeedDocumentCorrectionProperty().getName());
        Label l_isNeedCorrection = new Label(data.isNeedCorrectionProperty().getName());
        Label l_IsNeedAdditionalMeters = new Label(data.isNeedAdditionalMetersProperty().getName());

        Label l_titleDocument = new Label(I18n.getInstance().getString("plugin.action.needdocchange.title"));
        Label l_creator = new Label(data.fromUserProperty().getName());

        Label l_correctionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.correction"));
        Label l_nextActionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.followupaction"));
        Label l_alternativAction = new Label(I18n.getInstance().getString("plugin.action.alternativaction"));


        f_correctionIfNeeded.setPrefWidth(400);
        f_nextActionIfNeeded.setPrefWidth(400);
        f_alternativAction.setPrefWidth(400);


        l_isNeedProcessDocument.setText("Prozessanweisung");
        l_isNeedWorkInstruction.setText("Arbeitsanweisung");
        l_isNeedTestInstruction.setText("Prüfanweisung");
        l_isNeedDrawing.setText("Zeichnungen");
        l_isNeedOther.setText("Sonstige");

        CheckBox f_isNeedProcessDocument = new CheckBox();
        CheckBox f_isNeedWorkInstruction = new CheckBox();
        CheckBox f_isNeedTestInstruction = new CheckBox();
        CheckBox f_isNeedDrawing = new CheckBox();
        CheckBox f_isNeedOther = new CheckBox();


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
        //checklists
        gridPane.add(l_enpiAfter, 0, row);
        gridPane.add(l_enpiBefore, 0, ++row);
        gridPane.add(l_enpiChange, 0, ++row);

        row = 0;
        gridPane.add(f_enpiAfter, 1, row);
        gridPane.add(f_enpiBefore, 1, ++row);
        gridPane.add(f_enpiChange, 1, ++row);

        gridPane.add(l_FromUser, 3, 0);
        gridPane.add(l_CreateDate, 3, 1);
        gridPane.add(l_distributor, 3, 2);

        gridPane.add(f_FromUser, 4, 0);
        gridPane.add(f_CreateDate, 4, 1);
        gridPane.add(f_distributor, 4, 2);

        gridPane.add(l_correctionIfNeeded, 0, 5);
        gridPane.add(f_correctionIfNeeded, 0, 6, 2, 1);
        gridPane.add(l_nextActionIfNeeded, 3, 5);
        gridPane.add(f_nextActionIfNeeded, 3, 6, 2, 1);
        gridPane.add(l_alternativAction, 0, 7);
        gridPane.add(f_alternativAction, 0, 8, 2, 1);

        Separator sep = new Separator();
        GridPane.setHgrow(sep, Priority.ALWAYS);
        //sep.setPrefWidth(1000);
        gridPane.add(sep, 0, 9, 5, 1);

        row = 9;


        gridPane.add(new Label(I18n.getInstance().getString("plugin.action.dependencies.title")), 0, ++row, 2, 1);
        gridPane.add(q1, 0, ++row, 2, 1);
        gridPane.add(q2, 0, ++row, 2, 1);
        //gridPane.add(q3, 0, ++row, 2, 1);
        //gridPane.add(q4, 0, ++row, 2, 1);
        gridPane.add(q5, 0, ++row, 2, 1);

        row = 9;
        gridPane.add(l_titleDocument, 3, ++row, 2, 1);
        gridPane.add(qDoc1, 3, ++row, 2, 1);
        gridPane.add(qDoc2, 3, ++row, 2, 1);
        gridPane.add(qDoc3, 3, ++row, 2, 1);
        gridPane.add(qDoc4, 3, ++row, 2, 1);
        gridPane.add(qDoc5, 3, ++row, 2, 1);

    }


    private void updateView(ActionData data) {
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
        f_savingYear.textProperty().bindBidirectional(data.savingyearProperty());

        /*
        for (String s : data.statusTagsProperty().getValue().split(";")) {
            f_statusTags.getCheckModel().check(s);
        }
        f_statusTags.checkModelProperty().addListener((observable, oldValue, newValue) -> {
            data.statusTagsProperty().set(ActionPlan.listToString(f_statusTags.getCheckModel().getCheckedItems()));
        });
         */
        f_statusTags.getSelectionModel().select(data.statusTagsProperty().getValue());
        f_statusTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                data.statusTagsProperty().set(newValue);
            }
        });


        for (String s : data.mediaTagsProperty().getValue().split(";")) {
            f_mediaTags.getCheckModel().check(s);
        }
        f_mediaTags.checkModelProperty().addListener((observable, oldValue, newValue) -> {
            data.mediaTagsProperty().set(ActionPlan.listToString(f_mediaTags.getCheckModel().getCheckedItems()));
        });

        for (String s : data.fieldTagsProperty().getValue().split(";")) {
            f_fieldTags.getCheckModel().check(s);
        }
        f_fieldTags.checkModelProperty().addListener((observable, oldValue, newValue) -> {
            data.fieldTagsProperty().set(ActionPlan.listToString(f_fieldTags.getCheckModel().getCheckedItems()));
        });

        DateTime start = data.createDateProperty().get();
        f_CreateDate.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        f_CreateDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });

        f_doneDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

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

        f_ActionNr.setText(data.actionNrProperty().get() + "");

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

    }


}
