package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class MeterForm extends Dialog {

    private MeterData meterData;

    ScrollPane scrollPane = new ScrollPane();

    GridPane gridPane = new GridPane();

    private JEVisDataSource ds;



    private  Map<String,Map<Label,Node>> fields =new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2== null) {
                return -1;
            }
            return o2.compareTo(o1);
        }
    });


Map<Label, Node> textFields = new TreeMap<>(new Comparator<Label>() {
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
        initializeMap();

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);


        for (Map.Entry<JEVisTypeWrapper, Optional<JEVisSample>> entry : meterData.getJeVisAttributeJEVisSampleMap().entrySet()) {
            JEVisType jeVisType =entry.getKey().getJeVisType();

            try {
                if (jeVisType.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                    buildFileChooser(meterData, jeVisType,entry.getValue());
                } else if (jeVisType.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || jeVisType.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                    buildCal(meterData, jeVisType,entry.getValue());
                } else if (jeVisType.getName().equals("Online ID")) {
                    buildTargetSelect(meterData,jeVisType,entry.getValue());
                } else {
                    buildTextField(meterData, jeVisType,entry.getValue());
                }
            } catch (JEVisException e) {
                logger.error(e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
                alert.showAndWait();
            }

        }


        boolean firstRow =true;
        for(Map.Entry<String, Map<Label, Node>> entryMap : fields.entrySet()){
            for(Map.Entry<Label, Node> entry : entryMap.getValue().entrySet()){
                int rowcount = gridPane.getChildren().stream().mapToInt(value -> {
                    Integer row = GridPane.getRowIndex(value);
                    Integer rowSpan = GridPane.getRowSpan(value);
                    return (row == null ? 0 : row) + (rowSpan == null ? 0 : rowSpan - 1);
                }).max().orElse(-1);
                if(firstRow){
                    gridPane.addRow(rowcount + 1, entry.getKey(), entry.getValue());
                    firstRow = false;
                }else {
                    gridPane.addRow(rowcount, entry.getKey(), entry.getValue());
                    firstRow = true;
                }
            }
        }

        gridPane.setVgap(10);
        gridPane.setHgap(10);

        scrollPane.setContent(gridPane);

        getDialogPane().setContent(scrollPane);


    }

    private void initializeMap() {
        fields.put("Text",new TreeMap<>(new Comparator<Label>() {
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
        }));

        fields.put("Calendar",new TreeMap<>(new Comparator<Label>() {
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
        }));

        fields.put("File",new TreeMap<>(new Comparator<Label>() {
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
        }));

        fields.put("Target",new TreeMap<>(new Comparator<Label>() {
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
        }));
    }

    private void buildTextField(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = null;
        TextField textField = null;
        try {
            label = new Label(jeVisType.getName());
            textField = optionalJEVisSample.isPresent() ? new TextField(optionalJEVisSample.get().getValueAsString()) : new TextField();

            textField.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = jeVisType.getPrimitiveType();
                } catch (JEVisException e) {
                    logger.error(e);
                    return;
                }


                try {
                    switch (primitiveType) {
                        case JEVisConstants.PrimitiveType.STRING:
                            String str = s;
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), str));
                            break;
                        case JEVisConstants.PrimitiveType.LONG:
                            Long l = Long.valueOf(s);
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), l));
                            break;
                        case JEVisConstants.PrimitiveType.BOOLEAN:
                            Boolean b = Boolean.valueOf(s);
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), b));
                            break;
                        case JEVisConstants.PrimitiveType.DOUBLE:
                            Double d = Double.valueOf(s);
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), d));
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
        fields.get("Text").put(label,textField);
    }

    private void buildFileChooser(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {


        Label label = null;
        HBox hBox = null;
        JFXButton uploadButton = null;

        try {
            Label fileName = new Label();
            fileName.setText(optionalJEVisSample.isPresent() ? optionalJEVisSample.get().getValueAsFile().getFilename() : "");
            uploadButton = new JFXButton("", JEConfig.getSVGImage(Icon.CLOUD_UPLOAD, 20, 20));
            label = new Label(jeVisType.getName());
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
                        newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), jfile));
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
        fields.get("File").put(label,hBox);

    }

    private void buildCal(MeterData meterData,JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = null;
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        JEVisObject jeVisObject = meterData.getJeVisObject();
        try {
            label = new Label(jeVisType.getName());
            DateTime dateTime = DatabaseHelper.getObjectAsDate(jeVisObject, jeVisType);
//            System.out.println(DatabaseHelper.getObjectAsLocaleDate(jeVisObject, entry.getKey()).toLocalDate());
            jfxDatePicker.setValue(toLocalDate(dateTime));
            jfxDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {

                try {
                    newSamples.put(jeVisType, jeVisObject.getAttribute(
                            jeVisType).buildSample(DateTime.now(), toDateTime(localDate)));
                    System.out.println(newSamples.get(jeVisType));
                } catch (JEVisException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not create Sample", ButtonType.OK);
                    alert.showAndWait();

                }
            });

        } catch (Exception e) {
            logger.error(e);
        }

        fields.get("Calendar").put(label,jfxDatePicker);


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

    public void buildTargetSelect(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {

        Label label = null;
        JFXButton jfxButton = null;
        try {
            jfxButton = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 20, 20));
            label = new Label(jeVisType.getName());

            JEVisSample latestSample = meterData.getJeVisObject().getAttribute(jeVisType).getLatestSample();

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
                            JEVisSample newTargetSample = meterData.getJeVisObject().getAttribute(jeVisType).buildSample(new DateTime(), newTarget);
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
        fields.get("Target").put(label,jfxButton);
    }
}
