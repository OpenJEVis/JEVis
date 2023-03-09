/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisDataSource;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface Plugin {

    public static double IconSize = 15;

    String getClassName();

    String getName();

    void setName(String name);

    StringProperty nameProperty();

    String getUUID();

    void setUUID(String id);

    String getToolTip();

    StringProperty uuidProperty();

    Node getMenu();

    boolean supportsRequest(int cmdType);

    Node getToolbar();

    void updateToolbar();

    JEVisDataSource getDataSource();

    void setDataSource(JEVisDataSource ds);

    void handleRequest(int cmdType);

    Node getContentNode();

    Region getIcon();

    void fireCloseEvent();

    void setHasFocus();

    void lostFocus();

    void openObject(Object object);

    int getPrefTapPos();
}
