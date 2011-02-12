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

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class NaverJapanNewsWidget extends AppWidgetProvider {
    private static final int TOPIC_INTERVAL_SEC = 50;
    private static final String NAVER_JAPAN_URL = "http://www.naver.jp/";
    private static final String ACTION_NEWS_CHANGE = "com.komamitsu.android.naverjapan.news.NaverJapanNewsWidget";
    private static int newsIndex = 0;
    private static List<NaverJapanNews> newsList;
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        setAlarm(context);
    }
    
    public static NaverJapanNews getNextNews() {
      if (newsList == null) {
        final DefaultHttpClient client = new DefaultHttpClient();
        try {
          InputStream topPageContent = Utils.getInputStreamViaHttp(client, NAVER_JAPAN_URL);
          if (topPageContent != null) {
            NaverJapanNewsExtractor extractor = NaverJapanNewsExtractor.getInstance();
            try {
              newsList = extractor.extract(topPageContent);
            } catch (NaverJapanNewsParseException e) {
              Log.e("com.komamitsu.android.naverjapan.news.NaverJapanNewsWidget", "Failed to parse NAVER Japan top page (NaverJapanNewsParseException)", e);
            }
          }
        }
        finally {
          if (client != null && client.getConnectionManager() != null) {
            client.getConnectionManager().shutdown();
          }
        }
      }
      
      NaverJapanNews news = newsList.get(newsIndex);
      newsIndex++;
      newsIndex %= newsList.size();
      
      return news;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.i(getClass().getName(), "onReceive");
      super.onReceive(context, intent);
      context.startService(new Intent(context, UpdateService.class));
      setAlarm(context);
    }

    private void setAlarm(Context context) {
      Intent alarmIntent = new Intent(context, NaverJapanNewsWidget.class);
      alarmIntent.setAction(ACTION_NEWS_CHANGE);
      PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
      AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
      long now = System.currentTimeMillis();
      am.set(AlarmManager.RTC, now + TOPIC_INTERVAL_SEC * 1000, operation);
      /*
      // We want the alarm to go off 30 seconds from now.
      long firstTime = SystemClock.elapsedRealtime();

      // Schedule the alarm!
      AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
      am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                      firstTime, 30*1000, operation);
      */

      Log.i(getClass().getName(), "Alarm was set");
    }

    public static class UpdateService extends Service {
        @Override
        public void onStart(Intent intent, int startId) {
            Log.i(getClass().getName(), "Updating NAVER Japan news");
            
            RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget_word);
            NaverJapanNews news = getNextNews();
            
            final DefaultHttpClient client = new DefaultHttpClient();
            try {
                InputStream imageStream = Utils.getInputStreamViaHttp(client, news.getUrlOfImage());
                if (imageStream != null) {
                  Bitmap b = BitmapFactory.decodeStream(imageStream);
                  updateViews.setImageViewBitmap(R.id.news_image, b);
                }
                updateViews.setTextViewText(R.id.news_title, news.getTitle());
                updateViews.setTextViewText(R.id.news_detail, news.getDetail());
                updateViews.setTextViewText(R.id.news_time, news.getTime());
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
        
        
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
