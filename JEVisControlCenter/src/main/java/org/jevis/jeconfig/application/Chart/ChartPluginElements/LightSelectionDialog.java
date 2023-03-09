package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.*;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.dialog.Response;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LightSelectionDialog extends JFXDialog {

    private final FilterableTreeItem<SelectionObject> rootNode = new FilterableTreeItem<>(new SelectionObject("Root", 0L, 0L, null));//, rootIcon);
    private final SelectionMode selectionMode;
    private final List<SelectionObject> selectedItems = new ArrayList<>();
    private final List<SelectionObject> selectionObjects = new ArrayList<>();
    private Response response = Response.CANCEL;

    public LightSelectionDialog(StackPane dialogContainer, JEVisDataSource ds, SelectionMode selectionMode) {
        super();
        this.selectionMode = selectionMode;

        this.setDialogContainer(dialogContainer);

        rootNode.setExpanded(true);
        final TreeTableView<SelectionObject> treeView = new TreeTableView<>(rootNode);
        treeView.setTableMenuButtonVisible(true);
        treeView.setEditable(true);

        TreeTableColumn<SelectionObject, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(460);
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        nameColumn.setCellFactory(ImageTreeTableCell.forTreeTableColumn());

        TreeTableColumn<SelectionObject, Boolean> selectionColumn = new TreeTableColumn<>("Selection");
        selectionColumn.setStyle("-fx-alignment: CENTER;");
        selectionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
        selectionColumn.setCellFactory(SelectionTreeTableCell.forTreeTableColumn(selectionColumn));
        selectionColumn.setEditable(true);
        selectionColumn.setOnEditStart(selectionObjectBooleanCellEditEvent -> {
            System.out.println("Edit start");
        });
        selectionColumn.setOnEditCancel(selectionObjectBooleanCellEditEvent -> {
            System.out.println("Edit cancel");
        });
        selectionColumn.setOnEditCommit(selectionObjectBooleanCellEditEvent -> {
            selectionObjectBooleanCellEditEvent.getRowValue().getValue().setSelected(selectionObjectBooleanCellEditEvent.getNewValue());


            if (selectionObjectBooleanCellEditEvent.getNewValue() && !selectedItems.contains(selectionObjectBooleanCellEditEvent.getRowValue().getValue())) {
                if (selectionMode == SelectionMode.SINGLE && !selectedItems.isEmpty()) {
                    selectedItems.get(0).setSelected(false);
                    selectedItems.clear();
                }
                selectedItems.add(selectionObjectBooleanCellEditEvent.getRowValue().getValue());
            } else if (!selectionObjectBooleanCellEditEvent.getNewValue()) {
                selectedItems.remove(selectionObjectBooleanCellEditEvent.getRowValue().getValue());
            }
        });

        TreeTableColumn<SelectionObject, String> idColumn = new TreeTableColumn<>("Id");
        idColumn.setPrefWidth(70);
        idColumn.setVisible(false);
        idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));

        treeView.getColumns().setAll(nameColumn, idColumn, selectionColumn);

        try {
            List<JEVisObject> rootObjects = ds.getRootObjects();
            for (JEVisObject object : rootObjects) {
                try {
                    SelectionObject selectionObject = new SelectionObject(object.getName(), object.getID(), 0L, object);
                    selectionObjects.add(selectionObject);
                    rootNode.getInternalChildren().add(new FilterableTreeItem<>(selectionObject));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            List<JEVisObject> allObjects = ds.getObjects();
            allObjects.removeAll(rootObjects);

            List<JEVisObject> doneObject = new ArrayList<>();
            addChildren(allObjects, doneObject, rootNode.getInternalChildren());
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        VBox box = new VBox();
        box.setPadding(new Insets(12));
        box.setSpacing(8);

        treeView.setShowRoot(false);

        TextField filterField = new JFXTextField();

        rootNode.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (filterField.getText() == null || filterField.getText().isEmpty())
                return null;
            return TreeItemPredicate.create(selectionObject -> selectionObject.getName().contains(filterField.getText()));
        }, filterField.textProperty()));

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            try {
                response = Response.OK;
                this.close();
            } catch (Exception e) {

            }
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> this.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(8, spacer, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(new TreeTableViewPath(treeView), treeView, filterField, buttonBar);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        box.setMinHeight(960);
        box.setMinWidth(1024);

        this.setContent(box);
    }

    private void addChildren(List<JEVisObject> allObjects, List<JEVisObject> doneObject, ObservableList<TreeItem<SelectionObject>> internalChildren) throws JEVisException {

        for (TreeItem<SelectionObject> filterableTreeItem : internalChildren) {
            JEVisObject parentObject = filterableTreeItem.getValue().getObject();
            List<JEVisObject> children = parentObject.getChildren();
            for (JEVisObject object : allObjects) {
                if (children.contains(object)) {
                    SelectionObject selectionObject = new SelectionObject(object.getName(), object.getID(), parentObject.getID(), object);
                    selectionObjects.add(selectionObject);
                    ((FilterableTreeItem) filterableTreeItem).getInternalChildren().add(new FilterableTreeItem<>(selectionObject));
                    doneObject.add(object);
                }
            }

            addChildren(allObjects, doneObject, filterableTreeItem.getChildren());

            allObjects.removeAll(doneObject);
            doneObject.clear();
        }
    }

    public List<JEVisObject> getSelectedItems() {
        return selectedItems.stream().map(SelectionObject::getObject).collect(Collectors.toList());
    }


    @FunctionalInterface
    public interface TreeItemPredicate<T> {

        static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
            return (parent, value) -> predicate.test(value);
        }

        boolean test(TreeItem<T> parent, T value);

    }

    public class FilterableTreeItem<T> extends TreeItem<T> {
        final private ObservableList<TreeItem<T>> sourceList;
        private final FilteredList<TreeItem<T>> filteredList;
        private final ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>();


        public FilterableTreeItem(T value) {
            super(value);

            this.sourceList = FXCollections.observableArrayList();
            this.filteredList = new FilteredList<>(this.sourceList);
            this.filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> {
                return child -> {
                    // Set the predicate of child items to force filtering
                    if (child instanceof FilterableTreeItem) {
                        FilterableTreeItem<T> filterableChild = (FilterableTreeItem<T>) child;
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

        protected void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
            try {
                Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
                childrenField.setAccessible(true);
                childrenField.set(this, list);

                Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
                declaredField.setAccessible(true);
                list.addListener((ListChangeListener<? super TreeItem<T>>) declaredField.get(this));
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
            }
        }

        public ObservableList<TreeItem<T>> getInternalChildren() {
            return this.sourceList;
        }

        public TreeItemPredicate getPredicate() {
            return predicate.get();
        }

        public void setPredicate(TreeItemPredicate<T> predicate) {
            this.predicate.set(predicate);
        }

        public ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
            return predicate;
        }
    }


}
