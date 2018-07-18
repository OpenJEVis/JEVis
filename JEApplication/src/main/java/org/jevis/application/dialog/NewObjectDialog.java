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
package org.jevis.application.dialog;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.I18nWS;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.resource.ImageConverter;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.application.tools.NumberSpinner;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fs
 */
public class NewObjectDialog {

    public static String ICON = "1403104602_brick_add.png";

    private int createCount = 1;
    private JEVisClass createClass;
    private String createName = "No Name";
    private boolean userSetName = false;
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private Response response = Response.CANCEL;
    private ObjectProperty<Response> responseProperty = new SimpleObjectProperty<>(response);

    /**
     * @param owner
     * @param jclass
     * @param parent
     * @param fixClass
     * @param type
     * @param objName
     * @return
     */
    public Response show(Stage owner, final JEVisClass jclass, final JEVisObject parent, boolean fixClass, Type type, String objName) {

        Dialog<ButtonType> dialog = new Dialog();
        dialog.setTitle("jevistree.dialog.new.title");
        dialog.setHeaderText(rb.getString("jevistree.dialog.new.header"));
        dialog.getDialogPane().getButtonTypes().setAll();
        dialog.setGraphic(ResourceLoader.getImage(ICON, 50, 50));
        VBox root = new VBox();

        dialog.getDialogPane().setContent(root);


        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);
        int x = 0;

        Label lName = new Label(rb.getString("jevistree.dialog.new.name"));
        final TextField fName = new TextField();
        fName.setPromptText(rb.getString("jevistree.dialog.new.name.prompt"));

        if (objName != null) {
            fName.setText(objName);
            userSetName = true;
        }

        fName.setOnKeyTyped(event -> userSetName = true);

        Label lClass = new Label(rb.getString("jevistree.dialog.new.class"));

        ObservableList<JEVisClass> options = FXCollections.observableArrayList();

        if (type == Type.NEW) {
            try {

                options = FXCollections.observableArrayList(
                        parent.getAllowedChildrenClasses()
                );

            } catch (JEVisException ex) {
                Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
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
                                Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            setGraphic(box);

                        }
                    }
                };
                return cell;
            }
        };

        final ComboBox<JEVisClass> comboBox = new ComboBox<>(options);
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));

        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!userSetName) {
                    fName.setText(newValue.getName());
                }
            } catch (JEVisException ex) {
                Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        if (jclass != null) {
            comboBox.getSelectionModel().select(jclass);
        }

        comboBox.setMinWidth(250);
        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        Label lCount = new Label(rb.getString("jevistree.dialog.new.amount"));
        //TODo: disable spinner if class is unique also disable OK button if there is already one of its kind
        final NumberSpinner count = new NumberSpinner(BigDecimal.valueOf(1), BigDecimal.valueOf(1));

        if (fixClass) {
            comboBox.setDisable(true);
            count.setDisable(true);
        }

        gp.add(lName, 0, x);
        gp.add(fName, 1, x);


        gp.add(lClass, 0, ++x, 1, 1);
        gp.add(comboBox, 1, x, 1, 1);
        gp.add(lCount, 0, ++x);
        gp.add(count, 1, x);


        GridPane.setHgrow(count, Priority.ALWAYS);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(gp);
        VBox.setVgrow(gp, Priority.ALWAYS);


        fName.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (!fName.getText().equals("")) {
//                    ok.setDisable(false);
                    //@ TODO
                }
            }
        });

        fName.setDisable(true);
        comboBox.setDisable(true);
        count.setDisable(true);

        try {
            if (parent.getDataSource().getCurrentUser().canWrite(parent.getID())) {
                fName.setDisable(false);
                comboBox.setDisable(false);
                count.setDisable(false);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (type == Type.NEW) {
            dialog.setTitle(rb.getString("jevistree.dialog.new.title"));
            dialog.setHeaderText(rb.getString("jevistree.dialog.new.title"));
            comboBox.getSelectionModel().selectFirst();
        } else if (type == Type.RENAME) {
            dialog.setTitle(rb.getString("jevistree.dialog.rename.title"));
            dialog.setHeaderText(rb.getString("jevistree.dialog.rename.header"));
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
            comboBox.getSelectionModel().select(jclass);
        }


        final ButtonType ok = new ButtonType(rb.getString("jevistree.dialog.new.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(rb.getString("jevistree.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        Platform.runLater(() -> fName.requestFocus());
        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {

                        createName = fName.getText();
                        createClass = comboBox.getSelectionModel().getSelectedItem();
                        createCount = Integer.parseInt(count.getNumber().toString());//dirty :)

                        NewObjectDialog.this.response = Response.YES;
                    } else {
                        NewObjectDialog.this.response = Response.CANCEL;
                    }
                });


        return response;
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


}
