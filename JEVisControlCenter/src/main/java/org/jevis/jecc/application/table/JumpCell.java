package org.jevis.jecc.application.table;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.application.control.AnalysisLinkButton;
import org.jevis.jecc.dialog.EnterDataDialog;
import org.jevis.jecc.plugin.meters.data.SampleData;
import org.jevis.jecc.plugin.meters.ui.MeterTable;

public class JumpCell<T> implements Callback<TableColumn<T, SampleData>, TableCell<T, SampleData>> {
    private static final Logger logger = LogManager.getLogger(MeterTable.class);
    JEVisDataSource ds;

    public JumpCell(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public TableCell<T, SampleData> call(TableColumn<T, SampleData> param) {
        return new TableCell<T, SampleData>() {
            final HBox hBox = new HBox();

            final Button manSampleButton = new Button("", ControlCenter.getSVGImage(Icon.MANUAL_DATA_ENTRY, 20, 20));
            Button analysisLinkButton = new Button("", ControlCenter.getSVGImage(Icon.GRAPH, 20, 20));

            @Override
            protected void updateItem(SampleData item, boolean empty) {
                super.updateItem(item, empty);
                hBox.setAlignment(Pos.CENTER);
                hBox.getChildren().setAll(manSampleButton, analysisLinkButton);


                try {
                    if (item != null && item.getOptionalJEVisSample().isPresent()) {
                        TargetHelper th = new TargetHelper(ds, item.getOptionalJEVisSample().get().getAttribute());
                        JEVisObject firstCleanObject = CommonMethods.getFirstCleanObject(th.getObject().get(0));
                        analysisLinkButton = new AnalysisLinkButton(ControlCenter.getSVGImage(Icon.GRAPH, 20, 20), firstCleanObject.getAttribute("Value"));
                        hBox.getChildren().setAll(manSampleButton, analysisLinkButton);
                        manSampleButton.setOnAction(event -> {

                            if (th.isValid() && th.targetObjectAccessible()) {

                                EnterDataDialog enterDataDialog = new EnterDataDialog(ds);
                                enterDataDialog.setShowDetailedTarget(false);

                                if (th.isAttribute()) {
                                    enterDataDialog.setTarget(false, th.getAttribute().get(0));
                                } else {
                                    JEVisAttribute attribute = null;
                                    try {
                                        attribute = th.getObject().get(0).getAttribute("Value");
                                    } catch (JEVisException e) {
                                        logger.error(e);
                                        throw new RuntimeException(e);
                                    }
                                    if (attribute != null) {
                                        enterDataDialog.setTarget(false, attribute);
                                    } else {
                                        // logger.warn("No attribute target found");
                                    }
                                }

                                if (!th.getAttribute().isEmpty()) {
                                    JEVisSample lastValue = th.getAttribute().get(0).getLatestSample();
                                    enterDataDialog.setSample(lastValue);
                                }

                                enterDataDialog.setShowValuePrompt(true);

                                enterDataDialog.show();
                            }


                        });

                    }


                } catch (Exception ex) {
                    logger.error(ex);
                }


                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else if (!item.getOptionalJEVisSample().isPresent()) {
                    analysisLinkButton.setDisable(true);
                    manSampleButton.setDisable(true);
                    setGraphic(hBox);
                } else {
                    manSampleButton.setDisable(false);
                    analysisLinkButton.setDisable(false);
                    setGraphic(hBox);
                }
            }
        };
    }

}