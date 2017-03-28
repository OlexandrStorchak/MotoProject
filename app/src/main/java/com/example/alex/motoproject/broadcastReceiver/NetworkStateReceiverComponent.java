package com.example.alex.motoproject.broadcastReceiver;

import com.example.alex.motoproject.service.LocationListenerService;

import dagger.Subcomponent;

@Subcomponent(modules = NetworkStateReceiverModule.class)
@NetworkStateReceiverScope
public interface NetworkStateReceiverComponent {
    void inject(LocationListenerService locationListenerService);
}
