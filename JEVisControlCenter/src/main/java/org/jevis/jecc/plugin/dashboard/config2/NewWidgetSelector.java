package org.jevis.jecc.plugin.dashboard.config2;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.widget.Widget;
import org.jevis.jecc.plugin.dashboard.widget.WidgetSelection;
import org.jevis.jecc.plugin.dashboard.widget.Widgets;

import java.util.Collection;
import java.util.stream.Collectors;

public class NewWidgetSelector extends GridPane {


    final MFXComboBox<WidgetSelection> widgetComboBox = new MFXComboBox<>();
    final ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();
    final DashboardControl control;

    private final MFXButton newB = new MFXButton("", ControlCenter.getSVGImage(Icon.PLUS, 20, 20));


    public NewWidgetSelector(DashboardControl dashboardControl) {
        this.control = dashboardControl;
        Collection<WidgetSelection> widgets = Widgets.availableWidgets.values().stream().sorted((o1, o2) -> o1.getDisplayname().compareTo(o2.getDisplayname())).collect(Collectors.toList());
        widgetComboBox.getItems().addAll(widgets);

        Callback<ListView<WidgetSelection>, ListCell<WidgetSelection>> cellFactory = buildCellFactory();
        //TODO JFX17

        widgetComboBox.setConverter(new StringConverter<WidgetSelection>() {
            @Override
            public String toString(WidgetSelection object) {
                return object.getDisplayname();
            }

            @Override
            public WidgetSelection fromString(String string) {
                return widgetComboBox.getItems().get(widgetComboBox.getSelectedIndex());
            }
        });
        widgetComboBox.getSelectionModel().selectFirst();

        Label labelType = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.new.type"));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);


        newB.setOnAction(event -> {
            selectedWidgetProperty.setValue(getSelectedWidget());
        });

        newB.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.add")));


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

    /*
    @TODO
     */
    public MFXButton getNewB() {
        this.getChildren().remove(newB);
        return newB;
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
