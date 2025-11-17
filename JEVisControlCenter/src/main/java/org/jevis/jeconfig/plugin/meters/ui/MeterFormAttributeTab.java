package org.jevis.jeconfig.plugin.meters.ui;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.constants.GUIConstants;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.meters.data.MeterData;
import org.jevis.jeconfig.plugin.meters.data.SampleData;
import org.jevis.jeconfig.plugin.object.attribute.GPSEditor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.jevis.commons.constants.GUIConstants.GPS;

public class MeterFormAttributeTab extends Tab implements MeterFormTab {

    private static final Logger logger = LogManager.getLogger(MeterForm.class);
    private final MeterData meterData;
    private final JEVisDataSource ds;
    private final JEVisTypeWrapper targetType;

    private final DateTime commitDateTime;
    private final Map<Integer, Node> fields = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (Objects.equals(o1, o2)) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    });
    GridPane gridPane = new GridPane();
    Map<JEVisType, JEVisSample> newSamples = new HashMap<>();


    public MeterFormAttributeTab(MeterData meterData, JEVisDataSource ds, String name) {
        super(name);
        this.meterData = meterData;
        this.ds = ds;
        this.targetType = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_OnlineID));
        this.commitDateTime = DateTime.now();

        gridPane.setPadding(new Insets(6, 6, 6, 6));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        for (Map.Entry<JEVisTypeWrapper, SampleData> entry : meterData.getJeVisAttributeJEVisSampleMap().entrySet()) {
            JEVisType jeVisType = entry.getKey().getJeVisType();

            try {
                if (jeVisType.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE && !jeVisType.getGUIDisplayType().equals(GPS.getId())) {
                    buildFileChooser(meterData, jeVisType, entry.getValue().getOptionalJEVisSample());
                } else if (jeVisType.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE && jeVisType.getGUIDisplayType().equals(GPS.getId())) {
                    GPSEditor gpsEditor = new GPSEditor(entry.getValue().getJeVisAttribute());
                    gpsEditor.getValueChangedProperty().addListener((observableValue, aBoolean, t1) -> {
                        {
                            if (t1) {
                                try {
                                    newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, gpsEditor.getNewFile()));
                                } catch (JEVisException | IOException e) {
                                    logger.error(e);
                                }
                                gpsEditor.setChanged(false);
                            }
                        }
                    });
                    fields.put(getGuiPosition(jeVisType), gpsEditor.getEditor());
                } else if (jeVisType.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || jeVisType.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                    buildCal(meterData, jeVisType, entry.getValue().getOptionalJEVisSample());
                } else if (jeVisType.getName().equals("Online ID")) {
                    buildTargetSelect(meterData, jeVisType, entry.getValue().getOptionalJEVisSample());
                } else if (jeVisType.getName().equals("Remarks")) {
                    buildTextArea(meterData, jeVisType, entry.getValue().getOptionalJEVisSample());
                } else {
                    buildTextField(meterData, jeVisType, entry.getValue().getOptionalJEVisSample());
                }
            } catch (JEVisException e) {
                logger.error(e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
                alert.showAndWait();
            }
        }

        List<Map.Entry<Integer, Node>> listOfFields = new ArrayList<>(fields.entrySet());
        listOfFields.sort(Map.Entry.comparingByKey());

        boolean changedRow = false;
        int counter = 0;
        int row = 0;
        int col = 0;
        for (Map.Entry<Integer, Node> entryMap : listOfFields) {
            if (counter % 4 == 0) {
                row++;
                col = 0;
            }

            boolean isTextArea = false;
            if (entryMap.getValue() instanceof VBox) {
                VBox value = (VBox) entryMap.getValue();
                isTextArea = value.getChildren().stream().anyMatch(node -> node instanceof TextArea);
            }

            if (isTextArea) {
                row++;
                gridPane.add(entryMap.getValue(), 0, row, 4, 1);
                row++;
            } else if (changedRow) {
                gridPane.add(entryMap.getValue(), col, row, 1, 1);
                col++;
                changedRow = false;
            } else {
                gridPane.add(entryMap.getValue(), col, row, 1, 1);
                col++;
                changedRow = true;
            }

            counter++;
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        setContent(scrollPane);
    }

    public void commit() {
        try {
            for (Map.Entry<JEVisType, JEVisSample> entry : newSamples.entrySet()) {
                entry.getValue().commit();
            }
        } catch (JEVisException e) {
            logger.error(e);
        }

    }

    private JEVisType getJEVisType(String string) {
        try {
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
            JEVisType jeVisType = jeVisClass.getType(string);
            return jeVisType;

        } catch (Exception e) {
            logger.error(e);
        }
        return null;


    }

    private void buildFileChooser(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {

        Label fileName = new Label();
        Label label = new Label();
        JFXButton uploadButton = new JFXButton("", JEConfig.getSVGImage(Icon.CLOUD_UPLOAD, 18, 18));
        HBox hBox = new HBox(uploadButton, fileName);
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        try {

            fileName.setText(optionalJEVisSample.isPresent() ? optionalJEVisSample.get().getValueAsString() : "");
            label.setText(I18nWS.getInstance().getTypeName(jeVisType));

            uploadButton.setOnAction(actionEvent -> {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
                if (selectedFile != null) {
                    try {
                        JEConfig.setLastPath(selectedFile);
                        JEVisFile jfile = new JEVisFileImp(selectedFile.getName(), selectedFile);
                        fileName.setText(selectedFile.getName());
                        newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, jfile));
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

        Region region = new Region();
        VBox labelAlignBox = new VBox(label);
        HBox fieldBox = new HBox(labelAlignBox, region, hBox);
        HBox.setHgrow(region, Priority.ALWAYS);
        fieldBox.setPadding(new Insets(6));

        fields.put(getGuiPosition(jeVisType), fieldBox);

    }

    private int getGuiPosition(JEVisType jeVisType) {
        try {
            return jeVisType.getGUIPosition();
        } catch (JEVisException jeVisException) {
            logger.error(jeVisException);
        }
        return 0;
    }

    private void buildCal(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = new Label();
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        jfxDatePicker.getEditor().setAlignment(Pos.CENTER_RIGHT);
        JEVisObject jeVisObject = meterData.getJeVisObject();
        try {
            label.setText(I18nWS.getInstance().getTypeName(jeVisType));
            DateTime dateTime = DatabaseHelper.getObjectAsDate(jeVisObject, jeVisType);
            jfxDatePicker.setValue(toLocalDate(dateTime));
            jfxDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {

                try {
                    newSamples.put(jeVisType, jeVisObject.getAttribute(
                            jeVisType).buildSample(commitDateTime, toDateTime(t1)));
                } catch (JEVisException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not create Sample", ButtonType.OK);
                    alert.showAndWait();

                }
            });

        } catch (Exception e) {
            logger.error(e);
        }

        Region region = new Region();
        VBox labelAlignBox = new VBox(label);
        HBox fieldBox = new HBox(labelAlignBox, region, jfxDatePicker);
        HBox.setHgrow(region, Priority.ALWAYS);
        fieldBox.setPadding(new Insets(6));
        fields.put(getGuiPosition(jeVisType), fieldBox);
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

    public void buildTargetSelect(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {

        Label label = new Label();
        Label linkName = new Label();
        JFXButton jfxButton = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 18, 18));
        jfxButton.setAlignment(Pos.CENTER_RIGHT);

        try {
            label.setText(I18nWS.getInstance().getTypeName(jeVisType));

            TargetHelper th = null;
            try {
                th = new TargetHelper(ds, meterData.getJeVisObject().getAttribute(targetType.getJeVisType()));
            } catch (JEVisException e) {
                logger.error(e);
            }

            if (th.isValid() && th.targetObjectAccessible()) {
                logger.info("Target is valid");

                linkName.setText(th.getObject().get(0).getName());
            }
            TargetHelper finalTh = th;

            jfxButton.setOnAction(actionEvent -> {

                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataFilter();
                allFilter.add(allDataFilter);
                List<UserSelection> openList = new ArrayList<>();
                if (!finalTh.getObject().isEmpty()) {
                    for (JEVisObject obj : finalTh.getObject()) {
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
                                linkName.setText(us.getSelectedObject().getName());
                                if (us.getSelectedAttribute() != null) {
                                    newTarget += ":" + us.getSelectedAttribute().getName();

                                } else {
                                    newTarget += ":Value";
                                }
                            }
                            JEVisSample newTargetSample = meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, newTarget);
                            newSamples.put(jeVisType, newTargetSample);
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

        Region region = new Region();
        VBox labelAlignBox = new VBox(label);
        VBox linkNameAlignBox = new VBox(linkName);
        HBox fieldBox = new HBox(labelAlignBox, region, jfxButton, linkNameAlignBox);
        HBox.setHgrow(region, Priority.ALWAYS);
        fieldBox.setPadding(new Insets(6));
        fields.put(getGuiPosition(jeVisType), fieldBox);
    }

    private void buildTextArea(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = new Label();
        TextArea textArea = new TextArea();
        textArea.setPrefWidth(200);
        try {
            label.setText(I18nWS.getInstance().getTypeName(jeVisType));
            if (optionalJEVisSample.isPresent()) {
                textArea.setText(optionalJEVisSample.get().getValueAsString());
            }

            textArea.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = jeVisType.getPrimitiveType();
                } catch (JEVisException e) {
                    logger.error(e);
                    return;
                }
                if (t1.isEmpty()) {
                    try {
                        newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, ""));
                    } catch (Exception e) {
                        logger.error(e);
                    }
                } else
                    try {
                        switch (primitiveType) {
                            case JEVisConstants.PrimitiveType.STRING:
                                String str = t1;
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, str));
                                break;
                            case JEVisConstants.PrimitiveType.LONG:
                                Long l = Long.valueOf(t1);
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, l));
                                break;
                            case JEVisConstants.PrimitiveType.BOOLEAN:
                                Boolean b = Boolean.valueOf(t1);
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, b));
                                break;
                            case JEVisConstants.PrimitiveType.DOUBLE:
                                Double d = Double.valueOf(t1);
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, d));
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

        VBox fieldBox = new VBox(label, textArea);
        fieldBox.setPadding(new Insets(6));
        fields.put(getGuiPosition(jeVisType), fieldBox);
    }

    private void buildTextField(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = new Label();
        JFXTextField textField = new JFXTextField();
        textField.setPrefWidth(200);
        textField.setAlignment(Pos.CENTER_RIGHT);

        try {
            label.setText(I18nWS.getInstance().getTypeName(jeVisType));
            if (optionalJEVisSample.isPresent()) {
                textField.setText(optionalJEVisSample.get().getValueAsString());
            }

            textField.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = jeVisType.getPrimitiveType();
                } catch (Exception e) {
                    logger.error(e);
                    return;
                }
                if (t1.isEmpty()) {
                    try {
                        newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, ""));
                    } catch (Exception e) {
                        logger.error(e);
                    }
                } else
                    try {
                        switch (primitiveType) {
                            case JEVisConstants.PrimitiveType.STRING:
                                String str = t1;
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, str));
                                break;
                            case JEVisConstants.PrimitiveType.LONG:
                                Long l = Long.valueOf(t1);
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, l));
                                break;
                            case JEVisConstants.PrimitiveType.BOOLEAN:
                                Boolean b = Boolean.valueOf(t1);
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, b));
                                break;
                            case JEVisConstants.PrimitiveType.DOUBLE:
                                Double d = Double.valueOf(t1);
                                newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(commitDateTime, d));
                                break;
                        }
                    } catch (NumberFormatException e) {
                        logger.error(e);
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Number Format Mismatch", ButtonType.OK);
                        alert.showAndWait();
                    } catch (Exception e) {
                        logger.error(e);
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Exception: ", ButtonType.OK);
                        alert.showAndWait();
                    }
            });


        } catch (Exception e) {
            logger.error(e);
            return;
        }

        Region region = new Region();
        VBox labelAlignBox = new VBox(label);
        HBox fieldBox = new HBox(labelAlignBox, region, textField);
        HBox.setHgrow(region, Priority.ALWAYS);
        fieldBox.setPadding(new Insets(6));
        fields.put(getGuiPosition(jeVisType), fieldBox);
    }

}
