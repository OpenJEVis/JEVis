package org.jevis.jenotifier.exporter;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

public abstract class ExportEvent {

    public static String CLASS_NAME = "Export Event";
    protected JEVisObject eventObject;

    public ExportEvent(JEVisObject object) {
        eventObject = object;
//        init();
    }

    public void init() {
        initAttributes();
    }

    abstract void initAttributes();

    abstract boolean isTriggered(DateTime lastUpdate);
}
