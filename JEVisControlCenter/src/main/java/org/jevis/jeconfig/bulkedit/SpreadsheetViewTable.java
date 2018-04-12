package org.jevis.jeconfig.bulkedit;

import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TablePosition;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * @author Zeyd Bilal Calis
 */
//Diese Klasse abgeleitet von der SpreadsheetView. Mit Hilfe des ClipBoardSpecific Methode
//kann man die Inputs zu einem Excel-Sheet exportieren oder von einem Excel-Sheet in die Tabelle importieren.
public class SpreadsheetViewTable extends SpreadsheetView {

    private ObservableList<String> columnHeaderNames = FXCollections.observableArrayList();
    private static final String EXCEL_IDENTIFIER = "Biff8";
    private ObservableList<ObservableList<SpreadsheetCell>> rows;
    private DataFormat fmtExcel;
    private GridBase grid;

    public SpreadsheetViewTable(ObservableList<ObservableList<SpreadsheetCell>> rows, GridBase grid) {
        super();
        this.rows = rows;
        this.grid = grid;
    }

    @Override
    public void copyClipboard() {
        super.copyClipboard();
        copyClipBoardSpecific();
    }

    @Override
    public void pasteClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        DataFormat excelFormat = findExcelFormat(clipboard.getContentTypes());

        if (excelFormat == null) {
            super.pasteClipboard();
        } else {
            pasteClipBoardSpecific();
        }
    }

    //Kopie den Inhalt zum ClipBoard
    public void copyClipBoardSpecific() {
        columnHeaderNames = getGrid().getColumnHeaders();

        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            DataFormat spreadsheetViewFormat = findSpreadsheetViewFormat(clipboard.getContentTypes());
            ClipboardContent content = new ClipboardContent();

            ObservableList<TablePosition> focusedCell = this.getSelectionModel().getSelectedCells();
            String allText = "";
            String contentText = "";
            String colName = "";

            int oldRow = -1;
            for (final TablePosition<?, ?> p : focusedCell) {
                int currentRow = p.getRow();
                int currentColumn = p.getColumn();

                if (oldRow == currentRow) {
                    contentText += "\t";
                } else if (oldRow != -1) {
                    contentText += "\n";
                }

                String spcText = rows.get(currentRow).get(currentColumn).getText();
                contentText += spcText;

                oldRow = currentRow;

            }
            //Copy HeaderNames mit
            int rowControl = -1;
            for (final TablePosition<?, ?> p : focusedCell) {
                int currentRow = p.getRow();

                if (rowControl == currentRow) {
                    colName += "\t";
                } else if (rowControl != -1) {
                    break;
                }

                String colText = columnHeaderNames.get(p.getColumn());
                colName += colText;

                rowControl = currentRow;
            }

            allText = colName + "\n" + contentText;
            content.putString(allText);
            if (fmtExcel == null) {
                fmtExcel = DataFormat.PLAIN_TEXT;
            }
            Object templist = Clipboard.getSystemClipboard().getContent(spreadsheetViewFormat);
            content.put(spreadsheetViewFormat, templist);
            content.put(fmtExcel, allText);
            clipboard.setContent(content);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    //Paste den Inhalt vom ClipBoard
    public void pasteClipBoardSpecific() {
        columnHeaderNames = getGrid().getColumnHeaders();

        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            String[] words = clipboard.getString().split("\n");
            ObservableList<TablePosition> focusedCell = this.getSelectionModel().getSelectedCells();

            int currentRow = 0;
            int currentColumn = 0;

            for (final TablePosition<?, ?> p : focusedCell) {
                currentRow = p.getRow();
                currentColumn = p.getColumn();
            }

            //Check ObjektID's 
            //Ob sie gleich sind oder nicht!
            if (currentColumn == 0 && columnHeaderNames.get(0).equals("Object ID")) {
                for (String word : words) {
                    String[] parseWord = word.split("\t");
                    if (!rows.get(currentRow).get(0).getText().equals(parseWord[0])) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error Dialog");
                        alert.setHeaderText("Matching Error : Incorrect object ID !");
                        alert.setContentText("Please check your object ID on row " + (currentRow + 1) + " !");

                        alert.showAndWait();
                        return;
                    }
                    int col = currentColumn;
                    for (int i = 0; i < parseWord.length; i++) {
                        SpreadsheetCell spc = rows.get(currentRow).get(col);
                        grid.setCellValue(currentRow, col, spc.getCellType().convertValue(parseWord[i].trim()));

                        col++;
                    }
                    currentRow++;
                }
            } else {
                //Wenn in der Tabelle kein ObjektID gibt,wird das ausgeführt!
                for (String word : words) {
                    String[] parseWord = word.split("\t");
                    int col = currentColumn;
                    for (int i = 0; i < parseWord.length; i++) {
                        SpreadsheetCell spc = rows.get(currentRow).get(col);
                        grid.setCellValue(currentRow, col, spc.getCellType().convertValue(parseWord[i].trim()));

                        col++;
                    }
                    currentRow++;
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    //Sucht ein ExcelFormat im Clipboard.
    public DataFormat findExcelFormat(Set<DataFormat> formats) {
        for (DataFormat format : formats) {
            if (format.getIdentifiers().contains(EXCEL_IDENTIFIER)) {
                return format;
            }
        }
        return null;
    }

    //Sucht ein SpreadsheetViewFormat im Clipboard.
    public DataFormat findSpreadsheetViewFormat(Set<DataFormat> formats) {
        for (DataFormat format : formats) {
            if (format.getIdentifiers().contains("SpreadsheetView")) {
                return format;
            }
        }
        return null;
    }
}
