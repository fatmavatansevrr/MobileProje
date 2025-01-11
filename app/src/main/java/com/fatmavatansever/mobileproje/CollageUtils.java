package com.fatmavatansever.mobileproje;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CollageUtils {

    /**
     * Creates a dynamic collage with random scaling, rotation, and placement.
     *
     * @param bitmaps List of Bitmap images to include in the collage.
     * @param width   Width of the collage.
     * @param height  Height of the collage.
     * @return Generated collage as a Bitmap.
     */
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

    /**
     * Saves a Bitmap to a file in the application's internal storage.
     *
     * @param context Context to access the internal storage.
     * @param bitmap  Bitmap to save.
     * @return The absolute path of the saved file, or null if saving fails.
     */
    public static String saveBitmapToFile(Context context, Bitmap bitmap) {
        File storageDir = new File(context.getFilesDir(), "createdImages");
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return null; // Failed to create directory
        }

        String fileName = "collage_" + System.currentTimeMillis() + ".png";
        File file = new File(storageDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}