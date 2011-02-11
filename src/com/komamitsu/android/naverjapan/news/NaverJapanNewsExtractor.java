package com.komamitsu.android.naverjapan.news;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class NaverJapanNewsExtractor {
  private static final Pattern DATA_NA_RANK = Pattern.compile("^NA=.*?\\br:(\\d+)\\b.*?");
  private static final int MAX_RECORD = 18;
  
  public static NaverJapanNewsExtractor getInstance() {
    return new NaverJapanNewsExtractor();
  }

  private NaverJapanNewsExtractor() {}
  
  public List<NaverJapanNews> extract(InputStream is) throws NaverJapanNewsParseException {
    final List<NaverJapanNews> result = new ArrayList<NaverJapanNews>(MAX_RECORD * 2);
    
    final ContentHandler contentHandler = new ContentHandler() {
      private NaverJapanNews news;
      private boolean isTitle;
      private boolean isTitleSpan;
      private boolean isDetail;
      private boolean isTime;
      
      @Override
      public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // System.out.println("startPrefixMapping: prefix=" + prefix + ", uri=" + uri);
      }
      
      @Override
      public void startElement(String uri, String localName, String qName,
          Attributes atts) throws SAXException {
        if (news == null && qName.equals("li")) {
          String datana = atts.getValue("data-na");
          if (datana != null) {
            Matcher m = DATA_NA_RANK.matcher(datana);
            if (m != null && m.find()) {
              String ranking = m.group(1);
              news = new NaverJapanNews();
              news.setRank(Integer.valueOf(ranking));
            }
          }
        }
        else {
          if (qName.equals("a")) {
            String url = atts.getValue("href");
            news.setUrlOfLink(url);
          }
          else if (qName.equals("img")) {
            String url = atts.getValue("src");
            news.setUrlOfImage(url);
          }
          else if (qName.equals("span")) {
            String clazz = atts.getValue("class");
            
            if (clazz.equals("title")) {
              isTitleSpan = true;
            }
            else if (clazz.equals("description")) {
              isDetail = true;
            }
          }
          else if (qName.equals("em")) {
            String clazz = atts.getValue("class");
            if (clazz.equals("time")) {
              isTime = true;
            }
          }
          else if (isTitleSpan && qName.equals("strong")) {
            isTitle = true;
          }
        }
      }
      
      @Override
      public void startDocument() throws SAXException {
        // System.out.println("startDocument");
      }
      
      @Override
      public void skippedEntity(String name) throws SAXException {
        // System.out.println("skippedEntity: name=" + name);
      }
      
      @Override
      public void setDocumentLocator(Locator locator) {
        // System.out.println("setDocumentLocator: locator=" + locator);
      }
      
      @Override
      public void processingInstruction(String target, String data)
          throws SAXException {
        // System.out.println("processingInstruction: target=" + target + ", data=" + data);
      }
      
      @Override
      public void ignorableWhitespace(char[] ch, int start, int length)
          throws SAXException {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void endPrefixMapping(String prefix) throws SAXException {
        // System.out.println("endPrefixMapping: prefix=" + prefix);
      }
      
      @Override
      public void endElement(String uri, String localName, String qName)
          throws SAXException {
        // System.out.println("endElement: uri=" + uri + ", localName=" + localName + ", qName=" + qName);
        if (news != null) {
          if (qName.equals("li")) {
            result.add(news);
            news = null;
          }
          else if (qName.equals("strong")) {
            if (isTitle) isTitle = false;
          }
          else if (qName.equals("span")) {
            if (isTitleSpan) isTitleSpan = false;
            if (isDetail) isDetail = false;
            if (isTime) isTime = false;
          }
        }
      }
      
      @Override
      public void endDocument() throws SAXException {
        // System.out.println("endDocument");
      }
      
      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        // System.out.println("characters: ch=" + s + ", start=" + start + ", length=" + length);
        
        if (news != null) {
          if (isTitle) {
            news.setTitle(s);
          }
          else if (isDetail) {
            news.setDetail(s);
          }
          else if (isTime) {
            news.setTime(s);
          }
        }
      }
    };
    
    final Parser parser = new Parser();
    try {
      parser.setFeature(Parser.namespacesFeature, false);
      parser.setFeature(Parser.ignoreBogonsFeature, false);
      parser.setContentHandler(contentHandler);
      parser.parse(new InputSource(new BufferedInputStream(is)));
    } catch (Exception e) {
      e.printStackTrace();
      String msg = "HTML Parse error";
      Log.e(getClass().getName(), msg, e);
      throw new NaverJapanNewsParseException(msg, e);
    }
    
    return result;
  }
}
