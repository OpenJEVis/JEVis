package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.plugin.dtrc.RCTemplate;
import org.jevis.jeconfig.plugin.dtrc.TemplateFormula;
import org.jevis.jeconfig.plugin.dtrc.TemplateInput;
import org.jevis.jeconfig.plugin.dtrc.TemplateOutput;

public class TemplateCalculationFormulaDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(TemplateCalculationFormulaDialog.class);
    private final String ICON = "1404313956_evolution-tasks.png";
    private final AlphanumComparator ac = new AlphanumComparator();
    private FilteredList<JEVisObject> filteredList;
    private Response response = Response.CANCEL;

    public TemplateCalculationFormulaDialog(StackPane dialogContainer, JEVisDataSource ds, RCTemplate rcTemplate, TemplateFormula templateFormula) {
        super();

        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

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

        for (TemplateInput templateInput : rcTemplate.getTemplateFormulaInputs()) {
            JFXCheckBox jfxCheckBox = new JFXCheckBox(templateInput.getVariableName());
            jfxCheckBox.setStyle("-fx-text-fill: red !important;");
            jfxCheckBox.setMnemonicParsing(false);

            if (templateFormula.getInputIds().contains(templateInput.getTemplateFormula())) {
                jfxCheckBox.setSelected(true);
            }

            jfxCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !templateFormula.getInputIds().contains(templateInput.getTemplateFormula())) {
                    templateFormula.getInputIds().add(templateInput.getTemplateFormula());
                    jfxTextArea.setText(jfxTextArea.getText() + templateInput.getVariableName());
                } else templateFormula.getInputIds().remove(templateInput.getTemplateFormula());
            });

            inputsFlowPane.getChildren().add(jfxCheckBox);
        }

        for (TemplateInput templateInput : rcTemplate.getTemplateInputs()) {
            JFXCheckBox jfxCheckBox = new JFXCheckBox(templateInput.getVariableName());
            jfxCheckBox.setMnemonicParsing(false);

            if (templateFormula.getInputIds().contains(templateInput.getId())) {
                jfxCheckBox.setSelected(true);
            }

            jfxCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !templateFormula.getInputIds().contains(templateInput.getId())) {
                    templateFormula.getInputIds().add(templateInput.getId());
                    jfxTextArea.setText(jfxTextArea.getText() + templateInput.getVariableName());
                } else templateFormula.getInputIds().remove(templateInput.getId());
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

        for (TemplateOutput templateOutput : rcTemplate.getTemplateOutputs()) {
            JFXRadioButton jfxRadioButton = new JFXRadioButton();

            if (templateOutput.getName() != null) {
                jfxRadioButton.setText(templateOutput.getName());
            } else jfxRadioButton.setText(templateOutput.getVariableName());

            if (templateFormula.getOutput() != null && templateFormula.getOutput().equals(templateOutput.getId())) {
                jfxRadioButton.setSelected(true);
            }
            jfxRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> templateFormula.setOutput(templateOutput.getId()));
            jfxRadioButton.setToggleGroup(outputsToggleGroup);
            outputsGridPane.add(jfxRadioButton, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan());
        }

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        separator4.setPadding(new Insets(8, 0, 8, 0));

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            response = Response.OK;
            this.close();
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> this.close());

        JFXButton delete = new JFXButton(I18n.getInstance().getString("jevistree.menu.delete"));
        delete.setOnAction(event -> {
            response = Response.DELETE;
            this.close();
        });

        HBox buttonBar = new HBox(8, delete, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane outputsScrollPane = new ScrollPane(outputsGridPane);
        outputsScrollPane.setMinHeight(550);

        VBox vBox = new VBox(4, new HBox(4, nameLabel, jfxTextField), separator1,
                formulaLabel, jfxTextArea, separator2,
                inputsLabel, inputsFlowPane, separator3,
                outputsLabel, outputsScrollPane, new Label("UUID: " + templateFormula.getId()), separator4,
                buttonBar);

        vBox.setPadding(new Insets(12));

        ScrollPane vBoxScrollPane = new ScrollPane(vBox);
        vBoxScrollPane.setFitToHeight(true);
        vBoxScrollPane.setFitToWidth(true);
        vBoxScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        vBoxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setContent(vBoxScrollPane);
    }

    public Response getResponse() {
        return response;
    }
}
