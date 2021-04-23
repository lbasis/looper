package com.looper;

import android.util.Log;

public class Logger {
    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
    }

    public static void e(String tag, Object obj) {
        if (debug) Log.e(tag, obj.toString());
    }

}
