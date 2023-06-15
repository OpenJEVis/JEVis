package org.jevis.jecc.plugin.dtrc.dialogs;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.tools.NumberSpinner;
import org.jevis.jecc.dialog.Response;
import org.jevis.jecc.plugin.dtrc.TemplateOutput;

import java.math.BigDecimal;

public class TemplateCalculationOutputDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(TemplateCalculationOutputDialog.class);
    private final String ICON = "1404313956_evolution-tasks.png";
    private final AlphanumComparator ac = new AlphanumComparator();
    private final JEVisDataSource ds;
    private final TemplateOutput templateOutput;
    private FilteredList<JEVisObject> filteredList;
    private Response response = Response.CANCEL;

    public TemplateCalculationOutputDialog(JEVisDataSource ds, TemplateOutput templateOutput) {
        super();
        this.ds = ds;
        this.templateOutput = templateOutput;

        setTitle(I18n.getInstance().getString("plugin.trc.outputdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.trc.outputdialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(6));
        gridPane.setVgap(4);
        gridPane.setHgap(4);

        ColumnConstraints labelWidth = new ColumnConstraints(80);
        gridPane.getColumnConstraints().add(0, labelWidth);
        ColumnConstraints fieldWidth = new ColumnConstraints(150);
        gridPane.getColumnConstraints().add(1, fieldWidth);

        Label nameLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.namelabel"));

        MFXCheckbox nameBold = new MFXCheckbox(I18n.getInstance().getString("plugin.dtrc.dialog.bold"));
        nameBold.setSelected(templateOutput.getNameBold());
        GridPane.setHgrow(nameLabel, Priority.ALWAYS);

        Label variableNameLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.variablenamelabel"));
        GridPane.setHgrow(variableNameLabel, Priority.ALWAYS);

        Label unitLabel = new Label(I18n.getInstance().getString("graph.table.unit"));
        GridPane.setHgrow(unitLabel, Priority.ALWAYS);

        Label tooltipLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.tooltip"));
        GridPane.setHgrow(tooltipLabel, Priority.ALWAYS);

        MFXTextField nameField = new MFXTextField(templateOutput.getName());
        nameField.setFloatMode(FloatMode.DISABLED);
        MFXTextField variableNameField = new MFXTextField(templateOutput.getVariableName());
        variableNameField.setFloatMode(FloatMode.DISABLED);
        MFXTextField unitField = new MFXTextField(templateOutput.getUnit());
        unitField.setFloatMode(FloatMode.DISABLED);
        MFXTextField tooltipField = new MFXTextField(templateOutput.getTooltip());
        tooltipField.setFloatMode(FloatMode.DISABLED);

        MFXCheckbox isLinkToggle = new MFXCheckbox(I18n.getInstance().getString("plugin.accounting.tab.enterdata"));
        isLinkToggle.setSelected(templateOutput.getLink());

        MFXCheckbox showTooltip = new MFXCheckbox(I18n.getInstance().getString("plugin.dtrc.dialog.showtooltip"));
        showTooltip.setSelected(templateOutput.getShowTooltip());

        Label columnLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.columnlabel"));
        NumberSpinner columnSpinner = new NumberSpinner(new BigDecimal(templateOutput.getColumn()), new BigDecimal(1));

        Label rowLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.rowlabel"));
        NumberSpinner rowSpinner = new NumberSpinner(new BigDecimal(templateOutput.getRow()), new BigDecimal(1));

        Label colSpanLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.colspanlabel"));
        NumberSpinner colSpanSpinner = new NumberSpinner(new BigDecimal(templateOutput.getColSpan()), new BigDecimal(1));

        Label rowSpanLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.rowspanlabel"));
        NumberSpinner rowSpanSpinner = new NumberSpinner(new BigDecimal(templateOutput.getRowSpan()), new BigDecimal(1));

        MFXCheckbox resultBold = new MFXCheckbox(I18n.getInstance().getString("plugin.dtrc.dialog.bold"));
        resultBold.setSelected(templateOutput.getResultBold());

        MFXCheckbox showAnalysisLink = new MFXCheckbox(I18n.getInstance().getString("plugin.dtrc.dialog.showanalysislink"));
        showAnalysisLink.setSelected(templateOutput.getShowAnalysisLink());

        MFXCheckbox separatorToggle = new MFXCheckbox(I18n.getInstance().getString("plugin.dtrc.dialog.separator"));
        separatorToggle.setSelected(templateOutput.getSeparator());

        nameField.textProperty().addListener((observable, oldValue, newValue) -> templateOutput.setName(newValue));
        variableNameField.textProperty().addListener((observable, oldValue, newValue) -> templateOutput.setVariableName(newValue));
        unitField.textProperty().addListener((observable, oldValue, newValue) -> templateOutput.setUnit(newValue));
        tooltipField.textProperty().addListener((observable, oldValue, newValue) -> templateOutput.setTooltip(newValue));
        columnSpinner.numberProperty().addListener((observable, oldValue, newValue) -> templateOutput.setColumn(newValue.intValue()));
        rowSpinner.numberProperty().addListener((observable, oldValue, newValue) -> templateOutput.setRow(newValue.intValue()));
        colSpanSpinner.numberProperty().addListener((observable, oldValue, newValue) -> templateOutput.setColSpan(newValue.intValue()));
        rowSpanSpinner.numberProperty().addListener((observable, oldValue, newValue) -> templateOutput.setRowSpan(newValue.intValue()));
        nameBold.selectedProperty().addListener((observable, oldValue, newValue) -> templateOutput.setNameBold(newValue));
        resultBold.selectedProperty().addListener((observable, oldValue, newValue) -> templateOutput.setResultBold(newValue));
        separatorToggle.selectedProperty().addListener((observable, oldValue, newValue) -> templateOutput.setSeparator(newValue));
        isLinkToggle.selectedProperty().addListener((observable, oldValue, newValue) -> templateOutput.setLink(newValue));
        showTooltip.selectedProperty().addListener((observable, oldValue, newValue) -> templateOutput.setShowTooltip(newValue));
        showAnalysisLink.selectedProperty().addListener((observable, oldValue, newValue) -> templateOutput.setShowAnalysisLink(newValue));

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType deleteType = new ButtonType(I18n.getInstance().getString("jevistree.menu.delete"), ButtonBar.ButtonData.OTHER);

        this.getDialogPane().getButtonTypes().addAll(deleteType, cancelType, okType);

        Button deleteButton = (Button) this.getDialogPane().lookupButton(deleteType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
            response = Response.OK;
            this.close();
        });

        cancelButton.setOnAction(event -> this.close());

        deleteButton.setOnAction(event -> {
            response = Response.DELETE;
            this.close();
        });

        int row = 0;
        gridPane.add(nameLabel, 0, row);
        gridPane.add(nameField, 1, row);
        gridPane.add(nameBold, 2, row);
        row++;

        gridPane.add(variableNameLabel, 0, row);
        gridPane.add(variableNameField, 1, row);
        gridPane.add(resultBold, 2, row);
        row++;

        gridPane.add(showAnalysisLink, 0, row, 3, 1);
        row++;

        gridPane.add(unitLabel, 0, row);
        gridPane.add(unitField, 1, row);
        row++;

        gridPane.add(tooltipLabel, 0, row);
        gridPane.add(tooltipField, 1, row);
        gridPane.add(showTooltip, 2, row);
        row++;

        gridPane.add(separatorToggle, 0, row, 3, 1);
        row++;

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setPadding(new Insets(8, 0, 8, 0));
        gridPane.add(separator1, 0, row, 3, 1);
        row++;

        gridPane.add(isLinkToggle, 0, row, 2, 1);
        row++;

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setPadding(new Insets(8, 0, 8, 0));
        gridPane.add(separator2, 0, row, 3, 1);
        row++;

        gridPane.add(columnLabel, 0, row);
        gridPane.add(columnSpinner, 1, row);
        row++;

        gridPane.add(rowLabel, 0, row);
        gridPane.add(rowSpinner, 1, row);
        row++;

        gridPane.add(colSpanLabel, 0, row);
        gridPane.add(colSpanSpinner, 1, row);
        row++;

        gridPane.add(rowSpanLabel, 0, row);
        gridPane.add(rowSpanSpinner, 1, row);
        row++;

        gridPane.add(new Label("UUID: " + templateOutput.getId()), 0, row, 3, 1);
        row++;

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setPadding(new Insets(8, 0, 8, 0));
        gridPane.add(separator3, 0, row, 3, 1);
        row++;

        getDialogPane().setContent(gridPane);
    }

    public Response getResponse() {
        return response;
    }


}
