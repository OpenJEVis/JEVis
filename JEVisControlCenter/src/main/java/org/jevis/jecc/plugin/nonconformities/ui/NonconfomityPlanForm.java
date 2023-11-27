package org.jevis.jecc.plugin.nonconformities.ui;


import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.plugin.nonconformities.data.NonconformityPlan;
import org.jevis.jecc.tool.ScreenSize;

import java.util.Optional;

public class NonconfomityPlanForm extends Alert {


    Label nameLabel = new Label(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.name"));


    Label mediumLabel = new Label(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.medium"));
    Label seuLabel = new Label(I18n.getInstance().getString("plugin.nonconformities.seu"));
    Label fieldLabel = new Label(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.field"));


    Label numberPrefix = new Label(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.prefix"));
    TextField f_numberPrefix = new TextField();

    TextField nameField = new TextField();



    ListView<String> mediumListView = new ListView<>();
    ListView<String> seuListView = new ListView<>();
    ListView<String> fieldListView = new ListView<>();


    private final NonconformityPlan nonconformityPlan;
    StackPane stackPane = new StackPane();
    private final double iconSize = 12;

    public NonconfomityPlanForm(NonconformityPlan nonconformityPlan) {
        super(AlertType.INFORMATION);
        this.nonconformityPlan = nonconformityPlan;
        this.initOwner(ControlCenter.getStage());

        setTitle(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.header"));
        setResizable(true);
        setWidth(ScreenSize.fitScreenWidth(800));
        setHeight(ScreenSize.fitScreenHeight(400));

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);


        GridPane mediumPane = buildCustomList(mediumListView);
        GridPane fieldPane = buildCustomList(fieldListView);
        GridPane seuPane = buildCustomList(seuListView);


        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(8);


        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(numberPrefix, 0, 1);
        gridPane.add(f_numberPrefix, 1, 1);
        gridPane.add(new Region(), 0, 2);

        gridPane.add(mediumLabel, 3, 0, 2, 1);
        gridPane.add(mediumPane, 3, 1, 2, 2);

        gridPane.add(fieldLabel, 0, 3, 2, 1);
        gridPane.add(fieldPane, 0, 4, 2, 1);

        gridPane.add(seuLabel, 3, 3, 2, 1);
        gridPane.add(seuPane, 3, 4, 2, 1);

        stackPane.getChildren().add(gridPane);
        getDialogPane().setContent(stackPane);
        updateView(nonconformityPlan);
    }


    public void updateView(NonconformityPlan nonconformityPlan) {
        //nameField.setText(actionPlan.getName());

        nameField.textProperty().bindBidirectional(nonconformityPlan.getName());
        f_numberPrefix.textProperty().bindBidirectional(nonconformityPlan.prefixProperty());


        mediumListView.setItems(nonconformityPlan.getMediumTags());
        fieldListView.setItems(nonconformityPlan.getFieldsTags());
        seuListView.setItems(nonconformityPlan.getSignificantEnergyUseTags());


    }


    private GridPane buildCustomList(ListView<String> listView) {
        Button addButton = new Button("", ControlCenter.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
        Button removeButton = new Button("", ControlCenter.getSVGImage(Icon.MINUS_CIRCLE, iconSize, iconSize));

        addButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.newentry"));
            dialog.setHeaderText(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.addnewentry"));
            dialog.setContentText(I18n.getInstance().getString("plugin.nonconformities.nonconformityplan.dialog.name" + ":"));

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
