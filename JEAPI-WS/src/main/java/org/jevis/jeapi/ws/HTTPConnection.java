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
import org.jevis.commons.ws.sql.Session;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
    private static final long RETRY_DELAY_MS = 5000;
    private static final int RETRIES = 3;
    public static String API_PATH_V1 = "JEWebService/v1/";
    public static String RESOURCE_OBJECTS = "objects";
    public static String RESOURCE_CLASSES = "classes";
    public static String RESOURCE_ATTRIBUTES = "attributes";
    public static String RESOURCE_TYPES = "types";
    public static String RESOURCE_I18N = "i18n";
    public static String RESOURCE_ACCESSCONTROL = "accesscontrol";
    private final String baseURL;
    private final String username;
    private final String password;
    private final int readTimeout = 120000;//millis
    private String SSOtoken = null;
    private Session session = null;
    private Proxy proxy = null;
    private Trust trustmode = Trust.ALWAYS;


    public HTTPConnection(String baseurl, String username, String password, Trust trustMode) {
        this.baseURL = baseurl;
        this.username = username;
        this.password = password;
        this.trustmode = trustMode;

        setProxy();

        if (trustMode == Trust.ALWAYS) {
            logger.error("Enable trust for self signed certificates");
            HTTPConnection.trustAllCertificates();
        }
        if (trustMode == Trust.WINDOWS) {
            logger.error("using windows trust store");
            // HTTPConnection.trustAllCertificates();
            System.setProperty("javax.net.ssl.trustStore", "NUL");
            System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
        }
    }

    public void setTokenLogin(String token) {
        this.SSOtoken = token;
    }

    public void setSession(Session session) {
        this.session = session;
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
     * It's not safe to trust all ssl certificates. Better use trusted keys but for now it's better than simple http
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

    private void setProxy() {
        if (System.getProperty("java.net.useSystemProxies") != null) {

            String proxyHost = "";
            int proxyPort = 3128;

            if (System.getProperty("java.net.useSystemProxies").toLowerCase().equals("true")) {

                List l = null;
                try {
                    l = ProxySelector.getDefault().select(new URI(this.baseURL));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                if (l != null) {
                    for (Iterator iter = l.iterator(); iter.hasNext(); ) {
                        java.net.Proxy proxy = (java.net.Proxy) iter.next();
                        //System.out.println("proxy hostname : " + proxy.type());
                        InetSocketAddress addr = (InetSocketAddress) proxy.address();
                        if (addr == null) {
                            //logger.warn("No system http proxy found");
                        } else {
                            logger.info("found system http proxy: {}:{}", addr.getHostName(), addr.getPort());
                            proxyHost = addr.getHostName();
                            proxyPort = addr.getPort();
                            break;

                        }
                    }
                } else {
                    logger.warn("No system http proxy found");
                }


            } else {
                logger.info("Check Manual proxy: " + System.getProperty("http.proxyHost"));
                if (System.getProperty("http.proxyHost") != null && !System.getProperty("http.proxyHost").isEmpty()) {
                    proxyHost = System.getProperty("http.proxyHost");
                }
                if (System.getProperty("http.proxyPort") != null && !System.getProperty("http.proxyPort").isEmpty()) {
                    try {
                        proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
                    } catch (Exception ex) {
                        logger.warn("Could not parse proxy port: {}", System.getProperty("http.proxyPort"));
                    }
                }

                if (System.getProperty("https.proxyHost") != null && !System.getProperty("https.proxyHost").isEmpty()) {
                    proxyHost = System.getProperty("https.proxyHost");
                }
                if (System.getProperty("https.proxyPort") != null && !System.getProperty("https.proxyPort").isEmpty()) {
                    try {
                        proxyPort = Integer.parseInt(System.getProperty("https.proxyPort"));
                    } catch (Exception ex) {
                        logger.warn("Could not parse proxy port: {}", System.getProperty("https.proxyPort"));
                    }
                }
            }

            if (!proxyHost.isEmpty()) {
                logger.info("Using http(s) proxy: {}:{}", proxyHost, proxyPort);
                System.setProperty("jdk.http.auth.tunneling.disabledSchemes", ""); //Workaround for certain auth problems
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            } else {
                logger.warn("No http Proxy found");
            }

        }
    }

    private void addAuth(HttpURLConnection conn, String username, String password) {
        if (session != null) {
            conn.setRequestProperty("session", session.getId());
        }

        if (SSOtoken != null) {
            conn.setRequestProperty("token", this.SSOtoken);
        }

        if (username != null && password != null) {
            String auth = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
            conn.setRequestProperty("Authorization", "Basic " + auth);
        }

    }

    public HttpURLConnection getHTTPConnection(String resource) throws IOException {
        resource = resource.replaceAll("\\s+", "%20");
        URL url = new URL(this.baseURL + "/" + resource);


        HttpURLConnection conn;
        if (proxy == null) {
            conn = (HttpURLConnection) url.openConnection();
        } else {
            conn = (HttpURLConnection) url.openConnection(proxy);

        }
        return conn;


    }

    /**
     * TODO: this function need an rework. The error handling a retry function are suboptimal
     *
     * @param resource
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public InputStream getInputStreamRequest(String resource) throws IOException, InterruptedException {
        int retry = 0;
        boolean delay = false;
        do {
            if (delay) {
                Thread.sleep(RETRY_DELAY_MS);
            }
            HttpURLConnection conn = getHTTPConnection(resource);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setReadTimeout(this.readTimeout);
            //System.out.println("Using Proxy?" + conn.usingProxy());
            addAuth(conn, this.username, this.password);

            conn.setRequestProperty("User-Agent", "JEAPI-WS");

            logger.debug("HTTP request {}", conn.getURL());
            logger.debug("Response: {}   {}", conn.getResponseCode(), conn.getResponseMessage());

            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    logger.warn(resource + "**not found**");
                    return null;
                case HttpURLConnection.HTTP_FORBIDDEN:
                    logger.warn(resource + "**forbidden**");
                    return null;
                case HttpURLConnection.HTTP_OK:
                    if ("gzip".equals(conn.getContentEncoding())) {
                        return new GZIPInputStream(conn.getInputStream());
                    } else {
                        return conn.getInputStream();
                    }
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    logger.warn(resource + " **gateway timeout**");
                    break;
                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                    logger.warn(resource + " **client timeout**");
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    logger.warn(resource + "**unavailable**");
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    logger.warn(resource + "**internal server error** - " + conn.getInputStream());
                    return null;
                default:
                    logger.warn(resource + " **{} : unknown response code**.", conn.getResponseCode());
                    return null;
            }

            conn.disconnect();

            retry++;
            logger.error("Failed retry {} for '{}'", retry, resource);
            delay = true;

        } while (retry < RETRIES);

        logger.fatal("Aborting download of input stream. '{}'", resource);
        return null;
    }

    public BufferedImage getIconRequest(String resource) throws IOException {
        Date start = new Date();
        HttpURLConnection conn = getHTTPConnection(resource);
        conn.setRequestMethod("GET");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

            BufferedImage imBuff = ImageIO.read(conn.getInputStream());

            conn.disconnect();
            in.close();


            logger.trace("HTTP request closed after: " + ((new Date()).getTime() - start.getTime()) + " msec");
            return imBuff;
        } else {
            return null;
        }

    }

    public byte[] getByteRequest(String resource) throws IOException {
        HttpURLConnection conn = getHTTPConnection(resource);
        conn.setRequestMethod("GET");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        logger.trace("responseCode {}", responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = conn.getInputStream();
            byte[] response = IOUtils.toByteArray(inputStream);
            inputStream.close();
            conn.disconnect();

            return response;

        } else {
            return null;
        }

    }

    public StringBuffer postRequest(String resource, String json) throws IOException, JEVisException {
        Date start = new Date();
        HttpURLConnection con = getHTTPConnection(resource);
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
            con.disconnect();
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
        HttpURLConnection con = getHTTPConnection(resource);
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
            con.connect();
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
        HttpURLConnection conn = getHTTPConnection(resource);
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
        Date start = new Date();//replace spaces
        HttpURLConnection conn = getHTTPConnection(resource);
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
        HttpURLConnection conn = getHTTPConnection(resource);
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
        HttpURLConnection conn = getHTTPConnection(resource);
        conn.setRequestMethod("DELETE");
        addAuth(conn, this.username, this.password);

        conn.setRequestProperty("User-Agent", "JEAPI-WS");

        logger.debug("HTTP DELETE request {}", conn.getURL());

        int responseCode = conn.getResponseCode();
        logger.trace("resonseCode {}", responseCode);
        return conn;

    }

    public enum Trust {
        ALWAYS, SYSTEM, WINDOWS
    }
}
