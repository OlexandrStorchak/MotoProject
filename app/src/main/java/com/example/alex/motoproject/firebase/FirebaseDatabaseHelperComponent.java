package com.example.alex.motoproject.firebase;

import com.example.alex.motoproject.screenChat.ChatModel;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = FirebaseUtilsModule.class)
@Singleton
public interface FirebaseDatabaseHelperComponent {
    void inject(ScreenMapFragment screenMapFragment);

    void inject(ChatModel chatModel);

    void inject(OnlineUsersFragment onlineUsersFragment);
}
