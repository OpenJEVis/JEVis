package org.jevis.jecc.plugin.scada;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonTools;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.Plugin;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.plugin.scada.data.ScadaAnalysisData;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author
 */
public class SCADAPlugin implements Plugin {

    public final static String CLASS_SCADA_ANALYSIS = "SCADA Analysis";
    public final static String ATTRIBUTE_DATA_MODEL = "Data Model";
    public final static String ATTRIBUTE_BACKGROUND = "Background";
    private static final Logger logger = LogManager.getLogger(SCADAPlugin.class);
    private final DateTimeFormatter dfp = DateTimeFormat.forPattern("HH:mm:ss dd.MM.YYYY");
    private final BorderPane root = new BorderPane();
    private final FileChooser fileChooser = new FileChooser();
    private final StackPane viewArea = new StackPane();
    private final BooleanProperty lockProperty = new SimpleBooleanProperty(true);
    private final ObjectProperty<List<UserSelection>> userSelection = new SimpleObjectProperty(new ArrayList<>());
    private final Timer timer = new Timer();
    private JEVisDataSource ds;
    private SCADAAnalysis activeAnalyse = null;


    public SCADAPlugin(JEVisDataSource ds) {
        this.ds = ds;
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new ExtensionFilter("All", "*.*"));
        fileChooser.setTitle(I18n.getInstance().getString("plugin.dashboard.openfile"));
        root.setCenter(viewArea);
        this.activeAnalyse = new SCADAAnalysis(null);

