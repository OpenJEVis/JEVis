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
package org.jevis.application.jevistree;

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
import org.jevis.application.jevistree.filter.BasicCellFilter;
import org.jevis.application.jevistree.filter.CellFilterFactory;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.jevis.application.jevistree.plugin.MapPlugin;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeFactory {

    public final static KeyCombination findNode = KeyCodeCombination.keyCombination("Ctrl+F");
    public final static KeyCombination findAgain = new KeyCodeCombination(KeyCode.F3);
    private static final Logger logger = LogManager.getLogger(JEVisTreeFactory.class);
    private static KeyCombination lastCombination = null;

    public static void addDefaultKeys(JEVisTree tree) {

        final KeyCombination copyID = new KeyCodeCombination(KeyCode.F1);
        final KeyCombination copyObj = KeyCodeCombination.keyCombination("Ctrl+C");
        final KeyCombination cutObj = KeyCodeCombination.keyCombination("Ctrl+X");
        final KeyCombination pasteObj = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination add = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        final KeyCombination rename = new KeyCodeCombination(KeyCode.F2);
        final KeyCombination delete = new KeyCodeCombination(KeyCode.DELETE);
        final KeyCombination pageDown = new KeyCodeCombination(KeyCode.PAGE_DOWN);

        tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                logger.trace("TreeEvent: {}", t.getCode());

                final TreeItem<JEVisTreeRow> selectedObj = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());

                if (findNode.match(t)) {
                    TreeHelper.EventOpenObject(tree, findNode);
                    lastCombination = findNode;
                } else if (findAgain.match(t)) {
                    if (lastCombination.equals(findNode) || lastCombination.equals(findAgain)) {
                        TreeHelper.EventOpenObject(tree, findAgain);
                        lastCombination = findAgain;
                    }
                } else if (add.match(t)) {
                    TreeHelper.EventNew(tree, selectedObj.getValue().getJEVisObject());
                    lastCombination = add;
                } else if (delete.match(t)) {
                    TreeHelper.EventDelete(tree);
                    lastCombination = delete;
                } else if (copyObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject());
                    lastCombination = copyObj;
                } else if (cutObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject());
                    lastCombination = cutObj;
                } else if (pasteObj.match(t)) {
                    if (lastCombination.equals(copyObj)) {
                        final TreeItem<JEVisTreeRow> obj = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());
                        TreeHelper.EventDrop(tree, tree.getCopyObject(), obj.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.COPY);
                    } else if (lastCombination.equals(cutObj)) {
                        final TreeItem<JEVisTreeRow> obj = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());
                        TreeHelper.EventDrop(tree, tree.getCopyObject(), obj.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.MOVE);
                    }
                    lastCombination = pasteObj;
                } else if (rename.match(t)) {
                    TreeHelper.EventRename(tree, selectedObj.getValue().getJEVisObject());
                    lastCombination = rename;
                }
            }
        });

    }

    public static JEVisTree buildBasicDefault(JEVisDataSource ds) {
        JEVisTree tree = new JEVisTree(ds);

        ViewFilter filter = ViewFilterFactory.createDefaultGraphFilter();
        tree.setFiler(filter);

        TreeTableColumn nameCol = ColumnFactory.buildName();
        TreeTableColumn idCol = ColumnFactory.buildID();

        BasicCellFilter cellFilter = new BasicCellFilter();
        CellFilterFactory.addDefaultObjectTreeFilter(cellFilter, nameCol);
        CellFilterFactory.addDefaultObjectTreeFilter(cellFilter, idCol);
        tree.setCellFilter(cellFilter);

        tree.getColumns().addAll(nameCol, idCol);
        addDefaultKeys(tree);

        return tree;

    }

    public static JEVisTree buildDefaultGraphTree(JEVisDataSource ds) {
        JEVisTree tree = new JEVisTree(ds);

        ViewFilter filter = ViewFilterFactory.createDefaultGraphFilter();
        tree.setFiler(filter);

        TreePlugin bp = new ChartPlugin();

        tree.getColumns().addAll(ColumnFactory.buildName());

        tree.getPlugins().add(bp);

        addGraphKeys(tree);

        return tree;

    }

    public static JEVisTree buildDefaultMapTree(JEVisDataSource ds) {
        logger.info("build map tree");
        JEVisTree tree = new JEVisTree(ds);

        ViewFilter filter = ViewFilterFactory.createMapFilter();
        tree.setFiler(filter);

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
                System.out.println("TRee.UUID: " + tree.getUUID());
            }
        });


    }
}
