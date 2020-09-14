/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.driver.Converter;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.TimeConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class XMLParser {
    private static final Logger logger = LogManager.getLogger(XMLParser.class);

    private DateTimeZone timeZone;

    private final List<Result> _results = new ArrayList<Result>();

    public void parse(List<InputStream> input, DateTimeZone timeZone) {
        this.timeZone = timeZone;
        for (InputStream inputStream : input) {

            _converter.convertInput(inputStream, charset);
            List<Document> documents = (List<Document>) _converter.getConvertedInput(Document.class);

            for (Document d : documents) {
                NodeList elementsByTagName = d.getElementsByTagName(_mainElement);

                DOMSource domSource = new DOMSource(d);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer;
                try {
                    transformer = tf.newTransformer();
                    transformer.transform(domSource, result);
                } catch (TransformerConfigurationException ex) {
                    logger.fatal("Error in transformer configuration: {}", ex.getMessage(), ex);
                } catch (TransformerException ex) {
                    logger.fatal("Error while transforming: {}", ex.getMessage(), ex);
                }

                //iterate over all nodes with the element name
                for (int i = 0; i < elementsByTagName.getLength(); i++) {
                    Node currentNode = elementsByTagName.item(i);
                    Node mainAttributeNode = null;
                    if (_mainAttribute != null) {
                        NamedNodeMap attributes = currentNode.getAttributes();
                        mainAttributeNode = attributes.getNamedItem(_mainAttribute);
                        if (mainAttributeNode == null) {
                            continue;
                        }
                    }
                    parseNode(currentNode, mainAttributeNode);

                }
            }
        }
    }

    // member variables
//        private List<XMLDatapointParser> _datapointParsers = new ArrayList<XMLDatapointParser>();
    private String _dateFormat;
    private String _timeFormat;
    private String _decimalSeperator;
    private String _thousandSeperator;
    private String _mainElement;
    private String _mainAttribute;
    private String _valueElement;
    private String _valueAtribute;
    private Boolean _valueInElement;
    private String _dateElement;
    private String _dateAttribute;
    private Boolean _dateInElement;
    private Charset charset;

    private List<DataPoint> _dataPoints = new ArrayList<DataPoint>();

    private void parseNode(Node currentNode, Node mainAttributeNode) {

        for (DataPoint dp : _dataPoints) {
            try {

//                Long datapointID = dp.getID();
//                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                Long target = dp.getTarget();

                String valueIdentifier = dp.getValueIdentifier();

                if (mainAttributeNode != null && !mainAttributeNode.getNodeValue().equals(valueIdentifier)) {
                    continue;
                }

                boolean correct = false;
                //get Date
                Node dateNode = null;
                if (_dateElement != null) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node item = currentNode.getChildNodes().item(j);
                        if (item.getNodeName().equals(_dateElement)) {
                            dateNode = item;
                            break;
                        }
                    }
                } else {
                    dateNode = currentNode.cloneNode(true);
                }
                String dateString = null;
                if (_dateAttribute != null && dateNode != null) {
                    Node namedItem = dateNode.getAttributes().getNamedItem(_dateAttribute);
                    dateString = namedItem.getNodeValue();
                } else if (dateNode != null) {
                    dateString = dateNode.getTextContent();
                } else continue;

                String pattern = _dateFormat;

//                DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
//                DateTime dateTime = fmt.parseDateTime(dateString);
                DateTime dateTime = TimeConverter.parserDateTime(dateString, pattern, timeZone);

//                    dpParser.parse(ic);
//                    value = dpParser.getValue();
                //get value
                Node valueNode = null;
                if (_valueElement != null) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node item = currentNode.getChildNodes().item(j);
                        if (item.getNodeName().equals(_valueElement)) {
                            valueNode = item;
                            break;
                        }
                    }
                } else {
                    valueNode = currentNode.cloneNode(true);
                }
                String valueString = null;
                if (_valueAtribute != null && valueNode != null) {
                    Node namedItem = valueNode.getAttributes().getNamedItem(_valueAtribute);
                    valueString = namedItem.getNodeValue();
                } else if (valueNode != null) {
                    valueString = valueNode.getTextContent();
                } else continue;

                Double doubleValue = null;
                Object objectValue = null;
                try {
                    doubleValue = Double.parseDouble(valueString);
                } catch (Exception e) {
                    logger.warn("Could not get double value. continuing with object value");
                    objectValue = valueString;
                }

                if (doubleValue != null) _results.add(new Result(target, doubleValue, dateTime));
                else _results.add(new Result(target, objectValue, dateTime));
            } catch (Exception ex) {
                logger.error("Could not parse Node: {} from main Attribute Node: {}", currentNode.toString(), mainAttributeNode);
            }
        }
    }
    private Converter _converter;

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    //interfaces
    interface XML extends DataCollectorTypes.Parser {

        String NAME = "XML Parser";
        String GENERAL_TAG = "General Tag";
        String SPECIFICATION_TAG = "Specification Tag";
        String SPECIFICATION_ATTRIBUTE = "Specification In Attribute";
        String VALUE_TAG = "Value Tag";
        String VALUE_IN_ATTRIBUTE = "Value In Attribute";
        String DATE_TAG = "Date Tag";
        String DATE_IN_ATTRIBUTE = "Date In Attribute";
        String TIME_TAG = "Time Tag";
        String TIME_IN_ATTRIBUTE = "Time In Attribute";
        String MAIN_ELEMENT = "Main Element";
        String MAIN_ATTRIBUTE = "Main Attribute";
        String DATE_ELEMENT = "Date Element";
        String DATE_ATTRIBUTE = "Date Attribute";
        String DATE_IN_ELEMENT = "Date in Element";
        String VALUE_ELEMENT = "Value Element";
        String VALUE_ATTRIBUTE = "Value Attribute";
        String VALUE_IN_ELEMENT = "Value in Element";
        String DATE_FORMAT = "Date Format";
        String DECIMAL_SEPERATOR = "Decimal Separator";
        String TIME_FORMAT = "Time Format";
        String THOUSAND_SEPERATOR = "Thousand Separator";
    }

    public List<Result> getResult() {
        return _results;
    }

    public void setDateFormat(String _dateFormat) {
        this._dateFormat = _dateFormat;
    }

    public void setTimeFormat(String _timeFormat) {
        this._timeFormat = _timeFormat;
    }

    public void setDecimalSeperator(String _decimalSeperator) {
        this._decimalSeperator = _decimalSeperator;
    }

    public void setThousandSeperator(String _thousandSeperator) {
        this._thousandSeperator = _thousandSeperator;
    }

    public void setMainElement(String _mainElement) {
        this._mainElement = _mainElement;
    }

    public void setMainAttribute(String _mainAttribute) {
        this._mainAttribute = _mainAttribute;
    }

    public void setValueElement(String _valueElement) {
        this._valueElement = _valueElement;
    }

    public void setValueAtribute(String _valueAtribute) {
        this._valueAtribute = _valueAtribute;
    }

    public void setValueInElement(Boolean _valueInElement) {
        this._valueInElement = _valueInElement;
    }

    public void setDateElement(String _dateElement) {
        this._dateElement = _dateElement;
    }

    public void setDateAttribute(String _dateAttribute) {
        this._dateAttribute = _dateAttribute;
    }

    public void setDateInElement(Boolean _dateInElement) {
        this._dateInElement = _dateInElement;
    }

    public void setDataPoints(List<DataPoint> _dataPoints) {
        this._dataPoints = _dataPoints;
    }

    public void setConverter(Converter _converter) {
        this._converter = _converter;
    }

    interface XMLDataPoint extends DataCollectorTypes.DataPoint {

        String NAME = "XML Data Point";
        String MAPPING_IDENTIFIER = "Mapping Identifier";
        String VALUE_IDENTIFIER = "Value Identifier";
        String TARGET = "Target";
    }

}
