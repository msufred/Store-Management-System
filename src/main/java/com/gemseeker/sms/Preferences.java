package com.gemseeker.sms;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Custom preferences class for saving database and application settings to a
 * XML file. Preferences is a singleton, get an instance by calling
 * getInstance().
 *
 * @author Gem Seeker
 *
 */
public class Preferences {

    private static final String PREF_FILENAME = "preferences.xml";
    private Document doc;
    private static Preferences instance;
    private boolean isLoaded = false;

    private Preferences() {
        System.out.print("Creating instance of Preferences...");
        try {
            File file = new File(PREF_FILENAME); // debug
            // File file = new File(filename); // export÷
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            doc = builder.parse(file);
            doc.getDocumentElement().normalize();
            isLoaded = true;
            System.out.println("DONE");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("FAILED\n" + e);
        }
    }

    public static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
    
    // General Preferences
    public String getApplicationName() {
        return getPreference("general", "app-name");
    }

    // Database Preferences
    public String getDatabaseURL() {
        return getPreference("database", "url");
    }
    
    public void setDatabaseURL(String newURL) {
        setPreference("database", "url", newURL);
    }
    
    public String getDatabaseHost() {
        return getPreference("database", "host");
    }

    public void setDatabaseHost(String newHost) {
        setPreference("database", "host", newHost);
    }
    
    public int getDatabasePort() {
        return Integer.parseInt(getPreference("database", "port"));
    }
    
    public void setDatabasePort(int newPort) {
        setPreference("database", "port", String.valueOf(newPort));
    }
    
    public String getDatabaseName() {
        return getPreference("database", "name");
    }
    
    public void setDatabaseName(String newDBName) {
        setPreference("database", "name", newDBName);
    }
    
    public String getDatabaseUser() {
        return getPreference("database", "user");
    }
    
    public void setDatabaseUser(String newUser) {
        setPreference("database", "user", newUser);
    }

    public String getDatabasePassword() {
        return getPreference("database", "password");
    }
    
    public void setDatabasePassword(String newPassword) {
        setPreference("database", "password", newPassword);
    }

    private String getPreference(String parentTag, String valueTag) {
        if (doc != null) {
            Node node = doc.getElementsByTagName(parentTag).item(0);
            if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                return e.getElementsByTagName(valueTag).item(0).getTextContent();
            }
        }
        return null;
    }
    
    private void setPreference(String parentTag, String valueTag, String value) {
        if (doc != null) {
            Node node = doc.getElementsByTagName(parentTag).item(0);
            if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                e.getElementsByTagName(valueTag).item(0).setTextContent(value);
            }
        }
    }
    
    public void saveXml() {
        System.out.print("Saving preferences xml...");
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(PREF_FILENAME));
            transformer.transform(source, result);
            System.out.println("DONE");
        } catch (TransformerException e) {
            System.err.println("FAILED: " + e);
        }
    }

}
