package com.example.alex.motoproject.firebase;

import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiverComponent;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiverModule;
import com.example.alex.motoproject.mainActivity.AlertControl;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.screenChat.ChatModel;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenOnlineUsers.ScreenOnlineUsersFragment;
import com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment;
import com.example.alex.motoproject.service.LocationListenerService;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = FirebaseUtilsModule.class)
@Singleton
public interface CoreComponent {
    void inject(ScreenMapFragment screenMapFragment);

    void inject(ChatModel chatModel);

    void inject(ScreenOnlineUsersFragment onlineUsersFragment);

    void inject(MainActivity mainActivity);

    void inject(ScreenMyProfileFragment screenMyProfileFragment);

    void inject(LocationListenerService locationListenerService);

    void inject(AlertControl alertControl);

    NetworkStateReceiverComponent plusNetworkStateReceiverComponent(
            NetworkStateReceiverModule networkStateReceiverModule);
}
