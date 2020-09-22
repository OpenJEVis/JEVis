package org.jevis.jeconfig.plugin.object.extension.OPC;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

import org.jevis.jeopc.Nodes;

public class Node {

    public final SimpleObjectProperty<ReferenceDescription> descriptionProperty;
    public SimpleObjectProperty<NodeId> nodeIdProperty = new SimpleObjectProperty<>();
    public SimpleStringProperty stringNodeID = new SimpleStringProperty("");
    public SimpleStringProperty idStringProperty = new SimpleStringProperty("");
    public final StringProperty pathProperty;
    public StringProperty stringProperty = new SimpleStringProperty("hmmm");
    public StringProperty typeProperty = new SimpleStringProperty("");

    public Node(ReferenceDescription referenceDescription, String xpath) {
        pathProperty = new SimpleStringProperty(xpath);
        descriptionProperty=new SimpleObjectProperty<>(referenceDescription);

        try {
            NodeId nodeId = Nodes.toNodeID(descriptionProperty.get());
            nodeIdProperty.set(nodeId);
            stringNodeID.set("ns="+nodeId.getNamespaceIndex()+";i="+nodeId.getIdentifier());
            idStringProperty.set(referenceDescription.getBrowseName().getName());
            stringProperty.setValue(referenceDescription.toString());
            typeProperty.set(referenceDescription.getNodeClass().toString());
        }catch ( Exception ex){
            ex.printStackTrace();
        }


    }
/**
    public ReferenceDescription getDescriptionProperty() {
        return descriptionProperty.get();
    }

    public SimpleObjectProperty<ReferenceDescription> descriptionPropertyProperty() {
        return descriptionProperty;
    }

    public NodeId getNodeIdProperty() {
        return nodeIdProperty.get();
    }

    public SimpleObjectProperty<NodeId> nodeIdPropertyProperty() {
        return nodeIdProperty;
    }

    public String getIdStringProperty() {
        return idStringProperty.get();
    }

    public SimpleStringProperty idStringPropertyProperty() {
        return idStringProperty;
    }

    public String getPathProperty() {
        return pathProperty.get();
    }

    public StringProperty pathPropertyProperty() {
        return pathProperty;
    }

    public String getStringProperty() {
        return stringProperty.get();
    }

    public StringProperty stringPropertyProperty() {
        return stringProperty;
    }

 */
}
