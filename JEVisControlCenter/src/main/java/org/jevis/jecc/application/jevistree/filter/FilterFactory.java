package org.jevis.jecc.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.commons.classes.ClassHelper;

import java.util.ArrayList;
import java.util.List;

public class FilterFactory {

    public static List<ObjectAttributeFilter> buildFilterForHeirs(JEVisClass jclass, String attributeFilter) throws JEVisException {
        List<ObjectAttributeFilter> filter = new ArrayList<>();
        List<JEVisClass> heirs = new ArrayList<>();
        ClassHelper.addAllHeir(heirs, jclass);

        filter.add(new ObjectAttributeFilter(jclass.getName(), attributeFilter));
        heirs.forEach(jeVisClass -> {
            try {
                filter.add(new ObjectAttributeFilter(jeVisClass.getName(), attributeFilter));
            } catch (Exception ex) {
            }
        });

        return filter;
    }


    public static List<ObjectAttributeFilter> defaultObjectTreeFilter() {
        List<ObjectAttributeFilter> filter = new ArrayList<>();
        filter.add(new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));

        return filter;
    }

    public static List<ObjectAttributeFilter> defaultAttributeTreeFilter() {
        List<ObjectAttributeFilter> filter = new ArrayList<>();
        filter.add(new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.ALL));

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
        BasicCellFilter basicFilter = new BasicCellFilter("Default");
//        basicFilter.addItemFilter(new ObjectAttributeFilter(BasicCellFilter.TREE_ITEM_COLUMN, ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));

        basicFilter.addItemFilter(new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE));

        return basicFilter;
    }

}
