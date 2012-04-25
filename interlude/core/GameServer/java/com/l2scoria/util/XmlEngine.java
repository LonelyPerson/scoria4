package com.l2scoria.util;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * 
 * @author ProGramMoS
 *
 */
public final class XmlEngine {
	private final ArrayList<Node> _nodes = new ArrayList<Node>();
	
	public XmlEngine(File file) {
		this(file.getPath());
	}
	
	public XmlEngine(String path) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		try {
			Document doc = factory.newDocumentBuilder().parse(path);
			
			for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling()) {
				if ("list".equalsIgnoreCase(n.getNodeName())) {
					for (Node d=n.getFirstChild(); d != null; d = d.getNextSibling()) {
						if("record".equalsIgnoreCase(d.getNodeName()))
							_nodes.add(d);
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Node[] getNodes() {
		return (Node[])_nodes.toArray();
	}
}
