package org.jevis.shelly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShellyCloudClient {


    public ShellyCloudClient(Configuration config) throws Exception {
        //URL url = new URL("https://shelly-83-eu.shelly.cloud/device/status");
        //URL url = new URL("https://shelly-63-eu.shelly.cloud/device/status");
        URL url = new URL(config.server + "/device/status");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        writer.write("id=" + config.deviceid + "&auth_key=" + config.apikey);

        //writer.write("id=3ce90e6f567c&auth_key=MWVjYTVkdWlkEA77811B3A74981B11D275563AE6768478359558C09C1B47AA0150D698797091A3E3D776DB0DBB9C");
        //writer.write("id=483fdac3952c&auth_key=MTc3YTA0dWlk2FFAA07AC75C1188CF1648B33CD801E547D209495DD99B4A146E47BF5AD6A62D43A75ED9F9B736C3");

        writer.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;

        JEVisDataSource ds = new JEVisDataSourceWS(config.jevisServer);
        if (ds.connect(config.jevisUser, config.jevisPassword)) {
            while ((line = reader.readLine()) != null) {
                System.out.println("Data:" + line);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(line);

                JsonNode date = actualObj.findValue("_updated");
                DateTime updateTime = DateTime.parse(date.asText(), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                config.mapping.forEach((jsonkey, jevisid) -> {
                    JsonNode value = actualObj.findValue(jsonkey);

                    System.out.println("Test: " + value);
                    if (value.isDouble()) {
                        System.out.println("2: " + value.doubleValue());
                        try {
                            JEVisSample sample = ds.getObject(jevisid).getAttribute("Value")
                                    .buildSample(updateTime, value.asDouble());
                            System.out.println("JEVis Sample: " + sample);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                });
                //ShellyParser shellyParser = new ShellyParser(line, config);
            }
            writer.close();
            reader.close();

        }


    }
}
