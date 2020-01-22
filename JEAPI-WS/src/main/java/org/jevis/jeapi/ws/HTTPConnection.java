/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 * Helper class to build HTTP connections to the JEWebservice.
 *
 * @author fs
 * @TODO there is to much duplicate code here. There is much to improve.
 * @TODO the error handling is bad
 */
public class HTTPConnection {

    public static final DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").withZoneUTC();
    private static final Logger logger = LogManager.getLogger(HTTPConnection.class);
    /**
     *
     */
    public static String API_PATH_V1 = "JEWebService/v1/";
    public static String RESOURCE_OBJECTS = "objects";
    public static String RESOURCE_CLASSES = "classes";
    public static String RESOURCE_ATTRIBUTES = "attributes";
    public static String RESOURCE_TYPES = "types";
    public static String REAOURCE_I18N = "i18n";
    private final String baseURL;
    private final String username;
    private final String password;
    private int readTimeout = 88140;//mils

    public HTTPConnection(String baseurl, String username, String password) {
        this.baseURL = baseurl;
        this.username = username;
        this.password = password;
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
     * Its not save to trust all ssl certificats. Better use trusted keys but for now its better than simple http
     */
    public static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    private void addAuth(HttpURLConnection conn, String username, String password) {
        String auth = new String(Base64.encodeBase64((username + ":" + password).getBytes()));

//        logger.info("Using auth: 'Authorization Basic " + auth);
        conn.setRequestProperty("Authorization", "Basic " + auth);
    }

    public InputStream getInputStreamRequest(String resource) throws IOException {
//        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setReadTimeout(this.readTimeout);
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            if ("gzip".equals(conn.getContentEncoding())) {
                return new GZIPInputStream(conn.getInputStream());
            } else {
                return conn.getInputStream();
            }
        } else {
            return null;
        }

    }

    public BufferedImage getIconRequest(String resource) throws IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

            BufferedImage imBuff = ImageIO.read(conn.getInputStream());

            conn.disconnect();
            in.close();


            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");
            return imBuff;
//            return new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            return null;
        }

    }

    public byte[] getByteRequest(String resource) throws IOException {
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();


//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
        logger.trace("responseCode {}", responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {

            //            JEVisFile jf = new JEVisFileImp("tmp.file", bytes);//filename comes from the samples
            InputStream inputStream = conn.getInputStream();
            byte[] response = IOUtils.toByteArray(inputStream);
            inputStream.close();

            return response;

        } else {
            return null;
        }

    }

    public StringBuffer postRequest(String resource, String json) throws IOException, JEVisException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

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
        addAuth(con, this.username, this.password);

        logger.debug("HTTP POST request {}", con.getURL());
        con.connect();

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, StandardCharsets.UTF_8));
        writer.write(json);
        writer.close();
        wr.close();

        int responseCode = con.getResponseCode();
        logger.debug("Post status: {}", responseCode);

//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
//        logger.trace("resonseCode {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {

            InputStreamReader streamReader = null;
            if ("gzip".equals(con.getContentEncoding())) {
                streamReader = new InputStreamReader(new GZIPInputStream(con.getInputStream()), StandardCharsets.UTF_8);
            } else {
                streamReader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
            }

            BufferedReader in = new BufferedReader(streamReader);
            // BufferedReader in = new BufferedReader(
            //         new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logger.trace("response.Payload: {}", response);

//            try (PrintWriter out = new PrintWriter("/tmp/" + resource.replaceAll("\\/", "") + ".json")) {
//                out.println(response.toString());
//            }
            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");
            return response;
        } else {
            logger.error("Error getResponseCode: {} for '{}'", responseCode, resource);
            throw new JEVisException("[" + responseCode + "] ", responseCode);

//            return null;
        }

    }

    public StringBuffer getRequest(String resource) throws IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");


        URL url = new URL(this.baseURL + resource);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            logger.debug("Code is not OK return null");
            return null;
        } else {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            /**
             * this is producing a out of memory exception in some cases
             */
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");

            return response;
        }
    }

    /**
     * @param resource
     * @return
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     * @TODO this is not a generic post Connection like the name implies
     */
    public HttpURLConnection getPostFileConnection(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("User-Agent", "JEAPI-WS");

//        con.setRequestProperty("Accept-Encoding", "gzip");
        conn.setDoOutput(true);
//        conn.setDoInput(true);
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

//        int responseCode = conn.getResponseCode();
//        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

    /**
     * @param resource
     * @return
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     * @TODO this is not a generic post Connection like the name implies
     */
    public HttpURLConnection getPostIconConnection(String resource) throws MalformedURLException, ProtocolException, IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "image/png");
        conn.setRequestProperty("User-Agent", "JEAPI-WS");

//        con.setRequestProperty("Accept-Encoding", "gzip");
        conn.setDoOutput(true);
//        conn.setDoInput(true);
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

//        int responseCode = conn.getResponseCode();
//        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

    public HttpURLConnection getGetConnection(String resource) throws IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");
        conn.setRequestProperty("Accept-Encoding", "gzip");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        logger.trace("responseCode {}", responseCode);
        return conn;

    }

    public HttpURLConnection getDeleteConnection(String resource) throws IOException {
        Date start = new Date();
        //replace spaces
        resource = resource.replaceAll("\\s+", "%20");
//        logger.trace("after replcae: {}", resource);
        URL url = new URL(this.baseURL + "/" + resource);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP DELETE request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        logger.trace("resonseCode {}", responseCode);
        return conn;

    }
}
