package com.example.alex.motoproject.broadcastReceiver;

import android.support.annotation.NonNull;

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
