package com.test.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.test.app.GuiThreadExecutor;
import com.test.app.VKApplication;
import com.test.ui.util.BoundFunction;
import com.test.ui.util.observable.Observable;

import java.util.Iterator;

public abstract class RecyclerViewReactiveAdapter<T, O extends Observable<O>, V extends RecyclerView.ViewHolder>
        extends RecyclerViewObservableAdapter<O, V> implements Iterable<T> {
    protected final GuiThreadExecutor guiThreadExecutor = GuiThreadExecutor.getInstance();
    protected final UpdateDataCallback updateDataCallback = new UpdateDataCallback();
    private final Function<O, ImmutableList<? extends T>> transformUpdate;

    private ImmutableList<T> data = ImmutableList.of();

    public RecyclerViewReactiveAdapter(O observable, Function<O, ImmutableList<? extends T>> dataBuilder) {
        super();
        this.transformUpdate = dataBuilder;
        setObservable(observable);
        handleUpdate(observable);
    }

    protected ImmutableList<T> getData() {
        return data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public final void handleUpdate(O updatedObject) {
        setData(transformUpdate(updatedObject));
    }

    public T getItem(int position) {
        return getData().get(position);
    }

    protected final void setData(ListenableFuture<? extends ImmutableList<? extends T>> data) {
        Futures.addCallback(data, updateDataCallback, guiThreadExecutor);
    }

    protected final ListenableFuture<? extends ImmutableList<? extends T>> transformUpdate(O updatedObject) {
        return getBackgroundExecutor().submit(BoundFunction.bind(transformUpdate, updatedObject));
    }

    protected final ListeningExecutorService getBackgroundExecutor() {
        return VKApplication.getInstance().getBackgroundTaskExecutor();
    }

    protected final void updateData(ImmutableList<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }


    private class UpdateDataCallback implements FutureCallback<ImmutableList<? extends T>> {
        @Override
        @SuppressWarnings("unchecked")
        public void onSuccess(ImmutableList<? extends T> result) {
            updateData((ImmutableList<T>) result);
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e("Recycler view adapter", "Failed to update adapter data: " + t.getMessage());
        }
    }
}
