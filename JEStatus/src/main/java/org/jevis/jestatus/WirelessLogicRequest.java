package org.jevis.jestatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WirelessLogicRequest {
    private final String user;
    private final String password;
    private static final String API ="curl --user ${user}:${password} GET \"https://simpro4.wirelesslogic.com/api/v3/${api}\" -H  \"accept: application/json";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CANCELLED = "cancelled";
    public ObjectMapper objectMapper = new ObjectMapper();
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(WirelessLogicRequest.class);

    /**
     *
     * @param user username of wireless logic
     * @param password password of wireless logic
     */
    public WirelessLogicRequest(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     *
     * @param tariff Wireless logic tariff
     * @param status Status of Sim Card eg. active
     * @return list of Sim Cards
     * @throws IOException
     */
    public List<JsonNode> getSims(String tariff, String status) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sims?");
        if (status != null) {
            stringBuilder.append("status=" + status+"&");
        }
        stringBuilder.append("tariff_name=" + tariff);



        JsonNode jsonNode = objectMapper.readTree(getRequest(stringBuilder.toString()));
        ArrayNode arr = (ArrayNode) jsonNode.get("sims");
        List<JsonNode> sims = arrayNodeToList(arr);
        logger.debug(sims);
        return arrayNodeToList(arr);


    }

    /**
     *
     * @param tariff Wireless logic tariff
     * @return Tariff details
     * @throws IOException
     */
    public JsonNode getTariffDetails(String tariff) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tariffs/details?tariff-name=").append(tariff);


        JsonNode jsonNode = objectMapper.readTree(getRequest(stringBuilder.toString()));
        return jsonNode;
    }

    /**
     *
     * @param tariffDetails Tariff Details of Wireless Logic
     * @param activeSimCards List of Active Sim Cards in the Tariff
     * @return
     */
    public double getDataIncluded(JsonNode tariffDetails, List<JsonNode> activeSimCards) {
        if (tariffDetails.size() > 0 && !tariffDetails.has("error")) {
            return tariffDetails.get(0).get("gprs_inc").asDouble() * activeSimCards.size();
        }
        return 0;
    }

    /**
     *
     * @param sims List of Sim Cards
     * @return List of the Usage of the Sim Cards
     * @throws IOException
     */
    public List<JsonNode> getSimUsage(List<JsonNode> sims) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sims/usage?iccid=").append(String.join(",", sims.stream().map(jsonNode -> jsonNode.get("iccid").asText()).collect(Collectors.toList())));
        JsonNode jsonNode = objectMapper.readTree(getRequest(stringBuilder.toString()));
        ArrayNode arr = (ArrayNode) jsonNode.get("sims");
        return arrayNodeToList(arr);

    }

    /**
     *
     * @param simCardsUsage List of Sim Card Usage
     * @return amount of data all the sims have used this month
     */
    public double getTotalDataUsed(List<JsonNode> simCardsUsage) {
        double usage = 0;
        for (JsonNode jsonNode:simCardsUsage) {
            usage += jsonNode.get("month_to_date_bytes_up").asDouble();
            usage += jsonNode.get("month_to_date_bytes_down").asDouble();
        }
        return usage / 1024d / 1024d;
    }

    /**
     *
     * @param simCardsUsage List of Sim Card Usage
     * @param day offline since how many days to be counted as offline
     * @return a List of Sim Card Usage containing information like iccid and last seen
     */
    public List<JsonNode> getOfflineSim(List<JsonNode>simCardsUsage, int day) {
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (JsonNode jsonNode:simCardsUsage) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (jsonNode.get("last_seen").asText().equals("null")) {
                jsonNodes.add(jsonNode);
            } else {
                LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("last_seen").asText(), formatter);
                if (dateTime.isBefore(LocalDateTime.now().minusDays(day))) {
                    jsonNodes.add(jsonNode);
                }
            }
        }
        return jsonNodes;
    }

    /**
     * send a get request to the simpro
     * @param api the part of the api after api/v3/
     * @return the result json String
     * @throws IOException
     */
    public String getRequest(String api) throws IOException {
        Map<String, String> data = new HashMap<String, String>();
        data.put("user", user);
        data.put("password", password);
        data.put("api", api);

        String formattedString = StrSubstitutor.replace(API, data);


        Process process = Runtime.getRuntime().exec(formattedString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = reader.readLine();
        if (result == null) {
            return "";
        } else {
            return result;
        }


    }

    /**
     * @param arrayNode arrayNode to be converted
     * @return a List of JsonNodes
     */
    private List<JsonNode> arrayNodeToList(ArrayNode arrayNode) {
        List<JsonNode> jsonNodes = new ArrayList<>();
        if (arrayNode != null) {
            for (int i = 0; i < arrayNode.size(); i++) {
                jsonNodes.add(arrayNode.get(i));
            }
        }

        return jsonNodes;
        }
}
