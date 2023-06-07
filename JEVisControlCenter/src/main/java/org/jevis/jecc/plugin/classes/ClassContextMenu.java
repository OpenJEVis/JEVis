/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.plugin.classes;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.jevis.api.JEVisClass;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.tool.ImageConverter;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassContextMenu extends ContextMenu {

    private JEVisClass _jclass;
    private TreeItem<JEVisClass> _item;
    private ClassTree _tree;

    public ClassContextMenu(JEVisClass item, ClassTree tree) {
        super();
        _jclass = item;
        _tree = tree;
        getItems().add(buildMenuExport());
//        getItems().add(buildMenuNew());
//        getItems().add(new SeparatorMenuItem());
//        getItems().add(buildDelete());
//        getItems().add(buildRename());
//        getItems().add(buildProperties())
//        getItems().setAll(buildMenuNew(), new SeparatorMenuItem(), buildDelete(), buildRename());

    }

    public Menu buildMenuExport() {
        Menu addMenu = new Menu(I18n.getInstance().getString("plugin.classes.contextmenu.export"), ControlCenter.getImage("save.gif", 20, 20));
        addMenu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                _tree.fireEventExport(_tree.getSelectionModel().getSelectedItems());
            }
        });

//        logger.info("Disable menu: " + !JEConfig.getCurrentUser().isSysAdmin());
        try {
            addMenu.setDisable(!_jclass.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {
            ex.printStackTrace();
            addMenu.setDisable(true);
        }

        return addMenu;

    }

    public Menu buildMenuNew() {
        Menu addMenu = new Menu(I18n.getInstance().getString("plugin.classes.contextmenu.new"), ControlCenter.getImage("list-add.png", 20, 20));
        addMenu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                _tree.fireEventNew(_tree.getSelectionModel().getSelectedItem());
            }
        });

//        logger.info("Disable menu: " + !JEConfig.getCurrentUser().isSysAdmin());
        try {
            addMenu.setDisable(!ControlCenter.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {

        }

        return addMenu;

    }

    private ImageView getIcon(JEVisClass jclass) {
        try {
            return ImageConverter.convertToImageView(jclass.getIcon(), 20, 20);
        } catch (Exception ex) {
            return ControlCenter.getImage("1393615831_unknown2.png", 20, 20);
        }
    }

    private MenuItem buildProperties() {
        MenuItem menu = new MenuItem("Expand");//shoud be edit but i use it for expand for the time
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                PopOver popup = new PopOver(new HBox());
//                popup.show(_item.getGraphic(), 200d, 200d, Duration.seconds(1));
                //TMP test

//                logger.info("expand all");
//                _item.expandAll(true);
            }
        });
        return menu;
    }

    private MenuItem buildRename() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("plugin.classes.contextmenu.rename"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

//                _tree.edit(_item);
                //workaround
                if (_tree instanceof ClassTree) {
                    _tree.fireEventRename();
                }

            }
        });
        try {
            menu.setDisable(!ControlCenter.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {

        }

        return menu;
    }

    private MenuItem buildDelete() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("plugin.classes.contextmenu.delete"),
                ControlCenter.getImage("list-remove.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                _tree.fireDelete(_jclass);
            }
        });
        try {
            menu.setDisable(!ControlCenter.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {

        }
        return menu;
    }
}
