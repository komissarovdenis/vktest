package com.test.ui.adapter;

import android.support.v7.widget.RecyclerView;
import com.google.common.base.Optional;
import com.test.ui.util.Destroyable;
import com.test.ui.util.observable.Observable;
import com.test.ui.util.observable.Observer;

public abstract class RecyclerViewObservableAdapter<T, V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> implements Observer<T>, Destroyable {
    protected Optional<Observable<T>> observable = Optional.absent();

    protected final void setObservable(Observable<T> observable) {
        if (this.observable.isPresent()) {
            this.observable.get().removeObserver(this);
        }
        this.observable = Optional.fromNullable(observable);
        if (this.observable.isPresent()) {
            this.observable.get().addObserver(this);
        }
    }

    @Override
    public void destroy() {
        setObservable(null);
    }

    @Override
    public void handleUpdate(T updatedObject) {
        notifyDataSetChanged();
    }
}
