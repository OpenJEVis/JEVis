package org.jevis.jeconfig.tool;

import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.TreeHelper;

import java.util.ArrayList;
import java.util.List;

public class ObjectBin {

    private static final Logger logger = LogManager.getLogger(ObjectBin.class);

    public static void deleteObject(List<TreeItem<JEVisTreeRow>> objects) {
        //user can delete


        objects.forEach(treeItem -> {
            try {
                JEVisObject jeVisObject = treeItem.getValue().getJEVisObject();
                JEVisDataSource ds = jeVisObject.getDataSource();
                JEVisObject binObject = getUserBin(jeVisObject);

                logger.error("Bin Dir: {}", binObject);
                logger.error("Can create in Bin Dir: {}", ds.getCurrentUser().canCreate(binObject.getID()));
                logger.error("Can move Object: {}", ds.getCurrentUser().canDelete(jeVisObject.getID()));
                if (ds.getCurrentUser().canCreate(binObject.getID()) && ds.getCurrentUser().canDelete(jeVisObject.getID())) {
                    List<JEVisObject> objectToMove = new ArrayList<>();
                    objectToMove.add(jeVisObject);
                    TreeHelper.moveObject(objectToMove, binObject);
                }

            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        });

    }

    public static JEVisObject getUserBin(JEVisObject object) {

        try {
            JEVisObject firstBin = CommonMethods.getFirstParentalObjectOfClass(object, "Recycle Bin");
            return firstBin;
        } catch (Exception ex) {
            logger.error(ex, ex);
        }
        return null;
    }

}
