package com.example.alex.motoproject.app;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.firebase.CoreComponent;
import com.example.alex.motoproject.firebase.DaggerCoreComponent;
import com.example.alex.motoproject.firebase.FirebaseUtilsModule;
import com.example.alex.motoproject.screenMain.MainActivity;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;

public class App extends Application
        implements Application.ActivityLifecycleCallbacks {

    private static CoreComponent coreComponent;

    private boolean mMainActivityVisible;
    private boolean mLocationListenerServiceOn;

    private BroadcastReceiver mNetworkStateReceiver = new NetworkStateReceiver();

    public static CoreComponent getCoreComponent() {
        return coreComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        registerActivityLifecycleCallbacks(this);

        //Cache some data in the device storage for offline usage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        coreComponent = buildCoreComponent();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);
    }

    public void checkGpsState() {
        mNetworkStateReceiver.onReceive(this, null);
    }

    public boolean isLocationListenerServiceOn() {
        return mLocationListenerServiceOn;
    }

    public void setLocationListenerServiceOn(boolean locationListenerServiceOn) {
        this.mLocationListenerServiceOn = locationListenerServiceOn;
    }

    public boolean isMainActivityVisible() {
        return mMainActivityVisible;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof MainActivity) {
            mMainActivityVisible = true;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof MainActivity) {
            mMainActivityVisible = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public CoreComponent buildCoreComponent() {
        return DaggerCoreComponent.builder()
                .firebaseUtilsModule(new FirebaseUtilsModule())
                .build();
    }
}
