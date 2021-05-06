package org.jevis.jeconfig.plugin.dashboard;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFactoryBox;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameEditor;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.timeframe.ToolBarIntervalSelector;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class LoadDashboardDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(LoadDashboardDialog.class);
    private final ObjectRelations objectRelations;
    private Response response = Response.CANCEL;
    private JEVisObject selectedDashboard = null;
    private final JFXTextField filterInput = new JFXTextField();
    private JFXDatePicker pickerDateEnd = new JFXDatePicker();
    private final FilteredList<JEVisObject> filteredData;
    private final JFXListView<JEVisObject> analysisListView;
    private final JEVisDataSource ds;
    private JFXButton loadButton;
    private JFXButton newButton;
    private JFXButton cancelButton;
    private ToolBarIntervalSelector toolBarIntervalSelector;
    private Interval selectedInterval = null;
    private TimeFrameFactory selectedTimeFactory = null;
    private DateTime selectedDateTime = null;
    JFXButton dateButton = new JFXButton("");

    private DashboardControl control;

    public LoadDashboardDialog(StackPane dialogContainer, JEVisDataSource ds, DashboardControl control) {
        this.control = control;
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);

        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);


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

        updateGridLayout();

        JEVisHelp.getInstance().setActiveSubModule(this.getClass().getSimpleName());
        JEVisHelp.getInstance().update();

        JEVisHelp.getInstance().deactivatePluginModule();
    }

    private void addListener() {

        loadButton.setOnAction(event -> {
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

    }

    public JEVisObject getSelectedDashboard() {
        return selectedDashboard;
    }

    public TimeFrameFactory getTimeFrameFactory() {
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
            timeFactoryBox.getSelectionModel().select(selectedTimeFactory);
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

            HBox buttonBox = new HBox(10);
            Region spacer = new Region();
            cancelButton = new JFXButton(I18n.getInstance().getString("plugin.graph.changedate.cancel"));
            cancelButton.setId("cancel-button");
            loadButton = new JFXButton(I18n.getInstance().getString("plugin.graph.analysis.load"));
            newButton = new JFXButton(I18n.getInstance().getString("plugin.graph.analysis.new"));
            loadButton.setId("ok-button");
            loadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.loaddialog.load")));
            newButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.loaddialog.new")));
            cancelButton.setCancelButton(true);
            loadButton.setDefaultButton(true);

            HBox.setHgrow(loadButton, Priority.NEVER);
            HBox.setHgrow(newButton, Priority.NEVER);
            HBox.setHgrow(cancelButton, Priority.NEVER);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox.setMargin(cancelButton, new Insets(10));
            HBox.setMargin(loadButton, new Insets(10));
            HBox.setMargin(newButton, new Insets(10));
            //HBox.setMargin(drawOptimization, new Insets(10));

            buttonBox.getChildren().setAll(cancelButton, spacer, newButton, loadButton);
            VBox vBox = new VBox(4);
            vBox.setPadding(new Insets(15));

            Separator sep = new Separator(Orientation.HORIZONTAL);

            vBox.getChildren().setAll(gridLayout, sep, buttonBox);

            VBox.setVgrow(analysisListView, Priority.ALWAYS);
            VBox.setVgrow(gridLayout, Priority.ALWAYS);
            VBox.setVgrow(sep, Priority.NEVER);
            VBox.setVgrow(buttonBox, Priority.NEVER);

            setContent(vBox);

            addListener();

            JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), this.getClass().getSimpleName(),
                    JEVisHelp.LAYOUT.HORIZONTAL_TOP_LEFT, pickerDateEnd, analysisListView,
                    loadButton, newButton);
        });
    }


}
