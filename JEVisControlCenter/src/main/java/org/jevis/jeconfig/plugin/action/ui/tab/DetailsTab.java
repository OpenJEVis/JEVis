package org.jevis.jeconfig.plugin.action.ui.tab;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.converter.NumberStringConverter;
import org.jevis.api.JEVisAttribute;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.FreeObject;
import org.jevis.jeconfig.plugin.action.ui.DoubleConverter;
import org.jevis.jeconfig.plugin.action.ui.TextFieldWithUnit;
import org.jevis.jeconfig.plugin.action.ui.TimeRangeDialog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.Optional;

public class DetailsTab extends Tab {


    Label l_enpiAfter = new Label(I18n.getInstance().getString("plugin.action.enpiafter"));
    Label l_enpiBefore = new Label(I18n.getInstance().getString("plugin.action.enpiabefore"));
    Label l_enpiChange = new Label(I18n.getInstance().getString("plugin.action.enpiabechange"));
    Label l_correctionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.correction"));
    Label l_nextActionIfNeeded = new Label(I18n.getInstance().getString("plugin.action.followupaction"));
    Label l_alternativAction = new Label(I18n.getInstance().getString("plugin.action.alternativaction"));
    Label l_energyBefore = new Label("Verbrauch (Referenz)");
    Label l_energyAfter = new Label("Verbrauch (Ist)");
    Label l_energyChange = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));
    Button beforeDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
    Button afterDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
    Button diffDateButton = new Button("", JEConfig.getSVGImage(Icon.CALENDAR, 14, 14));
    JFXButton buttonOpenAnalysisBefore = new JFXButton("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
    Button buttonOpenAnalysisafter = new Button("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
    Button buttonOpenAnalysisaDiff = new Button("", JEConfig.getSVGImage(Icon.GRAPH, 14, 14));
    private TextArea f_correctionIfNeeded = new TextArea("Korrekturmaßnahmen");
    private TextFieldWithUnit f_enpiAfter = new TextFieldWithUnit();
    private TextFieldWithUnit f_enpiBefore = new TextFieldWithUnit();
    private TextFieldWithUnit f_enpiDiff = new TextFieldWithUnit();
    private TextArea f_nextActionIfNeeded = new TextArea("Folgemaßnahmen");
    private TextArea f_alternativAction = new TextArea("Alternativmaßnahmen");
    private TextFieldWithUnit f_consumptionBefore = new TextFieldWithUnit();
    private TextFieldWithUnit f_consumptionAfter = new TextFieldWithUnit();
    private TextFieldWithUnit f_consumptionDiff = new TextFieldWithUnit();
    private JFXTextField f_FromUser = new JFXTextField();
    private JFXTextField f_toUser = new JFXTextField();
    private JFXDatePicker f_CreateDate = new JFXDatePicker();
    private JFXTextField f_distributor = new JFXTextField();
    private ActionData names = new ActionData();
    Label l_CreateDate = new Label(names.createDateProperty().getName());
    Label l_FromUser = new Label(names.fromUserProperty().getName());
    Label l_distributor = new Label(names.distributorProperty().getName());

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

        NumberStringConverter nsc = DoubleConverter.getInstance().getCurrencyConverter();
        NumberStringConverter nscNoUnit = DoubleConverter.getInstance().getDoubleConverter();

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


        f_distributor.textProperty().bindBidirectional(data.distributorProperty());
        f_FromUser.textProperty().bindBidirectional(data.fromUserProperty());
        DateTime start = data.createDateProperty().get();
        f_CreateDate.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        f_CreateDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            data.doneDateProperty().set(new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0));
        });


        f_correctionIfNeeded.setPrefWidth(400);
        f_nextActionIfNeeded.setPrefWidth(400);
        f_alternativAction.setPrefWidth(400);
        f_enpiAfter.setAlignment(Pos.CENTER_RIGHT);
        f_enpiBefore.setAlignment(Pos.CENTER_RIGHT);
        f_enpiDiff.setAlignment(Pos.CENTER_RIGHT);

        gridPane.addRow(0, l_enpiBefore, box_EnpiBefore, new Region(), l_energyBefore, f_consumptionBefore);
        gridPane.addRow(1, l_enpiAfter, box_EnpiAfter, new Region(), l_energyAfter, f_consumptionAfter);
        gridPane.addRow(2, l_enpiChange, box_EnpiDiff, new Region(), l_energyChange, f_consumptionDiff);
        gridPane.addRow(3, new Region());

        System.out.println("l_FromUser: " + l_FromUser.textProperty().get());
        gridPane.addRow(4, l_FromUser, f_FromUser, new Region(), l_CreateDate, f_CreateDate);
        gridPane.addRow(5, l_distributor, f_distributor);

        gridPane.addRow(6, l_correctionIfNeeded, new Region(), new Region(), l_nextActionIfNeeded, new Region());
        gridPane.addRow(7, f_correctionIfNeeded, new Region(), new Region(), f_nextActionIfNeeded, new Region());
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
