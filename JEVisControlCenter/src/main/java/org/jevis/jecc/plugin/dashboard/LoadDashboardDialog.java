package org.jevis.jecc.plugin.dashboard;

import com.jfoenix.controls.JFXListView;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.dialog.Response;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.jevis.jecc.plugin.dashboard.timeframe.TimeFactoryBox;
import org.jevis.jecc.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jecc.plugin.dashboard.timeframe.TimeFrameEditor;
import org.jevis.jecc.plugin.dashboard.timeframe.ToolBarIntervalSelector;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class LoadDashboardDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(LoadDashboardDialog.class);
    private final ObjectRelations objectRelations;
    private final MFXTextField filterInput = new MFXTextField();
    private final MFXDatePicker pickerDateEnd = new MFXDatePicker();
    private final FilteredList<JEVisObject> filteredData;
    private final JFXListView<JEVisObject> analysisListView;
    private final JEVisDataSource ds;
    private final ToolBarIntervalSelector toolBarIntervalSelector;
    private final DateTime selectedDateTime = null;
    private final DashboardControl control;
    MFXButton dateButton = new MFXButton("");
    private Response response = Response.CANCEL;
    private JEVisObject selectedDashboard = null;
    private Interval selectedInterval = null;
    private TimeFrame selectedTimeFactory = null;

    public LoadDashboardDialog(JEVisDataSource ds, DashboardControl control) {
        this.control = control;
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);

        setTitle(I18n.getInstance().getString("plugin.dashboards.loaddashboard.title"));
        setHeaderText(I18n.getInstance().getString("plugin.dashboards.loaddashboard.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        toolBarIntervalSelector = new ToolBarIntervalSelector(control);

        filteredData = new FilteredList<>(this.control.getAllDashboards());

        filterInput.textProperty().addListener(obs -> {
            String filter = filterInput.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = (objectRelations.getObjectPath(s) + s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (objectRelations.getObjectPath(s) + s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
        });
        KeyCombination help = new KeyCodeCombination(KeyCode.F1);
        filterInput.setOnKeyPressed(event -> {
            if (help.match(event)) {
                JEVisHelp.getInstance().toggleHelp();
                event.consume();
            }
        });

        analysisListView = new JFXListView<>();
        analysisListView.setMinWidth(400);
        analysisListView.setItems(filteredData);
        analysisListView.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) ->
                        Platform.runLater(() ->
                                analysisListView.scrollTo(analysisListView.getSelectionModel().getSelectedIndex())));

        analysisListView.setCellFactory(param -> new ListCell<JEVisObject>() {

            @Override
            protected void updateItem(JEVisObject obj, boolean empty) {
                super.updateItem(obj, empty);
                if (empty || obj == null || obj.getName() == null) {
                    setText("");
                } else {
                    setText(obj.getName());
                    /**
                     if (!analysisDataModel.isMultiSite() && !analysisDataModel.isMultiDir())
                     setText(obj.getName());
                     else {
                     String prefix = "";
                     if (analysisDataModel.isMultiSite())
                     prefix += objectRelations.getObjectPath(obj);
                     if (analysisDataModel.isMultiDir()) {
                     prefix += objectRelations.getRelativePath(obj);
                     }

                     setText(prefix + obj.getName());
                     }
                     **/
                }

            }
        });

        ButtonType okType = new ButtonType(I18n.getInstance().getString("plugin.dashboard.load"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("plugin.graph.changedate.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType newType = new ButtonType(I18n.getInstance().getString("plugin.dashboard.new"), ButtonBar.ButtonData.NEXT_FORWARD);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType, newType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);
        okButton.setId("ok-button");
        okButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.loaddialog.load")));

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);
        cancelButton = new MFXButton(I18n.getInstance().getString(""));
        cancelButton.setId("cancel-button");

        Button newButton = (Button) this.getDialogPane().lookupButton(newType);
        newButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.loaddialog.new")));

        okButton.setOnAction(event -> {
            response = Response.LOAD;
            selectedDashboard = analysisListView.getSelectionModel().getSelectedItem();

            this.close();
        });

        newButton.setOnAction(event -> {
            response = Response.NEW;
            this.close();
        });

        cancelButton.setOnAction(event -> {
            this.close();
        });

        updateGridLayout();

        JEVisHelp.getInstance().setActiveSubModule(this.getClass().getSimpleName());
        JEVisHelp.getInstance().update();

        JEVisHelp.getInstance().deactivatePluginModule();
    }

    public JEVisObject getSelectedDashboard() {
        return selectedDashboard;
    }

    public TimeFrame getTimeFrameFactory() {
        return this.selectedTimeFactory;
    }

    public Interval getSelectedInterval() {
        return selectedInterval;
    }

    public Response getResponse() {
        return response;
    }

    private void updateDateText() {
        dateButton.setText(selectedTimeFactory.format(selectedInterval));
    }

    private void updateGridLayout() {
        Platform.runLater(() -> {
            dateButton.setMinWidth(100);
            //init timefactory
            selectedTimeFactory = control.getAllTimeFrames().week();
            selectedInterval = selectedTimeFactory.getInterval(new DateTime());
            updateDateText();

            TimeFrameEditor timeFrameEditor = new TimeFrameEditor(control.getActiveTimeFrame(), control.getInterval());
            TimeFactoryBox timeFactoryBox = new TimeFactoryBox(false);
            timeFactoryBox.getItems().setAll(FXCollections.observableArrayList(control.getAllTimeFrames().getAll()));
            timeFactoryBox.selectItem(selectedTimeFactory);
            timeFactoryBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                selectedTimeFactory = newValue;
                timeFrameEditor.setTimeFrame(selectedTimeFactory);
                updateDateText();
            });
            analysisListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                //timeFactoryBox.selectValue();
            });
            // ToDo: select default from selected dashboard
            //timeFactoryBox.selectValue(controller.getActiveTimeFrame());


            timeFrameEditor.getIntervalProperty().addListener((observable, oldValue, newValue) -> {
                selectedInterval = newValue;
                updateDateText();
            });

            dateButton.setOnAction(event -> {
                if (timeFrameEditor.isShowing()) {
                    timeFrameEditor.hide();
                } else {
                    timeFrameEditor.setDate(selectedInterval.getEnd());
                    Point2D point = dateButton.localToScreen(0.0, 0.0);
                    timeFrameEditor.show(dateButton, point.getX() - 40, point.getY() + 40);
                    updateDateText();
                }
            });


            timeFactoryBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                timeFrameEditor.setTimeFrame(newValue);
                //if (disableEventListener) return;
                //controller.setActiveTimeFrame(newValue);
            });
            filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

            final Label timeRangeLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.timerange"));
            final Label dateLabel = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.dateselector"));


            Region freeSpace = new Region();
            freeSpace.setPrefWidth(40);

            GridPane gridLayout = new GridPane();
            gridLayout.setPadding(new Insets(10, 10, 10, 10));
            gridLayout.setVgap(10);
//            GridPane.setFillWidth(freeSpace, true);

            gridLayout.add(filterInput, 0, 0, 1, 1);
            gridLayout.add(analysisListView, 0, 1, 1, 13);

            gridLayout.add(freeSpace, 1, 0, 1, 16);

            gridLayout.add(timeRangeLabel, 2, 0, 2, 1);
            gridLayout.add(timeFactoryBox, 3, 1);
            gridLayout.add(dateLabel, 2, 4, 2, 1);
            gridLayout.add(dateButton, 2, 5, 3, 1);

            GridPane.setFillWidth(analysisListView, true);
            GridPane.setFillHeight(analysisListView, true);

            GridPane.setHgrow(analysisListView, Priority.ALWAYS);
            GridPane.setVgrow(analysisListView, Priority.ALWAYS);

            //HBox.setMargin(drawOptimization, new Insets(10));

            VBox.setVgrow(analysisListView, Priority.ALWAYS);
            VBox.setVgrow(gridLayout, Priority.ALWAYS);

            getDialogPane().setContent(gridLayout);

            JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), this.getClass().getSimpleName(),
                    JEVisHelp.LAYOUT.HORIZONTAL_TOP_LEFT, pickerDateEnd, analysisListView);
        });
    }


}
