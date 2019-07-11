/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.BasicOption;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessChain;
import org.jevis.commons.dataprocessing.ProcessChains;
import org.jevis.commons.dataprocessing.ProcessOption;

import java.io.IOException;
import java.util.List;

/**
 * @author Florian Simon
 */
public class AddProcessChainOptionMenuItem extends Menu {
    private static final Logger logger = LogManager.getLogger(AddProcessChainOptionMenuItem.class);

    public AddProcessChainOptionMenuItem() {
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
                            logger.fatal(ex);
                        } catch (IOException e) {
                            logger.fatal(e);
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
//                    logger.info("Error while deleting option: " + ex);
//                }
//            }
//        });
    }

    private JEVisOption ProcessToJEVisOption(ProcessOption po) {
        JEVisOption jo = new BasicOption(po.getKey(), po.getValue(), "");
        return jo;
    }

    private JEVisOption ProcessToJEVisOption(JEVisOption parentOpt, Process pf) {
        if (parentOpt.getKey().equals("Data Processing")) {
            logger.info("");
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

}
