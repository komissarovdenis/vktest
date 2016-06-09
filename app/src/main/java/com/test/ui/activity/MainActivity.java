package com.test.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.GuiThreadExecutor;
import com.test.model.DialogsLoader;
import com.test.model.Message;
import com.test.model.UsersLoader;
import com.test.ui.adapter.DialogsAdapter;
import com.test.ui.util.images.AvatarUtils;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.test.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = "VK Dialogs";
    private static final String SCOPE = VKScope.MESSAGES;
    private static final RequestUsers requestUsers = new RequestUsers();
    private static final GetUserIds getUserIds = new GetUserIds();

    private final RequestCallback requestCallback = new RequestCallback();
    private final CollectDialogs collectDialogs = new CollectDialogs();
    private ImmutableList<Message> dialogs = ImmutableList.of();

    private ListView dialogsList;
    private ProgressBar loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialogsList = (ListView) findViewById(R.id.dialogs_list);
        loadingView = (ProgressBar) findViewById(R.id.loading_view);
        dialogsList.setOnItemClickListener(this);

        if (VKSdk.isLoggedIn()) {
            requestDialogs();
        } else {
            VKSdk.login(this, SCOPE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                requestDialogs();
            }

            @Override
            public void onError(VKError error) {
                Log.e(LOG_TAG, "Auth error: " + error);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void requestDialogs() {
        ListenableFuture<SparseArray<VKApiUserFull>> requestData = Futures.dereference(
                Futures.transform(
                        Futures.transform(DialogsLoader.getInstance().getDialogs(), collectDialogs),
                        requestUsers
                )
        );
        Futures.addCallback(requestData, requestCallback, GuiThreadExecutor.getInstance());
        updateProgress(true);
    }

    private void updateProgress(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        dialogsList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Message message = (Message) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.CHAT_ID_KEY, id);
        intent.putExtra(ChatActivity.TITLE_KEY, message.title);
        intent.putExtra(ChatActivity.USERS_COUNT_KEY, message.getUsersCount());
        intent.putExtra(ChatActivity.AVATAR_KEY, AvatarUtils.getAvatar(message));
        startActivity(intent);
    }


    private class RequestCallback implements FutureCallback<SparseArray<VKApiUserFull>> {
        @Override
        public void onSuccess(SparseArray<VKApiUserFull> result) {
            Log.d(LOG_TAG, "Users loaded: " + result.size());
            dialogsList.setAdapter(new DialogsAdapter(dialogs));
            updateProgress(false);
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e(LOG_TAG, "Failure on get dialog request: " + t.getMessage());
            updateProgress(false);
        }
    }

    private class CollectDialogs implements Function<List<Message>, List<Message>> {
        @Override
        public List<Message> apply(List<Message> input) {
            Log.d(LOG_TAG, "Dialogs loaded: " + input.size());
            dialogs = ImmutableList.copyOf(input);
            return input;
        }
    }

    private static class RequestUsers implements Function<List<Message>, ListenableFuture<SparseArray<VKApiUserFull>>> {
        @Override
        public ListenableFuture<SparseArray<VKApiUserFull>> apply(List<Message> input) {
            Iterable<Integer> userIds = Iterables.concat(Iterables.transform(input, getUserIds));
            return UsersLoader.getInstance().getUsers(ImmutableSet.copyOf(userIds));
        }
    }

    private static class GetUserIds implements Function<Message, List<Integer>> {
        @Override
        public List<Integer> apply(Message input) {
            return input.getChatActive();
        }
    }
}
