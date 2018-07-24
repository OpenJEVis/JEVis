/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.extension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessChains;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.extension.processchain.FunctionPane;
import org.jevis.jeconfig.plugin.object.extension.processchain.ResultPane;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: has to be an JEConfig Driver at runtime
 *
 * @deprecated i guess this will not be used anymore
 * @author Florian Simon
 */
public class ProcessChainExtension implements ObjectEditorExtension {

    public static String TITLE = I18n.getInstance().getString("plugin.object.processchain.title");
    private JEVisObject _obj;

//    private AnchorPane _rootPane = new AnchorPane();
    private BorderPane _rootPane = new BorderPane();
    private Process task;
    private SimpleBooleanProperty _needSave = new SimpleBooleanProperty(false);

    public ProcessChainExtension(JEVisObject obj) {
        _obj = obj;

    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            return obj.getJEVisClass().getName().equals("Process Chain");
        } catch (JEVisException ex) {
            Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public Node getView() {
        return _rootPane;
    }

    @Override
    public void setVisible() {
        task = initData();
        initGUI();
    }

    private Process initData() {
        try {
            if (_obj.getAttribute("Data").hasSample()) {
                String jsonString = _obj.getAttribute("Data").getLatestSample().getValueAsString();
                if (jsonString != null && !jsonString.isEmpty()) {
                    System.out.println("use existing Task");

                    return ProcessChains.getProcessChain(_obj);
                }
            } else {
                //Workaround
                return ProcessChains.BuildExampleTask(_obj.getDataSource());
            }

        } catch (JEVisException ex) {
            Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void buildGridPlan(int lvl, Map<Integer, List<Process>> maxGrid, Process parentTask) {

        List<Process> taskOnLvl = maxGrid.get(lvl);
        if (taskOnLvl == null) {
            taskOnLvl = new ArrayList<>();
        }

        taskOnLvl.add(parentTask);

        for (Process childtask : parentTask.getSubProcesses()) {
//            taskOnLvl.add(childtask);
            buildGridPlan(lvl + 1, maxGrid, childtask);
        }
        maxGrid.put(lvl, taskOnLvl);

    }

    private void initGUI() {
        try {
            System.out.println("initGUI() ");

//            _rootPane = new AnchorPane();
            _rootPane.getChildren().removeAll(_rootPane.getChildren());

            ScrollPane sp = new ScrollPane();

            AnchorPane.setTopAnchor(sp, 10.0);
            AnchorPane.setLeftAnchor(sp, 10.0);
            AnchorPane.setRightAnchor(sp, 0.0);
            AnchorPane.setBottomAnchor(sp, 0.0);

//            Task chain = ProcessChains.BuildExampleTask(_obj.getDataSource());
            Map<Integer, List<Process>> maxGrid = new HashMap<>();
//            List<Task> rootTask = new ArrayList<>();
//            rootTask.add(task);
//
//            maxGrid.put(0, rootTask);

            GridPane graph = new GridPane();
            graph.setVgap(2);
//            graph.setGridLinesVisible(true); //debug

            HBox resultRow = new HBox(25);
            ResultPane rp = new ResultPane(task);
            rp.setOnAddAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        task = ProcessChains.BuildProcessChain(_obj.getDataSource(), rp.getSelectedNewSubFunction(), "New Function", null);
                        initGUI();
                    } catch (JEVisException ex) {
                        Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });

            Region resultlSpace = new Region();
            Region resultrSpace = new Region();

            HBox.setHgrow(resultRow, Priority.NEVER);
            HBox.setHgrow(resultlSpace, Priority.ALWAYS);
            HBox.setHgrow(resultrSpace, Priority.ALWAYS);

            resultRow.getChildren().addAll(resultlSpace, rp, resultrSpace);
            graph.add(resultRow, 0, 0);

            if (task != null) {
                buildGridPlan(0, maxGrid, task);

                for (Map.Entry<Integer, List<Process>> entrySet : maxGrid.entrySet()) {
                    Integer key = entrySet.getKey() + 1;
                    List<Process> value = entrySet.getValue();

                    HBox row = new HBox(25);
                    Region lSpace = new Region();
                    Region rSpace = new Region();
                    row.getChildren().add(lSpace);
                    for (Process t : value) {
                        FunctionPane fp = new FunctionPane(t);
                        row.getChildren().add(fp);

                        fp.setOnAddAction(new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent event) {
                                System.out.println("Task: " + t.getID() + " new Sub envent: " + fp.getSelectedNewSubFunction());
                                try {
                                    System.out.println("t.subs: " + t.getSubProcesses().size());
                                    Process newTask = ProcessChains.BuildProcessChain(_obj.getDataSource(), fp.getSelectedNewSubFunction(), "New Function", t);
//                                    t.getSubTasks().add(newTask);
                                    System.out.println("t.subs2: " + t.getSubProcesses().size());
                                    System.out.println("new Task: " + newTask.getID() + "  function: " + newTask.getFunction().getName());
                                    initGUI();

                                } catch (JEVisException ex) {
                                    Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (ClassNotFoundException ex) {
                                    Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (InstantiationException ex) {
                                    Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IllegalAccessException ex) {
                                    Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });

                        fp.setOnCloseAction(new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent event) {
                                if (t.getParent() != null) {
                                    t.getParent().getSubProcesses().remove(t);//Dont like it

                                } else {
                                    task = null;
                                    System.out.println("Has no parent: " + t);
                                }
                                initGUI();

                            }
                        });

                    }
                    row.getChildren().add(rSpace);

                    HBox.setHgrow(row, Priority.NEVER);
                    HBox.setHgrow(lSpace, Priority.ALWAYS);
                    HBox.setHgrow(rSpace, Priority.ALWAYS);

                    graph.add(row, 0, key);
                }
            }

            sp.setContent(graph);

            _rootPane.setPrefSize(2000, 2000);
            sp.setPrefSize(2000, 2000);

//            _rootPane.getChildren().add(sp);
            _rootPane.setCenter(sp);

        } catch (Exception ex) {
            Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean needSave() {
        return _needSave.getValue();

    }

    @Override
    public void dismissChanges() {
        _needSave.setValue(false);
        //TODO delete changes
    }

    @Override
    public boolean save() {
        System.out.println("Save");
        try {

            _needSave.setValue(Boolean.FALSE);
            return true;

        } catch (Exception ex) {
            Logger.getLogger(ProcessChainExtension.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _needSave;
    }

}
