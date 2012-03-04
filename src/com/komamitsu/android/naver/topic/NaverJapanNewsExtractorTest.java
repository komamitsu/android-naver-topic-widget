package com.komamitsu.android.naverjapan.news;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class NaverJapanNewsExtractorTest {

  @Test
  public final void testExtract() throws NaverJapanNewsParseException {
    NaverJapanNewsExtractor extractor = NaverJapanNewsExtractor.getInstance();
    String src =
      "<html><head></head><body>" +
      "<li data-na=\"NA=i:17608,r:1\">" +
      "<a href=\"http://search.naver.jp/search?q=%E5%A4%A7%E9%9B%AA&sm=top_trd&sme=1\" data-na=\"NL:topicword\">" +
      "<span class=\"numn1\">1</span>" +
      "<span class=\"thumb\">" +
      "<img src=\"http://tc2.search.naver.jp/?/jthumb/http%3A%2F%2Fwww.uonoprint.com%2Fhitorigoto2%2F2010%2F02%2F04%2Fyuki2.jpg/c.sptb.112x119\" width=\"112\" height=\"119\" alt=\"\" onerror=\"imgOnError(this,'image',112,119);\"/>" +
      "</span>" +
      "<span class=\"data\">" +
      "<span class=\"title\"><strong>大雪</strong> <span class=\"ranking r_keep\">Stay</span></span>" +
      "<span class=\"description\">2/12にかけ、全国の広い範囲で大雪。「各地ともさらに積雪量が増える見...</span>" +
      "<em class=\"time\">2時間前</em>" +
      "</span>" +
      "</a>" +
      "</li>" +
      "<!--NA=i:17588,r:5--><li data-na=\"NA=i:17588,r:5\">" +
      "<!--NL:topicword--><a href=\"http://search.naver.jp/search?q=%E3%83%A0%E3%83%90%E3%83%A9%E3%82%AF%E5%A4%A7%E7%B5%B1%E9%A0%98&sm=top_trd&sme=5\" data-na=\"NL:topicword\">" +
      "<span class=\"num n5\">5</span>" +
      "" +
      "<span class=\"thumb\"><img src=\"http://tc1.search.naver.jp/?/jthumb/http%3A%2F%2Fcdn1.beeffco.com%2Ffiles%2Fpoll-images%2Fnormal%2Fmuhammad-hosni-mubarak_1844.jpg/c.spt.112x119\" width=\"112\" height=\"119\" alt=\"\" onerror=\"imgOnError(this,'image',112,119);\"/></span>" +
      "<span class=\"data\">" +
      "<span class=\"title\"><strong>ムバラク大統領</strong> <span class=\"ranking r_up\">Rank up</span></span>" +
      "<span class=\"description\">エジプト大統領。「即時辞任せず、副大統領に権限移譲の意向表明」 複数...</span>" +
      "<em class=\"time\">51分前</em>" +
      "</span>" +
      "</a>" +
      "</li>" +
      "" +
      "<!--NA=i:17611,r:16--><li data-na=\"NA=i:17611,r:16\">" +
      "<!--NL:topicword--><a href=\"http://search.naver.jp/search?q=%E3%81%82%E3%81%97%E3%81%9F%E3%81%AE%E3%82%B8%E3%83%A7%E3%83%BC&sm=top_trd&sme=16\" data-na=\"NL:topicword\">" +
      "<span class=\"num n16\">16</span>" +
      "" +
      "<span class=\"thumb\"><img src=\"http://tc2.search.naver.jp/?/jthumb/http%3A%2F%2Fwww.sanspo.com%2Fgeino%2Fimages%2F100417%2Fgnj1004170506013-p1.jpg/c.sptb.112x119\" width=\"112\" height=\"119\" alt=\"\" onerror=\"imgOnError(this,'image',112,119);\"/></span>" +
      "<span class=\"data\">" +
      "<span class=\"title\"><strong>あしたのジョー</strong> <span class=\"ranking r_new\">New</span></span>" +
      "<span class=\"description\">NEWSのメンバー。「シワがリアル!」 ベルト型チョコ贈呈され、映画「...</span>" +
      "<em class=\"time\">19分前</em>" +
      "</span>" +
      "</a>" +
      "</li>" +
      "</body></html>";
    List<NaverJapanNews> actual = extractor.extract(new ByteArrayInputStream(src.getBytes()));
    assertEquals(3, actual.size());
    
    int i = 0;
    assertEquals(1, actual.get(i).getRank());
    assertEquals("大雪", actual.get(i).getTitle());
    assertEquals("2/12にかけ、全国の広い範囲で大雪。「各地ともさらに積雪量が増える見...", actual.get(i).getDetail());
    assertEquals("2時間前", actual.get(i).getTime());
    assertEquals("http://tc2.search.naver.jp/?/jthumb/http%3A%2F%2Fwww.uonoprint.com%2Fhitorigoto2%2F2010%2F02%2F04%2Fyuki2.jpg/c.sptb.112x119", actual.get(i).getUrlOfImage());
    assertEquals("http://search.naver.jp/search?q=%E5%A4%A7%E9%9B%AA&sm=top_trd&sme=1", actual.get(i).getUrlOfLink());
    
    i = 1;
    assertEquals(5, actual.get(i).getRank());
    assertEquals("ムバラク大統領", actual.get(i).getTitle());
    assertEquals("エジプト大統領。「即時辞任せず、副大統領に権限移譲の意向表明」 複数...", actual.get(i).getDetail());
    assertEquals("51分前", actual.get(i).getTime());
    assertEquals("http://tc1.search.naver.jp/?/jthumb/http%3A%2F%2Fcdn1.beeffco.com%2Ffiles%2Fpoll-images%2Fnormal%2Fmuhammad-hosni-mubarak_1844.jpg/c.spt.112x119", actual.get(i).getUrlOfImage());
    assertEquals("http://search.naver.jp/search?q=%E3%83%A0%E3%83%90%E3%83%A9%E3%82%AF%E5%A4%A7%E7%B5%B1%E9%A0%98&sm=top_trd&sme=5", actual.get(i).getUrlOfLink());
    
    i = 2;
    assertEquals(16, actual.get(i).getRank());
    assertEquals("あしたのジョー", actual.get(i).getTitle());
    assertEquals("NEWSのメンバー。「シワがリアル!」 ベルト型チョコ贈呈され、映画「...", actual.get(i).getDetail());
    assertEquals("19分前", actual.get(i).getTime());
    assertEquals("http://tc2.search.naver.jp/?/jthumb/http%3A%2F%2Fwww.sanspo.com%2Fgeino%2Fimages%2F100417%2Fgnj1004170506013-p1.jpg/c.sptb.112x119", actual.get(i).getUrlOfImage());
    assertEquals("http://search.naver.jp/search?q=%E3%81%82%E3%81%97%E3%81%9F%E3%81%AE%E3%82%B8%E3%83%A7%E3%83%BC&sm=top_trd&sme=16", actual.get(i).getUrlOfLink());
  }

}
