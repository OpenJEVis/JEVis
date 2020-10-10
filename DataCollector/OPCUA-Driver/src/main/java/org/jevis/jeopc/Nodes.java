package org.jevis.jeopc;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

public class Nodes {

    public static NodeId toNodeID(ReferenceDescription referenceDescription) {
        NodeId nodeId = new NodeId(referenceDescription.getNodeId().getNamespaceIndex(), (UInteger) referenceDescription.getNodeId().getIdentifier());

        return nodeId;

    }
}
