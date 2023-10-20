package org.jevis.jeconfig.plugin.metersv2.ui;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.data.SampleData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class MeterFormAttributeTab extends Tab implements MeterFormTab {

    private static final Logger logger = LogManager.getLogger(MeterForm.class);
    private final MeterData meterData;
    private final JEVisDataSource ds;
    private final JEVisTypeWrapper targetType;

    private final DateTime commitDateTime;
    private final Map<Integer, List<Node>> fields = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (o1 == o2) {
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


        long startTime = System.nanoTime();


        gridPane.setVgap(10);
        gridPane.setHgap(10);


        initializeMap();


        for (Map.Entry<JEVisTypeWrapper, SampleData> entry : meterData.getJeVisAttributeJEVisSampleMap().entrySet()) {
            JEVisType jeVisType = entry.getKey().getJeVisType();

            try {
                if (jeVisType.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                    buildFileChooser(meterData, jeVisType, entry.getValue().getOptionalJEVisSample());
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


        boolean firstRow = true;
        for (Map.Entry<Integer, List<Node>> entryMap : fields.entrySet()) {
            int rowcount = getMaxRow();

            if (entryMap.getValue().get(1) instanceof TextArea) {
                gridPane.add(entryMap.getValue().get(0), 0, ++rowcount, 1, 1);
                gridPane.add(entryMap.getValue().get(1), 0, ++rowcount, 4, 1);
                firstRow = true;
                continue;
            }

            if (entryMap.getKey() % 10 == 0 && entryMap.getKey() != 0) {
                gridPane.add(new Separator(), 0, rowcount + 1, 4, 1);
                rowcount++;
                firstRow = true;
            }
            if (firstRow) {
                gridPane.addRow(rowcount + 1, entryMap.getValue().get(0), entryMap.getValue().get(1));
                firstRow = false;
            } else {
                gridPane.addRow(rowcount, entryMap.getValue().get(0), entryMap.getValue().get(1));
                firstRow = true;
            }
        }


        setContent(gridPane);


    }


    private int getMaxRow() {
        int rowcount = gridPane.getChildren().stream().mapToInt(value -> {
            Integer row = GridPane.getRowIndex(value);
            Integer rowSpan = GridPane.getRowSpan(value);
            return (row == null ? 0 : row) + (rowSpan == null ? 0 : rowSpan - 1);
        }).max().orElse(-1);
        return rowcount;
    }

    @NotNull
    private TargetHelper createTargetHelper() {
        try {
            JEVisAttribute onlineIdAttribute = meterData.getJeVisObject().getAttribute(targetType.getJeVisType());
            TargetHelper targetHelper = new TargetHelper(ds, onlineIdAttribute);
            return targetHelper;

        } catch (Exception e) {
            return null;
        }
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

    private void setVisibility(List<Node> nodes, boolean visible) {
        nodes.forEach(node -> {
            node.setVisible(visible);
        });
    }

    private void initializeMap() {

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


        Label label = null;
        HBox hBox = null;
        JFXButton uploadButton = null;

        try {
            Label fileName = new Label();
            fileName.setText(optionalJEVisSample.isPresent() ? optionalJEVisSample.get().getValueAsString() : "");
            uploadButton = new JFXButton("", JEConfig.getSVGImage(Icon.CLOUD_UPLOAD, 18, 18));
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
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
        fields.put(getGuiPosition(jeVisType), Arrays.asList(label, hBox));

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
        Label label = null;
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        JEVisObject jeVisObject = meterData.getJeVisObject();
        try {
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
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

        fields.put(getGuiPosition(jeVisType), Arrays.asList(label, jfxDatePicker));


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

        Label label = null;
        JFXButton jfxButton = null;
        try {
            jfxButton = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 18, 18));
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));

            JEVisSample latestSample = meterData.getJeVisObject().getAttribute(jeVisType).getLatestSample();

            jfxButton.setOnAction(actionEvent -> {
                TargetHelper th = null;
                try {
                    th = new TargetHelper(ds, meterData.getJeVisObject().getAttribute(targetType.getJeVisType()));
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
        fields.put(getGuiPosition(jeVisType), Arrays.asList(label, jfxButton));
    }

    private void buildTextArea(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = null;
        TextArea textArea = null;
        try {
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
            textArea = optionalJEVisSample.isPresent() ? new TextArea(optionalJEVisSample.get().getValueAsString()) : new TextArea();
            textArea.setPrefWidth(200);

            textArea.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = jeVisType.getPrimitiveType();
                } catch (JEVisException e) {
                    logger.error(e);
                    return;
                }
                if (t1.isEmpty()) return;


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
        fields.put(getGuiPosition(jeVisType), Arrays.asList(label, textArea));
    }

    private void buildTextField(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = null;
        JFXTextField textField = null;
        try {
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
            textField = optionalJEVisSample.isPresent() ? new JFXTextField(optionalJEVisSample.get().getValueAsString()) : new JFXTextField();
            textField.setPrefWidth(200);

            textField.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = jeVisType.getPrimitiveType();
                } catch (JEVisException e) {
                    logger.error(e);
                    return;
                }
                if (t1.isEmpty()) return;


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
        fields.put(getGuiPosition(jeVisType), Arrays.asList(label, textField));
    }

}
