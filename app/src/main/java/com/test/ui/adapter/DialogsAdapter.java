package com.test.ui.adapter;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.test.model.Message;
import com.test.ui.view.DialogItemView;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.List;

public class DialogsAdapter extends BaseAdapter {
    private final Function<Integer, Optional<VKApiUserFull>> getUser = new GetUser();
    private final List<Message> dialogs;
    private final SparseArray<VKApiUserFull> users;

    public DialogsAdapter(List<Message> dialogs, SparseArray<VKApiUserFull> users) {
        this.dialogs = dialogs;
        this.users = users;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return dialogs.size();
    }

    @Override
    public VKApiMessage getItem(int position) {
        return dialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dialogs.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DialogItemView view = convertView instanceof DialogItemView
                ? (DialogItemView) convertView : new DialogItemView(parent.getContext());
        Message message = dialogs.get(position);
        Iterable<VKApiUserFull> users = Optional.presentInstances(Iterables.transform(message.getChatActive(), getUser));
        view.bind(message, users);
        return view;
    }


    private class GetUser implements Function<Integer, Optional<VKApiUserFull>> {
        @Override
        public Optional<VKApiUserFull> apply(Integer input) {
            return Optional.fromNullable(users.get(input));
        }
    }
}
