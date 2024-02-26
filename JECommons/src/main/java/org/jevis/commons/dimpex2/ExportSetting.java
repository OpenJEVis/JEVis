package org.jevis.commons.dimpex2;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;

import java.util.UUID;

public class ExportSetting {

    private static final String fileending = ".json";
    private final boolean exportLastSamples = true;
    private final boolean exportAllSamples = false;
    private final UUID uuid = UUID.randomUUID();
    private JEVisObject object;
    private JEVisAttribute attribute;

    public ExportSetting() {
    }

    public ExportSetting(JEVisObject object) {
        this.object = object;
    }


    public String getFilename() {
        return uuid.toString() + fileending;
    }

    public JEVisObject getObject() {
        return object;
    }

    public JEVisAttribute getAttribute() {
        return attribute;
    }

    public boolean isExportLastSamples() {
        return exportLastSamples;
    }

    public boolean isExportAllSamples() {
        return exportAllSamples;
    }
}
