package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.table.SummeryData;
import org.jevis.jeconfig.application.table.SummeryTable;
import org.jevis.jeconfig.plugin.metersv2.MeterController;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterPlan;
import org.jevis.jeconfig.plugin.metersv2.data.SampleData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeterPlanTab extends Tab {

    private static final Logger logger = LogManager.getLogger(MeterPlanTab.class);

    private MeterPlan plan;
    private MeterPlanTable meterPlanTable;

    private JEVisDataSource ds;

    JEVisTypeWrapper typeWrapper;
    JEVisTypeWrapper locationWrapper;
    JEVisTypeWrapper verificationDateWrapper;

    JFXToggleButton jfxToggleButton = new JFXToggleButton();


    private final ObservableList<SummeryData> summeryData = FXCollections.observableArrayList();


    public MeterPlanTab(MeterPlan plan, MeterController controller, JEVisDataSource ds) {
        super();
        this.ds = ds;


        typeWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Type));
        locationWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Location));
        verificationDateWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_VerificationDate));



        setText(plan.getName());
        this.plan = plan;


        this.meterPlanTable = new MeterPlanTable(plan, plan.getMeterDataList(), ds, controller.lastRawValuePrecisionProperty());

        Statistics statistics = new Statistics(meterPlanTable.filteredData, meterPlanTable);


        meterPlanTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm(Optional.empty());//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });

        meterPlanTable.setMedium(FXCollections.observableArrayList("*"));
        meterPlanTable.filter();
        BorderPane borderPane = new BorderPane();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);


        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);
        gridPane.addColumn(0, new Label(I18n.getInstance().getString("plugin.meters.search")), buildSearch(meterPlanTable));
        gridPane.addColumn(1, vSep1);
        //gridPane.addColumn(2, new Label("Relevant"), relevantFilter);
        gridPane.addColumn(3, new Region(), buildClassFilterButton(meterPlanTable));
        gridPane.addColumn(4, new Region(), buildTypeFilterButton(meterPlanTable, I18n.getInstance().getString("plugin.meters.type"), meterPlanTable::setType, typeWrapper));
        gridPane.addColumn(5, new Region(), buildTypeFilterButton(meterPlanTable, I18n.getInstance().getString("plugin.meters.location"), meterPlanTable::setLocation, locationWrapper));
        gridPane.addColumn(6,new Label(I18n.getInstance().getString("plugin.meters.overdue")),jfxToggleButton);


        jfxToggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            meterPlanTable.setShowOnlyOvedue(t1);
            meterPlanTable.filter();
        });

        meterPlanTable.filter();

        borderPane.setTop(gridPane);
        borderPane.setCenter(meterPlanTable);


       List<JEVisClass> jeVisClasses = plan.getMeterDataList().stream().map(meterData -> meterData.getJeVisClass()).distinct().collect(Collectors.toList());
       List<String> type = plan.getMeterDataList().stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().get(typeWrapper)).filter(sampleData -> sampleData != null).map(sampleData -> sampleData.getOptionalJEVisSample()).filter(optionalJEVisSample -> optionalJEVisSample.isPresent()).map(optionalJEVisSample -> {
           try {
               return optionalJEVisSample.get().getValueAsString();
           } catch (JEVisException e) {
               throw new RuntimeException(e);
           }
       }).distinct().collect(Collectors.toList());
       int j = type.size() > jeVisClasses.size() ? type.size() : jeVisClasses.size();

        for (int i = 0; i < j; i++) {
            try {
                ObservableMap<TableColumn, StringProperty> summeryRow = FXCollections.observableHashMap();

                if (jeVisClasses.size() > i) {
                    summeryRow.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals(I18n.getInstance().getString("plugin.meters.medium"))).findAny().orElseThrow(RuntimeException::new), statistics.getAllOfMedium(jeVisClasses.get(i).getName(), jeVisClasses.get(i).getName()));
                }

                if (type.size() > i) {
                    summeryRow.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> {
                        try {
                            return meterDataTableColumn.getText().equals(I18nWS.getInstance().getTypeName(typeWrapper.getJeVisType()));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }).findAny().orElseThrow(RuntimeException::new), statistics.getType(typeWrapper, type.get(i)));
                }
                if (i == 0) {
                    summeryRow.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> {
                        try {
                            return meterDataTableColumn.getText().equals(I18nWS.getInstance().getTypeName(verificationDateWrapper.getJeVisType()));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }).findAny().orElseThrow(RuntimeException::new), statistics.getOverdue(verificationDateWrapper,I18n.getInstance().getString("plugin.meters.overdue")));
                }
               summeryData.add(new SummeryData(summeryRow));
            } catch (JEVisException jeVisException) {
                jeVisException.printStackTrace();
            }




        }



        SummeryTable summeryTable = new SummeryTable(meterPlanTable);
       summeryTable.setItems(summeryData);


        borderPane.setBottom(summeryTable);
        setContent(borderPane);



    }

    private JFXTextField buildSearch(MeterPlanTable meterPlanTable) {
        JFXTextField textField = new JFXTextField();
        textField.textProperty().addListener((observableValue, s, t1) -> {
            meterPlanTable.setContainsTextFilter(t1);
            meterPlanTable.filter();
        });

        return textField;
    }

    private void buildRow(List<TableColumn> tableColumns, List<Function<String,StringProperty>> list, List<String> translations) {

    }

    private TagButton buildClassFilterButton(MeterPlanTable meterPlanTable) {


        List<String> clasNAmes = plan.getMeterDataList().stream().map(meterData -> {
            try {
                return meterData.getJeVisClass().getName();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).distinct().collect(Collectors.toList());
        ObservableList<String> observableList = FXCollections.observableArrayList(clasNAmes);
        TagButton mediumButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.medium"), observableList, observableList);

        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}", c);
                while (c.next()) {
                    meterPlanTable.setMedium((ObservableList<String>) c.getList());
                    meterPlanTable.filter();
                }
            }
        });


        return mediumButton;
    }

    private TagButton buildTypeFilterButton(MeterPlanTable meterPlanTable, String name, Function<ObservableList<String>, Void> function, JEVisTypeWrapper jeVisTypeWrapper) {
        TagButton button = null;
        try {
            List<String> stringValues = plan.getMeterDataList().stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapper))
                    .filter(sampleData -> sampleData != null).map(sampleData -> sampleData.getOptionalJEVisSample()).filter(optionalJEVisSample -> optionalJEVisSample.isPresent()).map(optionalJEVisSample -> {
                        try {
                            return optionalJEVisSample.get().getValueAsString();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }).distinct().collect(Collectors.toList());

            ObservableList<String> strings = FXCollections.observableArrayList(stringValues);


            button = new TagButton(name, strings, strings);
            function.apply(FXCollections.observableArrayList("*"));

            button.getSelectedTags().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(Change<? extends String> c) {
                    logger.debug("List Changed: {}", c);
                    while (c.next()) {
                        function.apply((ObservableList<String>) c.getList());
                        meterPlanTable.filter();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e);
        }

        return button;
    }


    public MeterPlanTable getMeterPlanTable() {
        return meterPlanTable;
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

    public void setMeterPlanTable(MeterPlanTable meterPlanTable) {
        this.meterPlanTable = meterPlanTable;
    }

    public MeterPlan getPlan() {
        return plan;
    }
}
