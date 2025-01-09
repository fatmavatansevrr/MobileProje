package com.fatmavatansever.mobileproje.models;

import java.io.File;

public class VisionBoard {
    private File collageFile;

    public VisionBoard(File collageFile) {
        this.collageFile = collageFile;

    }

    public File getCollageFile() {
        return collageFile;
    }

}
