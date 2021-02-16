package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.dtrc.TemplateFormula;
import org.jevis.jeconfig.plugin.dtrc.TemplateInput;
import org.jevis.jeconfig.plugin.dtrc.TemplateOutput;

import java.util.List;

public class TemplateCalculationFormulaDialog {
    private static final Logger logger = LogManager.getLogger(TemplateCalculationFormulaDialog.class);
    private final String ICON = "1404313956_evolution-tasks.png";
    private final AlphanumComparator ac = new AlphanumComparator();
    private FilteredList<JEVisObject> filteredList;
    private Response response = Response.CANCEL;

    public Response show(JEVisDataSource ds, List<TemplateInput> templateInputList, List<TemplateOutput> templateOutputList, TemplateFormula templateFormula) {
        Stage stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("dialog.selection.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(JEConfig.getStage());

        Label nameLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.namelabel"));
        JFXTextField jfxTextField = new JFXTextField(templateFormula.getName());
        jfxTextField.textProperty().addListener((observable, oldValue, newValue) -> templateFormula.setName(newValue));

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setPadding(new Insets(8, 0, 8, 0));

        Label formulaLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel"));
        JFXTextArea jfxTextArea = new JFXTextArea(templateFormula.getFormula());
        jfxTextArea.textProperty().addListener((observable, oldValue, newValue) -> templateFormula.setFormula(newValue));

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setPadding(new Insets(8, 0, 8, 0));

        Label inputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.inputslabel"));
        FlowPane inputsFlowPane = new FlowPane(4, 4);

        for (TemplateInput templateInput : templateInputList) {
            JFXCheckBox jfxCheckBox = new JFXCheckBox(templateInput.getVariableName());
            jfxCheckBox.setMnemonicParsing(false);
            if (templateFormula.getInputs().contains(templateInput)) {
                jfxCheckBox.setSelected(true);
            }

            jfxCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !templateFormula.getInputs().contains(templateInput)) {
                    templateFormula.getInputs().add(templateInput);
                    jfxTextArea.setText(jfxTextArea.getText() + templateInput.getVariableName());
                } else templateFormula.getInputs().remove(templateInput);
            });

            inputsFlowPane.getChildren().add(jfxCheckBox);
        }

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setPadding(new Insets(8, 0, 8, 0));

        Label outputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.outputslabel"));
        GridPane outputsGridPane = new GridPane();
        outputsGridPane.setHgap(6);
        outputsGridPane.setVgap(6);
        final ToggleGroup outputsToggleGroup = new ToggleGroup();

        for (TemplateOutput templateOutput : templateOutputList) {
            JFXRadioButton jfxRadioButton = new JFXRadioButton();

            if (templateOutput.getName() != null) {
                jfxRadioButton.setText(templateOutput.getName());
            } else jfxRadioButton.setText(templateOutput.getVariableName());

            if (templateFormula.getOutput() != null && templateFormula.getOutput().equals(templateOutput.getVariableName())) {
                jfxRadioButton.setSelected(true);
            }
            jfxRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> templateFormula.setOutput(templateOutput.getVariableName()));
            jfxRadioButton.setToggleGroup(outputsToggleGroup);
            outputsGridPane.add(jfxRadioButton, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan());
        }

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        separator4.setPadding(new Insets(8, 0, 8, 0));

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            response = Response.OK;
            stage.close();
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> stage.close());

        JFXButton delete = new JFXButton(I18n.getInstance().getString("jevistree.menu.delete"));
        delete.setOnAction(event -> {
            response = Response.DELETE;
            stage.close();
        });

        HBox buttonBar = new HBox(8, delete, cancel, ok);

        VBox vBox = new VBox(4, new HBox(4, nameLabel, jfxTextField), separator1,
                formulaLabel, jfxTextArea, separator2,
                inputsLabel, inputsFlowPane, separator3,
                outputsLabel, outputsGridPane, separator4,
                buttonBar);

        vBox.setPadding(new Insets(12));

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setWidth(650);
        stage.setHeight(350);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.getIcons().setAll(ResourceLoader.getImage(ICON, 64, 64).getImage());
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TopMenu.applyActiveTheme(scene);
            }
        });
        stage.showAndWait();

        return response;
    }
}
