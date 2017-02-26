package com.example.alex.motoproject;

import android.support.annotation.NonNull;

import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkStateReceiverModule {
    @Provides
    @NonNull
    @NetworkStateReceiverScope
    public NetworkStateReceiver provideNetworkStateReceiver() {
        return new NetworkStateReceiver();
    }
}
