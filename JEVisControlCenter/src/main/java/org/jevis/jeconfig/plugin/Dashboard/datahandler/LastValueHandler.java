package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.application.jevistree.Finder;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.SearchFilterBar;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.Dashboard.wizzard.Page;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LastValueHandler extends SampleHandler {

    public ObjectProperty<DateTime> lastUpdate = new SimpleObjectProperty<>();
    Map<String, List<JEVisSample>> valueMap = new HashMap<>();
    Map<String, JEVisAttribute> attributeMap = new HashMap<>();
    private BooleanProperty enableMultiSelect = new SimpleBooleanProperty(false);
    private StringProperty unitProperty = new SimpleStringProperty("");
    private SimpleTargetPlugin simpleTargetPlugin = new SimpleTargetPlugin();

    public LastValueHandler(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    public static String generateValueKey(JEVisAttribute attribute) {
        return attribute.getObjectID() + ":" + attribute.getName();
    }

    public Map<String, JEVisAttribute> getAttributeMap() {
        return attributeMap;
    }

    @Override
    public void update() {
        System.out.println("LastValueHandler.update() " + durationProperty.getValue());
        attributeMap.forEach((s, jeVisAttribute) -> {
            System.out.println("Update -> " + s);
            getDataSource().reloadAttribute(jeVisAttribute);
            List<JEVisSample> newSample = jeVisAttribute.getSamples(durationProperty.getValue().getStart(), durationProperty.getValue().getEnd());
            if (newSample != null && !newSample.isEmpty()) {
                try {
                    System.out.println("newSamples:" + newSample.size());
                    valueMap.put(s, newSample);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("no new Samples");
            }

        });
        lastUpdate.setValue(new DateTime());
    }

    public void setMultiSelect(boolean enable) {
        this.enableMultiSelect.set(enable);
    }

    public Map<String, List<JEVisSample>> getValuePropertyMap() {
        return valueMap;
    }


    public StringProperty getUnitProperty() {
        return unitProperty;
    }

    @Override
    public void setUserSelectionDone() {
        System.out.println("Selection Done");
        simpleTargetPlugin.getUserSelection().forEach(userSelection -> {
            System.out.println("Userselect: " + userSelection.getSelectedObject() + "  att: " + userSelection.getSelectedAttribute());
            String key = generateValueKey(userSelection.getSelectedAttribute());
            valueMap.put(key, new ArrayList<>());
            attributeMap.put(key, userSelection.getSelectedAttribute());
        });
    }

    @Override
    public Page getPage() {
        AnchorPane anchorPane = new AnchorPane();


        JEVisTree tree = JEVisTreeFactory.buildBasicDefault(getDataSource());
        tree.getPlugins().add(simpleTargetPlugin);
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        simpleTargetPlugin.setAllowMultiSelection(enableMultiSelect.getValue());
        simpleTargetPlugin.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);

        ObservableList<JEVisTreeFilter> filterTypes = FXCollections.observableArrayList();
        filterTypes.setAll(SelectTargetDialog.buildAllAttributesFilter());

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, filterTypes, finder);

        enableMultiSelect.addListener((observable, oldValue, newValue) -> {
            simpleTargetPlugin.setAllowMultiSelection(newValue);

        });

        anchorPane.getChildren().addAll(tree, searchBar);
        Layouts.setAnchor(tree, 1.0);
        AnchorPane.setBottomAnchor(tree, 40.0);
        AnchorPane.setLeftAnchor(searchBar, 1.0);
        AnchorPane.setBottomAnchor(searchBar, 1.0);
        AnchorPane.setRightAnchor(searchBar, 1.0);


        Page page = new Page() {
            @Override
            public Node getNode() {
                return anchorPane;
            }

            @Override
            public boolean isSkipable() {
                return false;
            }
        };

        return page;
    }
}
