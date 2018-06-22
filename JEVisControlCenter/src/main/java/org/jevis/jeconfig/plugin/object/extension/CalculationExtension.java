package org.jevis.jeconfig.plugin.object.extension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.tool.I18n;

import java.io.IOException;

public class CalculationExtension implements ObjectEditorExtension {

    public static final String CALC_CLASS_NAME = "Calculation";
    private static final String TITLE = I18n.getInstance().getString("plugin.object.calculation");
    private final org.apache.logging.log4j.Logger log = LogManager.getLogger(CalculationExtension.class);
    private final BorderPane view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisObject _obj;

    public CalculationExtension(JEVisObject _obj) {
        this._obj = _obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        Boolean isCalcObject = false;
        try {
            isCalcObject = obj.getJEVisClassName().equals(CALC_CLASS_NAME);
        } catch (JEVisException e) {
            log.error("Could not get object type" + e.getLocalizedMessage());
        }
        return isCalcObject;
    }


    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void setVisible() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);

        AnchorPane ap = new AnchorPane();

        //    Button button = new Button();
        //  button.setText("Calc");
        //ap.getChildren().add(button);

        Pane editConfigPane = new Pane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EditCalculation.fxml"));
        //fxmlLoader.setRoot();
        //fxmlLoader.setController(new CalculationViewController());
        try {
            editConfigPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
//            editConfigPane = FXMLLoader.load(getClass().getResource("/fxml/EditConfiguration.fxml"));

        ap.getChildren().add(editConfigPane);
        view.setCenter(ap);

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
        _changed.setValue(false);
    }

    @Override
    public boolean save() {
        return _changed.getValue();
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }
}
