package org.jevis.jeconfig.plugin.dashboard.wizzard;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
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
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

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
        return this.jeVisDataSource;
    }

    public Optional<Widget> show(Window parent) {

        this.pageWidgetSelection.setWizard(this);
        this.pageList.add(this.pageWidgetSelection);


        HBox buttonBox = new HBox(12);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().addAll(this.cancelButton, this.previousButton, this.nextButton, this.finishButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        this.bottomContent.getChildren().addAll(separator, buttonBox);

        this.rootPane.setTop(DialogHeader.getDialogHeader("if_dashboard_46791.png", "dashboard Wizard"));
        this.rootPane.setCenter(this.contentPane);
        this.rootPane.setBottom(this.bottomContent);

        this.isLastStep = new SimpleBooleanProperty(false);

        this.isLastStep.addListener((observable, oldValue, newValue) -> {
            this.finishButton.setDisable(!newValue);
            this.finishButton.setDefaultButton(newValue);

            this.nextButton.setDefaultButton(!newValue);

        });
        this.nextButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.finishButton.setDisable(true);

        this.pageWidgetSelection.selectedWidgetProperty.addListener((observable, oldValue, newValue) -> {
            this.selectedWidget.setValue(newValue);//or bind?
        });

        this.selectedWidget.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.nextButton.setDisable(false);
            } else {
                this.nextButton.setDisable(true);
            }
        });

        this.cancelButton.setCancelButton(true);

        this.currentPage = this.pageWidgetSelection;

        this.contentPane.getChildren().add(this.pageWidgetSelection.getNode());
        this.nextButton.setDefaultButton(true);
        this.nextButton.setOnAction(event -> {
            nextPage();
        });


        /**
         * TODO: replace this hardcoded solution with something more flexible
         */
        this.pageIndex.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 2) {
                this.finishButton.setDisable(false);
                this.nextButton.setDisable(true);
                this.finishButton.setDefaultButton(true);
                this.nextButton.setDefaultButton(false);
            } else {
                this.finishButton.setDisable(true);
                this.nextButton.setDisable(false);//not correct
                this.finishButton.setDefaultButton(false);
                this.nextButton.setDefaultButton(true);
            }

            if (newValue.intValue() == 0) {
                //Widget Selection, TODO for previous()
            }


            //TODO revive
//            if (newValue.intValue() == 1) {
//                System.out.println("SampleHandlerPage");
//                SampleHandler sampleHandler = selectedWidget.getValue().getSampleHandler();
//
//                Node configNode = sampleHandler.getPage().getNode();
//                contentPane.getChildren().setAll(configNode);
//                Layouts.setAnchor(configNode, 5.0);
////                }
//            }
//
//            if (newValue.intValue() == 2) {
////                selectedWidget.getValue().init();
//                selectedWidget.getValue().getSampleHandler().setUserSelectionDone();
//                Node configNode = selectedWidget.getValue().config.getConfigSheet();
//                contentPane.getChildren().setAll(configNode);
//                Layouts.setAnchor(configNode, 5.0);
//            }

        });


//        contentPane.getChildren().add(contentPane);
        AnchorPane.setTopAnchor(this.contentPane, 5.0);
        AnchorPane.setBottomAnchor(this.contentPane, 5.0);
        AnchorPane.setLeftAnchor(this.contentPane, 5.0);
        AnchorPane.setRightAnchor(this.contentPane, 5.0);

        Scene scene = new Scene(this.rootPane, 600, 700);
        Stage newWindow = new Stage();

        newWindow.setTitle("dashboard Wizard");
        newWindow.setScene(scene);
        newWindow.toFront();

        BooleanProperty isFinised = new SimpleBooleanProperty(false);
        this.finishButton.setOnAction(event -> {
            isFinised.setValue(true);
            newWindow.hide();
        });

        this.cancelButton.setOnAction(event -> {
            newWindow.hide();
        });

        newWindow.showAndWait();

        if (isFinised.getValue()) {

//            selectedWidget.getValue().config.applyUserConfig();
            Optional<Widget> sc = Optional.of(this.selectedWidget.getValue());
            return sc;
        } else {
            Optional<Widget> sc = Optional.empty();
            return sc;
        }

    }

    public void nextPage() {
        this.pageIndex.setValue(this.pageIndex.getValue() + 1);
    }

//    public void setWidgetSelected(Widget widget) {
//        this.selectedWidget = widget;
//    }
}
