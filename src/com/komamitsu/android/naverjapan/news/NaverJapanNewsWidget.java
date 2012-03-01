package com.komamitsu.android.naverjapan.news;

import java.io.InputStream;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.http.impl.client.DefaultHttpClient;

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
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class NaverJapanNewsWidget extends AppWidgetProvider {
  private static final String TAG = NaverJapanNewsWidget.class.getSimpleName();
  private static final int TOPIC_INTERVAL_SEC = 8;
  private static final int TOPIC_REFRESH_SEC = 600;
  private static final String NAVER_JAPAN_URL = "http://www.naver.jp/";
  private static final String ACTION_NEWS_CHANGE = NaverJapanNewsWidget.class.getName() + ".CHANGE";
  private static int newsIndex = 0;
  private static long lastUpdateTime = -1;
  private static List<NaverJapanNews> newsList;
  private static boolean running;
  private AlarmManager am;

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    Log.d(TAG, "NaverJapanNewsWidget.onUpdate");
    running = true;
    setAlarm(context);
  }

  public static NaverJapanNews getNextNews() {
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
          NaverJapanNewsExtractor extractor = NaverJapanNewsExtractor.getInstance();
          try {
            newsList = extractor.extract(topPageContent);
          } catch (NaverJapanNewsParseException e) {
            Log.e(TAG, "Failed to parse NAVER Japan top page (NaverJapanNewsParseException)", e);
          }
        }
      } finally {
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
    super.onReceive(context, intent);
    if (intent.getAction().equals(ACTION_NEWS_CHANGE)) {
      context.startService(new Intent(context, UpdateService.class));
    }
  }

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    super.onDeleted(context, appWidgetIds);
    if (am != null) {
      PendingIntent pendingIntent = getActionNewsChangeIntent(context);
      am.cancel(pendingIntent);
      Log.i(TAG, "Canceled AlarmManager");
    }
    Log.i(TAG, "onDeleted");
  }

  @Override
  public void onDisabled(Context context) {
    super.onDisabled(context);
    Log.i(TAG, "onDisabled");
  }

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);
    Log.i(TAG, "onEnabled");
  }

  private void setAlarm(Context context) {
    PendingIntent pendingIntent = getActionNewsChangeIntent(context);
    long firstTime = SystemClock.elapsedRealtime();
    am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, TOPIC_INTERVAL_SEC * 1000, pendingIntent);
  }

  private PendingIntent getActionNewsChangeIntent(Context context) {
    Intent alarmIntent = new Intent(context, NaverJapanNewsWidget.class);
    alarmIntent.setAction(ACTION_NEWS_CHANGE);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
    return pendingIntent;
  }

  public static class UpdateService extends Service {
    @Override
    public void onStart(Intent intent, int startId) {
      RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget_word);
      NaverJapanNews news = getNextNews();
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
