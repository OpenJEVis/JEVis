package org.jevis.jecc.application.jevistree;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JEVisIdTreeTableCell extends TreeTableCell<JEVisTreeRow, Long> {
    private static final Logger logger = LogManager.getLogger(ColumnFactory.class);
    private final StackPane stackPane = new StackPane();
    private final Label label = new Label();

    public JEVisIdTreeTableCell() {
        stackPane.setStyle("-fx-background-color: transparent;");
        label.setStyle("-fx-background-color: transparent;");
        StackPane.setAlignment(stackPane, Pos.CENTER_RIGHT);

        stackPane.getChildren().setAll(label);
    }

    public static Callback<TreeTableColumn<JEVisTreeRow, Long>, TreeTableCell<JEVisTreeRow, Long>> callback() {
        return (param) -> new JEVisIdTreeTableCell();
    }

    @Override
    public void commitEdit(Long newValue) {
        super.commitEdit(newValue);
    }

    @Override
    protected void updateItem(Long item, boolean empty) {
        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
        setText(null);
        setGraphic(null);
        if (!empty) {
            JEVisTreeRow jeVisTreeRow = getTreeTableRow().getItem();
            if (jeVisTreeRow != null) {
                if (jeVisTreeRow.getType() == JEVisTreeRow.TYPE.OBJECT) {
                    label.setText(item + "");
                } else {
                    label.setText("");
                }
            }

            setGraphic(stackPane);
        }
    }
}
