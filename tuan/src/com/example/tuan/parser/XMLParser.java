package com.example.tuan.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XMLParser {

	class XMLContentHandler extends DefaultHandler {
		List<Display> lists = new ArrayList<Display>();
		Display display;
		String tag = "";

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String string = new String(ch, start, length);
			if (tag.equals("wap_url")) {
				display.setWap_url(string);
			} else if (tag.equals("title")) {
				Log.i("hanlder before", string);
				String ns = string;
				int index = string.indexOf("£º");
				if (index != -1) {
					ns = new String(string.substring(0, index));
				} else {
					index = string.indexOf("£¡");
					if (index != -1) { // µÚ¶þ¸ö£¡
						index = string.substring(index).indexOf("£¡");
						if (index != -1) {
							ns = new String(string.substring(0, index));
						}
					}
				}
				Log.i("hanlder after", ns);
				display.setTitle(ns);
			} else if (tag.equals("small_image")) {
				display.setSmall_image_url(string);
			} else if (tag.equals("price")) {
				display.setPrice(string);
			} else if (tag.equals("rebate")) {
				display.setRebate(string);
			} else if (tag.equals("bought")) {
				display.setBought(string);
			} else if (tag.equals("small_image")) {
				display.setSmall_image_url(string);
			} else if (tag.equals("addr")) {
				display.setAddr(string);
			}

		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.equals("url")) {
				display = new Display();
			} else if (qName.equals("wap_url") || qName.equals("title")
					|| qName.equals("small_image") || qName.equals("price")
					|| qName.equals("rebate") || qName.equals("bought")
					|| qName.equals("small_image") || qName.equals("addr")) {
				tag = qName;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("url")) {
				lists.add(display);
			} else if (qName.equals("wap_url") || qName.equals("title")
					|| qName.equals("small_image") || qName.equals("price")
					|| qName.equals("rebate") || qName.equals("bought")
					|| qName.equals("small_image") || qName.equals("addr")) {
				tag = "";
			}

		}
	}

	public List<Display> parseXML(String content) {
		List<Display> lists = new ArrayList<Display>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			XMLContentHandler handler = new XMLContentHandler();
			InputStream is = new ByteArrayInputStream(content.getBytes());
			parser.parse(is, handler);
			is.close();
			lists = handler.lists;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lists;
	}

}
