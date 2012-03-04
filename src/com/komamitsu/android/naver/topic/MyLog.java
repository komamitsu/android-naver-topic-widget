package com.komamitsu.android.naver.topic;

import android.util.Log;

public class MyLog {
  public static void d(String tag, String msg) {
    if (Config.isDebug)
      Log.d(tag, msg);
  }
}
