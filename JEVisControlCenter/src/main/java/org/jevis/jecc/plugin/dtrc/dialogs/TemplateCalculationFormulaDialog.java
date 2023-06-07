package org.jevis.jecc.plugin.dtrc.dialogs;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXRadioButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.dialog.Response;
import org.jevis.jecc.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jecc.plugin.dtrc.RCTemplate;
import org.jevis.jecc.plugin.dtrc.TemplateFormula;
import org.jevis.jecc.plugin.dtrc.TemplateInput;
import org.jevis.jecc.plugin.dtrc.TemplateOutput;

import java.util.List;

public class TemplateCalculationFormulaDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(TemplateCalculationFormulaDialog.class);
    private final String ICON = "1404313956_evolution-tasks.png";
    private final AlphanumComparator ac = new AlphanumComparator();
    private FilteredList<JEVisObject> filteredList;
    private Response response = Response.CANCEL;

    public TemplateCalculationFormulaDialog(JEVisDataSource ds, RCTemplate rcTemplate, TemplateFormula templateFormula, List<TimeFrame> allowedTimeFrames) {
        super();

        setTitle(I18n.getInstance().getString("plugin.trc.formuladialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.trc.formuladialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        Label nameLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.namelabel"));
        MFXTextField MFXTextField = new MFXTextField(templateFormula.getName());
        MFXTextField.textProperty().addListener((observable, oldValue, newValue) -> templateFormula.setName(newValue));

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setPadding(new Insets(8, 0, 8, 0));

        Label formulaLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel"));
        TextArea textArea = new TextArea(templateFormula.getFormula());
        textArea.textProperty().addListener((observable, oldValue, newValue) -> templateFormula.setFormula(newValue));

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setPadding(new Insets(8, 0, 8, 0));

        Label inputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.inputslabel"));
        FlowPane inputsFlowPane = new FlowPane(4, 4);

        for (TemplateInput templateInput : rcTemplate.getTemplateFormulaInputs()) {
            MFXCheckbox mfxCheckbox = new MFXCheckbox(templateInput.getVariableName());
            mfxCheckbox.setStyle("-fx-text-fill: red !important;");
            mfxCheckbox.setMnemonicParsing(false);

            if (templateFormula.getInputIds().contains(templateInput.getTemplateFormula())) {
                mfxCheckbox.setSelected(true);
            }

            mfxCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !templateFormula.getInputIds().contains(templateInput.getTemplateFormula())) {
                    templateFormula.getInputIds().add(templateInput.getTemplateFormula());
                    textArea.setText(textArea.getText() + templateInput.getVariableName());
                } else templateFormula.getInputIds().remove(templateInput.getTemplateFormula());
            });

            inputsFlowPane.getChildren().add(mfxCheckbox);
        }

        for (TemplateInput templateInput : rcTemplate.getTemplateInputs()) {
            MFXCheckbox mfxCheckbox = new MFXCheckbox(templateInput.getVariableName());
            mfxCheckbox.setMnemonicParsing(false);

            if (templateFormula.getInputIds().contains(templateInput.getId())) {
                mfxCheckbox.setSelected(true);
            }

            mfxCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !templateFormula.getInputIds().contains(templateInput.getId())) {
                    templateFormula.getInputIds().add(templateInput.getId());
                    textArea.setText(textArea.getText() + templateInput.getVariableName());
                } else templateFormula.getInputIds().remove(templateInput.getId());
            });

            inputsFlowPane.getChildren().add(mfxCheckbox);
        }

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setPadding(new Insets(8, 0, 8, 0));

        Label outputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.outputslabel"));
        GridPane outputsGridPane = new GridPane();
        outputsGridPane.setHgap(6);
        outputsGridPane.setVgap(6);
        final ToggleGroup outputsToggleGroup = new ToggleGroup();

        for (TemplateOutput templateOutput : rcTemplate.getTemplateOutputs()) {
            MFXRadioButton mfxRadioButton = new MFXRadioButton();

            if (templateOutput.getName() != null) {
                mfxRadioButton.setText(templateOutput.getName());
            } else mfxRadioButton.setText(templateOutput.getVariableName());

            if (templateFormula.getOutput() != null && templateFormula.getOutput().equals(templateOutput.getId())) {
                mfxRadioButton.setSelected(true);
            }
            mfxRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> templateFormula.setOutput(templateOutput.getId()));
            mfxRadioButton.setToggleGroup(outputsToggleGroup);
            outputsGridPane.add(mfxRadioButton, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan());
        }

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        separator4.setPadding(new Insets(8, 0, 8, 0));

        Label timeRestrictionsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.timerestictions"));
        MFXCheckbox timeRestrictionEnabledCheckBox = new MFXCheckbox(I18n.getInstance().getString("jevistree.dialog.enable.title.enable"));
        timeRestrictionEnabledCheckBox.setSelected(templateFormula.getTimeRestrictionEnabled());
        timeRestrictionEnabledCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> templateFormula.setTimeRestrictionEnabled(t1));

        MFXComboBox<TimeFrame> fixedTimeFrameBox = new MFXComboBox<>();

        //TODO JFX17

        fixedTimeFrameBox.setConverter(new StringConverter<TimeFrame>() {
            @Override
            public String toString(TimeFrame object) {
                return object.getListName();
            }

            @Override
            public TimeFrame fromString(String string) {
                return fixedTimeFrameBox.getItems().get(fixedTimeFrameBox.getSelectedIndex());
            }
        });

        fixedTimeFrameBox.getItems().setAll(allowedTimeFrames);
        if (templateFormula.getFixedTimeFrame() != null) {
            TimeFrame selectedTimeFrame = allowedTimeFrames.stream().filter(timeFrame -> templateFormula.getFixedTimeFrame().equals(timeFrame.getID())).findFirst().orElse(null);
            fixedTimeFrameBox.selectItem(selectedTimeFrame);
        }
        fixedTimeFrameBox.getSelectionModel().selectedItemProperty().addListener((observableValue, timeFrame, t1) -> templateFormula.setFixedTimeFrame(t1.getID()));

        MFXComboBox<TimeFrame> reducingTimeFrameBox = new MFXComboBox<>();

        //TODO JFX17

        reducingTimeFrameBox.setConverter(new StringConverter<TimeFrame>() {
            @Override
            public String toString(TimeFrame object) {
                return object.getListName();
            }

            @Override
            public TimeFrame fromString(String string) {
                return reducingTimeFrameBox.getItems().get(reducingTimeFrameBox.getSelectedIndex());
            }
        });

        reducingTimeFrameBox.getItems().setAll(allowedTimeFrames);
        if (templateFormula.getReducingTimeFrame() != null) {
            TimeFrame selectedTimeFrame = allowedTimeFrames.stream().filter(timeFrame -> templateFormula.getReducingTimeFrame().equals(timeFrame.getID())).findFirst().orElse(null);
            reducingTimeFrameBox.selectItem(selectedTimeFrame);
        }
        reducingTimeFrameBox.getSelectionModel().selectedItemProperty().addListener((observableValue, timeFrame, t1) -> templateFormula.setReducingTimeFrame(t1.getID()));

        HBox timeRestrictionsBox = new HBox(6, timeRestrictionEnabledCheckBox, fixedTimeFrameBox, reducingTimeFrameBox);

        Separator separator5 = new Separator(Orientation.HORIZONTAL);
        separator5.setPadding(new Insets(8, 0, 8, 0));

        Separator separator6 = new Separator(Orientation.HORIZONTAL);
        separator6.setPadding(new Insets(8, 0, 8, 0));

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

        ScrollPane outputsScrollPane = new ScrollPane(outputsGridPane);
        outputsScrollPane.setMinHeight(550);

        VBox vBox = new VBox(4, new HBox(4, nameLabel, MFXTextField), separator1,
                formulaLabel, textArea, separator2,
                inputsLabel, inputsFlowPane, separator3,
                outputsLabel, outputsScrollPane, separator4,
                timeRestrictionsLabel, timeRestrictionsBox, separator5,
                new Label("UUID: " + templateFormula.getId()), separator6);

        vBox.setPadding(new Insets(12));

        ScrollPane vBoxScrollPane = new ScrollPane(vBox);
        vBoxScrollPane.setFitToHeight(true);
        vBoxScrollPane.setFitToWidth(true);
        vBoxScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        vBoxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getDialogPane().setContent(vBoxScrollPane);
    }

    public Response getResponse() {
        return response;
    }
}
