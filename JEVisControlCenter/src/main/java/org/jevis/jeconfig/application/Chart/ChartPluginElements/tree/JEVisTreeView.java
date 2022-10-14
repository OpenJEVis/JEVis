package org.jevis.jeconfig.application.Chart.ChartPluginElements.tree;

import com.jfoenix.controls.JFXTreeView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.DirectoryHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JEVisTreeView extends JFXTreeView<JEVisTreeViewItem> {
    private final double iconSize = 18d;
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();
    private final boolean showAttributes;
    private final List<JEVisObject> selectedObjects = new ArrayList<>();
    private final List<JEVisAttribute> selectedAttributes = new ArrayList<>();
    private final List<FilterableTreeItem> selectedFilterableTreeItems = new ArrayList<>();

    public JEVisTreeView(JEVisDataSource ds, SelectionMode selectionMode, List<UserSelection> selection, boolean showAttributes) {
        this.showAttributes = showAttributes;
        final FilterableTreeItem rootNode = new FilterableTreeItem(null);
        rootNode.setExpanded(true);
        setShowRoot(false);
        getSelectionModel().setSelectionMode(selectionMode);

        for (UserSelection userSelection : selection) {
            if (userSelection.getType() == UserSelection.SelectionType.Object) {
                selectedObjects.add(userSelection.getSelectedObject());
            } else {
                selectedAttributes.add(userSelection.getSelectedAttribute());
            }
        }

        try {
            List<JEVisObject> rootObjects = ds.getRootObjects();
            rootObjects.sort(objectComparator());

            for (JEVisObject object : rootObjects) {
                try {

                    JEVisTreeViewItem jeVisTreeViewItem = new JEVisTreeViewItem(object);
                    FilterableTreeItem rootItem = new FilterableTreeItem(jeVisTreeViewItem);
                    if (selectedObjects.contains(object)) selectedFilterableTreeItems.add(rootItem);

                    addChildren(rootItem);

                    rootNode.getInternalChildren().add(rootItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setCellFactory(ImageTreeCell.callback());

        setRoot(rootNode);

        for (FilterableTreeItem filterableTreeItem : selectedFilterableTreeItems) {
            expandTreeView(filterableTreeItem);
            getSelectionModel().select(filterableTreeItem);
        }
    }

    private static <T> void expandTreeView(TreeItem<T> selectedItem) {
        if (selectedItem != null) {
            expandTreeView(selectedItem.getParent());
            if (!selectedItem.isLeaf()) {
                selectedItem.setExpanded(true);
            }
        }
    }

    public static TreeItem<JEVisObject> getTreeViewItem(TreeItem<JEVisObject> item, JEVisObject value) {
        if (item != null && item.getValue().equals(value))
            return item;

        for (TreeItem<JEVisObject> child : item.getChildren()) {
            TreeItem<JEVisObject> s = getTreeViewItem(child, value);
            if (s != null)
                return s;
        }
        return null;
    }

    public void select(JEVisObject object) {
        getChildren().forEach(node -> {

        });
    }

    private void addChildren(FilterableTreeItem rootItem) throws JEVisException {
        if (rootItem.getValue() != null && rootItem.getValue().getItemType() == JEVisTreeViewItem.ItemType.OBJECT) {
            JEVisObject value = rootItem.getValue().getObject();

            List<JEVisObject> children = value.getChildren();
            List<JEVisAttribute> attributes = value.getAttributes();

            children.sort(objectComparator());
            attributes.sort(attributeComparator());

            for (JEVisObject jeVisObject : children) {
                JEVisTreeViewItem jeVisTreeViewItem = new JEVisTreeViewItem(jeVisObject);
                FilterableTreeItem childItem = new FilterableTreeItem(jeVisTreeViewItem);
                rootItem.getInternalChildren().add(childItem);

                if (selectedObjects.contains(jeVisObject)) selectedFilterableTreeItems.add(rootItem);

                addChildren(childItem);
            }

            if (showAttributes) {
                for (JEVisAttribute attribute : attributes) {

                    JEVisTreeViewItem jeVisTreeViewItem = new JEVisTreeViewItem(attribute);
                    jeVisTreeViewItem.setObject(attribute.getObject());

                    FilterableTreeItem attributeItem = new FilterableTreeItem(jeVisTreeViewItem);
                    rootItem.getInternalChildren().add(attributeItem);

                    if (selectedAttributes.contains(attribute)) selectedFilterableTreeItems.add(attributeItem);
                }
            }
        }
    }

    public List<JEVisObject> getSelectedObjects() {
        List<JEVisObject> result = new ArrayList<>();
        for (TreeItem<JEVisTreeViewItem> selectedItem : getSelectionModel().getSelectedItems()) {
            if (selectedItem instanceof FilterableTreeItem) {
                FilterableTreeItem treeItem = (FilterableTreeItem) selectedItem;
                result.add(treeItem.getValue().getObject());
            }
        }

        return result;
    }

    public List<JEVisAttribute> getSelectedAttributes() {
        List<JEVisAttribute> result = new ArrayList<>();
        for (TreeItem<JEVisTreeViewItem> selectedItem : getSelectionModel().getSelectedItems()) {
            if (selectedItem instanceof FilterableTreeItem) {
                FilterableTreeItem treeItem = (FilterableTreeItem) selectedItem;
                result.add(treeItem.getValue().getAttribute());
            }
        }

        return result;
    }

    public List<UserSelection> getUserSelection() {
        List<UserSelection> result = new ArrayList<>();
        for (TreeItem<JEVisTreeViewItem> selectedItem : getSelectionModel().getSelectedItems()) {
            if (selectedItem instanceof FilterableTreeItem) {
                FilterableTreeItem treeItem = (FilterableTreeItem) selectedItem;
                JEVisTreeViewItem treeItemValue = treeItem.getValue();
                UserSelection userSelection;
                if (treeItemValue.getItemType() == JEVisTreeViewItem.ItemType.OBJECT) {
                    userSelection = new UserSelection(UserSelection.SelectionType.Object, treeItemValue.getObject());
                } else {
                    userSelection = new UserSelection(UserSelection.SelectionType.Attribute, treeItemValue.getAttribute(), null, null);
                }
                result.add(userSelection);
            }
        }

        return result;
    }

    private Comparator<JEVisObject> objectComparator() {

        return (o1, o2) -> {
            try {
                JEVisDataSource dataSource = o1.getDataSource();
                boolean o1isDir = DirectoryHelper.getInstance(dataSource).getDirectoryNames().contains(o1.getJEVisClassName());
                boolean o2isDir = DirectoryHelper.getInstance(dataSource).getDirectoryNames().contains(o2.getJEVisClassName());

                if (o1isDir && !o2isDir) {
                    return -1;
                } else if (!o1isDir && o2isDir) {
                    return 1;
                }

                return alphanumComparator.compare(o1.getName(), o2.getName());

            } catch (Exception ignored) {
            }

            return 0;
        };
    }

    private Comparator<JEVisAttribute> attributeComparator() {
        return (o1, o2) -> {
            try {
                return Integer.compare(o1.getType().getGUIPosition(), o2.getType().getGUIPosition());
            } catch (Exception ignored) {
            }

            return 0;
        };
    }
}
