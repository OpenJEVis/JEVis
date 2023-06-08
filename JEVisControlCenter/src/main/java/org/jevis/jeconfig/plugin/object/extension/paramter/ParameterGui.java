package org.jevis.jeconfig.plugin.object.extension.paramter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.driver.Parameter;
import org.jevis.commons.gson.GsonBuilder;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ParameterGui {

    private static final Logger logger = LogManager.getLogger(ParameterGui.class);

    private  Gson gson = GsonBuilder.createDefaultBuilder().create();

    private  Type parameterListType = new TypeToken<List<Parameter>>(){}.getType();

     private ObservableList<Parameter> parameters = FXCollections.observableArrayList();

     private String path;



    public Node buildGui(JEVisSample pathSample, JEVisSample parameterSample, String textOfTextFiled) {
        GridPane gridPane = new GridPane();
        JFXTextField textFieldPath = new JFXTextField();



        try {


            path = getPath(pathSample);

            JEVisFile jeVisFileParam = getFile(parameterSample);



            ColumnConstraints columnConstraints1 = new ColumnConstraints();
            columnConstraints1.setPrefWidth(100);

            ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints2.setPrefWidth(600);

            ColumnConstraints columnConstraints3 = new ColumnConstraints();
            columnConstraints3.setPrefWidth(100);

            gridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints2, columnConstraints3);

            textFieldPath.setText(path);

            textFieldPath.textProperty().addListener((observableValue, s, t1) -> {
                path = t1;
            });
            gridPane.addRow(0, new Label(textOfTextFiled), textFieldPath);
            ParameterTable parameterTable = new ParameterTable();
            JFXButton add = new JFXButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, 20, 20));
            JFXButton remove = new JFXButton("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 20, 20));
            add.setOnAction(actionEvent -> {
                parameters.add(new Parameter());
            });
            remove.setOnAction(actionEvent -> {
                parameters.remove(parameterTable.getSelectionModel().getSelectedItem());
            });



            gridPane.addRow(1,new HBox(add,remove));

            parameterTable.setItems(parameters);
            gridPane.add(parameterTable, 0,2,3,3);
            loadParameterFromFile(jeVisFileParam);
        } catch (Exception e) {
            logger.error(e);
        }

        return gridPane;
    }

    public void loadParameterFromFile(JEVisFile jeVisFile) {
        String s = new String(jeVisFile.getBytes(), StandardCharsets.UTF_8);
        List<Parameter> parameters1 = gson.fromJson(s, parameterListType);
        parameters.setAll(parameters1);


    }

    private  String getPath(JEVisSample jeVisSample) {
        try {
            if (jeVisSample == null) return "";
            return jeVisSample.getValueAsString();

        } catch (Exception e) {
            logger.error(e);
        }
        return "";
    }

    public  String parametersToJson(ObservableList<Parameter> parameters) {
        return gson.toJson(parameters, parameterListType);
    }

    public  JEVisFileImp parametersToFile(ObservableList<Parameter> parameters) {
        JEVisFileImp jsonFile = null;
        try {
            jsonFile = new JEVisFileImp(
                    "ParameterConfig" + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                    , gson.toJson(parameters, parameterListType).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonFile;


    }
    private JEVisFile getFile(JEVisSample jeVisSample) {
        try {
            if (jeVisSample == null) return null;
            return jeVisSample.getValueAsFile();
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    private boolean savePath(JEVisAttribute jeVisAttribute) {
        try {
           JEVisSample jeVisSample = jeVisAttribute.buildSample(DateTime.now(), path);
           jeVisSample.commit();
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;

    }

    private boolean saveParameter(JEVisAttribute jeVisAttribute) {
        try {
            JEVisSample jeVisSample = jeVisAttribute.buildSample(DateTime.now(), parametersToFile(parameters));
            jeVisSample.commit();
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public boolean save(JEVisAttribute pathAttribute, JEVisAttribute parameterAttribute) {
      return  saveParameter(parameterAttribute) && savePath(pathAttribute) ? true : false;
    }


}
