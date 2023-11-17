package org.jevis.jeconfig.plugin.meters.ui;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
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
import org.jetbrains.annotations.NotNull;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.table.SummeryData;
import org.jevis.jeconfig.application.table.SummeryTable;
import org.jevis.jeconfig.plugin.meters.MeterController;
import org.jevis.jeconfig.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.meters.data.MeterList;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeterListTab extends Tab {

    private static final Logger logger = LogManager.getLogger(MeterListTab.class);

    private final MeterList plan;
    private final JEVisDataSource ds;
    private final ObservableList<SummeryData> summeryData = FXCollections.observableArrayList();
    JEVisTypeWrapper typeWrapper;
    JEVisTypeWrapper locationWrapper;
    JEVisTypeWrapper verificationDateWrapper;

    JFXToggleButton jfxToggleButton = new JFXToggleButton();
    private MeterTable meterTable;

    private final SummeryTable summeryTable;
    private List<String> type;
    private List<JEVisClass> jeVisClasses;

    private final JFXComboBox<Integer> yearComboBox;


    public MeterListTab(MeterList plan, MeterController controller, JEVisDataSource ds) {
        super();
        this.ds = ds;


        typeWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Type));
        locationWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Location));
        verificationDateWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_VerificationDate));


        setText(plan.getName());
        this.plan = plan;


        this.meterTable = new MeterTable(plan, plan.getMeterDataList(), ds, controller.lastRawValuePrecisionProperty());

        Statistics statistics = new Statistics(meterPlanTable.filteredData, meterPlanTable);


        meterTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm(controller.getSelectedItem(), false, false);//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });

        meterTable.setMedium(FXCollections.observableArrayList("*"));
        meterTable.filter();
        BorderPane borderPane = new BorderPane();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);


        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);
        gridPane.addColumn(0, new Label(I18n.getInstance().getString("plugin.meters.search")), buildSearch(meterTable));
        gridPane.addColumn(1, vSep1);
        //gridPane.addColumn(2, new Label("Relevant"), relevantFilter);
        gridPane.addColumn(3, new Label(I18n.getInstance().getString("plugin.meters.filter")), buildClassFilterButton(meterTable));
        gridPane.addColumn(4, new Region(), buildTypeFilterButton(meterTable, I18n.getInstance().getString("plugin.meters.type"), meterTable::setType, typeWrapper));
        gridPane.addColumn(5, new Region(), buildTypeFilterButton(meterTable, I18n.getInstance().getString("plugin.meters.location"), meterTable::setLocation, locationWrapper));
        gridPane.addColumn(6, new Label(I18n.getInstance().getString("plugin.meters.overdue")), jfxToggleButton);

        yearComboBox = new JFXComboBox<>(getYearList());


        gridPane.addColumn(7, new Region(), yearComboBox);

        yearComboBox.valueProperty().addListener((observableValue, integer, t1) -> {
            meterTable.setYear(t1);
            meterTable.filter();
        });
        yearComboBox.setValue(LocalDate.now().getYear());


        jfxToggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            meterTable.setShowOnlyOverdue(t1);
            meterTable.filter();
        });

        meterTable.filter();

        borderPane.setTop(gridPane);
        borderPane.setCenter(meterTable);

        summeryTable = new SummeryTable(meterTable);


        meterTable.getMeterEventHandler().addEventListener(event -> {

            if (jeVisClasses.size() != getClasses(plan).size() || type.size() != getTypes(plan).size()) {
                updateStatistics(plan, statistics, borderPane);
            }
        });


        updateStatistics(plan, statistics, borderPane);


    }

    @NotNull
    private static List<JEVisClass> getClasses(MeterList plan) {
        return plan.getMeterDataList().stream().map(meterData -> meterData.getJeVisClass()).distinct().collect(Collectors.toList());
    }

    private void updateStatistics(MeterList plan, Statistics statistics, BorderPane borderPane) {


        summeryData.clear();
        jeVisClasses = getClasses(plan);
        type = getTypes(plan);
        int j = type.size() > jeVisClasses.size() ? type.size() : jeVisClasses.size();

        for (int i = 0; i < j; i++) {
            try {
                ObservableMap<TableColumn, StringProperty> summeryRow = FXCollections.observableHashMap();

                if (jeVisClasses.size() > i) {
                    summeryRow.put(meterTable.getColumns().stream().filter(meterDataTableColumn -> meterDataTableColumn.getId().equals(I18n.getInstance().getString("plugin.meters.medium"))).findAny().orElseThrow(RuntimeException::new), statistics.getAllOfMedium(jeVisClasses.get(i).getName()));
                }

                if (type.size() > i) {
                    summeryRow.put(meterTable.getColumns().stream().filter(meterDataTableColumn -> {
                        try {
                            return meterDataTableColumn.getText().equals(I18nWS.getInstance().getTypeName(typeWrapper.getJeVisType()));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }).findAny().orElseThrow(RuntimeException::new), statistics.getType(typeWrapper, type.get(i)));
                }
                if (i == 0) {
                    summeryRow.put(meterTable.getColumns().stream().filter(meterDataTableColumn -> {
                        try {
                            return meterDataTableColumn.getText().equals(I18nWS.getInstance().getTypeName(verificationDateWrapper.getJeVisType()));
                        } catch (JEVisException e) {
                            throw new RuntimeException(e);
                        }
                    }).findAny().orElseThrow(RuntimeException::new), statistics.getOverdue(verificationDateWrapper, I18n.getInstance().getString("plugin.meters.overdue"), yearComboBox.valueProperty()));
                }
                summeryData.add(new SummeryData(summeryRow));
            } catch (JEVisException jeVisException) {
                jeVisException.printStackTrace();
            }


        }


        summeryTable.setItems(summeryData);

        Platform.runLater(() -> {
            this.meterPlanTable.findScrollBar(meterPlanTable, Orientation.HORIZONTAL).valueProperty().bindBidirectional(summeryTable.findScrollBar(summeryTable, Orientation.HORIZONTAL).valueProperty());
        });


        borderPane.setBottom(summeryTable);
        setContent(borderPane);
    }

    @NotNull
    private List<String> getTypes(MeterList plan) {
        return plan.getMeterDataList().stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().get(typeWrapper)).filter(sampleData -> sampleData != null).map(sampleData -> sampleData.getOptionalJEVisSample()).filter(optionalJEVisSample -> optionalJEVisSample.isPresent()).map(optionalJEVisSample -> {
            try {
                return optionalJEVisSample.get().getValueAsString();
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }
        }).distinct().sorted(String::compareTo).collect(Collectors.toList());
    }

    private JFXTextField buildSearch(MeterTable meterTable) {
        JFXTextField textField = new JFXTextField();
        textField.textProperty().addListener((observableValue, s, t1) -> {
            meterTable.setContainsTextFilter(t1);
            meterTable.filter();
        });

        return textField;
    }

    private void buildRow(List<TableColumn> tableColumns, List<Function<String, StringProperty>> list, List<String> translations) {

    }

    private TagButton buildClassFilterButton(MeterTable meterTable) {


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
                    meterTable.setMedium((ObservableList<String>) c.getList());
                    meterTable.filter();
                }
            }
        });


        return mediumButton;
    }

    private TagButton buildTypeFilterButton(MeterTable meterTable, String name, Function<ObservableList<String>, Void> function, JEVisTypeWrapper jeVisTypeWrapper) {
        TagButton button = null;
        try {
            List<String> stringValues = plan.getMeterDataList().stream()
                    .map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapper))
                    .filter(sampleData -> sampleData != null)
                    .map(sampleData -> sampleData.getOptionalJEVisSample())
                    .filter(optionalJEVisSample -> optionalJEVisSample.isPresent())
                    .map(optionalJEVisSample -> {
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
                        meterTable.filter();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e);
        }

        return button;
    }


    public MeterTable getMeterPlanTable() {
        return meterTable;
    }

    public void setMeterPlanTable(MeterTable meterTable) {
        this.meterTable = meterTable;
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

    public MeterList getPlan() {
        return plan;
    }

    private ObservableList<Integer> getYearList() {
        LocalDate localDate = LocalDate.now();

        ObservableList<Integer> years = FXCollections.observableArrayList();

        for (int i = localDate.getYear() - 10; i < localDate.getYear() + 10; i++) {
            years.add(i);
        }

        return years;
    }
}
