/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.application.unit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.DefaultQuantities;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.unit.dimensions.Currency;
import tech.units.indriya.spi.DefaultServiceProvider;

import javax.measure.Dimension;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.spi.ServiceProvider;
import javax.measure.spi.SystemOfUnits;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TreeView to display JEVIsUnits in JEConfig
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitTree extends TreeView<UnitObject> {

    private static final Logger logger = LogManager.getLogger(UnitTree.class);
    //    private UnitEditor _editor = new UnitEditor();
    private JEVisDataSource _ds;

    private HashMap<String, TreeItem<UnitObject>> itemCache;
    private HashMap<String, UnitGraphic> graphicsCache;
    private HashMap<TreeItem<UnitObject>, ObservableList<TreeItem<UnitObject>>> itemChildren;
    private final ObservableList<TreeItem<UnitObject>> emptyList = FXCollections.emptyObservableList();


    private UnitObject _dragObj;

    public UnitTree() {

    }

    public UnitTree(JEVisDataSource ds) {
        super();
        try {
            _ds = ds;
            itemCache = new HashMap<>();
            graphicsCache = new HashMap<>();
            itemChildren = new HashMap<>();

            setMaxHeight(2014);

            final TreeItem rootNode = new TreeItem(null);
            rootNode.setExpanded(true);
            setShowRoot(false);

            for (Map.Entry<Class, Unit> entry : DefaultQuantities.getClassToSystemUnit().entrySet()) {
                Unit unit = DefaultQuantities.getClassToSystemUnit().get(entry.getKey());
                JEVisUnit jevisUnit = new JEVisUnitImp(unit);
                String translatedUnitName = UnitManager.getInstance().getNameMapQuantities().get(entry.getKey());
                UnitObject quantity = new UnitObject(entry.getValue().getDimension(), jevisUnit, translatedUnitName);

                TreeItem<UnitObject> rootItem = buildItem(quantity);

                addChildren(rootItem);

                rootNode.getChildren().add(rootItem);
            }

            getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
                if (t != null) {
//                        _editor.setUnit(t1.getValue());
                }

            });

            setCellFactory(new Callback<TreeView<UnitObject>, TreeCell<UnitObject>>() {
                @Override
                public TreeCell<UnitObject> call(TreeView<UnitObject> p) {
                    return new ObjectCell() {

                        @Override
                        protected void updateItem(UnitObject item, boolean emty) {
                            super.updateItem(item, emty); //To change body of generated methods, choose Tools | Templates.

                            setText(null);
                            setGraphic(null);

                            if (!emty) {
                                UnitGraphic gc = getObjectGraphic(item);
//                                setText(item);
                                setGraphic(gc.getGraphic());

                            }
                        }

                    };
                }
            });

            //TODO: give this its own id
            setId("objecttree");
//            getStylesheets().add("/styles/Styles.css");

            setPrefWidth(500);

            setRoot(rootNode);

        } catch (Exception ex) {
//            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    private void addChildren(TreeItem<UnitObject> rootItem) {

        Dimension dimension = rootItem.getValue().getUnit().getUnit().getDimension();
        List<SystemOfUnits> availableSystemsOfUnits = new ArrayList<>();
//        ServiceProvider cus = new CommonServiceProvider();
//        ServiceProvider sis = new SIServiceProvider();
//        availableSystemsOfUnits.addAll(cus.getSystemOfUnitsService().getAvailableSystemsOfUnits());
//        availableSystemsOfUnits.addAll(sis.getSystemOfUnitsService().getAvailableSystemsOfUnits());
        ServiceProvider ds = new DefaultServiceProvider();
        availableSystemsOfUnits.addAll(ds.getSystemOfUnitsService().getAvailableSystemsOfUnits());
        Dimension currencyDimension = DefaultQuantities.getUnitForClass(Currency.class).getDimension();
        if (!dimension.equals(currencyDimension)) {
            for (SystemOfUnits systemOfUnits : availableSystemsOfUnits) {
                logger.debug("System of units: {} contains {} units", systemOfUnits.getName(), systemOfUnits.getUnits().size());
                for (Unit unit1 : systemOfUnits.getUnits()) {
                    try {
                        if (unit1.getDimension().equals(dimension)) {
                            JEVisUnit jeVisUnit = new JEVisUnitImp(unit1);
                            UnitObject.Type uoType = null;
                            if (systemOfUnits.getName().equals("SI")) {
                                uoType = UnitObject.Type.SIUnit;
                            } else uoType = UnitObject.Type.NonSIUnit;

                            UnitObject uo = new UnitObject(uoType, jeVisUnit, rootItem.getValue().getID() + jeVisUnit);
                            TreeItem<UnitObject> childUnitObject = buildItem(uo);
                            rootItem.getChildren().add(childUnitObject);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }

        if (!dimension.equals(DefaultQuantities.getClassToSystemUnit().get(Currency.class).getDimension())) {
            for (Unit unit1 : UnitManager.getInstance().getNonSIUnits()) {
                try {
                    if (unit1.getDimension().equals(dimension)) {
                        JEVisUnit jeVisUnit = new JEVisUnitImp(unit1);
                        UnitObject.Type uoType = UnitObject.Type.NonSIUnit;
                        UnitObject uo = new UnitObject(uoType, jeVisUnit, rootItem.getValue().getID() + jeVisUnit);
                        TreeItem<UnitObject> childUnitObject = buildItem(uo);
                        rootItem.getChildren().add(childUnitObject);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

        Dimension dimensionLess = DefaultQuantities.getClassToSystemUnit().get(Dimensionless.class).getDimension();
        if (!dimension.equals(DefaultQuantities.getClassToSystemUnit().get(Dimensionless.class).getDimension())) {
            for (Unit unit1 : UnitManager.getInstance().getAdditionalUnits()) {
                try {
                    if (!unit1.getDimension().equals(dimensionLess)
                            && (!dimension.equals(dimensionLess))
                            && unit1.getDimension().equals(dimension)) {
                        JEVisUnit jeVisUnit = new JEVisUnitImp(unit1);
                        UnitObject.Type uoType = UnitObject.Type.NonSIUnit;
                        UnitObject uo = new UnitObject(uoType, jeVisUnit, rootItem.getValue().getID() + jeVisUnit);
                        TreeItem<UnitObject> childUnitObject = buildItem(uo);
                        rootItem.getChildren().add(childUnitObject);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }

    public UnitGraphic getObjectGraphic(UnitObject object) {
        if (graphicsCache.containsKey(object.toString())) {
            return graphicsCache.get(object.toString());
        }

//        logger.info("grahic does not exist create for: " + object);
        UnitGraphic graph = new UnitGraphic(object, this);
        graphicsCache.put(object.toString(), graph);

        return graph;
    }

    public TreeItem<UnitObject> buildItem(UnitObject object) {

        if (itemCache.containsKey(object.toString())) {
            return itemCache.get(object.toString());
        }

//        logger.info("buildItem: " + object);
        final TreeItem<UnitObject> newItem = new UnitItem(object, this);
        itemCache.put(object.toString(), newItem);

        return newItem;
    }

    public void reload() {

    }

    public UnitObject getSelectedObject() {
        return getSelectionModel().getSelectedItem().getValue();
    }


    /**
     *
     */
    public class ObjectCell extends TreeCell<UnitObject> {

        @Override
        protected void updateItem(final UnitObject obj, boolean emty) {
            super.updateItem(obj, emty);
            if (!emty) {
                UnitGraphic grph = getObjectGraphic(obj);
                setText(grph.getText());
                setGraphic(grph.getGraphic());
                setTooltip(grph.getToolTip());
            }

        }
    }

}
