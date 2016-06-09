package com.test.ui.util.observable;

import com.google.common.collect.Sets;

import java.util.Set;

public abstract class AbstractObservable<T> implements Observable<T> {
    private final Set<Observer<? super T>> observers = Sets.newCopyOnWriteArraySet();

    @Override
    public boolean addObserver(Observer<? super T> observer) {
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(Observer<? super T> observer) {
        return observers.remove(observer);
    }

    @Override
    public void removeObservers() {
        observers.clear();
    }

    @Override
    public int observersCount() {
        return observers.size();
    }

    protected void notifyObservers(T observed) {
        for (Observer<? super T> observer : observers) {
            observer.handleUpdate(observed);
        }
    }
}
