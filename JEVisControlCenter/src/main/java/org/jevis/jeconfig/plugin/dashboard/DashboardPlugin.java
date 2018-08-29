package org.jevis.jeconfig.plugin.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import org.jevis.api.*;
import org.jevis.application.jevistree.UserSelection;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaAnalysisData;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaElementData;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author
 */
public class DashboardPlugin implements Plugin {

    private final BorderPane root = new BorderPane();
    private FileChooser fileChooser = new FileChooser();
    private JEVisDataSource ds;
    private Rectangle rect;
    private StackPane viewArea = new StackPane();
    private BooleanProperty lockProperty = new SimpleBooleanProperty(true);
    private ObjectProperty<List<UserSelection>> userSelection = new SimpleObjectProperty(new ArrayList<>());
    private SCADAAnalysis activeAnalyse = null;
    private ObjectProperty<Node> movingNodeProperty = new SimpleObjectProperty<>(null);

    public DashboardPlugin(JEVisDataSource ds) {
        this.ds = ds;
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new ExtensionFilter("All", "*.*"));
        fileChooser.setTitle(I18n.getInstance().getString("plugin.dashboard.openfile"));

        root.setCenter(viewArea);

        rect = new Rectangle();
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));


    }

    @Override
    public String getClassName() {
        return "Dashboard Plugin";
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        return false;
    }

    @Override
    public void setHasFocus() {
    }

    @Override
    public String getName() {
        return I18n.getInstance().getString("plugin.dashboard.title");
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
    public StringProperty uuidProperty() {
        return new SimpleStringProperty("");
    }

    @Override
    public Node getMenu() {
        return new Region();
    }

    private void loadAnalysis(SCADAAnalysis analysis) {
        this.activeAnalyse = analysis;
        viewArea.getChildren().clear();


        Image orgImage = analysis.getImage();
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

            System.out.println("update Element: " + element.titleProperty());
            Node elementGraphic = element.getGraphic();

            // newPos = (bgSize/orgSize)*elementPos

            System.out.println("element Org pos: " + element.xPositionProperty().getValue() + " " + element.yPositionProperty().getValue());
            System.out.println("picture Org size: " + orgImage.getWidth() + " " + element.yPositionProperty().getValue());
            System.out.println("viewArea size: " + viewArea.getWidth() + " " + orgImage.getHeight());

            double newX = (viewArea.getWidth() / orgImage.getWidth()) * element.xPositionProperty().getValue();
            double newY = (viewArea.getHeight() / orgImage.getHeight() * element.yPositionProperty().getValue());

            System.out.println("Mode: " + analysis.getBackgroundMode());
            switch (analysis.getBackgroundMode()) {
                case ABSOLUTE:
                    elementGraphic.relocate(element.xPositionProperty().getValue(), element.yPositionProperty().getValue());
                    break;
                case STRETCH_BOTH:
                    elementGraphic.relocate(newX, newY);
                    break;
                case STRETCH_WIDTH:
                    elementGraphic.relocate(newX, element.yPositionProperty().getValue());
                    break;
                case STRETCH_HEIGHT:
                    elementGraphic.relocate(element.xPositionProperty().getValue(), newY);
                    break;
                default:
                    break;

            }
            System.out.println("== new Pos: " + elementGraphic.getLayoutX() + " " + elementGraphic.getLayoutY());

        });

        absoluteLayout.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
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

        analysis.getElements().forEach(element -> {
            System.out.println("Add Element: " + element.titleProperty().getValue());
            Node elementGraphic = element.getGraphic();
//            elementGraphic.minWidth(element.widthProperty().getValue());
//            elementGraphic.maxWidth(element.widthProperty().getValue());
//            elementGraphic.minHeight(element.heightProperty().getValue());
//            elementGraphic.maxHeight(element.heightProperty().getValue());
            absoluteLayout.getChildren().add(elementGraphic);

            elementGraphic.layoutXProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    element.xPositionProperty().setValue(newValue);
                }
            });
            elementGraphic.layoutYProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    element.yPositionProperty().setValue(newValue);
                }
            });


            //Add drag and drop
