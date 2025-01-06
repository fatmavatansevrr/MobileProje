package com.fatmavatansever.mobileproje;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.List;
import java.util.Random;

public class CollageUtils {

    public static Bitmap createDynamicCollage(List<Bitmap> bitmaps, int width, int height) {
        Bitmap collageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(collageBitmap);
        Paint paint = new Paint();
        Random random = new Random();

        for (Bitmap bitmap : bitmaps) {
            // Randomize scale
            float scale = 0.5f + random.nextFloat() * 0.5f; // Scale between 50% and 100%
            int newWidth = (int) (bitmap.getWidth() * scale);
            int newHeight = (int) (bitmap.getHeight() * scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

            // Randomize position
            int xOffset = random.nextInt(width - newWidth); // Ensure the image stays within canvas bounds
            int yOffset = random.nextInt(height - newHeight);

            // Randomize rotation
            float rotation = random.nextInt(30) - 15; // Rotation between -15 and 15 degrees

            // Apply transformations
            Matrix matrix = new Matrix();
            matrix.postTranslate(xOffset, yOffset);
            matrix.postRotate(rotation, xOffset + newWidth / 2f, yOffset + newHeight / 2f);

            // Draw the bitmap with transformations
            canvas.drawBitmap(scaledBitmap, matrix, paint);
        }

        return collageBitmap;
    }
}
