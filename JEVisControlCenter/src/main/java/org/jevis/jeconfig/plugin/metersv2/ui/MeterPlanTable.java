package org.jevis.jeconfig.plugin.metersv2.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.table.TableFindScrollbar;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.plugin.metersv2.cells.*;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.data.MeterPlan;
import org.jevis.jeconfig.plugin.metersv2.data.SampleData;
import org.jevis.jeconfig.plugin.metersv2.event.MeterEventHandler;
import org.jevis.jeconfig.plugin.metersv2.event.MeterPlanEvent;
import org.jevis.jeconfig.plugin.metersv2.event.PrecisionEventHandler;
import org.joda.time.DateTime;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.prefs.Preferences;


public class MeterPlanTable extends TableView<MeterData> implements TableFindScrollbar {

    private static final Logger logger = LogManager.getLogger(MeterPlanTable.class);
    private static final int DATE_TIME_WIDTH = 120;
    private static final int BIG_WIDTH = 200;
    private static final int SMALL_WIDTH = 60;
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.MeterPlugin");
    private final JEVisDataSource ds;
    private final Map<JEVisType, JEVisTypeWrapper> map = new HashMap<>();
    private final boolean showSumRow = false;
    private final ObservableList<String> fields = FXCollections.observableArrayList();
    private final ObservableList<String> seu = FXCollections.observableArrayList();
    FilteredList<MeterData> filteredData;
    SortedList<MeterData> sortedData;
    ObservableList<MeterData> data;
    JEVisTypeWrapper typeWrapper;
    JEVisTypeWrapper locationWrapper;
    JEVisTypeWrapper pointNameWrapper;
    JEVisTypeWrapper decimalPlacesWrapper;
    JEVisTypeWrapper verficationDateWrapper;
    private String containsTextFilter = "";
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> type = FXCollections.observableArrayList();
    private ObservableList<String> location = FXCollections.observableArrayList();
    private boolean showOnlyOvedue;

    private MeterEventHandler meterEventHandler = new MeterEventHandler();
    private PrecisionEventHandler precisionEventHandler = new PrecisionEventHandler();


