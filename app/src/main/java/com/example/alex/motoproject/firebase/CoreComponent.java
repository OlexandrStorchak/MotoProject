package com.example.alex.motoproject.firebase;

import com.example.alex.motoproject.locationService.LocationService;
import com.example.alex.motoproject.mainService.MainService;
import com.example.alex.motoproject.screenChat.ChatModel;
import com.example.alex.motoproject.screenMain.AlertControl;
import com.example.alex.motoproject.screenMain.MainActivity;
import com.example.alex.motoproject.screenMap.MapFragment;
import com.example.alex.motoproject.screenProfile.MyProfileFragment;
import com.example.alex.motoproject.screenProfile.UserProfileFragment;
import com.example.alex.motoproject.screenUsers.UsersModel;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = FirebaseUtilsModule.class)
@Singleton
public interface CoreComponent {
    void inject(MapFragment mapFragment);

    void inject(ChatModel chatModel);

    void inject(UsersModel usersModel);

    void inject(UserProfileFragment userProfileFragment);

    void inject(FirebaseLoginController firebaseLoginController);

    void inject(MainActivity mainActivity);

    void inject(MyProfileFragment myProfileFragment);

    void inject(LocationService locationService);

    void inject(AlertControl alertControl);

    void inject(MainService mainService);
}
