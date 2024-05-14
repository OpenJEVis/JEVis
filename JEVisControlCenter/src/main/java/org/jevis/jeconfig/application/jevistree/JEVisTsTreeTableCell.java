package org.jevis.jeconfig.application.jevistree;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JEVisTsTreeTableCell extends TreeTableCell<JEVisTreeRow, String> {
    private static final Logger logger = LogManager.getLogger(ColumnFactory.class);
    private final HBox hBox = new HBox();//10
    private final Label nameLabel = new Label();

    public JEVisTsTreeTableCell() {


        hBox.setStyle("-fx-background-color: transparent;");
        nameLabel.setStyle("-fx-background-color: transparent;");

        nameLabel.setPadding(new Insets(0, 0, 0, 8));


        hBox.getChildren().addAll(nameLabel);
    }

    public static Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>> callback() {
        return (param) -> new JEVisTsTreeTableCell();
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (!empty
                && getTreeTableRow() != null
                && getTreeTableRow().getTreeItem() != null
                && getTreeTableRow().getTreeItem().getValue() != null
                && getTreeTableRow().getTreeItem().getValue().getJEVisObject() != null) {
            nameLabel.setText(item);
            setGraphic(hBox);
        }
    }
}
