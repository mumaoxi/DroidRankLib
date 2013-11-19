package com.umeng.ad.app.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class ElementUtils {

	static ElementUtils elementUtils;

	public static ElementUtils getInstance() {
		if (elementUtils == null) {
			elementUtils = new ElementUtils();
		}
		return elementUtils;
	}

	public Element element(Element element, String elementName) {
		try {
			NodeList elNodeList = element.getElementsByTagName(elementName);
			if (elNodeList != null && elNodeList.getLength() > 0) {
				return (Element) elNodeList.item(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Element> elements(Element element) {
		try {
			NodeList elNodeList = element.getChildNodes();
			if (elNodeList != null && elNodeList.getLength() > 0) {
				
				List<Element> elements = new ArrayList<Element>();
				for (int i = 0; i < elNodeList.getLength(); i++) {
					elements.add((Element) elNodeList.item(i));
				}
				return elements;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Element> elements(Element element, String elementName) {
		try {
			NodeList elNodeList = element.getElementsByTagName(elementName);
			if (elNodeList != null && elNodeList.getLength() > 0) {
				List<Element> elements = new ArrayList<Element>();
				for (int i = 0; i < elNodeList.getLength(); i++) {
					elements.add((Element) elNodeList.item(i));
				}
				return elements;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Iterator<Element> elementIterator(Element element, String elementName) {
		try {
			List<Element> elements = elements(element, elementName);
			if (elements != null) {
				return elements.iterator();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Element> selectNodes(Element e, String nodesURI) {
		try {
			String[] array = nodesURI.split("\\/");
			Element element = null;
			for (int i = 2; i < array.length - 1; i++) {
				if (element == null) {
					element = element(e, array[i]);
				} else {
					element = element(element, array[i]);
				}
			}
			if (element != null) {
			NodeList lists=	element
				.getElementsByTagName(array[array.length - 1]);
				List<Element> elements = new ArrayList<Element>();
				if (lists!=null&&lists.getLength()>0) {
					for (int i = 0; i < lists.getLength(); i++) {
						elements.add((Element)lists.item(i));
					}
					return elements;
				}
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return null;
	}

	public String attributeValue(Element element, String name) {
		return element.getAttribute(name);
	}

	public List<Attribute> attributes(Element element) {
		NamedNodeMap map = element.getAttributes();
		List<Attribute> attributes = new ArrayList<Attribute>();
		if (map != null) {
			for (int i = 0; i < map.getLength(); i++) {
				Attribute attribute = new Attribute();
				attribute.setName(map.item(i).getNodeName());
				attribute.setValue(map.item(i).getNodeValue());
				attributes.add(attribute);
			}
			return attributes;
		}
		return null;
	}

	public String elementText(Element element, String name) {
		try {
			String value = ((Element)element.getElementsByTagName(name).item(0)).getTextContent();
			if (value.startsWith("\\!\\[CDATA\\[")) {
				value = value.replaceFirst("![CDATA[", "");
				value = new StringBuffer(new StringBuffer(value).reverse().toString().replaceFirst("\\]", "")).reverse().toString();
			}
			return value;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// @Override
	// public Node appendChild(Node newChild) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node cloneNode(boolean deep) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public short compareDocumentPosition(Node other) throws DOMException {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	//
	// @Override
	// public NamedNodeMap getAttributes() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getBaseURI() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public NodeList getChildNodes() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Object getFeature(String feature, String version) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node getFirstChild() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node getLastChild() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getLocalName() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getNamespaceURI() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node getNextSibling() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getNodeName() {
	// return null;
	// }
	//
	// @Override
	// public short getNodeType() {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	//
	// @Override
	// public String getNodeValue() throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Document getOwnerDocument() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node getParentNode() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getPrefix() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node getPreviousSibling() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getTextContent() throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Object getUserData(String key) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public boolean hasAttributes() {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public boolean hasChildNodes() {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public Node insertBefore(Node newChild, Node refChild) throws
	// DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public boolean isDefaultNamespace(String namespaceURI) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public boolean isEqualNode(Node arg) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public boolean isSameNode(Node other) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public boolean isSupported(String feature, String version) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public String lookupNamespaceURI(String prefix) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String lookupPrefix(String namespaceURI) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void normalize() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public Node removeChild(Node oldChild) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node replaceChild(Node newChild, Node oldChild) throws
	// DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void setNodeValue(String nodeValue) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setPrefix(String prefix) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setTextContent(String textContent) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public Object setUserData(String key, Object data, UserDataHandler
	// handler) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getAttribute(String name) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getAttributeNS(String namespaceURI, String localName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Attr getAttributeNode(String name) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Attr getAttributeNodeNS(String namespaceURI, String localName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public NodeList getElementsByTagName(String name) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public NodeList getElementsByTagNameNS(String namespaceURI, String
	// localName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public TypeInfo getSchemaTypeInfo() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getTagName() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public boolean hasAttribute(String name) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public boolean hasAttributeNS(String namespaceURI, String localName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public void removeAttribute(String name) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void removeAttributeNS(String namespaceURI, String localName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void setAttribute(String name, String value) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setAttributeNS(String namespaceURI, String qualifiedName,
	// String value) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public Attr setAttributeNode(Attr newAttr) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void setIdAttribute(String name, boolean isId) throws DOMException
	// {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setIdAttributeNS(String namespaceURI, String localName,
	// boolean isId) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setIdAttributeNode(Attr idAttr, boolean isId)
	// throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }

}
