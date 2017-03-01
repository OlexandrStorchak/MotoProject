package com.example.alex.motoproject;

import android.support.annotation.NonNull;

import com.example.alex.motoproject.screenChat.ChatMvp;
import com.example.alex.motoproject.screenChat.ChatPresenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PresenterModule {
    private ChatMvp.PresenterToView view;

    public PresenterModule(ChatMvp.PresenterToView view) {
        this.view = view;
    }

    @Provides
    @NonNull
    @Singleton
    ChatPresenter provideChatPresenter() {
        return new ChatPresenter(view);
    }
}
