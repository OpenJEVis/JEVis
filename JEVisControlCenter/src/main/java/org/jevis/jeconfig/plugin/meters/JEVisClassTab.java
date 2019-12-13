package org.jevis.jeconfig.plugin.meters;

import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import org.jevis.api.JEVisClass;

public class JEVisClassTab extends Tab {

    private final JEVisClass jeVisClass;

    public JEVisClassTab(JEVisClass jeVisClass) {
        super();
        this.jeVisClass = jeVisClass;
    }

    public JEVisClassTab(String className, TableView<MeterRow> tableView, JEVisClass jeVisClass) {
        super(className, tableView);
        this.jeVisClass = jeVisClass;
    }

    public JEVisClass getJeVisClass() {
        return jeVisClass;
    }
}
