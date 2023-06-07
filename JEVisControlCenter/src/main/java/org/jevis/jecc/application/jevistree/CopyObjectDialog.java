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
package org.jevis.jecc.application.jevistree;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXRadioButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.resource.ResourceLoader;
import org.jevis.jecc.application.tools.NumberSpinner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Dialog to prompt the user about the copy/clone/move action
 *
 * @author fs
 */
public class CopyObjectDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(CopyObjectDialog.class);

    public static String ICON = "1403555565_stock_folder-move.png";

    private final MFXTextField nameField = new MFXTextField();
    private final boolean includeDataAllowed = true;
    private final MFXCheckbox includeValues = new MFXCheckbox(I18n.getInstance().getString("jevistree.dialog.copy.addvalues"));
    private final MFXRadioButton move = new MFXRadioButton(I18n.getInstance().getString("jevistree.dialog.copy.move"));
    private final MFXRadioButton link = new MFXRadioButton(I18n.getInstance().getString("jevistree.dialog.copy.link"));
    private final MFXRadioButton copy = new MFXRadioButton(I18n.getInstance().getString("jevistree.dialog.copy.copy"));
    private final MFXCheckbox recursion = new MFXCheckbox(I18n.getInstance().getString("jevistree.dialog.copy.substructure"));
    private final MFXCheckbox includeSamples = new MFXCheckbox(I18n.getInstance().getString("jevistree.dialog.copy.adddata"));
    private final boolean includeValuesAllowed = true;
    private final NumberSpinner count = new NumberSpinner(BigDecimal.valueOf(1), BigDecimal.valueOf(1));
    private boolean recursionAllowed = false;
    private Response response = Response.CANCEL;

    public CopyObjectDialog(final JEVisObject object, final JEVisObject newParent, DefaultAction defaultAction) {
        this(Collections.singletonList(object), newParent, defaultAction);
    }

    /**
     * @param objects
     * @param newParent
     */
    public CopyObjectDialog(final List<JEVisObject> objects, final JEVisObject newParent, DefaultAction defaultAction) {
        super();

        try {

            boolean recursionForAll = true;

            for (JEVisObject object : objects) {
                if (!object.getDataSource().getCurrentUser().canCreate(object.getID())) {
                    showError(I18n.getInstance().getString("jevistree.dialog.copy.permission.denied"), I18n.getInstance().getString("jevistree.dialog.copy.permission.denied.message"));
                    this.response = Response.CANCEL;
                }

                if (!object.getJEVisClassName().equals("Link") && !object.isAllowedUnder(newParent)) {
                    showError(I18n.getInstance().getString("jevistree.dialog.copy.rules.error"),
                            String.format(I18n.getInstance().getString("jevistree.dialog.copy.rules.error.message"), object.getJEVisClass().getName(),
                                    newParent.getJEVisClass().getName()));
                    this.response = Response.CANCEL;
                }
                //Don't allow recursion if the process failed the recursion check
                this.recursionAllowed = !TreeHelper.isOwnChildCheck(object, newParent);
                if (!recursionAllowed) {
                    recursionForAll = false;
                }

                /**
                 if (!recursionAllowed) {
                 this.recursion.setSelected(false);
                 }

                 this.recursion.setDisable(!this.recursionAllowed);
                 this.move.setDisable(!this.recursionAllowed);
                 **/
            }

            this.recursion.setSelected(recursionForAll);
            this.recursion.setDisable(!recursionForAll);
            this.move.setDisable(!recursionForAll);


        } catch (Exception ex) {
            logger.fatal(ex);
            showError(ex.getMessage(), ex.getCause().getMessage());
            this.response = Response.CANCEL;
        }


        setTitle(I18n.getInstance().getString("jevistree.dialog.copy.title"));
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox();

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        getDialogPane().setMinWidth(450);
        getDialogPane().setMinHeight(400);
        initStyle(StageStyle.UTILITY);
        setResizable(true);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(I18n.getInstance().getString("jevistree.dialog.copy.chooseaction"));
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

        ButtonType okType = new ButtonType(I18n.getInstance().getString("jevistree.dialog.copy.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("jevistree.dialog.copy.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(8);

        final ToggleGroup group = new ToggleGroup();

        this.move.setMaxWidth(Double.MAX_VALUE);
        this.move.setMinWidth(120);
        this.link.setMaxWidth(Double.MAX_VALUE);
        this.copy.setMaxWidth(Double.MAX_VALUE);

        this.link.setToggleGroup(group);
        this.move.setToggleGroup(group);
        this.copy.setToggleGroup(group);

        this.nameField.setPrefWidth(250);
        this.nameField.setPromptText(I18n.getInstance().getString("jevistree.dialog.copy.name.prompt"));

        final Label nameLabel = new Label(I18n.getInstance().getString("jevistree.dialog.copy.name"));
        final Label countLabel = new Label(I18n.getInstance().getString("jevistree.dialog.copy.amount"));

        group.selectedToggleProperty().addListener((ov, t, t1) -> {

            if (t1 != null) {
                if (t1.equals(this.move)) {
                    okButton.setDisable(false);
                    this.nameField.setDisable(true);
                    nameLabel.setDisable(true);
                    countLabel.setDisable(true);
                    this.count.setDisable(true);

                    this.includeSamples.setDisable(true);
                    this.includeSamples.setSelected(true);
                    this.includeValues.setDisable(true);
                    this.includeValues.setSelected(true);
                    this.recursion.setDisable(true);
                    this.recursion.setSelected(true);

                } else if (t1.equals(this.link)) {
                    this.nameField.setDisable(false);
                    this.count.setDisable(true);
                    nameLabel.setDisable(false);
                    countLabel.setDisable(true);
                    this.includeSamples.setDisable(true);
                    this.includeSamples.setSelected(false);
                    this.includeValues.setDisable(true);
                    this.includeValues.setSelected(false);
                    this.recursion.setDisable(true);
                    this.recursion.setSelected(false);

                    checkName(okButton);

                } else if (t1.equals(this.copy)) {
                    this.nameField.setDisable(false);
                    this.count.setDisable(false);
                    nameLabel.setDisable(false);
                    countLabel.setDisable(false);
                    //CopyObjectDialog.this.nameField.setText(objects.getName());

                    this.includeSamples.setDisable(!this.includeDataAllowed);
                    this.includeSamples.setSelected(true);
                    this.includeValues.setDisable(!this.includeValuesAllowed);
                    this.includeValues.setSelected(true);
                    this.recursion.setDisable(!this.recursionAllowed);
                    this.recursion.setSelected(true);

                    checkName(okButton);
                }

                if (objects.size() != 1) {
                    nameLabel.setDisable(true);
                    nameField.setDisable(true);
                }
            }

            if (!recursionAllowed) {
                this.recursion.setSelected(false);
            }
        });

        try {
            if (objects.size() == 1) {
                this.nameField.setText(objects.get(0).getName());
            } else {
                this.nameField.setText("*");
            }

            boolean isAllowedForAll = true;
            for (JEVisObject obj : objects) {
                if (!obj.isAllowedUnder(newParent)) {
                    isAllowedForAll = false;
                }
            }

            if (isAllowedForAll) {
                if (recursionAllowed) {
                    this.move.setDisable(false);
                }
                this.copy.setDisable(false);
            } else {
                this.move.setDisable(true);
                this.copy.setDisable(true);
            }

            if (!this.link.isDisable()) {
                group.selectToggle(this.link);
                //this.nameField.setText(objects.getName());
                okButton.setDisable(false);
            } else if (!this.move.isDisable()) {
                group.selectToggle(this.move);
            } else if (!this.copy.isDisable()) {
                group.selectToggle(this.copy);
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }

        HBox nameBox = new HBox(5);
        nameBox.getChildren().setAll(nameLabel, this.nameField);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        final HBox countBox = new HBox(5);
        countBox.getChildren().setAll(countLabel, this.count);
        countBox.setAlignment(Pos.CENTER_LEFT);

        Separator s1 = new Separator(Orientation.HORIZONTAL);
        GridPane.setMargin(s1, new Insets(5, 0, 10, 0));

        //check allowed
        int yAxis = 0;

        gp.add(this.link, 0, yAxis);
        gp.add(this.move, 0, ++yAxis);

        gp.add(this.copy, 0, ++yAxis);
        gp.add(s1, 0, ++yAxis, 3, 1);

        gp.add(this.recursion, 0, ++yAxis, 3, 1);//new
        gp.add(this.includeSamples, 0, ++yAxis, 3, 1);//new
        gp.add(this.includeValues, 0, ++yAxis, 3, 1);//new
        gp.add(countBox, 0, ++yAxis, 3, 1);
        gp.add(new Separator(Orientation.HORIZONTAL), 0, ++yAxis, 3, 1);
        gp.add(nameBox, 0, ++yAxis, 3, 1);


        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), gp);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(header, Priority.NEVER);

        getDialogPane().setContent(root);

        okButton.setOnAction(t -> {
            if (group.getSelectedToggle().equals(this.move)) {
                this.response = Response.MOVE;
            } else if (group.getSelectedToggle().equals(this.link)) {
                this.response = Response.LINK;
            } else if (group.getSelectedToggle().equals(this.copy)) {
                this.response = Response.COPY;
            }
//                else if (group.getSelectedToggle().equals(clone)) {
//                    response = Response.CLONE;
//                }
            close();
        });

        cancelButton.setOnAction(t -> {
            this.response = Response.CANCEL;
            close();
        });

        this.nameField.setOnKeyPressed(t -> {
            if (this.nameField.getText() != null && !this.nameField.getText().equals("")) {
                okButton.setDisable(false);
            }
        });

        switch (defaultAction) {
            case COPY:
                if (!this.copy.isDisable()) {
                    this.copy.setSelected(true);
                }
                break;
            case MOVE:
                if (!this.move.isDisable()) {
                    this.move.setSelected(true);
                }
                break;
            case LINK:
                if (!this.link.isDisable()) {
                    this.link.setSelected(true);
                }
                break;

        }

        stage.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                if (this.move.isSelected()) {
                    okButton.fire();
                    ev.consume();
                }
            }
        });

        Platform.runLater(stage::requestFocus);
        Platform.runLater(stage::toFront);
    }

    public Response getResponse() {
        return response;
    }

    public String getCreateName() {
        return this.nameField.getText();
    }

    public boolean isRecursion() {
        return this.recursion.isSelected();
    }

    public int getCreateCount() {

        if (this.count.getNumber().intValue() > 0 && this.count.getNumber().intValue() < 500) {
            return this.count.getNumber().intValue();
        } else {
            return 1;
        }
    }

    public boolean isIncludeData() {
        return this.includeSamples.isSelected();
    }

    public boolean isIncludeValues() {
        return this.includeValues.isSelected();
    }

    private void checkName(Button okButton) {
        okButton.setDisable(this.nameField.getText() == null || this.nameField.getText().isEmpty());
    }

    private void showError(String titleLong, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(I18n.getInstance().getString("jevistree.dialog.copy.recursion.alert.title"));
        alert.setHeaderText(titleLong);
        alert.setContentText(message);
        alert.setResizable(true);
        alert.show();
    }

    public boolean parentCheck(JEVisObject obj, JEVisObject target) {
        try {
//            logger.info("parentCheck: " + obj.getName() + " -> " + target.getName());
            //Check if its the same object
            if (target.equals(obj)) {
//                logger.info("Error 1");
                return false;
            }

            //check if the obj its os own parent
            for (JEVisObject parent : target.getParents()) {
                if (parent.equals(target)) {
                    showError(I18n.getInstance().getString("jevistree.dialog.copy.recursion.error"),
                            I18n.getInstance().getString("jevistree.dialog.copy.recursion.error.message"));
                    return false;
                }
                if (!parentCheck(obj, parent)) {
//                    logger.info("Error 3.2");
                    return false;
                }

            }

        } catch (JEVisException ex) {
            return false;
        }
        return true;
    }

    public enum Response {

        MOVE, LINK, CANCEL, COPY //,CLONE
    }

    public enum DefaultAction {

        MOVE, LINK, COPY
    }
}
