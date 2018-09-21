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
package org.jevis.application.jevistree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.application.tools.NumberSpinner;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog to prompt the user about the copy/clone/move action
 *
 * @author fs
 */
public class CopyObjectDialog {

    public static String ICON = "1403555565_stock_folder-move.png";
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());

    private JEVisClass createClass;
    private String infoText = "";
    private TextField nameField = new TextField();
    private int createCount = 1;
    private boolean recursionAllowed = false;
    private boolean includeDataAllowed = true;
    final Button ok = new Button(rb.getString("jevistree.dialog.copy.ok"));

    final RadioButton move = new RadioButton(rb.getString("jevistree.dialog.copy.move"));
    final RadioButton link = new RadioButton(rb.getString("jevistree.dialog.copy.link"));
    final RadioButton copy = new RadioButton(rb.getString("jevistree.dialog.copy.copy"));
    //    final RadioButton clone = new RadioButton("Clone");
    final CheckBox recursion = new CheckBox(rb.getString("jevistree.dialog.copy.substructure"));
    final CheckBox includeSamples = new CheckBox(rb.getString("jevistree.dialog.copy.adddata"));
    final NumberSpinner count = new NumberSpinner(BigDecimal.valueOf(1), BigDecimal.valueOf(1));

    /**
     *
     * @param owner
     * @param object
     * @param newParent
     * @return
     */
    public Response show(Stage owner, final JEVisObject object, final JEVisObject newParent, DefaultAction defaultAction) {

        boolean linkOK = false;
        try {

            if (!object.getDataSource().getCurrentUser().canCreate(object.getID())) {
                showError("Error", "Permission denied", "You have no permission to create this object here");
                return Response.CANCEL;
            }

            if (newParent.getJEVisClassName().equals("Link")
                    || newParent.getJEVisClassName().equals("View Directory")) {
                linkOK = true;
            }

            if (!linkOK && !object.isAllowedUnder(newParent)) {
                showError("Error", "Rules Error", "Its not allowed to create an '" + object.getJEVisClass().getName() + "' under an '" + newParent.getJEVisClass().getName()
                        + "' object");
                return Response.CANCEL;
            }
            //Dont allow recrusion if the process faild the resursion check
            recursionAllowed = parentCheck(object, newParent);
            recursion.setDisable(!recursionAllowed);
            move.setDisable(!recursionAllowed);

//            if (!parentCheck(object, newParent)) {
//                return Response.CANCEL;
//            }
        } catch (JEVisException ex) {
            Logger.getLogger(CopyObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error", ex.getMessage(), ex.getCause().getMessage());
            return Response.CANCEL;
        }

        final Stage stage = new Stage();

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        stage.setTitle(rb.getString("jevistree.dialog.copy.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(450);
        stage.setHeight(400);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        scene.setCursor(Cursor.DEFAULT);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(rb.getString("jevistree.dialog.copy.chooseaction"));
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView imageView = ResourceLoader.getImage(ICON, 64, 64);

        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(imageView);

        header.setLeft(vboxLeft);

        header.setRight(vboxRight);

        HBox buttonPanel = new HBox();

        ok.setDefaultButton(true);
        ok.setDisable(true);

        Button cancel = new Button(rb.getString("jevistree.dialog.copy.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(8);

        final ToggleGroup group = new ToggleGroup();

        move.setMaxWidth(Double.MAX_VALUE);
        move.setMinWidth(120);
        link.setMaxWidth(Double.MAX_VALUE);
        copy.setMaxWidth(Double.MAX_VALUE);
//        clone.setMaxWidth(Double.MAX_VALUE);

        link.setToggleGroup(group);
        move.setToggleGroup(group);
        copy.setToggleGroup(group);
//        clone.setToggleGroup(group);

        nameField.setPrefWidth(250);
        nameField.setPromptText(rb.getString("jevistree.dialog.copy.name.prompt"));

        final Label nameLabel = new Label(rb.getString("jevistree.dialog.copy.name"));
        final Label countLabel = new Label(rb.getString("jevistree.dialog.copy.amount"));

//        final Label info = new Label("Test");
//        info.wrapTextProperty().setValue(true);
//        info.setPrefRowCount(4);
//        info.setDisable(true);
//        info.setMinWidth(1d);
//        info.setMaxWidth(200);
//        info.setPrefWidth(200);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {

                if (t1 != null) {
//                    System.out.println("new toggel: " + t1);
                    if (t1.equals(move)) {
//                        infoText = String.format("Move '%s' into '%s'", object.getName(), newParent.getName());
                        ok.setDisable(false);
                        nameField.setDisable(true);
                        nameLabel.setDisable(true);
                        countLabel.setDisable(true);
                        count.setDisable(true);

                        includeSamples.setDisable(true);
                        includeSamples.setSelected(false);
                        recursion.setDisable(true);
                        recursion.setSelected(false);

                    } else if (t1.equals(link)) {
//                        infoText = String.format("Create an new link of '%s' into '%s'", object.getName(), newParent.getName());
                        nameField.setDisable(false);
                        count.setDisable(true);
                        nameLabel.setDisable(false);
                        countLabel.setDisable(true);
                        includeSamples.setDisable(true);
                        includeSamples.setSelected(false);
                        recursion.setDisable(true);
                        recursion.setSelected(false);

                        checkName();

                    } else if (t1.equals(copy)) {
//                        infoText = String.format("Copy '%s' into '%s'", object.getName(), newParent.getName());

//                        infoText = String.format("<html>Copy <font color=\"#DB6A6A\">'%s'</font> into <font color=\"#DB6A6A\">'%s'</font> without data</html>", object.getName(), newParent.getName());
                        nameField.setDisable(false);
                        count.setDisable(false);
                        nameLabel.setDisable(false);
                        countLabel.setDisable(false);
                        nameField.setText(object.getName());

                        includeSamples.setDisable(!includeDataAllowed);
                        includeSamples.setSelected(false);
                        recursion.setDisable(!recursionAllowed);
                        recursion.setSelected(false);

                        checkName();
                    }
//                    else if (t1.equals(clone)) {
//                        infoText = String.format("Clone '%s' into '%s' with all data", object.getName(), newParent.getName());
//                        nameField.setDisable(false);
//                        count.setDisable(false);
//                        nameLabel.setDisable(false);
//                        countLabel.setDisable(false);
//                        nameField.setText(object.getName());
//                        checkName();
//                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

//                            info.setText(infoText);
                        }
                    });
                }

            }
        });

        try {
//            System.out.println("-> Object: " + object.getJEVisClass());
//            System.out.println("newParent: " + newParent.getJEVisClass());
//            System.out.println("Is allowed under target: " + object.isAllowedUnder(newParent));
//            System.out.println("");

            link.setDisable(!linkOK);

            if (object.isAllowedUnder(newParent)) {
                move.setDisable(false);
                copy.setDisable(false);
//                clone.setDisable(false);
            } else {
                move.setDisable(true);
                copy.setDisable(true);
//                clone.setDisable(true);
            }

            if (!link.isDisable()) {
                group.selectToggle(link);
                nameField.setText(object.getName());
                ok.setDisable(false);
            } else if (!move.isDisable()) {
                group.selectToggle(move);
            } else if (!copy.isDisable()) {
                group.selectToggle(copy);
            }

        } catch (JEVisException ex) {
            Logger.getLogger(CopyObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        HBox nameBox = new HBox(5);
        nameBox.getChildren().setAll(nameLabel, nameField);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        final HBox countBox = new HBox(5);
        countBox.getChildren().setAll(countLabel, count);
        countBox.setAlignment(Pos.CENTER_LEFT);

        Separator s1 = new Separator(Orientation.HORIZONTAL);
        GridPane.setMargin(s1, new Insets(5, 0, 10, 0));

        //check allowed
        int yAxis = 0;

        gp.add(link, 0, yAxis);
        gp.add(move, 0, ++yAxis);

        gp.add(copy, 0, ++yAxis);
//        gp.add(clone, 0, ++x);

//        gp.add(new Separator(Orientation.VERTICAL), 1, 0, 1, 3);
        gp.add(s1, 0, ++yAxis, 3, 1);

        gp.add(recursion, 0, ++yAxis, 3, 1);//new
        gp.add(includeSamples, 0, ++yAxis, 3, 1);//new
        gp.add(countBox, 0, ++yAxis, 3, 1);
        gp.add(new Separator(Orientation.HORIZONTAL), 0, ++yAxis, 3, 1);
        gp.add(nameBox, 0, ++yAxis, 3, 1);

//        gp.add(info, 2, 0, 1, 4);
//
//        GridPane.setHgrow(info, Priority.ALWAYS);
//        GridPane.setVgrow(info, Priority.ALWAYS);
//        GridPane.setHalignment(info, HPos.LEFT);
//        GridPane.setValignment(info, VPos.TOP);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), gp, buttonPanel);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();

                if (group.getSelectedToggle().equals(move)) {
                    response = Response.MOVE;
                } else if (group.getSelectedToggle().equals(link)) {
                    response = Response.LINK;
                } else if (group.getSelectedToggle().equals(copy)) {
                    response = Response.COPY;
                }
//                else if (group.getSelectedToggle().equals(clone)) {
//                    response = Response.CLONE;
//                }

            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;

            }
        });

        nameField.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (nameField.getText() != null && !nameField.getText().equals("")) {
                    ok.setDisable(false);
                }
            }
        });

        switch (defaultAction) {
            case COPY:
                if (!copy.isDisable()) {
                    copy.setSelected(true);
                }
                break;
            case MOVE:
                if (!move.isDisable()) {
                    move.setSelected(true);
                }
                break;
            case LINK:
                if (!link.isDisable()) {
                    link.setSelected(true);
                }
                break;

        }

        stage.requestFocus();
        stage.sizeToScene();
        stage.showAndWait();

        return response;
    }

    public enum Response {

        MOVE, LINK, CANCEL, COPY //,CLONE
    }

    private Response response = Response.CANCEL;

    public JEVisClass getCreateClass() {
        return createClass;
    }

    public String getCreateName() {
        return nameField.getText();
    }

    public boolean isRecursion() {
        return recursion.isSelected();
    }

    public int getCreateCount() {

        if (count.getNumber().intValue() > 0 && count.getNumber().intValue() < 500) {
            return count.getNumber().intValue();
        } else {
            return 1;
        }
    }

    public enum DefaultAction {

        MOVE, LINK, COPY
    }

    public boolean isIncludeData() {
        return includeSamples.isSelected();
    }

    private void checkName() {
        if (nameField.getText() != null && !nameField.getText().isEmpty()) {
            ok.setDisable(false);
        } else {
            ok.setDisable(true);
        }
    }

    private void showError(String title, String titleLong, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(titleLong);
        alert.setContentText(message);
        alert.setResizable(true);
        alert.show();
    }

    public boolean parentCheck(JEVisObject obj, JEVisObject target) {
        try {
//            System.out.println("parentCheck: " + obj.getName() + " -> " + target.getName());
            //Check if its the same object
            if (target.equals(obj)) {
//                System.out.println("Error 1");
                return false;
            }

            //check if the obj its os own parent
            for (JEVisObject parent : target.getParents()) {
                if (parent.equals(target)) {
                    showError("Error", "Recursion Error", "Recursion error detected. ");
                    return false;
                }
                if (!parentCheck(obj, parent)) {
//                    System.out.println("Error 3.2");
                    return false;
                }

            }

        } catch (JEVisException ex) {
            return false;
        }
        return true;
    }
}
