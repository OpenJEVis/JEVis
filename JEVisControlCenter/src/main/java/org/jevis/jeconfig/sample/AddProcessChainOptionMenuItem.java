/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sample;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.BasicOption;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessChain;
import org.jevis.commons.dataprocessing.ProcessChains;
import org.jevis.commons.dataprocessing.ProcessFunction;
import org.jevis.commons.dataprocessing.ProcessOption;
import org.jevis.commons.dataprocessing.ProcessOptions;

/**
 *
 * @author Florian Simon
 */
public class AddProcessChainOptionMenuItem extends Menu {

    public AddProcessChainOptionMenuItem() {
    }

    private JEVisOption ProcessToJEVisOption(JEVisOption parentOpt, Process pf) {
        if(parentOpt.getKey().equals("Data Processing")){
            System.out.println("");
        }
        
//        JEVisOption jo = 
//        
//        
//        
//        if (parentOpt != null) {
//            parentOpt.addOption(jo, true);
//        }

        for (ProcessOption po : pf.getOptions()) {
            JEVisOption pjo = new BasicOption();
            pjo.setKey(po.getKey());
            pjo.setValue(po.getValue());
            parentOpt.addOption(pjo, false);
        }

        if (pf.getSubProcesses() != null) {
            for (Process sub : pf.getSubProcesses()) {
                JEVisOption subObt = new BasicOption();
                subObt.setKey(sub.getID());  
//                subObt.setValue(sub.get);
                parentOpt.addOption(subObt, false);
                ProcessToJEVisOption(subObt, sub);
            }
        }

        return null;
    }

    private JEVisOption ProcessToJEVisOption(ProcessOption po) {
        JEVisOption jo = new BasicOption(po.getKey(), po.getValue(), "");
        return jo;
    }

    public AddProcessChainOptionMenuItem(TreeTableView<JEVisOption> treeview, JEVisDataSource ds) {
        super("Add ProcessChain");
//        ProcessOptions.

        try {
            List<ProcessChain> processes = ProcessChains.getAvailableProcessChains(ds);

            for (ProcessChain chain : processes) {
                MenuItem addProcess = new MenuItem(chain.getName());
                addProcess.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            Process pf = ProcessChains.getProcessChain(chain.getObject());

                            JEVisOption parentOpt = treeview.getSelectionModel().getSelectedItem().getValue();

                            JEVisOption jeop = ProcessToJEVisOption(parentOpt, pf);

//                            parentOpt.addOption(jeop, false);
                            
//                            ProcessOptions.ption(pf, key, defaultOption)
                        } catch (JEVisException ex) {
                            Logger.getLogger(AddProcessChainOptionMenuItem.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                getItems().add(addProcess);

            }

        } catch (JEVisException jex) {
            jex.printStackTrace();
        }

//        setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//
//                try {
//                    TreeItem<JEVisOption> treeItem = treeview.getSelectionModel().getSelectedItem();
//                    JEVisOption newOption = new BasicOption();
//                    newOption.setKey("New Option");
//
//                    treeItem.getValue().addOption(newOption, true);
//
//                    TreeItem<JEVisOption> newItem = new TreeItem<>(newOption);
//                    treeItem.getChildren().add(newItem);
//                } catch (Exception ex) {
//                    System.out.println("Error while deleting option: " + ex);
//                }
//            }
//        });
    }

}
