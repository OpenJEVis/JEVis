package org.jevis.jeconfig.plugin.object.extension;

import com.jfoenix.controls.JFXCheckBox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.childrentableeditor.ObjectTable;

import java.time.LocalDate;
import java.time.YearMonth;


public class ChildrenEditorExtension implements ObjectEditorExtension {

    private final BorderPane borderPane = new BorderPane();
    private final int iconSize = 20;
    private JEVisObject parentObject = null;
    private final MFXDatePicker startDatePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    private final MFXDatePicker endDatePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
    private final ToggleButton xlsxButton = new ToggleButton("", JEConfig.getImage("xlsx_315594.png", iconSize, iconSize));
    private final Label columnLabel = new Label(I18n.getInstance().getString("searchbar.filter") + " " + I18n.getInstance().getString("plugin.dtrc.dialog.columnlabel"));
    private final Label includeLabel = new Label(I18n.getInstance().getString("plugin.object.childreneditor.includelabel"));
    private final Label excludeLabel = new Label(I18n.getInstance().getString("plugin.object.childreneditor.excludelabel"));
    private final MFXComboBox<String> columnBox = new MFXComboBox<>();
    private final String nameString = I18n.getInstance().getString("plugin.object.attribute.overview.name");
    private final String classString = I18n.getInstance().getString("plugin.dtrc.dialog.classlabel");
    private final String sourceString = I18n.getInstance().getString("jevis.types.source");
    private final MFXTextField filterInclude = new MFXTextField();
    private final MFXTextField filterExclude = new MFXTextField();
    private final JFXCheckBox sourceDetails = new JFXCheckBox(I18n.getInstance().getString("plugin.object.childreneditor.sourcedetails"));

    private final GridPane dateSelection = new GridPane();

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

        dateSelection.setPadding(new Insets(5));
        dateSelection.setHgap(6);
        dateSelection.setVgap(6);

        columnBox.getItems().addAll(nameString, classString, sourceString);
        columnBox.getSelectionModel().selectFirst();

        int row = 0;
        dateSelection.add(startDatePicker, 0, row);
        dateSelection.add(endDatePicker, 1, row);
        dateSelection.add(reloadButton, 2, row);
        dateSelection.add(xlsxButton, 3, row);
        dateSelection.add(sourceDetails, 4, row);
        row++;

        dateSelection.add(columnLabel, 0, row);
        dateSelection.add(columnBox, 1, row);
        dateSelection.add(includeLabel, 2, row);
        dateSelection.add(filterInclude, 3, row);
        dateSelection.add(excludeLabel, 4, row);
        dateSelection.add(filterExclude, 5, row);
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
        ObjectTable objectTable = new ObjectTable(this.parentObject, startDatePicker, endDatePicker, reloadButton, xlsxButton, filterInclude, filterExclude, columnBox, sourceDetails);

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
