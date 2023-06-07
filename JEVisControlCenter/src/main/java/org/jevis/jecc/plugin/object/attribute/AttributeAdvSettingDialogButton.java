/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.plugin.object.attribute;

import io.github.palexdev.materialfx.controls.MFXButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.sample.SampleEditor;

/**
 * @author fs
 */
public class AttributeAdvSettingDialogButton extends MFXButton {

    private static final Logger logger = LogManager.getLogger(AttributeAdvSettingDialogButton.class);
    private final int height = 17;

    public AttributeAdvSettingDialogButton(JEVisAttribute attribute) {
        try {
//            setGraphic(JEConfig.getImage("1394566386_Graph.png", height, height));
//            setGraphic(JEConfig.getImage("if_table_gear_64761.png", height, height));
            setGraphic(ControlCenter.getImage("if_settings_115801.png", height, height));
//            setText("...");
            setStyle("-fx-padding: 0 2 0 2;-fx-background-insets: 0;-fx-background-radius: 0;-fx-background-color: transparent;");

            setMaxHeight(height);
            setMaxWidth(height);
            setPrefHeight(height);

            setOnAction(t -> {
                SampleEditor se = new SampleEditor();
                se.show(ControlCenter.getStage(), attribute);

            });
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

}
