/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.jevistree;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.*;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.*;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.jevistree.filter.BasicCellFilter;
import org.jevis.jeconfig.application.jevistree.filter.FilterFactory;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.filter.ObjectAttributeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPluginTree;
import org.jevis.jeconfig.application.jevistree.plugin.MapPlugin;
import org.jevis.jeconfig.dialog.HiddenConfig;
import org.jevis.jeconfig.dialog.LocalNameDialog;
import org.jevis.jeconfig.plugin.dashboard.datahandler.WidgetTreePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeFactory {

    public final static KeyCombination findNode = KeyCodeCombination.keyCombination("Ctrl+F");
    public final static KeyCombination findAgain = new KeyCodeCombination(KeyCode.F3);
    private static final Logger logger = LogManager.getLogger(JEVisTreeFactory.class);
    public static ExecutorService executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);

    public static void addDefaultKeys(JEVisTree tree) {

        final KeyCombination copyIDandValue = new KeyCodeCombination(KeyCode.F4);
        final KeyCombination copyObj = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        final KeyCombination cutObj = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
        final KeyCombination pasteObj = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination add = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        final KeyCombination rename = new KeyCodeCombination(KeyCode.F2);
        final KeyCombination delete = new KeyCodeCombination(KeyCode.DELETE);
        final KeyCombination deleteAllCleanAndRaw = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination deleteAllCalculations = new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
//        final KeyCombination deleteBrokenTS = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination moveToDiffTS = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination createMultiplierAndDifferential = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination setLimitsRecursive = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination setSubstitutionSettingsRecursive = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination enableAll = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination disableAll = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination setUnitAndPeriod = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        final KeyCombination pageDown = new KeyCodeCombination(KeyCode.PAGE_DOWN);

        tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                /**
                 * When changing, apply to handler in ObjectPlugin, too
                 */
                logger.debug("TreeEvent: {} source: {}", t.getCode(), t.getSource());

                final TreeItem<JEVisTreeRow> selectedObj = (TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem();

                if (findNode.match(t)) {
//                    TreeHelper.EventOpenObject(tree, findNode);
                    tree.getSearchFilterBar().requestCursor();
                    t.consume();
                } else if (findAgain.match(t)) {
                    if (tree.getSearchFilterBar() != null) {
                        tree.getSearchFilterBar().goNext();
                    }
                    t.consume();
                } else if (add.match(t)) {
                    TreeHelper.EventNew(tree, selectedObj.getValue().getJEVisObject());
                    t.consume();
                } else if (delete.match(t)) {
                    TreeHelper.EventDelete(tree);
                    t.consume();
                } else if (deleteAllCleanAndRaw.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventDeleteAllCleanAndRaw(tree);
                    t.consume();
                } else if (deleteAllCalculations.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventDeleteAllCalculations(tree);
                    t.consume();
//                } else if (deleteBrokenTS.match(t) && JEConfig.getExpert()) {
//                    TreeHelper.EventDeleteBrokenTS(tree);
                } else if (moveToDiffTS.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventMoveAllToDiffCleanTS(tree);
                    t.consume();
                } else if (createMultiplierAndDifferential.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventCreateMultiplierAndDifferential(tree);
                    t.consume();
                } else if (setLimitsRecursive.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventSetLimitsRecursive(tree);
                    t.consume();
                } else if (setSubstitutionSettingsRecursive.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventSetSubstitutionSettingsRecursive(tree);
                    t.consume();
                } else if (setUnitAndPeriod.match(t) && JEConfig.getExpert()) {
                    TreeHelper.EventSetUnitAndPeriodRecursive(tree);
                    t.consume();
                } else if ((enableAll.match(t) || disableAll.match(t)) && JEConfig.getExpert()) {
                    TreeHelper.EventSetEnableAll(tree, enableAll.match(t));
                    t.consume();
                } else if (copyObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), false);
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(selectedObj.getValue().getJEVisObject().getID() + " " + selectedObj.getValue().getJEVisObject().getName());
                    clipboard.setContent(content);
                    t.consume();
                } else if (cutObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), true);
                } else if (pasteObj.match(t)) {
                    if (tree.getCopyObject() != null) {
                        if (tree.isCut()) {
                            TreeHelper.EventDrop(tree, tree.getCopyObject(), selectedObj.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.MOVE);
                        } else {
                            TreeHelper.EventDrop(tree, tree.getCopyObject(), selectedObj.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.COPY);
                        }
                        t.consume();
                    }
                } else if (rename.match(t)) {
                    LocalNameDialog localNameDialog = new LocalNameDialog(selectedObj.getValue().getJEVisObject());
                    localNameDialog.show();
                    //TreeHelper.EventRename(tree, selectedObj.getValue().getJEVisObject());
                    //t.consume();
                } else if (copyIDandValue.match(t)) {
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(selectedObj.getValue().getJEVisObject().getID() + ":Value");
                    clipboard.setContent(content);
                    t.consume();
                }


            }
        });

    }

    public static JEVisTree buildBasicDefault(JEVisDataSource ds, boolean withMinMaxTSColumn) {

        BasicCellFilter cellFilter = new BasicCellFilter(I18n.getInstance().getString("tree.filter.nofilter"));
        cellFilter.addItemFilter(new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));

        return buildBasicDefault(ds, cellFilter, withMinMaxTSColumn);
    }

    private static void addContextMenu(JEVisTree tree) {

        final JEVisTreeContextMenu contextMenu = new JEVisTreeContextMenu();
        contextMenu.setTree(tree);
        tree.setContextMenu(contextMenu);
    }

    public static JEVisTree buildBasicDefault(JEVisDataSource ds, JEVisTreeFilter filter, boolean withMinMaxTSColumn) {

        TreeTableColumn<JEVisTreeRow, JEVisTreeRow> nameCol = ColumnFactory.buildName();
        TreeTableColumn<JEVisTreeRow, Long> idCol = ColumnFactory.buildID();
        TreeTableColumn<JEVisTreeRow, String> minTS = null;
        TreeTableColumn<JEVisTreeRow, String> maxTS = null;

        if (withMinMaxTSColumn) {
            minTS = ColumnFactory.buildDataTS(false);
            maxTS = ColumnFactory.buildDataTS(true);
        }

        idCol.setVisible(false);

        if (withMinMaxTSColumn) {
            minTS.setVisible(false);
            maxTS.setVisible(false);
        }
        nameCol.setPrefWidth(460);

        JEVisTree tree = new JEVisTree(ds, filter);
        tree.getColumns().addAll(nameCol, idCol);
        tree.getSortOrder().addAll(nameCol);
        tree.setSortMode(TreeSortMode.ALL_DESCENDANTS);

        if (withMinMaxTSColumn) {
            tree.getColumns().addAll(minTS, maxTS);
        }

        addDefaultKeys(tree);
        addContextMenu(tree);

        return tree;
    }

    public static JEVisTree buildDefaultWidgetTree(JEVisDataSource ds, WidgetTreePlugin plugin) {
        TreeTableColumn<JEVisTreeRow, JEVisTreeRow> nameCol = ColumnFactory.buildName();
        nameCol.setPrefWidth(500);
        nameCol.setMinWidth(250);

        BasicCellFilter cellFilter = new BasicCellFilter("Data");
        ObjectAttributeFilter dataFilter = new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE);

        cellFilter.addItemFilter(dataFilter);

        cellFilter.addFilter(WidgetTreePlugin.COLUMN, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_SELECTED, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_NAME, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_CHART_TYPE, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_COLOR, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_MANIPULATION, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_AGGREGATION, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_CLEANING, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_ENPI, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_UNIT, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_AXIS, dataFilter);
        cellFilter.addFilter(WidgetTreePlugin.COLUMN_CUSTOM_CSS, dataFilter);
        JEVisTree tree = new JEVisTree(ds, cellFilter);

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        allFilter.add(cellFilter);

