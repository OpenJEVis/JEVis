package org.jevis.jeconfig.plugin.Dashboard;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordAnalysis;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.wizzard.Wizard;
import org.jevis.jeconfig.tool.I18n;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DashBoardToolbar extends ToolBar {


    public DashBoardToolbar() {
    }


    public void updateToolbar(final DashBordPlugIn dashBordPlugIn, final DashBordAnalysis analyses) {
        Label analysisLabel = new Label(I18n.getInstance().getString("plugin.scada.analysis"));
        ComboBox<JEVisObject> listAnalysesComboBox = new ComboBox();
        listAnalysesComboBox.setPrefWidth(300);

//        try {
//            JEVisClass sadaAnalyses = ds.getJEVisClass(CLASS_SCADA_ANALYSIS);
//            listAnalysesComboBox.getItems().addAll(ds.getObjects(sadaAnalyses, true));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//
//        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
//            @Override
//            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
//                final ListCell<JEVisObject> cell = new ListCell<JEVisObject>() {
//
////                    {
////                        super.setPrefWidth(300);
////                    }
//
//                    @Override
//                    protected void updateItem(JEVisObject item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (item != null && !empty) {
//                            setText(item.getName());
//                            setGraphic(null);
//                            Tooltip tt = new Tooltip();
//                            tt.setText("ID: " + item.getID());
//                            setTooltip(tt);
//                        }
//
//
//                    }
//                };
//
//                return cell;
//            }
//        };
//
//        listAnalysesComboBox.setCellFactory(cellFactory);
//        listAnalysesComboBox.setButtonCell(cellFactory.call(null));
//
//        ComboBox<SCADAAnalysis.BGMode> listBGType = new ComboBox();
//        listBGType.setItems(FXCollections.observableArrayList(SCADAAnalysis.BGMode.values()));
//
//        Callback<ListView<SCADAAnalysis.BGMode>, ListCell<SCADAAnalysis.BGMode>> bgFactory = new Callback<ListView<SCADAAnalysis.BGMode>, ListCell<SCADAAnalysis.BGMode>>() {
//            @Override
//            public ListCell<SCADAAnalysis.BGMode> call(ListView<SCADAAnalysis.BGMode> param) {
//                final ListCell<SCADAAnalysis.BGMode> cell = new ListCell<SCADAAnalysis.BGMode>() {
//
//                    protected void updateItem(SCADAAnalysis.BGMode item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (item != null && !empty) {
//                            String localname = "";
//                            switch (item) {
//                                case STRETCH_HEIGHT:
//                                    localname = I18n.getInstance().getString("plugin.scada.background.stretch_height");
//                                    break;
//                                case STRETCH_WIDTH:
//                                    localname = I18n.getInstance().getString("plugin.scada.background.stretch_width");
//                                    break;
//                                case STRETCH_BOTH:
//                                    localname = I18n.getInstance().getString("plugin.scada.background.stretch_both");
//                                    break;
//                                case ABSOLUTE:
//                                    localname = I18n.getInstance().getString("plugin.scada.background.stretch_none");
//                                    break;
//                            }
//                            setText(localname);
//                            setGraphic(null);
//                        }
//                    }
//                };
//
//                return cell;
//            }
//        };
//        listBGType.setCellFactory(bgFactory);
//        listBGType.setButtonCell(bgFactory.call(null));

        double iconSize = 20;


        ToggleButton treeButton = new ToggleButton("", JEConfig.getImage("Data.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(treeButton);

        ToggleButton settingsButton = new ToggleButton("", JEConfig.getImage("Service Manager.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(settingsButton);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);


        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        ToggleButton zoomIn = new ToggleButton("", JEConfig.getImage("zoomIn_32.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomIn);

        ToggleButton zoomOut = new ToggleButton("", JEConfig.getImage("zoomOut_32.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);

        ToggleButton enlarge = new ToggleButton("", JEConfig.getImage("enlarge_32.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(enlarge);


        final ImageView lockIcon = JEConfig.getImage("if_lock_blue_68757.png", iconSize, iconSize);
        final ImageView unlockIcon = JEConfig.getImage("if_lock-unlock_blue_68758.png", iconSize, iconSize);

        final ToggleButton unlockB = new ToggleButton("", lockIcon);
//        unlockB.setSelected(analyses.editProperty.get());
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(unlockB);
        analyses.editProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("editProperty: " + newValue);
            if (!oldValue.equals(newValue)) {
                if (newValue) {
                    System.out.println("Icon = unlockIcon");
                    unlockB.setGraphic(unlockIcon);
                } else {
                    unlockB.setGraphic(lockIcon);
                }
            }

        });

        unlockB.onActionProperty().addListener((observable, oldValue, newValue) -> {
            analyses.editProperty.setValue(!analyses.editProperty.getValue());
        });

        ImageView pauseIcon = JEConfig.getImage("pause_32.png", iconSize, iconSize);
        ImageView playIcon = JEConfig.getImage("play_32.png", iconSize, iconSize);

        ToggleButton runUpdateButton = new ToggleButton("", playIcon);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(runUpdateButton);

        analyses.updateIsRunningProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                runUpdateButton.setGraphic(pauseIcon);
            } else {
                runUpdateButton.setGraphic(playIcon);
            }
        });

        runUpdateButton.setOnAction(event -> {
            analyses.updateIsRunningProperty.setValue(!analyses.updateIsRunningProperty.getValue());
        });

        ToggleButton backgroundButton = new ToggleButton("", JEConfig.getImage("if_32_171485.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(backgroundButton);

        ToggleButton newAnalyses = new ToggleButton("", JEConfig.getImage("1390343812_folder-open.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newAnalyses);

        newAnalyses.setOnAction(event -> {
            Wizard wizzard = new Wizard(JEConfig.getDataSource());
            Optional<Widget> newWidget = wizzard.show(null);

            if (newWidget.isPresent()) {
                dashBordPlugIn.addWidget(newWidget.get());
            }

        });

        backgroundButton.setOnAction(event -> {


            FileChooser fileChooser = new FileChooser();
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Pictures", "*.png", "*.gif", "*.jpg", "*.bmp"));
            File newBackground = fileChooser.showOpenDialog(JEConfig.getStage());
            if (newBackground != null) {
                try {
                    System.out.println("New image File: " + newBackground);
                    BufferedImage bufferedImage = ImageIO.read(newBackground);
                    System.out.println("Image size: " + bufferedImage.getWidth());
                    javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    System.out.println("Fx-Image size: " + fxImage.getWidth());
                    analyses.imageBoardBackground.setValue(fxImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });

        settingsButton.setOnAction(event -> {
            analyses.openConfig();
        });

        unlockB.setOnAction(event -> {
            analyses.editProperty.setValue(!analyses.editProperty.getValue());
        });

        zoomIn.setOnAction(event -> {
            analyses.zoomIn();
        });

        zoomOut.setOnAction(event -> {
            analyses.zoomOut();
        });

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        getItems().clear();
        getItems().addAll(
                analysisLabel, listAnalysesComboBox, newAnalyses, settingsButton, unlockB, backgroundButton, sep1,
                zoomOut, zoomIn, sep2,
                runUpdateButton);


    }


}
