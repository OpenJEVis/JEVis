package org.jevis.jecc.plugin.action.ui.tab;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.plugin.action.data.ActionData;
import org.jevis.jecc.plugin.action.data.FreeObject;
import org.jevis.jecc.plugin.action.ui.NumerFormating;
import org.jevis.jecc.plugin.action.ui.TimeRangeDialog;
import org.jevis.jecc.plugin.action.ui.control.TextFieldWithUnit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Optional;

public class DetailsTab extends Tab {


    private Label l_enpiAfter = new Label(I18n.getInstance().getString("plugin.action.enpiafter"));
    private Label l_enpiBefore = new Label(I18n.getInstance().getString("plugin.action.enpiabefore"));
    private Label l_enpiChange = new Label(I18n.getInstance().getString("plugin.action.enpiabechange"));
    private Label l_correctionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.correction"));
    private Label l_nextActionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.followupaction"));
    private Label l_alternativAction = new Label(I18n.getInstance().getString("plugin.action.alternativaction"));
    private Label l_energyBefore = new Label("Verbrauch (Referenz)");
    private Label l_energyAfter = new Label("Verbrauch (Ist)");
    private Label l_energyChange = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));
    private Button beforeDateButton = new Button("", ControlCenter.getSVGImage(Icon.CALENDAR, 14, 14));
    private Button afterDateButton = new Button("", ControlCenter.getSVGImage(Icon.CALENDAR, 14, 14));
    private Button diffDateButton = new Button("", ControlCenter.getSVGImage(Icon.CALENDAR, 14, 14));
    private MFXButton buttonOpenAnalysisBefore = new MFXButton("", ControlCenter.getSVGImage(Icon.GRAPH, 14, 14));
    private Button buttonOpenAnalysisafter = new Button("", ControlCenter.getSVGImage(Icon.GRAPH, 14, 14));
    private Button buttonOpenAnalysisaDiff = new Button("", ControlCenter.getSVGImage(Icon.GRAPH, 14, 14));
    private MFXComboBox<JEVisObject> f_EnpiSelection;
    private Label l_EnpiSelection = new Label("EnPI");
    private Label l_mediaTags = new Label();
    private MFXComboBox<String> f_mediaTags;
    private TextArea f_correctionIfNeeded = new TextArea("");
    private TextFieldWithUnit f_enpiAfter = new TextFieldWithUnit();
    private TextFieldWithUnit f_enpiBefore = new TextFieldWithUnit();
    private TextFieldWithUnit f_enpiDiff = new TextFieldWithUnit();
    private TextArea f_nextActionIfNeeded = new TextArea("");
    private TextArea f_alternativAction = new TextArea("");
    private TextFieldWithUnit f_consumptionBefore = new TextFieldWithUnit();
    private TextFieldWithUnit f_consumptionAfter = new TextFieldWithUnit();
    private TextFieldWithUnit f_consumptionDiff = new TextFieldWithUnit();

    private MFXTextField f_toUser = new MFXTextField();


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
            JEVisObject obj = FreeObject.getInstance();
            if (!data.enpiProperty().get().jevisLinkProperty().get().isEmpty()
                    && !data.enpiProperty().get().jevisLinkProperty().get().equals(FreeObject.getInstance().getID())) {
                try {
                    obj = data.getObject().getDataSource().getObject(Long.valueOf(data.enpiProperty().get().jevisLinkProperty().get()));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            //System.out.println("Select Object; " + obj);
            // f_Enpi.valueProperty().set(obj);
            f_EnpiSelection.selectItem(obj);
            f_EnpiSelection.getSelectionModel().selectLast();


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        f_EnpiSelection.valueProperty().addListener(new ChangeListener<JEVisObject>() {
            @Override
            public void changed(ObservableValue<? extends JEVisObject> observable, JEVisObject oldValue, JEVisObject newValue) {
                data.enpiProperty().get().jevisLinkProperty().set(newValue.getID().toString());
            }
        });
        try {
            //System.out.println("Select Object: " + data.getEnpi().dataObject.get());
            if (data.getEnpi().dataObject.get() <= 0l) {
                f_EnpiSelection.setValue(FreeObject.getInstance());
            } else {
                f_EnpiSelection.setValue(data.getObject().getDataSource().getObject(data.getEnpi().dataObject.get()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        l_mediaTags.setText("Medium");
        f_mediaTags = new MFXComboBox<>(data.getActionPlan().getMediumTags());
        f_mediaTags.selectItem(data.mediaTagsProperty().getValue());
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


        HBox box_EnpiAfter = new HBox(buttonOpenAnalysisBefore, afterDateButton, spacerEnpiBefore, f_enpiAfter);
        HBox box_EnpiBefore = new HBox(buttonOpenAnalysisafter, beforeDateButton, spacerEnpiBefore, f_enpiBefore);
        HBox box_EnpiDiff = new HBox(buttonOpenAnalysisaDiff, diffDateButton, spacerEnpiBefore, f_enpiDiff);
        box_EnpiAfter.setSpacing(8);
        box_EnpiBefore.setSpacing(8);
        box_EnpiDiff.setSpacing(8);
        HBox.setHgrow(f_enpiAfter, Priority.SOMETIMES);
        HBox.setHgrow(f_enpiBefore, Priority.SOMETIMES);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        beforeDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    new DateTime(2022, 8, 1, 0, 0),
                    new DateTime(2022, 8, 1, 0, 0));

            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                /* ToDo was macht das */
                //data.enpi.get().actualProperty().set(fmt.print(timeRangeDialog.getFromDate()) + "&" + fmt.print(timeRangeDialog.getUntilDate()));
            }
        });
        afterDateButton.setOnAction(event -> {
            TimeRangeDialog timeRangeDialog = new TimeRangeDialog(
                    new DateTime(2022, 8, 1, 0, 0),
                    new DateTime(2022, 8, 1, 0, 0));
            Optional<ButtonType> result = timeRangeDialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                /* ToDo was macht das */
                //data.enpiAfterProperty().set(fmt.print(timeRangeDialog.getFromDate()) + "&" + fmt.print(timeRangeDialog.getUntilDate()));
            }

        });


        buttonOpenAnalysisBefore.setOnAction(event -> {
            try {
                Long enpiData = Long.parseLong(data.enpiProperty().get().jevisLinkProperty().get().replace(";", ""));
                JEVisAttribute attribute = data.getObject().getDataSource().getObject(enpiData).getAttribute("Value");

                /**
                 AnalysisRequest analysisRequest = new AnalysisRequest(attribute.getObject(),
                 AggregationPeriod.NONE,
                 ManipulationMode.NONE,
                 startDateFromSampleRate, timestampFromLastSample);
                 analysisRequest.setAttribute(attribute);
                 JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest)
                 */
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        NumberStringConverter nsc = NumerFormating.getInstance().getCurrencyConverter();
        NumberStringConverter nscNoUnit = NumerFormating.getInstance().getDoubleConverter();

        Bindings.bindBidirectional(f_consumptionBefore.getTextField().textProperty(), data.consumptionProperty().get().actualProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_consumptionBefore.getUnitField().textProperty(), data.consumptionProperty().get().unitProperty());
        Bindings.bindBidirectional(f_consumptionAfter.getTextField().textProperty(), data.consumptionProperty().get().afterProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_consumptionAfter.getUnitField().textProperty(), data.consumptionProperty().get().unitProperty());
        Bindings.bindBidirectional(f_consumptionDiff.getTextField().textProperty(), data.consumptionProperty().get().diffProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_consumptionDiff.getUnitField().textProperty(), data.consumptionProperty().get().unitProperty());

        Bindings.bindBidirectional(f_enpiBefore.getTextField().textProperty(), data.enpiProperty().get().actualProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiBefore.getUnitField().textProperty(), data.enpiProperty().get().unitProperty());
        Bindings.bindBidirectional(f_enpiAfter.getTextField().textProperty(), data.enpiProperty().get().afterProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiAfter.getUnitField().textProperty(), data.enpiProperty().get().unitProperty());
        Bindings.bindBidirectional(f_enpiDiff.getTextField().textProperty(), data.enpiProperty().get().diffProperty(), nscNoUnit);
        Bindings.bindBidirectional(f_enpiDiff.getUnitField().textProperty(), data.enpiProperty().get().unitProperty());

        Bindings.bindBidirectional(f_nextActionIfNeeded.textProperty(), data.noteAlternativeMeasuresProperty());
        Bindings.bindBidirectional(f_correctionIfNeeded.textProperty(), data.noteCorrectionProperty());


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


        gridPane.addRow(0, l_EnpiSelection, f_EnpiSelection, new Region(), l_mediaTags, f_mediaTags);
        gridPane.addRow(1, l_enpiBefore, box_EnpiBefore, new Region(), l_energyBefore, f_consumptionBefore);
        gridPane.addRow(2, l_enpiAfter, box_EnpiAfter, new Region(), l_energyAfter, f_consumptionAfter);
        gridPane.addRow(3, l_enpiChange, box_EnpiDiff, new Region(), l_energyChange, f_consumptionDiff);
        gridPane.addRow(4, new Region());

        // gridPane.addRow(5, l_FromUser, f_FromUser, new Region(), l_CreateDate, f_CreateDate);
        // gridPane.addRow(6, l_distributor, f_distributor);

        gridPane.add(l_correctionIfNeeded, 0, 7);
        gridPane.add(f_correctionIfNeeded, 0, 8);

        gridPane.add(l_nextActionIfNeeded, 3, 7);
        gridPane.add(f_nextActionIfNeeded, 3, 8);
        //gridPane.addRow(7, l_correctionIfNeeded, new Region(), new Region(), l_nextActionIfNeeded, new Region());
        //gridPane.addRow(8, f_correctionIfNeeded, new Region(), new Region(), f_nextActionIfNeeded, new Region());
        // gridPane.addRow(8, l_alternativAction);
        // gridPane.addRow(9, f_alternativAction, new Region(), new Region(), new Region(), new Region());

        GridPane.setColumnSpan(l_correctionIfNeeded, 2);
        GridPane.setColumnSpan(l_nextActionIfNeeded, 2);
        GridPane.setColumnSpan(f_correctionIfNeeded, 2);
        GridPane.setColumnSpan(f_nextActionIfNeeded, 2);
        GridPane.setColumnSpan(l_alternativAction, 2);
        GridPane.setColumnSpan(f_alternativAction, 2);

        setContent(scrollPane);

    }

}
