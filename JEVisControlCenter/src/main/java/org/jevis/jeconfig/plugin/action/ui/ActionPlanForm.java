package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.plugin.action.data.ActionPlan;
import org.jevis.jeconfig.tool.ScreenSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActionPlanForm extends Alert {


    Label nameLabel = new Label("Name: ");
    Label statusLabel = new Label("Status");
    Label fieldsLabel = new Label("Bereiche");
    Label mediumLabel = new Label("Medien");
    Label enpiLabel = new Label("EnPI");

    JFXTextField nameField = new JFXTextField();

    ListView<String> statusListView = new ListView<>();
    ListView<String> fieldsListView = new ListView<>();
    ListView<String> mediumListView = new ListView<>();
    ListView<JEVisObject> enpiListView = new ListView<>();
    private ActionPlan actionPlan;
    StackPane stackPane = new StackPane();

    public ActionPlanForm(ActionPlan actionPlan) {
        super(AlertType.INFORMATION);
        this.actionPlan = actionPlan;
        this.initOwner(JEConfig.getStage());

        setTitle(I18n.getInstance().getString("planform.editor.title"));
        setHeaderText(I18n.getInstance().getString("planform.editor.header"));
        setResizable(true);
        setWidth(ScreenSize.fitScreenWidth(800));
        setHeight(ScreenSize.fitScreenHeight(600));

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);

        GridPane statusPane = buildCustomList(statusListView);
        GridPane mediumPane = buildCustomList(mediumListView);
        GridPane fieldPane = buildCustomList(fieldsListView);
        GridPane enpiPane = buildENPIList(enpiListView);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(8);

        gridPane.add(nameLabel, 0, 0);
        gridPane.add(statusLabel, 0, 1);
        gridPane.add(statusPane, 0, 2, 3, 1);
        gridPane.add(mediumLabel, 0, 3);
        gridPane.add(mediumPane, 0, 4, 3, 1);
        gridPane.add(fieldsLabel, 0, 5);
        gridPane.add(fieldPane, 0, 6, 3, 1);
        gridPane.add(enpiLabel, 0, 7);
        gridPane.add(enpiPane, 0, 8, 3, 1);


        gridPane.add(nameField, 1, 0);

        stackPane.getChildren().add(gridPane);
        getDialogPane().setContent(stackPane);
        updateView(actionPlan);
    }


    public void updateView(ActionPlan actionPlan) {
        //nameField.setText(actionPlan.getName());

        nameField.textProperty().bindBidirectional(actionPlan.getName());
        System.out.println("Init status: " + actionPlan.getStatustags());
        statusListView.setItems(actionPlan.getStatustags());
        fieldsListView.setItems(actionPlan.getFieldsTags());
        mediumListView.setItems(actionPlan.getMediumTags());
    }

    private GridPane buildENPIList(ListView<JEVisObject> listView) {
        enpiListView.setCellFactory(new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        //System.out.println("Update Item: " + item + "   is empty: " + empty);
                        if (item != null) {
                            setText(item.getName());
                            try {
                                //setTooltip(new Tooltip(objectRelations.getObjectPath(item) + objectRelations.getRelativePath(item) + item.getName()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });
        //actionPlan.getEnpis().add(actionPlan.getObject());
        enpiListView.setItems(actionPlan.getEnpis());
        enpiListView.selectionModelProperty().addListener(new ChangeListener<MultipleSelectionModel<JEVisObject>>() {
            @Override
            public void changed(ObservableValue<? extends MultipleSelectionModel<JEVisObject>> observable, MultipleSelectionModel<JEVisObject> oldValue, MultipleSelectionModel<JEVisObject> newValue) {

            }
        });

        Button addButton = new Button("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, 8, 14));
        Button removeButton = new Button("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 8, 14));
        addButton.setOnAction(event -> {
            try {
                List<JEVisClass> classes = new ArrayList<>();

                TreeSelectionDialog treeSelectionDialog = new TreeSelectionDialog(actionPlan.getObject().getDataSource(), classes, SelectionMode.MULTIPLE, new ArrayList<>(), false);
                Optional<ButtonType> optional = treeSelectionDialog.showAndWait();
                if (optional.get() == treeSelectionDialog.buttonOK) {
                    System.out.println("apply");
                    treeSelectionDialog.getUserSelection().forEach(userSelection -> {
                        actionPlan.getEnpis().add(userSelection.getSelectedObject());
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }


        });

        removeButton.setOnAction(event -> {
            try {
                listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
            }
        });

        listView.setMaxHeight(100);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);

        gridPane.add(listView, 0, 0, 1, 3);
        gridPane.add(addButton, 1, 0);
        gridPane.add(removeButton, 1, 1);

        return gridPane;

    }

    private GridPane buildCustomList(ListView<String> listView) {
        Button addButton = new Button("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, 8, 14));
        Button removeButton = new Button("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 8, 14));

        addButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Neuer Eintrag");
            dialog.setHeaderText("Neuen Eintrag hinzufügen");
            dialog.setContentText("Name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                listView.getItems().add(result.get());
            }
        });

        removeButton.setOnAction(event -> {
            try {
                listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
            }
        });

        //Icon.PLUS_CIRCLE
        //Icon.MINUS_CIRCLE

        listView.setMaxHeight(100);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);

        gridPane.add(listView, 0, 0, 1, 3);
        gridPane.add(addButton, 1, 0);
        gridPane.add(removeButton, 1, 1);

        return gridPane;

    }
}
