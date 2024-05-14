package org.jevis.jeconfig.application.jevistree;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.resource.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

public class JEVisNameTreeTableCell extends TreeTableCell<JEVisTreeRow, JEVisTreeRow> {
    public static final Image UNKNOWN_ICON = ResourceLoader.getImage("1393615831_unknown2.png");
    private static final Logger logger = LogManager.getLogger(ColumnFactory.class);
    private static final Image ATTRIBUTE_ICON = ResourceLoader.getImage("graphic-design.png");
    private static final Map<String, Image> classIconCache = new HashMap<>();
    final HBox hbox = new HBox();
    final VBox vbox = new VBox();
    final Label nameLabel = new Label();
    final Region spaceBetween = new Region();
    final ImageView icon = ResourceLoader.getImage("1393615831_unknown2.png", 18, 18);

    public JEVisNameTreeTableCell() {
        hbox.setStyle("-fx-background-color: transparent;");
        nameLabel.setStyle("-fx-background-color: transparent;");
        nameLabel.setPadding(new Insets(0, 0, 0, 0));
        spaceBetween.setMinWidth(8);
        hbox.getChildren().setAll(icon, spaceBetween, nameLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().setAll(hbox);

        icon.fitHeightProperty().setValue(18);
        icon.fitWidthProperty().setValue(18);
        icon.setSmooth(true);
    }

    public static Callback<TreeTableColumn<JEVisTreeRow, JEVisTreeRow>, TreeTableCell<JEVisTreeRow, JEVisTreeRow>> callback() {
        return (param) -> new JEVisNameTreeTableCell();
    }

    @Override
    public void commitEdit(JEVisTreeRow newValue) {
        super.commitEdit(newValue);
    }

    @Override
    protected void updateItem(JEVisTreeRow item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (!empty && item != null
                && getTreeTableRow() != null
                && getTreeTableRow().getTreeItem() != null
                && getTreeTableRow().getTreeItem().getValue() != null
                && getTreeTableRow().getTreeItem().getValue().getJEVisObject() != null) {

            try {
                JEVisObject jeVisObject = item.getJEVisObject();

                if (item.getType() == JEVisTreeRow.TYPE.OBJECT) {
                    nameLabel.setText(jeVisObject.getName());
                    try {
                        JEVisTree jevisTree = (JEVisTree) this.getTreeTableView();
                        if (!jeVisObject.getJEVisClassName().equals("Link")) {
                            icon.setImage(getClassIcon(jeVisObject.getJEVisClass()));

                            try {
                                if (jevisTree.getCalculationIDs().get(jeVisObject.getID()) != null) {
                                    {
                                        if (!classIconCache.containsKey("Fake_Virtual_DataPoints")) {
                                            classIconCache.put("Fake_Virtual_DataPoints", JEConfig.getImage("virt_data.png"));
                                        }
                                        icon.setImage(classIconCache.get("Fake_Virtual_DataPoints"));
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        } else {
                            JEVisObject linkedObject = jeVisObject.getLinkedObject();
                            icon.setImage(getClassIcon(linkedObject.getJEVisClass()));
                        }
                    } catch (Exception ex) {
                        icon.setImage(UNKNOWN_ICON);
                    }
                } else {
                    nameLabel.setText(I18nWS.getInstance().getAttributeName(item.getJEVisAttribute()));
                    icon.setImage(ATTRIBUTE_ICON);
                }
                setGraphic(vbox);
            } catch (Exception ex) {
                logger.catching(ex);
                setGraphic(null);
            }
        }
    }

    private Image getClassIcon(JEVisClass jclass) throws JEVisException {

        if (!classIconCache.containsKey(jclass.getName())) {
            classIconCache.put(jclass.getName(), SwingFXUtils.toFXImage(jclass.getIcon(), null));
        }
        return classIconCache.get(jclass.getName());
    }
}
