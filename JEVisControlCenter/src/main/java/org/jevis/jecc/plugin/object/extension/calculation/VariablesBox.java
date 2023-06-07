package org.jevis.jecc.plugin.object.extension.calculation;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.layout.FlowPane;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.application.jevistree.TreeHelper;

import java.util.ArrayList;
import java.util.List;

public class VariablesBox extends FlowPane {


    private final List<JEVisObject> variables = new ArrayList<>();
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
            MFXButton button = new MFXButton();
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

    public void listVariables(JEVisObject obj) {
        getChildren().clear();

        MFXButton addInputButton = new MFXButton("", ControlCenter.getSVGImage(Icon.PLUS_CIRCLE, 15, 15));
        addInputButton.setOnAction(event -> {
            try {
                TreeHelper.createCalcInput(obj, null, this, expression);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        getChildren().add(addInputButton);

        try {
            JEVisClass inputClass = obj.getDataSource().getJEVisClass(JC.Input.name);
            for (JEVisObject var : obj.getChildren()) {
                if (var.getJEVisClass().equals(inputClass)) {
                    buildVarButton(var);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
