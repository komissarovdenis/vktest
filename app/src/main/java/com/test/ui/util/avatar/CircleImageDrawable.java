package com.test.ui.util.avatar;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

public class CircleImageDrawable extends Drawable {
    private static final float circlePadding = 4.0f;

    private final Paint paint = initPaint();
    private final ShaderSource shaderSource;

    public CircleImageDrawable(final Drawable otherDrawable) {
        super();
        this.shaderSource = ShaderSource.from(otherDrawable);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect myBounds = getBounds();
        if (myBounds.width() <= 0 || myBounds.height() <= 0) {
            myBounds.set(myBounds.left, myBounds.top,
                    myBounds.left + canvas.getWidth(),
                    myBounds.top + canvas.getWidth());
        }

        if (myBounds.width() > 0 && myBounds.height() > 0) {
            paint.setShader(shaderSource.getShader(myBounds.width(), myBounds.height()));
            int sideSize = Math.min(myBounds.width(), myBounds.height());
            int circleRadius = sideSize / 2;
            canvas.drawCircle(myBounds.exactCenterX(), myBounds.exactCenterY(),
                    circleRadius - circlePadding, paint);
        }
    }

    @Override
    public Drawable getCurrent() {
        return shaderSource.drawable;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        shaderSource.drawable.setBounds(bounds);
        super.onBoundsChange(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return paint.getAlpha() / 255;
    }


    private static Paint initPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        return paint;
    }


    private static abstract class ShaderProvider {
        private int canvasWidth, canvasHeight;
        private Bitmap cachedBitmap;
        private Shader cachedShader;

        protected abstract Bitmap createBitmap(int canvasWidth, int canvasHeight);

        public Shader getShader(int canvasWidth, int canvasHeight) {
            if (!isCacheValid(canvasWidth, canvasHeight)) {
                Bitmap updatedBitmap = updateBitmap(canvasWidth, canvasHeight);
                if (this.cachedBitmap != updatedBitmap) {
                    this.cachedBitmap = updatedBitmap;
                    this.cachedShader = createShader(this.cachedBitmap);
                }
                this.canvasWidth = canvasWidth;
                this.canvasHeight = canvasHeight;
            }
            return this.cachedShader;
        }

        private Bitmap updateBitmap(int canvasWidth, int canvasHeight) {
            if (isSizeChanged(canvasWidth, canvasHeight) || this.cachedBitmap == null) {
                return createBitmap(canvasWidth, canvasHeight);
            } else {
                return this.cachedBitmap;
            }
        }

        private boolean isCacheValid(int canvasWidth, int canvasHeight) {
            return !isSizeChanged(canvasWidth, canvasHeight) && this.cachedBitmap != null && this.cachedShader != null;
        }

        private boolean isSizeChanged(int canvasWidth, int canvasHeight) {
            return this.canvasWidth != canvasWidth &&
                    this.canvasHeight != canvasHeight;
        }

        private static Shader createShader(Bitmap image) {
            return new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }
    }


    protected static class ShaderSource extends ShaderProvider {
        public final Drawable drawable;

        public static ShaderSource from(Drawable source) {
            return new ShaderSource(source);
        }

        private ShaderSource(Drawable source) {
            super();
            this.drawable = source;
        }

        @Override
        protected Bitmap createBitmap(int canvasWidth, int canvasHeight) {
            return CircleImageDrawable.createBitmap(drawable, canvasWidth, canvasHeight);
        }
    }


    public static Bitmap createBitmap(Drawable drawable, int canvasWidth, int canvasHeight) {
        int canvasSide = Math.min(canvasWidth, canvasHeight);
        if (drawable instanceof BitmapDrawable) {
            Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();
            return ThumbnailUtils.extractThumbnail(originalBitmap, canvasSide, canvasSide, 0);
        } else {
            Bitmap bitmap = Bitmap.createBitmap(canvasSide, canvasSide, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }
}
