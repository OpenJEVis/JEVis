package org.jevis.jeconfig.plugin.dashboard.datahandler;

import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.utils.Benchmark;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;

import java.util.ArrayList;
import java.util.List;

public class TreeManager {

    private static TreeManager treeManager = null;
    private WidgetTreePlugin selectionTree;
    private List<JEVisObject> openObjectsList;
    private JEVisTree jeVisTree;

    public static TreeManager getInstance() {
        if (treeManager == null)
            treeManager = new TreeManager();

        return treeManager;
    }

    public void setSelectionTree(WidgetTreePlugin selectionTree) {
        this.selectionTree = selectionTree;
    }

    public WidgetTreePlugin getSelectionTree(JEVisDataSource dataSource) {
        if (selectionTree == null) {
            Benchmark benchmark = new Benchmark();
            selectionTree = new WidgetTreePlugin();
            benchmark.printBenchmarkDetail("Done WTP");
            jeVisTree = JEVisTreeFactory.buildDefaultWidgetTree(dataSource, selectionTree);
            benchmark.printBenchmarkDetail("Done Tree");
            openObjectsList = new ArrayList<>();
        }
        return selectionTree;
    }

    public List<JEVisObject> getOpenObjectsList() {
        return openObjectsList;
    }

    public void setOpenObjectsList(List<JEVisObject> openObjectsList) {
        this.openObjectsList = openObjectsList;
    }

    public JEVisTree getTree() {
        return jeVisTree;
    }

}
