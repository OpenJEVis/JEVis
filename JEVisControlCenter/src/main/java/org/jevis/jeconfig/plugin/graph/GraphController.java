/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph;

import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;

/**
 *
 * @author broder
 */
public class GraphController {

    private final GraphPluginView view;
    private final GraphDataModel model;

    public GraphController(GraphPluginView view, GraphDataModel model) {
        this.view = view;
        this.model = model;
    }
}
