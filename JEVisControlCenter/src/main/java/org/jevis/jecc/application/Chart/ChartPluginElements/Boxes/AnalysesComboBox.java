package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.application.Chart.ChartTools;
import org.jevis.jecc.application.Chart.data.DataModel;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.jecc.application.Chart.data.AnalysisHandler.ANALYSIS_FILE_ATTRIBUTE_NAME;

public class AnalysesComboBox extends MFXComboBox<String> {
    public static final String ORGANIZATION_CLASS_NAME = "Organization";
    public static final String ANALYSES_DIRECTORY_CLASS_NAME = "Analyses Directory";
    public static final String BUILDING_CLASS_NAME = "Building";
    public static final String ANALYSIS_CLASS_NAME = "Analysis";
    public static final String USER_CLASS_NAME = "User";
    private static final Logger logger = LogManager.getLogger(AnalysesComboBox.class);
    private final JEVisDataSource ds;
    private final DataModel dataModel;
    private final ObjectRelations objectRelations;
    private final SimpleListProperty<JEVisObject> analyses = new SimpleListProperty<>(this, "analyses", FXCollections.observableArrayList(new ArrayList<>()));
    private final SimpleObjectProperty<JEVisObject> selectedAnalysis = new SimpleObjectProperty<>(this, "selectedAnalysis", null);
    private boolean updating = false;
    private Boolean multiSite = null;
    private Boolean multiDir = null;

    public AnalysesComboBox(JEVisDataSource ds, DataModel dataModel) {
        super();
        this.ds = ds;
        this.dataModel = dataModel;
        this.objectRelations = new ObjectRelations(ds);
        this.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.list")));
        this.setId("Graph Analysis List");

        JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, this);

        this.analyses.addListener((observable, oldValue, newValue) -> createNameList(newValue));

