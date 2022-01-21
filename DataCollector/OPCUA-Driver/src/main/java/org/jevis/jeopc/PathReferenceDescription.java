package org.jevis.jeopc;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

public class PathReferenceDescription {

    private final  ReferenceDescription referenceDescription;
    private final String path;
    private final DataValue dataValue;

    public PathReferenceDescription(ReferenceDescription referenceDescription, String path, DataValue dataValue) {
        this.referenceDescription = referenceDescription;
        this.path=path;
        this.dataValue = dataValue;
    }

    public ReferenceDescription getReferenceDescription() {
        return referenceDescription;
    }

    public String getPath() {
        return path;
    }

    public DataValue getDataValue() {
        return dataValue;
    }
}