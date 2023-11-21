package org.jevis.jecc.plugin.meters.export;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jecc.plugin.meters.data.MeterData;
import org.jevis.jecc.plugin.meters.data.MeterList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetersExport {
    private final MeterList meterList;


    private XSSFWorkbook workbook;

    public MetersExport(MeterList meterList) {
        this.meterList = meterList;


    }


    private List<String> getMediums() {
        return meterList.getMeterDataList().stream().map(meterData -> meterData.getjEVisClassName()).distinct().collect(Collectors.toList());
    }

    private void createSheetOfMedium(String medium) {
        XSSFSheet xssfSheet = workbook.createSheet(medium);
        List<JEVisTypeWrapper> types = meterList.getAllAvailableTypesOfClass(medium);
        xssfSheet = loadHeader(xssfSheet, types);
        xssfSheet = loadData(xssfSheet, medium, types);
    }

    private XSSFSheet loadHeader(XSSFSheet xssfSheet, List<JEVisTypeWrapper> types) {
        XSSFRow xssfRow = xssfSheet.createRow(0);
        XSSFCell xssfCellName = xssfRow.createCell(0, CellType.STRING);
        xssfCellName.setCellValue(I18n.getInstance().getString("plugin.meters.name"));
        for (int i = 0; i < types.size(); i++) {
            XSSFCell xssfCell = xssfRow.createCell(i + 1, CellType.STRING);
            xssfCell.setCellValue(types.get(i).getName());
        }

        return xssfSheet;

    }

    private XSSFSheet loadData(XSSFSheet xssfSheet, String medium, List<JEVisTypeWrapper> types) {
        List<MeterData> meterData = meterList.getMeterDataOfClass(medium);

        for (int i = 0; i < meterData.size(); i++) {
            XSSFRow xssfRow = xssfSheet.createRow(i + 1);
            XSSFCell xssfCellName = xssfRow.createCell(0, CellType.STRING);
            xssfCellName.setCellValue(meterData.get(i).getName());
            for (int j = 0; j < types.size(); j++) {
                String value = getValue(meterData.get(i).getJeVisAttributeJEVisSampleMap().get(types.get(j)).getOptionalJEVisSample());
                XSSFCell xssfCell = xssfRow.createCell(j + 1, CellType.STRING);
                xssfCell.setCellValue(value);
            }

        }
        return xssfSheet;
    }

    private String getValue(Optional<JEVisSample> jeVisSample) {
        try {
            return jeVisSample.isPresent() ? jeVisSample.get().getValueAsString() : "";
        } catch (JEVisException e) {
            return "";
        }
    }

    public void save(File file) throws IOException {
        workbook.write(new FileOutputStream(file));
    }

    public void export() {
        workbook = new XSSFWorkbook();
        List<String> mediums = getMediums();
        for (String medium : mediums) {
            createSheetOfMedium(medium);
        }
    }


}
