package org.jevis.jeconfig.csv;

import java.util.List;

/**
 * Common interface for file parsers that produce rows of import data.
 */
public interface ImportParser {
    /** Returns all data rows (after headers have been skipped). */
    List<ImportLine> getLines();
    /** Maximum number of columns across all rows. */
    int getColumnCount();
    /** Number of header rows that are skipped. */
    int getHeaderLines();
    /** Set the number of header rows to skip (does NOT trigger a re-parse). */
    void setHeaderLines(int count);
    /** Re-parse the source file with current settings. */
    void refresh();
}