    public MeterPlanTable(MeterPlan meterPlan, ObservableList<MeterData> data, JEVisDataSource ds, IntegerProperty integerProperty) {

        this.ds = ds;
        this.data = data;
        this.filteredData = new FilteredList<>(this.data);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.onScrollToProperty().addListener((observableValue, scrollToEventEventHandler, t1) -> {
            System.out.println("onScrollToProperty");
            System.out.println(t1);
        });

        this.onScrollProperty().addListener((observableValue, eventHandler, t1) -> {
            System.out.println("onScrollProperty");
            System.out.println(t1);
        });







        typeWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Type));
        locationWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Location));
        pointNameWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_MeasuringPointName));
        verficationDateWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_VerificationDate));

        decimalPlacesWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_DecimalPlaces));

        this.setTableMenuButtonVisible(true);

        this.getColumns().add(new MediumColumn(I18n.getInstance().getString("plugin.meters.medium"), BIG_WIDTH));
        JEVisType onlineIdType = null;
        try {
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
            onlineIdType = jeVisClass.getType("Online ID");

        } catch (Exception e) {
            e.printStackTrace();

        }
        List<JEVisTypeWrapper> jeVisTypes = meterPlan.getAllAvailbleTypes();


        try {
            for (JEVisTypeWrapper jeVisTypeWrapper : jeVisTypes) {
                int i = 0;
                JEVisType jeVisType = jeVisTypeWrapper.getJeVisType();
                TableColumn<MeterData, ?> col = null;
                switch (jeVisType.getPrimitiveType()) {
                    case JEVisConstants.PrimitiveType.LONG:
                        col = new DoubleColumn(jeVisType, BIG_WIDTH, I18nWS.getInstance().getTypeName(jeVisType));
                        break;
                    case JEVisConstants.PrimitiveType.FILE:
                        col = new FileColumn(jeVisType, BIG_WIDTH, I18nWS.getInstance().getTypeName(jeVisType));
                        break;
                    default:
                        if ((jeVisType.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || jeVisType.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId()))) {
                            System.out.println(jeVisTypeWrapper);

                            col = new DateColumn(I18nWS.getInstance().getTypeName(jeVisType), jeVisType, BIG_WIDTH);
                            break;
                        }
                        if (jeVisType.getName().equals(JC.MeasurementInstrument.a_OnlineID)) {
                            col = new LastRawValue(I18n.getInstance().getString("plugin.meters.lastrawvalue"), ds, jeVisType, BIG_WIDTH, decimalPlacesWrapper, precisionEventHandler);
                            i = 2;
                            this.getSortOrder().add(col);
                            break;

                        } else {
                            col = new ShortCellColumn(jeVisType, BIG_WIDTH, I18nWS.getInstance().getTypeName(jeVisType));
                            if (jeVisType.getName().equals(JC.MeasurementInstrument.a_MeasuringPointName)) {
                                col.setVisible(true);
                                i = 1;
                            } else if (jeVisType.getName().equals(JC.MeasurementInstrument.a_Type)) {
                                col.setVisible(true);
                                i = 3;
                            }
                            break;
                        }

                }
                col.setVisible(pref.getBoolean(jeVisType.getName(), true));
                col.visibleProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        pref.putBoolean(jeVisType.getName(), newValue);
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                });
                if (col != null) {
                    if (i == 0) {
                        this.getColumns().add(col);

                    } else {
                        this.getColumns().add(i, col);
                    }
                }
            }
            TableColumn<MeterData, ?> jumpColumn = new JumpColumn("", onlineIdType, BIG_WIDTH, ds);
            jumpColumn.setVisible(pref.getBoolean("Jump", true));
            jumpColumn.visibleProperty().addListener((observable, oldValue, newValue) -> {
                pref.putBoolean("Jump", newValue);
            });


            TableColumn<MeterData, ?> pathColumn = new PathColumnColumn(new ObjectRelations(ds), BIG_WIDTH, I18n.getInstance().getString("plugin.meters.path"));


            pathColumn.setVisible(pref.getBoolean("Path", true));
            pathColumn.visibleProperty().addListener((observable, oldValue, newValue) -> {
                pref.putBoolean("Path", newValue);
            });
            TableColumn<MeterData, ?> nameColumn = new ObjectNameColumn(I18n.getInstance().getString("plugin.meters.name"), BIG_WIDTH);


            nameColumn.setVisible(pref.getBoolean("nameColumn", true));
            nameColumn.visibleProperty().addListener((observable, oldValue, newValue) -> {
                pref.putBoolean("nameColumn", newValue);
            });

            this.getColumns().add(jumpColumn);
            this.getColumns().add(1, nameColumn);
            this.getColumns().add(2, pathColumn);


        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.comparatorProperty());
        Platform.runLater(() -> {

            setItems(sortedData);
        });


        //findScrollBar(this,Orientation.HORIZONTAL);
