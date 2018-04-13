/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector.CLIProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @deprecated (AI 13.03.2018)
 * Not used
 * 
 * @author bf
 */
public class ParsingCLIParser {

    private String _parsingType;
    private String _quote;
    private String _delim;
    private Integer _headerlines;
    private Integer _valueIndex;
    private String _thousandSep;
    private String _decimalSep;
    private Integer _dateIndex;
    private String _dateformat;
    private Integer _timeIndex;
    private String _timeformat;

    public ParsingCLIParser(String path) {
        Properties prop = new Properties();

        try {
            //load a properties file
            prop.load(new FileInputStream(path));

            //get the property value and print it out
            _parsingType = prop.getProperty("type");
            _quote = prop.getProperty("quote");
            _delim = prop.getProperty("delim");
            _headerlines = getPropAsInteger(prop,"headerlines");
            _valueIndex = getPropAsInteger(prop,"valueindex");
            _thousandSep = prop.getProperty("thousandsep");
            _decimalSep = prop.getProperty("decimalsep");
            _dateIndex = getPropAsInteger(prop,"dateindex");
            _dateformat = prop.getProperty("dateformat");
            _timeIndex = getPropAsInteger(prop,"timeindex");
            _timeformat = prop.getProperty("timeformat");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
        private Integer getPropAsInteger(Properties prop, String propertyName) {
        if (prop.getProperty(propertyName) != null) {
            return Integer.parseInt(prop.getProperty(propertyName));
        } else {
            return null;
        }
    }

    /**
     * @return the _parsingType
     */
    public String getParsingType() {
        return _parsingType;
    }

    /**
     * @return the _quote
     */
    public String getQuote() {
        return _quote;
    }

    /**
     * @return the _delim
     */
    public String getDelim() {
        return _delim;
    }

    /**
     * @return the _headerlines
     */
    public Integer getHeaderlines() {
        return _headerlines;
    }

    /**
     * @return the _valueIndex
     */
    public Integer getValueIndex() {
        return _valueIndex;
    }

    /**
     * @return the _thousandSep
     */
    public String getThousandSep() {
        return _thousandSep;
    }

    /**
     * @return the _decimalSep
     */
    public String getDecimalSep() {
        return _decimalSep;
    }

    /**
     * @return the _dateIndex
     */
    public Integer getDateIndex() {
        return _dateIndex;
    }

    /**
     * @return the _dateformat
     */
    public String getDateformat() {
        return _dateformat;
    }

    public Integer getTimeIndex() {
        return _timeIndex;
    }

    public String getTimeformat() {
        return _timeformat;
    }
}
