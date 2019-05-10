package org.jevis.jeconfig.plugin.Dashboard.config;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import jfxtras.scene.layout.GridPane;
import org.apache.commons.validator.routines.DoubleValidator;
import org.jevis.jeconfig.application.control.ValidatedTextField;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WidgetConfigEditor {


    private WidgetConfig originalWidgetConfig;
    private WidgetConfig newWidgetConfig;
    private List<Tab> tabs = new ArrayList<>();


    public WidgetConfigEditor(WidgetConfig widgetConfig) {
        this.originalWidgetConfig = widgetConfig;
    }


    public Tab buildGeneralTab() {
        Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general"));


        /** Position **/
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8d);

        ValidatedTextField textFieldXPos = new ValidatedTextField(originalWidgetConfig.xPosition.getValue().toString(), DoubleValidator.getInstance());
        ValidatedTextField textFieldYPos = new ValidatedTextField(originalWidgetConfig.yPosition.getValue().toString(), DoubleValidator.getInstance());
        ValidatedTextField textFieldWidth = new ValidatedTextField("" + originalWidgetConfig.size.get().getWidth(), DoubleValidator.getInstance());
        ValidatedTextField textFieldHeight = new ValidatedTextField("" + originalWidgetConfig.size.get().getHeight(), DoubleValidator.getInstance());
        ValidatedTextField textFieldTitle = new ValidatedTextField("" + originalWidgetConfig.title.get(), null);


        IntegerProperty row = new SimpleIntegerProperty(0);

        addRow(gridPane, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.title"), textFieldTitle, row);
        addRow(gridPane, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.xpos"), textFieldXPos, row);
        addRow(gridPane, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.ypos"), textFieldYPos, row);
        addRow(gridPane, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.width"), textFieldWidth, row);
        addRow(gridPane, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.height"), textFieldHeight, row);

        /** Font **/
        GridPane gridPaneFont = new GridPane();
        gridPaneFont.setPadding(new Insets(10));
        gridPaneFont.setVgap(8d);

        ValidatedTextField textFieldFontSize = new ValidatedTextField(originalWidgetConfig.fontSize.getValue().toString(), DoubleValidator.getInstance());
        ColorPicker colorPickerFont = new ColorPicker(originalWidgetConfig.fontColor.getValue());
        ColorPicker colorPickerBG = new ColorPicker(originalWidgetConfig.backgroundColor.getValue());
        colorPickerBG.setStyle("-fx-color-label-visible: false ;");
        colorPickerFont.setStyle("-fx-color-label-visible: false ;");
        colorPickerBG.setMinWidth(50d);
        colorPickerFont.setMinWidth(50d);
        CheckBox checkBoxShadow = new CheckBox();
        checkBoxShadow.setSelected(originalWidgetConfig.showShadow.getValue());
        IntegerProperty rowFont = new SimpleIntegerProperty(0);

        addRow(gridPaneFont, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.fontsize"), textFieldFontSize, rowFont);
        addRow(gridPaneFont, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.fontcolor"), colorPickerFont, rowFont, false);
        addRow(gridPaneFont, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.bgcolor"), colorPickerBG, rowFont, false);
        addRow(gridPaneFont, I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.showshadow"), checkBoxShadow, rowFont, false);

//        gridPaneFont.setGridLinesVisible(true);

        TitledPane basicTab = new TitledPane(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.window"), gridPane);
        TitledPane frontTab = new TitledPane(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.presentation"), gridPaneFont);

        basicTab.setCollapsible(false);
        frontTab.setCollapsible(false);

        GridPane outerGridPane = new GridPane();
        outerGridPane.setPadding(new Insets(10));
        outerGridPane.setVgap(25d);
        outerGridPane.setHgap(25d);

        basicTab.setPrefWidth(300);
        frontTab.setPrefWidth(300);

        outerGridPane.add(basicTab, new GridPane.C().col(0).row(0).vgrow(Priority.SOMETIMES));
        outerGridPane.add(frontTab, new GridPane.C().col(1).row(0).vgrow(Priority.SOMETIMES));
        outerGridPane.add(new Region(), new GridPane.C().col(0).row(1).colSpan(2).vgrow(Priority.ALWAYS));

        tab.setContent(outerGridPane);

        return tab;
    }


    private void addRow(GridPane gridPane, String labelText, Node field, IntegerProperty row, boolean fieldMinSize) {
        row.setValue(row.getValue() + 1);

        Label label = new Label(labelText);
        label.focusTraversableProperty().setValue(false);
        label.setMinHeight(26d);

        gridPane.add(label, new GridPane.C()
                .col(0).row(row.get())
                .valignment(VPos.BOTTOM)
                .hgrow(Priority.NEVER)
                .halignment(HPos.LEFT));

        gridPane.add(new Region(), new GridPane.C()
                .col(1).row(row.get())
                .valignment(VPos.BOTTOM)
                .hgrow(Priority.ALWAYS)
                .halignment(HPos.RIGHT));

        gridPane.add(field, new GridPane.C()
                .col(2).row(row.get())
                .valignment(VPos.BOTTOM)
                .halignment(HPos.RIGHT));


    }

    private void addRow(GridPane gridPane, String labelText, Node field, IntegerProperty row) {
        row.setValue(row.getValue() + 1);

        JFXTextField label = new JFXTextField(labelText);
        label.setEditable(false);
        label.focusTraversableProperty().setValue(false);
        label.setFocusColor(label.getUnFocusColor());
//        label.setPrefWidth(80);
        label.setMinHeight(26d);
        label.heightProperty().addListener((observable, oldValue, newValue) -> {
            label.setTooltip(new Tooltip(newValue + ""));
        });

        gridPane.add(label, new GridPane.C()
                .col(0).row(row.get())
                .valignment(VPos.BOTTOM)
                .hgrow(Priority.NEVER)
                .halignment(HPos.LEFT));

        gridPane.add(field, new GridPane.C()
                .col(1).row(row.get())
                .colSpan(2)
                .valignment(VPos.BOTTOM)
                .hgrow(Priority.ALWAYS)
                .halignment(HPos.RIGHT));


//        gridPane.add(new Region(), new GridPane.C().col(3).row(row.get()).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS));
    }

    public void addTab(Tab tab) {
        tabs.add(tab);
    }

    public Optional<ButtonType> show() {
        TabPane tabPane = new TabPane();


        tabPane.getTabs().add(buildGeneralTab());
        tabPane.getTabs().addAll(tabs);

        tabPane.getTabs().forEach(tab -> {
            tab.setClosable(false);
        });


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.dashboard.widget.config.title"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.dashboard.widget.config.message"));
//        alert.setContentText("Are you ok with this?");
        alert.setResizable(true);

        alert.getDialogPane().setContent(tabPane);

//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.get() == ButtonType.OK) {
//            // ... user chose OK
//        } else {
//            // ... user chose CANCEL or closed the dialog
//        }

        alert.setWidth(1230);
        alert.showAndWait();

        return Optional.ofNullable(ButtonType.OK);
    }


    public WidgetConfig getConfig() {
        return newWidgetConfig;
    }
}
