/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object.selectiontree;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.resource.ImageConverter;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.commons.CommonClasses;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectGraphic {

    private final HBox _view = new HBox();
    private ImageView icon = new ImageView();
    private final Label nameLabel = new Label("*Missing*");
    private final ObjectContainer _container;
    private final ObjectSelectionTree _tree;
    private final CheckBox _checkBox = new CheckBox();
    private final Region _spacer = new Region();
    private Tooltip _tip;

    public ObjectGraphic(ObjectContainer obj, ObjectSelectionTree tree) {
        _container = obj;
        _tree = tree;

//        _checkBox.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("select: " + _container.getIdentifier());
//
//                if (_checkBox.isSelected()) {
//                    _tree.addSelected(_container);
//                } else {
//                    _tree.removeSelected(_container);
//                }
//
//            }
//        });
        if (_container.isObject()) {
            try {
                if (_container.getObject().getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
                    icon = getIcon(_container.getObject().getLinkedObject());
                } else {
                    icon = ResourceLoader.getImage("1403724422_link_break.png", 20, 20);
                }
            } catch (JEVisException ex) {
                Logger.getLogger(ObjectGraphic.class.getName()).log(Level.SEVERE, null, ex);
            }
            icon = getIcon(_container.getObject());
        } else {
            icon = ResourceLoader.getImage("1404251828_code_brackets.png", 20, 20);
        }

        _view.setAlignment(Pos.CENTER_LEFT);
        _view.setSpacing(3);
        _view.setPadding(new Insets(0, 0, 0, 5));
        HBox.setHgrow(_spacer, Priority.ALWAYS);

//        try {
//            _tip = new Tooltip(String.format("ID:       %s\nName: %s\nClass:  %s\n", obj.getID().toString(), obj.getName(), obj.getJEVisClass().getName()));
//        } catch (JEVisException ex) {
//            Logger.getLogger(ObjectGraphic.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public boolean isSelected() {
        return _checkBox.isSelected();
    }

    public void setSelect(final boolean select) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                _checkBox.setSelected(select);
            }
        });

    }

    public Node getGraphic() {
//        icon = getIcon(_obj);

        nameLabel.setText(_container.getObject().getName());
        _view.getChildren().setAll(icon, nameLabel, _spacer);

        return _view;
    }

    public Tooltip getToolTip() {
        return _tip;
    }

    public String getText() {
        return "";
    }

    private ImageView getIcon(JEVisObject item) {
        try {
            if (item != null && item.getJEVisClass() != null) {
                return ImageConverter.convertToImageView(item.getJEVisClass().getIcon(), 20, 20);//20
            } else {
                return ResourceLoader.getImage("1390343812_folder-open.png", 20, 20);
            }

        } catch (Exception ex) {
            System.out.println("Error while get icon for object: " + ex);
        }
        return new ImageView();

    }

}
