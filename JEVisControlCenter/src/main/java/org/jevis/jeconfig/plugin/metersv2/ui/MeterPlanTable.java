package org.jevis.jeconfig.plugin.metersv2.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.LocalNameDialog;
import org.jevis.jeconfig.plugin.metersv2.cells.*;
import org.jevis.jeconfig.plugin.metersv2.data.*;
import org.jevis.jeconfig.plugin.metersv2.event.MeterPlanEvent;
import org.jevis.jeconfig.plugin.metersv2.event.MeterPlanEventListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.event.EventListenerList;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.prefs.Preferences;


public class MeterPlanTable extends TableView<MeterData> {

    private JEVisDataSource ds;

    private Map<JEVisType, JEVisTypeWrapper> map = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(MeterPlanTable.class);

    private final EventListenerList listeners = new EventListenerList();

    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.MeterPlugin");


    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    FilteredList<MeterData> filteredData;

    SortedList<MeterData> sortedData;
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private boolean showSumRow = false;
    private String containsTextFilter = "";
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> type = FXCollections.observableArrayList();
    private ObservableList<String> location = FXCollections.observableArrayList();
    private ObservableList<String> fields = FXCollections.observableArrayList();
    private ObservableList<String> seu = FXCollections.observableArrayList();

    private boolean showOnlyOvedue;

    private static int DATE_TIME_WIDTH = 120;
    private static int BIG_WIDTH = 200;
    private static int SMALL_WIDTH = 60;

    ObservableList<MeterData> data;

    JEVisTypeWrapper typeWrapper;
    JEVisTypeWrapper locationWrapper;
    JEVisTypeWrapper pointNameWrapper;

    JEVisTypeWrapper decimalPlacesWrapper;
    JEVisTypeWrapper verficationDateWrapper;




    // Map<JEVisType, JEVisTypeWrapper> jeVisTypeJEVisTypeWrapperMap;


    public MeterPlanTable(MeterPlan meterPlan, ObservableList<MeterData> data, JEVisDataSource ds, IntegerProperty integerProperty) {

        this.ds = ds;
        this.data = data;
        this.filteredData = new FilteredList<>(this.data);

        typeWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Type));
        locationWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_Location));
        pointNameWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_MeasuringPointName));
        verficationDateWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_VerificationDate));

        decimalPlacesWrapper = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_DecimalPlaces));
        //this.jeVisTypeJEVisTypeWrapperMap = jeVisTypeJEVisTypeWrapperMap;

        this.setTableMenuButtonVisible(true);

        this.getColumns().add(new MediumColumn(I18n.getInstance().getString("plugin.meters.medium"), BIG_WIDTH));
        JEVisType onlineIdType = null;
        JEVisType pointName = null;
        try {
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
            onlineIdType = jeVisClass.getType("Online ID");
            pointName = jeVisClass.getType(JC.MeasurementInstrument.a_MeasuringPointName);

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
                    case JEVisConstants.PrimitiveType.STRING:
                        if (jeVisType.getName().equals(JC.MeasurementInstrument.a_OnlineID)) {
                            col = new LastRawValue(I18n.getInstance().getString("plugin.meters.lastrawvalue"), ds, jeVisType,BIG_WIDTH, decimalPlacesWrapper);
                            i = 2;
                            this.getSortOrder().add(col);

                        } else {
                            col = new ShortCellColumn(jeVisType, BIG_WIDTH, I18nWS.getInstance().getTypeName(jeVisType));
                            if (jeVisType.getName().equals(JC.MeasurementInstrument.a_MeasuringPointName)){
                                col.setVisible(true);
                                i = 1;
                            } else if (jeVisType.getName().equals(JC.MeasurementInstrument.a_Type)) {
                                col.setVisible(true);
                                i = 3;
                            }
                        }
                        break;
                    case JEVisConstants.PrimitiveType.FILE:
                        col = new FileColumn(jeVisType, BIG_WIDTH, I18nWS.getInstance().getTypeName(jeVisType));
                        break;
                    default:

                        if ((jeVisType.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || jeVisType.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId()))) {

                            col = new DateColumn(I18nWS.getInstance().getTypeName(jeVisType), jeVisType, BIG_WIDTH);
                        } else {

                            col = new ShortCellColumn(jeVisType, BIG_WIDTH, I18nWS.getInstance().getTypeName(jeVisType));
                        }
                        break;
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





            TableColumn<MeterData, ?> pathColumn = new PathColumnColumn(new ObjectRelations(ds),BIG_WIDTH,I18n.getInstance().getString("plugin.meters.path"));


            pathColumn.setVisible(pref.getBoolean("Path", true));
            pathColumn.visibleProperty().addListener((observable, oldValue, newValue) -> {
                pref.putBoolean("Path", newValue);
            });
            TableColumn<MeterData, ?> nameColumn = new ObjectNameColumn(I18n.getInstance().getString("plugin.meters.jevisname"),BIG_WIDTH);


                    nameColumn.setVisible(pref.getBoolean("nameColumn", true));
            nameColumn.visibleProperty().addListener((observable, oldValue, newValue) -> {
                pref.putBoolean("nameColumn", newValue);
            });

            this.getColumns().add(jumpColumn);
            this.getColumns().add(2,nameColumn);
            this.getColumns().add(3,pathColumn);


        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.comparatorProperty());
        Platform.runLater(() -> {

            setItems(sortedData);
        });


    }


    public void replaceItem(MeterData meterData) {
        System.out.println("old");
        System.out.println(meterData);
        this.data.remove(meterData);
        meterData.load();
        System.out.println("new");
        System.out.println(meterData);
        this.data.add(meterData);
        notifyListeners(new MeterPlanEvent(this, MeterPlanEvent.TYPE.UPDATE));
    }
    public void removeItem(MeterData meterData){
        this.data.remove(meterData);
        notifyListeners(new MeterPlanEvent(this, MeterPlanEvent.TYPE.REMOVE));
    }

    public void addItem(MeterData meterData) {
        this.data.add(meterData);
        notifyListeners(new MeterPlanEvent(this, MeterPlanEvent.TYPE.ADD));
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
                            if (showOnlyOvedue == true) {
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
                                if (!containString.get()) return false;
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
            notifyListeners(new MeterPlanEvent(this, MeterPlanEvent.TYPE.FILTER));
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
        System.out.println(location);
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

    public void addEventListener(MeterPlanEventListener listener) {

        if (this.listeners.getListeners(JEVisEventListener.class).length > 0) {
        }

        this.listeners.add(MeterPlanEventListener.class, listener);
    }

    public void removeEventListener(MeterPlanEventListener listener) {
        this.listeners.remove(MeterPlanEventListener.class, listener);
    }

    public MeterPlanEventListener[] getEventListener() {
        return this.listeners.getListeners(MeterPlanEventListener.class);
    }

    private synchronized void notifyListeners(MeterPlanEvent event) {
        logger.error("SampleHandlerEvent: {}", event);
        for (MeterPlanEventListener l : this.listeners.getListeners(MeterPlanEventListener.class)) {
            l.fireEvent(event);
        }
    }


    public boolean isShowOnlyOvedue() {
        return showOnlyOvedue;
    }

    public void setShowOnlyOvedue(boolean showOnlyOvedue) {
        this.showOnlyOvedue = showOnlyOvedue;
    }

    public MeterData getSelectedItem(){
      return this.getSelectionModel().getSelectedItem();
    }
}
