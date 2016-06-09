package com.test.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.vk.test.R;

public class CustomLinearLayout extends LinearLayout {

    private View timeView;
    private View avatarView;
    private View contentView;
    private int avatarWidth;
    private int avatarHeight;

    public CustomLinearLayout(Context context) {
        super(context);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.avatarView = findViewById(R.id.avatar_view);
        this.contentView = findViewById(R.id.content_view);
        this.timeView = findViewById(R.id.time_stamp_view);
        this.avatarWidth = (int) getResources().getDimension(R.dimen.avatar_size_small);
        this.avatarHeight = (int) getResources().getDimension(R.dimen.avatar_size_small);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        final int contentRight = contentView.getRight();
        final int contentBottom = contentView.getBottom();
        timeView.layout(contentRight, contentBottom - timeView.getHeight(), r - l - getPaddingRight(), contentBottom);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

        avatarView.measure(MeasureSpec.makeMeasureSpec(avatarWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(avatarHeight, MeasureSpec.EXACTLY));
        timeView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        final int avatarAndTimeWidth = avatarView.getMeasuredWidth() + timeView.getMeasuredWidth();
        final int freeSpace = width - avatarAndTimeWidth;
        contentView.measure(MeasureSpec.makeMeasureSpec(freeSpace, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED);

        setMeasuredDimension(widthMeasureSpec, contentView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
    }
}
