/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.dialog.GraphSelectionDialog;
import org.jevis.application.jevistree.plugin.BarchartPlugin;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.ToolBarController;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;

/**
 *
 * @author broder
 */
public class ToolBarView {

    private GraphDataModel model;
    private ToolBarController controller;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds) {
        this.model = model;
        this.controller = new ToolBarController(this, model, ds);
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        //load basic stuff
        double iconSize = 20;
        ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
        ToggleButton delete = new ToggleButton("", JEConfig.getImage("list-remove.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        Separator sep1 = new Separator();
        save.setDisable(true);
        newB.setDisable(true);
        delete.setDisable(true);

//load new stuff
        Button select = new Button("", JEConfig.getImage("Data.png", iconSize, iconSize));
//        select.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//                controller.handleSelectRequest(ds);
//            }
//
//        });
        select.setOnAction(controller);

        toolBar.getItems().addAll(save, newB, delete, sep1, select);
        return toolBar;
    }
}