        viewArea.setOnDragOver(event -> {
            logger.info("Drag over view");
            event.acceptTransferModes(TransferMode.ANY);
        });
        viewArea.setOnDragDropped(event -> {
            logger.info("Drag drop over view");
        });

    }

    @Override
    public StringProperty uuidProperty() {
        return new SimpleStringProperty("");
    }

    @Override
    public Node getMenu() {
        return new Region();
    }


    private void loadAnalysisPane(SCADAAnalysis analysis) {
        logger.info("==Load Analysis==");
        this.activeAnalyse = analysis;
        viewArea.getChildren().clear();

        Image orgImage = SwingFXUtils.toFXImage(analysis.getImage(), null);
        ImageView imageView = new ImageView(orgImage);
        imageView.setSmooth(true);

        switch (analysis.getBackgroundMode()) {
            case ABSOLUTE:
                break;
            case STRETCH_BOTH:
                imageView.setPreserveRatio(false);
                imageView.fitHeightProperty().bind(viewArea.heightProperty());
                imageView.fitWidthProperty().bind(viewArea.widthProperty());
                break;
            case STRETCH_WIDTH:
                imageView.setPreserveRatio(true);
                imageView.fitWidthProperty().bind(viewArea.widthProperty());
                break;
            case STRETCH_HEIGHT:
                imageView.setPreserveRatio(true);
                imageView.fitHeightProperty().bind(viewArea.heightProperty());
                break;
            default:
                break;

        }


        Pane absoluteLayout = new Pane();
        ChangeListener<Number> sizeUpdate = (observable, oldValue, newValue) -> analysis.getElements().forEach(element -> {
//            logger.info("calculate element pos: " + element);

            double newX = (element.xPositionProperty().getValue() * viewArea.getWidth()) / 100;
            double newY = (element.yPositionProperty().getValue() * viewArea.getHeight()) / 100;
            element.getGraphic().relocate(newX, newY);
//            logger.info("new Rel pos: x: " + newX + " y: " + newY);
        });

        //debug
//        absoluteLayout.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        absoluteLayout.prefHeightProperty().bind(viewArea.prefHeightProperty());
        absoluteLayout.prefWidthProperty().bind(viewArea.prefWidthProperty());


        viewArea.heightProperty().addListener((observable, oldValue, newValue) -> {
            sizeUpdate.changed(null, null, null);
        });
        viewArea.widthProperty().addListener((observable, oldValue, newValue) -> {
            sizeUpdate.changed(null, null, null);
        });


        viewArea.getChildren().add(imageView);
        viewArea.getChildren().add(absoluteLayout);
        StackPane.setAlignment(imageView, Pos.TOP_LEFT);
        StackPane.setAlignment(absoluteLayout, Pos.TOP_LEFT);

        userSelection.getValue().clear();
        logger.info("Load Elements");
        analysis.getElements().forEach(element -> {
            logger.info("add Element tp pane: " + element);
            element.setParent(viewArea);
            Node elementGraphic = element.getGraphic();

            absoluteLayout.getChildren().add(elementGraphic);

            elementGraphic.layoutXProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    element.xPositionProperty().setValue(element.getRelativeXPos());
                }
            });
            elementGraphic.layoutYProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    element.yPositionProperty().setValue(element.getRelativeYPos());
                }
            });
            userSelection.getValue().add(element.getUserSeclection());
        });

        viewArea.widthProperty().addListener(sizeUpdate);
        viewArea.heightProperty().addListener(sizeUpdate);
        sizeUpdate.changed(null, null, null);

        Label lastUpdateLabel = new Label();
        absoluteLayout.getChildren().add(lastUpdateLabel);
        lastUpdateLabel.relocate(viewArea.widthProperty().doubleValue() - 200, viewArea.heightProperty().doubleValue() - 20);

        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("--- Update--- ");
                Platform.runLater(() -> {
                    lastUpdateLabel.setText("Update: " + (dfp.print(new DateTime())));
                    updateUI(analysis);
                });

            }
        };
        timer.schedule(updateTask, 1000, 60000 * 1);//ever 5 min

    }

    private void updateUI(SCADAAnalysis analysis) {
        analysis.getElements().forEach(element -> {
            element.update();
        });
    }

    private void reloadActiveAnalyse() {
        if (this.activeAnalyse != null) {
            loadAnalysisPane(this.activeAnalyse);
        }
    }

    @Override
    public String getToolTip() {
        return "";
    }

    private void updateToolbar(ToolBar toolBar, final SCADAAnalysis analyses) {
        logger.info("==Update Toolbar==");
        Label analysisLabel = new Label(I18n.getInstance().getString("plugin.scada.analysis"));
        MFXComboBox<JEVisObject> listAnalysesComboBox = new MFXComboBox<>();
        listAnalysesComboBox.setFloatMode(FloatMode.DISABLED);
        listAnalysesComboBox.setPrefWidth(300);

        try {
            JEVisClass sadaAnalyses = ds.getJEVisClass(CLASS_SCADA_ANALYSIS);
            listAnalysesComboBox.getItems().addAll(ds.getObjects(sadaAnalyses, true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //TODO JFX17
        listAnalysesComboBox.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                return object.getName();
            }

            @Override
            public JEVisObject fromString(String string) {
                return listAnalysesComboBox.getItems().get(listAnalysesComboBox.getSelectedIndex());
            }
        });

        MFXComboBox<SCADAAnalysis.BGMode> listBGType = new MFXComboBox<>();
        listBGType.setFloatMode(FloatMode.DISABLED);
        listBGType.setItems(FXCollections.observableArrayList(SCADAAnalysis.BGMode.values()));

        //TODO JFX17
        listBGType.setConverter(new StringConverter<SCADAAnalysis.BGMode>() {
            @Override
            public String toString(SCADAAnalysis.BGMode object) {
                String localname = "";
                if (object != null) {
                    switch (object) {
                        case STRETCH_HEIGHT:
                            localname = I18n.getInstance().getString("plugin.scada.background.stretch_height");
                            break;
                        case STRETCH_WIDTH:
                            localname = I18n.getInstance().getString("plugin.scada.background.stretch_width");
                            break;
                        case STRETCH_BOTH:
                            localname = I18n.getInstance().getString("plugin.scada.background.stretch_both");
                            break;
                        case ABSOLUTE:
                            localname = I18n.getInstance().getString("plugin.scada.background.stretch_none");
                            break;
                    }
                }

                return localname;
            }

            @Override
            public SCADAAnalysis.BGMode fromString(String string) {
                return listBGType.getItems().get(listBGType.getSelectedIndex());
            }
        });

        double iconSize = 20;

//        HBox updateBox = new HBox();
//        ToggleButton start = new ToggleButton("", JEConfig.getImage("folders_explorer.png", iconSize, iconSize));
//        GlobalToolBar.changeBackgroundOnHoverUsingBinding(start);
//
//        ToggleButton pause = new ToggleButton("", JEConfig.getImage("folders_explorer.png", iconSize, iconSize));
//        GlobalToolBar.changeBackgroundOnHoverUsingBinding(pause);

        ToggleButton treeButton = new ToggleButton("", ControlCenter.getImage("Data.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(treeButton);

        ToggleButton save = new ToggleButton("", ControlCenter.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);


        ToggleButton delete = new ToggleButton("", ControlCenter.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);


        final ImageView lockIcon = ControlCenter.getImage("if_lock_blue_68757.png", iconSize, iconSize);
        final ImageView unlockIcon = ControlCenter.getImage("if_lock-unlock_blue_68758.png", iconSize, iconSize);
        final ToggleButton unlockB = new ToggleButton("", unlockIcon);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(unlockB);
        lockProperty.bindBidirectional(unlockB.selectedProperty());
//        lockProperty.setValue(Boolean.TRUE);

        ToggleButton backgroundButton = new ToggleButton("", ControlCenter.getImage("if_32_171485.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(backgroundButton);

        ToggleButton newAnalyses = new ToggleButton("", ControlCenter.getImage("1390343812_folder-open.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newAnalyses);


        /**
         * set control element state
         * ---------------------------------------------------------------------------------------------------
         */
        if (analyses != null) {
            listAnalysesComboBox.selectItem(analyses.getObject());
            listBGType.selectItem(analyses.getBackgroundMode());
        } else {
            listBGType.getSelectionModel().selectFirst();
        }


        /**
         * Change listeners - have to be after element state or we get loops
         * ---------------------------------------------------------------------------------------------------
         */

        listBGType.valueProperty().addListener((observable, oldValue, newValue) -> {
            analyses.setBackgroundMode(newValue);
            reloadActiveAnalyse();
        });

        if (analyses != null) {
            analyses.getElements().forEach(element -> {
                element.movableProperty().bind(lockProperty.not());
            });
        }


        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == null) || (Objects.nonNull(newValue))) {
                try {
                    SCADAAnalysis analysis = new SCADAAnalysis(newValue);
                    analysis.load();
                    loadAnalysisPane(analysis);
                    updateToolbar(toolBar, analysis);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        lockProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if (newValue) {
                    unlockB.setGraphic(lockIcon);
                } else {
                    unlockB.setGraphic(unlockIcon);
                }

            }
        });

        treeButton.setOnAction((ActionEvent e) -> {
            SCADASelectionDialog selectionDialog = new SCADASelectionDialog(ds,
                    I18n.getInstance().getString("plugin.dashboard.att_select.title"),
                    userSelection.getValue(),
                    SCADASelectionDialog.MODE.ATTRIBUTE
            );

            selectionDialog.allowMultySelect(true);

            selectionDialog.setOnCloseRequest(event -> {
                if (selectionDialog.getResponse() == SCADASelectionDialog.Response.OK) {
                    analyses.getElements().clear();
                    userSelection.setValue(selectionDialog.getUserSelection());
                    for (UserSelection us : selectionDialog.getUserSelection()) {
                        /**
                         * TODO: add type based user selection
                         */
                        SCADAElement newEle = new LabelElement(analyses);
                        newEle.setAttribute(us.getSelectedAttribute());
                        newEle.titleProperty().setValue(us.getSelectedAttribute().getName() + ":");
                        newEle.yPositionProperty().setValue(ThreadLocalRandom.current().nextInt(10, 50));
                        newEle.xPositionProperty().setValue(ThreadLocalRandom.current().nextInt(10, 50));

                        analyses.getElements().add(newEle);

                    }
                    loadAnalysisPane(analyses);
                }
            });
            selectionDialog.show();
        });

        delete.setOnAction(event -> {
            try {
                JEVisObject obj = analyses.getObject();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(I18n.getInstance().getString("plugin.dashboard.delete.title"));
                String s = String.format(I18n.getInstance().getString("plugin.dashboard.delete.question"), obj.getName());
                alert.setContentText(s);

                Optional<ButtonType> result = alert.showAndWait();

                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                    obj.delete();
                    viewArea.getChildren().clear();
                    updateToolbar(toolBar, null);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        backgroundButton.setOnAction(event -> {
            File newBackground = fileChooser.showOpenDialog(ControlCenter.getStage());
            if (newBackground != null) {
                try {
                    BufferedImage bufferedImage = ImageIO.read(newBackground);
                    analyses.setImage(bufferedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                loadAnalysisPane(analyses);

            }

        });


        newAnalyses.setOnAction(event -> {
            try {
                SCADAAnalysis analysis = new SCADAAnalysis(null);
                analysis.load();
                loadAnalysisPane(analysis);
                updateToolbar(toolBar, analysis);
            } catch (JEVisException e) {
                e.printStackTrace();
            }

        });

        save.setOnAction(event -> {
            save(analyses);
        });


        backgroundButton.disableProperty().bind(lockProperty);
        treeButton.disableProperty().bind(lockProperty);
        backgroundButton.disableProperty().bind(lockProperty);
        listBGType.disableProperty().bind(lockProperty);
        save.disableProperty().bind(lockProperty);

        if (analyses != null && analyses.getObject() == null) {
            delete.disableProperty().setValue(true);
            lockProperty.setValue(false);
        } else {
            logger.info("is loaded analyses -> lock");
            lockProperty.setValue(true);
//            unlockB.setSelected(false);
        }

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        toolBar.getItems().clear();
        toolBar.getItems().addAll(analysisLabel, listAnalysesComboBox, newAnalyses, save, delete, unlockB, sep1,
                treeButton, sep2,
                backgroundButton, listBGType
        );


    }

    @Override
    public Node getToolbar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");
        updateToolbar(toolBar, this.activeAnalyse);


        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }


    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds
    ) {
        this.ds = ds;
    }

    private void save(SCADAAnalysis analyses) {
        logger.info("=SAVE==");

        /** New analyses **/
        if (analyses.getObject() == null) {
            NewAnalysisDialog saveDialog = new NewAnalysisDialog();

            try {
                NewAnalysisDialog.Response re = saveDialog.show(null, ds);
                if (re == NewAnalysisDialog.Response.YES) {
                    JEVisClass aClass = ds.getJEVisClass(CLASS_SCADA_ANALYSIS);
                    JEVisObject newObject = saveDialog.getParent().buildObject(saveDialog.getCreateName(), aClass);
                    newObject.commit();
                    analyses.setObject(newObject);

                }

            } catch (JEVisException e) {
                e.printStackTrace();
            }

        }

        ScadaAnalysisData aData = new ScadaAnalysisData();
//        aData.setAuthor("Florian Simon");
        aData.setBgMode(SCADAAnalysis.BGMode.STRETCH_BOTH.toString());
        aData.setElements(new ArrayList<>());

        analyses.getElements().forEach(e -> {
            logger.info(" Add element: " + e.titleProperty().getValue());
            aData.getElements().add(e.getData());
        });


        String json = null;
        try {
            json = JsonTools.prettyObjectMapper().writeValueAsString(aData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.info("json: \n" + json);

        try {
            JEVisSample sample = analyses.getObject().getAttribute(SCADAPlugin.ATTRIBUTE_DATA_MODEL).buildSample(new DateTime(), json);
            sample.commit();

            if (analyses.bgHasChanged()) {
                JEVisAttribute bgAttribute = analyses.getObject().getAttribute(SCADAPlugin.ATTRIBUTE_BACKGROUND);
                JEVisFile jfile = new JEVisFileImp();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(analyses.getImage(), "png", bos);
                byte[] data = bos.toByteArray();
                jfile.setBytes(data);
                jfile.setFilename("bg.png");

                JEVisSample newSample = bgAttribute.buildSample(new DateTime(), jfile);
                newSample.commit();
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getClassName() {
        return "Dashboard Plugin";
    }//TODO rename

    @Override
    public boolean supportsRequest(int cmdType) {
        return false;
    }

    @Override
    public void setHasFocus() {
    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 1000;
    }

    @Override
    public String getName() {
        return I18n.getInstance().getString("plugin.scada.title");
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public StringProperty nameProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUUID() {
        return "";
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public void handleRequest(int cmdType) {
    }

    @Override
    public Node getContentNode() {
        return root;
    }

    @Override
    public Region getIcon() {
        return ControlCenter.getSVGImage(Icon.DASHBOARD, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {
    }

}