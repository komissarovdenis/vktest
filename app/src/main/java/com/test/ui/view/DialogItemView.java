package com.test.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.GuiThreadExecutor;
import com.test.app.VKApplication;
import com.test.model.Message;
import com.test.ui.util.images.AvatarCollageUtils;
import com.test.ui.util.images.ImageDownloader;
import com.vk.test.R;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

public class DialogItemView extends LinearLayout {
    private static final ImageDownloader imageDownloader = VKApplication.getInstance().getImageDownloader();
    private static final String LAST_YEAR_DATE_FORMAT = "d MMM yyyy";
    private static final String SAME_YEAR_DATE_FORMAT = "d MMM";
    private static final String LOG_TAG = "Item view";

    private final ImageView avatarView;
    private final TextView titleView;
    private final TextView lastMessageView;
    private final TextView timeStampView;
    private final ImageCallback imageCallback;

    private int chatId;

    public DialogItemView(Context context) {
        this(context, null);
    }

    public DialogItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DialogItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context, R.layout.view_dialog_item, this);

        this.avatarView = (ImageView) findViewById(R.id.avatar_view);
        this.titleView  = (TextView) findViewById(R.id.title_view);
        this.lastMessageView  = (TextView) findViewById(R.id.last_message_view);
        this.timeStampView  = (TextView) findViewById(R.id.time_stamp_view);
        this.imageCallback = new ImageCallback(avatarView);
    }

    public void bind(Message message) {
        chatId = message.getChatId();
        titleView.setText(message.title);
        lastMessageView.setText(message.body);
        timeStampView.setText(formatTimeStamp(getContext(), message.date * 1000L));

        ImmutableSet<String> photoUris = AvatarCollageUtils.getAvatar(message);
        if (photoUris.isEmpty()) {
            avatarView.setImageDrawable(getPlaceholder(getContext()));
        } else {
            loadAvatar(photoUris);
        }
    }

    private void loadAvatar(ImmutableSet<String> photos) {
        imageCallback.bind(chatId);
        ListenableFuture<Bitmap> avatarFuture = imageDownloader.downloadImage(photos);
        Futures.addCallback(avatarFuture, imageCallback, GuiThreadExecutor.getInstance());
        if (!avatarFuture.isDone()) {
            avatarView.setImageDrawable(getPlaceholder(getContext()));
        }
    }


    private static Drawable getPlaceholder(Context context) {
        return context.getResources().getDrawable(R.drawable.avatar_placeholder);
    }

    private static CharSequence formatTimeStamp(Context context, long timeStamp) {
        if (DateUtils.isToday(timeStamp)) {
            return DateFormat.getTimeFormat(context).format(new Date(timeStamp));
        } else if (isYesterday(timeStamp)) {
            return context.getString(R.string.yesterday);
        } else if (isSameYear(timeStamp)) {
            return DateFormat.format(SAME_YEAR_DATE_FORMAT, new Date(timeStamp));
        } else {
            return DateFormat.format(LAST_YEAR_DATE_FORMAT, new Date(timeStamp));
        }
    }

    private static boolean isSameYear(long timeStamp) {
        Calendar stamp = Calendar.getInstance();
        stamp.setTimeInMillis(timeStamp);
        return Calendar.getInstance().get(Calendar.YEAR) == stamp.get(Calendar.YEAR);
    }

    private static boolean isYesterday(long timeStamp) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Calendar stamp = Calendar.getInstance();
        stamp.setTimeInMillis(timeStamp);
        return yesterday.get(Calendar.YEAR) == stamp.get(Calendar.YEAR)
                && yesterday.get(Calendar.MONTH) == stamp.get(Calendar.MONTH)
                && yesterday.get(Calendar.DAY_OF_MONTH) == stamp.get(Calendar.DAY_OF_MONTH);
    }


    private class ImageCallback implements FutureCallback<Bitmap> {
        private final WeakReference<ImageView> avatarView;
        private int chatId;

        public ImageCallback(ImageView avatarView) {
            this.avatarView = new WeakReference<>(avatarView);
        }

        public void bind(int chatId) {
            this.chatId = chatId;
        }

        @Override
        public void onSuccess(Bitmap result) {
            if (chatId == DialogItemView.this.chatId) {
                ImageView imageView = avatarView.get();
                if (imageView != null) {
                    imageView.setImageBitmap(result);
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e(LOG_TAG, "Download failure " + t.getMessage());
        }
    }
}
