package com.fatmavatansever.mobileproje.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class VisionBoard implements Parcelable {
    private File collageFile;

    public VisionBoard(File collageFile) {
        this.collageFile = collageFile;
    }

    protected VisionBoard(Parcel in) {
        collageFile = new File(in.readString());
    }

    public static final Creator<VisionBoard> CREATOR = new Creator<VisionBoard>() {
        @Override
        public VisionBoard createFromParcel(Parcel in) {
            return new VisionBoard(in);
        }

        @Override
        public VisionBoard[] newArray(int size) {
            return new VisionBoard[size];
        }
    };

    public File getCollageFile() {
        return collageFile;
    }

    public void setCollageFile(File collageFile) {
        this.collageFile = collageFile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(collageFile.getAbsolutePath());
    }
}
