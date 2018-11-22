package org.jevis.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;

import java.util.ArrayList;
import java.util.List;

public class FilterFactory {

    public static List<ObjectAttributeFilter> defaultObjectTreeFilter() {
        List<ObjectAttributeFilter> filter = new ArrayList<>();
        filter.add(new ObjectAttributeFilter("All Objects", ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));

        return filter;
    }

    public static List<ObjectAttributeFilter> defaultAttributeTreeFilter() {
        List<ObjectAttributeFilter> filter = new ArrayList<>();
        filter.add(new ObjectAttributeFilter("All Objects", ObjectAttributeFilter.ALL, ObjectAttributeFilter.ALL));

        return filter;
    }

    public static void addDefaultObjectTreeFilter(BasicCellFilter filter, TreeTableColumn column) {
        defaultObjectTreeFilter().forEach(objectAttributeFilter -> {
            filter.addFilter(column, objectAttributeFilter);
        });
    }

    public static void addDefaultAttributeTreeFilter(BasicCellFilter filter, TreeTableColumn column) {
        defaultAttributeTreeFilter().forEach(objectAttributeFilter -> {
            filter.addFilter(column, objectAttributeFilter);
        });
    }

    public static BasicCellFilter buildDefaultItemFilter() {
        BasicCellFilter basicFilter = new BasicCellFilter();
//        basicFilter.addItemFilter(new ObjectAttributeFilter(BasicCellFilter.TREE_ITEM_COLUMN, ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));

        basicFilter.addItemFilter(new ObjectAttributeFilter(BasicCellFilter.TREE_ITEM_COLUMN, "Data", ObjectAttributeFilter.NONE));

        return basicFilter;
    }

}
