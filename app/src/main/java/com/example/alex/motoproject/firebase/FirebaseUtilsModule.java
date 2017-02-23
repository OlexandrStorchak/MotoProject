package com.example.alex.motoproject.firebase;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FirebaseUtilsModule {
    @Provides
    @NonNull
    @Singleton
    public FirebaseDatabaseHelper provideFirebaseDatabaseHelper() {
        return new FirebaseDatabaseHelper();
    }
}
