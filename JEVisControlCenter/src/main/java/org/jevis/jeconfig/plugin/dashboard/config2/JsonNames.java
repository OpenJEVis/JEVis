package org.jevis.jeconfig.plugin.dashboard.config2;

import java.util.HashMap;
import java.util.Map;

public interface JsonNames {

    interface Dashboard {

        String BACKGROUND_COLOR = "Dash Board Color";
        String BACKGROUND_MODE = "backgroundMode";
        String X_GRID_INTERVAL = "X Axis Grid Interval";
        String Y_GRID_INTERVAL = "Y Axis Grid Interval";
        String ZOOM_FACTOR = "Zoom Factor";
        String UPDATE_RATE = "Update Rate";
        String SNAP_TO_GRID = "Snap to Grid";
        String SHOW_GRID = "Show Grid";
        String WIDGET_NODE = "Widget";
        String DEFAULT_PERIOD = "Data Period";
        String HEIGHT = "height";
        String WIDTH = "width";
        String JSON_VERSION = "version";

    }

    interface Widget {

        String UUID = "uuid";
        String TITLE = "title";
        String TOOLTIP = "tooltip";
        String TITLE_POSITION = "Title Position";
        String TYPE = "WidgetType";
        String BACKGROUND_COLOR = "Background Color";
        String FONT_COLOR = "Font Color";
        String HEIGHT = "height";
        String WIDTH = "width";
        String FONT_SIZE = "fontSize";
        String FONT_WEIGHT = "fontWeight";
        String FONT_POSTURE = "fontPosture";
        String FONT_UNDERLINED = "fontUnderlined";
        String X_POS = "xPos";
        String Y_POS = "yPos";
        String SHOW_SHADOW = "shadow";
        String BORDER_SIZE = "borderSize";
        String DATA_HANDLER_NODE = "dataHandler";
        String LAYER = "layer";
        String DECIMALS = "decimals";
        String SHOW_VALUE = "showValue";
        String FIXED_TIMEFRAME = "fixedTimeframe";
        String FORCED_TIMEFRAME = "forcedTimeFrame";

    }

    /**
     * names that should be fixed in old json files
     */
    interface NameFix {

        Map<String, String> lookup = new HashMap<String, String>() {{
            put("Dash Board Color", "dashboardColor");
            put("X Axis Grid Interval", "xGridInterval");
            put("Y Axis Grid Interval", "yGridInterval");
            put("Snap to Grid", "snapToGrid");
            put("Show Grid", "showGrid");
            put("Data Period", "dataPeriod");
            put("Zoom Factor", "zoom");
            put("Update Rat", "updateInterval");
            put("Widget", "widget");
            put("WidgetType", "widgetType");
            put("Background Color", "backgroundColor");
            put("Font Color", "fontColor");
            put("Font Weight", "fontWeight");
            put("Font Underlined", "fontUnderlined");
            put("Title Position", "titlePosition");
        }};


    }

}
