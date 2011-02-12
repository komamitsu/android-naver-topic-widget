package com.komamitsu.android.naverjapan.news;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import android.util.Log;

public class Utils {
  private static final int HTTP_GET_RETRY = 3;

  static public InputStream getInputStreamViaHttp(DefaultHttpClient client, String url) {
    HttpGet get = new HttpGet(url);
    client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(HTTP_GET_RETRY, false));
    HttpResponse response = null;
    InputStream is = null;
    try {
      response = client.execute(get);
      if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        is = response.getEntity().getContent();
      }
    } catch (ClientProtocolException e) {
      Log.e("com.komamitsu.android.naverjapan.news.Utils", "Failed to get InputStream from " + url, e);
    } catch (IOException e) {
      Log.e("com.komamitsu.android.naverjapan.news.Utils", "Failed to get InputStream from " + url, e);
    }
    
    return is;
  }
}
