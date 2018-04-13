/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.Converter;
import org.jevis.commons.driver.ConverterFactory;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTimeZone;

/**
 *
 * @author bf
 */
public class JEVisXMLParser implements Parser {

    interface XMLParserTypes extends DataCollectorTypes.Parser {

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

    interface XMLDataPointDirectoryTypes extends DataCollectorTypes.DataPointDirectory {

        public final static String NAME = "XML Data Point Directory";
    }

    interface XMLDataPointTypes extends DataCollectorTypes.DataPoint {

        public final static String NAME = "XML Data Point";
        public final static String MAPPING_IDENTIFIER = "Mapping Identifier";
        public final static String VALUE_IDENTIFIER = "Value Identifier";
        public final static String TARGET = "Target";
    }
    private XMLParser _xmlParser;

    /**
     *
     * @param inputList
     * @param timeZone
     */
    @Override
    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        _xmlParser.parse(inputList, timeZone);
    }

    @Override
    public List<Result> getResult() {
        return _xmlParser.getResult();
    }

    @Override
    public void initialize(JEVisObject parserObject) {
        initializeAttributes(parserObject);

        Converter converter = ConverterFactory.getConverter(parserObject);
        _xmlParser.setConverter(converter);

        initializeXMLDataPointParser(parserObject);
    }

    private void initializeAttributes(JEVisObject parserObject) {
        try {
            JEVisClass jeClass = parserObject.getJEVisClass();

            JEVisType dateFormatType = jeClass.getType(XMLParserTypes.DATE_FORMAT);
            JEVisType timeFormatType = jeClass.getType(XMLParserTypes.TIME_FORMAT);
            JEVisType decimalSeperatorType = jeClass.getType(XMLParserTypes.DECIMAL_SEPERATOR);
            JEVisType thousandSeperatorType = jeClass.getType(XMLParserTypes.THOUSAND_SEPERATOR);

            JEVisType mainElementType = jeClass.getType(XMLParserTypes.MAIN_ELEMENT);
            JEVisType mainAttributeType = jeClass.getType(XMLParserTypes.MAIN_ATTRIBUTE);
            JEVisType valueElementType = jeClass.getType(XMLParserTypes.VALUE_ELEMENT);
            JEVisType valueAttributeType = jeClass.getType(XMLParserTypes.VALUE_ATTRIBUTE);
            JEVisType valueInElementType = jeClass.getType(XMLParserTypes.VALUE_IN_ELEMENT);
            JEVisType dateElementType = jeClass.getType(XMLParserTypes.DATE_ELEMENT);
            JEVisType dateAttributeType = jeClass.getType(XMLParserTypes.DATE_ATTRIBUTE);
            JEVisType dateInElementType = jeClass.getType(XMLParserTypes.DATE_IN_ELEMENT);
            JEVisType charsetElementType = jeClass.getType(XMLParserTypes.CHARSET);

            String dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateFormat: " + dateFormat);
            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "TimeFormat: " + timeFormat);
            String decimalSeperator = DatabaseHelper.getObjectAsString(parserObject, decimalSeperatorType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DecimalSeperator: " + decimalSeperator);
            String thousandSeperator = DatabaseHelper.getObjectAsString(parserObject, thousandSeperatorType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ThousandSeperator: " + thousandSeperator);

            String mainElement = DatabaseHelper.getObjectAsString(parserObject, mainElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "MainElement: " + mainElement);
            String mainAttribute = DatabaseHelper.getObjectAsString(parserObject, mainAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "MainAttribute: " + mainAttribute);
            String valueElement = DatabaseHelper.getObjectAsString(parserObject, valueElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueElement: " + valueElement);
            String valueAtribute = DatabaseHelper.getObjectAsString(parserObject, valueAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueAttribute: " + valueAtribute);
            Boolean valueInElement = DatabaseHelper.getObjectAsBoolean(parserObject, valueInElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueInElement: " + valueInElement);
            String dateElement = DatabaseHelper.getObjectAsString(parserObject, dateElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateElement: " + dateElement);
            String dateAttribute = DatabaseHelper.getObjectAsString(parserObject, dateAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateAttribute: " + dateAttribute);
            Boolean dateInElement = DatabaseHelper.getObjectAsBoolean(parserObject, dateInElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateInElement: " + dateInElement);
            String charsetInElement = DatabaseHelper.getObjectAsString(parserObject, charsetElementType);
            Charset charset;
            if(charsetInElement==null || charsetInElement.equals("")){
               charset = Charset.defaultCharset();
            } else {
               charset = Charset.forName(charsetInElement);
            }
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "CharsetElement: " + charset.displayName());

            _xmlParser = new XMLParser();
            _xmlParser.setDateFormat(dateFormat);
            _xmlParser.setTimeFormat(timeFormat);
            _xmlParser.setDecimalSeperator(decimalSeperator);
            _xmlParser.setThousandSeperator(thousandSeperator);
            _xmlParser.setMainElement(mainElement);
            _xmlParser.setMainAttribute(mainAttribute);
            _xmlParser.setValueElement(valueElement);
            _xmlParser.setValueAtribute(valueAtribute);
            _xmlParser.setValueInElement(valueInElement);
            _xmlParser.setDateElement(dateElement);
            _xmlParser.setDateAttribute(dateAttribute);
            _xmlParser.setDateInElement(dateInElement);
            _xmlParser.setCharset(charset);
        } catch (JEVisException ex) {
            Logger.getLogger(JEVisXMLParser.class.getName()).log(Level.ERROR, ex.getMessage());
        }
    }

    private void initializeXMLDataPointParser(JEVisObject parserObject) {
        try {
            JEVisClass dirClass = parserObject.getDataSource().getJEVisClass(XMLDataPointDirectoryTypes.NAME);
            JEVisObject dir = parserObject.getChildren(dirClass, true).get(0);
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(XMLDataPointTypes.NAME);
            List<JEVisObject> dataPoints = dir.getChildren(dpClass, true);
            List<DataPoint> xmldatapoints = new ArrayList<DataPoint>();
            for (JEVisObject dp : dataPoints) {
                JEVisType mappingIdentifierType = dpClass.getType(XMLDataPointTypes.MAPPING_IDENTIFIER);
                JEVisType targetType = dpClass.getType(XMLDataPointTypes.TARGET);
                JEVisType valueIdentifierType = dpClass.getType(XMLDataPointTypes.VALUE_IDENTIFIER);

                Long datapointID = dp.getID();
                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                String targetString = DatabaseHelper.getObjectAsString(dp, targetType);
                Long target = null;
                try {
                    target = Long.parseLong(targetString);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String valueIdent = DatabaseHelper.getObjectAsString(dp, valueIdentifierType);

                DataPoint xmldp = new DataPoint();
                xmldp.setMappingIdentifier(mappingIdentifier);
                xmldp.setTarget(target);
                xmldp.setValueIdentifier(valueIdent);
                xmldatapoints.add(xmldp);
            }
            _xmlParser.setDataPoints(xmldatapoints);
        } catch (JEVisException ex) {
            Logger.getLogger(JEVisXMLParser.class.getName()).log(Level.ERROR, ex.getMessage());
        }
    }

}
