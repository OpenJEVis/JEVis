package org.jevis.jeconfig.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

public interface JEVisTreeFilter {


    boolean showCell(TreeTableColumn column, JEVisTreeRow row);

    boolean showRow(JEVisTreeItem item);

    boolean showItem(JEVisAttribute attribute);

    boolean showItem(JEVisType type);

    boolean showItem(JEVisObject attribute);

    String getName();


}
