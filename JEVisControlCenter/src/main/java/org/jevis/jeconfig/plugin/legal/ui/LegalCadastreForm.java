package org.jevis.jeconfig.plugin.legal.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.plugin.legal.data.LegalCadastre;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityPlan;
import org.jevis.jeconfig.tool.ScreenSize;

import java.util.Optional;

public class LegalCadastreForm extends Alert {

    Label nameLabel = new Label("Name");


    Label numberPrefix = new Label("Nr. Prefix");
    JFXTextField f_numberPrefix = new JFXTextField();

    JFXTextField nameField = new JFXTextField();






    private LegalCadastre legalCadastre;
    StackPane stackPane = new StackPane();
    private double iconSize = 12;

    public LegalCadastreForm(LegalCadastre legalCadastre) {
        super(AlertType.INFORMATION);
        this.legalCadastre = legalCadastre;
        this.initOwner(JEConfig.getStage());

        setTitle(I18n.getInstance().getString("plugin.Legalcadastre.Legalcadastre.dialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.Legalcadastre.Legalcadastre.dialog.header"));
        setResizable(true);
        setWidth(ScreenSize.fitScreenWidth(800));
        setHeight(ScreenSize.fitScreenHeight(400));

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);


        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(8);


        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);


        stackPane.getChildren().add(gridPane);
        getDialogPane().setContent(stackPane);

        updateView(legalCadastre);
    }


    public void updateView(LegalCadastre legalCadastre) {

        nameField.textProperty().bindBidirectional(legalCadastre.getName());



    }



    private GridPane buildCustomList(ListView<String> listView) {
        Button addButton = new Button("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
        Button removeButton = new Button("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, iconSize, iconSize));

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
