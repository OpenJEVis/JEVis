/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Table {

    private final List<TableColumn> columns = new ArrayList<>();

    public Table(TableColumn[] columns) {
        this.columns.addAll(Arrays.asList(columns));
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return "Table{" + "columns=" + columns + '}';
    }

}