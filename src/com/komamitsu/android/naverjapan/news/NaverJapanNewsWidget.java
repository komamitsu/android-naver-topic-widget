package com.komamitsu.android.naverjapan.news;

import com.komamitsu.android.naverjapan.news.R;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class NaverJapanNewsWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {
        @Override
        public void onStart(Intent intent, int startId) {
            RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget_word);
            updateViews.setTextViewText(R.id.news_title, "ベルリン国際音楽祭");
            updateViews.setTextViewText(R.id.news_detail, "ベルリン国際映画祭が開幕、日本からは堀北真希主演「白夜行」などが出品");
            updateViews.setTextViewText(R.id.news_time, "2時間前");

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
