package com.fatmavatansever.mobileproje.models;

public class Goal {
    private int imageResId;
    private String goalName;

    public Goal(int imageResId, String goalName) {
        this.imageResId = imageResId;
        this.goalName = goalName;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getGoalName() {
        return goalName;
    }
}
