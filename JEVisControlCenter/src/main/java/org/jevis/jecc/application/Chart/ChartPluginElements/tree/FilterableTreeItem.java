package org.jevis.jecc.application.Chart.ChartPluginElements.tree;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

import java.lang.reflect.Field;

public class FilterableTreeItem extends TreeItem<JEVisTreeViewItem> {
    final private ObservableList<TreeItem<JEVisTreeViewItem>> sourceList;
    private final FilteredList<TreeItem<JEVisTreeViewItem>> filteredList;
    private final ObjectProperty<TreeItemPredicate<JEVisTreeViewItem>> predicate = new SimpleObjectProperty<>();

    public FilterableTreeItem(JEVisTreeViewItem t) {
        super(t, null);

        this.sourceList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(this.sourceList);
        this.filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            return child -> {
                // Set the predicate of child items to force filtering
                if (child instanceof FilterableTreeItem) {
                    FilterableTreeItem filterableChild = (FilterableTreeItem) child;
                    filterableChild.setPredicate(this.predicate.get());
                }
                // If there is no predicate, keep this tree item
                if (this.predicate.get() == null)
                    return true;
                // If there are children, keep this tree item
                if (child.getChildren().size() > 0)
                    return true;
                // Otherwise ask the TreeItemPredicate
                return this.predicate.get().test(this, child.getValue());
            };
        }, this.predicate));
        setHiddenFieldChildren(this.filteredList);
    }

    protected void setHiddenFieldChildren(ObservableList<TreeItem<JEVisTreeViewItem>> list) {
        try {
            Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
            childrenField.setAccessible(true);
            childrenField.set(this, list);

            Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
            declaredField.setAccessible(true);
            list.addListener((ListChangeListener<? super TreeItem<JEVisTreeViewItem>>) declaredField.get(this));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
        }
    }

    public ObservableList<TreeItem<JEVisTreeViewItem>> getInternalChildren() {
        return this.sourceList;
    }

    public TreeItemPredicate getPredicate() {
        return predicate.get();
    }

    public void setPredicate(TreeItemPredicate<JEVisTreeViewItem> predicate) {
        this.predicate.set(predicate);
    }

    public ObjectProperty<TreeItemPredicate<JEVisTreeViewItem>> predicateProperty() {
        return predicate;
    }

}
