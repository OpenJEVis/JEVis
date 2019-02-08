package org.jevis.jeconfig.plugin.Dashboard.wizzard;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.tool.Layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Wizard {

    private final JEVisDataSource jeVisDataSource;
    public ObjectProperty<Widget> selectedWidget = new SimpleObjectProperty<>();
    private Button nextButton = new Button("Next");
    private Button previousButton = new Button("Previous");
    private Button finishButton = new Button("Finish");
    private Button cancelButton = new Button("Cancel");
    private BorderPane rootPane = new BorderPane();
    private AnchorPane contentPane = new AnchorPane();
    private VBox bottomContent = new VBox(10);
    private BooleanProperty isLastStep = new SimpleBooleanProperty(false);
    private PageWidgetSelection pageWidgetSelection = new PageWidgetSelection();
    private Page currentPage;

    private List<Page> pageList = new ArrayList<>();
    private IntegerProperty pageIndex = new SimpleIntegerProperty(0);


    public Wizard(JEVisDataSource jeVisDataSource) {
        this.jeVisDataSource = jeVisDataSource;
    }

    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    public Optional<Widget> show(Window parent) {

        pageWidgetSelection.setWizard(this);
        pageList.add(pageWidgetSelection);


        HBox buttonBox = new HBox(12);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().addAll(cancelButton, previousButton, nextButton, finishButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        bottomContent.getChildren().addAll(separator, buttonBox);

        rootPane.setTop(DialogHeader.getDialogHeader("if_dashboard_46791.png", "Dashboard Wizard"));
        rootPane.setCenter(contentPane);
        rootPane.setBottom(bottomContent);

        isLastStep = new SimpleBooleanProperty(false);

        isLastStep.addListener((observable, oldValue, newValue) -> {
            finishButton.setDisable(!newValue);
            finishButton.setDefaultButton(newValue);

            nextButton.setDefaultButton(!newValue);

        });
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        finishButton.setDisable(true);

        pageWidgetSelection.selectedWidgetProperty.addListener((observable, oldValue, newValue) -> {
            selectedWidget.setValue(newValue);//or bind?
        });

        selectedWidget.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nextButton.setDisable(false);
            } else {
                nextButton.setDisable(true);
            }
        });

        cancelButton.setCancelButton(true);

        currentPage = pageWidgetSelection;

        contentPane.getChildren().add(pageWidgetSelection.getNode());
        nextButton.setDefaultButton(true);
        nextButton.setOnAction(event -> {
            nextPage();
        });


        pageIndex.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 2) {
                finishButton.setDisable(false);
                nextButton.setDisable(true);
                finishButton.setDefaultButton(true);
                nextButton.setDefaultButton(false);
            } else {
                finishButton.setDisable(true);
                nextButton.setDisable(false);//not correct
                finishButton.setDefaultButton(false);
                nextButton.setDefaultButton(true);
            }

            if (newValue.intValue() == 0) {
                //Widget Selection, TODO for previous()
            } else if (newValue.intValue() == 1) {
                SampleHandler sampleHandler = selectedWidget.getValue().getSampleHandler();

                Node configNode = sampleHandler.getPage().getNode();
                contentPane.getChildren().setAll(configNode);
                Layouts.setAnchor(configNode, 5.0);
            } else if (newValue.intValue() == 2) {
                Node configNode = selectedWidget.getValue().config.getConfigSheet();
                contentPane.getChildren().setAll(configNode);
                Layouts.setAnchor(configNode, 5.0);
            }
        });


//        contentPane.getChildren().add(contentPane);
        AnchorPane.setTopAnchor(contentPane, 5.0);
        AnchorPane.setBottomAnchor(contentPane, 5.0);
        AnchorPane.setLeftAnchor(contentPane, 5.0);
        AnchorPane.setRightAnchor(contentPane, 5.0);

        Scene scene = new Scene(rootPane, 600, 700);
        Stage newWindow = new Stage();

        newWindow.setTitle("Dashboard Wizard");
        newWindow.setScene(scene);
        newWindow.toFront();

        BooleanProperty isFinised = new SimpleBooleanProperty(false);
        finishButton.setOnAction(event -> {
            isFinised.setValue(true);
            newWindow.hide();
        });

        cancelButton.setOnAction(event -> {
            newWindow.hide();
        });

        newWindow.showAndWait();

        if (isFinised.getValue()) {
            Optional<Widget> sc = Optional.of(selectedWidget.getValue());
            return sc;
        } else {
            Optional<Widget> sc = Optional.empty();
            return sc;
        }

    }

    public void nextPage() {
        pageIndex.setValue(pageIndex.getValue() + 1);
        System.out.println("Next Page: " + currentPage);
    }

//    public void setWidgetSelected(Widget widget) {
//        this.selectedWidget = widget;
//    }
}
