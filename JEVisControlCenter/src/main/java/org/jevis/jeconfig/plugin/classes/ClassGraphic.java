/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.classes;

import com.jfoenix.controls.JFXTooltip;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisEvent;
import org.jevis.api.JEVisEventListener;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassGraphic {
    private static final Logger logger = LogManager.getLogger(ClassGraphic.class);
    private final HBox _view = new HBox();
    private final HBox _edior = new HBox();
    private ImageView icon = new ImageView();
    private final Label nameLabel = new Label("*Missing*");
    private final JEVisClass _jclass;
    private final ClassContextMenu _menu;
    private final ClassTree _tree;
    private Tooltip _tip;

    public ClassGraphic(JEVisClass obj, ClassTree tree) {
        _jclass = obj;
        _tree = tree;
        _menu = new ClassContextMenu(obj, tree);
//        _menu = null;
        try {
            _tip = new JFXTooltip(String.format(""));
        } catch (Exception ex) {
            logger.fatal(ex);
        }

        obj.addEventListener(new JEVisEventListener() {
            @Override
            public void fireEvent(JEVisEvent event) {
                if (event.getType() == JEVisEvent.TYPE.CLASS_UPDATE) {
                    updateGraghic();
                }
            }
        });

    }

    private void updateGraghic() {
        Platform.runLater(() -> {

            try {
                icon = getIcon(_jclass);
                nameLabel.setText(_jclass.getName());
                _view.getChildren().setAll(icon, nameLabel);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }

        });
    }

    public Node getGraphic() {
        updateGraghic();
        return _view;
    }

    public ClassContextMenu getContexMenu() {
        return _menu;
    }

    public Tooltip getToolTip() {
        return _tip;
    }

    public String getText() {
        return "";
    }

    private ImageView getIcon(JEVisClass item) {
        try {
            if (item != null) {
                return ImageConverter.convertToImageView(item.getIcon(), 20, 20);//20
            } else {
                return JEConfig.getImage("1393615831_unknown2.png", 20, 20);
//                return JEConfig.getImage("1390343812_folder-open.png", 20, 20);
            }

        } catch (Exception ex) {
            logger.error("Error while get icon for object: " + ex);
        }
        return new ImageView();

    }

}