//        WidgetTreePlugin widgetTreePlugin = new WidgetTreePlugin();
        tree.getColumns().addAll(nameCol);
        tree.getSortOrder().addAll(nameCol);
        tree.getPlugins().add(plugin);

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, allFilter, finder);
        tree.setSearchFilterBar(searchBar);


        return tree;
    }


    public static JEVisTree buildDefaultGraphTree(JEVisDataSource ds, AnalysisDataModel analysisDataModel) {

        TreeTableColumn<JEVisTreeRow, JEVisTreeRow> nameCol = ColumnFactory.buildName();
        nameCol.setPrefWidth(500);
        nameCol.setMinWidth(250);
//        TreeTableColumn<JEVisTreeRow, Long> idCol = ColumnFactory.buildID();
//        TreeTableColumn<JEVisTreeRow, String> minTS = ColumnFactory.buildDataTS(false);
//        TreeTableColumn<JEVisTreeRow, String> maxTS = ColumnFactory.buildDataTS(true);

//        idCol.setVisible(false);
//        minTS.setVisible(false);
//        maxTS.setVisible(false);

        BasicCellFilter dataBasicFilter = new BasicCellFilter("Data");
        ObjectAttributeFilter dataObjectFilter = new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE);
        ObjectAttributeFilter stringDataObjectFilter = new ObjectAttributeFilter("String Data", ObjectAttributeFilter.NONE);

        dataBasicFilter.addItemFilter(dataObjectFilter);
        dataBasicFilter.addItemFilter(stringDataObjectFilter);

        dataBasicFilter.addFilter(SelectionColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(ChartTypeColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(NameColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(UnitColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(DateColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(ColorColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(DataProcessorColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(AggregationColumn.COLUMN_ID, dataObjectFilter);
        dataBasicFilter.addFilter(AxisColumn.COLUMN_ID, dataObjectFilter);

        dataBasicFilter.addFilter(SelectionColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(ChartTypeColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(NameColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(UnitColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(DateColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(ColorColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(DataProcessorColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(AggregationColumn.COLUMN_ID, stringDataObjectFilter);
        dataBasicFilter.addFilter(AxisColumn.COLUMN_ID, stringDataObjectFilter);

        JEVisTree tree = new JEVisTree(ds, dataBasicFilter);

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        allFilter.add(dataBasicFilter);

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, allFilter, finder);
        tree.setSearchFilterBar(searchBar);

        TreePlugin bp = new ChartPluginTree(analysisDataModel);
        //((ChartPluginTree) bp).setData(graphDataModel);
        tree.getColumns().addAll(nameCol);
        tree.getSortOrder().addAll(nameCol);
//                , idCol, minTS, maxTS);
        tree.getPlugins().add(bp);
//        addGraphKeys(tree);
//        addDefaultKeys(tree);

        return tree;

    }

    public static JEVisTree buildDefaultMapTree(JEVisDataSource ds) {
        logger.info("build map tree");
        JEVisTree tree = new JEVisTree(ds, FilterFactory.buildDefaultItemFilter());

//        ViewFilter filter = ViewFilterFactory.createMapFilter();
//        tree.setFiler(filter);

        TreePlugin bp = new MapPlugin();
        tree.getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID());

        tree.getPlugins().add(bp);
        return tree;

    }

    public static void addGraphKeys(JEVisTree tree) {

        final KeyCombination findNode = KeyCodeCombination.keyCombination("Ctrl+F");
        final KeyCombination showUUID = KeyCodeCombination.keyCombination("Ctrl+U");//For Debugging

        tree.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
            logger.trace("TreeEvent: {}", t.getCode());

            if (findNode.match(t)) {
                TreeHelper.EventOpenObject(tree, null);
            }
            if (showUUID.match(t)) {
                logger.info("TRee.UUID: " + tree.getUUID());
            }
        });


    }

    public static List<TreeTableColumn<JEVisTreeRow, JEVisAttribute>> createAttributeColumns(List<String> attributes) {
        Callback treeTableColumnCallback = new Callback<TreeTableColumn<JEVisTreeRow, JEVisAttribute>, TreeTableCell<JEVisTreeRow, JEVisAttribute>>() {
            @Override
            public TreeTableCell<JEVisTreeRow, JEVisAttribute> call(TreeTableColumn<JEVisTreeRow, JEVisAttribute> param) {
                TreeTableCell<JEVisTreeRow, JEVisAttribute> cell = new TreeTableCell<JEVisTreeRow, JEVisAttribute>() {


                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            Platform.runLater(() -> {
                                setText("Lade..");
                            });
                            Task updateTask = new Task() {
                                @Override
                                protected Object call() throws Exception {
                                    try {
                                        JEVisSample lastSample = item.getLatestSample();
                                        Platform.runLater(() -> {
                                            try {
                                                if (lastSample != null) {
                                                    setText(lastSample.getValueAsString());
                                                } else {
                                                    setText("");
                                                }

                                            } catch (Exception ex) {
                                                logger.error(ex);
                                            }
                                        });
                                    } catch (Exception ex) {
                                        logger.error(ex);
                                        ex.printStackTrace();
                                    }
                                    return null;
                                }
                            };

                            executor.execute(updateTask);

                        }

                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisAttribute>, ObservableValue<JEVisAttribute>>() {
            @Override
            public ObservableValue<JEVisAttribute> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisAttribute> param) {
                try {
                    if (param.getValue() != null && param.getValue().getValue() != null && param.getValue().getValue().getJEVisObject() != null) {
                        JEVisAttribute att = param.getValue().getValue().getJEVisObject().getAttribute(param.getTreeTableColumn().getId());
                        if (att != null) {
                            return new ReadOnlyObjectWrapper<>(att);
                        }
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };

        List<TreeTableColumn<JEVisTreeRow, JEVisAttribute>> result = new ArrayList<>();
        for (String attribute : attributes) {
            TreeTableColumn<JEVisTreeRow, JEVisAttribute> column = new TreeTableColumn<>(attribute);
            column.setId(attribute);

            column.setCellValueFactory(valueFactory);
            column.setCellFactory(treeTableColumnCallback);


            column.setVisible(false);
            result.add(column);
        }
        return result;
    }


    /**
     * Add an new attribute to the list id it does not allready exists.
     * TODO: for now we only check by name but we also need to check of its the same class or inherited.
     */
    public static void addAttributeSave(List<String> types, String type) {
        boolean contains = false;
        for (String attribute : types) {
            if (attribute.equals(type)) {
                contains = true;
            }
        }
        if (!contains) {
            types.add(type);
        }

    }
}
