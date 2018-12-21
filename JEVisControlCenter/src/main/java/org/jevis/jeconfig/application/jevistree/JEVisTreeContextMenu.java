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
package org.jevis.jeconfig.application.jevistree;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dimpex.DimpEX;
import org.jevis.commons.dimpex.DimpexObject;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.ImageConverter;
import org.jevis.jeconfig.dialog.JsonExportDialog;
import org.jevis.jeconfig.tool.I18n;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeContextMenu extends ContextMenu {
    private static final Logger logger = LogManager.getLogger(JEVisTreeContextMenu.class);

    private JEVisObject _obj;
    private JEVisTree _tree;

    public JEVisTreeContextMenu(JEVisObject obj, JEVisTree tree) {
        super();

        _obj = obj;
        _tree = tree;


//        getItems().add(buildMenuNew());
        getItems().setAll(
                buildNew2(),
                buildReload(),
                new SeparatorMenuItem(),
                buildDelete(),
                buildRename(),
                buildCopy(),
                buildPaste(),
                new SeparatorMenuItem(),
                buildMenuExport(),
                buildImport()
        );

        try {
            if (obj.getJEVisClassName().equals("Calculation")) {
                getItems().add(new SeparatorMenuItem());
                getItems().add(buildMenuAddInput());
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }

    }

    private MenuItem buildPaste() {
        //TODO: disable if not allowed
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.paste"), ResourceLoader.getImage("17_Paste_48x48.png", 20, 20));

        menu.setOnAction(new EventHandler<ActionEvent>() {

                             @Override
                             public void handle(ActionEvent t) {
//                final TreeItem<JEVisTreeRow> obj = ((TreeItem<JEVisTreeRow>) _tree.getSelectionModel().getSelectedItem());
                                 TreeHelper.EventDrop(_tree, _tree.getCopyObject(), _obj, CopyObjectDialog.DefaultAction.COPY);
                             }
                         }
        );
        return menu;
    }

    private MenuItem buildCopy() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.copy"), ResourceLoader.getImage("16_Copy_48x48.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {

                             @Override
                             public void handle(ActionEvent t) {
                                 _tree.setCopyObject(_obj, false);
                             }
                         }
        );
        return menu;
    }

    private MenuItem buildExport() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.export"), ResourceLoader.getImage("1401894975_Export.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {

                             @Override
                             public void handle(ActionEvent t) {


                                 JsonExportDialog dia = new JsonExportDialog("Import", _obj);
                             }
                         }
        );
        return menu;
    }

    private MenuItem buildImport() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.import"), ResourceLoader.getImage("1401894975_Export.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
                             @Override
                             public void handle(ActionEvent t) {

                                 FileChooser fileChooser = new FileChooser();
                                 fileChooser.setTitle("Open JEVis File");
                                 fileChooser.getExtensionFilters().addAll(
                                         new FileChooser.ExtensionFilter("JEVis File", "*.json"),
                                         new FileChooser.ExtensionFilter("All Files", "*.*"));
                                 File selectedFile = fileChooser.showOpenDialog(null);
                                 if (selectedFile != null) {
                                     try {
                                         List<DimpexObject> objects = DimpEX.readFile(selectedFile);
                                         DimpEX.importALL(_obj.getDataSource(), objects, _obj);
                                     } catch (Exception ex) {
                                         logger.fatal(ex);
                                     }
                                 }

                             }
                         }
        );
        return menu;
    }

    public List<MenuItem> buildMenuNewContent() {
        List<MenuItem> newContent = new ArrayList<>();
        try {
            for (JEVisClass jlass : _obj.getAllowedChildrenClasses()) {
                MenuItem classItem;

                classItem = new CheckMenuItem(jlass.getName(), getIcon(jlass));
                classItem.setOnAction(new EventHandler<ActionEvent>() {

                                          @Override
                                          public void handle(ActionEvent t) {
                                              TreeHelper.EventNew(_tree, _obj);
                                          }
                                      }
                );
                newContent.add(classItem);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        return newContent;
    }

    public MenuItem buildMenuAddInput() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.addinput"), ResourceLoader.getImage("1401894975_Export.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    TreeHelper.createCalcInput(_obj, null);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }
        });

        return menu;
    }


    public MenuItem buildMenuExport() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.export"), ResourceLoader.getImage("1401894975_Export.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Platform.runLater(() -> {
                    try {
                        TreeHelper.EventExportTree(_obj);
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                });


            }
        });

        return menu;
    }


    private Menu buildMenuNew() {
        Menu addMenu = new Menu(I18n.getInstance().getString("jevistree.menu.new"), ResourceLoader.getImage("list-add.png", 20, 20));
        addMenu.getItems().addAll(buildMenuNewContent());

        return addMenu;

    }

    private ImageView getIcon(JEVisClass jclass) {
        try {
            return ImageConverter.convertToImageView(jclass.getIcon(), 20, 20);
        } catch (Exception ex) {
            return ResourceLoader.getImage("1393615831_unknown2.png", 20, 20);
        }
    }

    private MenuItem buildProperties() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.expand")
        );//shoud be edit but i use it for expand for the time
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

    private MenuItem buildNew2() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.new"), ResourceLoader.getImage("list-add.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

                TreeHelper.EventNew(_tree, _obj);

            }
        });
        return menu;
    }

    private MenuItem buildRename() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.rename"), ResourceLoader.getImage("Rename.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                TreeHelper.EventRename(_tree, _obj);
            }
        });
        return menu;
    }

    private MenuItem buildReload() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.reload"), ResourceLoader.getImage("1476369770_Sync.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                TreeHelper.EventReload(_obj);
            }
        });
        return menu;
    }

    private MenuItem buildDelete() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.delete"), ResourceLoader.getImage("list-remove.png", 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TreeHelper.EventDelete(_tree);
            }
        });
        return menu;
    }

}
