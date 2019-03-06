package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
    public JsonNode toJsonNode() {
        ArrayNode dataArrayNode = JsonNodeFactory.instance.arrayNode();

        return dataArrayNode;
    }

    @Override
    public Page getPage() {
        return new Page() {
            @Override
            public Node getNode() {
                return new Pane(new Label("No data needed"));
            }

            @Override
            public boolean isSkipable() {
                return true;
            }
        };
    }

    @Override
    public void update() {

    }
}
