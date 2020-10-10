package org.jevis.loytecxmldl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.TimeConverter;
import org.jevis.commons.driver.inputHandler.GenericConverter;
import org.jevis.xmlparser.DataPoint;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoytecXmlDlParser {

    private final static Logger logger = LogManager.getLogger(LoytecXmlDlParser.class.getName());
    private final GenericConverter converter = new GenericConverter();
    private final Charset charset = StandardCharsets.UTF_8;
    private final String mainElement = "Items";
    private final Boolean dateInElement = Boolean.TRUE;
    private final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private final String dateAttribute = "Timestamp";
    private final String statusLogAttribute = "Status Log";
    private final List<DataPoint> dataPoints = new ArrayList<>();
    private final List<Result> results = new ArrayList<>();
    private final List<JEVisSample> statusResults = new ArrayList<>();
    private String dateElement;
    private String valueElement;
    private String valueAttribute;
    private String mainAttribute;
    private DateTimeZone timeZone;

    public LoytecXmlDlParser() {
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }

    public void initChannel(LoytecXmlDlChannel channel) {

        DataPoint dp = new DataPoint();
        dp.setTargetStr(channel.getTargetString());
        dataPoints.add(dp);
        //dp.setValueIdentifier("Value");
        this.valueElement = channel.getTarget().getName();
    }

    public List<Result> parseStream(List<InputStream> input, DateTimeZone timezone) {
        this.parse(input, timezone);
        return results;
    }

    private void parse(List<InputStream> input, DateTimeZone timeZone) {
        this.timeZone = timeZone;
        for (InputStream inputStream : input) {

            converter.convertInput(inputStream, charset);
            List<Document> documents = (List<Document>) converter.getConvertedInput(Document.class);

            for (Document d : documents) {
                if (logger.getLevel().equals(Level.DEBUG)) {
                    try {
                        printDocument(d, System.out);

                    } catch (TransformerException | IOException e) {
                        e.printStackTrace();
                    }
                }

                NodeList elementsByTagName = d.getElementsByTagName(mainElement);

                DOMSource domSource = new DOMSource(d);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer;
                try {
                    transformer = tf.newTransformer();
                    transformer.transform(domSource, result);
                } catch (TransformerConfigurationException ex) {
                    logger.fatal("Error in transformer configuration: " + ex);
                } catch (TransformerException ex) {
                    logger.fatal("Error while transforming: " + ex);
                }

                //iterate over all nodes with the element name
                for (int i = 0; i < elementsByTagName.getLength(); i++) {
                    Node currentNode = elementsByTagName.item(i);
                    Node mainAttributeNode = null;
                    if (mainAttribute != null) {
                        NamedNodeMap attributes = currentNode.getAttributes();
                        mainAttributeNode = attributes.getNamedItem(mainAttribute);
                        if (mainAttributeNode == null) {
                            continue;
                        }
                    }
                    parseNode(currentNode, mainAttributeNode);

                }


            }
        }
    }

    private void parseNode(Node currentNode, Node mainAttributeNode) {

        for (DataPoint dp : dataPoints) {
            try {

//                Long datapointID = dp.getID();
//                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                String target = dp.getTargetStr();

                String valueIdentifier = dp.getValueIdentifier();

                if (mainAttributeNode != null && !mainAttributeNode.getNodeValue().equals(valueIdentifier)) {
                    continue;
                }

                boolean correct = false;
                //get Date
                Node dateNode = null;
                if (dateElement != null) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node item = currentNode.getChildNodes().item(j);
                        if (item.getNodeName().equals(dateElement)) {
                            dateNode = item;
                            break;
                        }
                    }
                } else {
                    dateNode = currentNode.cloneNode(true);
                }
                String dateString = null;
                if (dateAttribute != null && dateNode != null) {
                    Node namedItem = dateNode.getAttributes().getNamedItem(dateAttribute);
                    dateString = namedItem.getNodeValue();
                } else if (dateNode != null) {
                    dateString = dateNode.getTextContent();
                } else continue;

                String pattern = dateFormat;

//                DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
//                DateTime dateTime = fmt.parseDateTime(dateString);
                DateTime dateTime = TimeConverter.parserDateTime(dateString, pattern, timeZone);

//                    dpParser.parse(ic);
//                    value = dpParser.getValue();
                //get value
                Node valueNode = null;
                if (valueElement != null) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node item = currentNode.getChildNodes().item(j);
                        if (item.getNodeName().equals(valueElement)) {
                            valueNode = item;
                            break;
                        }
                    }
                } else {
                    valueNode = currentNode.cloneNode(true);
                }
                String valueString = null;
                if (valueAttribute != null && valueNode != null) {
                    Node namedItem = valueNode.getAttributes().getNamedItem(valueAttribute);
                    valueString = namedItem.getNodeValue();
                } else if (valueNode != null) {
                    valueString = valueNode.getTextContent();
                } else continue;

                Node statusAttribute = valueNode.getAttributes().getNamedItem("Status");
                Long statusValue = null;
                if (statusAttribute != null) {
                    try {
                        statusValue = Long.parseLong(statusAttribute.getNodeValue());
                    } catch (Exception e) {
                        logger.error("Could not parse Status {}", statusAttribute.getNodeValue(), e);
                    }
                }

                Double doubleValue = null;
                Object objectValue = null;
                try {
                    doubleValue = Double.parseDouble(valueString);
                } catch (Exception e) {
                    logger.warn("Could not get double value. continuing with object value");
                    objectValue = valueString;
                }

                if (doubleValue != null && statusValue == null) {
                    results.add(new Result(target, doubleValue, dateTime));
                } else if (doubleValue != null && !statusValue.equals(16L) && !statusValue.equals(255L)) {
                    results.add(new Result(target, doubleValue, dateTime));
                    statusResults.add(new VirtualSample(dateTime, statusValue));
                } else if (doubleValue != null) {
                    statusResults.add(new VirtualSample(dateTime, statusValue));
                } else {
                    if (objectValue != null && statusValue == null) {
                        results.add(new Result(target, objectValue, dateTime));
                    } else if (objectValue != null && !statusValue.equals(16L) && !statusValue.equals(255L)) {
                        results.add(new Result(target, objectValue, dateTime));
                        statusResults.add(new VirtualSample(dateTime, statusValue));
                    } else {
                        statusResults.add(new VirtualSample(dateTime, statusValue));
                    }
                }
            } catch (Exception ex) {
                logger.error("Could not parse Node: " + currentNode.toString() + " from main Attribute Node: " + mainAttributeNode);
            }
        }
    }

    public List<JEVisSample> getStatusResults() {
        return statusResults;
    }
}