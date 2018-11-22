/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.AppPush;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich
 */
public class GetDeviceID {
    private static final Logger logger = LogManager.getLogger(GetDeviceID.class);

    public static final String ADD_URL = "https://office.cap3.de:53303/cmns/v1/devices";
    public static final String Bundle_URL = "https://office.cap3.de:53303/cmns/v1/bundles";
    public static final String API_KEY = " eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiUGVybWFuZW50IEFwcCBBY2Nlc3MgVG9rZW4iLCJzdWJqZWN0IjoiQXBwIiwiaXNzdWVyIjoiQy1NTlMiLCJ2ZXJzaW9uIjoiMS4wIiwiaXNzdWVkQXQiOiIyMDE1LTA0LTE2VDExOjA1OjQ4LjgyNyswMjowMCIsImV4cGlyYXRpb24iOiIyMTE1LTA0LTE2VDExOjA1OjQ4LjgyNyswMjowMCJ9.8780512312fc6f37a6bdf98b783120681b6c62cf0838952cb87c9054827344c9 ";

    /**
     * not finished
     *
     * @param urlDevice
     * @param deviceAPIKey
     * @param devices
     * @return
     * @throws Exception
     */
    public static List<String> DeviceToID(String urlDevice, String deviceAPIKey) throws Exception {//, List<String> devices
        List<String> ids = new ArrayList<String>();
        Certificate.doTrustToCertificates();
        HttpURLConnection connection = null;
        URL url = new URL(urlDevice);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Api-Key", deviceAPIKey);
        connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String lines;
        StringBuffer sb = new StringBuffer();
        while ((lines = reader.readLine()) != null) {
            lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
            sb.append(lines);
        }
        logger.info(sb);
        reader.close();
        connection.disconnect();
        return ids;
    }
}
