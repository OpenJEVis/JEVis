package org.jevis.jeconfig.plugin.object.extension;

import com.jfoenix.controls.JFXDatePicker;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.childrentableeditor.ObjectTable;

import java.time.LocalDate;


public class ChildrenEditorExtension implements ObjectEditorExtension {

    private final BorderPane borderPane = new BorderPane();
    private final int iconSize = 20;
    private JEVisObject parentObject = null;
    private final JFXDatePicker startDatePicker = new JFXDatePicker();
    private final JFXDatePicker endDatePicker = new JFXDatePicker();
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
    private final ToggleButton xlsxButton = new ToggleButton("", JEConfig.getImage("xlsx_315594.png", iconSize, iconSize));
    private final HBox dateSelection = new HBox(6, startDatePicker, endDatePicker, reloadButton, xlsxButton);

    public ChildrenEditorExtension(JEVisObject parentObject) {
        this.parentObject = parentObject;
        this.borderPane.getStyleClass().add("children-editor-plugin");
//        this.viewPane.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        startDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.startdate")));
        endDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.enddate")));
        Tooltip xlsxTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.xlsx"));
        xlsxButton.setTooltip(xlsxTooltip);

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reloadButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(xlsxButton);

        startDatePicker.setPrefWidth(120d);
        endDatePicker.setPrefWidth(120d);

        startDatePicker.setValue(LocalDate.now().minusDays(1));
        endDatePicker.setValue(LocalDate.now());
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        return true;
    }

    @Override
    public Node getView() {
        return this.borderPane;
    }

    @Override
    public void setVisible() {
        ObjectTable objectTable = new ObjectTable(this.parentObject, startDatePicker, endDatePicker, reloadButton, xlsxButton);

        this.borderPane.setTop(dateSelection);
        this.borderPane.setCenter(objectTable.getTableView());
    }

    @Override
    public String getTitle() {
        return I18n.getInstance().getString("plugin.object.childreneditor.title");
    }

    @Override
    public boolean needSave() {
        return false;
    }

    @Override
    public void dismissChanges() {

    }

    @Override
    public void showHelp(boolean show) {

    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return new SimpleBooleanProperty(false);
    }
}
