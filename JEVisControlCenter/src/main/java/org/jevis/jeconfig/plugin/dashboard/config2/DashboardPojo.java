package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.widget.Size;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class DashboardPojo {
    public static String DATA_HANDLER_NODE = "dataHandler";
    public static String WIDGET_SETTINGS_NODE = "extra";

    public String version = "1.0";
    private String name = "";
    public BorderWidths borderSize = new BorderWidths(0.2);
    public Color fontColor = Color.WHITE;
    public Color fontColorSecondary = Color.DODGERBLUE;
    public String title = "Default dashboard";

    public Color backgroundColor = Color.web("#126597");
    public Double xGridInterval = 50.0d;
    public Double yGridInterval = 50.0d;

    public Size size = Size.DEFAULT;

    private final ObjectMapper mapper = new ObjectMapper();
    private TimeFrameFactory timeFrame;

    private Interval interval = new Interval(new DateTime(), new DateTime());
    private Integer updateRate = 900;
    private Boolean snapToGrid = true;
    private Boolean showGrid = true;
    private JEVisObject jeVisObject = null;
    private Boolean isNew = false;

    private final List<WidgetPojo> widgetList = new ArrayList<>();

    public Double zoomFactor = 1.0d;

    public DashboardPojo() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getNew() {
        return this.isNew;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setNew(Boolean aNew) {
        this.isNew = aNew;
    }

    public List<WidgetPojo> getWidgetList() {
        return this.widgetList;
    }

    public Interval getInterval() {
        return this.interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Boolean getShowGrid() {
        return this.showGrid;
    }

    public void setShowGrid(Boolean showGrid) {
        this.showGrid = showGrid;
    }

    public Boolean getSnapToGrid() {
        return this.snapToGrid;
    }

    public void setSnapToGrid(Boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }

    public Integer getUpdateRate() {
        return this.updateRate;
    }

    public void setUpdateRate(Integer updateRate) {
        this.updateRate = updateRate;
    }

    public Double getZoomFactor() {
        return this.zoomFactor;
    }

    public void setZoomFactor(Double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public JEVisObject getDashboardObject() {
        return this.jeVisObject;
    }

    public void setJevisObject(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    public void setxGridInterval(Double xGridInterval) {
        this.xGridInterval = xGridInterval;
    }

    public void setyGridInterval(Double yGridInterval) {
        this.yGridInterval = yGridInterval;
    }

    public BorderWidths getBorderSize() {
        return this.borderSize;
    }

    public void setBorderSize(BorderWidths borderSize) {
        this.borderSize = borderSize;
    }

    public Color getFontColor() {
        return this.fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Color getFontColorSecondary() {
        return this.fontColorSecondary;
    }

    public void setFontColorSecondary(Color fontColorSecondary) {
        this.fontColorSecondary = fontColorSecondary;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    public Double getxGridInterval() {
        return this.xGridInterval;
    }

    public Double getyGridInterval() {
        return this.yGridInterval;
    }

    public Size getSize() {
        return this.size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public TimeFrameFactory getTimeFrame() {
        return this.timeFrame;
    }

    public void setTimeFrame(TimeFrameFactory timeFrame) {
        this.timeFrame = timeFrame;
    }
}
