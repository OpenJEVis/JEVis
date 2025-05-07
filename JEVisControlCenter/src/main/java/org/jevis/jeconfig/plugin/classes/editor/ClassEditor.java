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
package org.jevis.jeconfig.plugin.classes.editor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.commons.constants.DisplayType;
import org.jevis.commons.constants.GUIConstants;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ExceptionDialog;
import org.jevis.jeconfig.plugin.classes.ClassHelper;
import org.jevis.jeconfig.plugin.classes.ClassTree;
import org.jevis.jeconfig.plugin.classes.relationship.ValidParentEditor;
import org.jevis.jeconfig.tool.ImageConverter;
import tech.units.indriya.AbstractUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassEditor {

    private static final Logger logger = LogManager.getLogger(ClassEditor.class);
    //    private final UnitChooser pop = new UnitChooser();
    private final VBox _view;
    JFXButton fIcon;
    JFXTextField fName = new JFXTextField();
    JFXTextArea fDescription = new JFXTextArea();
    //    private Desktop desktop = Desktop.getDesktop();
    private JEVisClass _class;
    JFXCheckBox fUnique = new JFXCheckBox();
    private TitledPane t2;
    private List<JEVisType> _toDelete;
    private JFXTextField fInherit;
    private ClassTree _tree = null;

    public ClassEditor() {
        _view = new VBox();
//        _view.setStyle("-fx-background-color: #E2E2E2");
        _view.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
    }

    public void checkIfSaved(JEVisClass obj) {

    }

    public void setTreeView(ClassTree tree) {
        _tree = tree;
    }

    public void setJEVisClass(final JEVisClass jclass) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                _class = jclass;
                _toDelete = new ArrayList<>();

                final Accordion accordion = new Accordion();
//                accordion.setStyle("-fx-background-color: #E2E2E2");
                accordion.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

                GridPane gridPane = new GridPane();
                gridPane.setPadding(new Insets(5, 0, 20, 20));
                gridPane.setHgap(7);
                gridPane.setVgap(7);

                Label lName = new Label(I18n.getInstance().getString("plugin.classes.editor.name"));
                Label lDescription = new Label(I18n.getInstance().getString("plugin.classes.editor.description"));
                Label lIsUnique = new Label(I18n.getInstance().getString("plugin.classes.editor.unique"));
                Label lIcon = new Label(I18n.getInstance().getString("plugin.classes.editor.icon"));
                Label lRel = new Label(I18n.getInstance().getString("plugin.classes.editor.relationship"));
//                Label lInherit = new Label("Inheritance:");
                Label lTypes = new Label(I18n.getInstance().getString("plugin.classes.editor.types"));

                fName.prefWidthProperty().set(250d);

                fIcon = new JFXButton("", getIcon(jclass));

                fUnique.setSelected(false);

                int x = 0;

                gridPane.add(lName, 0, x);
                gridPane.add(fName, 1, x);
//                gridPane.add(lInherit, 0, 1);
//                gridPane.add(fInherit, 1, 1);
                gridPane.add(lIcon, 0, ++x);
                gridPane.add(fIcon, 1, x);
                gridPane.add(lIsUnique, 0, ++x);
                gridPane.add(fUnique, 1, x);
                gridPane.add(lDescription, 0, ++x);
                gridPane.add(fDescription, 1, x, 1, 2);

//                GridPane.setHalignment(lInherit, HPos.LEFT);
                GridPane.setHalignment(lIcon, HPos.LEFT);
                GridPane.setHalignment(lName, HPos.LEFT);
                GridPane.setHalignment(lIsUnique, HPos.LEFT);
                GridPane.setHalignment(lDescription, HPos.LEFT);
                GridPane.setValignment(lDescription, VPos.TOP);
                GridPane.setHalignment(lRel, HPos.LEFT);
                GridPane.setValignment(lRel, VPos.TOP);
//        GridPane.setHgrow(tTable, Priority.ALWAYS);
                GridPane.setHalignment(lTypes, HPos.LEFT);
                GridPane.setValignment(lTypes, VPos.TOP);

                try {
                    if (jclass != null) {
                        fName.setText(jclass.getName());
                        if (jclass.getInheritance() != null) {
                            HBox inBox = new HBox();
                            Label inLabel = new Label(jclass.getInheritance().getName());
                            inLabel.setMaxHeight(8);
                            ImageView inIcon = getIcon(jclass.getInheritance());
                            inIcon.fitHeightProperty().bind(inLabel.heightProperty());
                            inBox.getChildren().setAll(inIcon, inLabel);
//                            fInherit.setText(jclass.getInheritance().getName());
                        } else {
//                            fInherit.setText("Choose...");
                        }

                        fDescription.setWrapText(true);
                        fDescription.setText(jclass.getDescription());
                        fUnique.setSelected(jclass.isUnique());
                    }

                } catch (JEVisException ex) {
                    ExceptionDialog dia = new ExceptionDialog();
                    dia.show(JEConfig.getStage(), I18n.getInstance().getString("dialog.error.title"),
                            I18n.getInstance().getString("dialog.error.servercommit"), ex, null);
                }

                fIcon.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        FileChooser fileChooser = new FileChooser();
                        if (JEConfig.getLastPath() != null && JEConfig.getLastPath().isDirectory() && JEConfig.getLastPath().canRead()) {
                            fileChooser.setInitialDirectory(JEConfig.getLastPath().getParentFile());
                        }

                        FileChooser.ExtensionFilter allImagesFilter = new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.gif");
                        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
                        FileChooser.ExtensionFilter gifFilter = new FileChooser.ExtensionFilter("GIF files (*.gif)", "*.gif");
                        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
                        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
                        fileChooser.getExtensionFilters().addAll(allImagesFilter, gifFilter, extFilter, jpgFilter, allFilter);
                        final File file = fileChooser.showOpenDialog(JEConfig.getStage());
                        if (file != null) {
                            openFile(file);
                            JEConfig.setLastPath(file);
                            try {
                                _class.setIcon(file);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Image image = new Image(file.toURI().toString());
                                        ImageView iView = new ImageView(image);
                                        iView.setFitHeight(30);
                                        iView.setFitWidth(30);
                                        fIcon.setGraphic(iView);
                                    }
                                });

                            } catch (JEVisException ex) {
                                logger.catching(ex);
                                ExceptionDialog dia = new ExceptionDialog();
                                dia.show(I18n.getInstance().getString("dialog.error.title"),
                                        I18n.getInstance().getString("plugin.classes.editor.error.icon.message"), ex.getMessage(), ex, null);
                            }
                        }
                    }
                });

                try {
                    fIcon.setDisable(!_class.getDataSource().getCurrentUser().isSysAdmin());
                } catch (Exception ex) {

                }

                ScrollPane cpGenerell = new ScrollPane();
                cpGenerell.setContent(gridPane);

                final TitledPane t1 = new TitledPane(I18n.getInstance().getString("plugin.classes.editor.tab.general"), cpGenerell);
                t2 = new TitledPane(I18n.getInstance().getString("plugin.classes.editor.tab.types"), buildTypeNode());

                ValidParentEditor redit = new ValidParentEditor();
                redit.setJEVisClass(jclass);

                final TitledPane t3 = new TitledPane(I18n.getInstance().getString("plugin.classes.editor.tab.valid_parents"), redit.getView());

                t1.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                t2.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
                t3.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

                cpGenerell.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

                accordion.getPanes().addAll(t1, t2, t3);
                t1.setAnimated(false);
                t2.setAnimated(false);
                t3.setAnimated(false);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        accordion.setExpandedPane(t1);//TODO the selected pane is not blue highlighted like if the user clicked.....
                    }
                });

                try {
                    if (JEConfig.getDataSource().getCurrentUser().isSysAdmin()) {
                        fName.setDisable(false);
                        fUnique.setDisable(false);
                        fDescription.setDisable(false);
                    }
                } catch (Exception ex) {

                }

                _view.getChildren().setAll(accordion);
                VBox.setVgrow(accordion, Priority.ALWAYS);
            }
        });

    }

    public Node getView() {
        return _view;
    }

    /**
     * @return
     * @TODO: make this an extra GUI class
     */
    private Node buildTypeNode() {
        ScrollPane cp = new ScrollPane();
//        cp.setStyle("-fx-background-color: #E2E2E2");
        cp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 5, 20));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        Label headerName = new Label(I18n.getInstance().getString("plugin.classes.editor.type.name"));
        Label headerPType = new Label(I18n.getInstance().getString("plugin.classes.editor.type.prim_type"));
        Label headerUnit = new Label(I18n.getInstance().getString("plugin.classes.editor.type.unit"));
        Label headerGType = new Label(I18n.getInstance().getString("plugin.classes.editor.type.guitype"));
        Label headerControl = new Label(I18n.getInstance().getString("plugin.classes.editor.type.controls"));

        Separator headerSep = new Separator();
        gridPane.add(headerName, 0, 0);
        gridPane.add(headerPType, 1, 0);
        gridPane.add(headerGType, 2, 0);
        gridPane.add(headerUnit, 3, 0);
        gridPane.add(headerControl, 4, 0);
        gridPane.add(headerSep, 0, 1, 7, 1);

        //disabled as long the dont function right
        headerUnit.setVisible(false);
        headerControl.setVisible(false);
        headerSep.setVisible(false);

        int row = 2;
        try {
            Collections.sort(_class.getTypes());
            if (_class.getTypes().isEmpty()) {
                Label emty = new Label(I18n.getInstance().getString("plugin.classes.editor.type.emty"));
                gridPane.add(emty, 0, row, 4, 1);
                row++;
            }
            for (final JEVisType type : _class.getTypes()) {
//                boolean isInherited = !type.getJEVisClass().equals(_class);
                boolean isInherited = type.isInherited();

                final Label lName = new Label(type.getName());

                //test
                final ChoiceBox guiType = new ChoiceBox();
                guiType.setMaxWidth(500);
                guiType.setPrefWidth(160);

                List<String> gTypes = new ArrayList<>();
                for (DisplayType id : GUIConstants.getALL(type.getPrimitiveType())) {
                    gTypes.add(id.getId());
                }

                ObservableList<String> items = FXCollections.observableList(gTypes);
                guiType.setItems(items);
                guiType.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                guiType.getSelectionModel().select(type.getGUIDisplayType());
                guiType.valueProperty().addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            type.setGUIDisplayType(newValue);
                        } catch (JEVisException ex) {
                            logger.catching(ex);
                        }
                    }
                });

                final JFXButton unitSelector = new JFXButton("");
                unitSelector.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                unitSelector.setMaxWidth(56.0);
                setUnitButton(unitSelector, type);

                try {
                    unitSelector.setOnAction(new EventHandler<ActionEvent>() {
                                                 @Override
                                                 public void handle(ActionEvent t) {
                                                     try {
//                                UnitSelectDialog usd = new UnitSelectDialog();
//                                if (usd.show(JEConfig.getStage(), "Select Unit", _class.getDataSource()) == UnitSelectDialog.Response.YES) {
//                                    logger.info("OK");
//                                    unitSelector.setText(usd.getUnit().toString());
//                                    if (type.getUnit() != null && !type.getUnit().equals(usd.getUnit())) {
//                                        //TODO reimplement unit
////                                        type.setUnit(usd.getUnit());
//                                    }
//
//                                }

                                                     } catch (Exception ex) {
                                                         logger.catching(ex);
                                                     }
                                                 }
                                             }
                    );
                } catch (Exception ex) {
                    logger.catching(ex);
                }

                ChoiceBox primType = new ChoiceBox();
                primType.setItems(ClassHelper.getAllPrimitiveTypes());
                primType.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                primType.getSelectionModel().select(ClassHelper.getNameforPrimitiveType(type));
                primType.valueProperty().addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            type.setPrimitiveType(ClassHelper.getIDforPrimitiveType(newValue));

                            List<String> gTypes = new ArrayList<>();
                            for (DisplayType id : GUIConstants.getALL(type.getPrimitiveType())) {
                                logger.trace("Add Guid Tpye: {}", id.getId());

                                gTypes.add(id.getId());
                            }

                            ObservableList<String> items = FXCollections.observableList(gTypes);
                            guiType.setItems(items);
                            guiType.getSelectionModel().selectFirst();

                        } catch (JEVisException ex) {
                            logger.catching(ex);
                        }
                    }
                });

