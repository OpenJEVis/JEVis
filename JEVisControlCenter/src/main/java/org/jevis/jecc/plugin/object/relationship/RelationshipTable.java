/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc.plugin.object.relationship;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.resource.ImageConverter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RelationshipTable extends TableView {
    private static final Logger logger = LogManager.getLogger(RelationshipTable.class);

    public RelationshipTable(final JEVisObject obj, List<JEVisRelationship> relationships) {
        super();
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setPlaceholder(new Label("No Data"));

        TableColumn colDirection = new TableColumn(I18n.getInstance().getString("plugin.object.relationship.direction"));
        colDirection.setCellValueFactory(new PropertyValueFactory<TableSample, Integer>("Direction"));

        TableColumn colThisObject = new TableColumn(I18n.getInstance().getString("plugin.object.relationship.object"));
        colThisObject.setCellValueFactory(new PropertyValueFactory<TableSample, JEVisObject>("Object"));

        TableColumn colOtherObject = new TableColumn(I18n.getInstance().getString("plugin.object.relationship.otherobject"));
        colOtherObject.setCellValueFactory(new PropertyValueFactory<TableSample, JEVisObject>("Other"));

        TableColumn colType = new TableColumn(I18n.getInstance().getString("plugin.object.relationship.type"));
        colType.setCellValueFactory(new PropertyValueFactory<TableSample, Integer>("Type"));

        setMinWidth(555d);//TODo: replace Dirty workaround
//        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        getColumns().addAll(colThisObject, colDirection, colOtherObject, colType);

        List<TableSample> tjc = new LinkedList<>();
//        logger.info("Rel.size: " + relationships.size());
        for (JEVisRelationship rel : relationships) {
            try {
                tjc.add(new TableSample(obj, rel));
            } catch (Exception ex) {
                logger.error("Error with relationship: {} :", rel, ex);
            }
        }
//        setStyle("table-row-cell:empty { -fx-background-color: " + Constants.Color.LIGHT_GREY2 + ";}");

        colThisObject.setMinWidth(200);
        colOtherObject.setMinWidth(200);

        colDirection.prefWidthProperty().set(90);
        colDirection.maxWidthProperty().bind(colDirection.prefWidthProperty());
        colDirection.setResizable(false);

        colOtherObject.setCellFactory(new Callback<TableColumn<TableSample, JEVisObject>, TableCell<TableSample, JEVisObject>>() {
            @Override
            public TableCell<TableSample, JEVisObject> call(TableColumn<TableSample, JEVisObject> param) {
                TableCell<TableSample, JEVisObject> cell = new TableCell<TableSample, JEVisObject>() {
                    @Override
                    public void updateItem(JEVisObject item, boolean empty) {
                        if (item != null) {
                            HBox box = new HBox(10);
                            box.setAlignment(Pos.BASELINE_LEFT);

                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getJEVisClass().getIcon(), 20, 20);
                                box.getChildren().setAll(icon);
                            } catch (JEVisException ex) {
                                logger.fatal(ex);
                            }

                            box.getChildren().add(new Label(item.getName() + " (" + item.getID() + ")"));
                            setGraphic(box);
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                        } else {
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                            setStyle("-fx-background-color: transparent; ");
                        }
                    }
                };
                return cell;
            }
        });

        colThisObject.setCellFactory(new Callback<TableColumn<TableSample, JEVisObject>, TableCell<TableSample, JEVisObject>>() {
            @Override
            public TableCell<TableSample, JEVisObject> call(TableColumn<TableSample, JEVisObject> param) {
                TableCell<TableSample, JEVisObject> cell = new TableCell<TableSample, JEVisObject>() {
                    @Override
                    public void updateItem(JEVisObject item, boolean empty) {
                        if (item != null) {
                            HBox box = new HBox(10);
                            box.setAlignment(Pos.BASELINE_LEFT);

                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getJEVisClass().getIcon(), 20, 20);
                                box.getChildren().setAll(icon);
                            } catch (JEVisException ex) {
                                logger.fatal(ex);
                            }

                            box.getChildren().add(new Label(item.getName() + " (" + item.getID() + ")"));
                            setGraphic(box);
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                        } else {
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                            setStyle("-fx-background-color: transparent; ");
                        }
                    }
                };
                return cell;
            }
        });

        colDirection.setCellFactory(new Callback<TableColumn<TableSample, Integer>, TableCell<TableSample, Integer>>() {
            @Override
            public TableCell<TableSample, Integer> call(TableColumn<TableSample, Integer> param) {
                TableCell<TableSample, Integer> cell = new TableCell<TableSample, Integer>() {
                    @Override
                    public void updateItem(Integer item, boolean empty) {
                        try {
                            if (item != null) {
                                HBox box = new HBox();
                                box.setAlignment(Pos.CENTER);

                                ImageView icon = null;
                                if (item == 1) {
                                    icon = ControlCenter.getImage("left.png", 20, 20);
                                } else if (item == 2) {
                                    icon = ControlCenter.getImage("right.png", 20, 20);
                                }

                                box.getChildren().setAll(icon);
                                setGraphic(box);
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                            } else {
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                                setStyle("-fx-background-color: transparent; ");
                            }
                        } catch (Exception ex) {
                            logger.error("Error in direction cell updateData: {}:{}", item, ex);
                        }
                    }
                };
                return cell;
            }
        });

        colType.setCellFactory(new Callback<TableColumn<TableSample, Integer>, TableCell<TableSample, Integer>>() {
            @Override
            public TableCell<TableSample, Integer> call(TableColumn<TableSample, Integer> param) {
                TableCell<TableSample, Integer> cell = new TableCell<TableSample, Integer>() {
                    @Override
                    public void updateItem(Integer item, boolean empty) {
                        if (item != null) {

                            HBox box = new HBox();
                            box.setAlignment(Pos.BASELINE_LEFT);
//                            ImageView icon = JEConfig.getImage("right.png", 24, 24);
                            box.getChildren().setAll(new Label(getTypeName(item)));
                            setGraphic(box);
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                        } else {
//                            setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                            setStyle("-fx-background-color: transparent; ");
                        }
                    }
                };
                return cell;
            }
        });

        colDirection.setPrefWidth(94);
