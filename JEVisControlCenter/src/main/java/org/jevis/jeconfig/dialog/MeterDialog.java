package org.jevis.jeconfig.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.plugin.object.attribute.AttributeEditor;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;

import java.util.ArrayList;
import java.util.List;

public class MeterDialog {
    private static final Logger logger = LogManager.getLogger(MeterDialog.class);
    private final JEVisClass jeVisClass;
    private List<JEVisClass> possibleParents = new ArrayList<>();
    private JEVisDataSource ds;
    private Response response;
    private Stage stage;
    private GridPane gp;
    private JEVisObject newObject;
    private List<AttributeEditor> attributeEditors = new ArrayList<>();
    private String name;

    public MeterDialog(JEVisDataSource ds, JEVisClass jeVisClass) {
        this.ds = ds;
        this.jeVisClass = jeVisClass;

        try {
            for (JEVisClass aClass : ds.getJEVisClasses()) {
                List<JEVisClassRelationship> relationships = aClass.getRelationships(3);
                int count = 0;

                for (JEVisClassRelationship jeVisClassRelationship : relationships) {
                    if (jeVisClassRelationship.getStart() != null && jeVisClassRelationship.getEnd() != null) {
                        if (jeVisClassRelationship.getStart().equals(jeVisClass) && jeVisClassRelationship.getEnd().equals(aClass)) {
                            count++;
                        } else if (jeVisClassRelationship.getStart().equals(aClass) && jeVisClassRelationship.getEnd().equals(jeVisClass)) {
                            count++;
                        }
                    }
                }

                if (count == 2) {
                    possibleParents.add(aClass);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        possibleParents.remove(jeVisClass);
    }

    public Response showNewWindow() {
        response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("graph.selection.title"));

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(JEConfig.getStage());

        double maxScreenWidth = Screen.getPrimary().getBounds().getMaxX();
        stage.setWidth(maxScreenWidth * 0.85);

        stage.setHeight(768);
        stage.setResizable(true);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(12));
        vBox.setSpacing(4);

        gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);

        Label parentLabel = new Label(I18n.getInstance().getString("jevis.types.parent"));
        VBox parentVBox = new VBox(parentLabel);
        parentVBox.setAlignment(Pos.CENTER);

        Button treeButton = new Button(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        TextField nameField = new TextField();

        Region targetSpace = new Region();
        targetSpace.setPrefWidth(20);

        HBox targetBox = new HBox(parentVBox, treeButton, targetSpace, nameVBox, nameField);
        targetBox.setSpacing(4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button ok = new Button(I18n.getInstance().getString("jevistree.dialog.new.ok"));
        HBox.setHgrow(ok, Priority.NEVER);
        Button cancel = new Button(I18n.getInstance().getString("jevistree.dialog.new.cancel"));
        HBox.setHgrow(cancel, Priority.NEVER);

        Separator sep1 = new Separator(Orientation.HORIZONTAL);

        HBox buttonRow = new HBox(spacer, cancel, ok);
        buttonRow.setPadding(new Insets(4));
        buttonRow.setSpacing(10);

        VBox.setVgrow(targetBox, Priority.NEVER);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonRow, Priority.NEVER);
        vBox.setFillWidth(true);
        vBox.getChildren().setAll(DialogHeader.getDialogHeader("measurement_instrument.png", I18n.getInstance().getString("plugin.meters.title")), targetBox, gp, sep1, buttonRow);

        treeButton.setOnAction(event -> {
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allCurrentClassFilter = SelectTargetDialog.buildMultiClassFilter(jeVisClass, possibleParents);
            allFilter.add(allCurrentClassFilter);

            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allCurrentClassFilter, null, SelectionMode.SINGLE);
            selectTargetDialog.setInitOwner(stage.getScene().getWindow());

            List<UserSelection> openList = new ArrayList<>();


            if (selectTargetDialog.show(
                    ds,
                    I18n.getInstance().getString("dialog.target.data.title"),
                    openList
            ) == SelectTargetDialog.Response.OK) {
                logger.trace("Selection Done");

                List<UserSelection> selections = selectTargetDialog.getUserSelection();
                for (UserSelection us : selections) {
                    try {
                        newObject = us.getSelectedObject().buildObject(I18n.getInstance().getString("newobject.new.title"), jeVisClass);
                        newObject.commit();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                treeButton.setText(newObject.getName());
                nameField.setText(newObject.getName());

                nameField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        name = newValue;
                    }
                });

                updateGrid();
            }

        });

