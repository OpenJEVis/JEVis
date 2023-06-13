package org.jevis.jeconfig.plugin.object.extension.paramter;


import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jevis.commons.driver.Parameter;
import org.jevis.commons.driver.VarFiller;
import org.jevis.commons.i18n.I18n;

public class ParameterTable extends TableView<Parameter> {

    public ParameterTable() {
        super();
        buildTable();
    }

    public void buildTable() {
        TableColumn<Parameter, String> format = new TableColumn(I18n.getInstance().getString("plugin.objects.extension.paramter.source"));
        format.setCellFactory(TextFieldTableCell.forTableColumn());
        format.setCellValueFactory(param -> param.getValue().formatProperty());
        format.setEditable(true);

        TableColumn<Parameter, VarFiller.Variable> sourceCol = new TableColumn(I18n.getInstance().getString("plugin.objects.extension.paramter.format"));
        sourceCol.setCellFactory(ComboBoxTableCell.forTableColumn(VarFiller.Variable.LAST_TS, VarFiller.Variable.CURRENT_TS));
        sourceCol.setCellValueFactory(param -> param.getValue().variableProperty());
        sourceCol.setEditable(true);

        this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);


        this.getColumns().addAll(sourceCol,format);

        this.setEditable(true);


    }
}
