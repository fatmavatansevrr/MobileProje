package com.fatmavatansever.mobileproje.models;

import java.io.Serializable;

public class SwipeCard implements Serializable {
    private String imageUrl;

    public SwipeCard( String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
