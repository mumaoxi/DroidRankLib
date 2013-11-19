package com.umeng.ad.app.utils;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DocumentUtils {

	static DocumentUtils documentUtils;

	public static DocumentUtils getInstance() {
		if (documentUtils == null) {
			documentUtils = new DocumentUtils();
		}
		return documentUtils;
	}

	public Element getRootElement(Document document) {
		Element element = (Element) document.getDocumentElement();
		return element;
	}

	public List<Element> selectNodes(Document document, String uri) {
		try {
			Element element = getRootElement(document);
			return ElementUtils.getInstance().selectNodes(element, uri);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String selectSingleNode(Document document,String uri) {
		try {
			if (!uri.contains("@")) {
				return selectNodes(document,uri).get(0).getNodeValue();
			}
			String attributeName = uri.split("@")[1];
			List<Element> elements = selectNodes(document,uri.replaceFirst(
					"\\/@.{1,100}", ""));
			return elements.get(0).getAttribute(attributeName);
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
	// // TODO Auto-generated method stub
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
	// public org.w3c.dom.Document getOwnerDocument() {
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
	// public Node adoptNode(Node source) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Attr createAttribute(String name) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Attr createAttributeNS(String namespaceURI, String qualifiedName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public CDATASection createCDATASection(String data) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Comment createComment(String data) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public DocumentFragment createDocumentFragment() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Element createElement(String tagName) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Element createElementNS(String namespaceURI, String qualifiedName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public EntityReference createEntityReference(String name)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public ProcessingInstruction createProcessingInstruction(String target,
	// String data) throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Text createTextNode(String data) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public DocumentType getDoctype() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Element getDocumentElement() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getDocumentURI() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public DOMConfiguration getDomConfig() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Element getElementById(String elementId) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public NodeList getElementsByTagName(String tagname) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public NodeList getElementsByTagNameNS(String namespaceURI, String
	// localName) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public DOMImplementation getImplementation() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public String getInputEncoding() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public boolean getStrictErrorChecking() {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public String getXmlEncoding() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public boolean getXmlStandalone() {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public String getXmlVersion() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Node importNode(Node importedNode, boolean deep) throws
	// DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void normalizeDocument() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public Node renameNode(Node n, String namespaceURI, String qualifiedName)
	// throws DOMException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void setDocumentURI(String documentURI) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setStrictErrorChecking(boolean strictErrorChecking) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void setXmlVersion(String xmlVersion) throws DOMException {
	// // TODO Auto-generated method stub
	//
	// }

}
