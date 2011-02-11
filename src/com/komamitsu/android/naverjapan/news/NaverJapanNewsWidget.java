package com.komamitsu.android.naverjapan.news;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.komamitsu.android.naverjapan.news.R;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class NaverJapanNewsWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {
        private static final String NAVER_JAPAN_URL = "http://www.naver.jp/";
        private static final int HTTP_GET_RETRY = 3;
        
        @Override
        public void onStart(Intent intent, int startId) {
            RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget_word);
            
            final DefaultHttpClient client = new DefaultHttpClient();
            try {
              InputStream topPageContent = getInputStreamViaHttp(client, NAVER_JAPAN_URL);
              if (topPageContent != null) {
                NaverJapanNewsExtractor extractor = NaverJapanNewsExtractor.getInstance();
                try {
                  List<NaverJapanNews> newsList = extractor.extract(topPageContent);
                  NaverJapanNews news = newsList.get(0); // TODO
                  InputStream imageStream = getInputStreamViaHttp(client, news.getUrlOfImage());
                  if (imageStream != null) {
                    Bitmap b = BitmapFactory.decodeStream(imageStream);
                    updateViews.setImageViewBitmap(R.id.news_image, b);
                  }
                  updateViews.setTextViewText(R.id.news_title, news.getTitle());
                  updateViews.setTextViewText(R.id.news_detail, news.getDetail());
                  updateViews.setTextViewText(R.id.news_time, news.getTime());
                } catch (NaverJapanNewsParseException e) {
                  Log.e(getClass().getName(), "Failed to parse NAVER Japan top page (NaverJapanNewsParseException)", e);
                }
              }
            }
            finally {
              if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
              }
            }

            ComponentName thisWidget = new ComponentName(this, NaverJapanNewsWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }
        
        private InputStream getInputStreamViaHttp(DefaultHttpClient client, String url) {
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
              Log.e(getClass().getName(), "Failed to get InputStream from " + url, e);
            } catch (IOException e) {
              Log.e(getClass().getName(), "Failed to get InputStream from " + url, e);
            }
            
            return is;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
