package org.jevis.jeconfig.application.Chart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.relationship.ObjectRelations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox.ANALYSES_DIRECTORY_CLASS_NAME;

public class ChartTools {
    private static final Logger logger = LogManager.getLogger(ChartTools.class);
    private final static Map<Long, Long> calculationMap = new HashMap<>();
    private final static List<Calculation> calculationList = new ArrayList<>();

    public static long isObjectCalculated(JEVisObject object) {
        try {
            return getCalculationMap(object.getDataSource(), object).get(object.getID());
        } catch (Exception e) {
            logger.error("Could not find calculation for object {}:{}", object.getName(), object.getID(), e);
        }

        return -1;
    }

    private static Map<Long, Long> getCalculationMap(JEVisDataSource ds, JEVisObject object) throws JEVisException {
        if (calculationMap.get(object.getID()) != null) {
            return calculationMap;
        } else {

            JEVisClass calculationClass = ds.getJEVisClass("Calculation");

            List<Calculation> missingCalculations = new ArrayList<>();
            for (Calculation calculation : ds.getObjects(calculationClass, true).stream().map(Calculation::new).collect(Collectors.toList())) {
                if (!calculationList.contains(calculation)) {
                    missingCalculations.add(calculation);
                }
            }
            calculationList.addAll(missingCalculations);

            for (Calculation calculation : missingCalculations) {
                try {
                    if (calculation.getTargetObject() != null) {
                        JEVisAttribute targetAttribute = calculation.getTargetObject().getAttribute("Output");
                        if (targetAttribute != null) {
                            try {
                                TargetHelper th = new TargetHelper(ds, targetAttribute);
                                if (th.getObject() != null && !th.getObject().isEmpty()) {
                                    calculationMap.put(th.getObject().get(0).getID(), calculation.getCalculationObject().getID());
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return calculationMap;
    }

    public static boolean isMultiSite(JEVisDataSource ds) {
        if (ds != null) {
            boolean is = false;
            ObjectRelations objectRelations = new ObjectRelations(ds);
            try {
                JEVisClass directoryClass = ds.getJEVisClass(ANALYSES_DIRECTORY_CLASS_NAME);
                List<JEVisObject> objects = ds.getObjects(directoryClass, true);

                List<JEVisObject> buildingParents = new ArrayList<>();
                for (JEVisObject jeVisObject : objects) {
                    JEVisObject buildingParent = objectRelations.getBuildingParent(jeVisObject);
                    if (!buildingParents.contains(buildingParent)) {
                        buildingParents.add(buildingParent);

                        if (buildingParents.size() > 1) {
                            is = true;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return is;
        }

        return false;
    }

    public static boolean isMultiDir(JEVisDataSource ds) {
        if (ds != null) {
            boolean is = false;
            try {
                JEVisClass directoryClass = ds.getJEVisClass(ANALYSES_DIRECTORY_CLASS_NAME);
                List<JEVisObject> objects = ds.getObjects(directoryClass, true);
                if (objects.size() > 1) {
                    is = true;
                }
            } catch (Exception ignored) {
            }
            return is;
        }

        return false;
    }

    static class Calculation {
        private final JEVisObject calculationObject;
        private final List<JEVisObject> inputObjects = new ArrayList<>();
        private JEVisObject targetObject;

        public Calculation(JEVisObject calculationObject) {
            this.calculationObject = calculationObject;

            try {
                JEVisClass outputClass = calculationObject.getDataSource().getJEVisClass("Output");
                JEVisClass inputClass = calculationObject.getDataSource().getJEVisClass("Input");
                for (JEVisObject output : calculationObject.getChildren(outputClass, true)) {
                    this.targetObject = output;
                    break;
                }

                this.inputObjects.addAll(calculationObject.getChildren(inputClass, true));
            } catch (Exception e) {
                logger.error("Could not get output/inputs for {}:{}", calculationObject.getName(), calculationObject.getID(), e);
            }
        }

        public JEVisObject getCalculationObject() {
            return calculationObject;
        }

        public JEVisObject getTargetObject() {
            return targetObject;
        }

        public List<JEVisObject> getInputObjects() {
            return inputObjects;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Calculation) {
                return this.getCalculationObject().equals(((Calculation) obj).getCalculationObject());
            }

            return false;
        }
    }


}
