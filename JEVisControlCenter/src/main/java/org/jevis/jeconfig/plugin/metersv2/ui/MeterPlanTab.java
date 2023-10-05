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


        this.meterPlanTable = new MeterPlanTable(plan, plan.getMeterDataList(), ds);

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
        gridPane.addColumn(0, new Label("Search"), buildSearch(meterPlanTable));
        gridPane.addColumn(1, vSep1);
        //gridPane.addColumn(2, new Label("Relevant"), relevantFilter);
        gridPane.addColumn(3, new Region(), buildClassFilterButton(meterPlanTable));
        gridPane.addColumn(4, new Region(), buildTypeFilterButton(meterPlanTable, "Type", meterPlanTable::setType, typeWrapper));
        gridPane.addColumn(5, new Region(), buildTypeFilterButton(meterPlanTable, "Location", meterPlanTable::setLocation, locationWrapper));
        gridPane.addColumn(6,new Label("Overdue"),jfxToggleButton);


        jfxToggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            meterPlanTable.setShowOnlyOvedue(t1);
            meterPlanTable.filter();
        });

        //gridPane.addColumn(5, new Region(), scopeButton);
//        gridPane.addColumn(6, vSep2);
//        gridPane.addColumn(7, new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.date")), dateSelector);

        //gridPane.addColumn(0, initClassFilterButton(meterPlanTable));
        //gridPane.addColumn(1, buildTypeFilterButton(meterPlanTable));
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
                    summeryRow.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals("Medium")).findAny().orElseThrow(RuntimeException::new), statistics.getAllOfMedium(jeVisClasses.get(i).getName(), jeVisClasses.get(i).getName()));
                }

                if (type.size() > i) {
                    summeryRow.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals("Type")).findAny().orElseThrow(RuntimeException::new), statistics.getType(typeWrapper, type.get(i)));
                }
                if (i == 0) {
                    summeryRow.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals("Verification Date")).findAny().orElseThrow(RuntimeException::new), statistics.getOverdue(verificationDateWrapper,"Overdue"));
                }
                summeryData.add(new SummeryData(summeryRow));
            } catch (JEVisException jeVisException) {
                jeVisException.printStackTrace();
            }




        }


//        summeryData.add(new SummeryData(summeryRow1));
//        ObservableMap<TableColumn, StringProperty> summeryRow2 = FXCollections.observableHashMap();
//        summeryRow2.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals("Medium")).findAny().orElseThrow(RuntimeException::new), statistics.getAllOfType(JC.MeasurementInstrument.GasMeasurementInstrument.name,"Gas"));
//        summeryData.add(new SummeryData(summeryRow2));
//        ObservableMap<TableColumn, StringProperty> summeryRow3 = FXCollections.observableHashMap();
//        summeryRow3.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals("Medium")).findAny().orElseThrow(RuntimeException::new), statistics.getAllOfType(JC.MeasurementInstrument.HeatMeasurementInstrument.name,"Heat"));
//        summeryData.add(new SummeryData(summeryRow3));
//        ObservableMap<TableColumn, StringProperty> summeryRow4 = FXCollections.observableHashMap();
//        summeryRow4.put(meterPlanTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals("Medium")).findAny().orElseThrow(RuntimeException::new), statistics.getAllOfType(JC.MeasurementInstrument.WaterMeasurementInstrument.name,"Water"));
//        summeryData.add(new SummeryData(summeryRow4));
        SummeryTable summeryTable = new SummeryTable(meterPlanTable);
       summeryTable.setItems(summeryData);


        borderPane.setBottom(summeryTable);
        setContent(borderPane);


        //initClassFilterButton(nonconformityPlanTable);


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


//        GridPane gridPane = new GridPane();
//        gridPane.setPadding(new Insets(25));
//        gridPane.setHgap(10);
//        gridPane.setVgap(10);
//        double maxListHeight = 100;
//
//
//        Label lSuche = new Label(I18n.getInstance().getString("plugin.nonconformities.serach"));
//        JFXTextField fsearch = new JFXTextField();
//        fsearch.setPromptText(I18n.getInstance().getString("plugin.nonconformities.serachfor"));
//
//

