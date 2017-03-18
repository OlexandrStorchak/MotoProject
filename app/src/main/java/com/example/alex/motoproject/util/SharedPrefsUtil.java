package com.example.alex.motoproject.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.alex.motoproject.R;

public class SharedPrefsUtil {
    public static void saveToPrefs(Activity activity, String key, int value) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getFromPrefs(Activity activity, String key) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(key,
                activity.getResources().getInteger(R.integer.chat_location_limit_default));
    }
}
