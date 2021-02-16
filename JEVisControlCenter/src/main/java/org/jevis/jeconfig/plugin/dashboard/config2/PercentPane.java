package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.widget.ValueWidget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.Optional;

public class PercentPane extends GridPane {
    private static final Logger logger = LogManager.getLogger(PercentPane.class);


    private final Label sourceLabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.source"));
    private final Percent percent;
    private final ObservableList<Widget> widgetList;
    private JFXComboBox<Widget> widgetBox;


    public PercentPane(Percent percent, ObservableList<Widget> widgetList) {
        this.percent = percent;
        this.widgetList = widgetList;
        initControls();

        setPadding(new Insets(10));
        setVgap(8);
        setHgap(8);

        addRow(0, sourceLabel, widgetBox);
    }

    private void initControls() {

        widgetBox = new JFXComboBox<>(
                widgetList.filtered(widget -> widget.typeID().equals(ValueWidget.WIDGET_ID)));
        Callback<ListView<Widget>, ListCell<Widget>> cellFactory = new Callback<ListView<Widget>, ListCell<Widget>>() {
            @Override
            public ListCell<Widget> call(ListView<Widget> param) {
                return new ListCell<Widget>() {
                    @Override
                    protected void updateItem(Widget item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            try {
                                ValueWidget widget = ((ValueWidget) item);
                                String title = item.getConfig().getTitle().isEmpty()
                                        ? I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.list.nottitle")
                                        : item.getConfig().getTitle();

                                setText(String.format("[%d] '%s' | %.2f",
                                        item.getConfig().getUuid(),
                                        title,
                                        widget.getDisplayedSampleProperty().getValue()));
                            } catch (Exception ex) {
                                logger.error(ex);
                                setText(item.toString());
                            }
                        }
                    }
                };
            }
        };
        widgetBox.setButtonCell(cellFactory.call(null));
        widgetBox.setCellFactory(cellFactory);
        if (percent.percentWidget > 0) {
            Optional<Widget> widget = widgetList.stream()
                    .filter(widget1 -> widget1.getConfig().getUuid() == percent.percentWidget)
                    .findFirst();
            if (widget.isPresent()) {
                widgetBox.getSelectionModel().select(widget.get());
            }
        }

        widgetBox.setOnAction(event -> {
            if (widgetBox.getSelectionModel().getSelectedItem() != null) {
                percent.percentWidget = widgetBox.getSelectionModel().getSelectedItem().getConfig().getUuid();
            }
        });


    }

}
