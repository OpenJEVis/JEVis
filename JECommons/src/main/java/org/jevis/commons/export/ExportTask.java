package org.jevis.commons.export;

public abstract class ExportTask {

    private Object data;

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
