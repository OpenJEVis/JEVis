package org.jevis.jeconfig.plugin.Dashboard.config2;

import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.jeconfig.application.Chart.data.GraphDataModel.BUILDING_CLASS_NAME;
import static org.jevis.jeconfig.application.Chart.data.GraphDataModel.ORGANIZATION_CLASS_NAME;

/**
 * Sort lists of dashboard objects
 * This class is to clean up code an hide this log ugly code...
 */
public class DashboardSorter {

    private static final Logger logger = LogManager.getLogger(DashboardSorter.class);

    /**
     * Sort a list of dashboard objects based on the location in the tree(organisation/building)
     *
     * @param ds
     * @param observableList
     */
    public static void sortDashboards(JEVisDataSource ds, ObservableList<JEVisObject> observableList) {
        List<JEVisObject> listAnalysesDirectories = new ArrayList<>();
        boolean multipleDirectories = false;

        try {
            JEVisClass analysesDirectory = ds.getJEVisClass(GraphDataModel.ANALYSES_DIRECTORY_CLASS_NAME);
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);

            if (listAnalysesDirectories.size() > 1) {
                multipleDirectories = true;
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analyses directories", e);
        }

        AlphanumComparator ac = new AlphanumComparator();
        if (!multipleDirectories) observableList.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        else {
            observableList.sort((o1, o2) -> {

                String prefix1 = "";
                String prefix2 = "";

                try {
                    JEVisObject secondParent1 = o1.getParents().get(0).getParents().get(0);
                    JEVisClass buildingClass = ds.getJEVisClass(BUILDING_CLASS_NAME);
                    JEVisClass organisationClass = ds.getJEVisClass(ORGANIZATION_CLASS_NAME);

                    if (secondParent1.getJEVisClass().equals(buildingClass)) {
                        try {
                            JEVisObject organisationParent = secondParent1.getParents().get(0).getParents().get(0);
                            if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                prefix1 += organisationParent.getName() + " / " + secondParent1.getName() + " / ";
                            }
                        } catch (JEVisException e) {
                            logger.error("Could not get Organization parent of " + secondParent1.getName() + ":" + secondParent1.getID());

                            prefix1 += secondParent1.getName() + " / ";
                        }
                    } else if (secondParent1.getJEVisClass().equals(organisationClass)) {

                        prefix1 += secondParent1.getName() + " / ";

                    }

                } catch (Exception e) {
                }
                prefix1 = prefix1 + o1.getName();

                try {
                    JEVisObject secondParent2 = o2.getParents().get(0).getParents().get(0);
                    JEVisClass buildingClass = ds.getJEVisClass(BUILDING_CLASS_NAME);
                    JEVisClass organisationClass = ds.getJEVisClass(ORGANIZATION_CLASS_NAME);

                    if (secondParent2.getJEVisClass().equals(buildingClass)) {
                        try {
                            JEVisObject organisationParent = secondParent2.getParents().get(0).getParents().get(0);
                            if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                prefix2 += organisationParent.getName() + " / " + secondParent2.getName() + " / ";
                            }
                        } catch (JEVisException e) {
                            logger.error("Could not get Organization parent of " + secondParent2.getName() + ":" + secondParent2.getID());

                            prefix2 += secondParent2.getName() + " / ";
                        }
                    } else if (secondParent2.getJEVisClass().equals(organisationClass)) {

                        prefix2 += secondParent2.getName() + " / ";

                    }

                } catch (Exception e) {
                }
                prefix2 = prefix2 + o2.getName();

                return ac.compare(prefix1, prefix2);
            });
        }
    }
}
