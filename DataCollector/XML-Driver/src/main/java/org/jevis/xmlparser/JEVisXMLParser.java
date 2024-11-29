/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class JEVisXMLParser implements Parser {
    private static final Logger logger = LogManager.getLogger(JEVisXMLParser.class);

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
            logger.debug("DateFormat: {}", dateFormat);
            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);
            logger.debug("TimeFormat: {}", timeFormat);
            String decimalSeperator = DatabaseHelper.getObjectAsString(parserObject, decimalSeperatorType);
            logger.debug("DecimalSeperator: {}", decimalSeperator);
            String thousandSeperator = DatabaseHelper.getObjectAsString(parserObject, thousandSeperatorType);
            logger.debug("ThousandSeperator: {}", thousandSeperator);

            String mainElement = DatabaseHelper.getObjectAsString(parserObject, mainElementType);
            logger.debug("MainElement: {}", mainElement);
            String mainAttribute = DatabaseHelper.getObjectAsString(parserObject, mainAttributeType);
            logger.debug("MainAttribute: {}", mainAttribute);
            String valueElement = DatabaseHelper.getObjectAsString(parserObject, valueElementType);
            logger.debug("ValueElement: {}", valueElement);
            String valueAtribute = DatabaseHelper.getObjectAsString(parserObject, valueAttributeType);
            logger.debug("ValueAttribute: {}", valueAtribute);
            Boolean valueInElement = DatabaseHelper.getObjectAsBoolean(parserObject, valueInElementType);
            logger.debug("ValueInElement: {}", valueInElement);
            String dateElement = DatabaseHelper.getObjectAsString(parserObject, dateElementType);
            logger.debug("DateElement: {}", dateElement);
            String dateAttribute = DatabaseHelper.getObjectAsString(parserObject, dateAttributeType);
            logger.debug("DateAttribute: {}", dateAttribute);
            Boolean dateInElement = DatabaseHelper.getObjectAsBoolean(parserObject, dateInElementType);
            logger.debug("DateInElement: {}", dateInElement);
            String charsetInElement = DatabaseHelper.getObjectAsString(parserObject, charsetElementType);
            Charset charset;
            if (charsetInElement == null || charsetInElement.equals("")) {
                charset = Charset.defaultCharset();
            } else {
                charset = Charset.forName(charsetInElement);
            }
            logger.debug("CharsetElement: {}", charset.displayName());

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
            logger.fatal(ex);
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
                String target = null;
                TargetHelper targetHelper = new TargetHelper(dp.getDataSource(), targetString);
                if (targetHelper.isValid() && targetHelper.targetObjectAccessible()) {
                    target = targetString;
                } else {
                    logger.info("DataPoint target error: {}", datapointID);
                    continue;
                }
                String valueIdent = DatabaseHelper.getObjectAsString(dp, valueIdentifierType);

                DataPoint xmldp = new DataPoint();
                xmldp.setMappingIdentifier(mappingIdentifier);
                xmldp.setTargetStr(target);
                xmldp.setValueIdentifier(valueIdent);
                xmldatapoints.add(xmldp);
            }
            _xmlParser.setDataPoints(xmldatapoints);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    interface XMLParserTypes extends DataCollectorTypes.Parser {

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

    private XMLParser _xmlParser;

    /**
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
    public ParserReport getReport() {
        return _xmlParser.getReport();
    }

    @Override
    public void initialize(JEVisObject parserObject) {
        initializeAttributes(parserObject);

        Converter converter = ConverterFactory.getConverter(parserObject);
        _xmlParser.setConverter(converter);

        initializeXMLDataPointParser(parserObject);
    }

    interface XMLDataPointDirectoryTypes extends DataCollectorTypes.DataPointDirectory {

        String NAME = "XML Data Point Directory";
    }

    interface XMLDataPointTypes extends DataCollectorTypes.DataPoint {

        String NAME = "XML Data Point";
        String MAPPING_IDENTIFIER = "Mapping Identifier";
        String VALUE_IDENTIFIER = "Value Identifier";
        String TARGET = "Target";
    }

}
