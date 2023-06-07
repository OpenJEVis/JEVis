package org.jevis.jecc.plugin.dashboard.wizard;

import javafx.scene.Node;

public abstract class Page {

    public abstract Node getNode();

    /**
     * Temporary solution for pages which should be skipped like an empty SampleHandler for an label Widget
     *
     * @return
     */
    public abstract boolean isSkipable();

}
