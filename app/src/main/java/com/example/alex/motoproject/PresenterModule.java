package com.example.alex.motoproject;

import android.support.annotation.NonNull;

import com.example.alex.motoproject.screenChat.ChatMvp;
import com.example.alex.motoproject.screenChat.ChatPresenter;
import com.example.alex.motoproject.screenOnlineUsers.UsersMvp;
import com.example.alex.motoproject.screenOnlineUsers.UsersPresenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PresenterModule {
    private ChatMvp.PresenterToView chatView;
    private UsersMvp.PresenterToView usersView;

    public PresenterModule(ChatMvp.PresenterToView view) {
        chatView = view;
    }
    public PresenterModule(UsersMvp.PresenterToView view) {
        usersView = view;
    }

    @Provides
    @NonNull
    @Singleton
    ChatMvp.ViewToPresenter provideChatPresenter() {
        return new ChatPresenter(chatView);
    }

    @Provides
    @NonNull
    @Singleton
    UsersMvp.ViewToPresenter provideUsersPresenter() {
        return new UsersPresenter(usersView);
    }
}
