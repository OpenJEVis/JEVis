/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.soapdatasource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.driver.DataSourceHelper;
import org.jevis.commons.driver.Importer;
import org.jevis.commons.driver.Parser;
import org.joda.time.DateTime;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author bf
 */
public class SOAPDataSource {
    private static final Logger logger = LogManager.getLogger(SOAPDataSource.class);

    private String _host;
    private Integer _port;
    private Integer _connectionTimeout;
    private Integer _readTimeout;
    private String _userName;
    private String _password;
    private Boolean _ssl = false;
    private String _timezone;

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
            URL serverURL = new URL(uri);

//            DateTimeFormatter fmt = null;
//            if (_dateFormat != null && !_dateFormat.equals("")) {
//                fmt = DateTimeFormat.forPattern(_dateFormat);
//            }
            List<SOAPMessage> soapResponses = new LinkedList<SOAPMessage>();

            String templateQuery = template;
            boolean containsToken = DataSourceHelper.containsTokens(templateQuery);
            String realQuery = null;
            if (containsToken) {
                realQuery = DataSourceHelper.replaceDateFrom(templateQuery, lastReadout);
            } else {
                realQuery = templateQuery;
            }

            Document doc = buildDocument(realQuery);
            SOAPMessage buildSOAPMessage = buildSOAPMessage(doc);
//            List<InputHandler> inputHandler = new ArrayList<InputHandler>();
            if (_ssl) {
                DataSourceHelper.doTrustToCertificates();
            }
            javax.xml.soap.SOAPConnection conn = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage call = conn.call(buildSOAPMessage, serverURL);
            soapResponses.add(call);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            call.writeTo(out);
            InputStream input = new ByteArrayInputStream(out.toByteArray());

            //        soapRequests.add(buildSOAPMessage);
            //        for (int i = 0; i < soapRequests.size(); i++) {
            //        }
            //        }
//            inputHandler.add(InputHandlerFactory.getInputConverter(soapResponses));
            answer.add(input);
        } catch (JEVisException ex) {
            logger.error("SOAP Uri: " + uri + " and host: " + _host);
            logger.error(ex);
        } catch (MalformedURLException ex) {
            logger.error("SOAP Uri: " + uri + " and host: " + _host);
            logger.error(ex);
        } catch (Exception ex) {
            logger.error("SOAP Uri: " + uri);
            logger.error("SOAP Uri: " + uri + " and host: " + _host);
            logger.error(ex);
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
        } catch (SAXException ex) {
            logger.error("Host: " + _host);
            logger.error(ex);
        } catch (IOException ex) {
            logger.error("Host: " + _host);
            logger.error(ex);
        } catch (ParserConfigurationException ex) {
            logger.error("Host: " + _host);
            logger.error(ex);
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

        MimeHeaders headers = message.getMimeHeaders();
        headers.addHeader("SOAPAction", "\"\"");

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

}