//        TagButton fieldButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.field"), plan.getFieldsTags(), plan.getFieldsTags());
//        TagButton stausButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.staus"),plan.getStausTags(),plan.getStausTags());
//        TagButton seuButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.seu"),plan.getSignificantEnergyUseTags(),plan.getSignificantEnergyUseTags());
//
//
//
//        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
//            nonconformityPlanTable.setTextFilter(newValue);
//            nonconformityPlanTable.filter();
//        });
//
//        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);
//
//        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
//            @Override
//            public void changed(ObservableValue<? extends DateFilter> observableValue, DateFilter dateFilter, DateFilter t1) {
//                nonconformityPlanTable.setDateFilter(t1);
//                nonconformityPlanTable.filter();
//            }
//        });
//
//
//        Separator vSep1 = new Separator(Orientation.VERTICAL);
//        Separator vSep2 = new Separator(Orientation.VERTICAL);
//        ;
//
//        GridPane.setRowSpan(vSep1, 2);
//        GridPane.setRowSpan(vSep2, 2);
//        gridPane.addColumn(0, lSuche, fsearch);
//        gridPane.addColumn(1, vSep1);
//        gridPane.addColumn(2, new Label(I18n.getInstance().getString("plugin.nonconformities.filter")), stausButton);
//        gridPane.addColumn(3, new Region(), mediumButton);
//        gridPane.addColumn(4, new Region(), fieldButton);
//        gridPane.addColumn(5, new Region(), seuButton);
//        gridPane.addColumn(6, vSep2);
//        gridPane.addColumn(7, new Label(I18n.getInstance().getString("plugin.nonconformities.date")), dateSelector);


//        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
//            @Override
//            public void onChanged(Change<? extends String> c) {
//                logger.debug("List Changed: {}",c);
//                while (c.next()) {
//                    nonconformityPlanTable.setFilterMedium((ObservableList<String>) c.getList());
//                    nonconformityPlanTable.filter();
//                }
//            }
//        });


//        stausButton.getSelectedTags().addListener(new ListChangeListener<String>() {
//            @Override
//            public void onChanged(Change<? extends String> c) {
//                logger.debug("List Changed: {}",c);
//                while (c.next()) {
//                    nonconformityPlanTable.setStaus((ObservableList<String>) c.getList());
//                    nonconformityPlanTable.filter();
//                }
//            }
//        });
//        fieldButton.getSelectedTags().addListener(new ListChangeListener<String>() {
//            @Override
//            public void onChanged(Change<? extends String> c) {
//                logger.debug("List Changed: {}",c);
//                while (c.next()) {
//                    nonconformityPlanTable.setFields((ObservableList<String>) c.getList());
//                    nonconformityPlanTable.filter();
//                }
//            }
//        });
//
//        seuButton.getSelectedTags().addListener(new ListChangeListener<String>() {
//            @Override
//            public void onChanged(Change<? extends String> change) {
//                nonconformityPlanTable.setSeu((ObservableList<String>) change.getList());
//                nonconformityPlanTable.filter();
//            }
//        });


//nonconformityPlanTable.setStaus(stausButton.getSelectedTags());
//        nonconformityPlanTable.setSeu(seuButton.getSelectedTags());
//        nonconformityPlanTable.setMedium(mediumButton.getSelectedTags());
//        nonconformityPlanTable.setFields(fieldButton.getSelectedTags());
//        nonconformityPlanTable.setStaus(stausButton.getSelectedTags());
//        nonconformityPlanTable.filter();
//
//
//
//
//
//
//
//        //gridPane.add(hBox, 0, 1, 3, 1);
//
//
//        BorderPane borderPane = new BorderPane();
//        borderPane.setTop(gridPane);
//        borderPane.setCenter(nonconformityPlanTable);
//        SummeryTable summeryTable = new SummeryTable(nonconformityPlanTable);
//        summeryTable.setItems(nonconformityPlanTable.getSummeryData());
//
////        TableSumPanel tableSumPanel = new TableSumPanel(nonconformityPlanTable.getItems());
//        borderPane.setBottom(summeryTable);
//
//        nonconformityPlanTable.setOnMousePressed(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
//                    controller.openDataForm(false);//actionTable.getSelectionModel().getSelectedItem()
//                }
//            }
//        });
//        setContent(borderPane);
//
//
//    }
//
//    public MeterPlanTab(String text, Node content, NonconformityPlan plan, BooleanProperty updateTrigger) {
//        super(text, content);
//        this.plan = plan;
//    }
//
//    public NonconformityPlan getNonconformityPlan() {
//        return plan;
//    }
//
//    public void setActionPlan(NonconformityPlan plan) {
//        this.plan = plan;
//    }
//
//    public MeterPlanTable getActionTable() {
//        return nonconformityPlanTable;
//    }
//}
