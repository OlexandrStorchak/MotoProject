package com.example.alex.motoproject;

import android.support.annotation.NonNull;

import com.example.alex.motoproject.screenChat.ChatMVP;
import com.example.alex.motoproject.screenChat.ChatPresenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PresenterModule {
    private ChatMVP.PresenterToView view;

    public PresenterModule(ChatMVP.PresenterToView view) {
        this.view = view;
    }

    @Provides
    @NonNull
    @Singleton
    ChatPresenter provideChatPresenter() {
        return new ChatPresenter(view);
    }
}
