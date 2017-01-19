package com.example.alex.motoproject;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class App extends Application {
    private static final String TAG = "log";


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
        RealmConfiguration configuration =
                new RealmConfiguration.Builder()
                        .name("main_database.realm")
                        .schemaVersion(1)
                        .build();
        Realm.setDefaultConfiguration(configuration);
        Log.d(TAG, "realmInit: well done");
    }
}
