/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.application.jevistree.plugin;


import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.resource.ResourceLoader;
import org.jevis.jecc.application.unit.SampleRateNode;
import org.jevis.jecc.application.unit.UnitPanel;
import org.jevis.jecc.dialog.DialogHeader;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AttributeSettingsDialog {
    private static final Logger logger = LogManager.getLogger(AttributeSettingsDialog.class);
    private Response response = Response.CANCEL;
    private JEVisAttribute _attribute;
    private UnitPanel upDisplay;
    private UnitPanel upInput;
    private SampleRateNode _inputSampleRate;
    private SampleRateNode _displaySampleRate;

    private void saveInDataSource() {
//        logger.info("Save");
        try {
//            logger.info("Display unit: " + UnitManager.getInstance().format(upDisplay.getSelectedUnit()));
//            logger.info("Display sample rate: " + _displaySampleRate.nextPeriod());

            _attribute.setDisplayUnit(upDisplay.getSelectedUnit());
            _attribute.setDisplaySampleRate(_displaySampleRate.getPeriod());
            _attribute.setInputUnit(upInput.getSelectedUnit());
            _attribute.setInputSampleRate(_inputSampleRate.getPeriod());
            _attribute.commit();

        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    public Response show(Stage owner, final JEVisAttribute att) throws JEVisException {
        final Stage stage = new Stage();
        _attribute = att;

        stage.setTitle("Settings");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
//        stage.setX(position.getX());
//        stage.setY(position.getY());
        stage.setWidth(340);
        stage.setHeight(650);

        VBox root = new VBox();

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);

        ImageView imageView = ResourceLoader.getImage("1405444584_measure.png", 65, 65);

        Node header = DialogHeader.getDialogHeader("1405444584_measure.png", "Settings");

        stage.getIcons().add(imageView.getImage());

//        Node header = DialogHeader.getDialogHeader("1404313956_evolution-tasks.png", "Unit Selection");
        HBox buttonPanel = new HBox();

        final CheckBox setDefault = new CheckBox("Set as default");

        Button ok = new Button("OK");
        ok.setDefaultButton(true);

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);

        if (att.getDisplayUnit() != null) {
            att.getDisplayUnit().toString();
        }//            changeBaseUnigetDisplayUnitxt(att.getUnit().toString());

//        CheckBox keepDefault = new CheckBox("Set as DB unit");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonPanel.getChildren().addAll(setDefault, spacer, ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        _displaySampleRate = new SampleRateNode(att.getDisplaySampleRate());
        _inputSampleRate = new SampleRateNode(att.getInputSampleRate());

        Tab displayTab = new Tab("Data Visualisation");
        upDisplay = new UnitPanel(att.getDataSource(), att.getDisplayUnit(), true);

        VBox dispplayBox = new VBox(5);
        dispplayBox.getChildren().addAll(buildTitelPane("Unit"), upDisplay, buildTitelPane("Sample Rate"), _displaySampleRate);
        displayTab.setContent(dispplayBox);
        displayTab.setClosable(false);

        Tab inputTab = new Tab("Data Input");
        upInput = new UnitPanel(att.getDataSource(), att.getInputUnit(), false);
        VBox inputBox = new VBox(5);
        inputBox.getChildren().addAll(buildTitelPane("Unit"), upInput, buildTitelPane("Sample Rate"), _inputSampleRate);

        inputTab.setContent(inputBox);
        inputTab.setClosable(false);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(displayTab, inputTab);

        //new Separator(Orientation.HORIZONTAL_TOP_LEFT)
        root.getChildren().setAll(header, tabPane, new Separator(Orientation.HORIZONTAL), buttonPanel);

        ok.setOnAction(t -> {
            response = Response.YES;
            if (upInput.getSelectedUnit().isCompatible(upDisplay.getSelectedUnit())) {
                if (setDefault.isSelected()) {
                    saveInDataSource();
                }
                stage.close();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Waring");
                alert.setHeaderText("Unit error");
                String s = "The display Unit '" + upDisplay.getSelectedUnit() + "' is not compatible with the input unit '" + upInput.getSelectedUnit() + "'.";
                alert.setContentText(s);
                alert.showAndWait();

//                    InfoDialog id = new InfoDialog();
//                    id.show(ControlCenter.getStage(), "Waring", "Unit error", "The display Unit '" + upDisplay.getSelectedUnit() + "' is not compatible with the input unit '" + upInput.getSelectedUnit() + "'.");
            }

        });

        cancel.setOnAction(t -> {
            response = Response.CANCEL;
            stage.close();

        });

        stage.sizeToScene();
        stage.showAndWait();

        return response;
    }

    private HBox buildTitelPane(String title) {
        HBox box = new HBox(5);
        box.setPadding(new Insets(5));
        box.setAlignment(Pos.BASELINE_CENTER);
        Label titleLabel = new Label(title);
        Separator sepLeft = new Separator(Orientation.HORIZONTAL);
        Separator sepRight = new Separator(Orientation.HORIZONTAL);
        box.getChildren().addAll(sepLeft, titleLabel, sepRight);
        HBox.setHgrow(sepLeft, Priority.ALWAYS);
        HBox.setHgrow(sepRight, Priority.ALWAYS);
        return box;
    }

    public enum Response {

        YES, CANCEL
    }

}
