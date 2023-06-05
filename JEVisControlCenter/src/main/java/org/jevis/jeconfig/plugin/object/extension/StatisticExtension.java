package org.jevis.jeconfig.plugin.object.extension;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.classes.ClassHelper.isDirectory;

public class StatisticExtension implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(StatisticExtension.class);
    private static final String TITLE = I18n.getInstance().getString("plugin.object.statistic.title");
    private final ScrollPane _view = new ScrollPane();
    private final JEVisObject object;


    public StatisticExtension(JEVisObject object) {
        this.object = object;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            return isDirectory(obj.getJEVisClass());
        } catch (Exception ex) {

        }
        return false;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public void setVisible() {
        try {
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(5, 0, 20, 20));
            gridPane.setHgap(10);//7
            gridPane.setVgap(5);

            Map<JEVisClass, List<JEVisObject>> map = new HashMap<>();
            List<JEVisObject> allChildren = getAllChildren(object);

            AlphanumComparator ac = new AlphanumComparator();
            allChildren.sort((o1, o2) -> {
                try {
                    return ac.compare(I18nWS.getInstance().getClassName(o1.getJEVisClassName()), I18nWS.getInstance().getClassName(o2.getJEVisClassName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return -1;
            });

            for (JEVisObject child : allChildren) {
                JEVisClass childClass = child.getJEVisClass();
                if (map.containsKey(childClass)) {
                    map.get(childClass).add(child);
                } else {
                    List<JEVisObject> objects = new ArrayList<>();
                    objects.add(child);
                    map.put(childClass, objects);
                }
            }

            int row = 0;
            for (Map.Entry<JEVisClass, List<JEVisObject>> entry : map.entrySet()) {
                try {
                    Image icon = SwingFXUtils.toFXImage(entry.getKey().getIcon(), null);
                    ImageView imageView = new ImageView(icon);
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(24);


                    MFXComboBox<JEVisObject> box = new MFXComboBox<>();
                    //TODO JFX17
                    box.setConverter(new StringConverter<JEVisObject>() {
                        @Override
                        public String toString(JEVisObject object) {
                            return object.getName();
                        }

                        @Override
                        public JEVisObject fromString(String string) {
                            return box.getItems().get(box.getSelectedIndex());
                        }
                    });

                    box.getItems().addAll(entry.getValue());
                    if (!entry.getValue().isEmpty()) {
                        box.getSelectionModel().selectFirst();
                    }


                    gridPane.add(imageView, 0, row);
                    gridPane.add(new Label(I18nWS.getInstance().getClassName(entry.getKey().getName()) + " x " + entry.getValue().size()), 1, row);
                    gridPane.add(box, 2, row);

                    row++;
                } catch (Exception e) {
                    logger.error("Could not create entry for {}", entry.getKey(), e);
                }
            }
            _view.setContent(gridPane);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private List<JEVisObject> getAllChildren(JEVisObject obj) {
        List<JEVisObject> children = new ArrayList<>();

        try {
            getAllChildrenRec(children, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return children;
    }

    private void getAllChildrenRec(List<JEVisObject> children, JEVisObject obj) throws JEVisException {
        for (JEVisObject child : obj.getChildren()) {
            children.add(child);
            getAllChildrenRec(children, child);
        }
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean needSave() {
        return false;
    }

    @Override
    public void dismissChanges() {

    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    public void showHelp(boolean show) {

    }
}
