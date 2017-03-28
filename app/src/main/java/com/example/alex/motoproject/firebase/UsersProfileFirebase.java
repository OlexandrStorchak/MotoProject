package com.example.alex.motoproject.firebase;

import android.os.Parcel;
import android.os.Parcelable;

public class UsersProfileFirebase implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UsersProfileFirebase> CREATOR = new Parcelable.Creator<UsersProfileFirebase>() {
        @Override
        public UsersProfileFirebase createFromParcel(Parcel in) {
            return new UsersProfileFirebase(in);
        }

        @Override
        public UsersProfileFirebase[] newArray(int size) {
            return new UsersProfileFirebase[size];
        }
    };
    private String id;
    private String name;
    private String avatar;
    private String email;
    private String motorcycle;
    private String nickName;

    private UsersProfileFirebase(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatar = in.readString();
        email = in.readString();
        motorcycle = in.readString();
        nickName = in.readString();
    }

    public String getMotorcycle() {
        return motorcycle;
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

    public String getNickName() {
        return nickName;
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
    }
}

