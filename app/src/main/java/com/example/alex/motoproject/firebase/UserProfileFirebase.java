package com.example.alex.motoproject.firebase;

import android.os.Parcel;
import android.os.Parcelable;

public class UserProfileFirebase implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserProfileFirebase> CREATOR = new Parcelable.Creator<UserProfileFirebase>() {
        @Override
        public UserProfileFirebase createFromParcel(Parcel in) {
            return new UserProfileFirebase(in);
        }

        @Override
        public UserProfileFirebase[] newArray(int size) {
            return new UserProfileFirebase[size];
        }
    };
    private String id;
    private String name;
    private String avatar;
    private String email;
    private String motorcycle;
    private String nickName;
    private String aboutMe;

    public UserProfileFirebase() {

    }

    private UserProfileFirebase(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatar = in.readString();
        email = in.readString();
        motorcycle = in.readString();
        nickName = in.readString();
        aboutMe = in.readString();
    }

    public String getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(String motorcycle) {
        this.motorcycle = motorcycle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(email);
        dest.writeString(motorcycle);
        dest.writeString(nickName);
        dest.writeString(aboutMe);
    }
}

