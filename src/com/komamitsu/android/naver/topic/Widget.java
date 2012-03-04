package com.komamitsu.android.naver.topic;

import java.io.InputStream;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
  private static final String TAG = Widget.class.getSimpleName();
  private static final int TOPIC_INTERVAL_SEC = 10;
  private static final int TOPIC_REFRESH_SEC = 900;
  private static final int REQUEST_CODE = 0;
  private static final String NAVER_JAPAN_URL = "http://www.naver.jp/";
  private static int newsIndex = 0;
  private static long lastUpdateTime = -1;
  private static List<Topic> newsList;
  private static PendingIntent service;
  private static BroadcastReceiver wakeupReceiver;
  private static BroadcastReceiver sleepReceiver;

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);
    Log.d(TAG, "NaverJapanNewsWidget.onEnabled() : this=" + this);

    // set screen on receiver
    wakeupReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Waking up!");
        setAlarm(context);
      }
    };
    context.getApplicationContext().registerReceiver(wakeupReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

    // set screen off receiver
    sleepReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Going to sleep...");
        cancelAlarm(context);
      }
    };
    context.getApplicationContext().registerReceiver(sleepReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    Log.d(TAG, "NaverJapanNewsWidget.onUpdate(): this=" + this);
    setAlarm(context);
  }

  public static Topic getNextNews() {
    boolean shouldDownLoadNews = newsList == null;
    long now = System.currentTimeMillis();
    if (now > lastUpdateTime + TOPIC_REFRESH_SEC * 1000) {
      Log.i(TAG, "getNextNews(): refresh newsList");
      lastUpdateTime = now;
      shouldDownLoadNews = true;
    }

    if (shouldDownLoadNews) {
      final DefaultHttpClient client = new DefaultHttpClient();
      try {
        InputStream topPageContent = Utils.getInputStreamViaHttp(client, NAVER_JAPAN_URL);
        if (topPageContent != null) {
          Extractor extractor = Extractor.getInstance();
          try {
            newsList = extractor.extract(topPageContent);
          } catch (ParseException e) {
            Log.e(TAG, "Failed to parse NAVER Japan top page (NaverJapanNewsParseException)", e);
          }
        }
      } finally {
        if (client != null && client.getConnectionManager() != null) {
          client.getConnectionManager().shutdown();
        }
      }
    }

    Topic news = newsList.get(newsIndex);
    newsIndex++;
    newsIndex %= newsList.size();

    return news;
  }

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    Log.d(TAG, "NaverJapanNewsWidget.onDelete(): this=" + this);
    cancelAlarm(context);
    context.stopService(new Intent(context, UpdateService.class));

    if (wakeupReceiver != null)
      context.unregisterReceiver(wakeupReceiver);

    if (sleepReceiver != null)
      context.unregisterReceiver(sleepReceiver);

    super.onDeleted(context, appWidgetIds);
  }

  private void setAlarm(Context context) {
    Log.d(TAG, "NaverJapanNewsWidget.setAlarm() this=" + this);
    final Intent intent = new Intent(context, UpdateService.class);
    if (service == null) {
      service = PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
    long firstTime = SystemClock.elapsedRealtime();
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    long interval = TOPIC_INTERVAL_SEC * 1000;
    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, interval, service);
  }

  private void cancelAlarm(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.cancel(service);
  }

  public static class UpdateService extends Service {
    private static WeakHashMap<String, Bitmap> imageCache = new WeakHashMap<String, Bitmap>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget_word);
      Topic news = getNextNews();
      // Log.i(TAG, "Next news: " + news);

      final DefaultHttpClient client = new DefaultHttpClient();
      try {
        if (news.getImage() == null) {
          Bitmap b = null;
          String urlOfImage = news.getUrlOfImage();
          // debug
          for (String key : imageCache.keySet())
            Log.d(TAG, "#### " + key);
          if (imageCache.containsKey(urlOfImage)) {
            b = imageCache.get(urlOfImage);
            Log.d(TAG, "Found a image in cache: " + urlOfImage);
          } else {
            InputStream imageStream = Utils.getInputStreamViaHttp(client, urlOfImage);
            if (imageStream != null) {
              b = BitmapFactory.decodeStream(imageStream);
              Log.d(TAG, "Downloaded the news image: " + urlOfImage);
              imageCache.put(urlOfImage, b);
            }
          }
          news.setImage(b);
        }
        updateViews.setImageViewBitmap(R.id.news_image, news.getImage());
        String title = news.getRank() + "位 :  " + news.getTitle();
        updateViews.setTextViewText(R.id.news_title, title);
        updateViews.setTextViewText(R.id.news_detail, news.getDetail());
        updateViews.setTextViewText(R.id.news_time, news.getTime());
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrlOfLink()));
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, webIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
      } finally {
        if (client != null && client.getConnectionManager() != null) {
          client.getConnectionManager().shutdown();
        }
      }

      ComponentName thisWidget = new ComponentName(this, Widget.class);
      AppWidgetManager manager = AppWidgetManager.getInstance(this);
      manager.updateAppWidget(thisWidget, updateViews);
      return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
  }
}
