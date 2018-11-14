package org.jevis.application.jevistree.filter;

import javafx.scene.control.TreeTableColumn;
import org.jevis.api.JEVisException;
import org.jevis.application.jevistree.JEVisTreeRow;

public interface CellFilter {


    boolean showCell(TreeTableColumn<JEVisTreeRow, String> column, JEVisTreeRow row) throws JEVisException;

}
