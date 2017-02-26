package com.example.alex.motoproject;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiverComponent;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiverModule;
import com.example.alex.motoproject.firebase.CoreComponent;
import com.example.alex.motoproject.firebase.DaggerCoreComponent;
import com.example.alex.motoproject.firebase.FirebaseUtilsModule;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class App extends Application
        implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "log";
    private static CoreComponent coreComponent;
    private static NetworkStateReceiverComponent networkStateReceiverComponent;
    //    private static ChatPresenterComponent chatPresenterComponent;
    private boolean isMainActivityVisible = false;
    private boolean isLocationListenerServiceOn = false;
    private boolean isMainActivityDestroyed;

    public static CoreComponent getCoreComponent() {
        return coreComponent;
    }

//    public static ChatPresenterComponent getChatPresenterComponent(Context context,
//                                                                   ChatMVP.PresenterToView view) {
//        if (App.chatPresenterComponent == null) {
//            App app = (App) context.getApplicationContext();
//        }
//        return chatPresenterComponent;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Realm.init(getApplicationContext());
        RealmConfiguration configuration =
                new RealmConfiguration.Builder()
                        .name("main_database.realm")
                        .schemaVersion(1)
                        .build();
        Realm.setDefaultConfiguration(configuration);
        Log.d(TAG, "realmInit: well done");

        registerActivityLifecycleCallbacks(this);

        //Cache some data in gadget storage for offline usage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        coreComponent = buildCoreComponent();
    }

    public NetworkStateReceiverComponent plusNetworkStateReceiverComponent() {
        if (networkStateReceiverComponent == null) {
            networkStateReceiverComponent = coreComponent
                    .plusNetworkStateReceiverComponent(new NetworkStateReceiverModule());
        }
        return networkStateReceiverComponent;
    }

    public boolean isLocationListenerServiceOn() {
        return isLocationListenerServiceOn;
    }

    public void setLocationListenerServiceOn(boolean locationListenerServiceOn) {
        isLocationListenerServiceOn = locationListenerServiceOn;
    }

    public boolean isMainActivityVisible() {

        return isMainActivityVisible;
    }

    public boolean isMainActivityDestroyed() {
        return isMainActivityDestroyed;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof MainActivity) {
            isMainActivityVisible = true;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof MainActivity) {
            isMainActivityDestroyed = false;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof MainActivity) {
            isMainActivityVisible = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof MainActivity) {
            isMainActivityDestroyed = true;
        }
    }

    public CoreComponent buildCoreComponent() {
        return DaggerCoreComponent.builder()
                .firebaseUtilsModule(new FirebaseUtilsModule())
                .build();
    }

//    public ChatPresenterComponent buildChatPresenterComponent(ChatMVP.PresenterToView view) {
//        return DaggerChatPresenterComponent.builder()
//                .presenterModule(new PresenterModule(view))
//                .build();
//    }
}
