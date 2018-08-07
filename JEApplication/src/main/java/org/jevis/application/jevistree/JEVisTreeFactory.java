/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
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
import org.jevis.application.jevistree.plugin.BarchartPlugin;
import org.jevis.application.jevistree.plugin.MapPlugin;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeFactory {

    public static Logger LOGGER = LogManager.getLogger(JEVisTreeFactory.class);

    public static void addDefaultKeys(JEVisTree tree) {

        final KeyCombination copyID = new KeyCodeCombination(KeyCode.F1);
        final KeyCombination copyObj = KeyCodeCombination.keyCombination("Ctrl+C");
        final KeyCombination pasteObj = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination add = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        final KeyCombination rename = new KeyCodeCombination(KeyCode.F2);
        final KeyCombination delete = new KeyCodeCombination(KeyCode.DELETE);
        final KeyCombination pageDown = new KeyCodeCombination(KeyCode.PAGE_DOWN);
        final KeyCombination findNode = KeyCodeCombination.keyCombination("Ctrl+F");

        tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                LOGGER.trace("TreeEvent: {}", t.getCode());

                final TreeItem<JEVisTreeRow> selectedObj = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());

                if (findNode.match(t)) {
                    TreeHelper.EventOpenObject(tree);
                } else if (add.match(t)) {
                    TreeHelper.EventNew(tree, selectedObj.getValue().getJEVisObject());
                } else if (delete.match(t)) {
                    TreeHelper.EventDelete(tree);
                } else if (copyObj.match(t)) {
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject());
                } else if (pasteObj.match(t)) {
                    final TreeItem<JEVisTreeRow> obj = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());
                    TreeHelper.EventDrop(tree, tree.getCopyObject(), obj.getValue().getJEVisObject(),CopyObjectDialog.DefaultAction.COPY);
                } else if (rename.match(t)) {
                    TreeHelper.EventRename(tree, selectedObj.getValue().getJEVisObject());
                }
            }
        });

    }

    public static JEVisTree buildBasicDefault(JEVisDataSource ds) {
        JEVisTree tree = new JEVisTree(ds);

        ViewFilter filter = ViewFilterFactory.createDefaultGraphFilter();
        tree.setFiler(filter);

        tree.getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID());
        addDefaultKeys(tree);

        return tree;

    }

    public static JEVisTree buildDefaultGraphTree(JEVisDataSource ds) {
        JEVisTree tree = new JEVisTree(ds);

        ViewFilter filter = ViewFilterFactory.createDefaultGraphFilter();
        tree.setFiler(filter);

        TreePlugin bp = new BarchartPlugin();
//        bp.setTree(tree);

//        getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID(), ColumnFactory.buildClass(), ColumnFactory.buildColor(this), ColumnFactory.buildBasicRowSelection(this));
//        tree.getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID(), ColumnFactory.buildClass(), ColumnFactory.buildBasicGraph(tree));
        tree.getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID());

        tree.getPlugins().add(bp);

//        for (TreeTableColumn<SelectionTreeRow, Long> column : bp.getColumns()) {
//            tree.getColumns().add(column);
//        }
        return tree;

    }

    public static JEVisTree buildDefaultMapTree(JEVisDataSource ds) {
        System.out.println("build map tree");
        JEVisTree tree = new JEVisTree(ds);

        ViewFilter filter = ViewFilterFactory.createMapFilter();
        tree.setFiler(filter);

        TreePlugin bp = new MapPlugin();
        tree.getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID());

        tree.getPlugins().add(bp);
        return tree;

    }

}
