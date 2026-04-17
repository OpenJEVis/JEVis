package org.jevis.jeconfig.csv;

/**
 * Common interface for a single parsed data row, regardless of source format (CSV, XLSX, …).
 */
public interface ImportLine {
    String getColumn(int index);
    int getColumnCount();
    boolean isEmpty();
    int getRowNumber();
}