        ok.setOnAction(event -> {
            for (AttributeEditor attributeEditor : attributeEditors) {
                if (attributeEditor.hasChanged()) {
                    try {
                        attributeEditor.commit();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                newObject.setName(name);
                newObject.commit();
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            response = Response.OK;
            stage.close();
        });

        cancel.setOnAction(event -> {
            response = Response.CANCEL;
            stage.close();
            if (newObject != null) {
                try {
                    ds.deleteObject(newObject.getID());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        });

        stage.showAndWait();

        return response;
    }

    public Response showReplaceWindow(JEVisObject selectedMeter) {
        response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("graph.selection.title"));

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(JEConfig.getStage());

        double maxScreenWidth = Screen.getPrimary().getBounds().getMaxX();
        stage.setWidth(maxScreenWidth * 0.85);

        stage.setHeight(768);
        stage.setResizable(true);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(12));
        vBox.setSpacing(4);

        gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        TextField nameField = new TextField(selectedMeter.getName());

        HBox targetBox = new HBox(nameVBox, nameField);
        targetBox.setSpacing(4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button ok = new Button(I18n.getInstance().getString("jevistree.dialog.new.ok"));
        HBox.setHgrow(ok, Priority.NEVER);
        Button cancel = new Button(I18n.getInstance().getString("jevistree.dialog.new.cancel"));
        HBox.setHgrow(cancel, Priority.NEVER);

        Separator sep1 = new Separator(Orientation.HORIZONTAL);

        HBox buttonRow = new HBox(spacer, cancel, ok);
        buttonRow.setPadding(new Insets(4));
        buttonRow.setSpacing(10);

        VBox.setVgrow(targetBox, Priority.NEVER);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonRow, Priority.NEVER);
        vBox.setFillWidth(true);
        vBox.getChildren().setAll(DialogHeader.getDialogHeader("measurement_instrument.png", I18n.getInstance().getString("plugin.meters.title")), targetBox, gp, sep1, buttonRow);

        newObject = selectedMeter;
        updateGrid();

        ok.setOnAction(event -> {
            for (AttributeEditor attributeEditor : attributeEditors) {
                if (attributeEditor.hasChanged()) {
                    try {
                        attributeEditor.commit();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                newObject.setName(name);
                newObject.commit();
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            response = Response.OK;
            stage.close();
        });

        cancel.setOnAction(event -> {
            response = Response.CANCEL;
            stage.close();
        });

        stage.showAndWait();

        return response;
    }

    private void updateGrid() {
        if (newObject != null) {
            gp.getChildren().clear();
            attributeEditors.clear();
            try {
                int column = 0;
                int row = 0;
                List<JEVisAttribute> attributes = newObject.getAttributes();
                for (JEVisAttribute attribute : attributes) {
                    int index = attributes.indexOf(attribute);

                    if (index == 2 || (index > 2 && index % 2 == 0)) {
                        column = 0;
                        row++;
                    }

                    Label typeName = new Label(I18nWS.getInstance().getTypeName(attribute.getType()));
                    VBox typeBox = new VBox(typeName);
                    typeBox.setAlignment(Pos.CENTER);

                    AttributeEditor attributeEditor = GenericAttributeExtension.getEditor(attribute.getType(), attribute);
                    attributeEditor.setReadOnly(false);
                    attributeEditors.add(attributeEditor);
                    VBox editorBox = new VBox(attributeEditor.getEditor());
                    editorBox.setAlignment(Pos.CENTER);

                    if (column < 2) {
                        gp.add(typeBox, column, row);
                    } else {
                        gp.add(typeBox, column + 1, row);
                    }
                    column++;

                    if (column < 2) {
                        gp.add(editorBox, column, row);
                    } else {
                        gp.add(editorBox, column + 1, row);
                    }
                    column++;
                }

                Separator separator = new Separator(Orientation.VERTICAL);
                gp.add(separator, 2, 0, 1, row + 1);

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
    }
}