//                PopOver poUnit = new PopOver(unitSelector);
                JFXButton up = new JFXButton();
                if (_class.getTypes().indexOf(type) == 0) {
//                    up.disableProperty().set(true);
                    up.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                }
                up.setGraphic(JEConfig.getImage("1395085229_arrow_return_right_up.png", 20, 20));
                up.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        try {
                            int pos = _class.getTypes().indexOf(type);
                            int lastPos = type.getGUIPosition();
                            if (pos > 0) {
                                JEVisType prevType = _class.getTypes().get(pos - 1);
                                type.setGUIPosition(prevType.getGUIPosition());
                                prevType.setGUIPosition(lastPos);
                            }
                            t2.setContent(buildTypeNode());

                        } catch (JEVisException ex) {
                            logger.catching(ex);
                        }
                    }
                });

                JFXButton down = new JFXButton();
                if (_class.getTypes().indexOf(type) == _class.getTypes().size() - 1) {
//                    down.disableProperty().set(true);
                    down.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                }
                down.setGraphic(JEConfig.getImage("1395085233_arrow_return_right_down.png", 20, 20));
                down.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        try {
                            int pos = _class.getTypes().indexOf(type);
                            int lastPos = type.getGUIPosition();
                            if (pos < _class.getTypes().size() - 1) {

                                JEVisType afterType = _class.getTypes().get(pos + 1);
                                type.setGUIPosition(afterType.getGUIPosition());
                                afterType.setGUIPosition(lastPos);
                            }
                            t2.setContent(buildTypeNode());

                        } catch (JEVisException ex) {
                            logger.catching(ex);
                        }
                    }
                });

                JFXButton remove = new JFXButton();
                remove.setGraphic(JEConfig.getImage("list-remove.png", 20, 20));
                remove.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                remove.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        try {
                            _class.getTypes().remove(type);//TODo: this is no so save..
                            _toDelete.add(type);
                            type.delete();//TODO remove this and use the global "Save action"
                            t2.setContent(buildTypeNode());

                        } catch (JEVisException ex) {
                            logger.catching(ex);
                        }
                    }
                });

                //                                              x, y
                gridPane.add(lName, 0, row);
                gridPane.add(primType, 1, row);
                gridPane.add(guiType, 2, row);
                gridPane.add(unitSelector, 3, row);
                gridPane.add(remove, 4, row);
                gridPane.add(up, 5, row);
                gridPane.add(down, 6, row);

                lName.setDisable(isInherited);
                primType.setDisable(isInherited);
                guiType.setDisable(isInherited);
                unitSelector.setDisable(isInherited);
                remove.setDisable(isInherited);
                up.setDisable(isInherited);
                down.setDisable(isInherited);

                //disabled as long the dont function right
                up.setVisible(false);
                down.setVisible(false);
                unitSelector.setVisible(false);

                GridPane.setHgrow(lName, Priority.ALWAYS);

                row++;

            }
        } catch (JEVisException ex) {
            logger.catching(ex);
        }

        Separator newSep = new Separator();
        gridPane.add(newSep, 0, row++, 6, 1);

        final JFXTextField fName = new JFXTextField();
        fName.setPromptText(I18n.getInstance().getString("plugin.classes.editor.type.newname"));
