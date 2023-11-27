package org.jevis.jecc.application.Chart.ChartPluginElements;


import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;


public class SelectionTreeTableCell<S, T> extends TreeTableCell<S, T> {
    private static final StringConverter<?> defaultTreeItemStringConverter = new StringConverter<TreeItem<?>>() {
        public String toString(TreeItem<?> treeItem) {
            return treeItem != null && treeItem.getValue() != null ? treeItem.getValue().toString() : "";
        }

        public TreeItem<?> fromString(String s) {
            return new TreeItem(s);
        }
    };
    private final CheckBox checkBox;
    private final ObjectProperty<StringConverter<T>> converter;
    private final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallback;
    private boolean showLabel;
    private ObservableValue<Boolean> booleanProperty;

    public SelectionTreeTableCell() {
        this(null, null);
    }

    public SelectionTreeTableCell(Callback<Integer, ObservableValue<Boolean>> integerObservableValueCallback) {
        this(integerObservableValueCallback, null);
    }

    public SelectionTreeTableCell(Callback<Integer, ObservableValue<Boolean>> integerObservableValueCallback, StringConverter<T> stringConverter) {
        this.converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter") {
            protected void invalidated() {
                SelectionTreeTableCell.this.updateShowLabel();
            }
        };
        this.selectedStateCallback = new SimpleObjectProperty(this, "selectedStateCallback");
        this.getStyleClass().add("check-box-tree-table-cell");
        this.checkBox = new CheckBox();
        this.setGraphic(null);
        this.setSelectedStateCallback(integerObservableValueCallback);
        this.setConverter(stringConverter);
    }

    public static <S> Callback<TreeTableColumn<S, Boolean>, TreeTableCell<S, Boolean>> forTreeTableColumn(TreeTableColumn<S, Boolean> treeTableColumn) {
        return forTreeTableColumn(null, (StringConverter) null);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(Callback<Integer, ObservableValue<Boolean>> integerObservableValueCallback) {
        return forTreeTableColumn(integerObservableValueCallback, (StringConverter) null);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(Callback<Integer, ObservableValue<Boolean>> integerObservableValueCallback, boolean showLabel) {
        StringConverter stringConverter = !showLabel ? null : defaultTreeItemStringConverter;
        return forTreeTableColumn(integerObservableValueCallback, stringConverter);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(Callback<Integer, ObservableValue<Boolean>> integerObservableValueCallback, StringConverter<T> stringConverter) {
        return (var2) -> {
            return new SelectionTreeTableCell(integerObservableValueCallback, stringConverter);
        };
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return this.converter;
    }

    public final StringConverter<T> getConverter() {
        return this.converterProperty().get();
    }

    public final void setConverter(StringConverter<T> converter) {
        this.converterProperty().set(converter);
    }

    public final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return this.selectedStateCallback;
    }

    public final Callback<Integer, ObservableValue<Boolean>> getSelectedStateCallback() {
        return this.selectedStateCallbackProperty().get();
    }

    public final void setSelectedStateCallback(Callback<Integer, ObservableValue<Boolean>> selectedStateCallback) {
        this.selectedStateCallbackProperty().set(selectedStateCallback);
    }

    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            StringConverter converter = this.getConverter();
            if (this.showLabel) {
                this.setText(converter.toString(item));
            }

            this.setGraphic(this.checkBox);
            if (this.booleanProperty instanceof BooleanProperty) {
                this.checkBox.selectedProperty().unbindBidirectional((BooleanProperty) this.booleanProperty);
            }

            ObservableValue selectedProperty = this.getSelectedProperty();
            if (selectedProperty instanceof BooleanProperty) {
                this.booleanProperty = selectedProperty;
                this.checkBox.selectedProperty().bindBidirectional((BooleanProperty) this.booleanProperty);
            }

            this.checkBox.disableProperty().bind(Bindings.not(this.getTreeTableView().editableProperty().and(this.getTableColumn().editableProperty()).and(this.editableProperty())));
        }

    }

    private void updateShowLabel() {
        this.showLabel = this.converter != null;
        this.checkBox.setAlignment(this.showLabel ? Pos.CENTER_LEFT : Pos.CENTER);
    }

    private ObservableValue<?> getSelectedProperty() {
        return this.getSelectedStateCallback() != null ? this.getSelectedStateCallback().call(this.getIndex()) : this.getTableColumn().getCellObservableValue(this.getIndex());
    }
}
