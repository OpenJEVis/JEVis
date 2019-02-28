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

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.*;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.filter.BasicCellFilter;
import org.jevis.jeconfig.application.jevistree.filter.FilterFactory;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.filter.ObjectAttributeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPlugin;
import org.jevis.jeconfig.application.jevistree.plugin.MapPlugin;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeFactory {

    public final static KeyCombination findNode = KeyCodeCombination.keyCombination("Ctrl+F");
    public final static KeyCombination findAgain = new KeyCodeCombination(KeyCode.F3);
    private static final Logger logger = LogManager.getLogger(JEVisTreeFactory.class);


    public static void addDefaultKeys(JEVisTree tree) {

        final KeyCombination copyID = new KeyCodeCombination(KeyCode.F1);
        final KeyCombination copyObj = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        final KeyCombination cutObj = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
        final KeyCombination pasteObj = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination add = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        final KeyCombination rename = new KeyCodeCombination(KeyCode.F2);
        final KeyCombination delete = new KeyCodeCombination(KeyCode.DELETE);
        final KeyCombination pageDown = new KeyCodeCombination(KeyCode.PAGE_DOWN);

        logger.error("Add Tree Hotkeys to: " + tree);
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
                } else if (findAgain.match(t)) {
                    if (tree.getSearchFilterBar() != null) {
                        tree.getSearchFilterBar().goNext();
                    }
                } else if (add.match(t)) {
                    TreeHelper.EventNew(tree, selectedObj.getValue().getJEVisObject());
                } else if (delete.match(t)) {
                    TreeHelper.EventDelete(tree);
                } else if (copyObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), false);
                } else if (cutObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), true);
                } else if (pasteObj.match(t)) {
                    if (tree.getCopyObject() != null) {
                        if (tree.isCut()) {
                            TreeHelper.EventDrop(tree, tree.getCopyObject(), selectedObj.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.MOVE);
                        } else {
                            TreeHelper.EventDrop(tree, tree.getCopyObject(), selectedObj.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.COPY);
                        }
                    }
                } else if (rename.match(t)) {
                    TreeHelper.EventRename(tree, selectedObj.getValue().getJEVisObject());
                }
            }
        });

    }

    public static JEVisTree buildBasicDefault(JEVisDataSource ds, boolean withMinMaxTSColumn) {

        BasicCellFilter cellFilter = new BasicCellFilter(I18n.getInstance().getString("tree.filter.nofilter"));
        cellFilter.addItemFilter(new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));

        return buildBasicDefault(ds, cellFilter, withMinMaxTSColumn);
    }

    public static JEVisTree buildBasicDefault(JEVisDataSource ds, JEVisTreeFilter filter, boolean withMinMaxTSColumn) {

        TreeTableColumn<JEVisTreeRow, String> nameCol = ColumnFactory.buildName();
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

//        BasicCellFilter cellFilter = new BasicCellFilter("All");
//        cellFilter.addItemFilter(new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE));
//
//        FilterFactory.addDefaultObjectTreeFilter(cellFilter, nameCol);
//        FilterFactory.addDefaultObjectTreeFilter(cellFilter, idCol);
        JEVisTree tree = new JEVisTree(ds, filter);

        tree.getColumns().addAll(nameCol, idCol);
        if (withMinMaxTSColumn) {
            tree.getColumns().addAll(minTS, maxTS);
        }

        addDefaultKeys(tree);

        return tree;
    }


    public static JEVisTree buildDefaultGraphTree(JEVisDataSource ds, GraphDataModel graphDataModel) {

        TreeTableColumn<JEVisTreeRow, String> nameCol = ColumnFactory.buildName();
        nameCol.setPrefWidth(500);
        nameCol.setMinWidth(250);
//        TreeTableColumn<JEVisTreeRow, Long> idCol = ColumnFactory.buildID();
//        TreeTableColumn<JEVisTreeRow, String> minTS = ColumnFactory.buildDataTS(false);
//        TreeTableColumn<JEVisTreeRow, String> maxTS = ColumnFactory.buildDataTS(true);

//        idCol.setVisible(false);
//        minTS.setVisible(false);
//        maxTS.setVisible(false);

        BasicCellFilter cellFilter = new BasicCellFilter("Data");
        ObjectAttributeFilter dataFilter = new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE);

        cellFilter.addItemFilter(dataFilter);

        cellFilter.addFilter(SelectionColumn.COLUMN_ID, dataFilter);
        cellFilter.addFilter(UnitColumn.COLUMN_ID, dataFilter);
        cellFilter.addFilter(DateColumn.COLUMN_ID, dataFilter);
        cellFilter.addFilter(ColorColumn.COLUMN_ID, dataFilter);
        cellFilter.addFilter(DataProcessorColumn.COLUMN_ID, dataFilter);
        cellFilter.addFilter(AggregationColumn.COLUMN_ID, dataFilter);
        cellFilter.addFilter(AxisColumn.COLUMN_ID, dataFilter);


        JEVisTree tree = new JEVisTree(ds, cellFilter);

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        allFilter.add(cellFilter);

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, allFilter, finder);
        tree.setSearchFilterBar(searchBar);

        TreePlugin bp = new ChartPlugin(graphDataModel);
        //((ChartPlugin) bp).setData(graphDataModel);
        tree.getColumns().addAll(nameCol);
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
}
