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
		int count;
		Display display;
		String tag = "";
StringBuilder builder;
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String string = new String(ch, start, length);
			if (tag.equals("wap_url")) {
				display.setWap_url(string);
			} else if (tag.equals("title")) {
			//	Log.d("title",string);
				builder.append(string);
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
			}else if (tag.equals("gid")) {
				display.setGid(string);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.equals("url")) {
				display = new Display();
			} else if (qName.equals("wap_url") 
					|| qName.equals("small_image") || qName.equals("price")
					|| qName.equals("rebate") || qName.equals("bought")
					|| qName.equals("small_image") || qName.equals("addr")|| qName.equals("gid")) {
				tag = qName;
				
			}else if(qName.equals("title")){
				tag = qName;
				builder = new StringBuilder();
			}else if(qName.equals("urlset")){
				int index = attributes.getIndex("count");
				String string = attributes.getValue(index);
				try {
					count = Integer.parseInt(string);
				} catch (Exception e) {
					System.out.println(e);
				}
				
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("url")) {
				lists.add(display);
			} else if (qName.equals("wap_url") 
					|| qName.equals("small_image") || qName.equals("price")
					|| qName.equals("rebate") || qName.equals("bought")
					|| qName.equals("small_image") || qName.equals("addr")|| qName.equals("gid")) {
				tag = "";
			}else if(qName.equals("title")){
				//Log.d("endElement",builder.toString());
				String string = builder.toString();
				String ns = string;
				int index = string.indexOf("：");
				if (index != -1) {
					ns = new String(string.substring(0, index));
					//Log.d("endElement","1++"+ns);
					//去掉第一个!
					index = ns.indexOf("！");
					if(index!=-1){
						ns = ns.substring(index+1);
					}
					
				} else {
					index = string.indexOf("！");
				//	Log.d("endElement","1.5++ index: "+index+string);
					if (index != -1) { // 第二个！
						String temp = string.substring(index+1);
						int index2 = temp.indexOf("！");
					//	Log.d("endElement","2++"+index2+"");
						if (index2 != -1) {
							ns = temp.substring(0, index2);
					//		ns = new String(string.substring(0, index+index2+2));
					//		Log.d("endElement","3++"+index+"---"+ns);
						}
					}
				}
				
				display.setTitle(ns);
				tag = "";
			}

		}
	}
	
	/**
	 * 出错时为-1
	 * @param content
	 * @return
	 */
	public int getCount(String content){
		int count = -1;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			XMLContentHandler handler = new XMLContentHandler();
			InputStream is = new ByteArrayInputStream(content.getBytes());
			parser.parse(is, handler);
			is.close();
			count = handler.count;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
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
