package org.jevis.jecc.application.Chart.ChartPluginElements.tree;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.resource.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

public class ImageTreeCell extends TreeCell<JEVisTreeViewItem> {
    public static final Image UNKNOWN_ICON = ResourceLoader.getImage("1393615831_unknown2.png");
    private static final Image ATTRIBUTE_ICON = ResourceLoader.getImage("graphic-design.png");

    private static final Image CALCULATION_DATA = ResourceLoader.getImage("virt_data.png");
    private final static Map<String, Image> classIconCache = new HashMap<>();

    public ImageTreeCell() {
        this.getStyleClass().add("image-tree-cell");
        this.setGraphic(null);
    }

    public static Callback<TreeView<JEVisTreeViewItem>, TreeCell<JEVisTreeViewItem>> callback() {
        return (param) -> new ImageTreeCell();
    }

    public void updateItem(JEVisTreeViewItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else if (item != null) {
            String text = "";
            Node icon = getIcon(UNKNOWN_ICON, 18, 18);
            if (item.getItemType() == JEVisTreeViewItem.ItemType.OBJECT) {
                text = item.getObject().getLocalName(I18n.getInstance().getLocale().getLanguage());
                try {
                    icon = getClassIcon(item.getObject().getJEVisClass(), 18, 18);
                } catch (Exception ignored) {
                }
            } else if (item.getItemType() == JEVisTreeViewItem.ItemType.ATTRIBUTE) {
                text = item.getAttribute().getName();
                icon = getIcon(ATTRIBUTE_ICON, 18, 18);
            }

            setText(text);
            setGraphic(icon);
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

    private ImageView getIcon(Image image, double h, double w) {
        ImageView iv = new ImageView(image);
        iv.fitHeightProperty().setValue(h);
        iv.fitWidthProperty().setValue(w);
        iv.setSmooth(true);
        return iv;
    }
}
