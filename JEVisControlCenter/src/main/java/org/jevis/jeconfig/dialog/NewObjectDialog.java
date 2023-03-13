/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.resource.ImageConverter;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.NumberSpinner;
import org.jevis.jeconfig.tool.template.NullTemplate;
import org.jevis.jeconfig.tool.template.Template;
import org.jevis.jeconfig.tool.template.Templates;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author fs
 */
public class NewObjectDialog {
    private static final Logger logger = LogManager.getLogger(NewObjectDialog.class);
    public static String ICON = "1403104602_brick_add.png";

    private int createCount = 1;
    private JEVisClass createClass;
    private String createName = "No Name";
    private boolean userSetName = false;
    private Response response = Response.CANCEL;
    private final ObjectProperty<Response> responseProperty = new SimpleObjectProperty<>(response);
    private boolean withCleanData = true;
    private Template template = new NullTemplate();

    /**
     * @param jclass
     * @param parent
     * @param fixClass
     * @param type
     * @param objName
     * @return
     */
    public Response show(final JEVisClass jclass, final JEVisObject parent, boolean fixClass, Type type, String objName) {

        Dialog<ButtonType> dialog = new Dialog();
        dialog.initOwner(JEConfig.getStage());
        dialog.setTitle(I18n.getInstance().getString("jevistree.dialog.new.title"));
        dialog.setHeaderText(I18n.getInstance().getString("jevistree.dialog.new.header"));
        dialog.getDialogPane().getButtonTypes().setAll();
        dialog.setGraphic(ResourceLoader.getImage(ICON, 50, 50));
        dialog.setResizable(true);
        VBox root = new VBox();

        dialog.getDialogPane().setContent(root);

        JEVisClass parentClass = null;
        JEVisClass dataDirectoryClass = null;
        JEVisClass dataClass = null;
        try {
            parentClass = parent.getJEVisClass();
            dataDirectoryClass = parent.getDataSource().getJEVisClass("Data Directory");
            dataClass = parent.getDataSource().getJEVisClass("Data");
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);
        int x = 0;

        Label lName = new Label(I18n.getInstance().getString("jevistree.dialog.new.name"));
        final JFXTextField fName = new JFXTextField();
        fName.setPromptText(I18n.getInstance().getString("jevistree.dialog.new.name.prompt"));

        if (objName != null) {
            fName.setText(objName);
            userSetName = true;
        }

        fName.setOnKeyTyped(event -> userSetName = true);

        Label lClass = new Label(I18n.getInstance().getString("jevistree.dialog.new.class"));

        ObservableList<JEVisClass> options = FXCollections.observableArrayList();

        if (type == Type.NEW) {
            try {

                options = FXCollections.observableArrayList(
                        parent.getAllowedChildrenClasses()
                );

            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        } else if (type == Type.RENAME) {
            options.add(jclass);
        }

        Callback<ListView<JEVisClass>, ListCell<JEVisClass>> cellFactory = new Callback<ListView<JEVisClass>, ListCell<JEVisClass>>() {
            @Override
            public ListCell<JEVisClass> call(ListView<JEVisClass> param) {
                final ListCell<JEVisClass> cell = new ListCell<JEVisClass>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(JEVisClass item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);
                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getIcon(), 15, 15);
                                //Label cName = new Label(item.getName());
                                Label cName = new Label(I18nWS.getInstance().getClassName(item.getName()));
                                cName.setTextFill(Color.BLACK);
                                box.getChildren().setAll(icon, cName);

                            } catch (JEVisException ex) {
                                logger.fatal(ex);
                            }

                            setGraphic(box);

                        }
                    }
                };
                return cell;
            }
        };

        Callback<ListView<Template>, ListCell<Template>> templateCellFactory = new Callback<ListView<Template>, ListCell<Template>>() {
            @Override
            public ListCell<Template> call(ListView<Template> param) {
                final ListCell<Template> cell = new ListCell<Template>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(Template item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        }
                    }
                };
                return cell;
            }
        };

        Label templateLabel = new Label(I18n.getInstance().getString("jevistree.dialog.new.template"));
        JFXComboBox<Template> templateBox = new JFXComboBox<>();
        templateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            root.getChildren().remove(1, root.getChildren().size());
            template = newValue;
            for (Map.Entry<String, Node> entry : template.getOptions().entrySet()) {
                String s = entry.getKey();
                Node node = entry.getValue();
                Platform.runLater(() -> root.getChildren().add(node));
            }
            Platform.runLater(() -> dialog.getDialogPane().autosize());
        });
        templateBox.setCellFactory(templateCellFactory);
        templateBox.setButtonCell(templateCellFactory.call(null));

        final JFXComboBox<JEVisClass> jeVisClassComboBox = new JFXComboBox<>(options);
        JFXCheckBox createCleanData = new JFXCheckBox(I18n.getInstance().getString("jevistree.dialog.new.withcleandata"));
        createCleanData.setVisible(true);

        jeVisClassComboBox.setCellFactory(cellFactory);
        jeVisClassComboBox.setButtonCell(cellFactory.call(null));

        JEVisClass finalDataClass = dataClass;
        jeVisClassComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!userSetName) {
                    fName.setText(I18nWS.getInstance().getClassName(newValue.getName()));
                }
                if (newValue.equals(finalDataClass)) {
                    Platform.runLater(() -> createCleanData.setVisible(true));
                } else {
                    Platform.runLater(() -> createCleanData.setVisible(false));
                }

                ObservableList observableList = FXCollections.observableArrayList();
                observableList.add(new NullTemplate());
                Templates.getAllTemplates().forEach(template -> {
                    try {
                        if (template.supportsClass(newValue)) {
                            observableList.add(template);
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                });
                Platform.runLater(() -> {
                    templateBox.setItems(observableList);
                    templateBox.getSelectionModel().selectFirst();
                });


            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        if (jclass != null) {
            jeVisClassComboBox.getSelectionModel().select(jclass);
        }

        jeVisClassComboBox.setMinWidth(250);
        jeVisClassComboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        Label lCount = new Label(I18n.getInstance().getString("jevistree.dialog.new.amount"));
        //TODo: disable spinner if class is unique also disable OK button if there is already one of its kind
        final NumberSpinner count = new NumberSpinner(BigDecimal.valueOf(1), BigDecimal.valueOf(1));

        if (fixClass) {
            jeVisClassComboBox.setDisable(true);
            count.setDisable(true);
        }


        createCleanData.setSelected(true);
        createCleanData.setOnAction(event -> withCleanData = !withCleanData);

        gp.add(lName, 0, x);
        gp.add(fName, 1, x);


        gp.add(lClass, 0, ++x, 1, 1);
        gp.add(jeVisClassComboBox, 1, x, 1, 1);
        gp.add(templateLabel, 0, ++x);
        gp.add(templateBox, 1, x);
        gp.add(lCount, 0, ++x);
        gp.add(count, 1, x);

        if (parentClass != null && parentClass.equals(dataDirectoryClass)) {
            gp.add(createCleanData, 1, ++x);
        }


        GridPane.setHgrow(count, Priority.ALWAYS);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(gp);
        VBox.setVgrow(gp, Priority.ALWAYS);


        fName.setDisable(true);
        jeVisClassComboBox.setDisable(true);
        count.setDisable(true);

        try {
            if (parent.getDataSource().getCurrentUser().canWrite(parent.getID())) {
                fName.setDisable(false);
                jeVisClassComboBox.setDisable(false);
                count.setDisable(false);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        if (type == Type.NEW) {
            dialog.setTitle(I18n.getInstance().getString("jevistree.dialog.new.title"));
            dialog.setHeaderText(I18n.getInstance().getString("jevistree.dialog.new.title"));
            jeVisClassComboBox.getSelectionModel().selectFirst();
        } else if (type == Type.RENAME) {
            dialog.setTitle(I18n.getInstance().getString("jevistree.dialog.rename.title"));
            dialog.setHeaderText(I18n.getInstance().getString("jevistree.dialog.rename.header"));
            fName.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean t, Boolean t1) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (fName.isFocused() && !fName.getText().isEmpty()) {
                                fName.selectAll();
                            }
                        }
                    });
                }
            });

            count.setDisable(true);
            jeVisClassComboBox.getSelectionModel().select(jclass);
            jeVisClassComboBox.setDisable(true);
            templateBox.setDisable(true);
        }


        final ButtonType ok = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(cancel, ok);

        Platform.runLater(() -> fName.requestFocus());
        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.FINISH.getButtonData().getTypeCode())) {

                        createName = fName.getText();
                        createClass = jeVisClassComboBox.getSelectionModel().getSelectedItem();
//                        createCount = Integer.parseInt(count.getNumber().toString());//dirty :)
                        createCount = count.getNumber().intValue(); //haha

                        NewObjectDialog.this.response = Response.YES;
                    } else {
                        NewObjectDialog.this.response = Response.CANCEL;
                    }
                });


        return response;
    }

    public Template getTemplate() {
        return this.template;
    }

    public int getCreateCount() {
        if (createCount > 0 && createCount < 100) {
            return createCount;
        } else {
            return 1;
        }
    }

    public String getCreateName() {
        return createName;
    }

    public JEVisClass getCreateClass() {
        return createClass;
    }

    public enum Type {

        NEW, RENAME
    }

    public enum Response {

        NO, YES, CANCEL
    }

    public boolean isWithCleanData() {
        return withCleanData;
    }
}
