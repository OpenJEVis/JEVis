/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object.extension;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.application.application.I18nWS;
import org.jevis.application.dialog.ExceptionDialog;
import org.jevis.application.dialog.SelectTargetDialog2;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.type.GUIConstants;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.attribute.*;
import org.jevis.jeconfig.sample.SampleEditor;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.jeconfig.JEConfig.PROGRAMM_INFO;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GenericAttributeExtension implements ObjectEditorExtension {

    private static final String TITEL = I18n.getInstance().getString("plugin.object.attribute.title");
    private final BorderPane _view = new BorderPane();
    private JEVisObject _obj;
    private boolean _needSave = false;
    private List<AttributeEditor> _attributesEditor;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(GenericAttributeExtension.class);
    private JEVisTree tree;
    public static DoubleProperty editorWhith = new SimpleDoubleProperty(350);

    public GenericAttributeExtension(JEVisObject obj, JEVisTree tree) {
        this.tree = tree;
        _obj = obj;
        _attributesEditor = new ArrayList<>();
        _view.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
    }

    @Override
    public boolean isForObject(JEVisObject obj) {

        //its for all in the memoment
        //TODO: handel the case that we have an spezial representation an dont whant the generic
        return true;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public void setVisible() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildGui(_obj);
            }
        });
    }

    @Override
    public String getTitel() {
        return TITEL;
    }

    @Override
    public boolean needSave() {
        return _changed.getValue();
//        return true;//TODO: implement
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
        //TODO delete changes
    }

    @Override
    public boolean save() {
        logger.debug("Extensions: {}", _attributesEditor.size());
        for (AttributeEditor editor : _attributesEditor) {
            try {
                logger.debug("{}.save(): {}", this.getTitel(), editor.getAttribute().getName());
                editor.commit();

            } catch (Exception ex) {
                logger.catching(ex);
                ExceptionDialog dia = new ExceptionDialog();
                ExceptionDialog.Response re = dia.show(JEConfig.getStage(),
                        I18n.getInstance().getString("dialog.error.title"),
                        I18n.getInstance().getString("dialog.error.servercommit"), ex, PROGRAMM_INFO);
                if (re == ExceptionDialog.Response.CANCEL) {
                    return false;
                }

            }

        }
        _changed.setValue(false);
        _needSave = false;
        return true;
    }

    private JEVisAttribute getAttribute(String name, List<JEVisAttribute> atts) {
        for (JEVisAttribute att : atts) {
            if (att.getName().equals(name)) {
                return att;
            }
        }
        return null;
    }

    private void buildGui(JEVisObject obj) {
        logger.trace("load: {}", obj.getID());
        _needSave = false;

        boolean readOnly = true;
        try {
            readOnly = !obj.getDataSource().getCurrentUser().canWrite(obj.getID());
        } catch (Exception ex) {
            readOnly = true;
            logger.catching(ex);
        }

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(10);//7
        gridPane.setVgap(5);

        try {
            int coloum = 0;


            if (!obj.getJEVisClass().getTypes().isEmpty()) {
                List<JEVisAttribute> attributes = obj.getAttributes();//load once because this function is not cached
                for (JEVisType type : obj.getJEVisClass().getTypes()) {//loop types not attributes to be sure only no deletet type are shown
                    JEVisAttribute att = getAttribute(type.getName(), attributes);
                    if (att == null) {
                        continue;
                    }
                    AttributeEditor editor = new ErrorEditor();

                    try {
                        String guiDisplayType = type.getGUIDisplayType();

                        //hier ClassTable.getObjectClass
                        switch (type.getPrimitiveType()) {
                            case JEVisConstants.PrimitiveType.STRING:

                                try {

                                    if (guiDisplayType == null) {
                                        editor = new BasicEditorNew(att, BasicEditorNew.TYPE.STRING, false, true, false);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BASIC_TEXT.getId())) {
                                        editor = new BasicEditorNew(att, BasicEditorNew.TYPE.STRING, false, true, false);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BASIC_TEXT_MULTI.getId())) {
                                        editor = new StringMultyLine(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                                        editor = new DateEditor(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.TARGET_OBJECT.getId())) {
                                        editor = new TargetEditor(att, SelectTargetDialog2.MODE.OBJECT, tree);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.TARGET_ATTRIBUTE.getId())) {
                                        editor = new TargetEditor(att, SelectTargetDialog2.MODE.ATTRIBUTE, tree);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BASIC_PASSWORD.getId())) {
                                        editor = new ReadablePasswordEditor(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.DATE_TIME.getId())) {
                                        editor = new DateTimeEditor2(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.SCHEDULE.getId())) {
                                        editor = new ScheduleEditor(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.TIME_ZONE.getId())) {
                                        editor = new TimeZoneEditor(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BASIC_ENUM.getId())) {
                                        editor = new EnumEditor(att);
                                    } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.LOCALE.getId())) {
                                        editor = new LanguageEditor(att);
                                    }

//                                else if (guiDisplayType.equalsIgnoreCase(GUIConstants.TIME_ZONE.getId())) {
//                                    editor = new TimeZoneEditor(att);
//                                }
                                } catch (Exception e) {
                                    logger.error("Error with GUI Type: {} {} {}", type.getName(), type.getPrimitiveType(), type.getGUIDisplayType());
                                    editor = new StringValueEditor(att);
                                }

                                break;
                            case JEVisConstants.PrimitiveType.BOOLEAN:
                                if (guiDisplayType == null) {
                                    editor = new BooleanValueEditor(att);
                                } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BASIC_BOOLEAN.getId())) {
                                    editor = new BooleanValueEditor(att);
                                } else if (guiDisplayType.equalsIgnoreCase(GUIConstants.BOOLEAN_BUTTON.getId())) {
                                    editor = new BooleanButton(att);
                                }
                                break;
                            case JEVisConstants.PrimitiveType.FILE:
                                if (guiDisplayType == null) {
                                    editor = new FileEdior(att);
                                } else if (guiDisplayType.equals(GUIConstants.BASIC_FILER.getId())) {
                                    editor = new FileEdior(att);
                                } else {
                                    editor = new StringValueEditor(att);
                                }
                                break;
                            case JEVisConstants.PrimitiveType.DOUBLE:

                                if (guiDisplayType == null) {
                                    editor = new FileEdior(att);
                                } else {
                                    editor = new BasicEditorNew(att, BasicEditorNew.TYPE.DOUBLE, true, true, true);
                                }
                                break;
                            case JEVisConstants.PrimitiveType.PASSWORD_PBKDF2:
                                if (guiDisplayType == null) {
                                    editor = new FileEdior(att);
                                } else {
                                    editor = new PasswordEditor(att);
                                }
                                break;
                            case JEVisConstants.PrimitiveType.LONG:

                                try {
                                    editor = new BasicEditorNew(att, BasicEditorNew.TYPE.LONG, true, true, true);
                                } catch (Exception e) {
                                    logger.catching(e);
                                    editor = new NumberWithUnit(att, false);
//                            editor = new NumberWithUnit(att);
                                }
                                break;

                            default:
                                editor = new StringValueEditor(att);
                                break;

                        }

                        editor.setReadOnly(readOnly);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.catching(ex);
                    }

                    //Label name = new Label(I18n.getInstance().getString("plugin.object.attribute.missingname"));
                    //Label name = new Label(I18n.getInstance().getString(I18nWS.TYPE_NAME,type.getJEVisClassName()+type.getName()));
                    Label name = new Label(I18nWS.getInstance().getAttributeName(att));
                    Tooltip tt = new Tooltip(I18nWS.getInstance().getAttributeDescription(att));
                    if (!tt.getText().isEmpty()) {
                        name.setTooltip(tt);
                    }


