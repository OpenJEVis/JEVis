package org.jevis.jeconfig.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the JEVisTreeFilter. THis filter can filter based on the JEVIsObject.getJEVisClass
 * and Attribute.getName
 *
 * @TODO: Add an feature to also positive filter objects under the objects we want so see, use case user want to see all DataSource and so all Objects under it.
 */
public class BasicCellFilter implements JEVisTreeFilter {

    private static final Logger logger = LogManager.getLogger(BasicCellFilter.class);
    /**
     * Identifier for the TreeItem filter which is not a real column but will be
     * handled like one.
     */
    public static String TREE_ITEM_COLUMN = "TREE_ITEM_COLUMN";
    private Map<String, List<ObjectAttributeFilter>> filters = new HashMap<>();
    private List<ObjectAttributeFilter> itemFilters = new ArrayList<>();
    private String name = "";


    /**
     * Create an new empty filter
     */
    public BasicCellFilter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    private List<ObjectAttributeFilter> getColumnFilter(String column) {
        if (!filters.containsKey(column)) {
            filters.put(column, new ArrayList<>());
        }

        return filters.get(column);
    }

    private List<ObjectAttributeFilter> getColumnFilter(TreeTableColumn column) {
        return getColumnFilter(column.getId());
    }

    public void addFilter(String column, String objectClass, String attribute) {
        getColumnFilter(column).add(new ObjectAttributeFilter(objectClass, attribute));
    }


    public void addFilter(TreeTableColumn column, String objectClass, String attribute) {
        addFilter(column.getId(), objectClass, attribute);
    }

    public void addFilter(String column, ObjectAttributeFilter filter) {
        getColumnFilter(column).add(filter);
    }

    public void addFilter(TreeTableColumn column, ObjectAttributeFilter filter) {
        logger.trace("Add new filter column: {} filter: {}" + column.getId(), filter);
        getColumnFilter(column).add(filter);
    }

    /**
     * Filter to decide which JEVisObject/JEVisAttribute will have an JEVisTreeItem in the JEVisTree
     * <p>
     * Used by the showItem and showRow functions
     *
     * @param filter
     */
    public void addItemFilter(ObjectAttributeFilter filter) {
        itemFilters.add(filter);
    }


    @Override
    public boolean showCell(TreeTableColumn column, JEVisTreeRow row) {
        try {
//            System.out.println("showCell?: \n-- '" + column.getId() + "' " + column.getClass() + " --- " + row);
            List<ObjectAttributeFilter> fLtst = filters.get(column.getId());
            if (fLtst != null) {
                for (ObjectAttributeFilter objectAttributeFilter : fLtst) {
                    try {
                        if (objectAttributeFilter.showCell(row)) {
                            return true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                logger.warn("No filter for column: {}", column);
            }

        } catch (Exception ex) {
            logger.error(ex);
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
        try {
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
        } catch (Exception ex) {
            logger.error(ex);
        }
        return false;
    }

    public void registerFilter(JEVisTree tree) {
        tree.setFilter(this);
    }

}
