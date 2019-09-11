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
package org.jevis.jeconfig.sample;

import javafx.scene.Node;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface SampleEditorExtension {

    /**
     * Returns true if thes plugin can work with the given JEVisObject
     *
     * @param obj
     * @return
     */
    boolean isForAttribute(JEVisAttribute obj);

    /**
     * Returns the Node where this extension drows its content
     *
     * @return
     */
    Node getView();

    /**
     * Returns the Displayname of this extension
     *
     * @return
     */
    String getTitle();

    /**
     * Set the new samples to load.
     *
     * @param att
     * @param samples
     */
    void setSamples(JEVisAttribute att, List<JEVisSample> samples);

    /**
     * updateData the gui of this extension. This should be called if the sample
     * changed and the extension is visible again.
     */
    void update();

    /**
     * Send the OK button action to this extension. The is an tmp solution and
     * may be replaced.
     *
     * @return
     * @deprecated
     */
    boolean sendOKAction();

    /**
     * Disable editing(writing to DataSource) in the plugin. For example if the user
     * selected an aggregation we cannot edit them in the DataSource.
     *
     * @param disable
     */
    void disableEditing(boolean disable);
}
