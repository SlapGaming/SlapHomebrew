package me.naithantu.SlapHomebrew.Storage;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class XMLParser {
	
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	public static ArrayList<Integer> getThreads(){
		ArrayList<Integer> threadnummers = new ArrayList<>();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL("http://forums.slapgaming.com/external.php?type=xml&forumids=132").openStream());
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("thread");
			for (int xCount = 0; xCount < nList.getLength(); xCount++) {
				Node node = nList.item(xCount);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					threadnummers.add(Integer.parseInt(element.getAttribute("id")));
				}
			}
		} catch (Exception e) {
			threadnummers = null;
		}
		
		return threadnummers;
	}
	
	public static Object[] createApplyThread(int threadNr) {
		Object[] returnThread = null;
		try {
			String username = null;
			boolean complete = false;
			boolean afgehandeld = false;
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL("http://slapgaming.com/applicationthread/getThread.php?thread=" + threadNr).openStream());
			doc.getDocumentElement().normalize();
			Node mainPostNode = doc.getElementsByTagName("mainpost").item(0);
			if (mainPostNode.getNodeType() == Node.ELEMENT_NODE) {
				Element mainPost = (Element) mainPostNode;
				
				// Username Node
				Node usernameNode = mainPost.getElementsByTagName("username").item(0);
				if (usernameNode.getNodeType() == Node.ELEMENT_NODE) {
					Element usernameEl = (Element) usernameNode;
					username = usernameEl.getTextContent();
				}
				
				//Complete Node
				Node completeNode = mainPost.getElementsByTagName("complete").item(0);
				if (completeNode.getNodeType() == Node.ELEMENT_NODE) {
					Element completeEl = (Element) completeNode;
					complete = Boolean.parseBoolean(completeEl.getTextContent());
				}
				
			}
			
			//Afgehandeld Node
			Node afgehandeldNode = doc.getElementsByTagName("done").item(0);
			if (afgehandeldNode.getNodeType() == Node.ELEMENT_NODE) {
				Element afgehandeldEl = (Element) afgehandeldNode;
				afgehandeld = Boolean.parseBoolean(afgehandeldEl.getTextContent());
			}
			
			if (username != null) {
				returnThread = new Object[]{threadNr, username, complete, afgehandeld};
			}
		} catch (Exception e) {}
		return returnThread;
	}

}
