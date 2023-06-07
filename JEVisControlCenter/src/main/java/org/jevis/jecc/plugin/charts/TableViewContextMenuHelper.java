package org.jevis.jecc.plugin.charts;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Helper class to replace default column selection popup for TableView.
 *
 * <p>
 * The original idea credited to Roland and was found on
 * {@link=http://stackoverflow.com/questions/27739833/adapt-tableview-menu-button}
 * </p>
 * <p>
 * This improved version targets to solve several problems:
 * <ul>
 * <li>avoid to have to assign the TableView with the new context menu after the
 * window shown (it could cause difficulty when showAndWait() should be used. It
 * solves the problem by registering the onShown event of the containing Window.
 * </li>
 * <li>corrects the mispositioning bug when clicking the + button while the menu
 * is already on.</li>
 * <li>works using keyboard</li>
 * <li>possibility to add additional menu items</li>
 * </ul>
 * </p>
 * <p>
 * Usage from your code:
 *
 * <pre>
 * contextMenuHelper = new TableViewContextMenuHelper(this);
 * // Adding additional menu items
 * MenuItem exportMenuItem = new MenuItem("Export...");
 * contextMenuHelper.getAdditionalMenuItems().add(exportMenuItem);
 * </pre>
 * </p>
 *
 * @author Roland
 * @author bvissy
 */
public class TableViewContextMenuHelper {

    private final TableView<?> tableView;
    private final List<MenuItem> additionalMenuItems = new ArrayList<>();
    private ContextMenu columnPopupMenu;
    private boolean showAllColumnsOperators = true;
    // Default key to show menu: Shortcut + Shift + Space
    private Function<KeyEvent, Boolean> showMenuByKeyboardCheck =
            ke -> ke.getCode().equals(KeyCode.SPACE) && ke.isShortcutDown() && ke.isShiftDown();


    public TableViewContextMenuHelper(TableView<?> tableView) {
        super();
        this.tableView = tableView;

        // Hooking at the event when the whole window is shown
        // and then implementing the event handler assignment
//        tableView.sceneProperty().addListener(i -> {
//
//            tableView.getScene().windowProperty().addListener(i2 -> {
//                tableView.getScene().getWindow().setOnShown(i3 -> {
//                    tableView.tableMenuButtonVisibleProperty().addListener((ob, o, n) -> {
//                        if (n == true) {
//                            registerListeners();
//                        }
//                    });
//                    if (tableView.isTableMenuButtonVisible()) {
//                        registerListeners();
//                    }
//
//                });
//
//            });
//        });

        tableView.skinProperty().addListener((a, b, newSkin) -> {
            tableView.tableMenuButtonVisibleProperty().addListener((ob, o, n) -> {
                if (n == true) {
                    registerListeners();
                }
            });
            if (tableView.isTableMenuButtonVisible()) {
                registerListeners();
            }
        });
    }

    /**
     * Registers the listeners.
     */
    private void registerListeners() {
        final Node buttonNode = findButtonNode();

        // Keyboard listener on the table
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, ke -> {
            if (showMenuByKeyboardCheck.apply(ke)) {
                showContextMenu();
                ke.consume();
            }
        });

        // replace mouse listener on "+" node
        buttonNode.setOnMousePressed(me -> {
            showContextMenu();
            me.consume();

        });

    }

    public void showContextMenu() {
        final Node buttonNode = findButtonNode();

        setFixedHeader();

        // When the menu is already shown clicking the + button hides it.
        if (columnPopupMenu != null) {
            columnPopupMenu.hide();
        } else {
            // Show the menu
            final ContextMenu newColumnPopupMenu = createContextMenu();
            newColumnPopupMenu.setOnHidden(ev -> {
                columnPopupMenu = null;
            });
            columnPopupMenu = newColumnPopupMenu;
            columnPopupMenu.show(buttonNode, Side.BOTTOM, 0, 0);
            // Repositioning the menu to be aligned by its right side (keeping inside the table view)
            columnPopupMenu.setX(
                    buttonNode.localToScreen(buttonNode.getBoundsInLocal()).getMaxX()
                            - columnPopupMenu.getWidth());
        }
    }


    private void setFixedHeader() {
        // setting the preferred height for the table header row
        // if the preferred height isn't set, then the table header would disappear if there are no visible columns
        // and with it the table menu button
        // by setting the preferred height the header will always be visible
        // note: this may need adjustments in case you have different heights in columns (eg when you use grouping)
        Region tableHeaderRow = getTableHeaderRow();
        double defaultHeight = tableHeaderRow.getHeight();
        tableHeaderRow.setPrefHeight(defaultHeight);
    }

    private Node findButtonNode() {
        TableHeaderRow tableHeaderRow = getTableHeaderRow();
        if (tableHeaderRow == null) {
            return null;
        }

        for (Node child : tableHeaderRow.getChildren()) {

            // child identified as cornerRegion in TableHeaderRow.java
            if (child.getStyleClass().contains("show-hide-columns-button")) {
                return child;
            }
        }
        return null;
    }

    public Node getButtonNode() {
        TableHeaderRow tableHeaderRow = getTableHeaderRow();
        if (tableHeaderRow == null) {
            return null;
        }

        for (Node child : tableHeaderRow.getChildren()) {

            // child identified as cornerRegion in TableHeaderRow.java
            if (child.getStyleClass().contains("show-hide-columns-button")) {
                return child;
            }
        }
        return null;
    }

    public TableHeaderRow getTableHeaderRow() {
        TableViewSkin<?> tableSkin = (TableViewSkin<?>) tableView.getSkin();
        if (tableSkin == null) {
            return null;
        }

        // get all children of the skin
        ObservableList<Node> children = tableSkin.getChildren();

        // find the TableHeaderRow child
        for (int i = 0; i < children.size(); i++) {

            Node node = children.get(i);

            if (node instanceof TableHeaderRow) {
                return (TableHeaderRow) node;
            }
        }
        return null;
    }


    /**
     * Create a menu with custom items. The important thing is that the menu
     * remains open while you click on the menu items.
     */
    private ContextMenu createContextMenu() {

        ContextMenu cm = new ContextMenu();

        // create new context menu
        CustomMenuItem cmi;

        if (showAllColumnsOperators) {
            // select all item
            Label selectAll = new Label("Select all");
            selectAll.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> doSelectAll(event));

            cmi = new CustomMenuItem(selectAll);
            cmi.setOnAction(e -> doSelectAll(e));
            cmi.setHideOnClick(false);
            cm.getItems().add(cmi);

            // deselect all item
            Label deselectAll = new Label("Deselect all");
            deselectAll.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> doDeselectAll(event));

            cmi = new CustomMenuItem(deselectAll);
            cmi.setOnAction(e -> doDeselectAll(e));
            cmi.setHideOnClick(false);
            cm.getItems().add(cmi);

            // separator
            cm.getItems().add(new SeparatorMenuItem());
        }

        // menu item for each of the available columns
        for (Object obj : tableView.getColumns()) {

            TableColumn<?, ?> tableColumn = (TableColumn<?, ?>) obj;

            MFXCheckbox cb = new MFXCheckbox(tableColumn.getText());
            cb.selectedProperty().bindBidirectional(tableColumn.visibleProperty());

            cmi = new CustomMenuItem(cb);
            cmi.setOnAction(e -> {
                cb.setSelected(!cb.isSelected());
                e.consume();
            });
            cmi.setHideOnClick(false);

            cm.getItems().add(cmi);
        }

        if (!additionalMenuItems.isEmpty()) {
            cm.getItems().add(new SeparatorMenuItem());
            cm.getItems().addAll(additionalMenuItems);
        }

        return cm;
    }

    protected void doDeselectAll(Event e) {
        for (Object obj : tableView.getColumns()) {
            ((TableColumn<?, ?>) obj).setVisible(false);
        }
        e.consume();
    }

    protected void doSelectAll(Event e) {
        for (Object obj : tableView.getColumns()) {
            ((TableColumn<?, ?>) obj).setVisible(true);
        }
        e.consume();
    }

    public boolean isShowAllColumnsOperators() {
        return showAllColumnsOperators;
    }

    /**
     * Sets whether the Select all/Deselect all buttons are visible
     *
     * @param showAllColumnsOperators
     */
    public void setShowAllColumnsOperators(boolean showAllColumnsOperators) {
        this.showAllColumnsOperators = showAllColumnsOperators;
    }

    public List<MenuItem> getAdditionalMenuItems() {
        return additionalMenuItems;
    }

    public Function<KeyEvent, Boolean> getShowMenuByKeyboardCheck() {
        return showMenuByKeyboardCheck;
    }

    /**
     * Overrides the keypress check to show the menu. Default is Shortcut +
     * Shift + Space.
     *
     * <p>
     * To disable keyboard shortcut use the <code>e -> false</code> function.
     * </p>
     *
     * @param showMenuByKeyboardCheck
     */
    public void setShowMenuByKeyboardCheck(Function<KeyEvent, Boolean> showMenuByKeyboardCheck) {
        this.showMenuByKeyboardCheck = showMenuByKeyboardCheck;
    }

}
