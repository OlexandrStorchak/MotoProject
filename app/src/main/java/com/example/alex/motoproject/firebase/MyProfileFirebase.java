package com.example.alex.motoproject.firebase;

import android.os.Parcel;
import android.os.Parcelable;

public class MyProfileFirebase implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyProfileFirebase> CREATOR = new Parcelable.Creator<MyProfileFirebase>() {
        @Override
        public MyProfileFirebase createFromParcel(Parcel in) {
            return new MyProfileFirebase(in);
        }

        @Override
        public MyProfileFirebase[] newArray(int size) {
            return new MyProfileFirebase[size];
        }
    };
    String name;
    String avatar;
    String email;
    String nickName;
    String motorcycle;
    String aboutMe;
    private String id;

    public MyProfileFirebase() {

    }

    private MyProfileFirebase(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatar = in.readString();
        email = in.readString();
        nickName = in.readString();
        motorcycle = in.readString();
        aboutMe = in.readString();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(String motorcycle) {
        this.motorcycle = motorcycle;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
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
        dest.writeString(nickName);
        dest.writeString(motorcycle);
        dest.writeString(aboutMe);
    }
}