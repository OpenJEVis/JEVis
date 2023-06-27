package org.jevis.jeconfig.plugin.dashboard.wizard;

import org.jevis.jeconfig.plugin.dashboard.config.DataModelNode;
import org.jevis.jeconfig.plugin.dashboard.config.DataPointNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExampleConverter {


    private final Map<Integer, Double> yPos = new HashMap<>();
    private final Map<String, Double> columns = new HashMap<>();

    public ExampleConverter() {

        this.yPos.put(1, 50d);
        this.yPos.put(2, 500d);
        this.yPos.put(3, 950d);
        this.yPos.put(4, 1400d);
        this.yPos.put(5, 1850d);
        this.yPos.put(6, 2300d);
        this.yPos.put(7, 2750d);

        this.columns.put("1968", 700d);
        this.columns.put("PT24H", 550d);
        this.columns.put("P7D", 400d);
        this.columns.put("P30D", 250d);
        this.columns.put("P365D", 100d);


    }


    public void sampleHandlerToValue(DataModelNode dataModelNode) throws IOException {

        String stringBuilder = "";

//        stringBuilder.append("{\n" +
//                "  \"Dash Board Color\": \"0x00000000\",\n" +
//                "  \"X Axis Grid Interval\": \"50.0\",\n" +
//                "  \"Y Axis Grid Interval\": \"50.0\",\n" +
//                "  \"Snap to Grid\": \"false\",\n" +
//                "  \"Show Grid\": \"false\",\n" +
//                "  \"Data Period\": \"P1D\",\n" +
//                "  \"defaultPeriod\": \"1968\",\n" +
//                "  \"Zoom Factor\": \"1.0\",\n" +
//                "  \"Update Rate\": \"450\",\n" +
//                "  \"width\": \"1560\",\n" +
//                "  \"height\": \"1650\",\n" +
//                "  \"disableIntervalUI\": false,\n" +
//                "  \"Widget\": [");

//        int table = DashboardControl.nextTableID();

        int row = 0;
        for (DataPointNode dataPointNode : dataModelNode.getData()) {
            row++;
//            System.out.println("\n--Table: " + table + " -- Row: " + row + " -----OID: " + dataPointNode.getObjectID() + "-----------\n");

            for (Map.Entry<String, Double> entry : this.columns.entrySet()) {
                String s = entry.getKey();
                Double aDouble = entry.getValue();
//                double yp = this.yPos.get(table) + (50 * row);
//                System.out.println("ypos: " + this.yPos.get(table) + "+" + "(50*" + row + ")=" + yp + "/" + aDouble);

//                stringBuilder.append(dataToVlaueWidget(dataPointNode, s, aDouble, yp));

            }
        }
//        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//        stringBuilder.append("]}");
//        System.out.println("Final Json: ----------------------------------");
//        System.out.println(stringBuilder.toString());
//        System.out.println("--------------------------------------------");
        BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/DasNeue.json", true));
        writer.write(stringBuilder);
        writer.close();

//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("\"data\": [ {");
//        stringBuilder.append("\"objectID\":" + dataModelNode.getData());


    }


    public String dataToVlaueWidget(DataPointNode dataPointNode, String interval, double xpos, double ypos) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{\n");
        stringBuilder.append("\"id\": \"\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"title\": \"\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"WidgetType\": \"Value\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"Background Color\": \"0xcacacaff\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"Font Color\": \"0x000000ff\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"height\": 20");
        stringBuilder.append(",\n");
        stringBuilder.append("\"width\": 100");
        stringBuilder.append(",\n");
        stringBuilder.append("\"xPos\": " + xpos);
        stringBuilder.append(",\n");
        stringBuilder.append("\"yPos\": " + ypos);
        stringBuilder.append(",\n");
        stringBuilder.append("\"fontSize\": 12");
        stringBuilder.append(",\n");
        stringBuilder.append("\"Title Position\": \"CENTER\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"dataHandler\": {");
        stringBuilder.append("\"type\": \"SimpleDataHandler\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"forcedInterval\": \"" + interval + "\"");
        stringBuilder.append(",\n");
        stringBuilder.append("\"data\": [ {");

        try {
            long calcID = 0;

            stringBuilder.append(dataConterter(dataPointNode.getObjectID(),
                    dataPointNode.getCleanObjectID(),
                    calcID,
                    dataPointNode.isEnpi(),
                    dataPointNode.getAggregationPeriod().toString(),
                    dataPointNode.getColor().toString()));
        } catch (Exception ex) {
            System.err.println("dataPointNode: " + dataPointNode);
            ex.printStackTrace();
        }

        stringBuilder.append("}]}},");

        return stringBuilder.toString();
    }

    public String dataConterter(long objectid, long cleanID, long calcID, boolean enpi, String aggergation, String color) {


        String stringBuilder = "\"objectID\":" + objectid +
                ",\n" +
                "\"cleanObjectID\":" + cleanID +
                ",\n" +
                "\"calculationID\":" + calcID +
                ",\n" +
                "\"attribute\":" + "\"Value\"" +
                ",\n" +
                "\"aggregationPeriod\":" + "\"" + aggergation + "\"" +
                ",\n" +
                "\"manipulationMode\":" + "\"NONE\"" +
                ",\n" +
                "\"enpi\":" + enpi +
                ",\n" +
                "\"color\":" + "\"" + color + "\"" +
                ",\n" +
                "\"absolute\": false";


        return stringBuilder;
    }


}
