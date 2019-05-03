package org.jevis.jeconfig.plugin.Dashboard.config;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
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

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));

        ValidatedTextField textFieldXpos = new ValidatedTextField(originalWidgetConfig.xPosition.getValue().toString(), DoubleValidator.getInstance());
        Label labelXPos = new Label(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.xpos"));

        ValidatedTextField textFieldYPos = new ValidatedTextField(originalWidgetConfig.yPosition.getValue().toString(), DoubleValidator.getInstance());
        Label labelYPos = new Label(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.ypos"));

        ValidatedTextField textFieldWidth = new ValidatedTextField("" + originalWidgetConfig.size.get().getWidth(), DoubleValidator.getInstance());
        Label labelWidth = new Label(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.width"));

        ValidatedTextField textFieldHeight = new ValidatedTextField("" + originalWidgetConfig.size.get().getHeight(), DoubleValidator.getInstance());
        Label labelHeight = new Label(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.general.height"));

        int row = 0;
        gridPane.add(labelXPos, new GridPane.C().col(1).row(row).valignment(VPos.BASELINE));
        gridPane.add(textFieldXpos, new GridPane.C().col(2).row(row).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS));

        row++;
        gridPane.add(labelYPos, new GridPane.C().col(1).row(row).valignment(VPos.BASELINE));
        gridPane.add(textFieldYPos, new GridPane.C().col(2).row(row).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS));

        row++;
        gridPane.add(labelWidth, new GridPane.C().col(1).row(row).valignment(VPos.BASELINE));
        gridPane.add(textFieldWidth, new GridPane.C().col(2).row(row).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS));

        row++;
        gridPane.add(labelHeight, new GridPane.C().col(1).row(row).valignment(VPos.BASELINE));
        gridPane.add(textFieldHeight, new GridPane.C().col(2).row(row).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS));

        row++;
        Region spacer = new Region();
        spacer.setMinHeight(20);
        gridPane.add(spacer, new GridPane.C().col(1).row(row).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS).colSpan(2));

        row++;
        Separator separator = new Separator(Orientation.HORIZONTAL);
        gridPane.add(separator, new GridPane.C().col(1).row(row).valignment(VPos.BASELINE).hgrow(Priority.ALWAYS).colSpan(2));

        tab.setContent(gridPane);

        return tab;
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

        alert.showAndWait();

        return Optional.ofNullable(ButtonType.OK);
    }


    public WidgetConfig getConfig() {
        return newWidgetConfig;
    }
}
