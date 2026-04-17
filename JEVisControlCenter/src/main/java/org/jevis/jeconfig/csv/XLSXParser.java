package org.jevis.jeconfig.csv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an XLSX (or XLS) file using Apache POI and exposes rows via the {@link ImportParser} interface.
 */
public class XLSXParser implements ImportParser {

    private static final Logger logger = LogManager.getLogger(XLSXParser.class);

    private final File file;
    private int sheetIndex;
    private int headerLines;

    private List<ImportLine> lines = new ArrayList<>();
    private List<String> sheetNames = new ArrayList<>();
    private int maxColumnCount = 0;

    public XLSXParser(File file, int sheetIndex, int headerLines) {
        this.file = file;
        this.sheetIndex = sheetIndex;
        this.headerLines = headerLines;
        parse();
    }

    private void parse() {
        lines.clear();
        sheetNames.clear();
        maxColumnCount = 0;

        try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }

            int safeSheetIndex = Math.min(sheetIndex, workbook.getNumberOfSheets() - 1);
            XSSFSheet sheet = workbook.getSheetAt(safeSheetIndex);

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // Determine column count from the first 10 rows (same as DataCollector XLSX driver)
            int colCount = 0;
            int scanLimit = Math.min(10, sheet.getLastRowNum() + 1);
            for (int i = 0; i < scanLimit; i++) {
                XSSFRow row = sheet.getRow(i);
                if (row != null && row.getLastCellNum() > colCount) {
                    colCount = row.getLastCellNum();
                }
            }
            maxColumnCount = colCount;

            for (int i = headerLines; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                lines.add(new XLSXLine(row, i, formatter, evaluator, colCount));
            }

        } catch (Exception e) {
            logger.error("Error parsing XLSX file: " + file, e);
        }
    }

    @Override
    public List<ImportLine> getLines() {
        return lines;
    }

    @Override
    public int getColumnCount() {
        return maxColumnCount;
    }

    @Override
    public int getHeaderLines() {
        return headerLines;
    }

    @Override
    public void setHeaderLines(int count) {
        this.headerLines = count;
    }

    @Override
    public void refresh() {
        parse();
    }

    public List<String> getSheetNames() {
        return sheetNames;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }
}
