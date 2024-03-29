package org.jevis.jeconfig.plugin.action.ui.tab;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.EmptyObject;
import org.jevis.jeconfig.plugin.action.data.FreeObject;
import org.jevis.jeconfig.plugin.action.data.Medium;
import org.jevis.jeconfig.plugin.action.ui.NumerFormating;
import org.jevis.jeconfig.plugin.action.ui.TimeRangeDialog;
import org.jevis.jeconfig.plugin.action.ui.control.TextFieldWithUnit;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

import java.util.Optional;

public class DetailsTab extends Tab {
    private static final Logger logger = LogManager.getLogger(DetailsTab.class);

    private final Label l_enpiAfter = new Label(I18n.getInstance().getString("plugin.action.enpiafter"));
    private final Label l_enpiBefore = new Label(I18n.getInstance().getString("plugin.action.enpiabefore"));
    private final Label l_enpiChange = new Label(I18n.getInstance().getString("plugin.action.enpiabechange"));
    private final Label l_correctionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.correction"));
    private final Label l_nextActionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.followupaction"));
    private final Label l_alternativAction = new Label(I18n.getInstance().getString("plugin.action.alternativaction"));
    private final Label l_energyBefore = new Label(I18n.getInstance().getString("plugin.action.consumption.before"));//"Verbrauch (Referenz)");f_consumptionBefore
    private final Label l_energyAfter = new Label(I18n.getInstance().getString("plugin.action.consumption.after"));//"Verbrauch (Ist)");
    private final Label l_energyChange = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));
    private final Button beforeDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
    private final Button afterDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
    private final Button diffDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
    private final JFXButton buttonOpenAnalysisBefore = new JFXButton("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
    private final Button buttonOpenAnalysisAfter = new Button("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
    private final Button buttonOpenAnalysisaDiff = new Button("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
    private final JFXComboBox<JEVisObject> f_EnpiSelection;
    private final Label l_EnpiSelection = new Label(I18n.getInstance().getString("plugin.action.enpi"));//"EnPI"
    private final Label l_mediaTags = new Label();
    private final JFXComboBox<Medium> f_mediaTags;
    private final TextArea f_correctionIfNeeded = new TextArea("");
    private final TextFieldWithUnit f_enpiAfter = new TextFieldWithUnit();
    private final TextFieldWithUnit f_enpiBefore = new TextFieldWithUnit();
    private final TextFieldWithUnit f_enpiDiff = new TextFieldWithUnit();
    private final TextArea f_nextActionIfNeeded = new TextArea("");
    private final TextArea f_alternativAction = new TextArea("");
    private final TextFieldWithUnit f_consumptionBefore = new TextFieldWithUnit();
    private final TextFieldWithUnit f_consumptionAfter = new TextFieldWithUnit();
    private final TextFieldWithUnit f_consumptionDiff = new TextFieldWithUnit();

    private final JFXTextField f_toUser = new JFXTextField();


    public DetailsTab(ActionData data) {
        super(I18n.getInstance().getString("actionform.editor.tab.deteils"));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(15);
        gridPane.setHgap(15);

        scrollPane.setContent(gridPane);


        diffDateButton.setVisible(false);
        f_consumptionDiff.setEditable(false);
        f_enpiDiff.setEditable(false);

        data.EnPI.get().jevisLinkProperty().addListener((observable, oldValue, newValue) -> {
            f_enpiBefore.setEditable(newValue.equals(FreeObject.getInstance().getID().toString()));
            f_enpiAfter.setEditable(newValue.equals(FreeObject.getInstance().getID().toString()));
        });

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> enpiCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        };
        f_EnpiSelection = new JFXComboBox(data.getActionPlan().getEnpis());
        f_EnpiSelection.setCellFactory(enpiCellFactory);
        f_EnpiSelection.setButtonCell(enpiCellFactory.call(null));
        try {
            JEVisObject obj = EmptyObject.getInstance();

            try {
                Long id = Long.parseLong(data.EnPIProperty().get().jevisLinkProperty().get());
                if (id.equals(EmptyObject.getInstance().getID())) {
                    obj = EmptyObject.getInstance();
                } else if (id.equals(FreeObject.getInstance().getID())) {
                    obj = FreeObject.getInstance();
                } else {
                    obj = data.getObject().getDataSource().getObject(id);
                }
            } catch (Exception exception) {
                logger.error(exception);
            }
            f_EnpiSelection.getSelectionModel().select(obj);


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        f_EnpiSelection.valueProperty().addListener(new ChangeListener<JEVisObject>() {
            @Override
            public void changed(ObservableValue<? extends JEVisObject> observable, JEVisObject oldValue, JEVisObject newValue) {
                if (newValue == null) {
                    data.EnPIProperty().get().jevisLinkProperty().set("");
                } else {
                    data.EnPIProperty().get().jevisLinkProperty().set(newValue.getID().toString());

                    DateTime now = new DateTime();
                    if (data.EnPIProperty().get().afterFromDate.get() == null) {
                        data.EnPIProperty().get().afterFromDate.set(new DateTime(now.getYear(), 1, 1, 0, 0));
                        data.EnPIProperty().get().afterUntilDate.set(new DateTime(now.getYear(), 12, 31, 23, 59));
                    }
                    if (data.EnPIProperty().get().beforeFromDate.get() == null) {
                        data.EnPIProperty().get().beforeFromDate.set(new DateTime(now.getYear() - 1, 1, 1, 0, 0));
                        data.EnPIProperty().get().beforeUntilDate.set(new DateTime(now.getYear() - 1, 12, 31, 23, 59));
                    }
                }
            }
        });

        l_mediaTags.setText(I18n.getInstance().getString("actionform.editor.tab.deteils.medium"));

        f_mediaTags = new JFXComboBox<>(data.getActionPlan().getMedium());
        f_mediaTags.setConverter(new StringConverter<Medium>() {
            @Override
            public String toString(Medium medium) {
                return medium.getName();
            }

            @Override
            public Medium fromString(String s) {

                Optional<Medium> medium = data.getActionPlan().getMedium().stream().filter(m -> m.getId().equals(s)).findFirst();
                if (medium.isPresent()) {
                    return medium.get();
                } else {
                    return new Medium(s, "Error (" + s + ")", 0);
                }
            }
        });

        f_mediaTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Medium>() {
            @Override
            public void changed(ObservableValue<? extends Medium> observable, Medium oldValue, Medium newValue) {
                data.mediaTagsProperty().set(newValue.getId());
            }
        });

        /*

        //_mediaTags = new JFXComboBox<>(data.getActionPlan().getMediumTags());
        f_mediaTags.getSelectionModel().select(data.mediaTagsProperty().getValue());
        f_mediaTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                data.mediaTagsProperty().set(newValue);
            }
        });

         */

        Region spacerEnpiBefore = new Region();
        Region spacerEnpiAfter = new Region();
        Region spacerEnpiDiff = new Region();
        HBox.setHgrow(spacerEnpiBefore, Priority.ALWAYS);
        HBox.setHgrow(spacerEnpiAfter, Priority.ALWAYS);
        HBox.setHgrow(spacerEnpiDiff, Priority.ALWAYS);

        HBox box_EnpiBefore = new HBox(buttonOpenAnalysisBefore, beforeDateButton, spacerEnpiBefore, f_enpiBefore);
        HBox box_EnpiAfter = new HBox(buttonOpenAnalysisAfter, afterDateButton, spacerEnpiBefore, f_enpiAfter);
        HBox box_EnpiDiff = new HBox(new Region(), new Region(), spacerEnpiBefore, f_enpiDiff);

        f_enpiAfter.widthProperty().addListener((observableValue, number, t1) -> f_enpiDiff.setMinWidth(t1.doubleValue()));

        box_EnpiAfter.setSpacing(8);
        box_EnpiBefore.setSpacing(8);
        box_EnpiDiff.setSpacing(8);
        HBox.setHgrow(f_enpiAfter, Priority.SOMETIMES);
        HBox.setHgrow(f_enpiBefore, Priority.SOMETIMES);

        beforeDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    data.EnPIProperty().get().beforeFromDate.get(),
                    data.EnPIProperty().get().beforeUntilDate.get());

            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                data.EnPIProperty().get().beforeFromDate.set(timeRangeDialog.getFromDate());
                data.EnPIProperty().get().beforeUntilDate.set(timeRangeDialog.getUntilDate());
                data.EnPIProperty().get().updateEnPIData();
            }
        });
        afterDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    data.EnPIProperty().get().afterFromDate.get(),
                    data.EnPIProperty().get().afterFromDate.get());
            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                data.EnPIProperty().get().afterFromDate.set(timeRangeDialog.getFromDate());
                data.EnPIProperty().get().afterFromDate.set(timeRangeDialog.getUntilDate());
                data.EnPIProperty().get().updateEnPIData();
            }

        });


        buttonOpenAnalysisBefore.setOnAction(event -> {
            try {
                DateTime from = data.EnPIProperty().get().beforeFromDate.get();
                DateTime until = data.EnPIProperty().get().beforeUntilDate.get();
                openAnalysis(data, from, until);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonOpenAnalysisAfter.setOnAction(actionEvent -> {
            try {
                DateTime from = data.EnPIProperty().get().afterFromDate.get();
                DateTime until = data.EnPIProperty().get().afterUntilDate.get();
                openAnalysis(data, from, until);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        NumberStringConverter nsc = NumerFormating.getInstance().getCurrencyConverter();
        NumberStringConverter nscNoUnit = NumerFormating.getInstance().getDoubleConverter();

        f_consumptionBefore.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_consumptionBefore.getTextField().textProperty(), data.consumptionProperty().get().actualProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_consumptionBefore.getUnitField().textProperty(), data.consumptionProperty().get().unitProperty());
        f_consumptionAfter.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_consumptionAfter.getTextField().textProperty(), data.consumptionProperty().get().afterProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_consumptionAfter.getUnitField().textProperty(), data.consumptionProperty().get().unitProperty());
        f_consumptionDiff.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_consumptionDiff.getTextField().textProperty(), data.consumptionProperty().get().diffProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_consumptionDiff.getUnitField().textProperty(), data.consumptionProperty().get().unitProperty());
        f_enpiBefore.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_enpiBefore.getTextField().textProperty(), data.EnPIProperty().get().actualProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiBefore.getUnitField().textProperty(), data.EnPIProperty().get().unitProperty());
        f_enpiAfter.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_enpiAfter.getTextField().textProperty(), data.EnPIProperty().get().afterProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiAfter.getUnitField().textProperty(), data.EnPIProperty().get().unitProperty());
        f_enpiDiff.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_enpiDiff.getTextField().textProperty(), data.EnPIProperty().get().diffProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiDiff.getUnitField().textProperty(), data.EnPIProperty().get().unitProperty());


        Bindings.bindBidirectional(f_nextActionIfNeeded.textProperty(), data.noteAlternativeMeasuresProperty());
        Bindings.bindBidirectional(f_correctionIfNeeded.textProperty(), data.noteCorrectionProperty());

        //f_consumptionBefore.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));

        f_correctionIfNeeded.setPrefWidth(400);
        f_nextActionIfNeeded.setPrefWidth(400);
        //f_alternativAction.setPrefWidth(400);

        f_enpiAfter.setAlignment(Pos.CENTER_RIGHT);
        f_enpiBefore.setAlignment(Pos.CENTER_RIGHT);
        f_enpiDiff.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHgrow(f_EnpiSelection, Priority.ALWAYS);
        GridPane.setHgrow(f_mediaTags, Priority.ALWAYS);
        GridPane.setFillWidth(f_EnpiSelection, true);
        GridPane.setFillWidth(f_mediaTags, true);


        //Workaround
        f_EnpiSelection.setPrefWidth(320);
        f_mediaTags.setPrefWidth(320);

        f_nextActionIfNeeded.setWrapText(true);
        f_alternativAction.setWrapText(true);
        f_correctionIfNeeded.setWrapText(true);

        gridPane.addRow(0, l_mediaTags, f_mediaTags, new Region(), l_EnpiSelection, f_EnpiSelection);
        gridPane.addRow(1, l_energyBefore, f_consumptionBefore, new Region(), l_enpiBefore, box_EnpiBefore);
        gridPane.addRow(2, l_energyAfter, f_consumptionAfter, new Region(), l_enpiAfter, box_EnpiAfter);
        gridPane.addRow(3, l_energyChange, f_consumptionDiff, new Region(), l_enpiChange, box_EnpiDiff);
        gridPane.addRow(4, new Region());

        gridPane.add(l_correctionIfNeeded, 0, 7);
        gridPane.add(f_correctionIfNeeded, 0, 8);

        gridPane.add(l_nextActionIfNeeded, 3, 7);
        gridPane.add(f_nextActionIfNeeded, 3, 8);

        GridPane.setColumnSpan(l_correctionIfNeeded, 2);
        GridPane.setColumnSpan(l_nextActionIfNeeded, 2);
        GridPane.setColumnSpan(f_correctionIfNeeded, 2);
        GridPane.setColumnSpan(f_nextActionIfNeeded, 2);
        GridPane.setColumnSpan(l_alternativAction, 2);
        GridPane.setColumnSpan(f_alternativAction, 2);

        setContent(scrollPane);

    }

    private void openAnalysis(ActionData data, DateTime from, DateTime until) throws Exception {
        Long enpiData = Long.parseLong(data.EnPIProperty().get().jevisLinkProperty().get().replace(";", ""));
        JEVisAttribute attribute = data.getObject().getDataSource().getObject(enpiData).getAttribute("Value");

        AnalysisRequest analysisRequest = new AnalysisRequest(attribute.getObject(),
                AggregationPeriod.NONE,
                ManipulationMode.NONE,
                from, until);
        analysisRequest.setAttribute(attribute);
        JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest);
    }

}
