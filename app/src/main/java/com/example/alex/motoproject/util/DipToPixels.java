package com.example.alex.motoproject.util;

import android.content.res.Resources;
import android.util.TypedValue;

public class DipToPixels {
    public static int toPx(float dp) {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, Resources.getSystem().getDisplayMetrics()));
    }
}
