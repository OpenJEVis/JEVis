/**
 *
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
package org.jevis.application.unit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitGraphic {

    private final HBox _view = new HBox();
    private final HBox _edior = new HBox();
    private final Label nameLabel = new Label("*Missing*");
    private final UnitObject _obj;
    private final ContextMenu _menu;
    private final UnitTree _tree;
    private Tooltip _tip;
    private static final Image ICON_QUANTITI = new Image(UnitGraphic.class.getResourceAsStream("/icons/" + "quntity.png"));
    private static final Image ICON_SIUNIT = new Image(UnitGraphic.class.getResourceAsStream("/icons/" + "siunit.png"));
    private static final Image ICON_NONSIUNIT = new Image(UnitGraphic.class.getResourceAsStream("/icons/" + "nonsiunit.png"));
    private static final Image ICON_LABEL = new Image(UnitGraphic.class.getResourceAsStream("/icons/" + "label.png"));

    private ImageView _icon;

    public UnitGraphic(UnitObject obj, UnitTree tree) {
        _obj = obj;
        _tree = tree;
//        _menu = new ObjectContextMenu(obj, tree);
        _menu = new ContextMenu();

//        icon = getIcon(_obj.);
        _view.setAlignment(Pos.CENTER_LEFT);
        _view.setSpacing(3);
        _view.setPadding(new Insets(0, 0, 0, 5));

    }

    public Node getGraphic() {
//        icon = getIcon(_obj);
        nameLabel.setText(_obj.getName());
        _view.getChildren().setAll(getIcon(_obj), nameLabel);

        return _view;
    }

    public ContextMenu getContexMenu() {
        return _menu;
    }

    public Tooltip getToolTip() {
        return _tip;
    }

    public String getText() {
        return "";
    }

    private ImageView getIcon(UnitObject item) {
        if (_icon != null) {
            return _icon;
        }

        switch (item.getType()) {
            case Quntity:
                _icon = new ImageView(ICON_QUANTITI);
                break;
            case SIUnit:
                _icon = new ImageView(ICON_SIUNIT);
                break;
            case NonSIUnit:
                _icon = new ImageView(ICON_NONSIUNIT);
                break;
            case AltSymbol:
                _icon = new ImageView(ICON_LABEL);
                break;
            default:
                _icon = new ImageView();
                break;
        }

        _icon.fitHeightProperty().set(20);
        _icon.fitWidthProperty().set(60);

        return _icon;

    }

}
