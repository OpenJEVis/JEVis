package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;

import java.util.HashMap;
import java.util.Map;

public class ImageTreeTableCell<S, T> extends TreeTableCell<S, T> {

    private final Map<String, Image> classIconCache = new HashMap<>();

    public ImageTreeTableCell() {

        this.getStyleClass().add("image-tree-table-cell");
        this.setGraphic(null);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn() {
        return (param) -> {
            return new ImageTreeTableCell();
        };
    }

    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setText(null);
            this.setGraphic(null);
        } else if (getTreeTableRow() != null
                && getTreeTableRow().getTreeItem() != null
                && getTreeTableRow().getTreeItem().getValue() != null) {

            if (this.getTreeTableRow().getTreeItem().getValue() instanceof SelectionObject) {

                SelectionObject selectionObject = (SelectionObject) getTreeTableRow().getTreeItem().getValue();
                this.setText((String) item);

                try {
                    ImageView icon = getClassIcon(selectionObject.getObject().getJEVisClass(), 18, 18);
                    setGraphic(icon);
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private ImageView getClassIcon(JEVisClass jclass, double h, double w) throws JEVisException {

        if (!classIconCache.containsKey(jclass.getName())) {
            classIconCache.put(jclass.getName(), SwingFXUtils.toFXImage(jclass.getIcon(), null));
        }

        ImageView iv = new ImageView(classIconCache.get(jclass.getName()));
        iv.fitHeightProperty().setValue(h);
        iv.fitWidthProperty().setValue(w);
        iv.setSmooth(true);
        return iv;
    }
}
