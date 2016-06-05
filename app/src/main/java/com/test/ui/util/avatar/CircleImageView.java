package com.test.ui.util.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends ImageView {
    private CircleImageDrawable drawable;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public CircleImageDrawable getDrawable() {
        return drawable;
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void setImageBitmap(Bitmap bitmap) {
        this.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            this.drawable = null;
        } else {
            this.drawable = new CircleImageDrawable(drawable);
        }
        super.setImageDrawable(this.drawable);
        this.requestLayout();
    }
}
