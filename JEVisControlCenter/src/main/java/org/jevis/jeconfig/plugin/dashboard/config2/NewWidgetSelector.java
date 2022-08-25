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
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.dashboard.widget.WidgetSelection;
import org.jevis.jeconfig.plugin.dashboard.widget.Widgets;

import java.util.Collection;
import java.util.stream.Collectors;

public class NewWidgetSelector extends GridPane {


    final JFXComboBox<WidgetSelection> widgetComboBox = new JFXComboBox<>();
    final ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();
    final DashboardControl control;

    public NewWidgetSelector(DashboardControl dashboardControl) {
        this.control = dashboardControl;
        Collection<WidgetSelection> widgets = Widgets.availableWidgets.values().stream().sorted((o1, o2) -> o1.getDisplayname().compareTo(o2.getDisplayname())).collect(Collectors.toList());
        widgetComboBox.getItems().addAll(widgets);

        Callback<ListView<WidgetSelection>, ListCell<WidgetSelection>> cellFactory = buildCellFactory();
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

        try {
            WidgetSelection selectedWidget = widgetComboBox.getSelectionModel().getSelectedItem();
            Widget newWidget = Widgets.createWidget(selectedWidget.getType(), control, null);
            //Widget newWidget = selectedWidget.getControl().createNewWidget(selectedWidget.createDefaultConfig());
            newWidget.getConfig().setUuid(newWidget.getControl().getNextFreeUUID());
            return newWidget;
        } catch (Exception ex) {
            return null;
        }

    }

    private Callback<ListView<WidgetSelection>, ListCell<WidgetSelection>> buildCellFactory() {
        return new Callback<ListView<WidgetSelection>, ListCell<WidgetSelection>>() {
            @Override
            public ListCell<WidgetSelection> call(ListView<WidgetSelection> param) {
                return new ListCell<WidgetSelection>() {
                    @Override
                    protected void updateItem(WidgetSelection widget, boolean empty) {
                        super.updateItem(widget, empty);

                        if (!empty) {
                            setText(widget.getDisplayname());
                            try {
                                setText(widget.getDisplayname());
                                ImageView preview = Widgets.getImage(widget.getType(), 25, 25);
                                preview.setPreserveRatio(false);
                                preview.setFitWidth(25);
                                preview.setFitHeight(25);
                                //setGraphic(preview);
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
