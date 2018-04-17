/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author gf
 */
public class ConfigXMLParse {

    private static final String NODE_JEVISCONFIG = "JEVisConfig";
    private static final String NODE_JENOTIFIER = "JENotifier";
    private static final String NODE_NOTIFIER = "Notifier";
//    private Map<String, String> _jevisConfig;
    private Map<String, String> _jenotifier;

    public ConfigXMLParse() {
//        _jevisConfig = new HashMap<String, String>();
        _jenotifier = new HashMap<String, String>();
    }

    /**
     * Return the configuration parameters of jevis
     *
     * @return
     */
//    public Map<String, String> getJEVisConfigParam() {
//        return _jevisConfig;
//    }
    /**
     * Return the configuration parameters of jenotifier
     *
     * @return
     */
    public Map<String, String> getJENotifierParam() {
        return _jenotifier;
    }

    /**
     * Use the methode DOM to parse the XML Document to get all configuration
     * parameters and put them into map.
     *
     * @param xmlPath
     */
    public void XMLToMap(String xmlPath) {
        try {
            File xmlFile = new File(xmlPath);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            doc.getDocumentElement().normalize();
            //        System.out.println("Root  element: " + doc.getDocumentElement().getNodeName());

            readXMLAttribute(doc, NODE_JEVISCONFIG, _jenotifier);
            readXMLAttribute(doc, NODE_JENOTIFIER, _jenotifier);
            readXMLAttribute(doc, NODE_NOTIFIER, _jenotifier);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ConfigXMLParse.class.getName()).log(Level.ERROR, null, ex);
            Logger.getLogger("EXCEPTION").log(Level.ERROR, "", ex);
        } catch (SAXException ex) {
            Logger.getLogger(ConfigXMLParse.class.getName()).log(Level.ERROR, null, ex);
            Logger.getLogger("EXCEPTION").log(Level.ERROR, "", ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigXMLParse.class.getName()).log(Level.ERROR, null, ex);
            Logger.getLogger("EXCEPTION").log(Level.ERROR, "", ex);
        }

    }
    
    /**
     * To read all attributes' value of the tagName and put the attrubutes' name and vaule into Map m. 
     * @param doc
     * @param tagName
     * @param m the map to receive the attrubutes' name and vaule
     */
    private void readXMLAttribute(Document doc, String tagName, Map<String, String> m) {
        NodeList jevisconfig = doc.getElementsByTagName(tagName);
//            System.out.println(jevisconfig.getLength());
        if (jevisconfig.getLength() != 0) {
            for (int i = 0; i < jevisconfig.getLength(); i++) {
                Node node = jevisconfig.item(i);
                //                System.out.println("Node name: " + node.getNodeName());
                NamedNodeMap nodeMap = node.getAttributes();
                for (int j = 0; j < nodeMap.getLength(); j++) {
                    Node nodenew = nodeMap.item(j);
                    m.put(nodenew.getNodeName(), nodenew.getNodeValue());

                    //                    System.out.println("node name " + "node value ");
//                        System.out.println(nodenew.getNodeName() + ": " + nodenew.getNodeValue());
                }
            }
        }
    }
}
