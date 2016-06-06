package com.test.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.GuiThreadExecutor;
import com.test.app.VKApplication;
import com.test.ui.util.avatar.CircleImageView;
import com.test.ui.util.images.ImageDownloader;
import com.vk.test.R;

import java.lang.ref.WeakReference;

public class ChatActivity extends AppCompatActivity {
    private static final ImageDownloader imageDownloader = VKApplication.getInstance().getImageDownloader();
    private static final String LOG_TAG = "Chat activity";
    public static final String USERS_COUNT_KEY = "users_count";
    public static final String CHAT_ID_KEY = "chat_id";
    public static final String AVATAR_KEY = "avatar";
    public static final String TITLE_KEY = "title";

    private long chatId;
    private RecyclerView chatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        chatId = intent.getLongExtra(CHAT_ID_KEY, 0);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            int users = intent.getIntExtra(USERS_COUNT_KEY, 0);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(intent.getStringExtra(TITLE_KEY));
            actionBar.setSubtitle(getResources().getQuantityString(R.plurals.users, users, users));
            initAvatar(actionBar, (ImmutableSet<String>) intent.getSerializableExtra(AVATAR_KEY));
        }
        setContentView(R.layout.activity_chat);
        chatView = (RecyclerView) findViewById(R.id.chat_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initAvatar(ActionBar actionBar, ImmutableSet<String> photos) {
        int paddingVertical = (int) getResources().getDimension(R.dimen.action_bar_avatar_padding_vertical);
        int paddingHorizontal = (int) getResources().getDimension(R.dimen.action_bar_avatar_padding_horizontal);
        final TypedArray attrs = getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        int actionBarSize = (int) attrs.getDimension(0, 0) - paddingVertical;
        attrs.recycle();
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView imageView = new CircleImageView(actionBar.getThemedContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                actionBarSize, actionBarSize, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        imageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(imageView);
        Toolbar parent = (Toolbar) imageView.getParent();
        parent.setContentInsetsAbsolute(paddingHorizontal, paddingHorizontal);

        ListenableFuture<Bitmap> avatarFuture = imageDownloader.downloadImage(photos);
        Futures.addCallback(avatarFuture, new AvatarCallback(imageView), GuiThreadExecutor.getInstance());
    }


    private class AvatarCallback implements FutureCallback<Bitmap> {
        private final WeakReference<ImageView> avatarView;

        public AvatarCallback(ImageView avatarView) {
            this.avatarView = new WeakReference<>(avatarView);
        }

        @Override
        public void onSuccess(Bitmap result) {
            ImageView imageView = avatarView.get();
            if (imageView != null) {
                imageView.setImageBitmap(result);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e(LOG_TAG, "Download failure " + t.getMessage());
        }
    }
}
