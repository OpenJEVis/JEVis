package org.jevis.jeconfig.plugin.object.extension.calculation;

import com.jfoenix.controls.JFXTextArea;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.sourceforge.jeval.Evaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.dialog.ExceptionDialog2;
import org.jevis.jeconfig.dialog.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FormulaBox extends HBox {

    private static final Logger logger = LogManager.getLogger(FormulaBox.class);
    private final List<String> variables = new ArrayList<>();
    Label errorArea = new Label();
    //    JFXTextArea textArea = new TextArea();
    JFXTextArea textArea = new JFXTextArea();
    private JEVisObject calcObj;
    private MFXButton outputButton;

    public FormulaBox() {
        super();
        setSpacing(5);

//        textArea.setPrefHeight(200);
//        errorArea.setPrefWidth(200);
        errorArea.setTextFill(Color.FIREBRICK);
        textArea.setWrapText(true);
        textArea.setMinHeight(90d);
        getChildren().addAll(textArea);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
//                eval();
        });

    }


    public String getExpression() {
        return textArea.getText();
    }

    public void setExpression(String expression) {
        textArea.setText(expression);
    }

    public void backspaceExpression() {
        int caret = textArea.getCaretPosition();
        String beforeText = textArea.getText().substring(0, caret - 1);
        Platform.runLater(() -> {
            textArea.setText(beforeText);
            textArea.positionCaret(caret - 1);

        });

    }

    public void addExpression(String expression) {
        if (expression != null && !expression.isEmpty()) {
            int caret = textArea.getCaretPosition();
            String beforeText = textArea.getText().substring(0, caret);
            String afterText = textArea.getText().substring(caret);
            String newText = beforeText + expression + afterText;
            Platform.runLater(() -> {
                textArea.setText(newText);
                textArea.positionCaret(caret + expression.length());
                textArea.selectPositionCaret(caret + expression.length());

            });
        }

    }

    public void eval() {
        Evaluator eval = new Evaluator();
        try {
            eval.parse(textArea.getText());

            for (String var : variables) {
                eval.putVariable(var, "1");
            }

            List<String> warnings = new ArrayList();
            List<String> error = new ArrayList<>();
            List<String> info = new ArrayList<>();
            List<Stats> stats = new ArrayList<>();

            AtomicBoolean hasInput = new AtomicBoolean(false);
            AtomicBoolean hasOutput = new AtomicBoolean(false);
            calcObj.getChildren().forEach(jeVisObject -> {
                try {
                    if (jeVisObject.getJEVisClassName().equals(JC.Input.name)) {
                        hasInput.set(true);
                        String inputName = jeVisObject.getName();

                        JEVisAttribute targetAtt = jeVisObject.getAttribute(JC.Input.a_InputData);
                        JEVisAttribute identifyAtt = jeVisObject.getAttribute(JC.Input.a_Identifier);
                        JEVisAttribute inputTypeAtt = jeVisObject.getAttribute(JC.Input.a_InputDataType);

                        if (targetAtt == null || !targetAtt.hasSample()) {
                            error.add(String.format("%s - %s", inputName, I18n.getInstance().getString("plugin.object.extension.calculation.error.noinputdata")));
                        } else {
                            TargetHelper targetHelper = new TargetHelper(targetAtt.getDataSource(), targetAtt);

                            if (!targetHelper.isValid()) {
                                error.add(String.format("%s - %s", inputName, I18n.getInstance().getString("plugin.object.extension.calculation.error.novalidtarget")));
                                stats.add(new Stats(null, inputName, null, null, 0));
                            } else {
                                JEVisAttribute targetAttData = targetHelper.getAttribute().get(0);
                                if (!targetAttData.hasSample()) {
                                    error.add(String.format("%s - %s", inputName, I18n.getInstance().getString("plugin.object.extension.calculation.error.notarget")));
                                    stats.add(new Stats(jeVisObject, inputName, null, null, 0));
                                } else {
                                    stats.add(new Stats(jeVisObject,
                                            inputName, targetAttData.getTimestampFromFirstSample(),
                                            targetAttData.getTimestampFromLastSample(),
                                            0)); // targetAttData.getAllSamples().size()
                                }
                            }
                        }

                        if (identifyAtt == null || !identifyAtt.hasSample()) {
                            error.add(String.format("%s - %s", inputName, I18n.getInstance().getString("plugin.object.extension.calculation.warn.noid")));
                            if (!variables.contains(identifyAtt)) {
                                warnings.add(String.format("%s - %s", inputName, I18n.getInstance().getString("plugin.object.extension.calculation.warn.varnotused")));
                            }
                        }

                        if (inputTypeAtt == null || !inputTypeAtt.hasSample()) {
                            error.add(String.format("%s - %s", inputName, I18n.getInstance().getString("plugin.object.extension.calculation.error.missingtype")));
                        }


                    }

                    if (jeVisObject.getJEVisClassName().equals("Output")) {
                        hasOutput.set(true);
                        JEVisAttribute outputTargetAtt = jeVisObject.getAttribute("Output");
                        if (outputTargetAtt == null || !outputTargetAtt.hasSample()) {
                            error.add(String.format("%s - %s", jeVisObject.getName(), I18n.getInstance().getString("plugin.object.extension.calculation.error.missingoutput")));
                        } else {
                            //check if target ins also a target of something else (calc, datasource)
                        }
                    }

                } catch (Exception ex) {
                    error.add(String.format("%s: %S", I18n.getInstance().getString("plugin.object.extension.calculation.error.unkown"), jeVisObject, ex.getMessage()));
                }
            });

            if (!hasInput.get()) {
                error.add(I18n.getInstance().getString("plugin.object.extension.calculation.error.noinput"));
            }
            if (!hasOutput.get()) {
                error.add(I18n.getInstance().getString("plugin.object.extension.calculation.error.nooutput"));
            }


            boolean valIsOK = false;
            try {
                String value = eval.evaluate();
                valIsOK = true;
                info.add(I18n.getInstance().getString("plugin.object.extension.calculation.error.noerrorformula"));

            } catch (Exception ex) {
                error.add(String.format("%s: %S", I18n.getInstance().getString("plugin.object.extension.calculation.error.formula"), ex.getMessage()));
            }

            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

            /*
            info.add("Min Data       | Max Data        | Sample Count | Input");
            stats.forEach(stat1 -> {
                String from = "";
                if (stat1.fromDate != null) {
                    from = stat1.fromDate.toString(fmt);
                }

                String until = "";
                if (stat1.untilDate != null) {
                    until = stat1.untilDate.toString(fmt);
                }

                info.add(String.format("%s | %s | %10d | %s", from, until, stat1.sampleCount, stat1.name));
            });

             */


            StringBuilder errorStringBuilder = new StringBuilder();
            if (error.isEmpty()) {
                errorStringBuilder.append(I18n.getInstance().getString("plugin.object.extension.calculation.error.noerror"));
                errorStringBuilder.append(System.getProperty("line.separator"));
            } else {
                //errorStringBuilder.append("Errors:");
                errorStringBuilder.append(System.getProperty("line.separator"));
                error.forEach(s -> {
                    errorStringBuilder.append("- ");
                    errorStringBuilder.append(s);
                    errorStringBuilder.append(System.getProperty("line.separator"));
                });
            }

            StringBuilder warningsStringBuilder = new StringBuilder();
            if (warnings.isEmpty()) {
                warningsStringBuilder.append(I18n.getInstance().getString("plugin.object.extension.calculation.error.nowarning"));
                warningsStringBuilder.append(System.getProperty("line.separator"));
            } else {
                //warningsStringBuilder.append("Warnings:");
                warningsStringBuilder.append(System.getProperty("line.separator"));
                warnings.forEach(s -> {
                    warningsStringBuilder.append("- ");
                    warningsStringBuilder.append(s);
                    warningsStringBuilder.append(System.getProperty("line.separator"));
                });
            }

            Label waringLabel = new Label(I18n.getInstance().getString("plugin.object.extension.calculation.label.warning"));
            Label warningTextArea = new Label();
            waringLabel.setWrapText(true);
            warningTextArea.setText(warningsStringBuilder.toString());

            Label errorLabel = new Label(I18n.getInstance().getString("plugin.object.extension.calculation.label.error"));
            Label errorTextArea = new Label();
            errorTextArea.setWrapText(true);
            errorTextArea.setText(errorStringBuilder.toString());


            TableView tableView = new TableView();
            TableColumn<Stats, String> firstNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.dr"));
            firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            TableColumn<Stats, String> firstDateColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.firstts"));
            firstDateColumn.setCellValueFactory(new PropertyValueFactory<>("fromDate"));
            TableColumn<Stats, String> lastDateColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.lastts"));
            lastDateColumn.setCellValueFactory(new PropertyValueFactory<>("untilDate"));
            TableColumn<Stats, String> identifierColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.varname"));
            identifierColumn.setCellValueFactory(new PropertyValueFactory<>("Identifier"));
            TableColumn<Stats, String> targetPeriodColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.drperiode"));
            targetPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("TargetPeriod"));
            TableColumn<Stats, String> inputDataTypeColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.inputperiod"));
            inputDataTypeColumn.setCellValueFactory(new PropertyValueFactory<>("InputDataType"));
            TableColumn<Stats, String> targetNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.extension.calculation.table.drname"));
            targetNameColumn.setCellValueFactory(new PropertyValueFactory<>("TargetName"));

            Label infoLabel = new Label(I18n.getInstance().getString("plugin.object.extension.calculation.label.information"));

            tableView.getColumns().addAll(targetNameColumn, identifierColumn, firstDateColumn, lastDateColumn, targetPeriodColumn, inputDataTypeColumn);
            tableView.getItems().addAll(stats);

            VBox vBox = new VBox();

            vBox.getChildren().addAll(
                    errorLabel,
                    new Separator(Orientation.HORIZONTAL),
                    errorTextArea,
                    waringLabel,
                    new Separator(Orientation.HORIZONTAL),
                    warningTextArea,
                    infoLabel,
                    new Separator(Orientation.HORIZONTAL),
                    tableView
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.title"));
            alert.setHeaderText(I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.header"));
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(1350);
            //alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            alert.getDialogPane().setContent(vBox);
            //alert.setContentText(I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.success"));

            alert.showAndWait();

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.fail"));
            alert.setHeaderText(null);
            alert.setContentText(getErrorMessage(ex));
            alert.showAndWait();


            ex.printStackTrace();
        }

    }

    private String getErrorMessage(Exception ex) {
        String message = I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.fail");
        try {
            message = ex.getCause().toString();
        } catch (NullPointerException np) {
            message = ex.getMessage();
        }

        return message;
    }

    public void updateVariables() throws JEVisException {
        variables.clear();
        JEVisClass input = calcObj.getDataSource().getJEVisClass("Input");
        for (JEVisObject inpuObject : calcObj.getChildren()) {
            if (inpuObject.getJEVisClass().equals(input)) {
                try {
                    JEVisAttribute id = inpuObject.getAttribute("Identifier");
                    if (id != null) {
                        JEVisSample value = id.getLatestSample();
                        if (value != null && !value.getValueAsString().isEmpty()) {
                            variables.add(value.getValueAsString());
                        }
                    }
                } catch (Exception inputEx) {
                    inputEx.printStackTrace();
                }
            }
        }
    }

    public void setOutputButton(MFXButton buttonOutput) {
        outputButton = buttonOutput;
        try {
            JEVisClass outputClass = this.calcObj.getDataSource().getJEVisClass("Output");
            List<JEVisObject> outputs = this.calcObj.getChildren(outputClass, true);
            if (!outputs.isEmpty()) {//there can only be one output
                JEVisObject outputObj = outputs.get(0);
                outputButton.setText(outputObj.getName());
                Tooltip tt = new Tooltip();
                tt.setText("ID: " + outputObj.getID());
                outputButton.setTooltip(tt);
            } else {
                outputButton.setText(I18n.getInstance().getString("extension.calc.outputbutton"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.debug("Button.text: " + buttonOutput.getText());
        outputButton.textProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("Button text changed: " + oldValue + " new: " + newValue);
        });
        outputButton.setOnAction(event -> setOnOutputAction());
    }

    void setOnOutputAction() {

        try {
            JEVisClass outputClass = this.calcObj.getDataSource().getJEVisClass(JC.Output.name);
            List<JEVisObject> outputs = this.calcObj.getChildren(outputClass, true);
            List<UserSelection> openList = new ArrayList<>();
            JEVisObject outputObj = null;

            if (!outputs.isEmpty()) {//there can only be one output
                outputObj = outputs.get(0);
                JEVisAttribute targetAttribute = outputObj.getAttribute(JC.Output.a_Output);
                JEVisSample targetSample = targetAttribute.getLatestSample();

                if (targetSample != null) {
                    TargetHelper th = new TargetHelper(this.calcObj.getDataSource(), targetSample.getValueAsString());
                    openList.add(new UserSelection(UserSelection.SelectionType.Attribute, th.getAttribute().get(0), null, null));
                }
            }

            List<JEVisClass> classes = new ArrayList<>();

            for (String className : TreeSelectionDialog.allData) {
                classes.add(calcObj.getDataSource().getJEVisClass(className));
            }

            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(calcObj.getDataSource(), classes, SelectionMode.SINGLE, openList, true);
            final JEVisObject out = outputObj;
            selectionDialog.setOnCloseRequest(event -> {
                try {
                    if (selectionDialog.getResponse() == Response.OK) {
                        for (UserSelection us : selectionDialog.getUserSelection()) {
                            JEVisObject outputObject = out;
                            JEVisAttribute targetAtt = us.getSelectedAttribute();
                            if (targetAtt == null) {
                                targetAtt = us.getSelectedObject().getAttribute("Value");
                            }
                            TargetHelper th = new TargetHelper(this.calcObj.getDataSource(), us.getSelectedObject(), targetAtt);

                            if (th.isValid() && th.targetObjectAccessible()) {

                                /**
                                 *  Create an new target Object if not exists
                                 */
                                try {
                                    if (outputObject == null) {
                                        JEVisObject newObject = this.calcObj.buildObject(CalculationNameFormatter.createVariableName(us.getSelectedObject()), outputClass);
                                        newObject.setLocalNames(us.getSelectedObject().getLocalNameList());
                                        newObject.commit();
                                        outputObject = newObject;
                                    }
                                } catch (Exception ex) {
                                    logger.error(ex);
                                    ExceptionDialog2.showException(JEConfig.getStage(), ex);
                                }

                                /**
                                 * Add an new Sample with the new target
                                 */

                                JEVisSample newSample = outputObject.getAttribute("Output").buildSample(new DateTime(), th.getSourceString());
                                newSample.commit();

                                /** update output variable name **/
                                if (!outputObject.getName().equals(CalculationNameFormatter.createVariableName(us.getSelectedObject()))) {
                                    outputObject.setName(CalculationNameFormatter.createVariableName(us.getSelectedObject()));
                                    outputObject.commit();
                                }
                                outputButton.setText(th.getObject().get(0).getName() + "." + th.getAttribute().get(0).getName());

                            }
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            selectionDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setCalculation(JEVisObject calcObj) {
        this.calcObj = calcObj;
        try {
            JEVisAttribute expression = calcObj.getAttribute("Expression");

            JEVisSample expVal = expression.getLatestSample();
            if (expVal != null) {

                textArea.setText(expVal.getValueAsString());
            }
            updateVariables();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
