package org.jevis.jecc.dialog;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.application.I18nWS;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jecc.plugin.object.attribute.AttributeEditor;
import org.jevis.jecc.plugin.object.extension.GenericAttributeExtension;

import java.util.ArrayList;
import java.util.List;

public class EquipmentDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(EquipmentDialog.class);
    private final JEVisClass jeVisClass;
    private final List<JEVisClass> possibleParents = new ArrayList<>();
    private final JEVisDataSource ds;
    private final List<AttributeEditor> attributeEditors = new ArrayList<>();
    private Response response;
    private GridPane gp;
    private JEVisObject newObject;
    private String name;

    public EquipmentDialog(JEVisDataSource ds, JEVisClass jeVisClass) {
        super();
        this.ds = ds;
        this.jeVisClass = jeVisClass;
        setTitle(I18n.getInstance().getString("plugin.equipment.equipmentdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.equipment.equipmentdialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        try {
            for (JEVisClass aClass : ds.getJEVisClasses()) {
                List<JEVisClassRelationship> relationships = aClass.getRelationships(3);

                for (JEVisClassRelationship jeVisClassRelationship : relationships) {
                    if (jeVisClassRelationship.getStart() != null && jeVisClassRelationship.getEnd() != null) {
                        if (jeVisClassRelationship.getStart().equals(jeVisClass) && jeVisClassRelationship.getEnd().equals(aClass)) {
                            possibleParents.add(aClass);
                        }
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        possibleParents.remove(jeVisClass);

        response = Response.CANCEL;


        VBox vBox = new VBox();
        vBox.setPadding(new Insets(12));
        vBox.setSpacing(4);

        gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);

        Label parentLabel = new Label(I18n.getInstance().getString("jevis.types.parent"));
        VBox parentVBox = new VBox(parentLabel);
        parentVBox.setAlignment(Pos.CENTER);

        MFXButton treeButton = new MFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                ControlCenter.getImage("folders_explorer.png", 18, 18));

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        MFXTextField nameField = new MFXTextField();

        Region targetSpace = new Region();
        targetSpace.setPrefWidth(20);

        HBox targetBox = new HBox(parentVBox, treeButton, targetSpace, nameVBox, nameField);
        targetBox.setSpacing(4);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Separator sep1 = new Separator(Orientation.HORIZONTAL);

        VBox.setVgrow(targetBox, Priority.NEVER);
        VBox.setVgrow(gp, Priority.ALWAYS);
        vBox.setFillWidth(true);
        vBox.getChildren().setAll(DialogHeader.getDialogHeader("building_equipment.png", I18n.getInstance().getString("plugin.equipment.title")), targetBox, gp, sep1);
        getDialogPane().setContent(vBox);

        treeButton.setOnAction(event3 -> {
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisClass first = jeVisClass;
            if (possibleParents.size() > 0) {
                first = possibleParents.get(0);
                possibleParents.remove(first);
            }

            JEVisTreeFilter allCurrentClassFilter = SelectTargetDialog.buildMultiClassFilter(first, possibleParents);
            allFilter.add(allCurrentClassFilter);

            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allCurrentClassFilter, null, SelectionMode.SINGLE, ds, null);

            List<UserSelection> openList = new ArrayList<>();

            selectTargetDialog.setOnCloseRequest(event1 -> {
                if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
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
        });

        okButton.setOnAction(event -> {
            for (AttributeEditor attributeEditor : attributeEditors) {
                if (attributeEditor.hasChanged()) {
                    try {
                        attributeEditor.commit();
                    } catch (Exception e) {
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
            close();
        });

        cancelButton.setOnAction(event -> {
            response = Response.CANCEL;
            if (newObject != null) {
                try {
                    ds.deleteObject(newObject.getID(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            close();
        });
    }

    public void showReplaceWindow(JEVisObject selectedMeter) {
        response = Response.CANCEL;

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(12));
        vBox.setSpacing(4);

        gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);


        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        MFXTextField nameField = new MFXTextField(selectedMeter.getName());

        HBox targetBox = new HBox(nameVBox, nameField);
        targetBox.setSpacing(4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        MFXButton ok = new MFXButton(I18n.getInstance().getString("jevistree.dialog.new.ok"));
        HBox.setHgrow(ok, Priority.NEVER);
        MFXButton cancel = new MFXButton(I18n.getInstance().getString("jevistree.dialog.new.cancel"));
        HBox.setHgrow(cancel, Priority.NEVER);

        Separator sep1 = new Separator(Orientation.HORIZONTAL);

        HBox buttonRow = new HBox(spacer, cancel, ok);
        buttonRow.setPadding(new Insets(4));
        buttonRow.setSpacing(10);

        VBox.setVgrow(targetBox, Priority.NEVER);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonRow, Priority.NEVER);
        vBox.setFillWidth(true);
        vBox.getChildren().setAll(DialogHeader.getDialogHeader("building_equipment.png", I18n.getInstance().getString("plugin.equipment.title")), targetBox, gp, sep1, buttonRow);

        newObject = selectedMeter;

        getDialogPane().setContent(vBox);

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
            close();
        });

        cancel.setOnAction(event -> {
            response = Response.CANCEL;
            close();
        });

        show();
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

    public Response getResponse() {
        return response;
    }
}
