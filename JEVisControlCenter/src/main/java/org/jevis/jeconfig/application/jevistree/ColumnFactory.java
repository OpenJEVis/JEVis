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
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ColumnFactory {

    private static final Logger logger = LogManager.getLogger(ColumnFactory.class);

    private static final String OBJECT_NAME = "Name";
    private static final String OBJECT_ID = "ID";
    private static final String OBJECT_CLASS = "Type";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:MM");
    private static final Image ATTRIBUTE_ICON = ResourceLoader.getImage("graphic-design.png");
    private static final Map<String, Image> classIconCache = new HashMap<>();

    public static TreeTableColumn<JEVisTreeRow, String> buildName() {
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn<>(OBJECT_NAME);
        column.setId(OBJECT_NAME);
        column.setPrefWidth(460);
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

        final JEVisTreeContextMenu contextMenu = new JEVisTreeContextMenu();

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>>() {

                                  @Override
                                  public TreeTableCell<JEVisTreeRow, String> call(TreeTableColumn<JEVisTreeRow, String> param) {

                                      return new TreeTableCell<JEVisTreeRow, String>() {

                                          @Override
                                          public void commitEdit(String newValue) {
                                              super.commitEdit(newValue);
                                          }

                                          private void highlight(ObservableList<JEVisObject> objects, JEVisObject obj, Label node) {
                                              Platform.runLater(() -> {
                                                  try {
                                                      if (objects.contains(obj)) {
//                                                          System.out.println("+Object event: " + obj);
                                                          node.setStyle("-fx-background-color: yellow;");
//                                                          node.setTextFill(Color.valueOf("#6495ED"));
//                                                          node.setTextFill(Color.valueOf("#248f24"));//green


                                                      } else {
//                                                          node.setStyle("-fx-background-color: transparent;");
//                                                          System.out.println("-Object event: " + obj);
//                                                          node.setTextFill(Color.BLACK);
                                                          node.setStyle("-fx-background-color: transparent;");
                                                      }
                                                  } catch (Exception ex) {
                                                  }
                                              });
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

                                                      JEVisTreeRow jeVisTreeRow = getTreeTableRow().getItem();
                                                      JEVisObject jeVisObject = jeVisTreeRow.getJEVisObject();

                                                      HBox hbox = new HBox();
                                                      Label nameLabel = new Label();
                                                      Node icon;

                                                      setContextMenu(contextMenu);
                                                      setOnContextMenuRequested(event -> {
                                                          contextMenu.setItem(getTreeTableRow());
                                                      });
//
//                                                      hbox.setOsetOnAction(new EventHandler<ActionEvent>() {
//                                                          public void handle(ActionEvent e) {
//                                                              contextMenu.show(textField, Side.BOTTOM, 0, 0);
//                                                          }
//                                                      });

                                                      hbox.setStyle("-fx-background-color: transparent;");
                                                      nameLabel.setStyle("-fx-background-color: transparent;");

                                                      nameLabel.setPadding(new Insets(0, 0, 0, 0));
                                                      Region spaceBetween = new Region();
                                                      spaceBetween.setMinWidth(8);

                                                      //highlight(tree.getHighlighterList(), jeVisObject, nameLabel);

                                                      /** if this object is positive in the search highlight the cell,
                                                       * this seems really bad, a listener for each object in the tree? there goes the memory
                                                       *
                                                       * **/
//                                                      tree.getHighlighterList().addListener(new ListChangeListener<JEVisObject>() {
//                                                          @Override
//                                                          public void onChanged(Change<? extends JEVisObject> c) {
//                                                              highlight(tree.getHighlighterList(), jeVisObject, nameLabel);
//                                                          }
//                                                      });

                                                      if (jeVisTreeRow.getType() == JEVisTreeRow.TYPE.OBJECT) {
                                                          nameLabel.setText(jeVisObject.getName());
                                                          try {
                                                              if (!jeVisObject.getJEVisClassName().equals("Link")) {
                                                                  icon = getClassIcon(jeVisObject.getJEVisClass(), 18, 18);
                                                              } else {
                                                                  JEVisObject linkedObject = jeVisObject.getLinkedObject();
                                                                  icon = getClassIcon(linkedObject.getJEVisClass(), 18, 18);
                                                              }
                                                          } catch (Exception ex) {
                                                              icon = ResourceLoader.getImage("1393615831_unknown2.png", 18, 18);
                                                          }
                                                      } else {//Attribute

                                                          nameLabel.setText(I18nWS.getInstance().getAttributeName(jeVisTreeRow.getJEVisAttribute()));
                                                          HBox hbox2 = new HBox();
                                                          Region spacer = new Region();
                                                          spacer.setPrefWidth(12);
                                                          ImageView image = new ImageView(ATTRIBUTE_ICON);
                                                          image.fitHeightProperty().set(18);
                                                          image.fitWidthProperty().set(18);

                                                          hbox2.getChildren().addAll(spacer, image);
                                                          icon = hbox2;
                                                      }

                                                      hbox.getChildren().addAll(icon, spaceBetween, nameLabel);
                                                      //Tooltip debugTip = new Tooltip(jeVisTreeRow.toString());
                                                      //setTooltip(debugTip);
                                                      setGraphic(hbox);

                                                  } catch (Exception ex) {
                                                      logger.catching(ex);
                                                  }
                                              }
                                          }

                                          private ImageView getClassIcon(JEVisClass jclass, double h, double w) throws JEVisException {

                                              if (!classIconCache.containsKey(jclass.getName())) {
                                                  classIconCache.put(jclass.getName(), SwingFXUtils.toFXImage(jclass.getIcon(), null));
                                              }

                                              ImageView iv = new ImageView(classIconCache.get(jclass.getName()));
                                              iv.fitHeightProperty().setValue(h);
                                              iv.fitWidthProperty().setValue(w);
                                              iv.setSmooth(true);
                                              return iv;
                                          }
                                      };
                                  }
                              }
        );


        return column;

    }

    /**
     * TODo: localize
     *
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


        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn<>(coumnName);
        column.setId(coumnName);
        column.setPrefWidth(135);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, String> p) -> {
            try {
                if (p != null
                        && p.getValue() != null
                        && p.getValue().getValue() != null
                        && p.getValue().getValue().getJEVisObject() != null) {
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

                    if (value != null && value.getLatestSample() != null) {
                        if (max) {
                            return new ReadOnlyObjectWrapper<String>(dateTimeFormatter.print(value.getTimestampFromLastSample()));
                        } else {
                            return new ReadOnlyObjectWrapper<String>(dateTimeFormatter.print(value.getTimestampFromFirstSample()));
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

                                      return new TreeTableCell<JEVisTreeRow, String>() {

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

//                                                      boolean showCell = tree.getFilter().showCell(getTableColumn(), getTreeTableRow().getTreeItem().getValue());
//                                                      showCell = true;

//                                                      setContextMenu(new JEVisTreeContextMenu(obj, tree));

                                                      hbox.setStyle("-fx-background-color: transparent;");
                                                      nameLabel.setStyle("-fx-background-color: transparent;");

                                                      nameLabel.setText(item);
                                                      nameLabel.setPadding(new Insets(0, 0, 0, 8));


                                                      hbox.getChildren().addAll(nameLabel);
                                                      setGraphic(hbox);

                                                  } catch (Exception ex) {
                                                      logger.catching(ex);
                                                  }
                                              }
                                          }
                                      };
                                  }
                              }
        );


        return column;

    }


    public static TreeTableColumn<JEVisTreeRow, String> buildClass() {
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn<>(OBJECT_CLASS);
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
                        return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getJEVisObject().getJEVisClass().getName());
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

    public static TreeTableColumn<JEVisTreeRow, Long> buildID() {
        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn<>(OBJECT_ID);
        column.setId(OBJECT_ID);
        column.setPrefWidth(70);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, Long> p) -> {
            try {
                if (p != null
                        && p.getValue() != null
                        && p.getValue().getValue() != null
                        && p.getValue().getValue().getJEVisObject() != null) {

                    return new ReadOnlyObjectWrapper<Long>(p.getValue().getValue().getJEVisObject().getID());

                } else {
                    return new ReadOnlyObjectWrapper<Long>(-2L);
                }

            } catch (Exception ex) {
                logger.info("Error in Column Fatory: " + ex);
                return new ReadOnlyObjectWrapper<Long>(-1L);
            }

        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Long>, TreeTableCell<JEVisTreeRow, Long>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Long> call(TreeTableColumn<JEVisTreeRow, Long> param) {

                return new TreeTableCell<JEVisTreeRow, Long>() {


                    @Override
                    public void commitEdit(Long newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        setText(null);
                        setGraphic(null);
                        if (!empty) {
                            StackPane stackPane = new StackPane();
                            Label label = new Label();

                            stackPane.getChildren().addAll(label);
                            StackPane.setAlignment(stackPane, Pos.CENTER_RIGHT);
                            JEVisTreeRow jeVisTreeRow = getTreeTableRow().getItem();
                            if (jeVisTreeRow != null) {
                                if (jeVisTreeRow.getType() == JEVisTreeRow.TYPE.OBJECT) {
                                    label.setText(item + "");
                                } else {
                                    label.setText("");
                                }
                            }

                            stackPane.setStyle("-fx-background-color: transparent;");
                            label.setStyle("-fx-background-color: transparent;");
                            setGraphic(stackPane);
                        }
                    }
                };
            }
        });

        return column;

    }
}
