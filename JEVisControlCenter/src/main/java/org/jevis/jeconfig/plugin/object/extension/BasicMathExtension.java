/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.extension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.application.dialog.SelectTargetDialog;
import org.jevis.commons.dataprocessing.v2.DataProcessing;
import org.jevis.commons.object.plugin.FomulaInput;
import org.jevis.commons.object.plugin.Input;
import org.jevis.commons.object.plugin.JsonInput;
import org.jevis.commons.object.plugin.JsonVirtualCalc;
import org.jevis.commons.object.plugin.VirtualSumData;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.joda.time.DateTime;

/**
 * TODO: has to be an JEConfig Driver at runtime TODO: Delete?
 *
 * @author Florian Simon
 */
public class BasicMathExtension implements ObjectEditorExtension {

    public static String TITLE = "Calculation Editor";
    private JEVisObject _obj;

    private AnchorPane _rootPane = new AnchorPane();
    private VirtualSumData virtualSum;
    private SimpleBooleanProperty _needSave = new SimpleBooleanProperty(false);

    public BasicMathExtension(JEVisObject obj) {
        _obj = obj;

    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            return obj.getJEVisClass().getName().equals("List Calculation");
        } catch (JEVisException ex) {
            Logger.getLogger(BasicMathExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public Node getView() {
        return _rootPane;
    }

    @Override
    public void setVisible() {
        virtualSum = initData();
        initGUI();
    }

    private VirtualSumData initData() {
        try {
            String jsonString = _obj.getAttribute("Formula").getLatestSample().getValueAsString();
            if (jsonString != null && !jsonString.isEmpty()) {
                System.out.println("use existing sum");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonVirtualCalc virMathJson = gson.fromJson(jsonString, JsonVirtualCalc.class);
                return new VirtualSumData(_obj.getDataSource(), virMathJson);
            } else {
                System.out.println("create emty sum");
                return new VirtualSumData();
            }

        } catch (JEVisException ex) {
            Logger.getLogger(BasicMathExtension.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void initGUI() {
        try {

//            _rootPane = new AnchorPane();
            _rootPane.getChildren().removeAll(_rootPane.getChildren());

            AnchorPane.setTopAnchor(_rootPane, 10.0);
            AnchorPane.setLeftAnchor(_rootPane, 10.0);
            AnchorPane.setRightAnchor(_rootPane, 0.0);
            AnchorPane.setBottomAnchor(_rootPane, 0.0);

            List<String> operationNames = new ArrayList<>();
            operationNames.add(VirtualSumData.Operator.PLUS.name());
            operationNames.add(VirtualSumData.Operator.MINUS.name());
            operationNames.add(VirtualSumData.Operator.TIMES.name());
            operationNames.add(VirtualSumData.Operator.DIVIDED.name());

            final ChoiceBox operation = new ChoiceBox();
            operation.setMaxWidth(500);
            operation.setPrefWidth(160);

            ObservableList<String> items = FXCollections.observableList(operationNames);

//            operation.selectionModelProperty().addListener(new ChangeListener() {
//
//                @Override
//                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                    System.out.println("new operator: " + newValue.toString());
//                    virtualSum.setOperator(VirtualSumData.Operator.valueOf(newValue.toString()));
//                }
//            });
            operation.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    System.out.println("new operator: " + newValue.toString());
                    virtualSum.setOperator(VirtualSumData.Operator.valueOf(newValue.toString()));
                    _needSave.set(true);
                }
            });
            operation.setItems(items);
            operation.getSelectionModel().select(virtualSum.getOperator().name());
            virtualSum.getResult(new DateTime(2015, 4, 1, 0, 0), new DateTime(2015, 4, 2, 0, 0));
//            for (JEVisSample sample : sd.getResult(new DateTime(2015, 4, 1, 0, 0), new DateTime(2015, 4, 2, 0, 0))) {
//                System.out.println("R: " + sample.getTimestamp() + " v: " + sample.getValueAsDouble());
//            }

            VBox mainPane = new VBox();
            mainPane.setSpacing(7);
            mainPane.setPadding(new Insets(10));

//            HBox operationBox = new HBox(7);
            Label opLabel = new Label("Operation:");
            opLabel.setLabelFor(operation);

            operation.setPrefHeight(opLabel.getPrefHeight());

//            operationBox.getChildren().add(opLabel);
//            operationBox.getChildren().add(operation);
//            opLabel.setAlignment(Pos.BOTTOM_LEFT);
            GridPane headerPane = new GridPane();
            headerPane.setHgap(7);
            headerPane.setVgap(7);
            headerPane.add(opLabel, 0, 0);
            headerPane.add(operation, 1, 0);

//            Label dpLabel = new Label("Data Points:");
            GridPane inputPane = new GridPane();
            inputPane.setHgap(7);
            inputPane.setVgap(7);

            Label columnObj = new Label("Input Object");
            Label columnAtt = new Label("Attribute");
            Label columnWork = new Label("Workflow");

            int coloums = 4;

            inputPane.add(columnObj, 0, 0);
            inputPane.add(columnAtt, 1, 0);
            inputPane.add(columnWork, 2, 0);

            inputPane.add(new Separator(Orientation.HORIZONTAL), 0, 1, coloums, 1);

            int row = 2;
            for (Input input : virtualSum.getInputs()) {

                try {
                    String objName = "";
                    String attName = "";
//                    String workName = "";
                    final ChoiceBox workName = new ChoiceBox();
                    try {
                        objName = input.getObject().getName();
                    } catch (Exception ex) {
                        System.out.println("Missing object");
//                    ex.printStackTrace();
                    }
                    try {
                        attName = input.getAttribute().getName();
                    } catch (Exception ex) {
                        System.out.println("missing attribute");
//                    ex.printStackTrace();
                    }
                    try {
//                        workName = input.getWorkflowID();

                        List<String> workflows = new ArrayList<>();
                        for (String wName : DataProcessing.GetConfiguredWorkflowNames(input.getAttribute())) {
                            workflows.add(wName);
                        }

                        workName.setMaxWidth(500);
                        workName.setPrefWidth(160);

                        ObservableList<String> workflowsItems = FXCollections.observableList(workflows);

                        workName.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                            @Override
                            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                                input.setWorkflowID(newValue.toString());
                                _needSave.set(true);//check if its the same as saved before
                            }
                        });

//                        workName.selectionModelProperty().addListener(new ChangeListener() {
//
//                            @Override
//                            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                                input.setWorkflowID(newValue.toString());
//                            }
//                        });
                        workName.setItems(workflowsItems);
                        if (input.getWorkflowID() != null && !input.getWorkflowID().isEmpty()) {
                            try {
                                workName.getSelectionModel().select(input.getWorkflowID());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                    } catch (Exception ex) {
                        System.out.println("missing workflow");
//                    ex.printStackTrace();
                    }

                    Button remove = new Button();
                    remove.setGraphic(JEConfig.getImage("list-remove.png", 10, 10));

                    inputPane.add(new Label(objName), 0, row);
                    inputPane.add(new Label(attName), 1, row);
                    inputPane.add(workName, 2, row);
                    inputPane.add(remove, 3, row);

                    remove.setOnAction(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent event) {

                            List<Input> inputs = virtualSum.getInputs();
                            inputs.remove(input);
                            _needSave.setValue(Boolean.TRUE);
                            initGUI();
                        }
                    });

                } catch (Exception hmm) {
                    inputPane.add(new Label("*error*"), 0, row);
                }
                row++;
            }

            Button addButton = new Button("Add Input");

            addButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        System.out.println("add pressed");
                        SelectTargetDialog sd = new SelectTargetDialog();
                        if (sd.show(JEConfig.getStage(), _obj.getDataSource()) == SelectTargetDialog.Response.OK) {
                            JEVisObject selectedObj = sd.getUserSelection().get(0).getSelectedObject();
                            JEVisAttribute selectedAtt = sd.getUserSelection().get(0).getSelectedAttribute();

                            FomulaInput newInput = new FomulaInput();
                            System.out.println("setObject: " + selectedObj);
                            newInput.setObject(selectedAtt.getObject());
                            newInput.setAttribute(selectedAtt);
                            newInput.setId("new ID");//Ask User
                            newInput.setWorkflow("Default");

                            List<Input> inputs = virtualSum.getInputs();
                            inputs.add(newInput);

                            _needSave.set(true);
                            initGUI();
                        }
                    } catch (JEVisException ex) {
                        Logger.getLogger(BasicMathExtension.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            Region spacer = new Region();
            spacer.setPrefHeight(20);

            mainPane.getChildren().addAll(
                    headerPane,
                    //                    dpLabel,
                    //                    new Separator(Orientation.HORIZONTAL),
                    spacer,
                    inputPane,
                    new Separator(Orientation.HORIZONTAL),
                    addButton);

            _rootPane.getChildren().add(mainPane);

        } catch (Exception ex) {
            Logger.getLogger(BasicMathExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String workaround() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            JsonVirtualCalc test = new JsonVirtualCalc();
//            test.setVersion("1.0");
//            test.setOperator("+");
//
//            JsonInput i1 = new JsonInput();
//            i1.setAttribute("Value");
//            i1.setObject(1256);
//            i1.setId("Input A");
//            i1.setWorkflow("default");
//
//            JsonInput i2 = new JsonInput();
//            i2.setAttribute("Value");
//            i2.setObject(1258);
//            i2.setId("Input B");
//            i2.setWorkflow("default");
//
//            List<JsonInput> ins = new ArrayList<>();
//            ins.add(i1);
//            ins.add(i2);
//
//            test.setInputs(ins);
//
//            System.out.println("Test String:");
//            System.out.println(gson.toJson(test));

        // return "{\"calc\":{\"version\":\"1\",\"operator\":\"*\",\"inputs\":{\"input\":[{\"id\":\"a\",\"object\":\"1256\",\"attribute\":\"Value\",\"workflow\":\"default\"},{\"id\":\"b\",\"object\":\"1258\",\"attribute\":\"Value\",\"workflow\":\"default\"}]}}}";
//        return "{\"version\":\"1\",\"operator\":\"*\",\"inputs\":{\"input\":[{\"id\":\"a\",\"object\":\"1256\",\"attribute\":\"Value\",\"workflow\":\"default\"},{\"id\":\"b\",\"object\":\"1258\",\"attribute\":\"Value\",\"workflow\":\"default\"}]}}";
        return "{\"operator\":\"+\",\"version\":\"1.0\",\"inputs\":[{\"object\":1256,\"attribute\":\"Value\",\"workflow\":\"default\",\"id\":\"InputA\"},{\"object\":1258,\"attribute\":\"Value\",\"workflow\":\"default\",\"id\":\"InputB\"}]}";
    }

    @Override
    public String getTitel() {
        return TITLE;
    }

    @Override
    public boolean needSave() {
        return _needSave.getValue();

    }

    @Override
    public boolean save() {
        System.out.println("Save");
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonVirtualCalc jsonSum = new JsonVirtualCalc();
            List<JsonInput> jsonInputs = new ArrayList<>();//need Factory
            for (Input in : virtualSum.getInputs()) {
                JsonInput jin = new JsonInput();
                jin.setObject(in.getObject().getID());
                jin.setAttribute(in.getAttribute().getName());
                jin.setWorkflow(in.getWorkflowID());
                jin.setId(in.getID());
                jsonInputs.add(jin);
            }

            jsonSum.setInputs(jsonInputs);
            jsonSum.setOperator(virtualSum.getOperator().name());
            jsonSum.setVersion(virtualSum.getVersion());

            String json = gson.toJson(jsonSum, JsonVirtualCalc.class);

            JEVisSample sample = _obj.getAttribute("Formula").buildSample(new DateTime(), json);
            sample.commit();
            System.out.println("Saved Jon: " + sample.getValueAsString());
            _needSave.setValue(Boolean.FALSE);
            return true;

        } catch (JEVisException ex) {
            Logger.getLogger(BasicMathExtension.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _needSave;
    }

    @Override
    public void dismissChanges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
