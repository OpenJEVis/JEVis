package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeapi.ws.JEVisSampleWS;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.tool.DragResizeMod;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.xml.soap.Text;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MeterForm extends Dialog {

    private MeterData meterData;

    ScrollPane scrollPane = new ScrollPane();

    GridPane gridPane = new GridPane();

    private JEVisDataSource ds;
    Map<Label, Node> fields = new TreeMap<>(new Comparator<Label>() {
        @Override
        public int compare(Label o1, Label o2) {
            if (o1.getText() == o2.getText()) {
                return 0;
            }
            if (o1.getText() == null) {
                return -1;
            }
            if (o2.getText() == null) {
                return 1;
            }
            return o1.getText().compareTo(o2.getText());
        }
    });
    Map<JEVisType, JEVisSample> newSamples = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(MeterForm.class);

    public MeterForm(MeterData meterData, JEVisDataSource ds) {
        this.ds = ds;
        this.meterData = meterData;

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);


        for (Map.Entry<JEVisType, Optional<JEVisSample>> entry : meterData.getJeVisAttributeJEVisSampleMap().entrySet()) {

            try {
                if (entry.getKey().getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                    buildFileChooser(meterData, entry);
                } else if (entry.getKey().getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || entry.getKey().getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                    buildCal(meterData, entry);
                } else if (entry.getKey().getName().equals("Online ID")) {
                    buildTargetSelect(meterData,entry);
                } else {
                    buildTextField(meterData, entry);
                }
            } catch (JEVisException e) {
                logger.error(e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
                alert.showAndWait();
            }

        }


        for (Map.Entry<Label, Node> entry : fields.entrySet()) {
            int rowcount = gridPane.getChildren().stream().mapToInt(value -> {
                Integer row = GridPane.getRowIndex(value);
                Integer rowSpan = GridPane.getRowSpan(value);
                return (row == null ? 0 : row) + (rowSpan == null ? 0 : rowSpan - 1);
            }).max().orElse(-1);
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
            gridPane.addRow(rowcount + 1, entry.getKey(), entry.getValue());
        }

        gridPane.setVgap(10);
        gridPane.setHgap(10);

        scrollPane.setContent(gridPane);

        getDialogPane().setContent(scrollPane);


    }

    private void buildTextField(MeterData meterData, Map.Entry<JEVisType, Optional<JEVisSample>> entry) {
        Label label = null;
        TextField textField = null;
        try {
            label = new Label(entry.getKey().getName());
            textField = entry.getValue().isPresent() ? new TextField(entry.getValue().get().getValueAsString()) : new TextField();

            textField.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = entry.getKey().getPrimitiveType();
                } catch (JEVisException e) {
                    logger.error(e);
                    return;
                }


                try {
                    switch (primitiveType) {
                        case JEVisConstants.PrimitiveType.STRING:
                            String str = s;
                            newSamples.put(entry.getKey(), meterData.getJeVisObject().getAttribute(entry.getKey()).buildSample(DateTime.now(), str));
                            break;
                        case JEVisConstants.PrimitiveType.LONG:
                            Long l = Long.valueOf(s);
                            newSamples.put(entry.getKey(), meterData.getJeVisObject().getAttribute(entry.getKey()).buildSample(DateTime.now(), l));
                            break;
                        case JEVisConstants.PrimitiveType.BOOLEAN:
                            Boolean b = Boolean.valueOf(s);
                            newSamples.put(entry.getKey(), meterData.getJeVisObject().getAttribute(entry.getKey()).buildSample(DateTime.now(), b));
                            break;
                        case JEVisConstants.PrimitiveType.DOUBLE:
                            Double d = Double.valueOf(s);
                            newSamples.put(entry.getKey(), meterData.getJeVisObject().getAttribute(entry.getKey()).buildSample(DateTime.now(), d));
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Number Format Mismatch", ButtonType.OK);
                    alert.showAndWait();
                } catch (JEVisException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
                    alert.showAndWait();
                }

            });


        } catch (JEVisException e) {
            logger.error(e);
            return;
        }

        fields.put(label, textField);
    }

    private void buildFileChooser(MeterData meterData, Map.Entry<JEVisType, Optional<JEVisSample>> entry) {


        Label label = null;
        HBox hBox = null;
        JFXButton uploadButton = null;

        try {
            Label fileName = new Label();
            fileName.setText(entry.getValue().isPresent() ? entry.getValue().get().getValueAsFile().getFilename() : "");
            uploadButton = new JFXButton("", JEConfig.getSVGImage(Icon.CLOUD_UPLOAD, 20, 20));
            label = new Label(entry.getKey().getName());
            hBox = new HBox(uploadButton, fileName);
            hBox.setSpacing(5);


            uploadButton.setOnAction(actionEvent -> {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
                if (selectedFile != null) {
                    try {
                        JEConfig.setLastPath(selectedFile);
                        JEVisFile jfile = new JEVisFileImp(selectedFile.getName(), selectedFile);
                        fileName.setText(selectedFile.getName());
                        newSamples.put(entry.getKey(), meterData.getJeVisObject().getAttribute(entry.getKey()).buildSample(DateTime.now(), jfile));
                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                }
            });
        } catch (JEVisException e) {
            logger.error(e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
            alert.showAndWait();
        }
        fields.put(label, hBox);

    }

    private void buildCal(MeterData meterData, Map.Entry<JEVisType, Optional<JEVisSample>> entry) {
        Label label = null;
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        JEVisObject jeVisObject = meterData.getJeVisObject();
        try {
            label = new Label(entry.getKey().getName());
            DateTime dateTime = DatabaseHelper.getObjectAsDate(jeVisObject, entry.getKey());
//            System.out.println(DatabaseHelper.getObjectAsLocaleDate(jeVisObject, entry.getKey()).toLocalDate());
            jfxDatePicker.setValue(toLocalDate(dateTime));
            jfxDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {

                try {
                    newSamples.put(entry.getKey(), jeVisObject.getAttribute(
                            entry.getKey()).buildSample(DateTime.now(), toDateTime(localDate)));
                    System.out.println(newSamples.get(entry.getKey()));
                } catch (JEVisException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not create Sample", ButtonType.OK);
                    alert.showAndWait();

                }
            });

        } catch (Exception e) {
            logger.error(e);
        }

        fields.put(label, jfxDatePicker);


    }

    public Map<JEVisType, JEVisSample> getNewSamples() {
        return newSamples;
    }

    public void setNewSamples(Map<JEVisType, JEVisSample> newSamples) {
        this.newSamples = newSamples;
    }

    private LocalDate toLocalDate(DateTime dateTime) {
        DateTime dateTimeUtc = dateTime.withZone(DateTimeZone.UTC);
        return LocalDate.of(dateTimeUtc.getYear(), dateTimeUtc.getMonthOfYear(), dateTimeUtc.getDayOfMonth());
    }

    public DateTime toDateTime(LocalDate localDate) {
        return new DateTime(DateTimeZone.UTC).withDate(
                localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
        ).withTime(0, 0, 0, 0);
    }

    public void buildTargetSelect(MeterData meterData, Map.Entry<JEVisType, Optional<JEVisSample>> entry) {

        Label label = null;
        JFXButton jfxButton = null;
        try {
            jfxButton = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 20, 20));
            label = new Label(entry.getKey().getName());

            JEVisSample latestSample = meterData.getJeVisObject().getAttribute(entry.getKey()).getLatestSample();

            jfxButton.setOnAction(actionEvent -> {
                TargetHelper th = null;
                try {
                    th = new TargetHelper(ds, latestSample.getValueAsString());
                } catch (JEVisException e) {
                    logger.error(e);
                }

                if (th.isValid() && th.targetObjectAccessible()) {
                    logger.info("Target Is valid");
                }
                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataFilter();
                allFilter.add(allDataFilter);
                List<UserSelection> openList = new ArrayList<>();
                if (th != null && !th.getObject().isEmpty()) {
                    for (JEVisObject obj : th.getObject()) {
                        openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                    }


                }
                SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.SINGLE, ds, openList);
                selectTargetDialog.setOnCloseRequest(event1 -> {
                    try {
                        if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                            logger.trace("Selection Done");

                            String newTarget = "";
                            List<UserSelection> selections = selectTargetDialog.getUserSelection();
                            for (UserSelection us : selections) {
                                int index = selections.indexOf(us);
                                if (index > 0) newTarget += ";";

                                newTarget += us.getSelectedObject().getID();
                                if (us.getSelectedAttribute() != null) {
                                    newTarget += ":" + us.getSelectedAttribute().getName();

                                } else {
                                    newTarget += ":Value";
                                }
                            }
                            JEVisSample newTargetSample = meterData.getJeVisObject().getAttribute(entry.getKey()).buildSample(new DateTime(), newTarget);
                            newSamples.put(entry.getKey(), newTargetSample);
                        }

                    } catch (JEVisException e) {
                        logger.error(e);
                        Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis Exception", ButtonType.OK);
                        alert.showAndWait();
                    }
                });
                selectTargetDialog.showAndWait();
            });

        } catch (Exception e) {
            logger.error(e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis Exception", ButtonType.OK);
            alert.showAndWait();
        }
        fields.put(label, jfxButton);
    }
}
