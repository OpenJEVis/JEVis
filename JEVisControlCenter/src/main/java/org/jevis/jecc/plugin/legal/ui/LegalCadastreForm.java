package org.jevis.jecc.plugin.legal.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.plugin.legal.data.IndexOfLegalProvisions;
import org.jevis.jecc.tool.ScreenSize;

import java.util.Optional;

public class LegalCadastreForm extends Alert {

    Label nameLabel = new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.planname"));


    JFXTextField nameField = new JFXTextField();

    Label l_category = new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.category"));
    Label l_scope = new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.scope"));

    ListView<String> categoryListView = new ListView<>();
    ListView<String> validityListView = new ListView<>();
    StackPane stackPane = new StackPane();
    private IndexOfLegalProvisions indexOfLegalProvisions;
    private double iconSize = 12;

    public LegalCadastreForm(IndexOfLegalProvisions indexOfLegalProvisions) {
        super(AlertType.INFORMATION);
        this.indexOfLegalProvisions = indexOfLegalProvisions;
        this.initOwner(ControlCenter.getStage());

        setTitle(I18n.getInstance().getString("plugin.indexoflegalprovisions.indexoflegalprovisions.dialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.indexoflegalprovisions.indexoflegalprovisions.dialog.header"));
        setResizable(true);
        setWidth(ScreenSize.fitScreenWidth(800));
        setHeight(ScreenSize.fitScreenHeight(400));

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);


        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(8);

        GridPane categoryPane = buildCustomList(categoryListView);
        GridPane levelPane = buildCustomList(validityListView);


        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);


        gridPane.add(l_category, 0, 4, 2, 1);
        gridPane.add(categoryPane, 0, 5, 2, 2);

        gridPane.add(l_scope, 0, 2, 2, 1);
        gridPane.add(levelPane, 0, 3, 2, 1);


        stackPane.getChildren().add(gridPane);
        getDialogPane().setContent(stackPane);

        updateView(indexOfLegalProvisions);
    }


    public void updateView(IndexOfLegalProvisions indexOfLegalProvisions) {

        nameField.textProperty().bindBidirectional(indexOfLegalProvisions.getName());

        categoryListView.setItems(indexOfLegalProvisions.getCategories());
        validityListView.setItems(indexOfLegalProvisions.getScopes());


    }


    private GridPane buildCustomList(ListView<String> listView) {
        Button addButton = new Button("", ControlCenter.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
        Button removeButton = new Button("", ControlCenter.getSVGImage(Icon.MINUS_CIRCLE, iconSize, iconSize));

        addButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle(I18n.getInstance().getString("plugin.indexoflegalprovisions.newentry.title"));
            dialog.setHeaderText(I18n.getInstance().getString("plugin.indexoflegalprovisions.newentry.header"));
            dialog.setContentText(I18n.getInstance().getString("plugin.indexoflegalprovisions.newentry.content"));

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
