package org.jevis.jeconfig.plugin.graph.view;

import javafx.stage.FileChooser;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.application.jevistree.plugin.ChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class GraphExport {
    private final GraphDataModel model;
    private final JEVisDataSource ds;
    private boolean needSave = false;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    private Boolean multiAnalyses = false;
    private List<String> charts = new ArrayList<>();
    final DateTimeFormatter standard = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public GraphExport(JEVisDataSource ds, GraphDataModel model, String analysisName) {
        this.model = model;
        this.ds = ds;
        this.setDates();

        for (ChartDataModel mdl : model.getSelectedData()) {
            if (mdl.getSelected()) {
                for (String s : mdl.get_selectedCharts()) {
                    if (charts.isEmpty() || !charts.contains(s)) charts.add(s);
                }
            }
        }
        if (charts.size() > 1) {
            AlphanumComparator ac = new AlphanumComparator();
            Collections.sort(charts, ac);
            multiAnalyses = true;
        }

        String formattedName = analysisName.replaceAll(" ", "_");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSV File Destination");
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
        fileChooser.setInitialFileName(formattedName + I18n.getInstance().getString("plugin.graph.dialog.export.from")
                + fmtDate.print(minDate) + I18n.getInstance().getString("plugin.graph.dialog.export.to")
                + fmtDate.print(maxDate) + "_" + fmtDate.print(new DateTime()) + ".csv");
        File file = fileChooser.showSaveDialog(JEConfig.getStage());
        if (file != null) {
            destinationFile = file;
            needSave = true;
        }
    }

    private void setDates() {
        for (ChartDataModel mdl : model.getSelectedData()) {
            DateTime startNow = mdl.getSelectedStart();
            DateTime endNow = mdl.getSelectedEnd();
            if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
            if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
        }
    }

    public void export() throws FileNotFoundException, UnsupportedEncodingException, JEVisException {
        String exportStrg = "";
        if (!multiAnalyses) exportStrg = createCSVString(Integer.MAX_VALUE);
        else exportStrg = createCSVStringMulti(Integer.MAX_VALUE);

        if (destinationFile != null && exportStrg.length() > 90) {
            writeFile(destinationFile, exportStrg);
        }
    }

    private void writeFile(File file, String text) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer;
        writer = new PrintWriter(file, "UTF-8");
        writer.println(text);
        writer.close();
    }

    private String createCSVString(int lineCount) throws JEVisException {
        final StringBuilder sb = new StringBuilder();

        String header = "Date";
        for (ChartDataModel mdl : model.getSelectedData()) {
            String objectName = mdl.getObject().getName();
            String dpName = "";
            if (mdl.getDataProcessor() != null) dpName = mdl.getDataProcessor().getName();
            header += ";" + objectName;
            if (mdl.getDataProcessor() != null) header += " (" + dpName + ")";
        }
        sb.append(header);
        sb.append(System.getProperty("line.separator"));

        List<String> dateColumn = new ArrayList<>();
        Boolean firstSet = true;
        for (ChartDataModel mdl : model.getSelectedData()) {
            if (firstSet) {
                for (JEVisSample sample : mdl.getSamples()) {
                    dateColumn.add(standard.print(sample.getTimestamp()));
                }
                firstSet = false;
            }
        }

        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (ChartDataModel mdl : model.getSelectedData()) {
            map.put(mdl.getObject().getName(), mdl.getSamples());
        }

        for (int i = 0; i < dateColumn.size(); i++) {
            String s = "";
            s += dateColumn.get(i) + ";";

            for (ChartDataModel mdl : model.getSelectedData()) {
                s += map.get(mdl.getObject().getName()).get(i).getValueAsDouble() + ";";
            }
            sb.append(s);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    private String createCSVStringMulti(int lineCount) throws JEVisException {
        final StringBuilder sb = new StringBuilder();

        for (String s : charts) {
            String header = "Date";
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.get_selectedCharts().contains(s)) {
                    String objectName = mdl.getObject().getName();
                    String dpName = "";
                    if (mdl.getDataProcessor() != null) dpName = mdl.getDataProcessor().getName();
                    header += ";" + objectName;
                    if (mdl.getDataProcessor() != null) header += " (" + dpName + ")";
                }
            }
            header += ";";
            sb.append(header);
        }

        sb.append(System.getProperty("line.separator"));


        Long size = 0L;

        List<List<String>> listDateColumns = new ArrayList<>();
        for (String s : charts) {
            List<String> dateColumn = new ArrayList<>();
            Boolean firstSet = true;
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (firstSet && mdl.get_selectedCharts().contains(s)) {
                    for (JEVisSample sample : mdl.getSamples()) {
                        dateColumn.add(standard.print(sample.getTimestamp()));
                    }
                    firstSet = false;
                    size = Math.max(size, dateColumn.size());
                }
            }
            listDateColumns.add(dateColumn);
        }

        List<Map<String, List<JEVisSample>>> listMaps = new ArrayList<>();
        for (String s : charts) {
            Map<String, List<JEVisSample>> map = new HashMap<>();
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.get_selectedCharts().contains(s)) {
                    map.put(mdl.getObject().getName(), mdl.getSamples());
                }
            }
            listMaps.add(map);
        }

        for (int i = 0; i < size; i++) {
            String str = "";
            for (String s : charts) {
                int chartsIndex = charts.indexOf(s);
                boolean hasValues = i < listDateColumns.get(chartsIndex).size();
                if (hasValues) {
                    str += listDateColumns.get(chartsIndex).get(i) + ";";
                } else {
                    str += ";";
                }

                for (ChartDataModel mdl : model.getSelectedData()) {
                    String objName = mdl.getObject().getName();
                    if (mdl.get_selectedCharts().contains(s)) {
                        if (hasValues) {
                            str += listMaps.get(chartsIndex).get(objName).get(i).getValueAsDouble() + ";";
                        } else {
                            str += ";";
                        }
                    }
                }
            }
            sb.append(str);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}
