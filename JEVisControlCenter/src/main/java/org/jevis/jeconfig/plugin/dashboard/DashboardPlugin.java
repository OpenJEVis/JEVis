package org.jevis.jeconfig.plugin.dashboard;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.application.dialog.SelectTargetDialog2;
import org.jevis.application.jevistree.UserSelection;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.tool.I18n;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author br
 */
public class DashboardPlugin implements Plugin {

    private final StringProperty name = new SimpleStringProperty("Schematics");
    private final StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private final Pane root;
    private double orgSceneX, orgSceneY;
    private ObservableList<DataObject> objects = FXCollections.observableArrayList();
    private double orgTranslateX, orgTranslateY;
    private ToolBar toolBar;
    SelectionModel selectionModel = new SelectionModel();
    //private final RubberBandSelection rubberBandSelection;

    FileChooser fileChooser = new FileChooser();

    private Rectangle rect;

    private Image picture;
    private ImageView imageView;
    private BooleanProperty lockProperty = new SimpleBooleanProperty(true);
    private ObjectProperty<List<UserSelection>> userSelection = new SimpleObjectProperty(new ArrayList<>());

    @Override
    public String getClassName() {
        return "Dashboard Plugin";
    }

    public DashboardPlugin(JEVisDataSource ds) {
        root = new Pane();
//        root.getStylesheets().add("/styles/Dashboard.css");
        this.ds = ds;

        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new ExtensionFilter("All", "*.*"));
        fileChooser.setTitle(I18n.getInstance().getString("plugin.dashboard.openfile"));

//        picture = new Image(location);
        picture = JEConfig.getImage("scanda2.jpg");
        imageView = new ImageView(picture);
        imageView.fitHeightProperty().bind(root.heightProperty());
        imageView.fitWidthProperty().bind(root.widthProperty());
        root.getChildren().add(imageView);

        rect = new Rectangle();
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

        objects.addListener((ListChangeListener.Change<? extends DataObject> change) -> {
            System.out.println("Detected a change! ");
            while (change.next()) {
                System.out.println("Was added? " + change.wasAdded());
                System.out.println("Was removed? " + change.wasRemoved());
            }
        });

        //MouseControlUtil.addSelectionRectangleGesture(root, rect);
        //rubberBandSelection = new RubberBandSelection(root, selectionModel);
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

    @Override
    public Node getToolbar() {
        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");

            double iconSize = 20;
            ToggleButton newB = new ToggleButton("", JEConfig.getImage("if_textfield_add_64870.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
            //newB.setDisable(true);

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

            newP.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    File newBackground = fileChooser.showOpenDialog(JEConfig.getStage());
                    if (newBackground != null) {
                        picture = new Image(newBackground.toURI().toString());
                        imageView.setImage(picture);
                        imageView.fitHeightProperty().bind(root.heightProperty());
                        imageView.fitWidthProperty().bind(root.widthProperty());
                        imageView.autosize();
                    }
//                    picture = new Image(fileChooser.showOpenDialog((Stage) newB.getParent().getScene().getWindow()).toURI().toString());

                }
            });

            newB.setOnAction((ActionEvent e) -> {
                SelectTargetDialog2 selectionDialog2 = new SelectTargetDialog2();

                selectionDialog2.allowMultySelect(true);
                if (selectionDialog2.show(
                        JEConfig.getStage(),//JEConfig.getStage()
                        ds,
                        I18n.getInstance().getString("plugin.dashboard.att_select.title"),
                        userSelection.getValue(),
                        SelectTargetDialog2.MODE.ATTRIBUTE
                ) == SelectTargetDialog2.Response.OK) {
                    userSelection.setValue(selectionDialog2.getUserSelection());
                    for (UserSelection us : selectionDialog2.getUserSelection()) {
                        boolean exists = false;
                        for (DataObject data : objects) {
                            if (data.getAttribute().getName().equals(us.getSelectedAttribute().getName())
                                    && data.getAttribute().getObjectID().equals(us.getSelectedAttribute().getObjectID())) {
                                exists = true;
                            }
                        }
                        if (!exists) {
                            try {
                                objects.add(new DataObject(us.getSelectedAttribute()));
                                buildNode(root, objects.get(objects.size() - 1));
                            } catch (JEVisException ex) {
                                Logger.getLogger(DashboardPlugin.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    }
                }

                ////old
//                if (selectionDialog == null) {
//                    selectionDialog = new SelectTargetDialog();
//                }
//                if (selectionDialog.show(JEConfig.getStage(), ds) == SelectTargetDialog.Response.OK) {
//                    for (int i = 0; i < selectionDialog.getUserSelection().size(); i++) {
//                        try {
//                            objects.add(new DataObject(selectionDialog.getUserSelection().get(i).getSelectedAttribute()));
//                            buildNode(root, objects.get(objects.size() - 1));
//                        } catch (JEVisException ex) {
//                            Logger.getLogger(DashboardPlugin.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//
//                }
            });

            lockProperty.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    System.out.println("locked: " + newValue);
                    switchMoveable(root, newValue);
                    if (newValue) {
                        unlockB.setGraphic(unlockIcon);
                    } else {
                        unlockB.setGraphic(lockIcon);
                    }

                }
            });

            toolBar.getItems().addAll(save, newB, delete, sep1, unlockB, newP);
            //toolBar.setDisable(true);
        }
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

    private void buildNode(Pane parent, DataObject object) throws JEVisException {
        MovableLabel label;

        if (object.getValue().getUnit() == null || object.getValue().getUnit().getLabel() == null) {
            label = new MovableLabel(object.getValue().getValueAsString(), object);
        } else {
            label = new MovableLabel(object.getValue().getValueAsString() + object.getValue().getUnit().getLabel(), object);

        }
        label.moveable(lockProperty.getValue());
        label.setStyle("-fx-background-color: white;-fx-border-style: solid");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                parent.getChildren().addAll(label);
            }
        });

    }

    private void deleteObjects(DataObject object) {
        objects.remove(object);
    }

    private void switchMoveable(Pane parent) {
        for (Node children : parent.getChildren()) {
            if (children instanceof MovableLabel) {
                MovableLabel label = (MovableLabel) children;
                label.moveable(!label.isMoveable());
            }
        }
    }

    private void switchMoveable(Pane parent, boolean isMoveable) {
        for (Node children : parent.getChildren()) {
            if (children instanceof MovableLabel) {
                MovableLabel label = (MovableLabel) children;
                label.moveable(isMoveable);
            }
        }
    }
}
