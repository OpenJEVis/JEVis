package org.jevis.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeItem;
import org.jevis.application.jevistree.JEVisTreeRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.util.HashMap;

public class BasicCellFilter implements CellFilter {

    /**
     * Identifier for the TreeItem filter which is not a real column but will be
     * handled like one.
     */
    public static String TREE_ITEM_COLUMN = "TREE_ITEM_COLUMN";

    private Map<TreeTableColumn<JEVisTreeRow, String>, List<ObjectAttributeFilter>> filters = new HashMap();
    private List<ObjectAttributeFilter> itemFilters = new ArrayList<>();
//    private ObservableList<ObjectAttributeFilter> filters = FXCollections.observableArrayList();


    public BasicCellFilter() {
    }

    private List<ObjectAttributeFilter> getColumnFilter(TreeTableColumn column) {

        if (!filters.containsKey(column)) {
            System.out.println("addColumnFilter: " + column);
            filters.put(column, new ArrayList<>());
        }

        return filters.get(column);
    }

    public void addFilter(TreeTableColumn column, String filterName, String objectClass, String attribute) {

        getColumnFilter(column).add(new ObjectAttributeFilter(filterName, objectClass, attribute));
    }

    public void addFilter(TreeTableColumn column, ObjectAttributeFilter filter) {
        System.out.println("AddFilter: " + column.getId() + " f: " + filter);
        getColumnFilter(column).add(filter);
    }

    public void addItemFilter(ObjectAttributeFilter filter) {
        itemFilters.add(filter);
    }


    @Override
    public boolean showCell(TreeTableColumn column, JEVisTreeRow row) throws JEVisException {
        System.out.println("showCell?: \n-- " + column + "\n-- " + row);
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

    @Override
    public boolean showItem(JEVisAttribute attribute) {
        for (ObjectAttributeFilter objectAttributeFilter : itemFilters) {
            try {
                if (objectAttributeFilter.showAttribute(attribute.getName())) {
                    return true;
                }
            } catch (Exception ex) {
            }
        }
        return false;
    }

    @Override
    public boolean showItem(JEVisType type) {
        for (ObjectAttributeFilter objectAttributeFilter : itemFilters) {
            try {
                if (objectAttributeFilter.showAttribute(type.getName())) {
                    return true;
                }
            } catch (Exception ex) {
            }
        }
        return false;
    }

    @Override
    public boolean showItem(JEVisObject object) {
        for (ObjectAttributeFilter objectAttributeFilter : itemFilters) {
            try {
                if (objectAttributeFilter.showClass(object.getJEVisClassName())) {
                    return true;
                }
            } catch (Exception ex) {
            }
        }
        return false;
    }

    @Override
    public boolean showRow(JEVisTreeItem item) {
        System.out.println("showItem?: \n-- " + item);
        if (itemFilters == null || itemFilters.isEmpty()) {
            System.out.println("Warning no filter set show all");
//            return true;
        }
        for (ObjectAttributeFilter objectAttributeFilter : itemFilters) {
            try {
                if (item.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                    if (objectAttributeFilter.showAttribute(item.getValue().getJEVisAttribute().getName())) {
                        return true;
                    }
                }

                if (item.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
                    if (objectAttributeFilter.showAttribute(item.getValue().getJEVisObject().getJEVisClassName())) {
                        return true;
                    }
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
