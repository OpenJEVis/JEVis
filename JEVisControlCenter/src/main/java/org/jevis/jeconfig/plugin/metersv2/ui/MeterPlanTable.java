package org.jevis.jeconfig.plugin.metersv2.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.application.table.FileCell;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.plugin.metersv2.cells.*;
import org.jevis.jeconfig.plugin.metersv2.data.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class MeterPlanTable extends TableView<MeterData> {

    private JEVisDataSource ds;

    private static final Logger logger = LogManager.getLogger(MeterPlanTable.class);


    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    ObservableList<NonconformityData> data = FXCollections.observableArrayList();
    FilteredList<MeterData> filteredData;

    SortedList<MeterData> sortedData;
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    private NonconformityData sumRow = new NonconformityData();
    //private DateFilter dateFilter;
    private boolean showSumRow = false;
    private String containsTextFilter = "";
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> type = FXCollections.observableArrayList();
    private ObservableList<String> staus = FXCollections.observableArrayList();
    private ObservableList<String> fields = FXCollections.observableArrayList();
    private ObservableList<String> seu = FXCollections.observableArrayList();

    private static int DATE_TIME_WIDTH = 120;
    private static int BIG_WIDTH = 200;
    private static int SMALL_WIDTH = 60;




    public MeterPlanTable(MeterPlan meterPlan, ObservableList<MeterData> data, JEVisDataSource ds) {
        this.ds = ds;
        this.filteredData = new FilteredList<>(data);

        this.setTableMenuButtonVisible(true);

        this.getColumns().add(new MediumColumn("Medium",BIG_WIDTH));
        JEVisType onlineIdType = null;
        JEVisType pointName = null;
        try {
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
            onlineIdType = jeVisClass.getType("Online ID");
            pointName = jeVisClass.getType(JC.MeasurementInstrument.a_MeasuringPointName);

        } catch (Exception e) {


        }


        List<JEVisType> jeVisTypes = data.stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().keySet()).flatMap(jeVisTypes1 -> jeVisTypes1.stream()).distinct().collect(Collectors.toList());
        try {
            for (JEVisType jeVisType : jeVisTypes) {

                TableColumn<MeterData, ?> col = null;
                switch (jeVisType.getPrimitiveType()) {
                    case JEVisConstants.PrimitiveType.LONG:
                        col = new ShortCellColumn(jeVisType, BIG_WIDTH, jeVisType.getName());
                        break;
                    case JEVisConstants.PrimitiveType.STRING:

                        if(jeVisType.equals(onlineIdType)){
                            col = new LastRawValue("Last Raw Value",ds,jeVisType,BIG_WIDTH);
                        }

                        else {
                            col = new ShortCellColumn(jeVisType, BIG_WIDTH, jeVisType.getName());
                            if (jeVisType.equals(pointName)) col.setVisible(true);


                        }
                        break;
                    case JEVisConstants.PrimitiveType.FILE:
                        col = new FileColumn(jeVisType,BIG_WIDTH,jeVisType.getName());
                        break;
                    default:

                        if ((jeVisType.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || jeVisType.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId()))) {
                            col = new DateColumn(jeVisType.getName(), jeVisType, BIG_WIDTH);
                        }else{

                            col = new ShortCellColumn(jeVisType, BIG_WIDTH, jeVisType.getName());
                        }
                        break;
                }
                if (col != null) {
                    this.getColumns().add(col);
                }
            }
            this.getColumns().add(new JumpColumn("Jump", onlineIdType, BIG_WIDTH,ds));


        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.comparatorProperty());
        setItems(sortedData);


    }


    public void setMedium(ObservableList<String> medium) {
        this.medium = medium;
    }

    public void setTpe(ObservableList<String> type) {
        this.type = type;
    }


    public void filter() {
        filteredData.setPredicate(
                new Predicate<MeterData>() {
                    @Override
                    public boolean test(MeterData meterData) {
                        try {

                            AtomicBoolean mediumMatch = new AtomicBoolean(false);
                            if (!medium.contains("*")) {
                                if (medium.contains(meterData.getJeVisClass().getName())) {
                                    mediumMatch.set(true);
                                }
                                if (!mediumMatch.get()) return false;
                            }



                            AtomicBoolean typMatch = new AtomicBoolean(false);
                            System.out.println(type);
                            System.out.println(type.contains("*"));
                            if (!type.contains("*")) {

                                if (type.contains(meterData.getJeVisAttributeJEVisSampleMap().get(getJEVisType(JC.MeasurementInstrument.a_Type)).get().getValueAsString())) {
                                    typMatch.set(true);
                                }
                                if (!typMatch.get()) return false;
                            }
                            return true;




                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return false;
                    }
                });
        Platform.runLater(() -> {
            System.out.println(filteredData);
            sort();
        });

    }

    private JEVisType getJEVisType(String string){
        try{
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
            JEVisType jeVisType = jeVisClass.getType(string);
            return jeVisType;

        }catch (Exception e){
            logger.error(e);
        }
        return null;


    }
//
//
//    public ObservableList<String> getMedium() {
//        return medium;
//    }
//
//    public void setMedium(ObservableList<String> medium) {
//        this.medium = medium;
//    }
//
//    public ObservableList<String> getStaus() {
//        return staus;
//    }
//
//    public void setStaus(ObservableList<String> staus) {
//        this.staus = staus;
//    }
//
//    public ObservableList<String> getFields() {
//        return fields;
//    }
//
//    public void setFields(ObservableList<String> fields) {
//        this.fields = fields;
//    }
//
//    public ObservableList<String> getSeu() {
//        return seu;
//    }
//
//    public void setSeu(ObservableList<String> seu) {
//        this.seu = seu;
//    }

}
