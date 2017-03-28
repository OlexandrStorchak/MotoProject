package com.example.alex.motoproject;

import android.support.annotation.NonNull;

import com.example.alex.motoproject.screenChat.ChatMvp;
import com.example.alex.motoproject.screenChat.ChatPresenter;
import com.example.alex.motoproject.screenUsers.UsersMvp;
import com.example.alex.motoproject.screenUsers.UsersPresenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PresenterModule {
    private ChatMvp.PresenterToView mChatView;
    private UsersMvp.PresenterToView mUserView;

    public PresenterModule(ChatMvp.PresenterToView view) {
        mChatView = view;
    }
    public PresenterModule(UsersMvp.PresenterToView view) {
        mUserView = view;
    }

    @Provides
    @NonNull
    @Singleton
    ChatMvp.ViewToPresenter provideChatPresenter() {
        return new ChatPresenter(mChatView);
    }

    @Provides
    @NonNull
    @Singleton
    UsersMvp.ViewToPresenter provideUsersPresenter() {
        return new UsersPresenter(mUserView);
    }
}
