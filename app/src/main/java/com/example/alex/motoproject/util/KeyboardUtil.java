package com.example.alex.motoproject.util;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) return;

        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
