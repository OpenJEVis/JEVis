/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.soapdatasource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.driver.DataSourceHelper;
import org.jevis.commons.driver.Importer;
import org.jevis.commons.driver.Parser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class SOAPDataSource {
    private static final Logger logger = LogManager.getLogger(SOAPDataSource.class);

    private String _host;
    private Integer _port;
    private Integer _connectionTimeout = 30;
    private Integer _readTimeout = 60;
    private String _userName;
    private String _password;
    private Boolean _ssl = false;
    private DateTimeZone _timezone;

    private Parser _parser;
    private Importer _importer;

    /**
     * komplett überarbeiten!!!!!
     *
     * @param channel
     * @return
     */
    public List<InputStream> sendSampleRequest(Channel channel) {
        String uri = "";
        List<InputStream> answer = new ArrayList<InputStream>();
        javax.xml.soap.SOAPConnection conn = null;
        try {
            String path = channel.getPath();
            DateTime lastReadout = channel.getLastReadout();
            String template = channel.getTemplate();

            if (_userName != null) {
                uri += _userName;
                if (_password != null) {
                    uri += ":" + _password + "@";
                } else {
                    uri += "@";
                }
            }
//            String path = dp.getDirectory().getFolderName();
            uri += _host;
            if (_port != null) {
                uri += ":" + _port + path;
            } else {
                uri += ":80" + path;
            }

            if (!uri.contains("://")) {
                uri = "http://" + uri;
            }

            if (_ssl) {
                uri = uri.replace("http", "https");
            }
            logger.info("SOAP Uri: " + uri);
            URL serverURL = new URL(new URL(uri),
                    "",
                    new URLStreamHandler() {
                        @Override
                        protected URLConnection openConnection(URL url) throws IOException {
                            URL target = new URL(url.toString());
                            URLConnection connection = target.openConnection();
                            // Connection settings
                            connection.setConnectTimeout(_connectionTimeout * 1000); // 10 sec
                            connection.setReadTimeout(_readTimeout * 1000); // 1 min
                            return (connection);
                        }
                    });


//            DateTimeFormatter fmt = null;
//            if (_dateFormat != null && !_dateFormat.equals("")) {
//                fmt = DateTimeFormat.forPattern(_dateFormat);
//            }

            boolean containsToken = DataSourceHelper.containsTokens(template);
            String realQuery = null;
            if (containsToken) {
                realQuery = DataSourceHelper.replaceDateFrom(template, lastReadout, _timezone);
            } else {
                realQuery = template;
            }

            Document doc = buildDocument(realQuery);
            SOAPMessage buildSOAPMessage = buildSOAPMessage(doc);
//            List<InputHandler> inputHandler = new ArrayList<InputHandler>();
            if (_ssl) {
                DataSourceHelper.doTrustToCertificates();
            }
            conn = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage call = conn.call(buildSOAPMessage, serverURL);
            SOAPFault fault = call.getSOAPBody().getFault();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            call.writeTo(out);
            InputStream input = new ByteArrayInputStream(out.toByteArray());

            answer.add(input);
        } catch (MalformedURLException ex) {
            logger.error("Malformed URL exception. SOAP Uri: {}  and host: {}", uri, _host, ex);
        } catch (IOException ex) {
            logger.error("IO exception. SOAP Uri: {}  and host: {}", uri, _host, ex);
        } catch (UnsupportedOperationException ex) {
            logger.error("Unsupported operation exception. SOAP Uri: {}  and host: {}", uri, _host, ex);
        } catch (SOAPException ex) {
            logger.error("SOAP exception. SOAP Uri: {}  and host: {}", uri, _host, ex);
        } catch (Exception ex) {
            logger.error("Exception. SOAP Uri: {}  and host: {}", uri, _host, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SOAPException e) {
                    logger.error(e);
                }
            }
        }
        return answer;
    }

    private Document buildDocument(String s) {
        Document document = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(
                    new StringReader(s)));
            return document;
        } catch (SAXException ex) {
            logger.error("SAX exception. Host: {}", _host, ex);
        } catch (IOException ex) {
            logger.error("IO exception. Host: {}", _host, ex);
        } catch (ParserConfigurationException ex) {
            logger.error("Parser configuration exception. Host: {}", _host, ex);
        } finally {
            return document;
        }
    }

    private SOAPMessage buildSOAPMessage(Document doc) {
        MessageFactory msgFactory;
        SOAPMessage message = null;

        try {
            msgFactory = MessageFactory.newInstance();
            message = msgFactory.createMessage();
            SOAPPart soapPart = message.getSOAPPart();
            //         Load the SOAP text into a stream source
//            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            soapPart.setContent(new DOMSource(doc));

        } catch (Exception ex) {
            logger.error("Host: " + _host);
            logger.error(ex);
        }

        MimeHeaders headers = null;
        if (message != null) {
            headers = message.getMimeHeaders();
        }
        if (headers != null) {
            headers.addHeader("SOAPAction", "\"\"");
        }

        return message;
    }

    public void setHost(String _host) {
        this._host = _host;
    }

    public void setPort(Integer _port) {
        this._port = _port;
    }

    public void setConnectionTimeout(Integer _connectionTimeout) {
        this._connectionTimeout = _connectionTimeout;
    }

    public void setReadTimeout(Integer _readTimeout) {
        this._readTimeout = _readTimeout;
    }

    public void setUserName(String _userName) {
        this._userName = _userName;
    }

    public void setPassword(String _password) {
        this._password = _password;
    }

    public void setSsl(Boolean _ssl) {
        this._ssl = _ssl;
    }

    public void set_timezone(DateTimeZone _timezone) {
        this._timezone = _timezone;
    }
}
