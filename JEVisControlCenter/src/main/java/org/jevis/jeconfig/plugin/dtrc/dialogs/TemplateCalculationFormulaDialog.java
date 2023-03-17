package org.jevis.jeconfig.plugin.dtrc.dialogs;

import com.jfoenix.controls.*;
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
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dtrc.RCTemplate;
import org.jevis.jeconfig.plugin.dtrc.TemplateFormula;
import org.jevis.jeconfig.plugin.dtrc.TemplateInput;
import org.jevis.jeconfig.plugin.dtrc.TemplateOutput;

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
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

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

        Label timeRestrictionsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.timerestictions"));
        JFXCheckBox timeRestrictionEnabledCheckBox = new JFXCheckBox(I18n.getInstance().getString("jevistree.dialog.enable.title.enable"));
        timeRestrictionEnabledCheckBox.setSelected(templateFormula.getTimeRestrictionEnabled());
        timeRestrictionEnabledCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> templateFormula.setTimeRestrictionEnabled(t1));

        JFXComboBox<TimeFrame> fixedTimeFrameBox = new JFXComboBox<>();
        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> fixedTimeFrameJFXComboBoxCellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
                return new JFXListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getListName());
                        }
                    }
                };
            }
        };

        fixedTimeFrameBox.setCellFactory(fixedTimeFrameJFXComboBoxCellFactory);
        fixedTimeFrameBox.setButtonCell(fixedTimeFrameJFXComboBoxCellFactory.call(null));

        fixedTimeFrameBox.getItems().setAll(allowedTimeFrames);
        if (templateFormula.getFixedTimeFrame() != null) {
            TimeFrame selectedTimeFrame = allowedTimeFrames.stream().filter(timeFrame -> templateFormula.getFixedTimeFrame().equals(timeFrame.getID())).findFirst().orElse(null);
            fixedTimeFrameBox.getSelectionModel().select(selectedTimeFrame);
        }
        fixedTimeFrameBox.getSelectionModel().selectedItemProperty().addListener((observableValue, timeFrame, t1) -> templateFormula.setFixedTimeFrame(t1.getID()));

        JFXComboBox<TimeFrame> reducingTimeFrameBox = new JFXComboBox<>();
        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> reducingTimeFrameJFXComboBoxCellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
                return new JFXListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getListName());
                        }
                    }
                };
            }
        };

        reducingTimeFrameBox.setCellFactory(reducingTimeFrameJFXComboBoxCellFactory);
        reducingTimeFrameBox.setButtonCell(reducingTimeFrameJFXComboBoxCellFactory.call(null));

        reducingTimeFrameBox.getItems().setAll(allowedTimeFrames);
        if (templateFormula.getReducingTimeFrame() != null) {
            TimeFrame selectedTimeFrame = allowedTimeFrames.stream().filter(timeFrame -> templateFormula.getReducingTimeFrame().equals(timeFrame.getID())).findFirst().orElse(null);
            reducingTimeFrameBox.getSelectionModel().select(selectedTimeFrame);
        }
        reducingTimeFrameBox.getSelectionModel().selectedItemProperty().addListener((observableValue, timeFrame, t1) -> templateFormula.setReducingTimeFrame(t1.getID()));

        HBox timeRestrictionsBox = new HBox(6, timeRestrictionEnabledCheckBox, fixedTimeFrameBox, reducingTimeFrameBox);

        Separator separator5 = new Separator(Orientation.HORIZONTAL);
        separator5.setPadding(new Insets(8, 0, 8, 0));

        Separator separator6 = new Separator(Orientation.HORIZONTAL);
        separator6.setPadding(new Insets(8, 0, 8, 0));

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
            response = Response.OK;
            this.close();
        });

        cancelButton.setOnAction(event -> this.close());

        JFXButton delete = new JFXButton(I18n.getInstance().getString("jevistree.menu.delete"));
        delete.setOnAction(event -> {
            response = Response.DELETE;
            this.close();
        });


        ScrollPane outputsScrollPane = new ScrollPane(outputsGridPane);
        outputsScrollPane.setMinHeight(550);

        VBox vBox = new VBox(4, new HBox(4, nameLabel, jfxTextField), separator1,
                formulaLabel, jfxTextArea, separator2,
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
