/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.sample.csvexporttable;

/**
 * @author br
 */

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.joda.time.DateTime;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Graham Smith
 * @deprecated
 */
public class EditingCell extends TableCell<CSVExportTableSample, String> {
    private MFXTextField textField;

    public EditingCell() {
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (textField == null) {
            createTextField();
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textField.requestFocus();
                textField.selectAll();
            }
        });
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    private void createTextField() {
        textField = new MFXTextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                } else if (t.getCode() == KeyCode.TAB) {
                    commitEdit(textField.getText());
                    TableColumn nextColumn = getNextColumn(!t.isShiftDown());
                    if (nextColumn != null) {
                        getTableView().edit(getTableRow().getIndex(), nextColumn);
                    }
                }
            }
        });
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue && textField != null) {
                    commitEdit(textField.getText());

                    String value = textField.getText();
                    TableRow<CSVExportTableSample> row = getTableRow();
                    JEVisSample sample = row.getItem().getSample();

                    Dialog<ButtonType> dialogSaveValue = new Dialog<>();
                    dialogSaveValue.setTitle(I18n.getInstance().getString("sampleeditor.sampletable.confirmationdialog.title"));
                    dialogSaveValue.getDialogPane().setContentText(I18n.getInstance().getString("sampleeditor.sampletable.confirmationdialog.message"));
                    final ButtonType overwrite_ok = new ButtonType(I18n.getInstance().getString("sampleeditor.sampletable.confirmationdialog.yes"), ButtonBar.ButtonData.OK_DONE);
                    final ButtonType overwrite_cancel = new ButtonType(I18n.getInstance().getString("sampleeditor.sampletable.confirmationdialog.no"), ButtonBar.ButtonData.CANCEL_CLOSE);

                    dialogSaveValue.getDialogPane().getButtonTypes().addAll(overwrite_ok, overwrite_cancel);

                    Platform.runLater(() -> {
                        dialogSaveValue.showAndWait().ifPresent(response -> {
                            if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                try {
                                    DateTime ts = sample.getTimestamp();
                                    String note = sample.getNote();
                                    JEVisUnit unit = sample.getUnit();
                                    JEVisAttribute att = sample.getAttribute();
                                    JEVisSample newSample;

                                    att.deleteSamplesBetween(sample.getTimestamp(), sample.getTimestamp());
                                    JEVisUnit u = new JEVisUnitImp(Unit.ONE);

                                    if (unit != null && !unit.equals(u)) {
                                        try {
                                            Long l = Long.parseLong(value);
                                            newSample = att.buildSample(ts, l, note, unit);
                                        } catch (Exception e1) {
                                            try {
                                                Double d = Double.parseDouble(value);
                                                newSample = att.buildSample(ts, d, note, unit);
                                            } catch (Exception e2) {
                                                newSample = att.buildSample(ts, value, note);
                                            }
                                        }
                                    } else {
                                        try {
                                            Long l = Long.parseLong(value);
                                            newSample = att.buildSample(ts, l, note, unit);
                                        } catch (Exception e1) {
                                            try {
                                                Double d = Double.parseDouble(value);
                                                newSample = att.buildSample(ts, d, note, unit);
                                            } catch (Exception e2) {
                                                newSample = att.buildSample(ts, value, note);
                                            }
                                        }
                                    }

                                    if (newSample != null) {
                                        newSample.commit();
                                    }

                                } catch (JEVisException e) {
                                    e.printStackTrace();
                                }
                            } else {

                            }
                        });
                    });
                }
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }

    /**
     * @param forward true gets the column to the right, false the column to the left of the current column
     * @return
     */

    private TableColumn<CSVExportTableSample, ?> getNextColumn(boolean forward) {
        List<TableColumn<CSVExportTableSample, ?>> columns = new ArrayList<>();
        for (TableColumn<CSVExportTableSample, ?> column : getTableView().getColumns()) {
            columns.addAll(getLeaves(column));
        }
        //There is no other column that supports editing.
        if (columns.size() < 2) {
            return null;
        }
        int currentIndex = columns.indexOf(getTableColumn());
        int nextIndex = currentIndex;
        if (forward) {
            nextIndex++;
            if (nextIndex > columns.size() - 1) {
                nextIndex = 0;
            }
        } else {
            nextIndex--;
            if (nextIndex < 0) {
                nextIndex = columns.size() - 1;
            }
        }
        return columns.get(nextIndex);
    }

    private List<TableColumn<CSVExportTableSample, ?>> getLeaves(TableColumn<CSVExportTableSample, ?> root) {
        List<TableColumn<CSVExportTableSample, ?>> columns = new ArrayList<>();
        if (root.getColumns().isEmpty()) {
            //We only want the leaves that are editable.
            if (root.isEditable()) {
                columns.add(root);
            }
            return columns;
        } else {
            for (TableColumn<CSVExportTableSample, ?> column : root.getColumns()) {
                columns.addAll(getLeaves(column));
            }
            return columns;
        }
    }
}
