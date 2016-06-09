package com.test.model;

import com.test.ui.util.observable.Observable;
import com.test.ui.util.observable.ObservableDelegate;
import com.test.ui.util.observable.ObservableSupport;
import com.test.ui.util.observable.Observer;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

public class Chat implements Observable<Chat> {

    private final ObservableDelegate observableDelegate = ObservableSupport.delegateOf(this);
    private final long chatId;
    private final VKList<VKApiMessage> messages;

    private int count;

    public Chat(long chatId, int count, VKList<VKApiMessage> messages) {
        this.chatId = chatId;
        this.count = count;
        this.messages = new VKList<>(messages);
    }

    public long getChatId() {
        return this.chatId;
    }

    public int getCount() {
        return count;
    }

    public boolean canFetch() {
        return getOffset() < count;
    }

    public void addItems(VKList<VKApiMessage> messages) {
        this.messages.addAll(messages);
        notifyObservers();
    }

    public VKList<VKApiMessage> getMessages() {
        return this.messages;
    }

    public void setCount(int count) {
        if (this.count != count) {
            this.count = count;
            notifyObservers();
        }
    }

    public int getOffset() {
        return count - (count - messages.size());
    }

    @Override
    public boolean addObserver(Observer<? super Chat> observer) {
        return observableDelegate.addObserver(observer);
    }

    @Override
    public boolean removeObserver(Observer<? super Chat> observer) {
        return observableDelegate.removeObserver(observer);
    }

    @Override
    public void removeObservers() {
        observableDelegate.removeObservers();
    }

    @Override
    public int observersCount() {
        return observableDelegate.observersCount();
    }

    @Override
    public void notifyObservers() {
        observableDelegate.notifyObservers();
    }
}
