package com.fatmavatansever.mobileproje.models;

import java.io.Serializable;

public class SwipeCard implements Serializable {
    private String imageUrl;
    private String tag;

    public SwipeCard(String imageUrl, String tag) {
        this.imageUrl = imageUrl;
        this.tag = tag;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
