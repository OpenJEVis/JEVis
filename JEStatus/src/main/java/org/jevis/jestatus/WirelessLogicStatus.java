package org.jevis.jestatus;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.i18n.I18n;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class WirelessLogicStatus extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(WirelessLogicStatus.class);
    private static final String timestampFormat = "yyyy-MM-dd HH:MM:ss";

    public WirelessLogicStatus(JEVisDataSource ds, List<String> tariffs, String username, String password) {
        super(ds);

        try {
            createTableString(tariffs, username, password);
        } catch (Exception e) {
            logger.error("Exception while creating Wireless Logic Status", e);
        }
    }

    private void createTableString(List<String> tariffs, String username, String password) throws Exception {

        StringBuilder sb = new StringBuilder();
        if (tariffs != null && username != null && password != null) {
            if (!username.equals("") && !password.equals("")) {

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);


                sb.append("<br>");
                sb.append("<br>");

                sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.wirelesslogic")).append("</h2>");
                sb.append("<table style=\"");
                sb.append(tableCSS);
                sb.append("\" border=\"1\" >");
                sb.append("<tr style=\"");
                sb.append(headerCSS);
                sb.append("\" >");
                sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.tariff")).append("</th>");
                sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.dataicluded")).append("</th>");
                sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.dataused")).append("</th>");
                sb.append("  </tr>");

                List<JsonNode> offlineSims = new ArrayList<>();
                WirelessLogicRequest wirelessLogicRequest = new WirelessLogicRequest(username, password);
                for (String tariff : tariffs) {
                    List<JsonNode> sims = wirelessLogicRequest.getSims(tariff, WirelessLogicRequest.STATUS_ACTIVE);
                    JsonNode tariffDetails = wirelessLogicRequest.getTariffDetails(tariff);
                    if (tariffDetails.has("error")) {
                        continue;
                    }
                    double dataIncluded = wirelessLogicRequest.getDataIncluded(tariffDetails, sims);
                    double totalDataUsed = wirelessLogicRequest.getTotalDataUsed(wirelessLogicRequest.getSimUsage(sims));
                    offlineSims.addAll(wirelessLogicRequest.getOfflineSim(wirelessLogicRequest.getSimUsage(sims), 1));


                    sb.append("<tr>");
                    sb.append("<td style=\"");
                    sb.append(rowCss);
                    sb.append("\">");
                    sb.append(tariff);
                    sb.append("</td>");

                    sb.append("<td style=\"");
                    sb.append(rowCss);
                    sb.append("\">");

                    sb.append(nf.format(dataIncluded)).append(" MB");
                    sb.append("</td>");

                    sb.append("<td style=\"");
                    sb.append(rowCss);
                    sb.append("\">");

                    sb.append(nf.format(totalDataUsed)).append(" MB").append(" (").append(nf.format((totalDataUsed / dataIncluded) * 100)).append("%)");
                    sb.append("</td>");
                    sb.append("</tr>");

                }

                sb.append("</table>");
                sb.append("<br>");
                sb.append("<br>");

                sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.simoffline")).append("</h2>");
                sb.append("<table style=\"");
                sb.append(tableCSS);
                sb.append("\" border=\"1\" >");
                sb.append("<tr style=\"");
                sb.append(headerCSS);
                sb.append("\" >");
                sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.iccid")).append("</th>");
                sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.lastseen")).append("</th>");
                sb.append("  </tr>");
                for (JsonNode offlineSim : offlineSims) {

                    sb.append("<tr>");
                    sb.append("<td style=\"");
                    sb.append(rowCss);
                    sb.append("\">");
                    sb.append(offlineSim.get("iccid").asText());
                    sb.append("</td>");

                    sb.append("<td style=\"");
                    sb.append(rowCss);
                    sb.append("\">");

                    sb.append(offlineSim.get("last_seen").asText());
                    sb.append("</td>");
                    sb.append("</tr>");
                }

                sb.append("</table>");
                sb.append("<br>");
            }
        }

        setTableString(sb.toString());
    }
}