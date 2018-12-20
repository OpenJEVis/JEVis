package org.jevis.jeconfig.plugin.scada;

import javafx.scene.control.TreeTableColumn;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.TreePlugin;

import java.util.List;

public class ColorSelectionPlugin implements TreePlugin {
    @Override
    public void setTree(JEVisTree tree) {
//        FontSelectorDialog
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        return null;
    }

    @Override
    public void selectionFinished() {

    }

    @Override
    public String getTitle() {
        return null;
    }
}
