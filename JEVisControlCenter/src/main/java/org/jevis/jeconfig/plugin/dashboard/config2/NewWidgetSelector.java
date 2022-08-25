package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.List;

public class NewWidgetSelector extends GridPane {


    final JFXComboBox<Widget> widgetComboBox = new JFXComboBox<>();
    final ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();

    public NewWidgetSelector(List<Widget> widgets) {

        widgetComboBox.getItems().addAll(widgets);

        Callback<ListView<Widget>, ListCell<Widget>> cellFactory = buildCellFactory();
        widgetComboBox.setCellFactory(cellFactory);
        widgetComboBox.setButtonCell(cellFactory.call(null));
        widgetComboBox.getSelectionModel().selectFirst();

        Label labelType = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.new.type"));
        ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);


        newB.setOnAction(event -> {
            selectedWidgetProperty.setValue(getSelectedWidget());
        });

        this.setHgap(8d);
        this.add(labelType, 0, 0);
        this.add(widgetComboBox, 1, 0);
        //this.add(sizeType, 2, 0);
        //this.add(sizeComboBox, 3, 0);
        this.add(newB, 4, 0);
    }

    public ObjectProperty<Widget> getSelectedWidgetProperty() {
        return selectedWidgetProperty;
    }

    public Widget getSelectedWidget() {
        Widget selectedWidget = widgetComboBox.getSelectionModel().getSelectedItem();
        Widget newWidget = selectedWidget.getControl().createNewWidget(selectedWidget.createDefaultConfig());
        newWidget.getConfig().setUuid(newWidget.getControl().getNextFreeUUID());
        return newWidget;
    }

    private Callback<ListView<Widget>, ListCell<Widget>> buildCellFactory() {
        return new Callback<ListView<Widget>, ListCell<Widget>>() {
            @Override
            public ListCell<Widget> call(ListView<Widget> param) {
                return new ListCell<Widget>() {
                    @Override
                    protected void updateItem(Widget widget, boolean empty) {
                        super.updateItem(widget, empty);

                        if (!empty) {
                            setText(widget.typeID());
                            try {
                                setText(widget.typeID());
                                ImageView preview = widget.getImagePreview();
                                preview.setPreserveRatio(false);
                                preview.setFitWidth(25);
                                preview.setFitHeight(25);
                                setGraphic(preview);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                    }
                };
            }
        };
    }


}
