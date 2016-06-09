package com.test.model;

import android.support.v4.util.LongSparseArray;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.VKApplication;
import com.vk.sdk.api.*;
import com.vk.sdk.api.methods.VKApiMessages;
import com.vk.sdk.api.model.VKApiGetMessagesResponse;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public final class ChatLoader {
    private static final String LOG_TAG = "Chat loader";
    private static final long CHAT_ID_OFFSET = 2000000000;
    private static final ChatLoader instance = new ChatLoader();

    private final LongSparseArray<Chat> chats = new LongSparseArray<>();

    public static ChatLoader getInstance() {
        return instance;
    }

    public ListenableFuture<Chat> fetchChatHistory(long chatId) {
        Chat chat = chats.get(chatId);
        int offset = chat == null ? 0 : chat.getOffset();
        return Futures.transform(VKApplication.getInstance().getBackgroundTaskExecutor()
                .submit(new ChatRequest(chatId, offset)), new ConvertToChat(chatId));
    }

    public Optional<Chat> getChat(long chatId) {
        return Optional.fromNullable(chats.get(chatId));
    }


    private class ConvertToChat implements Function<VKApiGetMessagesResponse, Chat> {
        private final long chatId;

        public ConvertToChat(long chatId) {
            this.chatId = chatId;
        }

        @Override
        public Chat apply(VKApiGetMessagesResponse input) {
            Chat chat = chats.get(chatId);
            if (chat == null) {
                chat = new Chat(chatId, input.count, input.items);
            } else {
                chat.setCount(input.count);
                chat.addItems(input.items);
            }
            chats.put(chatId, chat);
            return chat;
        }
    }


    private static VKApiGetMessagesResponse requestChat(long chatId, int offset) {
        final List<VKApiGetMessagesResponse> dialogResponse = new ArrayList<>(1);
        VKRequest request = new ApiMessages().getHistory(
                VKParameters.from(
                        VKApiConst.USER_ID, CHAT_ID_OFFSET + chatId,
                        VKApiConst.OFFSET, offset
                )
        );
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse source) {
                dialogResponse.add((VKApiGetMessagesResponse) source.parsedModel);
            }

            @Override
            public void onError(VKError error) {
                Log.e(LOG_TAG, "Loading error: " + error);
            }
        });
        return dialogResponse.get(0);
    }

    private static class ChatRequest implements Callable<VKApiGetMessagesResponse> {
        private final long userID;
        private final int offset;

        public ChatRequest(long userID, int offset) {
            this.userID = userID;
            this.offset = offset;
        }

        @Override
        public VKApiGetMessagesResponse call() {
            return requestChat(userID, offset);
        }
    }


    private static class ApiMessages extends VKApiMessages {
        public VKRequest getHistory(VKParameters params) {
            return prepareRequest("getHistory", params, new VKParser() {
                @Override
                public Object createModel(JSONObject object) {
                    return new VKApiGetMessagesResponse(object);
                }
            });
        }
    }
}
