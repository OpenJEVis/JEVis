package org.jevis.jeconfig.plugin.object.extension.calculation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import net.sourceforge.jeval.Evaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.dialog.ExceptionDialog2;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class FormulaBox extends HBox {

    private static final Logger logger = LogManager.getLogger(FormulaBox.class);
    private final List<String> variables = new ArrayList<>();
    private StackPane dialogContainer;
    Label errorArea = new Label();
    //    JFXTextArea textArea = new TextArea();
    JFXTextArea textArea = new JFXTextArea();
    private JEVisObject calcObj;
    private JFXButton outputButton;

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

            String value = eval.evaluate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.title"));
            alert.setHeaderText(null);
            alert.setContentText(I18n.getInstance().getString("plugin.object.extension.calculation.formulabox.eval.success"));

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

    public void setOutputButton(JFXButton buttonOutput) {
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
            JEVisClass outputClass = this.calcObj.getDataSource().getJEVisClass("Output");
            List<JEVisObject> outputs = this.calcObj.getChildren(outputClass, true);
            List<UserSelection> openList = new ArrayList<>();
            JEVisObject outputObj = null;

            if (!outputs.isEmpty()) {//there can only be one output
                outputObj = outputs.get(0);
                JEVisAttribute targetAttribute = outputObj.getAttribute("Output");
                JEVisSample targetSample = targetAttribute.getLatestSample();

                if (targetSample != null) {
                    TargetHelper th = new TargetHelper(this.calcObj.getDataSource(), targetSample.getValueAsString());
                    openList.add(new UserSelection(UserSelection.SelectionType.Attribute, th.getAttribute().get(0), null, null));
                }
            }
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
            allFilter.add(basicFilter);

            SelectTargetDialog selectionDialog = new SelectTargetDialog(dialogContainer, allFilter, basicFilter, null, SelectionMode.SINGLE, calcObj.getDataSource(), openList);
            final JEVisObject out = outputObj;
            selectionDialog.setOnDialogClosed(event -> {
                try {
                    if (selectionDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        for (UserSelection us : selectionDialog.getUserSelection()) {
                            JEVisObject outputObject = out;
                            JEVisAttribute targetAtt = us.getSelectedAttribute();
                            if (targetAtt == null) {
                                targetAtt = us.getSelectedObject().getAttribute("Value");
                            }
                            TargetHelper th = new TargetHelper(this.calcObj.getDataSource(), us.getSelectedObject(), targetAtt);

                            if (th.isValid() && th.targetAccessible()) {

                                /**
                                 *  Create an new target Object if not exists
                                 */
                                try {
                                    if (outputObject == null) {
                                        JEVisObject newObject = this.calcObj.buildObject(CalculationNameFormatter.createVariableName(us.getSelectedObject()), outputClass);
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

    public void setDialogContainer(StackPane dialogContainer) {
        this.dialogContainer = dialogContainer;
    }
}
