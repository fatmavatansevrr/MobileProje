package com.fatmavatansever.mobileproje.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SwipeCard implements Parcelable {
    private String imageUrl;
    private String tag;

    // Constructor
    public SwipeCard(String imageUrl, String tag) {
        this.imageUrl = imageUrl;
        this.tag = tag;
    }

    // Getter and Setter methods
    public String getImageUrl() {
        return imageUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // Parcelable implementation
    protected SwipeCard(Parcel in) {
        imageUrl = in.readString();
        tag = in.readString();
    }

    public static final Creator<SwipeCard> CREATOR = new Creator<SwipeCard>() {
        @Override
        public SwipeCard createFromParcel(Parcel in) {
            return new SwipeCard(in);
        }

        @Override
        public SwipeCard[] newArray(int size) {
            return new SwipeCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(tag);
    }
}
