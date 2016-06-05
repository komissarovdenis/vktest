package com.test.model;

import android.util.Log;
import android.util.SparseArray;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.VKApplication;
import com.vk.sdk.api.*;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import java.util.Iterator;
import java.util.concurrent.Callable;

public final class UsersLoader {
    private static final String LOG_TAG = "Users loader";
    private static final UsersLoader instance = new UsersLoader();

    private final CollectUsers collectUsers = new CollectUsers();
    private final SparseArray<VKApiUserFull> users = new SparseArray<>();

    public static UsersLoader getInstance() {
        return instance;
    }

    public Optional<VKApiUserFull> get(Integer userID) {
        return Optional.fromNullable(users.get(userID));
    }

    public ListenableFuture<SparseArray<VKApiUserFull>> getUsers(Iterable<Integer> userIDs) {
        return Futures.transform(VKApplication.getInstance().getBackgroundTaskExecutor()
                .submit(new UsersRequest(userIDs)), collectUsers);
    }

    private class CollectUsers implements Function<SparseArray<VKApiUserFull>, SparseArray<VKApiUserFull>> {
        @Override
        public SparseArray<VKApiUserFull> apply(SparseArray<VKApiUserFull> input) {
            for (int index = 0; index < input.size(); ++index) {
                VKApiUserFull user = input.valueAt(index);
                users.put(user.getId(), user); // don't check exist 'cause user fields may be changed
            }
            return input;
        }
    }

    private static SparseArray<VKApiUserFull> requestUsers(Iterable<Integer> userIDs) {
        final SparseArray<VKApiUserFull> result = new SparseArray<>();
        VKRequest request = VKApi.users().get(
                VKParameters.from(
                        VKApiConst.USER_IDS, mkString(userIDs, ","),
                        VKApiConst.FIELDS, "photo_100",
                        VKApiConst.NAME_CASE, "Nom"
                )
        );
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse source) {
                VKList usersArray = (VKList) source.parsedModel;
                for (Object obj : usersArray) {
                    VKApiUserFull user = (VKApiUserFull) obj;
                    result.put(user.getId(), user);
                }
            }

            @Override
            public void onError(VKError error) {
                Log.e(LOG_TAG, "Loading error: " + error.errorMessage);
            }
        });
        return result;
    }

    public static <T> String mkString(Iterable<T> iterable, String separator) {
        StringBuilder builder = new StringBuilder();
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            builder.append(item);
            if (iterator.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    private static class UsersRequest implements Callable<SparseArray<VKApiUserFull>> {
        private final Iterable<Integer> userIDs;

        public UsersRequest(Iterable<Integer> userIDs) {
            this.userIDs = userIDs;
        }

        @Override
        public SparseArray<VKApiUserFull> call() {
            return requestUsers(userIDs);
        }
    }
}
