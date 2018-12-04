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
package org.jevis.application.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.I18nWS;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.*;
import org.jevis.application.jevistree.filter.BasicCellFilter;
import org.jevis.application.jevistree.filter.FilterFactory;
import org.jevis.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.application.jevistree.filter.ObjectAttributeFilter;
import org.jevis.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.application.resource.ResourceLoader;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SelectTargetDialog {

    private static SaveResourceBundle bundle = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private Button ok = new Button("OK");
    private String ICON = "1404313956_evolution-tasks.png";
    private JEVisDataSource _ds;
    private Stage stage;
    private Response _response = Response.CANCEL;
    private JEVisTree tree;
    private SimpleTargetPlugin simpleTargetPlugin = new SimpleTargetPlugin();
    private ObservableList<JEVisTreeFilter> filterTypes = FXCollections.observableArrayList();
    private JEVisTreeFilter selectedFilter = null;


    /**
     * @param filter
     * @param selected Filter who should selected by default, if null the first will be taken.
     */
    public SelectTargetDialog(List<JEVisTreeFilter> filter, JEVisTreeFilter selected) {
        this.filterTypes.addAll(filter);
        this.selectedFilter = selected;
    }

    public static JEVisTreeFilter buildCalanderFilter() {
        BasicCellFilter onlyCalender = new BasicCellFilter(bundle.getString("tree.filter.calender"));
        ObjectAttributeFilter c1 = new ObjectAttributeFilter("Custom Period", ObjectAttributeFilter.NONE);
        onlyCalender.addItemFilter(c1);
        onlyCalender.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, c1);
        return onlyCalender;
    }

    public static JEVisTreeFilter buildAllObjects() {
        BasicCellFilter onlyData = new BasicCellFilter(bundle.getString("tree.filter.nofilter"));
        ObjectAttributeFilter d1 = new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.NONE);
        onlyData.addItemFilter(d1);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d1);


        return onlyData;
    }

    public static JEVisTreeFilter buildAllDataSources(JEVisDataSource ds) {
        try {
            JEVisClass dsClass = ds.getJEVisClass("Data Server");
            BasicCellFilter onlyData = new BasicCellFilter(I18nWS.getInstance().getClassName(dsClass));
            List<ObjectAttributeFilter> filter = FilterFactory.buildFilterForHeirs(dsClass, ObjectAttributeFilter.NONE);
            filter.forEach(objectAttributeFilter -> {
                onlyData.addItemFilter(objectAttributeFilter);
                onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, objectAttributeFilter);
            });

            return onlyData;
        } catch (Exception ex) {
        }

        return new BasicCellFilter("Data Server");
    }


    public static JEVisTreeFilter buildAllDataFilter() {
        BasicCellFilter onlyData = new BasicCellFilter(bundle.getString("tree.filter.data"));
        ObjectAttributeFilter d1 = new ObjectAttributeFilter("Data", ObjectAttributeFilter.NONE);
        ObjectAttributeFilter d2 = new ObjectAttributeFilter("Clean Data", ObjectAttributeFilter.NONE);
        onlyData.addItemFilter(d1);
        onlyData.addItemFilter(d2);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d1);
        onlyData.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, d2);

        return onlyData;
    }


    public static JEVisTreeFilter buildAllAttributesFilter() {
        BasicCellFilter allAttributes = new BasicCellFilter(bundle.getString("tree.filter.allattributes"));
        ObjectAttributeFilter a1 = new ObjectAttributeFilter(ObjectAttributeFilter.ALL, ObjectAttributeFilter.ALL);
        ObjectAttributeFilter a2 = new ObjectAttributeFilter(ObjectAttributeFilter.NONE, ObjectAttributeFilter.ALL);
        allAttributes.addItemFilter(a1);
        allAttributes.addFilter(SimpleTargetPlugin.TARGET_COLUMN_ID, a2);
        return allAttributes;
    }

    public Response show(Stage owner, JEVisDataSource ds, String title, List<UserSelection> uselection) {
        stage = new Stage();
        _ds = ds;

        stage.setTitle(bundle.getString("dialog.selection.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        VBox root = build(ds, title, uselection);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(700);
        stage.setHeight(800);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.getIcons().setAll(ResourceLoader.getImage(ICON, 64, 64).getImage());
        stage.setAlwaysOnTop(true);
//        stage.sizeToScene();
        stage.toFront();
        stage.showAndWait();

        return _response;
    }

    public void allowMultySelect(boolean allowMulty) {
        simpleTargetPlugin.setAllowMultySelection(allowMulty);
    }

    private VBox build(JEVisDataSource ds, String title, List<UserSelection> uselection) {
        VBox root = new VBox(0);
//        root.setPadding(new Insets(10));
        Node header = DialogHeader.getDialogHeader(ICON, title);
        HBox buttonPanel = new HBox(8);
        VBox content = new VBox();


        tree = JEVisTreeFactory.buildBasicDefault(ds);
        tree.getPlugins().add(simpleTargetPlugin);
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        content.getChildren().setAll(tree);

        simpleTargetPlugin.setUserSelection(uselection);

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, filterTypes, finder);


        ok.setDefaultButton(true);

        Button cancel = new Button(bundle.getString("dialog.selection.cancel"));
        cancel.setCancelButton(true);
        cancel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                stage.hide();
            }
        });

        ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                _response = Response.OK;
                stage.hide();
            }
        });


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonPanel.getChildren().setAll(searchBar, spacer, cancel, ok);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(5));


//        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), content, buttonPanel);
        root.getChildren().setAll(header, content, buttonPanel);
        VBox.setVgrow(header, Priority.NEVER);
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(tree, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        return root;
    }

    public List<UserSelection> getUserSelection() {
        return simpleTargetPlugin.getUserSelection();
    }


    public enum Response {

        OK, CANCEL
    }

    public enum MODE {
        OBJECT, ATTRIBUTE, FILTER
    }

}
