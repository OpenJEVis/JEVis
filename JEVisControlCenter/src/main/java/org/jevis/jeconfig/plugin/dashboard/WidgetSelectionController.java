package org.jevis.jeconfig.plugin.dashboard;

import com.google.common.collect.Iterables;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the set of currently selected widgets on the dashboard canvas and
 * provides bulk-edit operations that apply uniformly to all selected widgets.
 *
 * <p>Extracted from {@link DashboardControl} to separate selection concerns
 * from the central lifecycle coordinator. Holds a reference to
 * {@link DashboardControl} for callbacks that affect the broader UI state
 * (e.g. requesting a view update, redrawing, showing the config panel).
 */
public class WidgetSelectionController {

    private static final Logger logger = LogManager.getLogger(WidgetSelectionController.class);

    private final DashboardControl control;
    private final List<Widget> selectedWidgets = new ArrayList<>();

    /**
     * Creates a new selection controller bound to the given dashboard controller.
     *
     * @param control the dashboard controller that owns this selection state
     */
    public WidgetSelectionController(DashboardControl control) {
        this.control = control;
    }

    /**
     * Returns a live view of the currently selected widgets. Never {@code null}.
     */
    public List<Widget> getSelectedWidgets() {
        return selectedWidgets;
    }

    /**
     * Replaces the selection with the given widget list.
     * Only takes effect when the dashboard is in edit mode.
     *
     * @param widgets widgets to select; may be empty to clear the selection
     */
    public void setSelectedWidgets(List<Widget> widgets) {
        if (control.editableProperty.get()) {
            selectedWidgets.clear();
            selectedWidgets.addAll(widgets);
            updateHighlightSelected();
            control.getDashBordPlugIn().getScrollPane().requestFocus();
            control.showConfig();
        }
    }

    /**
     * Toggles individual widgets in the selection (Ctrl+click behaviour).
     * Widgets already selected are deselected; others are added.
     *
     * @param widgetsToToggle list of widgets whose selection state should be toggled
     */
    public void addToWidgetSelection(List<Widget> widgetsToToggle) {
        widgetsToToggle.forEach(widget -> {
            if (selectedWidgets.contains(widget)) {
                selectedWidgets.remove(widget);
            } else {
                selectedWidgets.add(widget);
            }
        });
        updateHighlightSelected();
        control.showConfig();
    }

    /**
     * Replaces the selection with a single widget.
     *
     * @param widget the widget to select
     */
    public void setSelectedWidget(Widget widget) {
        List<Widget> selected = new ArrayList<>();
        selected.add(widget);
        setSelectedWidgets(selected);
    }

    /**
     * Selects all widgets of the same type as the given reference widget.
     *
     * @param widget the reference widget whose type determines the selection
     */
    public void setSelectAllFromType(Widget widget) {
        List<Widget> selected = new ArrayList<>();
        control.getWidgetList().forEach(w -> {
            if (widget.getConfig().getType().equals(w.getConfig().getType())) {
                selected.add(w);
            }
        });
        setSelectedWidgets(selected);
    }

    /**
     * Clears the selection without triggering any config panel update.
     */
    public void clearSelection() {
        selectedWidgets.clear();
    }

    /**
     * Updates the highlight glow on all widgets to reflect the current selection.
     */
    void updateHighlightSelected() {
        for (Widget widget : control.getWidgetList()) {
            widget.setGlow(selectedWidgets.contains(widget), false);
        }
    }

    // -------------------------------------------------------------------------
    // Bulk-edit operations on selected widgets
    // -------------------------------------------------------------------------

    /**
     * Moves all selected widgets by the given pixel deltas.
     * Exactly one of the four values should be {@code > 0}; all others {@code -1}.
     */
    public void moveSelected(double up, double down, double left, double right) {
        selectedWidgets.forEach(widget -> {
            if (up > 0) {
                widget.getConfig().setyPosition(widget.getConfig().getyPosition() - up);
            } else if (down > 0) {
                widget.getConfig().setyPosition(widget.getConfig().getyPosition() + down);
            } else if (left > 0) {
                widget.getConfig().setxPosition(widget.getConfig().getxPosition() - left);
            } else if (right > 0) {
                widget.getConfig().setxPosition(widget.getConfig().getxPosition() + right);
            }
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the z-layer for all selected widgets and redraws the dashboard.
     */
    public void layerSelected(int layer) {
        selectedWidgets.forEach(widget -> widget.getConfig().setLayer(layer));
        control.redrawDashboardPane();
    }

    /**
     * Sets the foreground (font) colour for all selected widgets.
     */
    public void fgColorSelected(Color color) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontColor(color);
            widget.updateConfig();
        });
        control.redrawDashboardPane();
    }

    /**
     * Sets the background colour for all selected widgets.
     */
    public void bgColorSelected(Color color) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setBackgroundColor(color);
            widget.updateConfig();
        });
    }

    /**
     * Resizes all selected widgets. Pass {@code -1} to leave a dimension unchanged.
     *
     * @param width  new width in pixels, or {@code -1} to keep current
     * @param height new height in pixels, or {@code -1} to keep current
     */
    public void sizeSelected(double width, double height) {
        selectedWidgets.forEach(widget -> {
            Size size = widget.getConfig().getSize();
            if (width > 0) size.setWidth(width);
            if (height > 0) size.setHeight(height);
            widget.getConfig().setSize(size);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Moves all selected widgets to an absolute position.
     * Pass {@code -1} for a coordinate to leave it unchanged.
     */
    public void positionSelected(double xpos, double ypos) {
        selectedWidgets.forEach(widget -> {
            if (xpos > -1) widget.getConfig().setxPosition(xpos);
            if (ypos > -1) widget.getConfig().setyPosition(ypos);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Copies the data model of the last selected widget to all other selected widgets.
     * Only applies to widgets implementing {@link DataModelWidget}.
     */
    public void equalizeDataModel() {
        Widget lastWidget = Iterables.getLast(selectedWidgets);
        if (lastWidget instanceof DataModelWidget) {
            selectedWidgets.forEach(widget -> {
                if (widget instanceof DataModelWidget && !widget.equals(lastWidget)) {
                    ((DataModelWidget) widget).setDataHandler(((DataModelWidget) lastWidget).getDataHandler());
                    widget.updateConfig();
                    control.requestViewUpdate(widget);
                    widget.updateData(control.getInterval());
                }
            });
        }
    }

    /**
     * Toggles drop-shadow for all selected widgets.
     */
    public void shadowSelected(boolean shadows) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setShowShadow(shadows);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Toggles the value label visibility for all selected widgets.
     */
    public void showValueSelected(boolean showValue) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setShowValue(showValue);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the title alignment for all selected widgets.
     */
    public void alignSelected(Pos pos) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setTitlePosition(pos);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the font size for all selected widgets.
     */
    public void fontSizeSelected(double size) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontSize(size);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the font weight for all selected widgets.
     */
    public void fontWeightSelected(FontWeight fontWeight) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontWeight(fontWeight);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the font posture (italic/normal) for all selected widgets.
     */
    public void fontPostureSelected(FontPosture fontPosture) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontPosture(fontPosture);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Toggles underline for all selected widgets.
     */
    public void fontUnderlinedSelected(Boolean underlined) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontUnderlined(underlined);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the display title for all selected widgets.
     */
    public void setWidgetTitle(String name) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setTitle(name);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }

    /**
     * Sets the decimal precision for all selected widgets.
     */
    public void decimalsSelected(int size) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setDecimals(size);
            widget.updateConfig();
            control.requestViewUpdate(widget);
        });
    }
}
