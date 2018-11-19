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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.resource.ResourceLoader;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ColumnFactory {

    public static final String OBJECT_NAME = "Name";
    public static final String OBJECT_ID = "ID";
    public static final String COLOR = "Color";
    public static final String OBJECT_CLASS = "Type";
    public static final String SELECT_OBJECT = "Select";
    public static final String ATTRIBUTE_LAST_MOD = "Last Modified";
    private static final Logger logger = LogManager.getLogger(ColumnFactory.class);
    private static final Map<String, Image> icons = new HashMap<>();
    private static final DateTimeFormatter TS_DATES_FORMATE = DateTimeFormat.forPattern("yyyy-MM-dd HH:MM");
    private static String fallbackIcon = "b86bebea-1880-11e8-accf-0ed5f89f718b";
    private static Map<String, Image> classIconCache = new HashMap<>();

    public static TreeTableColumn<JEVisTreeRow, String> buildName() {
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn(OBJECT_NAME);
        column.setId(OBJECT_NAME);
        column.setPrefWidth(300);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, String> p) -> {
            try {
                if (p != null && p.getValue() != null && p.getValue().getValue() != null && p.getValue().getValue().getJEVisObject() != null) {
                    TreeItem<JEVisTreeRow> item = p.getValue();
                    JEVisTreeRow selectionObject = item.getValue();

                    if (selectionObject.getType() == JEVisTreeRow.TYPE.OBJECT) {
                        JEVisObject obj = selectionObject.getJEVisObject();
                        return new ReadOnlyObjectWrapper<String>(obj.getName());
                    } else if (selectionObject.getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                        JEVisAttribute att = selectionObject.getJEVisAttribute();
                        return new ReadOnlyObjectWrapper<String>(att.getName());
                    } else {
                        return new ReadOnlyObjectWrapper<String>("");
                    }
                } else {
                    return new ReadOnlyObjectWrapper<String>("Null");
                }

            } catch (Exception ex) {
                logger.info("Error in Column Factory: " + ex);
                return new ReadOnlyObjectWrapper<String>("Error");
            }

        });


        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>>() {

                                  @Override
                                  public TreeTableCell<JEVisTreeRow, String> call(TreeTableColumn<JEVisTreeRow, String> param) {

                                      TreeTableCell<JEVisTreeRow, String> cell = new TreeTableCell<JEVisTreeRow, String>() {

                                          @Override
                                          public void commitEdit(String newValue) {
                                              super.commitEdit(newValue);
                                          }

                                          @Override
                                          protected void updateItem(String item, boolean empty) {
                                              super.updateItem(item, empty);
                                              setText(null);
                                              setGraphic(null);
                                              if (!empty
                                                      && getTreeTableRow() != null
                                                      && getTreeTableRow().getTreeItem() != null
                                                      && getTreeTableRow().getTreeItem().getValue() != null
                                                      && getTreeTableRow().getTreeItem().getValue().getJEVisObject() != null) {


                                                  try {
                                                      HBox hbox = new HBox();//10
                                                      Label nameLabel = new Label();
                                                      Node icon = new Region();

                                                      JEVisTree tree = (JEVisTree) getTreeTableRow().getTreeTableView();
                                                      JEVisObject obj = getTreeTableRow().getTreeItem().getValue().getJEVisObject();

//                                                      boolean showCell = tree.getCellFilter().showCell(getTableColumn(), getTreeTableRow().getTreeItem().getValue());
//                                                      showCell = true;

                                                      setContextMenu(new JEVisTreeContextMenu(obj, tree));

                                                      hbox.setStyle("-fx-background-color: transparent;");
                                                      nameLabel.setStyle("-fx-background-color: transparent;");

                                                      nameLabel.setText(item);
                                                      nameLabel.setPadding(new Insets(0, 0, 0, 8));


                                                      if (getTreeTableRow().getItem().getType() == JEVisTreeRow.TYPE.OBJECT) {
                                                          try {
                                                              if (getTreeTableRow().getItem().getJEVisObject().getJEVisClassName().equals("Link")) {
                                                                  JEVisObject linkedObject = getTreeTableRow().getItem().getJEVisObject().getLinkedObject();
                                                                  icon = getClassIcon(linkedObject.getJEVisClass(), 18, 18);

                                                              } else {
                                                                  icon = getClassIcon(getTreeTableRow().getItem().getJEVisObject().getJEVisClass(), 18, 18);
                                                              }
                                                          } catch (Exception ex) {
                                                              icon = ResourceLoader.getImage("1393615831_unknown2.png", 18, 18);
                                                          }
                                                      } else {//Attribute

                                                          HBox hbox2 = new HBox(10);
                                                          Region spacer = new Region();
                                                          spacer.setPrefWidth(12);
                                                          hbox2.getChildren().setAll(spacer, ResourceLoader.getImage("down_right-24.png", 10, 10));
                                                          icon = hbox2;
                                                      }

                                                      hbox.getChildren().setAll(icon, nameLabel);
                                                      setGraphic(hbox);

                                                  } catch (Exception ex) {
                                                      logger.catching(ex);
                                                  }
                                              }
                                          }
                                      };

                                      return cell;
                                  }
                              }
        );


        return column;

    }

    /**
     * @param max if 1 the max date, if false the min date
     * @return
     */
    public static TreeTableColumn<JEVisTreeRow, String> buildDataTS(boolean max) {
        String coumnName = "Value Max";
        if (max) {
            coumnName = "Max Value TS";
        } else {
            coumnName = "Min Value TS";
        }


        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn(coumnName);
        column.setId(coumnName);
        column.setPrefWidth(135);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, String> p) -> {
            try {
                if (p != null && p.getValue() != null && p.getValue().getValue() != null && p.getValue().getValue().getJEVisObject() != null) {
                    TreeItem<JEVisTreeRow> item = p.getValue();
                    JEVisTreeRow selectionObject = item.getValue();

                    JEVisAttribute value = null;

                    if (selectionObject.getType() == JEVisTreeRow.TYPE.OBJECT) {
                        JEVisObject obj = selectionObject.getJEVisObject();
                        value = obj.getAttribute("Value");
                    } else if (selectionObject.getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                        JEVisAttribute att = selectionObject.getJEVisAttribute();
                        if (att.getName().equals("Value")) {
                            value = att;
                        }
                    }

                    if (value != null) {
                        if (max == true) {
                            return new ReadOnlyObjectWrapper<String>(TS_DATES_FORMATE.print(value.getTimestampFromLastSample()));
                        } else {
                            return new ReadOnlyObjectWrapper<String>(TS_DATES_FORMATE.print(value.getTimestampFromFirstSample()));
                        }

                    } else {
                        return new ReadOnlyObjectWrapper<String>("");
                    }

                } else {
                    return new ReadOnlyObjectWrapper<String>("Null");
                }

            } catch (Exception ex) {
                logger.info("Error in Column Factory: " + ex);
                return new ReadOnlyObjectWrapper<String>("Error");
            }

        });


        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>>() {

                                  @Override
                                  public TreeTableCell<JEVisTreeRow, String> call(TreeTableColumn<JEVisTreeRow, String> param) {

                                      TreeTableCell<JEVisTreeRow, String> cell = new TreeTableCell<JEVisTreeRow, String>() {

                                          @Override
                                          public void commitEdit(String newValue) {
                                              super.commitEdit(newValue);
                                          }

                                          @Override
                                          protected void updateItem(String item, boolean empty) {
                                              super.updateItem(item, empty);
                                              setText(null);
                                              setGraphic(null);
                                              if (!empty
                                                      && getTreeTableRow() != null
                                                      && getTreeTableRow().getTreeItem() != null
                                                      && getTreeTableRow().getTreeItem().getValue() != null
                                                      && getTreeTableRow().getTreeItem().getValue().getJEVisObject() != null) {


                                                  try {
                                                      HBox hbox = new HBox();//10
                                                      Label nameLabel = new Label();
//                                                      Node icon = new Region();

//                                                      JEVisTree tree = (JEVisTree) getTreeTableRow().getTreeTableView();
//                                                      JEVisObject obj = getTreeTableRow().getTreeItem().getValue().getJEVisObject();

//                                                      boolean showCell = tree.getCellFilter().showCell(getTableColumn(), getTreeTableRow().getTreeItem().getValue());
//                                                      showCell = true;

//                                                      setContextMenu(new JEVisTreeContextMenu(obj, tree));

                                                      hbox.setStyle("-fx-background-color: transparent;");
                                                      nameLabel.setStyle("-fx-background-color: transparent;");

                                                      nameLabel.setText(item);
                                                      nameLabel.setPadding(new Insets(0, 0, 0, 8));


                                                      hbox.getChildren().setAll(nameLabel);
                                                      setGraphic(hbox);

                                                  } catch (Exception ex) {
                                                      logger.catching(ex);
                                                  }
                                              }
                                          }
                                      };

                                      return cell;
                                  }
                              }
        );


        return column;

    }

    private static ImageView getClassIcon(JEVisClass jclass, double h, double w) throws JEVisException {
        try {
            if (!classIconCache.containsKey(jclass.getName())) {
                classIconCache.put(jclass.getName(), SwingFXUtils.toFXImage(jclass.getIcon(), null));
            }

        } catch (Exception ex) {
            classIconCache.put(jclass.getName(), new Image(ColumnFactory.class.getResourceAsStream("/icons/Folder_blank.png")));
        }


        ImageView iv = new ImageView(classIconCache.get(jclass.getName()));
        iv.fitHeightProperty().setValue(h);
        iv.fitWidthProperty().setValue(w);
        iv.setSmooth(true);
        return iv;


//        try {
//            Image image = null;
//            if (jclass == null || jclass.getIcon() == null) {
//                if (icons.containsKey(fallbackIcon)) {
//                    image = icons.get(fallbackIcon);
//                } else {
//                    icons.put(fallbackIcon, new Image(ColumnFactory.class.getResourceAsStream("/icons/Folder_blank.png")));
//                    image = icons.get(fallbackIcon);
//                }
//            } else if (icons.containsKey(jclass.getName())) {
//                image = icons.get(jclass.getName());
//            } else if (jclass.getIcon() != null) {
//                image = SwingFXUtils.toFXImage(jclass.getIcon(), null);
//                icons.put(jclass.getName(), image);
//            }
//            ImageView iv = new ImageView(image);
//            iv.fitHeightProperty().setValue(h);
//            iv.fitWidthProperty().setValue(w);
//            iv.setSmooth(true);
//            return iv;
//        } catch (Exception ex) {
//            logger.catching(ex);
//            return new ImageView();
//        }
    }

    public static TreeTableColumn<JEVisTreeRow, String> buildClass() {
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn(OBJECT_CLASS);
        column.setId(OBJECT_CLASS);
        column.setPrefWidth(190);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, String> p) -> {
            try {
                if (p != null && p.getValue() != null && p.getValue().getValue() != null && p.getValue().getValue().getJEVisObject() != null) {
                    TreeItem<JEVisTreeRow> item = p.getValue();
                    JEVisTreeRow selectionObject = item.getValue();
//                    JEVisObject obj = selectionObject.getJEVisObject();
//                    return new ReadOnlyObjectWrapper<String>(obj.getJEVisClass().getName());

                    if (selectionObject.getType() == JEVisTreeRow.TYPE.OBJECT) {
                        JEVisObject obj = selectionObject.getJEVisObject();
                        return new ReadOnlyObjectWrapper<String>(obj.getJEVisClass().getName());
                    } else if (selectionObject.getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                        JEVisAttribute att = selectionObject.getJEVisAttribute();
                        return new ReadOnlyObjectWrapper<String>(att.getType().getName());
                    } else {
                        return new ReadOnlyObjectWrapper<String>("");
                    }

                } else {
                    return new ReadOnlyObjectWrapper<String>("Null");
                }

            } catch (Exception ex) {
                logger.info("Error in Column Fatory: " + ex);
                return new ReadOnlyObjectWrapper<String>("Error");
            }

        });
        return column;

    }

    public static TreeTableColumn<JEVisTreeRow, Long> buildBasicGraph(JEVisTree tree) {
        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn("JEGraph");

        column.getColumns().addAll(buildColor(tree), buildBasicRowSelection(tree));

        return column;

    }

    public static TreeTableColumn<JEVisTreeRow, Long> buildID() {
        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn(OBJECT_ID);
        column.setId(OBJECT_ID);
        column.setPrefWidth(70);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, Long> p) -> {
            try {
                if (p != null && p.getValue() != null && p.getValue().getValue() != null && p.getValue().getValue().getJEVisObject() != null) {
                    TreeItem<JEVisTreeRow> item = p.getValue();
                    JEVisTreeRow selectionObject = item.getValue();
                    JEVisObject obj = selectionObject.getJEVisObject();
                    return new ReadOnlyObjectWrapper<Long>(obj.getID());

//                    if (p.getValue().getValue().getType() == SelectionTreeRow.TYPE.OBJECT) {
//                        JEVisObject obj = selectionObject.getJEVisObject();
//                        return new ReadOnlyObjectWrapper<Long>(obj.getID());
//                    } else {
//                        return new ReadOnlyObjectWrapper<Long>(-3l);
//                    }
                } else {
                    return new ReadOnlyObjectWrapper<Long>(-2l);
                }

            } catch (Exception ex) {
                logger.info("Error in Column Fatory: " + ex);
                return new ReadOnlyObjectWrapper<Long>(-1l);
            }

        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Long>, TreeTableCell<JEVisTreeRow, Long>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Long> call(TreeTableColumn<JEVisTreeRow, Long> param) {

                TreeTableCell<JEVisTreeRow, Long> cell = new TreeTableCell<JEVisTreeRow, Long>() {

                    StackPane hbox = new StackPane();
                    Label label = new Label();

                    @Override
                    public void commitEdit(Long newValue) {
                        super.commitEdit(newValue);
//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            hbox.getChildren().setAll(label);
                            StackPane.setAlignment(hbox, Pos.CENTER_RIGHT);
                            if (getTreeTableRow().getItem() != null) {
                                if (getTreeTableRow().getItem().getType() == JEVisTreeRow.TYPE.OBJECT) {
                                    label.setText(item + "");
                                } else {
                                    label.setText("");
                                }
                            }

                            hbox.setStyle("-fx-background-color: transparent;");
                            label.setStyle("-fx-background-color: transparent;");
                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    public static TreeTableColumn<JEVisTreeRow, Color> buildColor(JEVisTree tree) {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(COLOR);
        column.setId(COLOR);
        column.setPrefWidth(130);
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, Color>, ObservableValue<Color>>() {

            @Override
            public ObservableValue<Color> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, Color> param) {
                return param.getValue().getValue().getColorProperty();
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                TreeTableCell<JEVisTreeRow, Color> cell = new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), COLOR)) {
                                ColorPicker colorPicker = new ColorPicker();
                                hbox.getChildren().setAll(colorPicker);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                colorPicker.setValue(item);

                                colorPicker.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        commitEdit(colorPicker.getValue());
                                    }
                                });
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    public static TreeTableColumn<JEVisTreeRow, Boolean> buildBasicRowSelection(JEVisTree tree) {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(SELECT_OBJECT);
        column.setId(SELECT_OBJECT);
        column.setPrefWidth(60);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean> param) -> param.getValue().getValue().getObjectSelectedProperty());

        column.setEditable(true);

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                TreeTableCell<JEVisTreeRow, Boolean> cell = new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().getObjectSelectedProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            CheckBox cbox = new CheckBox();

                            if (getTreeTableRow().getItem() != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), SELECT_OBJECT)) {
                                hbox.getChildren().setAll(cbox);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                cbox.setSelected(item);

                                cbox.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        commitEdit(cbox.isSelected());
                                    }
                                });
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

}
