package org.jevis.jeconfig.plugin.meters;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import org.jevis.api.JEVisClass;

public class JEVisClassTab extends Tab {

    private JEVisClass jeVisClass;
    private FilteredList<RegisterTableRow> filteredList;

    public JEVisClassTab() {
        super();
    }

    public JEVisClassTab(JEVisClass jeVisClass) {
        super();
        this.jeVisClass = jeVisClass;
    }

    public JEVisClassTab(String className, TableView<RegisterTableRow> tableView, JEVisClass jeVisClass) {
        super(className, tableView);
        this.jeVisClass = jeVisClass;
    }

    public void setClassName(String className) {
        setText(className);
    }

    public void setTableView(TableView<RegisterTableRow> tableView) {
        setContent(tableView);
    }

    public void setJEVisClass(JEVisClass jeVisClass) {
        this.jeVisClass = jeVisClass;
    }

    public JEVisClass getJeVisClass() {
        return jeVisClass;
    }

    public FilteredList<RegisterTableRow> getFilteredList() {
        return filteredList;
    }

    public void setFilteredList(FilteredList<RegisterTableRow> filteredList) {
        this.filteredList = filteredList;
    }
}
