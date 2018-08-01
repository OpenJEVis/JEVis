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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.extension.calculation.CalculationViewController;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

public class CalculationExtension implements ObjectEditorExtension  {

    public static final String CALC_CLASS_NAME = "Calculation";
    private static final String TITLE = I18n.getInstance().getString("plugin.object.calculation");
    private final org.apache.logging.log4j.Logger log = LogManager.getLogger(CalculationExtension.class);
    private final BorderPane view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisObject _obj;
    private CalculationViewController contol;
    private String oldExpression = "";

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
            contol = fxmlLoader.getController();
            contol.setData(_obj);

            JEVisAttribute aExprsssion = _obj.getAttribute("Expression");
            JEVisSample lastValue = aExprsssion.getLatestSample();

            if(lastValue!=null){
                System.out.println("LastSample: "+lastValue.getTimestamp()+" "+lastValue.getValueAsString());
                oldExpression=lastValue.getValueAsString();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
//            editConfigPane = FXMLLoader.load(getClass().getResource("/fxml/EditConfiguration.fxml"));



        ap.getChildren().add(editConfigPane);
        view.setCenter(new ScrollPane(ap));

    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean needSave() {


        return  _changed.getValue();
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
    }

    @Override
    public boolean save() {
        try {
            _changed.setValue(!contol.getFormel().equals(oldExpression));
            if(needSave()){
                String newExpression = contol.getFormel();
                JEVisAttribute aExprsssion = _obj.getAttribute("Expression");

                JEVisSample newSample =aExprsssion.buildSample(new DateTime(),newExpression);
                newSample.commit();
                oldExpression=newExpression;
                _changed.setValue(false);
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }
}
