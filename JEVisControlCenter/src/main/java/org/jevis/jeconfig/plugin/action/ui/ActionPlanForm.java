package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.plugin.action.data.ActionPlan;
import org.jevis.jeconfig.tool.ScreenSize;

import java.util.Optional;

public class ActionPlanForm extends Alert {


    Label nameLabel = new Label("Name: ");
    Label statusLabel = new Label("Status");
    Label fieldsLabel = new Label("Bereiche");
    Label mediumLabel = new Label("Medien");

    JFXTextField nameField = new JFXTextField();

    ListView<String> statusListView = new ListView<>();
    ListView<String> fieldsListView = new ListView<>();
    ListView<String> mediumListView = new ListView<>();
    private ActionPlan actionPlan;

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

        gridPane.add(nameField, 1, 0);

        getDialogPane().setContent(gridPane);
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
