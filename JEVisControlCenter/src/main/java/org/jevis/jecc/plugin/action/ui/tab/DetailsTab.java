package org.jevis.jecc.plugin.action.ui.tab;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
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
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.plugin.AnalysisRequest;
import org.jevis.jecc.plugin.action.data.ActionData;
import org.jevis.jecc.plugin.action.data.FreeObject;
import org.jevis.jecc.plugin.action.ui.NumerFormating;
import org.jevis.jecc.plugin.action.ui.TimeRangeDialog;
import org.jevis.jecc.plugin.action.ui.control.TextFieldWithUnit;
import org.jevis.jecc.plugin.charts.ChartPlugin;
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
    private final Label l_energyBefore = new Label("Verbrauch (Referenz)");
    private final Label l_energyAfter = new Label("Verbrauch (Ist)");
    private final Label l_energyChange = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));
    private final Button beforeDateButton = new Button("", ControlCenter.getSVGImage(Icon.CALENDAR, 14, 14));
    private final Button afterDateButton = new Button("", ControlCenter.getSVGImage(Icon.CALENDAR, 14, 14));
    private final Button diffDateButton = new Button("", ControlCenter.getSVGImage(Icon.CALENDAR, 14, 14));
    private final MFXButton buttonOpenAnalysisBefore = new MFXButton("", ControlCenter.getSVGImage(Icon.GRAPH, 14, 14));
    private final Button buttonOpenAnalysisAfter = new Button("", ControlCenter.getSVGImage(Icon.GRAPH, 14, 14));
    private final Button buttonOpenAnalysisDiff = new Button("", ControlCenter.getSVGImage(Icon.GRAPH, 14, 14));
    private final MFXComboBox<JEVisObject> f_EnpiSelection;
    private final Label l_EnpiSelection = new Label("EnPI");
    private final Label l_mediaTags = new Label();
    private final MFXComboBox<String> f_mediaTags;
    private final TextArea f_correctionIfNeeded = new TextArea("");
    private final TextFieldWithUnit f_enpiAfter = new TextFieldWithUnit();
    private final TextFieldWithUnit f_enpiBefore = new TextFieldWithUnit();
    private final TextFieldWithUnit f_enpiDiff = new TextFieldWithUnit();
    private final TextArea f_nextActionIfNeeded = new TextArea("");
    private final TextArea f_alternativAction = new TextArea("");
    private final TextFieldWithUnit f_consumptionBefore = new TextFieldWithUnit();
    private final TextFieldWithUnit f_consumptionAfter = new TextFieldWithUnit();
    private final TextFieldWithUnit f_consumptionDiff = new TextFieldWithUnit();

    private final MFXTextField f_toUser = new MFXTextField();


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

        data.enpi.get().jevisLinkProperty().addListener((observable, oldValue, newValue) -> {
            f_enpiBefore.setEditable(newValue.equals(FreeObject.getInstance().getID().toString()));
            f_enpiAfter.setEditable(newValue.equals(FreeObject.getInstance().getID().toString()));
        });

        f_EnpiSelection = new MFXComboBox(data.getActionPlan().getEnpis());
        f_EnpiSelection.setFloatMode(FloatMode.DISABLED);
        //TODO JFX17

        f_EnpiSelection.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                return object.getName();
            }

            @Override
            public JEVisObject fromString(String string) {
                return f_EnpiSelection.getItems().get(f_EnpiSelection.getSelectedIndex());
            }
        });

        try {
            JEVisObject obj = org.jevis.jecc.plugin.action.data.EmptyObject.getInstance();

            try {
                Long id = Long.parseLong(data.enpiProperty().get().jevisLinkProperty().get());
                if (id.equals(org.jevis.jecc.plugin.action.data.EmptyObject.getInstance().getID())) {
                    obj = org.jevis.jecc.plugin.action.data.EmptyObject.getInstance();
                } else if (id.equals(FreeObject.getInstance().getID())) {
                    obj = FreeObject.getInstance();
                } else {
                    obj = data.getObject().getDataSource().getObject(id);
                }
            } catch (Exception exception) {
                logger.error(exception);
            }
            f_EnpiSelection.getSelectionModel().selectItem(obj);


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        f_EnpiSelection.valueProperty().addListener(new ChangeListener<JEVisObject>() {
            @Override
            public void changed(ObservableValue<? extends JEVisObject> observable, JEVisObject oldValue, JEVisObject newValue) {
                if (newValue == null) {
                    data.enpiProperty().get().jevisLinkProperty().set("");
                } else {
                    data.enpiProperty().get().jevisLinkProperty().set(newValue.getID().toString());

                    DateTime now = new DateTime();
                    if (data.enpiProperty().get().afterFromDate.get() == null) {
                        data.enpiProperty().get().afterFromDate.set(new DateTime(now.getYear(), 1, 1, 0, 0));
                        data.enpiProperty().get().afterUntilDate.set(new DateTime(now.getYear(), 12, 31, 23, 59));
                    }
                    if (data.enpiProperty().get().beforeFromDate.get() == null) {
                        data.enpiProperty().get().beforeFromDate.set(new DateTime(now.getYear() - 1, 1, 1, 0, 0));
                        data.enpiProperty().get().beforeUntilDate.set(new DateTime(now.getYear() - 1, 12, 31, 23, 59));
                    }
                }
            }
        });

        l_mediaTags.setText(I18n.getInstance().getString("actionform.editor.tab.deteils.medium"));
        f_mediaTags = new MFXComboBox<>(data.getActionPlan().getMediumTags());
        f_mediaTags.setFloatMode(FloatMode.DISABLED);
        f_mediaTags.getSelectionModel().selectItem(data.mediaTagsProperty().getValue());
        f_mediaTags.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                data.mediaTagsProperty().set(newValue);
            }
        });

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
                    data.enpiProperty().get().beforeFromDate.get(),
                    data.enpiProperty().get().beforeUntilDate.get());

            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                data.enpiProperty().get().beforeFromDate.set(timeRangeDialog.getFromDate());
                data.enpiProperty().get().beforeUntilDate.set(timeRangeDialog.getUntilDate());
                data.enpiProperty().get().updateEnPIData();
            }
        });
        afterDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    data.enpiProperty().get().afterFromDate.get(),
                    data.enpiProperty().get().afterFromDate.get());
            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                data.enpiProperty().get().afterFromDate.set(timeRangeDialog.getFromDate());
                data.enpiProperty().get().afterFromDate.set(timeRangeDialog.getUntilDate());
                data.enpiProperty().get().updateEnPIData();
            }

        });


        buttonOpenAnalysisBefore.setOnAction(event -> {
            try {
                DateTime from = data.enpiProperty().get().beforeFromDate.get();
                DateTime until = data.enpiProperty().get().beforeUntilDate.get();
                openAnalysis(data, from, until);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonOpenAnalysisAfter.setOnAction(actionEvent -> {
            try {
                DateTime from = data.enpiProperty().get().afterFromDate.get();
                DateTime until = data.enpiProperty().get().afterUntilDate.get();
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
        Bindings.bindBidirectional(f_enpiBefore.getTextField().textProperty(), data.enpiProperty().get().actualProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiBefore.getUnitField().textProperty(), data.enpiProperty().get().unitProperty());
        f_enpiAfter.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_enpiAfter.getTextField().textProperty(), data.enpiProperty().get().afterProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiAfter.getUnitField().textProperty(), data.enpiProperty().get().unitProperty());
        f_enpiDiff.getTextField().setTextFormatter(new TextFormatter<>(nscNoUnit));
        Bindings.bindBidirectional(f_enpiDiff.getTextField().textProperty(), data.enpiProperty().get().diffProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiDiff.getUnitField().textProperty(), data.enpiProperty().get().unitProperty());


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
        Long enpiData = Long.parseLong(data.enpiProperty().get().jevisLinkProperty().get().replace(";", ""));
        JEVisAttribute attribute = data.getObject().getDataSource().getObject(enpiData).getAttribute("Value");

        AnalysisRequest analysisRequest = new AnalysisRequest(attribute.getObject(),
                AggregationPeriod.NONE,
                ManipulationMode.NONE,
                from, until);
        analysisRequest.setAttribute(attribute);
        ControlCenter.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest);
    }

}
