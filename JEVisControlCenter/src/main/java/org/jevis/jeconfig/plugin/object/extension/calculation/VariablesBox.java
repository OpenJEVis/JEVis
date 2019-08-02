package org.jevis.jeconfig.plugin.object.extension.calculation;

import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.application.jevistree.TreeHelper;
import org.jevis.jeconfig.application.resource.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

public class VariablesBox extends FlowPane {


    private List<JEVisObject> variables = new ArrayList<>();
    private FormulaBox expression;

    public VariablesBox() {
        super();
//        setSpacing(5);
        setVgap(8);
        setHgap(4);
        setPrefWrapLength(200);
    }

    public void bindVariableBox(FormulaBox expression, JEVisObject obj) {
        this.expression = expression;
        listVariables(obj);
    }


    private void buildVarButton(JEVisObject inputObject) {

        try {
            Button button = new Button();
            button.setFocusTraversable(false);
            JEVisAttribute id = inputObject.getAttribute("Identifier");
            if (id != null) {
                JEVisSample value = id.getLatestSample();
                if (value != null && !value.getValueAsString().isEmpty()) {
                    String name = value.getValueAsString();
                    button.setText(name);

                    button.setOnAction(event -> expression.addExpression("#{" + name + "}"));
                } else {
                    button.setText("*no name* ID: " + inputObject.getID());
                    button.disableProperty().setValue(true);
                }

                getChildren().add(button);
            }

        } catch (Exception np) {
            np.printStackTrace();
        }
    }

    private void listVariables(JEVisObject obj) {
        getChildren().clear();

        Button addInputButton = new Button("", ResourceLoader.getImage("list-add.png", 15, 15));
        addInputButton.setOnAction(event -> {
            try {
                TreeHelper.createCalcInput(obj, null);
                listVariables(obj);
                expression.updateVariables();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        getChildren().add(addInputButton);

        try {
            JEVisClass input = obj.getDataSource().getJEVisClass("Input");
            for (JEVisObject var : obj.getChildren()) {

                buildVarButton(var);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
