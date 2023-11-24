package org.jevis.jecc.plugin.legal.ui.tab;

import com.jfoenix.controls.JFXCheckBox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.legal.data.IndexOfLegalProvisions;
import org.jevis.jecc.plugin.legal.data.ObligationData;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.YearMonth;

public class GeneralTab extends Tab {
    private final MFXDatePicker f_issueDate = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    private final MFXDatePicker f_activeVersion = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    private final MFXDatePicker f_dateOfExamination = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());

    private final JFXCheckBox f_relevance = new JFXCheckBox();

    private final MFXTextField f_Nr = new MFXTextField();
    private final MFXTextField f_title = new MFXTextField();
    private final MFXTextField f_designation = new MFXTextField();
    private final TextArea f_description = new TextArea();

    private final MFXComboBox f_category = new MFXComboBox<>();
    private final MFXComboBox f_scope = new MFXComboBox<>();

    private final MFXTextField f_Attachment = new MFXTextField();

    private final TextArea f_importanceForTheCompany = new TextArea();
    private final MFXTextField f_link = new MFXTextField();


    private final Label l_title = new Label();
    private final Label l_designation = new Label();
    private final Label l_Nr = new Label();
    private final Label l_description = new Label();
    private final Label l_issueDate = new Label();
    private final Label l_activeVersion = new Label();
    private final Label l_relevance = new Label();
    private final Label l_dateOfExamination = new Label();
    private final Label l_importanceForTheCompany = new Label();
    private final Label l_link = new Label();

    private final Label l_category = new Label();
    private final Label l_scope = new Label();

    ObligationData obligationData;

    public GeneralTab(String s) {
        super(s);
    }

    @Override
    public void initTab(ObligationData data) {
        this.obligationData = data;
        f_category.setFloatMode(FloatMode.DISABLED);
        f_scope.setFloatMode(FloatMode.DISABLED);

        GridPane gridPane = new GridPane();
        notificationPane.setContent(gridPane);
        gridPane.setPadding(new Insets(20));

        IndexOfLegalProvisions nonconformityPlan = obligationData.getLegalCadastre();



        gridPane.setVgap(10);
        gridPane.setHgap(15);

        f_scope.setItems(data.getLegalCadastre().getScopes());
        f_category.setItems(data.getLegalCadastre().getCategories());

        add(gridPane, 1, 1, 1, 1, Priority.ALWAYS, l_Nr);
        add(gridPane, 1, 2, 1, 1, Priority.ALWAYS, l_title);
        add(gridPane, 1, 3, 1, 1, Priority.ALWAYS, l_designation);
        add(gridPane, 1, 4, 1, 1, Priority.ALWAYS, l_relevance);
        add(gridPane, 1, 5, 1, 1, Priority.ALWAYS, l_category);


        add(gridPane, 3, 1, 1, 1, Priority.ALWAYS, l_issueDate);
        add(gridPane, 3, 2, 1, 1, Priority.ALWAYS, l_activeVersion);
        add(gridPane, 3, 3, 1, 1, Priority.ALWAYS, l_dateOfExamination);
        add(gridPane, 3, 4, 1, 1, Priority.ALWAYS, l_link);
        add(gridPane, 3, 5, 1, 1, Priority.ALWAYS, l_scope);


        add(gridPane, 2, 1, 1, 1, Priority.ALWAYS, f_Nr);
        add(gridPane, 2, 2, 1, 1, Priority.ALWAYS, f_title);
        add(gridPane, 2, 3, 1, 1, Priority.ALWAYS, f_designation);
        add(gridPane, 2, 4, 1, 1, Priority.ALWAYS, f_relevance);
        add(gridPane, 2, 5, 1, 1, Priority.ALWAYS, f_category);

        add(gridPane, 4, 1, 1, 1, Priority.ALWAYS, f_issueDate);
        add(gridPane, 4, 2, 1, 1, Priority.ALWAYS, f_activeVersion);
        add(gridPane, 4, 3, 1, 1, Priority.ALWAYS, f_dateOfExamination);
        add(gridPane, 4, 4, 1, 1, Priority.ALWAYS, f_link);
        add(gridPane, 4, 5, 1, 1, Priority.ALWAYS, f_scope);


        add(gridPane, 1, 6, 2, 1, Priority.ALWAYS, l_description);
        add(gridPane, 1, 7, 2, 1, Priority.ALWAYS, f_description);

        add(gridPane, 3, 6, 2, 1, Priority.ALWAYS, l_importanceForTheCompany);
        add(gridPane, 3, 7, 2, 1, Priority.ALWAYS, f_importanceForTheCompany);

        f_description.setWrapText(true);
        f_importanceForTheCompany.setWrapText(true);


        Region col3Spacer = new Region();

        col3Spacer.setMinWidth(25);


        int textFieldHeight = 200;
        int textFieldWeight = 400;


        f_Nr.setEditable(ControlCenter.getExpert());


        //scrollPane.setContent(gridPane);
        this.setContent(notificationPane);
    }

    @Override
    public void updateView(ObligationData data) {
        ObligationData fake = new ObligationData();
        f_description.textProperty().bindBidirectional(data.descriptionProperty());
        f_title.textProperty().bindBidirectional(data.titleProperty());
        f_importanceForTheCompany.textProperty().bindBidirectional(data.importanceForTheCompanyProperty());
        f_designation.textProperty().bindBidirectional(data.designationProperty());
        f_link.textProperty().bindBidirectional(data.linkToVersionProperty());
        f_relevance.selectedProperty().bindBidirectional(data.relevantProperty());
        f_category.valueProperty().bindBidirectional(data.categoryProperty());
        f_scope.valueProperty().bindBidirectional(data.scopeProperty());

        l_Nr.setText(fake.nrProperty().getName());
        l_title.setText(fake.titleProperty().getName());
        l_designation.setText(fake.designationProperty().getName());
        l_activeVersion.setText(fake.currentVersionDateProperty().getName());
        l_relevance.setText(fake.relevantProperty().getName());
        l_dateOfExamination.setText(fake.dateOfExaminationProperty().getName());
        l_description.setText(fake.descriptionProperty().getName());
        l_importanceForTheCompany.setText(fake.importanceForTheCompanyProperty().getName());
        l_link.setText(fake.linkToVersionProperty().getName());
        l_issueDate.setText(fake.issueDateProperty().getName());
        l_category.setText(fake.categoryProperty().getName());
        l_scope.setText(fake.scopeProperty().getName());

        f_relevance.setMinWidth(200);
        f_relevance.setMaxWidth(200);
        f_issueDate.setMinWidth(200);
        f_issueDate.setMaxWidth(200);
        f_activeVersion.setMinWidth(200);
        f_activeVersion.setMaxWidth(200);
        f_dateOfExamination.setMaxWidth(200);
        f_dateOfExamination.setMinWidth(200);
        f_Nr.setMaxWidth(200);
        f_Nr.setMinWidth(200);
        f_title.setMinWidth(200);
        f_title.setMaxWidth(200);
        f_designation.setMinWidth(200);
        f_designation.setMaxWidth(200);
        f_category.setMinWidth(200);
        f_category.setMaxWidth(200);
        f_scope.setMaxWidth(200);
        f_scope.setMinWidth(200);
        //f_description.setMinWidth(200);
        //f_description.setMaxWidth(200);
        //f_importanceForTheCompany.setMinWidth(200);
        //f_importanceForTheCompany.setMaxWidth(200);
        f_link.setMinWidth(200);
        f_link.setMaxWidth(200);


        if (data.issueDateProperty().isNotNull().get()) {
            f_issueDate.setValue(LocalDate.of(data.getIssueDate().getYear(), data.getIssueDate().getMonthOfYear(), data.getIssueDate().getDayOfMonth()));
        }

        if (data.dateOfExaminationProperty().isNotNull().get()) {
            f_dateOfExamination.setValue(LocalDate.of(data.getDateOfExamination().getYear(), data.getDateOfExamination().getMonthOfYear(), data.getDateOfExamination().getDayOfMonth()));
        }

        if (data.currentVersionDateProperty().isNotNull().get()) {
            f_activeVersion.setValue(LocalDate.of(data.getCurrentVersionDate().getYear(), data.getCurrentVersionDate().getMonthOfYear(), data.getCurrentVersionDate().getDayOfMonth()));
        }


        f_activeVersion.valueProperty().addListener((observableValue, localDate, newValue) -> {
            data.currentVersionDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });


        f_dateOfExamination.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.dateOfExaminationProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

        });

        f_issueDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.issueDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));

        });

        f_Nr.setText(String.valueOf(data.nrProperty().get()));
        f_Nr.textProperty().addListener((observableValue, s, t1) -> {
            Integer i = Integer.valueOf(t1);
            data.nrProperty().set(i);
        });
    }
}
