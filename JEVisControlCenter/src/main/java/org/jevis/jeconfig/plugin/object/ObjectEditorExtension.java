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
package org.jevis.jeconfig.plugin.object;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import org.jevis.api.JEVisObject;

/**
 * Interface for the numerous plugins to configure objects.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface ObjectEditorExtension {

    /**
     * Returns true if thes plugin can work with the given JEVisObject
     *
     * @param obj
     * @return
     */
    public boolean isForObject(JEVisObject obj);

    /**
     * Returns the Node where this extension drows its content
     *
     * @return
     */
    public Node getView();

    /**
     * Will be called if this Extension is Visible to the user. Put the loading
     * logic into this function to keep the programm flow fast
     */
    public void setVisible();

    /**
     * Returns the Displayname of this extension
     *
     * @return
     */
    public String getTitel();

    /**
     * return true if the user changed someting and it needs to be saved
     *
     * @return
     */
    public boolean needSave();

    /**
     * Dismiss all made changes
     */
    public void dismissChanges();

    /**
     * Save the lastest changes. returns true if the save was successful or
     * nothing needet to be changed.
     *
     * @return
     */
    public boolean save();

    /**
     * Returns the boolean property the changed status. this will return true if
     * the user changed something and its needs to be saved.
     *
     * @return
     */
    public BooleanProperty getValueChangedProperty();

}
