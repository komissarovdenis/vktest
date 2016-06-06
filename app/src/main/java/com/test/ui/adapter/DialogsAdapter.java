package com.test.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.test.model.Message;
import com.test.ui.view.DialogItemView;
import com.vk.sdk.api.model.VKApiMessage;

import java.util.List;

public class DialogsAdapter extends BaseAdapter {
    private final List<Message> dialogs;

    public DialogsAdapter(List<Message> dialogs) {
        this.dialogs = dialogs;
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
        return dialogs.get(position).getChatId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DialogItemView view = convertView instanceof DialogItemView
                ? (DialogItemView) convertView : new DialogItemView(parent.getContext());
        view.bind(dialogs.get(position));
        return view;
    }
}