//        final ChoiceBox pTypeBox = buildPrimitiveTypeBox(null);

        JFXButton newB = new JFXButton();
        newB.setGraphic(JEConfig.getImage("list-add.png", 20, 20));
        newB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                createTypeAction(fName.getText());
            }
        });
        try {
            newB.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
            fName.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {

        }

        fName.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    createTypeAction(fName.getText());
                }
            }
        });

        fName.visibleProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            fName.requestFocus();
                        }
                    });
                }
            }
        });

//        ChoiceBox guiType = new ChoiceBox();
//        guiType.setItems(FXCollections.observableArrayList(
//                "Text", "IP-Address", "Number", "File Selector", "Check Box", "PASSWORD Field"));
        gridPane.add(fName, 0, row);
//        gridPane.add(pTypeBox, 1, row);
//        gridPane.add(guiType, 2, row);
        gridPane.add(newB, 1, row);

        cp.setContent(gridPane);

        return cp;
    }

    private void createTypeAction(String name) {
        try {
            if (name.isEmpty()) {
                return;
            }

            if (!name.matches("^[A-Za-z0-9 _]*[A-Za-z0-9][A-Za-z0-9 _]*$")) {
                //no specail chars allowed, will this be a problem is some countries?
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(I18n.getInstance().getString("plugin.classes.editor.type.alert.name.title"));
                alert.setHeaderText(I18n.getInstance().getString("plugin.classes.editor.type.alert.name.header"));
                alert.setContentText(I18n.getInstance().getString("plugin.classes.editor.type.alert.name.message"));
                alert.showAndWait();
                return;
            }

            if (_class.getType(name) != null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(I18n.getInstance().getString("plugin.classes.editor.type.alert.exist.title"));
                alert.setHeaderText(I18n.getInstance().getString("plugin.classes.editor.type.alert.exist.header"));
                alert.setContentText(I18n.getInstance().getString("plugin.classes.editor.type.error.exist.messagetype"));
                alert.showAndWait();
                return;
            }

            JEVisType newType = _class.buildType(name);

            JEVisType lastType = _class.getTypes().get(_class.getTypes().size() - 1);
//                    newType.setPrimitiveType(ClassHelper.getIDforPrimitiveType(pTypeBox.getSelectionModel().getSelectedItem().toString()));
            newType.setGUIPosition(lastType.getGUIPosition() + 1);
//            logger.info("new pos for new Type: " + newType.getGUIPosition());

            t2.setContent(buildTypeNode());

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    private void setUnitButton(Button button, JEVisType type) throws JEVisException {
        if (type.getUnit() != null) {
//            logger.info("editor.Unit: " + type.getUnit());
            if (type.getUnit().equals(AbstractUnit.ONE)) {
//                button.setText("None");
            } else {
//                logger.info(UnitManager.getInstance().format(type.getUnit()));
//                button.setText(type.getUnit().toString());
//                button.setText(UnitManager.getInstance().format(type.getUnit()));
            }

        }
    }

    private void openFile(File file) {
        try {
//            Image image = new Image(file.toURI().toString());
//            ImageView iv = new ImageView(image);
//            _class.setIcon(new ImageIcon(convertToAwtImage(image).getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH)));
            _class.setIcon(file);
//            File newIcon = desktop.open(file);
            _class.commit();

            //reload tree(icon)
            if (_tree != null) {
                _tree.reload(_class);
            }

//            fIcon.setGraphic(getImageView(_class));
        } catch (Exception ex) {
            logger.catching(ex);

            ExceptionDialog dia = new ExceptionDialog();
            dia.show(JEConfig.getStage(), I18n.getInstance().getString("plugin.classes.editor.type.alert.openfile.title"),
                    I18n.getInstance().getString("plugin.classes.editor.type.alert.openfile.message"), ex, null);

        }
    }

    public void commitAll() {
        try {
            logger.trace("commitAll() old: '{}' new: '{}'", _class.getName(), fName.getText());
            logger.trace("Type.count: {}", _class.getTypes().size());
            _class.setName(fName.getText());
            _class.setDescription(fDescription.getText());
            _class.setUnique(fUnique.isSelected());

            //ToDo: if inheritace change also change the tree
            _class.commit();

            for (JEVisType type : _class.getTypes()) {
                logger.trace("Type: {}", type.getName());
                if (!_toDelete.contains(type)) {
                    logger.trace("Type.commit: {}", type.getName());
                    type.commit();
                }
            }
            for (JEVisType type : _toDelete) {
                logger.trace("Type.delete: {}", type.getName());
                type.delete();
            }

            org.jevis.commons.classes.ClassHelper.updateTypesForHeirs(_class.getDataSource(), _class.getName());
//            _tree.reload(_class);
//            _class.notifyListeners(new JEVisEvent(_class, JEVisEvent.TYPE.CLASS_UPDATE));


        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
            ExceptionDialog dia = new ExceptionDialog();
            dia.show(I18n.getInstance().getString("plugin.classes.editor.type.alert.save.title"),
                    I18n.getInstance().getString("plugin.classes.editor.type.alert.save.message"), ex.getLocalizedMessage(), ex, null);
        }
    }

    public void rollback() {
        try {
            for (JEVisType type : _class.getTypes()) {
                type.rollBack();
            }
            _class.rollBack();

        } catch (JEVisException ex) {
            logger.catching(ex);

            ExceptionDialog dia = new ExceptionDialog();
            dia.show(JEConfig.getStage(), "Error", "Could not  rollback changes", ex, null);
        }
    }

    private ImageView getIcon(JEVisClass jclass) {
        try {
//            logger.info("getIcon for :" + jclass);
            if (jclass.getIcon() == null) {
                return JEConfig.getImage("1393615831_unknown2.png", 30, 30);
            }

            return ImageConverter.convertToImageView(jclass.getIcon(), 30, 30);
        } catch (Exception ex) {
            logger.info("Error while geeting class icon: " + ex);
            ex.printStackTrace();
            return JEConfig.getImage("1393615831_unknown2.png", 30, 30);
        }

    }
}
