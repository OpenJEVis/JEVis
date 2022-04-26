package org.jevis.jeconfig.plugin.object.extension.OPC;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

import org.jevis.jeopc.Nodes;

public class Node {

    private final SimpleObjectProperty<ReferenceDescription> descriptionProperty;
    private SimpleObjectProperty<NodeId> nodeIdProperty = new SimpleObjectProperty<>();
    private SimpleStringProperty stringNodeID = new SimpleStringProperty("");
    private SimpleStringProperty idStringProperty = new SimpleStringProperty("");
    private final StringProperty pathProperty;
    private StringProperty stringProperty = new SimpleStringProperty("hmmm");
    private StringProperty typeProperty = new SimpleStringProperty("");
    private DataValue dataValue;
    private String TrendID;
    private String logInterval = "";
    private String name;
    public boolean selected;
    private String trendType = GERNERIC_TREND;

    public static final String GERNERIC_TREND = "generic";
    public static final String BACNET_TREND = "bacnet";

    public Node(ReferenceDescription referenceDescription, String xpath, DataValue dataValue) {
        pathProperty = new SimpleStringProperty(xpath);
        descriptionProperty=new SimpleObjectProperty<>(referenceDescription);
        this.dataValue = dataValue;
        name = descriptionProperty.get().getBrowseName().getName();

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

    public String readData(){
        try {
            if (descriptionProperty.get().getNodeClass().getValue() == 2) {
                return dataValue.getValue().getValue().toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTrendID() {
        return TrendID;
    }

    public void setTrendID(String trendID) {
        TrendID = trendID;
    }

    public String getLogInterval() {
        return logInterval;
    }

    public void setLogInterval(String logInterval) {
        this.logInterval = logInterval;
    }

    public ReferenceDescription getDescriptionProperty() {
        return descriptionProperty.get();
    }

    public SimpleObjectProperty<ReferenceDescription> descriptionPropertyProperty() {
        return descriptionProperty;
    }

    public void setDescriptionProperty(ReferenceDescription descriptionProperty) {
        this.descriptionProperty.set(descriptionProperty);
    }

    public NodeId getNodeIdProperty() {
        return nodeIdProperty.get();
    }

    public SimpleObjectProperty<NodeId> nodeIdPropertyProperty() {
        return nodeIdProperty;
    }

    public void setNodeIdProperty(NodeId nodeIdProperty) {
        this.nodeIdProperty.set(nodeIdProperty);
    }

    public String getStringNodeID() {
        return stringNodeID.get();
    }

    public SimpleStringProperty stringNodeIDProperty() {
        return stringNodeID;
    }

    public void setStringNodeID(String stringNodeID) {
        this.stringNodeID.set(stringNodeID);
    }

    public String getIdStringProperty() {
        return idStringProperty.get();
    }

    public SimpleStringProperty idStringPropertyProperty() {
        return idStringProperty;
    }

    public void setIdStringProperty(String idStringProperty) {
        this.idStringProperty.set(idStringProperty);
    }

    public String getPathProperty() {
        return pathProperty.get();
    }

    public StringProperty pathPropertyProperty() {
        return pathProperty;
    }

    public void setPathProperty(String pathProperty) {
        this.pathProperty.set(pathProperty);
    }

    public String getStringProperty() {
        return stringProperty.get();
    }

    public StringProperty stringPropertyProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty.set(stringProperty);
    }

    public String getTypeProperty() {
        return typeProperty.get();
    }

    public StringProperty typePropertyProperty() {
        return typeProperty;
    }

    public void setTypeProperty(String typeProperty) {
        this.typeProperty.set(typeProperty);
    }

    public DataValue getDataValue() {
        return dataValue;
    }

    public void setDataValue(DataValue dataValue) {
        this.dataValue = dataValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrendType() {
        return trendType;
    }

    public void setTrendType(String trendType) {
        this.trendType = trendType;
    }

    @Override
    public String toString() {
        return "Node{" +
                "descriptionProperty=" + descriptionProperty +
                ", nodeIdProperty=" + nodeIdProperty +
                ", stringNodeID=" + stringNodeID +
                ", idStringProperty=" + idStringProperty +
                ", pathProperty=" + pathProperty +
                ", stringProperty=" + stringProperty +
                ", typeProperty=" + typeProperty +
                ", dataValue=" + dataValue +
                ", TrendID='" + TrendID + '\'' +
                ", logInterval='" + logInterval + '\'' +
                ", name='" + name + '\'' +
                ", selected=" + selected +
                ", trendType='" + trendType + '\'' +
                '}';
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
