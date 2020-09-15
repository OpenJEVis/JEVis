package org.jevis.jeopc;

import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

public class PathReferenceDescription {

    private final  ReferenceDescription referenceDescription;
    private final String path;

    public PathReferenceDescription(ReferenceDescription referenceDescription, String path) {
        this.referenceDescription = referenceDescription;
        this.path=path;
    }

    public ReferenceDescription getReferenceDescription() {
        return referenceDescription;
    }

    public String getPath() {
        return path;
    }
}
