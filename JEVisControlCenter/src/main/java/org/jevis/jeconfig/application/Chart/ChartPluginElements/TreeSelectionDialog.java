package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.*;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tree.FilterableTreeItem;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tree.JEVisTreeView;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tree.TreeItemPredicate;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tree.TreeViewPath;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.dialog.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TreeSelectionDialog extends JFXDialog {

    public static List<String> allDataAndCleanDataClasses = new ArrayList<>(Arrays.asList("Data", "Clean Data", "Base Data", "Math Data"));
    private final JEVisTreeView treeView;
    private Response response = Response.CANCEL;

    public TreeSelectionDialog(StackPane dialogContainer, JEVisDataSource ds, List<JEVisClass> classFilter, SelectionMode selectionMode) {
        this(dialogContainer, ds, classFilter, selectionMode, new ArrayList<>(), false);
    }

    public TreeSelectionDialog(StackPane dialogContainer, JEVisDataSource ds, List<JEVisClass> classFilter, SelectionMode selectionMode, List<UserSelection> selection, boolean showAttributes) {
        super();

        this.setDialogContainer(dialogContainer);


        VBox box = new VBox();
        box.setPadding(new Insets(12));
        box.setSpacing(8);

        treeView = new JEVisTreeView(ds, selectionMode, selection, showAttributes);

        Label filterLabel = new Label(I18n.getInstance().getString("searchbar.filter"));
        VBox filterVBox = new VBox(filterLabel);
        filterVBox.setAlignment(Pos.CENTER);
        JFXTextField filterTextField = new JFXTextField();

        if (treeView.getRoot() instanceof FilterableTreeItem) {
            ((FilterableTreeItem) treeView.getRoot()).predicateProperty().bind(Bindings.createObjectBinding(() -> {
                if ((filterTextField.getText() == null || filterTextField.getText().isEmpty()) && (classFilter.isEmpty()))
                    return null;
                else if ((filterTextField.getText() == null || filterTextField.getText().isEmpty()) && (!classFilter.isEmpty())) {
                    return TreeItemPredicate.create(jeVisTreeViewItem -> {
                        try {
                            return classFilter.contains(jeVisTreeViewItem.getObject().getJEVisClass());
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        return false;
                    });
                } else return TreeItemPredicate.create(jeVisTreeViewItem -> {
                    boolean containsName = jeVisTreeViewItem.getObject().getLocalName(I18n.getInstance().getLocale().getLanguage()).toLowerCase(I18n.getInstance().getLocale()).contains(filterTextField.getText().toLowerCase(I18n.getInstance().getLocale()));
                    boolean classFiltered = false;
                    try {
                        classFiltered = classFilter.contains(jeVisTreeViewItem.getObject().getJEVisClass());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return containsName && classFiltered;
                });
            }, filterTextField.textProperty()));
        }

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            try {
                boolean correctChoice = true;
                List<JEVisObject> incorrectObjects = new ArrayList<>();
                for (JEVisObject object : treeView.getSelectedObjects()) {
                    if (!classFilter.contains(object.getJEVisClass())) {
                        correctChoice = false;
                        incorrectObjects.add(object);
                    }
                }

                if (correctChoice) {
                    response = Response.OK;
                    this.close();
                } else {
                    StringBuilder stringBuilder = new StringBuilder();

                    for (JEVisObject object : incorrectObjects) {
                        stringBuilder.append("\n").append(object.getName());
                    }
                    Alert selectionShow = new Alert(Alert.AlertType.ERROR, "Error: " + stringBuilder);
                    selectionShow.show();
                }
            } catch (Exception e) {

            }
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> this.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(8, filterVBox, filterTextField, spacer, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(new TreeViewPath(treeView), treeView, buttonBar);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        box.setMinHeight(dialogContainer.getHeight() - 40);
        box.setMinWidth(1024);

        this.setContent(box);
    }

    private static String getItemText(Cell<JEVisObject> cell) {
        return (DataMethods.getObjectName(cell.getItem()));
    }

    public static TreeSelectionDialog createSelectionDialog(final Cell<JEVisObject> cell, StackPane dialogContainer) {
        JEVisObject item = cell.getItem();
        UserSelection userSelection = new UserSelection(UserSelection.SelectionType.Object, item);
        TreeSelectionDialog treeSelectionDialog = null;
        try {
            List<JEVisClass> classes = new ArrayList<>();
            for (String className : allDataAndCleanDataClasses) {
                classes.add(item.getDataSource().getJEVisClass(className));
            }

            treeSelectionDialog = new TreeSelectionDialog(dialogContainer, item.getDataSource(), classes, SelectionMode.SINGLE, Collections.singletonList(userSelection), false);

            // Use onAction here rather than onKeyReleased (with check for Enter),
            // as otherwise we encounter RT-34685
            TreeSelectionDialog finalTreeSelectionDialog = treeSelectionDialog;
            treeSelectionDialog.setOnDialogClosed(event -> {
                if (finalTreeSelectionDialog.getResponse() == Response.OK) {
                    cell.commitEdit(finalTreeSelectionDialog.getTreeView().getSelectedObjects().get(0));
                } else {
                    cell.cancelEdit();
                }

                event.consume();
            });

        } catch (Exception ignored) {
        }
        return treeSelectionDialog;
    }

    public static void startEdit(final Cell<JEVisObject> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final TreeSelectionDialog treeSelectionDialog) {
        if (treeSelectionDialog != null) {
            treeSelectionDialog.getTreeView().select(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, treeSelectionDialog);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(treeSelectionDialog);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        treeSelectionDialog.requestFocus();
    }

    public static void cancelEdit(Cell<JEVisObject> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<JEVisObject> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final TreeSelectionDialog treeSelectionDialog) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (treeSelectionDialog != null) {
                    treeSelectionDialog.getTreeView().select(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, treeSelectionDialog);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(treeSelectionDialog);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }

    public JEVisTreeView getTreeView() {
        return treeView;
    }

    public Response getResponse() {
        return response;
    }

    public List<UserSelection> getUserSelection() {
        return treeView.getUserSelection();
    }
}
