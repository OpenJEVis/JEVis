package org.jevis.jeconfig.plugin.graph.view;

import javafx.stage.FileChooser;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphExport {
    private final GraphDataModel model;
    private final JEVisDataSource ds;
    private boolean needSave = false;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;

    public GraphExport(JEVisDataSource ds, GraphDataModel model) {
        this.model = model;
        this.ds = ds;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSV File Destination");
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
        fileChooser.setInitialFileName("XXXXXXXXXX" + "_" + fmtDate.print(new DateTime()) + ".csv");
        File file = fileChooser.showSaveDialog(JEConfig.getStage());
        if (file != null) {
            destinationFile = file;
            needSave = true;
        }
    }

    public void export() throws FileNotFoundException, UnsupportedEncodingException, JEVisException {
        String exportStrg = createCSVString(Integer.MAX_VALUE);

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

        DateTimeFormatter standard = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");

        String header = "Date";
        for (BarChartDataModel mdl : model.getSelectedData()) {
            String objectName = mdl.getObject().getName();
            String dpName = mdl.getDataProcessor().getName();
            header += ";" + objectName + " (" + dpName + ")";
        }
        sb.append(header);
        sb.append(System.getProperty("line.separator"));

        for (BarChartDataModel mdl : model.getSelectedData()) {
            DateTime startNow = mdl.getSelectedStart();
            DateTime endNow = mdl.getSelectedEnd();
            if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
            if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
        }


        List<String> dateColumn = new ArrayList<>();
        Boolean firstSet = true;
        for (BarChartDataModel mdl : model.getSelectedData()) {
            if (firstSet) {
                for (JEVisSample sample : mdl.getSamples()) {
                    dateColumn.add(standard.print(sample.getTimestamp()));
                }
                firstSet = false;
            }
        }

        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (BarChartDataModel mdl : model.getSelectedData()) {
            map.put(mdl.getObject().getName(), mdl.getSamples());
        }

        for (int i = 0; i < dateColumn.size(); i++) {
            String s = "";
            s += dateColumn.get(i) + ";";

            for (BarChartDataModel mdl : model.getSelectedData()) {
                s += map.get(mdl.getObject().getName()).get(i).getValueAsDouble() + ";";
            }
            sb.append(s);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}
