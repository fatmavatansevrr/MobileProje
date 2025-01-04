package com.fatmavatansever.mobileproje;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;

public class CollageUtils {

    public static Bitmap createCollage(List<Bitmap> bitmaps, int columnCount, int width, int height) {
        int rowCount = (int) Math.ceil((double) bitmaps.size() / columnCount);

        Bitmap collageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(collageBitmap);
        Paint paint = new Paint();

        int cellWidth = width / columnCount;
        int cellHeight = height / rowCount;

        int x = 0, y = 0;

        for (int i = 0; i < bitmaps.size(); i++) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmaps.get(i), cellWidth, cellHeight, false);
            canvas.drawBitmap(scaledBitmap, x, y, paint);

            x += cellWidth;
            if ((i + 1) % columnCount == 0) {
                x = 0;
                y += cellHeight;
            }
        }

        return collageBitmap;
    }
}
