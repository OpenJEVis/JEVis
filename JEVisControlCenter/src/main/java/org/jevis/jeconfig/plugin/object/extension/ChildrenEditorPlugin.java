package org.jevis.jeconfig.plugin.object.extension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.childrentableeditor.ObjectTable;
import org.jevis.jeconfig.tool.Layouts;


public class ChildrenEditorPlugin implements ObjectEditorExtension {

    private final AnchorPane viewPane = new AnchorPane();
    private JEVisObject parentObject = null;

    public ChildrenEditorPlugin(JEVisObject parentObject) {
        this.parentObject = parentObject;
        this.viewPane.getStyleClass().add("children-editor-plugin");
//        this.viewPane.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        return true;
    }

    @Override
    public Node getView() {
        return this.viewPane;
    }

    @Override
    public void setVisible() {
        ObjectTable objectTable = new ObjectTable(this.parentObject);
        TableView tableView = objectTable.getTableView();
        Layouts.setAnchor(tableView, 0);
        this.viewPane.getChildren().setAll(tableView);
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
