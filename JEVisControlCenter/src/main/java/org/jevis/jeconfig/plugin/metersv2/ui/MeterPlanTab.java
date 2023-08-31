package org.jevis.jeconfig.plugin.metersv2.ui;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.metersv2.MeterController;
import org.jevis.jeconfig.plugin.metersv2.data.MeterPlan;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeterPlanTab extends Tab {

    private static final Logger logger = LogManager.getLogger(MeterPlanTab.class);

    private MeterPlan plan;
    private MeterPlanTable meterPlanTable;

    private JEVisDataSource ds;


    public MeterPlanTab(MeterPlan plan, MeterController controller, JEVisDataSource ds) {
        super();
        this.ds = ds;


        setText(plan.getName());
        this.plan = plan;
        this.meterPlanTable = new MeterPlanTable(plan, plan.getMeterDataList(), ds);



        meterPlanTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm(false);//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });


        BorderPane borderPane = new BorderPane();

        GridPane gridPane = new GridPane();
        gridPane.addColumn(0, initClassFilterButton(meterPlanTable));
        gridPane.addColumn(1, buildTypeFilterButton(meterPlanTable));


        borderPane.setTop(gridPane);
        borderPane.setCenter(meterPlanTable);
        setContent(borderPane);



        //initClassFilterButton(nonconformityPlanTable);


    }

    private TagButton initClassFilterButton(MeterPlanTable meterPlanTable) {


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

    private TagButton buildTypeFilterButton(MeterPlanTable meterPlanTable) {
        TagButton button = null;
        try {
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);

            JEVisType jeVisType = jeVisClass.getType(JC.MeasurementInstrument.a_Type);
            List<Map<JEVisType,Optional<JEVisSample>>> meterTypes = plan.getMeterDataList().stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap()).collect(Collectors.toList());
            List<JEVisSample> samples =  meterTypes.stream().map(jeVisTypeOptionalMap -> jeVisTypeOptionalMap.get(jeVisType)).filter(jeVisSample -> jeVisSample.isPresent()).map(jeVisSample -> jeVisSample.get()).collect(Collectors.toList());
            List<String> stringValues = samples.stream().map(jeVisSample -> {
                try {
                    return jeVisSample.getValueAsString();
                } catch (JEVisException e) {
                    return null;
                }
            }).filter(s -> s!= null).distinct().collect(Collectors.toList());

            ObservableList<String> stings = FXCollections.observableArrayList(stringValues);






           button = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.medium"), stings, stings);

            button.getSelectedTags().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(Change<? extends String> c) {
                    logger.debug("List Changed: {}", c);
                    while (c.next()) {
                        System.out.println(c.getList());
                        meterPlanTable.setTpe((ObservableList<String>) c.getList());
                        meterPlanTable.filter();
                    }
                }
            });

        }
        catch (Exception e) {
            logger.error(e);
        }

        return button;
    }

    public MeterPlanTable getMeterPlanTable() {
        return meterPlanTable;
    }

    public void setMeterPlanTable(MeterPlanTable meterPlanTable) {
        this.meterPlanTable = meterPlanTable;
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
