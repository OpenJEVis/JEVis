package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.plugin.Dashboard.wizzard.Page;

/**
 * This Sample handler does nothing and can be used for widgets which does not need dynamic data.
 */
public class NullSampleHandel extends SampleHandler {


    public NullSampleHandel(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    @Override
    public void setUserSelectionDone() {

    }

    @Override
    public Page getPage() {
        return new Page() {
            @Override
            public Node getNode() {
                return new Pane();
            }
        };
    }

    @Override
    public void update() {

    }
}
