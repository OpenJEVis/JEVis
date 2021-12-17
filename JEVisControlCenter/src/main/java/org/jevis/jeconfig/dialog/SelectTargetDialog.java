/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.*;
import org.jevis.jeconfig.application.jevistree.filter.BasicCellFilter;
import org.jevis.jeconfig.application.jevistree.filter.FilterFactory;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.filter.ObjectAttributeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SelectTargetDialog extends JFXDialog {

    public static final String VALUE = "Value";
    private final SelectionMode selectionMode;
    private final JEVisTreeFilter basicFilter;
    private final JFXButton ok = new JFXButton("OK");
    private final String ICON = "1404313956_evolution-tasks.png";
    private final StackPane dialogContainer;
    private final JEVisDataSource ds;
    private final List<UserSelection> userSelections;
    private JEVisDataSource _ds;
    private Response response = Response.CANCEL;
    private JEVisTree tree;
    private final SimpleTargetPlugin simpleTargetPlugin = new SimpleTargetPlugin();
    private final ObservableList<JEVisTreeFilter> filterTypes = FXCollections.observableArrayList();
    private JEVisTreeFilter selectedFilter = null;

    private Window dialogOwner = null;

    /**
     * @param filters
     * @param selected Filter who should selected by default, if null the first will be taken.
     */
    public SelectTargetDialog(StackPane dialogContainer, List<JEVisTreeFilter> filters, JEVisTreeFilter basicFilter, JEVisTreeFilter selected, SelectionMode selectionMode, JEVisDataSource ds, List<UserSelection> userSelections) {
        super();
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);
        setMinWidth(450);

        this.dialogContainer = dialogContainer;
        this.ds = ds;
        this.userSelections = userSelections;
        this.filterTypes.addAll(filters);
        this.basicFilter = basicFilter;
        this.selectedFilter = selected;
        this.selectionMode = selectionMode;
        this.ok.getStyleClass().add("button-raised");

        setContent(build(userSelections));
    }

    public static JEVisTreeFilter buildMultiClassFilter(JEVisClass firstClass, List<JEVisClass> classes) {
        String className = "";
        try {
            BasicCellFilter onlyData = new BasicCellFilter(I18nWS.getInstance().getClassName(firstClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(firstClass, ObjectAttributeFilter.NONE);
            filter.forEach(objectAttributeFilter -> {
                onlyData.addItemFilter(objectAttributeFilter);
                onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            for (JEVisClass jeVisClass : classes) {
                ObjectAttributeFilter filter1 = new ObjectAttributeFilter(jeVisClass.getName(), ObjectAttributeFilter.NONE);
                onlyData.addItemFilter(filter1);
                onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, filter1);
            }

            return onlyData;
        } catch (Exception ex) {
        }

        return new BasicCellFilter(className);
    }

    public static JEVisTreeFilter buildCalendarFilter() {
        BasicCellFilter onlyCalender = new BasicCellFilter(I18n.getInstance().getString("tree.filter.calendar"));
        ObjectAttributeFilter c1 = new ObjectAttributeFilter("Custom Period", ObjectAttributeFilter.NONE);
        onlyCalender.addItemFilter(c1);
        onlyCalender.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, c1);
        return onlyCalender;
    }

    public static JEVisTreeFilter buildAllObjects() {
        BasicCellFilter onlyData = new BasicCellFilter(I18n.getInstance().getString("tree.filter.nofilter"));
        ObjectAttributeFilter d1 = new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE);
        onlyData.addItemFilter(d1);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d1);


        return onlyData;
    }

    public static JEVisTreeFilter buildClassFilter(JEVisDataSource ds, String jevisClass) {
        try {
            JEVisClass dsClass = ds.getJEVisClass(jevisClass);
            BasicCellFilter onlyData = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.forEach(objectAttributeFilter -> {
                onlyData.addItemFilter(objectAttributeFilter);
                onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return onlyData;
        } catch (Exception ex) {
        }

        return new BasicCellFilter(jevisClass);
    }

    public void setInitOwner(Window dialogOwner) {
        this.dialogOwner = dialogOwner;
    }

    public static JEVisTreeFilter buildAllAlarms(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Alarm Directory");
            JEVisClass alarmConfigurationClass = ds.getJEVisClass("Alarm Configuration");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(alarmConfigurationClass, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Alarm Directory");
    }

    public static JEVisTreeFilter buildAllDocuments(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Document Directory");
            JEVisClass documentClass = ds.getJEVisClass("Document");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(documentClass, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Document Directory");
    }

    public static JEVisTreeFilter buildAllReports(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Report Directory");
            JEVisClass reportClass = ds.getJEVisClass("Report");
            JEVisClass periodicReportClass = ds.getJEVisClass("Periodic Report");
            JEVisClass emailNotificationClass = ds.getJEVisClass("E-Mail Notification");
            JEVisClass reportLinkDirectoryClass = ds.getJEVisClass("Report Link Directory");
            JEVisClass reportLinkClass = ds.getJEVisClass("Report Link");
            JEVisClass reportAttributeClass = ds.getJEVisClass("Report Attribute");
            JEVisClass reportPeriodConfigurationClass = ds.getJEVisClass("Report Period Configuration");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(reportClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(periodicReportClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(emailNotificationClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(reportLinkDirectoryClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(reportLinkClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(reportAttributeClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(reportPeriodConfigurationClass, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Report Directory");
    }

    public static JEVisTreeFilter buildAllAnalyses(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Analyses Directory");
            JEVisClass dashboardAnalysis = ds.getJEVisClass("Dashboard Analysis");
            JEVisClass analysis = ds.getJEVisClass("Analysis");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(dashboardAnalysis, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(analysis, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Analyses Directory");
    }


    public static JEVisTreeFilter buildAllCalculation(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Calculation");
            JEVisClass inputClass = ds.getJEVisClass("Input");
            JEVisClass outputClass = ds.getJEVisClass("Output");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(inputClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(outputClass, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Calculation");
    }

    public static JEVisTreeFilter buildAllMeasurement(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Measurement Directory");
            JEVisClass dirClass = ds.getJEVisClass("Measurement Instrument");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(dirClass, ObjectAttributeFilter.NONE));
//            filter.addAll(FilterFactory.buildFilterForHeirs(dpClass, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Measurement Directory");
    }


    public static JEVisTreeFilter buildAllDataSources(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Data Server");
            JEVisClass dirClass = ds.getJEVisClass("Data Point Directory");
            JEVisClass dpClass = ds.getJEVisClass("Data Point");
            JEVisClass channelDirectoryClass = ds.getJEVisClass("Channel Directory");
            JEVisClass channelClass = ds.getJEVisClass("Channel");

            BasicCellFilter allFilter = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.addAll(FilterFactory.buildFilterForHeirs(dirClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(dpClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(channelDirectoryClass, ObjectAttributeFilter.NONE));
            filter.addAll(FilterFactory.buildFilterForHeirs(channelClass, ObjectAttributeFilter.NONE));
            filter.forEach(objectAttributeFilter -> {
                allFilter.addItemFilter(objectAttributeFilter);
                allFilter.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return allFilter;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Data Server");
    }


    public static JEVisTreeFilter buildAllDataAndCleanDataFilter() {
        BasicCellFilter onlyData = new BasicCellFilter(I18n.getInstance().getString("tree.filter.dataandcleandata"));
        ObjectAttributeFilter d1 = new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE);
        ObjectAttributeFilter d2 = new ObjectAttributeFilter("Clean Data", ObjectAttributeFilter.NONE);
        ObjectAttributeFilter d3 = new ObjectAttributeFilter("Base Data", ObjectAttributeFilter.NONE);
        ObjectAttributeFilter d4 = new ObjectAttributeFilter("Math Data", ObjectAttributeFilter.NONE);
        onlyData.addItemFilter(d1);
        onlyData.addItemFilter(d2);
        onlyData.addItemFilter(d3);
        onlyData.addItemFilter(d4);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d1);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d2);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d3);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d4);

        return onlyData;
    }

    public static JEVisTreeFilter buildAllDataFilter() {
        BasicCellFilter onlyData = new BasicCellFilter(I18n.getInstance().getString("tree.filter.data"));
        ObjectAttributeFilter d1 = new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE);
        onlyData.addItemFilter(d1);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d1);

        return onlyData;
    }


    public static JEVisTreeFilter buildAllAttributesFilter() {
        BasicCellFilter allAttributes = new BasicCellFilter(I18n.getInstance().getString("tree.filter.allattributes"));
        ObjectAttributeFilter a1 = new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.ALL);
        ObjectAttributeFilter a2 = new ObjectAttributeFilter(ObjectAttributeFilter.NONE, ObjectAttributeFilter.ALL);
        allAttributes.addItemFilter(a1);
        allAttributes.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, a2);
        return allAttributes;
    }

    private VBox build(List<UserSelection> userSelections) {
        VBox root = new VBox(0);
//        root.setPadding(new Insets(10));
        HBox buttonPanel = new HBox(8);
        VBox content = new VBox();


        tree = JEVisTreeFactory.buildBasicDefault(dialogContainer, ds, basicFilter, false);

        tree.getPlugins().add(simpleTargetPlugin);

        tree.getSelectionModel().setSelectionMode(selectionMode);
        if (selectionMode.equals(SelectionMode.SINGLE)) simpleTargetPlugin.setAllowMultiSelection(false);
        else if (selectionMode.equals(SelectionMode.MULTIPLE)) simpleTargetPlugin.setAllowMultiSelection(true);

        content.getChildren().setAll(tree);

        simpleTargetPlugin.setUserSelection(userSelections);

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, filterTypes, finder, false);

        ok.setDefaultButton(true);

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("dialog.selection.cancel"));
        cancel.getStyleClass().add("button-raised");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());

        ok.setOnAction(event -> {
            response = Response.OK;
            close();
        });


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonPanel.getChildren().setAll(searchBar, spacer, cancel, ok);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(12));
//        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL_TOP_LEFT), content, buttonPanel);
        root.getChildren().setAll(content, buttonPanel);
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(tree, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        return root;
    }

    public List<UserSelection> getUserSelection() {
        return simpleTargetPlugin.getUserSelection();
    }

    public void setMode(SimpleTargetPlugin.MODE mode) {
        simpleTargetPlugin.setMode(mode);
    }

    public enum Response {

        OK, CANCEL
    }

    public enum MODE {
        OBJECT, ATTRIBUTE, FILTER
    }

    public JEVisTreeFilter getSelectedFilter() {
        return tree.getFilter();
    }

    public void setFilter(JEVisTreeFilter filter) {
        tree.setFilter(filter);
    }

    public Response getResponse() {
        return response;
    }
}