//            elementGraphic.setOnMousePressed(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    movingNodeProperty.setValue(elementGraphic);
//                }
//            });

//            absoluteLayout.setOnMouseMoved(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    if (movingNodeProperty.getValue() != null) {
//                        Node node = movingNodeProperty.getValue();
//                        double newX = node.getLayoutX() + event.getX();
//                        double newY = node.getLayoutY() + event.getY();
//
//                        node.relocate(newX, newY);
//                    }
//                }
//            });
//            elementGraphic.setOnMouseReleased(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    if (movingNodeProperty.getValue() != null) {
//                        movingNodeProperty.setValue(elementGraphic);
//                    } else {
//                        movingNodeProperty.setValue(null);
//                    }
////                    movingNodeProperty.setValue(null);
//                }
//            });

        });

        viewArea.widthProperty().addListener(sizeUpdate);
        viewArea.heightProperty().addListener(sizeUpdate);
        sizeUpdate.changed(null, null, null);


        //TODO: what if the set size is bigger than screen
        Platform.runLater(() -> {
            updateUI(analysis);
//            viewArea.getChildren().add(stackPane);
//            System.out.println("viewArea: " + viewArea.getWidth());

        });

    }

    private void updateUI(SCADAAnalysis analysis) {
        analysis.getElements().forEach(element -> {
            element.update();
        });
    }

    private void reloadActivAnalyse() {
        if (this.activeAnalyse != null) {
            loadAnalysis(this.activeAnalyse);
        }
    }


    private void updateToolbar(ToolBar toolBar, final SCADAAnalysis analyses) {
        Label analysisLabel = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));//TODO
        ComboBox<JEVisObject> listAnalysesComboBox = new ComboBox();
        listAnalysesComboBox.setPrefWidth(300);

        try {
            JEVisClass sadaAnalyses = ds.getJEVisClass("SCADA Analysis");
            listAnalysesComboBox.getItems().addAll(ds.getObjects(sadaAnalyses, true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                final ListCell<JEVisObject> cell = new ListCell<JEVisObject>() {

//                    {
//                        super.setPrefWidth(300);
//                    }

                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                            setGraphic(null);
                            Tooltip tt = new Tooltip();
                            tt.setText("ID: " + item.getID());
                            setTooltip(tt);
                        }


                    }
                };

                return cell;
            }
        };

        listAnalysesComboBox.setCellFactory(cellFactory);
        listAnalysesComboBox.setButtonCell(cellFactory.call(null));

        ComboBox<SCADAAnalysis.BGMode> listBGType = new ComboBox();
        listBGType.setItems(FXCollections.observableArrayList(SCADAAnalysis.BGMode.values()));


        double iconSize = 20;
        ToggleButton newB = new ToggleButton("", JEConfig.getImage("if_textfield_add_64870.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);


        ToggleButton delete = new ToggleButton("", JEConfig.getImage("list-remove.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        Separator sep1 = new Separator();

        final ImageView lockIcon = JEConfig.getImage("if_lock_blue_68757.png", iconSize, iconSize);
        final ImageView unlockIcon = JEConfig.getImage("if_lock-unlock_blue_68758.png", iconSize, iconSize);
        final ToggleButton unlockB = new ToggleButton("", unlockIcon);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(unlockB);
        lockProperty.bindBidirectional(unlockB.selectedProperty());
        lockProperty.setValue(Boolean.TRUE);

        ToggleButton newP = new ToggleButton("", JEConfig.getImage("if_32_171485.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newP);


        /**
         * set control element state
         * ---------------------------------------------------------------------------------------------------
         */
        if (analyses != null) {
            listAnalysesComboBox.getSelectionModel().select(analyses.getObject());
            listBGType.getSelectionModel().select(analyses.getBackgroundMode());
        } else {
            listBGType.getSelectionModel().selectFirst();
        }


        /**
         * Change listeners - have to be after element state or we get loops
         * ---------------------------------------------------------------------------------------------------
         */

        listBGType.valueProperty().addListener((observable, oldValue, newValue) -> {
            analyses.setBackgroundMode(newValue);
            reloadActivAnalyse();
        });


        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == null) || (Objects.nonNull(newValue))) {
                try {
                    SCADAAnalysis analysis = new SCADAAnalysis(newValue);
                    analysis.load(newValue);
                    loadAnalysis(analysis);
                    updateToolbar(toolBar, analysis);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        lockProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                System.out.println("locked: " + newValue);

                if (newValue) {
                    unlockB.setGraphic(unlockIcon);
                } else {
                    unlockB.setGraphic(lockIcon);
                }

            }
        });

        newB.setOnAction((ActionEvent e) -> {
            SCADASelectionDialog selectionDialog = new SCADASelectionDialog();

            selectionDialog.allowMultySelect(true);
            if (selectionDialog.show(
                    JEConfig.getStage(),//JEConfig.getStage()
                    ds,
                    I18n.getInstance().getString("plugin.dashboard.att_select.title"),
                    userSelection.getValue(),
                    SCADASelectionDialog.MODE.ATTRIBUTE
            ) == SCADASelectionDialog.Response.OK) {
                analyses.getElements().clear();
                userSelection.setValue(selectionDialog.getUserSelection());
                System.out.println("US.count: " + selectionDialog.getUserSelection().size());
                for (UserSelection us : selectionDialog.getUserSelection()) {
                    //TODO get type
                    SCADAElement newEle = new LabelElement(analyses);
                    newEle.setAttribute(us.getSelectedAttribute());
                    newEle.titleProperty().setValue(us.getSelectedAttribute().getName() + ":");
                    newEle.yPositionProperty().setValue(ThreadLocalRandom.current().nextInt(10, 200));
                    newEle.xPositionProperty().setValue(ThreadLocalRandom.current().nextInt(10, 300));
//                    newEle.widthProperty().setValue(100);
//                    newEle.heightProperty().setValue(20);


                    analyses.getElements().add(newEle);
                    System.out.println("New add: " + newEle.titleProperty().getValue());

                }
                loadAnalysis(analyses);
//                updateToolbar(toolBar, analyses);

            }

        });


        newP.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                File newBackground = fileChooser.showOpenDialog(JEConfig.getStage());
                if (newBackground != null) {

                    //TODO: here set icon in analysis
//                    picture = new Image(newBackground.toURI().toString());
//                    imageView.setImage(picture);
//                    imageView.fitHeightProperty().bind(root.heightProperty());
//                    imageView.fitWidthProperty().bind(root.widthProperty());
//                    imageView.autosize();
                }
//                    picture = new Image(fileChooser.showOpenDialog((Stage) newB.getParent().getScene().getWindow()).toURI().toString());

            }
        });

        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                toJson(analyses);
            }
        });

        toolBar.getItems().clear();
        toolBar.getItems().addAll(analysisLabel, listAnalysesComboBox, save, newB, delete, sep1, unlockB, newP, listBGType);


    }

    @Override
    public Node getToolbar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");
        updateToolbar(toolBar, this.activeAnalyse);


        return toolBar;
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

    @Override
    public void handleRequest(int cmdType) {
    }

    @Override
    public Node getContentNode() {
        return root;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("if_dashboard_46791.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {
    }


    private void toJson(SCADAAnalysis analyses) {

        ScadaAnalysisData aData = new ScadaAnalysisData();
        aData.setAuthor("Florian Simon");
        aData.setBgMode(SCADAAnalysis.BGMode.STRETCH_BOTH.toString());
        aData.setElements(new ArrayList<>());

        ScadaElementData ele = new ScadaElementData();
        ele.setObjectID(1l);
        ele.setAttribute("Hostname");
        ele.setxPos(50);
        ele.setyPos(50);
        ele.setType("LabelElement");
        aData.getElements().add(ele);


        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(aData, ScadaAnalysisData.class);
        System.out.println("json: \n" + json);

        try {
            JEVisSample sample = analyses.getObject().getAttribute("Data Model").buildSample(new DateTime(), json);
            sample.commit();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

}
