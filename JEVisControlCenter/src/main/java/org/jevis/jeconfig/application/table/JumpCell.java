package org.jevis.jeconfig.application.table;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.AnalysisLinkButton;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterPlanTable;
import org.joda.time.DateTime;

import java.util.Optional;

public class JumpCell<T> implements Callback<TableColumn<T, Optional<JEVisSample>>, TableCell<T, Optional<JEVisSample>>> {
    private static final Logger logger = LogManager.getLogger(MeterPlanTable.class);
    JEVisDataSource ds;
    public JumpCell(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public TableCell<T, Optional<JEVisSample>> call(TableColumn<T, Optional<JEVisSample>> param) {
        return new TableCell<T, Optional<JEVisSample>>() {
            @Override
            protected void updateItem(Optional<JEVisSample> item, boolean empty) {
                super.updateItem(item, empty);
                HBox hBox = new HBox();
                try {
                TargetHelper th = new TargetHelper(ds,item.get().getAttribute());
                JFXButton manSampleButton = new JFXButton("", JEConfig.getSVGImage(Icon.MANUAL_DATA_ENTRY,20,20));
                AnalysisLinkButton analysisLinkButton = null;
                if (item.isPresent()) {
                    JEVisObject firstCleanObject = CommonMethods.getFirstCleanObject(th.getObject().get(0));
                    analysisLinkButton = new AnalysisLinkButton(JEConfig.getSVGImage(Icon.GRAPH,20,20),firstCleanObject.getAttribute("Value"));
                }

                hBox.getChildren().addAll(manSampleButton, analysisLinkButton);

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

                } catch (Exception ex) {
                    logger.error(ex);
                }


                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(hBox);
                }
            }
        };
    }

};