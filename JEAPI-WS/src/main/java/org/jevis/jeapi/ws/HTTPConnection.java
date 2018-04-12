/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Helper class to build HTTP connections to the JEWebservice.
 *
 * @TODO there is to much duplicate code here. There is much to improve.
 * @TODO the error handling is bad
 * @author fs
 */
public class HTTPConnection {

    private final String baseURL;
    private final String username;
    private final String password;
    private final Logger logger = LogManager.getLogger(HTTPConnection.class);
    public static final DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").withZoneUTC();

    /**
     *
     */
    public static String API_PATH_V1 = "JEWebService/v1/";

    public static String RESOURCE_OBJECTS = "objects";
    public static String RESOURCE_CLASSES = "classes";
    public static String RESOURCE_ATTRIBUTES = "attributes";
    public static String RESOURCE_TYPES = "types";

    public HTTPConnection(String baseurl, String username, String password) {
        this.baseURL = baseurl;
        this.username = username;
        this.password = password;
    }

    private void addAuth(HttpURLConnection conn, String username, String password) {
        String auth = new String(Base64.encodeBase64((username + ":" + password).getBytes()));

//        System.out.println("Using auth: 'Authorization Basic " + auth);
        conn.setRequestProperty("Authorization", "Basic " + auth);
    }

    public InputStream getInputStreamRequest(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {

            return conn.getInputStream();
//            return new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            return null;
        }

    }

    public BufferedImage getIconRequest(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

            BufferedImage imBuff = ImageIO.read(conn.getInputStream());

            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");
            return imBuff;
//            return new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            return null;
        }

    }

    public byte[] getByteRequest(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {

            byte[] bytes = IOUtils.toByteArray(conn.getInputStream());
//            JEVisFile jf = new JEVisFileImp("tmp.file", bytes);//filename comes from the samples

            return bytes;

        } else {
            return null;
        }

    }

    public StringBuffer postRequest(String resource, String json) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "JEAPI-WS");
        con.setRequestProperty("Accept-Charset", "UTF-8");
        con.setRequestProperty("Accept-Encoding", "gzip");
        con.setDoOutput(true);
        con.setDoInput(true);
        addAuth(con, username, password);

        logger.debug("HTTP POST request {}", con.getURL());
        con.connect();

//        Reader reader = null;
//        if ("gzip".equals(con.getContentEncoding())) {
//            OutputStream os = (new GZIPInputStream(con.getInputStream()).
//            reader = new InputStreamReader(new GZIPInputStream(con.getInputStream()));
//        } else {
//            reader = new InputStreamReader(con.getInputStream());
//        }
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
        writer.write(json);
        writer.close();
        wr.close();

        int responseCode = con.getResponseCode();
        logger.debug("Post status: {}", responseCode);

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logger.trace("respone.Payload: {}", response);

//            try (PrintWriter out = new PrintWriter("/tmp/" + resource.replaceAll("\\/", "") + ".json")) {
//                out.println(response.toString());
//            }
            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");
            return response;
        } else {
            return null;
        }

    }

    public StringBuffer getRequest(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//            logger.trace("Payload: {}", response);

            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");
            return response;
        } else {
            logger.trace("Code is not OK return null");
            return null;
        }

    }

    public static StringBuffer getPayload(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
//            logger.trace("Payload: {}", response);

        return response;
    }

    /**
     * @TODO this is not a generic post Connection like the name implies
     * @param resource
     * @return
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    public HttpURLConnection getPostFileConnection(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("User-Agent", "JEAPI-WS");

//        con.setRequestProperty("Accept-Encoding", "gzip");
        conn.setDoOutput(true);
//        conn.setDoInput(true);
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

//        int responseCode = conn.getResponseCode();
//        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

    /**
     * @TODO this is not a generic post Connection like the name implies
     * @param resource
     * @return
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    public HttpURLConnection getPostIconConnection(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "image/png");
        conn.setRequestProperty("User-Agent", "JEAPI-WS");

//        con.setRequestProperty("Accept-Encoding", "gzip");
        conn.setDoOutput(true);
//        conn.setDoInput(true);
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

//        int responseCode = conn.getResponseCode();
//        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

    public HttpURLConnection getGetConnection(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

    public HttpURLConnection getDeleteConnection(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        addAuth(conn, username, password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP DELETE request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

}
