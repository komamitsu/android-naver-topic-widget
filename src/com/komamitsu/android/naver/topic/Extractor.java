package com.komamitsu.android.naver.topic;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class Extractor {
  private static final Pattern DATA_NA_RANK = Pattern.compile("^NA=.*?\\br:(\\d+)\\b.*?");
  private static final int MAX_RECORD = 18;

  public static Extractor getInstance() {
    return new Extractor();
  }

  private Extractor() {
  }

  public List<Topic> extract(InputStream is) throws ParseException {
    final List<Topic> result = new ArrayList<Topic>(MAX_RECORD * 2);

    final ContentHandler contentHandler = new ContentHandler() {
      private Topic news;
      private boolean isTitle;
      private boolean isTitleSpan;
      private boolean isDetail;
      private boolean isTime;

      @Override
      public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // System.out.println("startPrefixMapping: prefix=" + prefix + ", uri="
        // + uri);
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // Log.d(getClass().getName(), "endElement: uri=" + uri + ", localName="
        // + localName + ", qName=" + qName);
        if (news == null) {
          if (qName.equals("li")) {
            String datana = atts.getValue("data-na");
            if (datana != null) {
              Matcher m = DATA_NA_RANK.matcher(datana);
              if (m != null && m.find()) {
                String ranking = m.group(1);
                news = new Topic();
                news.setRank(Integer.valueOf(ranking));
              }
            }
          }
        } else {
          if (qName.equals("a") && atts != null) {
            String url = atts.getValue("href");
            news.setUrlOfLink(url);
          } else if (qName.equals("img") && atts != null) {
            String url = atts.getValue("src");
            news.setUrlOfImage(url);
          } else if (qName.equals("span") && atts != null) {
            String clazz = atts.getValue("class");

            if (clazz.equals("title")) {
              isTitleSpan = true;
            } else if (clazz.equals("description")) {
              isDetail = true;
            }
          } else if (qName.equals("em") && atts != null) {
            String clazz = atts.getValue("class");
            if (clazz.equals("time")) {
              isTime = true;
            }
          } else if (isTitleSpan && qName.equals("strong")) {
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
      public void processingInstruction(String target, String data) throws SAXException {
        // System.out.println("processingInstruction: target=" + target +
        // ", data=" + data);
      }

      @Override
      public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // TODO Auto-generated method stub

      }

      @Override
      public void endPrefixMapping(String prefix) throws SAXException {
        // System.out.println("endPrefixMapping: prefix=" + prefix);
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        // System.out.println("endElement: uri=" + uri + ", localName=" +
        // localName + ", qName=" + qName);
        if (news != null) {
          if (qName.equals("li")) {
            if (news.getTitle().length() > 0 && news.getDetail().length() > 0 && news.getTime().length() > 0) {
              result.add(news);
            }
            news = null;
          } else if (qName.equals("strong")) {
            if (isTitle)
              isTitle = false;
          } else if (qName.equals("span")) {
            if (isTitleSpan)
              isTitleSpan = false;
            if (isDetail)
              isDetail = false;
          } else if (qName.equals("em")) {
            if (isTime)
              isTime = false;
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
        // System.out.println("characters: ch=" + s + ", start=" + start +
        // ", length=" + length);

        if (news != null) {
          if (isTitle) {
            news.setTitle(s);
          } else if (isDetail) {
            news.setDetail(s);
          } else if (isTime) {
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
      parser.parse(new InputSource(new BufferedInputStream(is, 8192)));
    } catch (Exception e) {
      e.printStackTrace();
      String msg = "HTML Parse error";
      Log.e(getClass().getName(), msg, e);
      throw new ParseException(msg, e);
    }

    return result;
  }
}
