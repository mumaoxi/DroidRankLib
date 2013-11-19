package com.umeng.ad.app.utils;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class SAXReader {

	public Document read(InputStream inputStream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document dos = builder.parse(inputStream);
//			MLog.v("dos:"+dos);
			return (Document) dos;
		} catch (Exception e) {
//			MLog.e("sax read,exception:"+e.getMessage());
			e.printStackTrace();
		}

		return null;
	}
}
