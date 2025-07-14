package org.jevis.jeconfig.plugin.meters.cells;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.plugin.meters.event.PrecisionEvent;
import org.jevis.jeconfig.plugin.meters.event.PrecisionEventHandler;

public class LastRawValueCell<T> implements Callback<TableColumn<T, LastRawValuePojo>, TableCell<T, LastRawValuePojo>> {
    private static final Logger logger = LogManager.getLogger(LastRawValueCell.class);

    private final PrecisionEventHandler precisionEventHandler;

    public LastRawValueCell(PrecisionEventHandler precisionEventHandler) {
        this.precisionEventHandler = precisionEventHandler;
    }


    @Override
    public TableCell<T, LastRawValuePojo> call(TableColumn<T, LastRawValuePojo> tMeterDataTableColumn) {

        TableCell<T, LastRawValuePojo> tableCell = new TableCell<T, LastRawValuePojo>() {
            @Override
            protected void updateItem(LastRawValuePojo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    logger.debug(item);
                    String content = getString(item.getValue(), item.getPrecision(), item.getUnitLabel());

                    JFXButton manSampleButton = new JFXButton("", JEConfig.getImage("if_textfield_add_64870.png", 18, 18));
                    manSampleButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.mansample")));

                    final Label label = new Label(content);
                    label.setAlignment(Pos.CENTER_LEFT);
                    VBox textVBox = new VBox(label);
                    textVBox.setAlignment(Pos.CENTER);
                    Region spacer = new Region();

                    HBox hBox = new HBox(8, textVBox, spacer, manSampleButton);
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    setGraphic(hBox);

                    addEventManSampleAction(item.getTargetAttribute(), manSampleButton);

                    precisionEventHandler.addEventListener(event -> {
                        PrecisionEvent precisionEvent = event;
                        if (precisionEvent.getSource().equals(item.getMeterData())) {
                            if (precisionEvent.getType().equals(PrecisionEvent.TYPE.INCREASE)) {
                                item.setPrecision(item.getPrecision() + 1);
                            } else if (precisionEvent.getType().equals(PrecisionEvent.TYPE.DECREASE)) {
                                item.setPrecision(item.getPrecision() - 1);
                            }
                        }
                        label.setText(getString(item.getValue(), item.getPrecision(), item.getUnitLabel()));
                    });
                }


            }
        };

        return tableCell;


    }

    private String getString(double value, int precision, String unitLabel) {
        return (String.format("%.0" + precision + "f", value) + " " + unitLabel);
    }

    protected void addEventManSampleAction(JEVisAttribute targetAttribute, JFXButton buttonToAddEvent) {

        buttonToAddEvent.setOnAction(event -> {
            if (targetAttribute != null) {
                try {
                    EnterDataDialog enterDataDialog = new EnterDataDialog(targetAttribute.getDataSource());
                    enterDataDialog.setShowDetailedTarget(false);
                    enterDataDialog.setTarget(false, targetAttribute);

                    enterDataDialog.setShowValuePrompt(true);

                    enterDataDialog.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
