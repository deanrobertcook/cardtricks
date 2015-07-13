package com.example.cardtricks;

import android.graphics.Bitmap;
import android.util.Log;

public class Utils {
    public static void logBitMapDetails(String tag, Bitmap bitmap) {
        Log.d(tag, "Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
    }
}
