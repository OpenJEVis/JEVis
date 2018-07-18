package org.jevis.jeconfig.plugin.object.extension.calculation;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import net.sourceforge.jeval.Evaluator;
import org.jevis.api.*;

import java.util.ArrayList;
import java.util.List;

public class FormelBox extends HBox {

    //    TextArea textArea = new TextArea();
    TextArea textArea = new TextArea();
    Label errorArea = new Label();
    private List<String> variables = new ArrayList<>();
    private JEVisObject calcObj;

    public FormelBox() {
        super();
        setSpacing(5);

//        textArea.setPrefHeight(200);
//        errorArea.setPrefWidth(200);
        errorArea.setTextFill(Color.FIREBRICK);
        textArea.setWrapText(true);
        getChildren().addAll(textArea);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                eval();
            }
        });

    }


    public String getExpression(){
        return textArea.getText();
    }

    public void addExpression(String expression) {
        if (expression != null && !expression.isEmpty()) {
            int caret = textArea.getCaretPosition();
            String beforeText = textArea.getText().substring(0, caret);
            String afterText = textArea.getText().substring(caret, textArea.getText().length());
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
            alert.setTitle("Erfolg");
            alert.setHeaderText(null);
            alert.setContentText("Keinen Fehler gefunden.");

            alert.showAndWait();

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText(getErrorMessage(ex));
            alert.showAndWait();


            ex.printStackTrace();
        }

    }

    private String getErrorMessage(Exception ex) {
        String message = "Unknows error";
        try {
            message = ex.getCause().toString();
        } catch (NullPointerException np) {
            message = ex.getMessage();
        }


        return message;
    }


    public void updateVariables() throws JEVisException {
        JEVisClass input = calcObj.getDataSource().getJEVisClass("Input");
        for (JEVisObject inpuObject : calcObj.getChildren()) {
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

    public void setCalculation(JEVisObject calcObj) {
        this.calcObj=calcObj;
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

    public void setExpression(String expression) {
        Evaluator eval = new Evaluator();

        textArea.setText(expression);
//            eval();

    }



}