        this.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            this.updating = true;
            if (t1 != null && t1.intValue() > -1) {
                setSelectedAnalysis(analyses.get(t1.intValue()));
            } else {
                setSelectedAnalysis(null);
            }
            this.updating = false;
        });

        this.selectedAnalysisProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = analyses.indexOf(newValue);

            if (selectedIndex > -1 && analyses.size() > selectedIndex) {
                if (!updating) {
                    getSelectionModel().selectIndex(selectedIndex);
                }

                //TODO: JFX17
//                MFXComboBoxListViewSkin<?> skin = (MFXComboBoxListViewSkin<?>) getSkin();
//                if (skin != null) {
//                    ListView<?> popupContent = (ListView<?>) skin.getPopupContent();
//                    if (popupContent != null) {
//                        Platform.runLater(() -> popupContent.scrollTo(selectedIndex));
//                    }
//                }
            }
        });
    }

    private void createNameList(ObservableList<JEVisObject> changedList) {
        getItems().clear();
        List<String> nameList = new ArrayList<>();
        for (JEVisObject obj : changedList) {
            String name = "";
            try {
                if (obj.getJEVisClassName().equals(ANALYSIS_CLASS_NAME)) {
                    if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, obj))
                        name = obj.getName();
                    else {
                        String prefix = "";
                        if (ChartTools.isMultiSite(ds)) {
                            prefix += objectRelations.getObjectPath(obj);
                        }
                        if (ChartTools.isMultiDir(ds, obj)) {
                            prefix += objectRelations.getRelativePath(obj);
                        }

                        name = prefix + obj.getName();
                    }
                } else {
                    if (obj.getJEVisClassName().equals(USER_CLASS_NAME)) {
                        name = I18n.getInstance().getString("plugin.graph.analysis.tempanalysis");
                    }
                }
            } catch (Exception e) {
                logger.error("could not get JEVisClassName", e);
            }

            if (!name.equals("")) {
                nameList.add(name);
            }
        }

        getItems().addAll(nameList);
    }


    public ObservableList<JEVisObject> getObservableListAnalyses() {
        if (analyses.isEmpty()) updateListAnalyses();
        return analyses;
    }

    public void updateListAnalyses() {
        logger.debug("Updating analyses...");
        DateTime updateStart = new DateTime();

        List<JEVisObject> listAnalysesDirectories = new ArrayList<>();
        multiDir = null;
        multiSite = null;
        JEVisObject selectedAnalysis = selectedAnalysisProperty().get();

        try {
            JEVisClass analysesDirectory = ds.getJEVisClass(ANALYSES_DIRECTORY_CLASS_NAME);
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            logger.error("Error: could not get analyses directories", e);
        }

        List<JEVisObject> analysisObjects = new ArrayList<>();
        if (listAnalysesDirectories.isEmpty()) {
            List<JEVisObject> listBuildings = new ArrayList<>();
            try {
                JEVisClass building = ds.getJEVisClass(BUILDING_CLASS_NAME);
                listBuildings = ds.getObjects(building, false);

                if (!listBuildings.isEmpty()) {
                    JEVisClass analysesDirectory = ds.getJEVisClass(ANALYSES_DIRECTORY_CLASS_NAME);
                    JEVisObject analysesDir = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.graph.analysesdir.defaultname"), analysesDirectory);
                    analysesDir.commit();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not create new analyses directory", e);
            }
        }
        try {
            analysisObjects.addAll(ds.getObjects(ds.getJEVisClass(ANALYSIS_CLASS_NAME), false));
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis", e);
        }

        AlphanumComparator ac = new AlphanumComparator();
        if (!ChartTools.isMultiDir(ds) && !ChartTools.isMultiSite(ds))
            analysisObjects.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        else {
            analysisObjects.sort((o1, o2) -> {

                String prefix1 = "";
                String prefix2 = "";

                if (ChartTools.isMultiSite(ds)) {
                    prefix1 += objectRelations.getObjectPath(o1);
                }
                if (ChartTools.isMultiDir(ds, o1)) {
                    prefix1 += objectRelations.getRelativePath(o1);
                }
                prefix1 += o1.getName();

                if (ChartTools.isMultiSite(ds)) {
                    prefix2 += objectRelations.getObjectPath(o2);
                }
                if (ChartTools.isMultiDir(ds, o2)) {
                    prefix2 += objectRelations.getRelativePath(o2);
                }
                prefix2 += o2.getName();

                return ac.compare(prefix1, prefix2);
            });
        }

        try {
            JEVisObject userObject = ds.getCurrentUser().getUserObject();
            JEVisAttribute analysisFileAttribute = userObject.getAttribute(ANALYSIS_FILE_ATTRIBUTE_NAME);
            if (analysisFileAttribute != null && analysisFileAttribute.hasSample()) {
                analysisObjects.add(userObject);
            }
        } catch (Exception e) {
            logger.error("Error while checking temp analysis attribute", e);
        }

        getAnalyses().clear();
        getAnalyses().addAll(analysisObjects);

        logger.debug("Updated analyses in {}", new Period(new DateTime().getMillis() - updateStart.getMillis()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale())));
        logger.debug("selecting old Analysis...");
        updateStart = new DateTime();

        if (selectedAnalysis != null) {
            int selectedIndex = analyses.indexOf(selectedAnalysis);

            if (selectedIndex > -1 && analyses.size() > selectedIndex) {
                getSelectionModel().selectIndex(selectedIndex);
            }
        }

        logger.debug("Selected old analysis in {}", new Period(new DateTime().getMillis() - updateStart.getMillis()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale())));
    }

    public ObservableList<JEVisObject> getAnalyses() {
        return analyses.get();
    }

    public void setAnalyses(ObservableList<JEVisObject> analyses) {
        this.analyses.set(analyses);
    }

    public SimpleListProperty<JEVisObject> analysesProperty() {
        return analyses;
    }

    public JEVisObject getSelectedAnalysis() {
        return selectedAnalysis.get();
    }

    public void setSelectedAnalysis(JEVisObject selectedAnalysis) {
        this.selectedAnalysis.set(selectedAnalysis);
    }

    public SimpleObjectProperty<JEVisObject> selectedAnalysisProperty() {
        return selectedAnalysis;
    }
}