//        try {
//            scrollbar.valueProperty().addListener((observableValue, number, t1) -> {
//                System.out.println("scroll detected");
//                System.out.println(t1);
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public ObjectProperty<EventHandler<ScrollToEvent<Integer>>> getScrollToProperty() {
        return this.getScrollToProperty();
    }


    public void replaceItem(MeterData meterData) {
        this.data.remove(meterData);
        meterData.load();
        this.data.add(meterData);
        getMeterEventHandler().fireEvent(new MeterPlanEvent(this, MeterPlanEvent.TYPE.UPDATE));
    }

    public void removeItem(MeterData meterData) {
        this.data.remove(meterData);
        getMeterEventHandler().fireEvent(new MeterPlanEvent(this, MeterPlanEvent.TYPE.REMOVE));
    }

    public void addItem(MeterData meterData) {
        this.data.add(meterData);
        getMeterEventHandler().fireEvent(new MeterPlanEvent(this, MeterPlanEvent.TYPE.ADD));
    }

    public void setItems(List<MeterData> meterDatas) {
        this.data.setAll(meterDatas);
    }


    public void setMedium(ObservableList<String> medium) {
        this.medium = medium;
    }

    public Void setType(ObservableList<String> type) {
        this.type = type;
        return null;
    }


    public void filter() {


        filteredData.setPredicate(

                new Predicate<MeterData>() {
                    @Override
                    public boolean test(MeterData meterData) {

                        try {

                            AtomicBoolean mediumMatch = new AtomicBoolean(false);
                            if (!medium.contains("*")) {
                                if (medium.contains(meterData.getjEVisClassName())) {
                                    mediumMatch.set(true);
                                }
                                if (!mediumMatch.get()) return false;
                            }


                            if (!filter(typeWrapper, type, meterData)) return false;
                            if (!filter(locationWrapper, location, meterData)) return false;
                            AtomicBoolean overdueMatch = new AtomicBoolean(false);
                            if (showOnlyOvedue) {
                                SampleData sampleData = meterData.getJeVisAttributeJEVisSampleMap().get(verficationDateWrapper);
                                if (sampleData != null && sampleData.getOptionalJEVisSample().isPresent()) {
                                    JEVisSample jeVisSample = sampleData.getOptionalJEVisSample().get();
                                    if (new DateTime(jeVisSample.getValueAsString()).isBeforeNow()) {
                                        overdueMatch.set(true);
                                    }
                                }
                                if (!overdueMatch.get()) return false;
                            }


                            AtomicBoolean containString = new AtomicBoolean(false);
                            if (containsTextFilter != null && !containsTextFilter.isEmpty()) {
                                //Optional<JEVisSample> meteringPoint = meterData.getJeVisAttributeJEVisSampleMap().get(meteringPointWrapper).getOptionalJEVisSample();
                                try {
                                    String meteringPoint = meterData.getJeVisAttributeJEVisSampleMap().get(pointNameWrapper).getOptionalJEVisSample().orElseThrow(RuntimeException::new).getValueAsString();
                                    //String meteringIP = meterData.getJeVisAttributeJEVisSampleMap().get(meteringPointWrapper).getOptionalJEVisSample().orElseThrow(RuntimeException::new).getValueAsString();
                                    //String meteringPoint = meterData.getJeVisAttributeJEVisSampleMap().get(meteringPointWrapper).getOptionalJEVisSample().orElseThrow(RuntimeException::new).getValueAsString();
                                    if (meteringPoint.toLowerCase().contains(containsTextFilter.toLowerCase())) {
                                        containString.set(true);
                                    }

                                } catch (Exception e) {
                                    logger.error(e);
                                }

                                //TODO: may also check if column is visible
                                return containString.get();
                            }

                            return true;


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return false;
                    }
                });
        Platform.runLater(() -> {
            sort();
            getMeterEventHandler().fireEvent(new MeterPlanEvent(this, MeterPlanEvent.TYPE.FILTER));
        });


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

    public String getContainsTextFilter() {
        return containsTextFilter;
    }

    public void setContainsTextFilter(String containsTextFilter) {
        this.containsTextFilter = containsTextFilter;
    }

    public ObservableList<String> getLocation() {
        return location;
    }

    public Void setLocation(ObservableList<String> location) {
        this.location = location;
        return null;
    }

    private boolean filter(JEVisTypeWrapper jeVisTypeWrapper, ObservableList<String> observableList, MeterData meterData) {
        try {

            if (observableList.contains("*")) return true;
            if (meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapper).getOptionalJEVisSample().isPresent()) {
                String s = (meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapper).getOptionalJEVisSample().get().getValueAsString());
                if (observableList.contains(s)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public boolean isShowOnlyOvedue() {
        return showOnlyOvedue;
    }

    public void setShowOnlyOvedue(boolean showOnlyOvedue) {
        this.showOnlyOvedue = showOnlyOvedue;
    }

    public MeterData getSelectedItem() {
        return this.getSelectionModel().getSelectedItem();
    }

    public List<MeterData> getSelectedItems() {
        return this.getSelectionModel().getSelectedItems();
    }

    public MeterEventHandler getMeterEventHandler() {
        return meterEventHandler;
    }

    public PrecisionEventHandler getPrecisionEventHandler() {
        return precisionEventHandler;
    }

}
