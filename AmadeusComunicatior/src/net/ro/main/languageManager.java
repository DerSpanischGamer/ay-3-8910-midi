package net.ro.main;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class languageManager {
	
	private final preferences prefs;
	
	private static final String[] idiomas = {"english", "espanol"};
	
	private final HashMap<String, String> texts = new HashMap<String, String>();
	
	public languageManager(preferences p) {
		
		prefs = p;
		
		// Instantiate the Factory
	      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	      try {

	          // optional, but recommended
	          // process XML securely, avoid attacks like XML External Entities (XXE)
	          dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

	          // parse XML file
	          DocumentBuilder db = dbf.newDocumentBuilder();

	          Document doc = db.parse(FileUtils.toFile(this.getClass().getResource("/languages.xml")));

	          // optional, but recommended
	          // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	          doc.getDocumentElement().normalize();

	          // get <staff>
	          NodeList list = doc.getElementsByTagName("languages");

	          for (int temp = 0; temp < list.getLength(); temp++) {		// TODO : DO

	        	  Node node = list.item(temp);

	              if (node.getNodeType() != Node.ELEMENT_NODE)
	            	  continue;
	            	  	
	              NodeList lst = node.getChildNodes();
	            	 
	              for (int i = 0; i < lst.getLength(); i++) {
	            	if (lst.item(i).getNodeName() != idiomas[prefs.getLanguage()])
	            		continue;
	            		 
	            	NodeList cats = lst.item(i).getChildNodes(); // cats = categories :3
	            		 
	            	for (int j = 0; j < cats.getLength(); j++) {
	            		if (cats.item(j).getNodeType() != Node.ELEMENT_NODE)
	            			continue;
	            			 
	            		NodeList finalList = cats.item(j).getChildNodes();
	            		
	            		for (int k = 0; k < finalList.getLength(); k++) {
	            			if (finalList.item(k).getNodeType() != Node.ELEMENT_NODE)
	            				continue;
	            			
	            			texts.put(finalList.item(k).getNodeName(),finalList.item(k).getTextContent());
	            		}
	            	 }
	              }
	          }
	      } catch (ParserConfigurationException | SAXException | IOException e) {
	          e.printStackTrace();
	      }
	}
	
	public String getName(String key) { return texts.get(key); }
}