package com.test.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.GuiThreadExecutor;
import com.test.app.VKApplication;
import com.test.model.Chat;
import com.test.model.ChatLoader;
import com.test.ui.adapter.ChatAdapter;
import com.test.ui.util.avatar.CircleImageView;
import com.test.ui.util.images.ImageDownloader;
import com.vk.test.R;

import java.lang.ref.WeakReference;

public class ChatActivity extends AppCompatActivity {
    private static final ImageDownloader imageDownloader = VKApplication.getInstance().getImageDownloader();
    private static final String LOG_TAG = "Chat activity";
    private static final int FETCH_SCROLL_OFFSET = 10;
    public static final String USERS_COUNT_KEY = "users_count";
    public static final String CHAT_ID_KEY = "chat_id";
    public static final String AVATAR_KEY = "avatar";
    public static final String TITLE_KEY = "title";

    private RecyclerView chatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        long chatId = intent.getLongExtra(CHAT_ID_KEY, 0);
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        chatView.setLayoutManager(layoutManager);
        chatView.addOnScrollListener(new ScrollListener(chatId));
        initChat(chatId);
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

    @Override
    protected void onDestroy() {
        RecyclerView.Adapter adapter = chatView.getAdapter();
        if (adapter instanceof ChatAdapter) {
            ((ChatAdapter) adapter).destroy();
        }
        super.onDestroy();
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

    private void initChat(long chatId) {
        ChatLoader chatLoader = ChatLoader.getInstance();
        Optional<Chat> chat = chatLoader.getChat(chatId);
        if (chat.isPresent()) {
            chatView.setAdapter(new ChatAdapter(chat.get()));
            chatView.scrollToPosition(0);
        } else {
            Futures.addCallback(chatLoader.fetchChatHistory(chatId),
                    new ChatCallback(chatView), GuiThreadExecutor.getInstance());
        }
    }


    private static class ChatCallback implements FutureCallback<Chat> {
        private final WeakReference<RecyclerView> chatView;

        public ChatCallback(RecyclerView chatView) {
            this.chatView = new WeakReference<>(chatView);
        }

        @Override
        public void onSuccess(Chat result) {
            RecyclerView recyclerView = chatView.get();
            if (recyclerView != null) {
                recyclerView.setAdapter(new ChatAdapter(result));
                recyclerView.scrollToPosition(0);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e(LOG_TAG, "Chat request failure " + t.getMessage());
        }
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

    private static class ScrollListener extends RecyclerView.OnScrollListener {
        private final FetchCallback fetchCallback = new FetchCallback();
        private final ChatLoader chatLoader = ChatLoader.getInstance();
        private final long chatId;
        private boolean loading;

        public ScrollListener(long chatId) {
            this.chatId = chatId;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy < 0) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                for (Chat chat : chatLoader.getChat(chatId).asSet()) {
                    if (!loading && chat.canFetch()) {
                        if (visibleItemCount + pastVisibleItems + FETCH_SCROLL_OFFSET >= totalItemCount) {
                            setLoading(true);
                            Futures.addCallback(chatLoader.fetchChatHistory(chatId), fetchCallback);
                        }
                    }
                }
            }
        }

        private void setLoading(final boolean loading) {
            this.loading = loading;
        }

        private class FetchCallback implements FutureCallback<Chat> {
            @Override
            public void onSuccess(Chat result) {
                setLoading(false);
            }

            @Override
            public void onFailure(Throwable t) {
                setLoading(false);
            }
        }
    }
}
