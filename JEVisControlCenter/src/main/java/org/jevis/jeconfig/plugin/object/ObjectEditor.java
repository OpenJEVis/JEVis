/**
 * Copyright (C) 2009 - 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.HiddenSidesPane;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.application.application.I18nWS;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.extension.*;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ImageConverter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This Edior is used to configure the Attributes of an Objects. Its used in the
 * right side next to the Objects Tree.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectEditor {

    private JEVisObject _currentObject = null;
    private List<ObjectEditorExtension> extensions = new LinkedList<>();
    private boolean _hasChanged = true;
    private String _lastOpenEditor = "";
    private final Logger logger = LogManager.getLogger(ObjectEditor.class);
    private JEVisTree tree;

    private HiddenSidesPane _view;

    public ObjectEditor() {
        _view = new HiddenSidesPane();
        _view.setId("objecteditorpane");
        _view.getStylesheets().add("/styles/Styles.css");
        _view.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

    }

    public void commitAll() {
        for (ObjectEditorExtension extension : extensions) {
            System.out.println("ObjectEditor.comitall: " + extension.getClass());
            extension.save();
        }
    }

    public boolean needSave() {
        if (_currentObject != null) {
            for (ObjectEditorExtension extension : extensions) {
                if (extension.needSave()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dismissChanges() {
        if (_currentObject != null) {
            for (ObjectEditorExtension extension : extensions) {
                extension.dismissChanges();
            }
        }
    }

    //    public void checkIfSaved(JEVisObject obj) {
//        System.out.println("checkIfSaved: " + obj);
//        if (_currentObject != null && !Objects.equals(obj.getID(), _currentObject.getID())) {
//
//            List<ObjectEditorExtension> needSave = new ArrayList<>();
//
//            _hasChanged = true;
//            for (ObjectEditorExtension extension : extensions) {
//                if (extension.needSave()) {
//                    System.out.println("extension need save: " + extension.getTitle());
//                    needSave.add(extension);
//                }
//            }
//            System.out.println("needSave.size: " + needSave.size());
//
//            commitAll();
//            if (!needSave.isEmpty()) {
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        ConfirmDialog dia = new ConfirmDialog();
//                        ConfirmDialog.Response re = dia.show(JEConfig.getStage(), "Save", "Save Attribute Changes", "Changes will be lost if not saved, do you want to save now?");
//                        if (re == ConfirmDialog.Response.YES) {
//                            commitAll();
//                        } else {
//                            _hasChanged = false;
//                        }
//
//                    }
//                });
//
//            }
//
//        }
//    }
    public Node getView() {
        return _view;
    }

    public void setTree(JEVisTree tree) {
        this.tree = tree;
    }

    public void setObject(final JEVisObject obj) {
        Task<Void> load = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadObject(obj);
                return null;
            }
        };

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                JEConfig.getStage().getScene().setCursor(Cursor.WAIT);

            }
        });

        load.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        JEConfig.getStage().getScene().setCursor(Cursor.DEFAULT);

                    }
                });
            }
        });

        new Thread(load).start();

    }

    public void loadObject(final JEVisObject obj) {
        //        checkIfSaved(obj);
        _currentObject = obj;

        Platform.runLater(
                new Runnable() {

                    @Override
                    public void run() {

//                AnchorPane content = new AnchorPane();
                        Accordion accordion = new Accordion();
                        accordion.getStylesheets().add("/styles/objecteditor.css");
                        accordion.setStyle("-fx-box-border: transparent;");

                        List<TitledPane> taps = new ArrayList<>();
                        extensions = new ArrayList<>();

                        // Add optional extensions
                        try {
                            switch (obj.getJEVisClassName()) {
                                case CalculationExtension.CALC_CLASS_NAME:
                                    extensions.add(new CalculationExtension(obj));
                                    break;
                                default:
                            }
                        } catch (JEVisException e) {
                            logger.error("Could not get object class" + e.getLocalizedMessage());
                            e.printStackTrace();
                        }

                        // Add general extensions
                        extensions.add(new GenericAttributeExtension(obj, tree));
//                extensions.add(new BasicMathExtension(obj));
                        extensions.add(new MemberExtension(obj));
                        extensions.add(new PermissionExtension(obj));
                        extensions.add(new RootExtension(obj));
                        extensions.add(new LinkExtension(obj));
                        extensions.add(new ProcessChainExtension(obj));

                        for (final ObjectEditorExtension ex : extensions) {
                            if (ex.isForObject(obj)) {
                                TitledPane newTab = new TitledPane(ex.getTitle(), ex.getView());
                                newTab.getStylesheets().add("/styles/objecteditor.css");
//                        newTab.setStyle("-fx-background-color: transparent;");

                                newTab.setAnimated(false);
                                taps.add(newTab);
                                ex.getValueChangedProperty().addListener(new ChangeListener<Boolean>() {

                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                                        if (t1) {
                                            _hasChanged = t1;//TODO: enable/disbale the save button
                                        }
                                    }
                                });

                                newTab.expandedProperty().addListener(new ChangeListener<Boolean>() {

                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                                        if (t1) {
                                            try {
                                                JEConfig.loadNotification(true);
//                                        loaderP.setProgress(1);
                                                ex.setVisible();
//                                        loaderP.setContent(content);

//                                        updateView(content, ex);
//                                        loaderP.setProgress(100);
                                                _lastOpenEditor = ex.getTitle();
                                                JEConfig.loadNotification(false);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }
                                });

                            }
                        }

                        accordion.getPanes().addAll(taps);
                        AnchorPane.setTopAnchor(accordion, 0.0);
                        AnchorPane.setRightAnchor(accordion, 0.0);
                        AnchorPane.setLeftAnchor(accordion, 0.0);
                        AnchorPane.setBottomAnchor(accordion, 0.0);
//                content.getChildren().add(accordion);

                        //load the last Extensions for the new object
                        boolean foundTab = false;
                        if (!taps.isEmpty()) {

                            for (ObjectEditorExtension ex : extensions) {
                                if (ex.getTitle().equals(_lastOpenEditor)) {
                                    ex.setVisible();
//                            updateView(content, ex);
                                }
                            }
                            for (TitledPane tap : taps) {
                                if (tap.getText().equals(_lastOpenEditor)) {
                                    accordion.setExpandedPane(tap);
                                    foundTab = true;
                                }
                            }

                        }
//                FXProgressPanel loaderP = new FXProgressPanel(true, accordion);
//                _view.getChildren().setAll(loaderP);

                        if (!foundTab) {
//                    updateView(content, extensions.get(0));
                            extensions.get(0).setVisible();
                            accordion.setExpandedPane(taps.get(0));
                            taps.get(0).requestFocus();
                            _lastOpenEditor = extensions.get(0).getTitle();
                        }

                        Button helpButton = new Button("", JEConfig.getImage("1400874302_question_blue.png", 34, 34));
                        helpButton.setStyle("-fx-padding: 0 2 0 2;-fx-background-insets: 0;-fx-background-radius: 0;-fx-background-color: transparent;");

                        //Header
                        ImageView classIcon;
                        try {
                            classIcon = ImageConverter.convertToImageView(obj.getJEVisClass().getIcon(), 60, 60);
                        } catch (Exception ex) {
                            classIcon = JEConfig.getImage("1390343812_folder-open.png", 20, 20);
                        }

//                FlowPane header = new FlowPane();
                        GridPane header = new GridPane();
                        try {
//                header.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #1a719c, #f4f4f4)");
                            Label nameLabel = new Label(I18n.getInstance().getString("plugin.object.editor.name"));
                            Label objectName = new Label(obj.getName());
                            objectName.setStyle("-fx-font-weight: bold;");
                            Label classlabel = new Label(I18n.getInstance().getString("plugin.object.editor.type"));
                            //Label className = new Label(obj.getJEVisClass().getName());
                            Label className = new Label(I18nWS.getInstance().getClassName(obj.getJEVisClassName()));

                            Label idlabel = new Label(I18n.getInstance().getString("plugin.object.editor.id"));
                            Label idField = new Label(obj.getID() + "");

                            //TODO: would be nice if the user can copy the ID and name but the layout is broken if i use this textfield code
//                    idField.setEditable(false);
//                    header.setStyle("-fx-background-color: red;");
                            Region spacer = new Region();
                            GridPane.setVgrow(spacer, Priority.ALWAYS);
                            GridPane.setFillWidth(spacer, true);
                            GridPane.setHgrow(spacer, Priority.ALWAYS);

                            GridPane.setVgrow(nameLabel, Priority.NEVER);
                            GridPane.setVgrow(classlabel, Priority.NEVER);
                            GridPane.setVgrow(idlabel, Priority.NEVER);
                            GridPane.setVgrow(objectName, Priority.NEVER);
                            GridPane.setVgrow(className, Priority.NEVER);
                            GridPane.setVgrow(idField, Priority.NEVER);
                            GridPane.setVgrow(helpButton, Priority.NEVER);
                            GridPane.setHgrow(helpButton, Priority.NEVER);

                            //                    x  y  xw yh
                            header.add(classIcon, 0, 0, 1, 4);

                            header.add(nameLabel, 1, 0, 1, 1);
                            header.add(classlabel, 1, 1, 1, 1);
                            header.add(idlabel, 1, 2, 1, 1);

                            header.add(objectName, 2, 0, 1, 1);
                            header.add(className, 2, 1, 1, 1);
                            header.add(idField, 2, 2, 1, 1);

                            header.add(spacer, 3, 0, 1, 4);
                            header.add(helpButton, 4, 1, 1, 2);

                            Separator sep = new Separator(Orientation.HORIZONTAL);
                            GridPane.setVgrow(sep, Priority.ALWAYS);

//                    header.add(sep, 0, 4, 4, 1);
                            header.setPadding(new Insets(10));
                            header.setVgap(5);
                            header.setHgap(12);
                            header.setPadding(new Insets(10, 0, 20, 10));
//                    header.getChildren().setAll(classIcon, objectName);
                        } catch (Exception ex) {

                        }


                        BorderPane pane = new BorderPane();
                        pane.setTop(header);
                        pane.setCenter(accordion);
                        AnchorPane.setRightAnchor(pane, 1.0);
                        AnchorPane.setLeftAnchor(pane, 1.0);
                        AnchorPane.setTopAnchor(pane, 1.0);
                        AnchorPane.setBottomAnchor(pane, 1.0);

//                _view.getChildren().setAll(pane);
                        try {
                            //SideNode help = new SideNode(obj.getJEVisClassName(), obj.getJEVisClass().getDescription(), Side.RIGHT, _view);

                            StringBuilder sb = new StringBuilder();
                            sb.append("<htlm><body>");
                            //sb.append("<h1>");
                            //sb.append(I18nWS.getInstance().getClassName(obj.getJEVisClassName()));
                            //sb.append("</h1>");
                            sb.append("<p>");
                            sb.append(I18nWS.getInstance().getClassDescription(obj.getJEVisClassName()));
                            sb.append("</p><br>");
                            for (JEVisType type : obj.getJEVisClass().getTypes()) {
                                sb.append("<h4>");
                                sb.append(I18nWS.getInstance().getTypeName(obj.getJEVisClassName(), type.getName()));
                                sb.append("</h4>");
                                sb.append("<p>");
                                sb.append(I18nWS.getInstance().getTypeDescription(obj.getJEVisClassName(), type.getName()));
                                sb.append("</p>");
                            }


                            sb.append("</body></htlm>");

                            SideNode help = new SideNode(
                                    I18nWS.getInstance().getClassName(obj.getJEVisClassName()),
                                    sb.toString(),
                                    Side.RIGHT, _view);


                            _view.setRight(help);
                            helpButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    _view.setPinnedSide(Side.RIGHT);
                                }
                            });

                        } catch (Exception ex) {
                            logger.catching(ex);
                        }

                        _view.setContent(pane);

//                _view.getChildren().setAll(accordion);
                    }
                }
        );

    }

    class SideNode extends VBox {

        public SideNode(final String headline, final String text, final Side side,
                        final HiddenSidesPane pane) {
            super();

            Color font = Color.BLACK;
            Color background = Color.WHITE;
            String backrounfCSS = "-fx-background-color: white;";
//            super(text + " (Click to pin / unpin)");
            setPrefSize(300, 200);
            setOpacity(0.7d);
            setSpacing(10d);
            setPadding(new Insets(10));
            setStyle("-fx-background-color: white;");

            Region spacerHeader = new Region();
            spacerHeader.setMinHeight(30d);//120d);

            Label header = new Label(headline);
            header.setFont(Font.font(22));

//            Label helpText = new Label(text);
            WebView helpText = new WebView();
//            helpText.setTextFill(font);
//            helpText.setAlignment(Pos.TOP_LEFT);
//            helpText.setWrapText(true);
            helpText.getEngine().loadContent(text);

            getChildren().addAll(header, spacerHeader, helpText);
            VBox.setVgrow(header, Priority.NEVER);
            VBox.setVgrow(helpText, Priority.ALWAYS);

            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    pane.setPinnedSide(null);
//                    if (pane.getPinnedSide() != null) {
//                        setText(text + " (unpinned)");
//                        pane.setPinnedSide(null);
//                    } else {
//                        setText(text + " (pinned)");
//                        pane.setPinnedSide(side);
//                    }
                }
            });

        }
    }

}
