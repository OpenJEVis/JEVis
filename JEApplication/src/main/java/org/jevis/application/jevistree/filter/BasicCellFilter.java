package org.jevis.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;
import org.jevis.api.JEVisException;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.util.HashMap;

public class BasicCellFilter implements CellFilter {

    private Map<TreeTableColumn<JEVisTreeRow, String>, List<ObjectAttributeFilter>> filters = new HashMap();
//    private ObservableList<ObjectAttributeFilter> filters = FXCollections.observableArrayList();


    public BasicCellFilter() {
        this.filters = filters;
    }

    private List<ObjectAttributeFilter> getColumnFilter(TreeTableColumn column) {
        if (!filters.containsKey(column)) {
            filters.put(column, new ArrayList<>());
        }

        return filters.get(column);
    }

    public void addFilter(TreeTableColumn column, String filterName, String objectClass, String attribute) {
        getColumnFilter(column).add(new ObjectAttributeFilter(filterName, objectClass, attribute));
    }

    public void addFilter(TreeTableColumn column, ObjectAttributeFilter filter) {
        getColumnFilter(column).add(filter);
    }


    @Override
    public boolean showCell(TreeTableColumn<JEVisTreeRow, String> column, JEVisTreeRow row) throws JEVisException {
        for (ObjectAttributeFilter objectAttributeFilter : filters.get(column)) {
            try {
                if (objectAttributeFilter.showCell(row)) {
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }


    public void registerFilter(JEVisTree tree) {
        tree.setCellFilter(this);
    }

}
