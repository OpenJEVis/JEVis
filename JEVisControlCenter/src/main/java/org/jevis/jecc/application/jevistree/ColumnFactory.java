/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.application.jevistree;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ColumnFactory {

    private static final Logger logger = LogManager.getLogger(ColumnFactory.class);

    private static final String OBJECT_NAME = "Name";
    private static final String OBJECT_ID = "ID";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public static TreeTableColumn<JEVisTreeRow, JEVisTreeRow> buildName() {
        TreeTableColumn<JEVisTreeRow, JEVisTreeRow> column = new TreeTableColumn<>(I18n.getInstance().getString("jevistree.header.name"));
        column.setId(OBJECT_NAME);
        column.setPrefWidth(460);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisTreeRow> p) -> {
            try {
                if (p != null && p.getValue() != null && p.getValue().getValue() != null && p.getValue().getValue().getJEVisObject() != null) {
                    TreeItem<JEVisTreeRow> item = p.getValue();
                    JEVisTreeRow selectionObject = item.getValue();

                    return new ReadOnlyObjectWrapper<>(selectionObject);

                } else {
                    return new ReadOnlyObjectWrapper<>();
                }

            } catch (Exception ex) {
                logger.debug("Error in Column Factory: " + ex);
                return new ReadOnlyObjectWrapper<JEVisTreeRow>();
            }

        });

        column.setComparator(JEVisTreeItem.getComparator());

        column.setCellFactory(JEVisNameTreeTableCell.callback());

        return column;

    }

    /**
     *
     * @param max if 1 the max date, if false the min date
     * @return
     */
    public static TreeTableColumn<JEVisTreeRow, String> buildDataTS(boolean max) {
        String columnName;
        if (max) {
            columnName = I18n.getInstance().getString("jevistree.column.maxts");
        } else {
            columnName = I18n.getInstance().getString("jevistree.column.mints");
        }

        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn<>(columnName);
        column.setId(columnName);
        column.setPrefWidth(135);

        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, String> p) -> {
            try {
                if (p != null
                        && p.getValue() != null
                        && p.getValue().getValue() != null
                        && p.getValue().getValue().getJEVisObject() != null) {
                    TreeItem<JEVisTreeRow> item = p.getValue();
                    JEVisTreeRow selectionObject = item.getValue();

                    JEVisAttribute value = null;

                    if (selectionObject.getType() == JEVisTreeRow.TYPE.OBJECT) {
                        JEVisObject obj = selectionObject.getJEVisObject();
                        value = obj.getAttribute("Value");
                    } else if (selectionObject.getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                        JEVisAttribute att = selectionObject.getJEVisAttribute();
                        if (att.getName().equals("Value")) {
                            value = att;
                        }
                    }

                    if (value != null && value.getLatestSample() != null) {
                        if (max) {
                            return new ReadOnlyObjectWrapper<>(dateTimeFormatter.print(value.getTimestampOfLastSample()));
                        } else {
                            return new ReadOnlyObjectWrapper<>(dateTimeFormatter.print(value.getTimestampOfFirstSample()));
                        }

                    } else {


                        return new ReadOnlyObjectWrapper<>("");
                    }

                } else {
                    return new ReadOnlyObjectWrapper<>("Null");
                }

            } catch (Exception ex) {
                logger.error("Error in Column Factory: ", ex);
                return new ReadOnlyObjectWrapper<>("Error");
            }

        });

        column.setCellFactory(JEVisTsTreeTableCell.callback());


        return column;

    }

    public static TreeTableColumn<JEVisTreeRow, Long> buildID() {
        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn<>(OBJECT_ID);
        column.setId(OBJECT_ID);
        column.setPrefWidth(70);
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisTreeRow, Long> p) -> {
            try {
                if (p != null
                        && p.getValue() != null
                        && p.getValue().getValue() != null
                        && p.getValue().getValue().getJEVisObject() != null) {
                    return new ReadOnlyObjectWrapper<>(p.getValue().getValue().getJEVisObject().getID());

                } else {
                    return new ReadOnlyObjectWrapper<>(-2L);
                }

            } catch (Exception ex) {
                logger.error("Error in Column Factory: ", ex);
                return new ReadOnlyObjectWrapper<>(-1L);
            }

        });

        column.setCellFactory(JEVisIdTreeTableCell.callback());

        return column;

    }
}
