/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.driver.Converter;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.TimeConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author bf
 */
public class XMLParser {

    private DateTimeZone timeZone;

    //interfaces
    interface XML extends DataCollectorTypes.Parser {

        public final static String NAME = "XML Parser";
        public final static String GENERAL_TAG = "General Tag";
        public final static String SPECIFICATION_TAG = "Specification Tag";
        public final static String SPECIFICATION_ATTRIBUTE = "Specification In Attribute";
        public final static String VALUE_TAG = "Value Tag";
        public final static String VALUE_IN_ATTRIBUTE = "Value In Attribute";
        public final static String DATE_TAG = "Date Tag";
        public final static String DATE_IN_ATTRIBUTE = "Date In Attribute";
        public final static String TIME_TAG = "Time Tag";
        public final static String TIME_IN_ATTRIBUTE = "Time In Attribute";
        public final static String MAIN_ELEMENT = "Main Element";
        public final static String MAIN_ATTRIBUTE = "Main Attribute";
        public final static String DATE_ELEMENT = "Date Element";
        public final static String DATE_ATTRIBUTE = "Date Attribute";
        public final static String DATE_IN_ELEMENT = "Date in Element";
        public final static String VALUE_ELEMENT = "Value Element";
        public final static String VALUE_ATTRIBUTE = "Value Attribute";
        public final static String VALUE_IN_ELEMENT = "Value in Element";
        public final static String DATE_FORMAT = "Date Format";
        public final static String DECIMAL_SEPERATOR = "Decimal Separator";
        public final static String TIME_FORMAT = "Time Format";
        public final static String THOUSAND_SEPERATOR = "Thousand Separator";
    }

    interface XMLDataPoint extends DataCollectorTypes.DataPoint {

        public final static String NAME = "XML Data Point";
        public final static String MAPPING_IDENTIFIER = "Mapping Identifier";
        public final static String VALUE_IDENTIFIER = "Value Identifier";
        public final static String TARGET = "Target";
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
    private List<Result> _results = new ArrayList<Result>();
    private Converter _converter;
   

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
                    Logger.getLogger(XMLParser.class.getName()).log(Level.ERROR, ex.getMessage());
                } catch (TransformerException ex) {
                    Logger.getLogger(XMLParser.class.getName()).log(Level.ERROR, ex.getMessage());
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
//                    ic.setXMLInput(currentNode);

                    //single parsing
                    boolean isCorrectNode = true; //eigentl false
                    DateTime dateTime = null;
                    Double value = null;
                    Long datapoint = null;
                    try {
                        parseNode(currentNode, mainAttributeNode);
                    } catch (JEVisException ex) {
                        Logger.getLogger(XMLParser.class.getName()).log(Level.ERROR, ex.getMessage());
                    }

                    //parse the correct node
                    if (isCorrectNode) {
                    }
                }
            }
        }
    }

    private void parseNode(Node currentNode, Node mainAttributeNode) throws JEVisException {

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
                if (_dateAttribute != null) {
                    Node namedItem = dateNode.getAttributes().getNamedItem(_dateAttribute);
                    dateString = namedItem.getNodeValue();
                } else {
                    dateString = dateNode.getTextContent();
                }
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
                if (_valueAtribute != null) {
                    Node namedItem = valueNode.getAttributes().getNamedItem(_valueAtribute);
                    valueString = namedItem.getNodeValue();
                } else {
                    valueString = valueNode.getTextContent();
                }
                Double value = Double.parseDouble(valueString);
                correct = true;

//                    if (dpParser.outOfBounce()) {
//                        org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.WARN, "Date for value out of bounce: " + dateTime);
//                        org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.WARN, "Value out of bounce: " + value);
//                    }
                if (!correct) {
                    continue;
                }
                _results.add(new Result(target, value, dateTime));
            } catch (Exception ex) {
                Logger.getLogger(XMLParser.class.getName()).log(Level.ERROR, ex.getMessage());
            }
        }
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
    
    public void setCharset(Charset charset){
        this.charset = charset;
    }

}
