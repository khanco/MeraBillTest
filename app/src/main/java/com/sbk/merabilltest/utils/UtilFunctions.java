package com.sbk.merabilltest.utils;

import android.content.Context;
import android.widget.Toast;

public class UtilFunctions {
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    public static void showToastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
