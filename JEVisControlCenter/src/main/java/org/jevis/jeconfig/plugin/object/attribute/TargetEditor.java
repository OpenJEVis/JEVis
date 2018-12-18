/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.application.dialog.SelectTargetDialog;
import org.jevis.application.dialog.SelectTargetDialog.MODE;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.TreeHelper;
import org.jevis.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fs
 */
public class TargetEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(TargetEditor.class);
    private final MODE mode;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    HBox box = new HBox();
    private boolean _hasChanged = false;
    private Button _treeButton;
    private boolean _readOnly = true;
    private JEVisSample newSample;
    private JEVisTree tree;

    public TargetEditor(JEVisAttribute att, MODE mode, JEVisTree tree) {
        logger.debug("new TagetEditor for: {}", att.getName());
        _attribute = att;
        this.mode = mode;
        this.tree = tree;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            init();
        });
    }

    @Override
    public boolean hasChanged() {
//        logger.info(_attribute.getName() + " changed: " + _hasChanged);
        return _changed.getValue();
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly = canRead;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void commit() throws JEVisException {
        if (newSample != null) {
            newSample.commit();
        }

    }

    @Override
    public Node getEditor() {
        try {
            init();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    private void init() {
        logger.debug("init TargetEditor");
        _treeButton = new Button(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        Button gotoButton = new Button(I18n.getInstance().getString("plugin.object.attribute.target.goto"),
                JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", 18, 18));//icon
        gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        box.setSpacing(10);

        box.getChildren().setAll(_treeButton, gotoButton, rightSpacer);

        _treeButton.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent t) {
                                        try {

                                            /**
                                             * TODO:
                                             * The not so pretty solution to add target specific filter
                                             */
                                            if (_attribute.getObject().getJEVisClassName().equals("Input")
                                                    || _attribute.getObject().getJEVisClassName().equals("Output")) {
                                                JEVisAttribute newTargetAttribute = TreeHelper.updateCalcTarget(_attribute.getDataSource(), _attribute);
                                                TargetHelper th = new TargetHelper(_attribute.getDataSource(), newTargetAttribute.getObject(), newTargetAttribute);
                                                if (th.isValid() && th.targetAccessable()) {
                                                    logger.info("Target Is valid");
                                                    newSample = _attribute.buildSample(new DateTime(), th.getSourceString());
                                                    newSample.commit();
                                                    setButtonText();
                                                }
                                                return;
                                            }

                                            List<JEVisTreeFilter> allFilter = new ArrayList<>();

                                            if (_attribute.getObject().getJEVisClassName().equals("")) {


                                            }

                                            allFilter.add(SelectTargetDialog.buildAllAttributesFilter());
                                            allFilter.add(SelectTargetDialog.buildAllDataFilter());


                                            JEVisAttribute newTargetAttribute = TreeHelper.updateTarget(_attribute.getDataSource(), _attribute, allFilter);
                                            TargetHelper th = new TargetHelper(_attribute.getDataSource(), newTargetAttribute.getObject(), newTargetAttribute);
                                            if (th.isValid() && th.targetAccessable()) {
                                                logger.info("Target Is valid");
                                                newSample = _attribute.buildSample(new DateTime(), th.getSourceString());
                                                newSample.commit();
                                                setButtonText();
                                            }

                                        } catch (Exception ex) {
                                            logger.catching(ex);
                                        }
                                    }
                                }
        );

        gotoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {

                    TargetHelper th;
                    if (newSample != null) {
                        th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
                    } else {
                        th = new TargetHelper(_attribute.getDataSource(), _attribute);
                    }

                    if (th.isValid() && th.targetAccessable()) {
                        JEVisObject findObj = _attribute.getDataSource().getObject(th.getObject().getID());

                        List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                        toOpen.add(findObj);
                        logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                        TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);
                    }
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }
        });

        setButtonText();

    }

    private void setButtonText() {
        TargetHelper th;
        try {
            if (newSample != null) {
                th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
            } else {
                th = new TargetHelper(_attribute.getDataSource(), _attribute);
            }

            if (th.isValid() && th.targetAccessable()) {
//                newSample = _attribute.buildSample(new DateTime(), th.getSourceString());
                String bText = "[" + th.getObject().getID() + "] " + th.getObject().getName();
                if (th.hasAttribute()) {
                    bText += " - " + th.getAttribute().getName();
                }
                _treeButton.setText(bText);
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
