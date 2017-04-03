package com.example.alex.motoproject.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;

public class DimensHelper {
    public static int dpToPx(float dp) {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, Resources.getSystem().getDisplayMetrics()));
    }

    public static void getScaledAvatar(String ref, int size, final AvatarRefReceiver receiver) {
        if (ref.contains(".googleusercontent.com/")) {
            receiver.onRefReady(ref.replace("/s96-c", "/s" + size + "-c")); //Google avatar
        } else if (ref.matches("[0-9]+")) {
            //This ref is Facebook user id, that will be used to download his avatar
            Bundle params = new Bundle();
            params.putBoolean("redirect", false);
            params.putInt("height", size);
            params.putInt("width", size);

            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + ref + "/picture",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {
                                receiver.onRefReady((String) response
                                        .getJSONObject().getJSONObject("data").get("url"));
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                                receiver.onError();
                            }
                        }
                    }
            ).executeAsync();
        } else {
            receiver.onRefReady(ref); //Avatar from Firebase, do not change anything
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
            return size.x;
        } else {
            display.getSize(size);
            return size.x;
        }
    }

    public interface AvatarRefReceiver {
        void onRefReady(String ref);

        void onError();
    }
}
