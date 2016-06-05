package com.test.ui.util.images;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.List;

import static android.media.ThumbnailUtils.extractThumbnail;

public final class AvatarCollageUtils {
    private static final int gapSize = 1;

    private AvatarCollageUtils() {}

    public static Bitmap createBitmap(List<Bitmap> bitmaps, int width, int height) {
        switch (bitmaps.size()) {
            case 0:
                return null;
            case 1: {
                return bitmaps.get(0);
            }
            case 2: {
                Bitmap left = extractThumbnail(bitmaps.get(0), width / 2, height, 0);
                Bitmap right = extractThumbnail(bitmaps.get(1), width / 2, height, 0);
                return concatHorizontal(left, right, width, height);
            }
            case 3: {
                Bitmap left = extractThumbnail(bitmaps.get(0), width / 2, height, 0);
                Bitmap rightTop = extractThumbnail(bitmaps.get(1), width / 2, height / 2, 0);
                Bitmap rightBottom = extractThumbnail(bitmaps.get(2), width / 2, height / 2, 0);
                Bitmap right = concatVertical(rightTop, rightBottom, width / 2, height);
                return concatHorizontal(left, right, width, height);
            }
            case 4:
            default: {
                Bitmap leftTop = extractThumbnail(bitmaps.get(0), width / 2, height / 2, 0);
                Bitmap leftBottom = extractThumbnail(bitmaps.get(3), width / 2, height / 2, 0);
                Bitmap rightTop = extractThumbnail(bitmaps.get(1), width / 2, height / 2, 0);
                Bitmap rightBottom = extractThumbnail(bitmaps.get(2), width / 2, height / 2, 0);
                Bitmap left = concatVertical(leftTop, leftBottom, width / 2, height);
                Bitmap right = concatVertical(rightTop, rightBottom, width / 2, height);
                return concatHorizontal(left, right, width, height);
            }
        }
    }

    private static Bitmap concatVertical(Bitmap top, Bitmap bottom, int canvasWidth, int canvasHeight) {
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(top, 0, -gapSize, null);
        canvas.drawBitmap(bottom, 0, canvasWidth + gapSize, null);
        top.recycle();
        bottom.recycle();
        return bitmap;
    }

    private static Bitmap concatHorizontal(Bitmap left, Bitmap right, int canvasWidth, int canvasHeight) {
        int canvasSide = Math.min(canvasWidth, canvasHeight);
        Bitmap bitmap = Bitmap.createBitmap(canvasSide, canvasSide, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(left, -gapSize, 0, null);
        canvas.drawBitmap(right, left.getWidth() + gapSize, 0, null);
        left.recycle();
        right.recycle();
        return bitmap;
    }
}
