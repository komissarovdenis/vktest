package com.test.model;

import android.util.Log;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.VKApplication;
import com.vk.sdk.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public final class DialogsLoader {
    private static final int COUNT_PER_REQUEST = 30;
    private static final int MAX_DIALOGS_TO_LOAD = 20;
    private static final String LOG_TAG = "Dialogs loader";
    private static final DialogsLoader instance = new DialogsLoader();

    public static DialogsLoader getInstance() {
        return instance;
    }

    public ListenableFuture<List<Message>> getDialogs() {
        return VKApplication.getInstance().getBackgroundTaskExecutor().submit(new DialogsRequest());
    }


    private static class DialogsRequest implements Callable<List<Message>> {
        private final List<Message> result = new ArrayList<>(MAX_DIALOGS_TO_LOAD);
        private final Ordering<Message> order = Ordering.from(new MessagesComparator());
        private int count = -1;

        @Override
        public List<Message> call() {
            requestDialogs(0);
            return order.immutableSortedCopy(result);
        }

        private void requestDialogs(final int offset) {
            if (count < 0 || (offset <= count && result.size() < MAX_DIALOGS_TO_LOAD)) {
                VKRequest request = VKApi.messages().getDialogs(
                        VKParameters.from(VKApiConst.COUNT, COUNT_PER_REQUEST, VKApiConst.OFFSET, offset));
                request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        try {
                            JSONObject root = response.json.getJSONObject("response");
                            count = root.getInt("count");
                            JSONArray items = root.getJSONArray("items");
                            for (int index = 0; index < items.length(); ++index) {
                                JSONObject item = items.getJSONObject(index);
                                JSONObject message = item.getJSONObject("message");
                                if (message.has("chat_id")) {
                                    Message chatMessage = new Message();
                                    result.add(chatMessage.parse(message));
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Parsing error " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(VKError error) {
                        Log.e(LOG_TAG, "Loading error: " + error.errorMessage);
                    }
                });

                requestDialogs(offset + COUNT_PER_REQUEST);
            }
        }
    }

    private static class MessagesComparator implements Comparator<Message> {
        @Override
        public int compare(Message lhs, Message rhs) {
            return (int) (rhs.date - lhs.date);
        }
    }
}