//                    name.setId("attributelabel");
                    SampleEditor se = new SampleEditor();

                    AttributeAdvSettingDialogButton attSettingsButton = new AttributeAdvSettingDialogButton(att);


                    Node editNode = editor.getEditor();
                    editNode.maxWidth(editorWhith.getValue());


                    gridPane.add(name, 0, coloum);
                    gridPane.add(editNode, 2, coloum);
                    gridPane.add(attSettingsButton, 1, coloum);

//                    name.setTextFill(Color.CORNFLOWERBLUE);

                    coloum++;
                    Separator sep = new Separator(Orientation.HORIZONTAL);
                    sep.setOpacity(0.2d);
//                    sep.setStyle("-fx-border-color: blue;");
                    gridPane.add(sep, 0, coloum, 3, 1);
//                    String bgColor = "-fx-background: #EDEFF0";
//                    if (coloum % 2 == 0) {
//                        bgColor = "-fx-background-color: #EDEFF0";

//                    } else {
//                        bgColor = "-fx-background-color: #DFE3E";
//                    }
//                    editorRegion.setStyle(bgColor);
//                    attNameRegion.setStyle(bgColor);
//                    advRegion.setStyle(bgColor);

//                    JFXHamburger testB = new JFXHamburger();
//                    testB.setMaxHeight(18);
//                    testB.setMaxWidth(18);
                    name.setText(type.getName() + ":");
//                    name.setTextAlignment(TextAlignment.RIGHT);
//                    name.setContentDisplay(ContentDisplay.RIGHT);
                    name.setAlignment(Pos.CENTER_RIGHT);

                    coloum++;
                    _attributesEditor.add(editor);
                    logger.trace("done: {}", obj.getID());
                }
            }
//
        } catch (Exception ex) {
            logger.catching(ex);
        }

        AnchorPane.setTopAnchor(gridPane, 0.0);
        AnchorPane.setRightAnchor(gridPane, 0.0);
        AnchorPane.setLeftAnchor(gridPane, 0.0);
        AnchorPane.setBottomAnchor(gridPane, 0.0);

        ScrollPane scroll = new ScrollPane();
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);

        gridPane.setStyle("-fx-background-color: transparent;");

        scroll.getStylesheets().add("/styles/Styles.css");
        _view.setStyle("-fx-background-color: transparent;");

        _view.setCenter(scroll);

    }
}
