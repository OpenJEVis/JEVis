/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Broder
 */
public abstract class InputHandler implements Iterable<Object> {
    private static final Logger logger = LogManager.getLogger(InputHandler.class);
    protected Object _rawInput;
    protected List<InputStream> _inputStream;
    protected String[] _csvInput;
    private String[] _stringArrayOutput;
    private boolean _stringArrayOutputParsed;
    private String _stringOutput;
    private boolean _stringOutputParsed;
    private Node _xmlInput;
    protected List<Document> _document;
    private Object _tmpInput;
    private String _filePath;
    protected String _filePattern;
    protected DateTime _lastReadout;
    private Charset charset;

    public InputHandler(Object rawInput, Charset charset) {
        _inputStream = new ArrayList<InputStream>();
        _rawInput = rawInput;
        _document = new ArrayList<Document>();
        this.charset = charset;
    }

    public void setInput(Object input) {
        _rawInput = input;
    }

    public abstract void convertInput();

    @Override
    public Iterator iterator() {
        return _inputStream.iterator();
    }

    public Object getRawInput() {
        return _rawInput;
    }

    public String[] getStringArrayInput() {
        if (!_stringArrayOutputParsed) {
            List<String> stringInput = new ArrayList<String>();
            for (InputStream s : _inputStream) {
                try {
                    String tmp = null;
                    BufferedReader buf = new BufferedReader(new InputStreamReader(s, charset));
                    while ((tmp = buf.readLine()) != null) {
                        stringInput.add(tmp);
                    }

//                    String inputStreamString = IOUtils.toString(s, "UTF-8");
//                    for (int i = 0; i < inputStreamString.length; i++) {
//                        stringInput.add(inputStreamString[i]);
//                    }
                } catch (IOException ex) {
                    logger.fatal(ex);
                }
            }
            String[] inputArray = new String[stringInput.size()];
            _stringArrayOutput = stringInput.toArray(inputArray);
            _stringArrayOutputParsed = true;
        }
        return _stringArrayOutput;
    }

    public void setCSVInput(String[] input) {
        _csvInput = input;
    }

    public void setTmpInput(Object o) {
        _tmpInput = o;
    }

    public Object getTmpInput() {
        return _tmpInput;
    }

    public String[] getCSVInput() {
        return _csvInput;
    }

    public void setXMLInput(Node input) {
        _xmlInput = input;
    }

    public Node getXMLInput() {
        return _xmlInput;
    }

    public void setInputStream(List<InputStream> input) {
        _inputStream = input;
    }

    public String getStringInput() {
        if (!_stringOutputParsed) {

            StringBuilder buffer = new StringBuilder();
            for (InputStream s : _inputStream) {
                String tmp = null;
                BufferedReader buf = new BufferedReader(new InputStreamReader(s, charset));
                try {
                    while ((tmp = buf.readLine()) != null) {
                        buffer.append(tmp);
                    }
                } catch (IOException ex) {
                    logger.fatal(ex);
                }
            }
            _stringOutput = buffer.toString();
            _stringOutputParsed = true;
        }
        return _stringOutput;
    }

    public List<Document> getDocuments() {
        if (_document.isEmpty()) {
            try {
                String stringInput = getStringInput();
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                domFactory.setNamespaceAware(true); // never forget this!
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                _document.add(builder.parse(new InputSource(new StringReader(stringInput))));
            } catch (ParserConfigurationException ex) {
                logger.fatal(ex);
            } catch (SAXException ex) {
                logger.fatal(ex);
            } catch (IOException ex) {
                logger.fatal(ex);
            }
        }
        return _document;
    }

    public void setFilePath(String fileName) {
        _filePath = fileName;
    }

    public void setFilePattern(String filePattern) {
        _filePattern = filePattern;
    }

    public String getFilePath() {
        return _filePath;
    }

    public void setDateTime(DateTime lastReadout) {
        _lastReadout = lastReadout;
    }
}
