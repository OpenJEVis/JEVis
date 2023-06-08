package org.jevis.jeconfig.plugin.object.extension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.extension.paramter.ParameterGui;

import java.util.Optional;

public class HttpChanelExtension implements ObjectEditorExtension {

    private BooleanProperty changed = new SimpleBooleanProperty(false);
    private final JEVisObject jeVisObject;
    private static final Logger logger = LogManager.getLogger(HttpChanelExtension.class);


    private final BorderPane borderPane = new BorderPane();

    private JEVisAttribute pathAttribute;
    private JEVisAttribute parameterAttribute;

    JEVisClass httpChannelClass;

    ParameterGui parameterGui = new ParameterGui();

    public HttpChanelExtension(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {

            return obj.getJEVisClassName().equals(JC.Channel.HTTPChannel.name) ? true : false;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public Node getView() {
        return borderPane;
    }

    public void buildGui() {

        try {
            pathAttribute = jeVisObject.getAttribute(JC.Channel.HTTPChannel.a_Path);
            parameterAttribute = jeVisObject.getAttribute("parameter");


            Node node = parameterGui.buildGui(pathAttribute.getLatestSample(), parameterAttribute.getLatestSample(), I18n.getInstance().getString("plugin.objects.extension.paramter.path"));
            borderPane.setCenter(node);


        } catch (Exception e) {
            logger.error(e);
        }


    }

    @Override
    public void setVisible() {
        buildGui();

    }

    @Override
    public String getTitle() {
        return "Config Parameter";
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
        return parameterGui.save(pathAttribute, parameterAttribute);
    }


    @Override
    public BooleanProperty getValueChangedProperty() {
        return changed;
    }

    @Override
    public void showHelp(boolean show) {

    }


    private Optional<JEVisObject> getParent(JEVisObject jeVisObject) {
        try {

            for (JEVisObject parent : jeVisObject.getParents()) {
                if (parent.getJEVisClass().equals(httpChannelClass)) {
                    return Optional.of(parent);
                }

            }

        } catch (Exception e) {
            logger.error(e);
        }
        return Optional.empty();


    }


}