//        colDirection.setMaxWidth(94);
        colType.setPrefWidth(160);
        final ObservableList<TableSample> data = FXCollections.observableArrayList(tjc);
        setItems(data);

    }

    public String getTypeName(int type) {
        switch (type) {
            case JEVisConstants.ObjectRelationship.DATA:
                return I18n.getInstance().getString("jevis.types.data");
            case JEVisConstants.ObjectRelationship.INPUT:
                return I18n.getInstance().getString("jevis.types.input");
            case JEVisConstants.ObjectRelationship.LINK:
                return I18n.getInstance().getString("jevis.types.link");
            case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                return I18n.getInstance().getString("jevis.types.member_create");
            case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                return I18n.getInstance().getString("jevis.types.member_delete");
            case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                return I18n.getInstance().getString("jevis.types.member_execte");
            case JEVisConstants.ObjectRelationship.MEMBER_READ:
                return I18n.getInstance().getString("jevis.types.member_read");
            case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                return I18n.getInstance().getString("jevis.types.member_write");
            case JEVisConstants.ObjectRelationship.NESTED_CLASS:
                return I18n.getInstance().getString("jevis.types.nested_class");
            case JEVisConstants.ObjectRelationship.OWNER:
                return I18n.getInstance().getString("jevis.types.owner");
            case JEVisConstants.ObjectRelationship.PARENT:
                return I18n.getInstance().getString("jevis.types.parent");
            case JEVisConstants.ObjectRelationship.ROOT:
                return I18n.getInstance().getString("jevis.types.root");
            case JEVisConstants.ObjectRelationship.SERVICE:
                return I18n.getInstance().getString("jevis.types.service");
            case JEVisConstants.ObjectRelationship.SOURCE:
                return I18n.getInstance().getString("jevis.types.source");
            default:
                return "Unknow: " + type;
        }

    }

    public class TableSample {

        private final SimpleStringProperty other = new SimpleStringProperty("");
        private SimpleIntegerProperty direction = new SimpleIntegerProperty();
        private SimpleStringProperty object = new SimpleStringProperty("");
        private SimpleIntegerProperty type = new SimpleIntegerProperty();
        private JEVisRelationship _relationship = null;
        private JEVisObject _thisObject;

        /**
         * @param obj
         * @param rel
         */
        public TableSample(final JEVisObject obj, JEVisRelationship rel) {
            try {
                logger.error("new TableSample: {}, {}", obj, rel);
                this.direction = new SimpleIntegerProperty(1);
                this.object = new SimpleStringProperty(rel.getStartObject().getName());
                this.type = new SimpleIntegerProperty(rel.getType());
                this._relationship = rel;
                this._thisObject = obj;
            } catch (Exception ex) {
                logger.error("Error in relationship table entry: {}", ex);
                logger.error(ex);
            }
        }

        public Integer getDirection() {
            try {
                logger.debug("getDirection: {} |  {} | {}", this._relationship, this._relationship.getEndObject(), this._thisObject);
                if (this._relationship.getEndObject().equals(this._relationship.getOtherObject(this._thisObject))) {
//                if (_relationship.getEndObject().equals(_relationship.getOtherObject(_thisObject))) {
                    return 2;
                } else {
                    return 1;
                }
            } catch (Exception ex) {
                logger.fatal("{}:{}", this._relationship, ex);
            }
            return 0;
        }

        public void setDirection(Integer direction) {
//            this.direction = direction;
        }

        public Integer getType() {
            try {
                return this._relationship.getType();
            } catch (Exception ex) {
                logger.fatal("{}:{}", this._relationship, ex);
            }
            return -1;
        }

        public void setType(Integer type) {
//            this.type = type;
        }

        public JEVisObject getOther() {
            try {
                return this._relationship.getOtherObject(this._thisObject);
            } catch (Exception ex) {
                logger.fatal("{}:{}", this._relationship, ex);
            }
            return null;
//            try {
//                return _relationship.getStartObject().getName();
//            } catch (JEVisException ex) {
//                Logger.getLogger(RelationshipTable.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return null;

        }

        public JEVisObject getObject() {
            return this._thisObject;
//            try {
//                return _relationship.getStartObject().getName();
//            } catch (JEVisException ex) {
//                Logger.getLogger(RelationshipTable.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return null;

        }

        public void setObject(JEVisObject object) {
            this._thisObject = object;
//            this.object = object;
        }

//        public void setObject(String object) {
////            this.object = object;
//        }
    }
}
