package org.jevis.jecc.plugin.dashboard.widget;

import javafx.scene.image.Image;

public class WidgetSelection {


    Image icon;
    String classname;
    String displayname;
    String type;

    public WidgetSelection(String classname, String type, String displayname, Image icon) {
        this.classname = classname;
        this.displayname = displayname;
        this.icon = icon;
        this.type = type;
    }

    public Image getIcon() {
        return icon;
    }

    public String getClassname() {
        return classname;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getType() {
        return type;
    }
}
