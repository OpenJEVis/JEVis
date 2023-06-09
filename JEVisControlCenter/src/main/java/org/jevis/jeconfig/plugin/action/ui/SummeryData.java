package org.jevis.jeconfig.plugin.action.ui;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class SummeryData {
    private final ObservableMap<TableColumn, StringProperty> summeryList;

    public SummeryData(ObservableMap<TableColumn, StringProperty> summeryList) {
        this.summeryList = summeryList;
    }


    public StringProperty getProperty(TableColumn column) {
        return summeryList.getOrDefault(column, null);
    }

    public ObservableMap<TableColumn, StringProperty> getSummeryList() {
        return summeryList;
    }

    public interface SummeryFunction {

        TableColumn getColumn();

        StringProperty getValue();

        default TableCell<SummeryData, String> getCellFactory() {
            return null;
        }

    }
}
