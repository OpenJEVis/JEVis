package org.jevis.jecc.dialog;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.control.KPIVariable;
import org.jevis.jecc.application.resource.ResourceLoader;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KPIWizard extends Dialog {
    private static final Logger logger = LogManager.getLogger(KPIWizard.class);
    private final List<KPIVariable> variables = new ArrayList<>();
    private ObjectRelations objectRelations;
    private int index = 0;

    public KPIWizard(JEVisObject object) {
        super();
        setTitle(I18n.getInstance().getString("plugin.configuration.kpiwizard.title"));
        setHeaderText(I18n.getInstance().getString("plugin.configuration.kpiwizard.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        JEVisClass calcClass = null;
        JEVisClass dataClass = null;
        JEVisClass cleanDataClass = null;
        JEVisClass inputClass = null;
        JEVisClass outputClass = null;
        try {
            objectRelations = new ObjectRelations(object.getDataSource());
            calcClass = object.getDataSource().getJEVisClass("Calculation");
            dataClass = object.getDataSource().getJEVisClass("Data");
            cleanDataClass = object.getDataSource().getJEVisClass("Clean Data");
            inputClass = object.getDataSource().getJEVisClass("Input");
            outputClass = object.getDataSource().getJEVisClass("Output");

        } catch (JEVisException e) {
            e.printStackTrace();
        }

        List<JEVisObject> objects = getAllChildren(object);

        MFXTextField name = new MFXTextField();
        name.setFloatMode(FloatMode.DISABLED);
        name.setPromptText("Enter Name for created Folder Structure");
        TextArea formula = new TextArea();
        formula.setPromptText("Enter formula (Var1, Var2, ... VarX)");
        MFXButton add = new MFXButton("", ResourceLoader.getImage("list-add.png", 15, 15));
        FlowPane variablePane = new FlowPane();
        variablePane.setHgap(6);
        variablePane.setVgap(6);
        ScrollPane scrollPane = new ScrollPane(variablePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinSize(900, 650);

        add.setOnAction(event -> {
            KPIVariable kpiVariable = new KPIVariable(objects, objectRelations, index);
            kpiVariable.getVariableButton().setOnAction(event1 -> formula.setText("#{" + formula.getText() + "Var" + kpiVariable.getIndex() + "}"));
            variables.add(kpiVariable);
            variablePane.getChildren().add(kpiVariable);
            index++;
        });

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        JEVisClass finalCalcClass = calcClass;
        JEVisClass finalInputClass = inputClass;
        JEVisClass finalDataClass = dataClass;
        JEVisClass finalCleanDataClass = cleanDataClass;
        JEVisClass finalOutputClass = outputClass;
        okButton.setOnAction(event -> {
            if (!name.getText().equals("") && checkMatchingListContent()) {
                try {
                    JEVisObject nearestBuilding = CommonMethods.getFirstParentalObjectOfClass(object, "Building");
                    JEVisObject calculationDirectory = null;
                    JEVisObject dataDirectory = null;
                    for (JEVisObject child : nearestBuilding.getChildren()) {
                        if (child.getJEVisClassName().equals("Calculation Directory")) {
                            calculationDirectory = child;
                        } else if (child.getJEVisClassName().equals("Data Directory")) {
                            dataDirectory = child;
                        }
                    }

                    if (calculationDirectory != null && object.getDataSource().getCurrentUser().canCreate(calculationDirectory.getID())
                            && dataDirectory != null && object.getDataSource().getCurrentUser().canCreate(dataDirectory.getID())) {

                        JEVisObject calcDir = calculationDirectory.buildObject(name.getText(), calculationDirectory.getJEVisClass());
                        calcDir.commit();
                        JEVisObject dataDir = dataDirectory.buildObject(name.getText(), dataDirectory.getJEVisClass());
                        dataDir.commit();

                        KPIVariable notForAll = variables.stream().filter(kpiVariable -> !kpiVariable.getUseOneForAll().isSelected()).findFirst().orElse(null);
                        KPIVariable useForName = variables.stream().filter(kpiVariable -> kpiVariable.getUseForName().isSelected()).findFirst().orElse(null);

                        if (notForAll != null && notForAll.getSelectedItems() != null) {
                            for (int i = 0; i < notForAll.getSelectedItems().size(); i++) {
                                String kpiName = "KPI - " + useForName.getSelectedItems().get(i).getName();
                                JEVisObject calculation = calcDir.buildObject(kpiName, finalCalcClass);
                                calculation.commit();
                                JEVisAttribute calculationEnabledAttribute = calculation.getAttribute("Enabled");
                                calculationEnabledAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), true)));
                                JEVisAttribute expressionAttribute = calculation.getAttribute("Expression");
                                expressionAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), formula.getText())));

                                Period p = null;
                                for (KPIVariable kpiVariable : variables) {
                                    JEVisObject target = null;
                                    if (kpiVariable.getSelectedItems().size() == notForAll.getSelectedItems().size()) {
                                        target = kpiVariable.getSelectedItems().get(i);
                                    } else if (kpiVariable.getUseOneForAll().isSelected()) {
                                        target = kpiVariable.getSelectedItems().get(0);
                                    }

                                    p = new Period(target.getAttribute("Period").getLatestSample().getValueAsString());
                                    String varName = target.getName();
                                    JEVisObject input = calculation.buildObject(varName, finalInputClass);
                                    input.commit();

                                    if (target.getJEVisClassName().equals("Data")) {
                                        for (JEVisObject child : target.getChildren()) {
                                            if (child.getJEVisClassName().equals("Clean Data")) {
                                                JEVisAttribute childPeriodAttribute = child.getAttribute("Period");
                                                Period pChild = new Period(childPeriodAttribute.getLatestSample().getValueAsString());
                                                if (pChild.equals(p)) {
                                                    target = child;
                                                    break;
                                                }
                                            }
                                        }

                                    }

                                    JEVisAttribute identifierAttribute = input.getAttribute("Identifier");
                                    identifierAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), "Var" + kpiVariable.getIndex())));
                                    JEVisAttribute inputDataAttribute = input.getAttribute("Input Data");
                                    inputDataAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), target.getID() + ":Value")));
                                    JEVisAttribute inputDataType = input.getAttribute("Input Data Type");
                                    if (!p.equals(Period.ZERO)) {
                                        inputDataType.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), "PERIODIC")));
                                    } else {
                                        inputDataType.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), "NON_PERIODIC")));
                                    }
                                }

                                JEVisObject outputData = dataDir.buildObject(kpiName, finalDataClass);
                                outputData.commit();
                                JEVisAttribute periodAttribute = outputData.getAttribute("Period");
                                periodAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), p)));

                                JEVisObject cleanDataObject = outputData.buildObject(I18n.getInstance().getString("tree.treehelper.cleandata.name"), finalCleanDataClass);
                                cleanDataObject.commit();

                                JEVisAttribute cleanDataEnabledAttribute = cleanDataObject.getAttribute("Enabled");
                                cleanDataEnabledAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), true)));
                                JEVisAttribute valueIsAQuantityAttribute = cleanDataObject.getAttribute("Value is a Quantity");
                                valueIsAQuantityAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), false)));

                                JEVisObject output = calculation.buildObject(kpiName, finalOutputClass);
                                output.commit();

                                JEVisAttribute outputAttribute = output.getAttribute("Output");
                                outputAttribute.addSamples(Collections.singletonList(calculationEnabledAttribute.buildSample(new DateTime(), outputData.getID() + ":Value")));

                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.WARNING, "Not enough objects in " + "Var" + notForAll.getIndex());
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.notallowed"));
                        alert.showAndWait();
                    }

                } catch (JEVisException e) {
                    logger.error("Could not commit.");
                }
                close();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Input Lists aren't matching");
                alert.showAndWait();
            }

        });

        cancelButton.setOnAction(event -> close());

        VBox vBox = new VBox(6, name, formula, add, scrollPane, separator);
        vBox.setFillWidth(true);
        vBox.setPadding(new Insets(6));

        getDialogPane().setContent(vBox);
    }

    private boolean checkMatchingListContent() {
        boolean matching = true;
        int size = 0;

        for (KPIVariable kpiVariable : variables) {
            if (variables.indexOf(kpiVariable) == 0 && !kpiVariable.getUseOneForAll().isSelected()) {
                size = kpiVariable.getSelectedItems().size();
            } else if (kpiVariable.getSelectedItems().size() != size && !kpiVariable.getUseOneForAll().isSelected()) {
                matching = false;
                break;
            } else {
                size = kpiVariable.getSelectedItems().size();
            }
        }

        return matching;
    }

    private List<JEVisObject> getAllChildren(JEVisObject object) {
        List<JEVisObject> children = new ArrayList<>();
        try {
            children.addAll(object.getChildren());

            for (JEVisObject child : object.getChildren()) {
                children.addAll(getAllChildren(child));
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return children;
    }

}
