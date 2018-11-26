/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.graph.view.ToolBarView;

/**
 * @author broder
 */
public class ToolBarController implements EventHandler {

    private final JEVisDataSource ds;
    private GraphDataModel model;
    private ToolBarView view;
    private ChartSelectionDialog selectionDialog;

    public ToolBarController(ToolBarView view, GraphDataModel model, JEVisDataSource ds) {
        this.model = model;
        this.view = view;
        this.ds = ds;
    }

    @Override
    public void handle(Event event) {
        if (selectionDialog == null) {
            selectionDialog = new ChartSelectionDialog(ds, model);
        }

        if (selectionDialog.show(new Stage()) == ChartSelectionDialog.Response.OK) {


        }
    }

}
