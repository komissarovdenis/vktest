package com.test.ui.util.observable;

public interface Observable<T> {
    boolean addObserver(Observer<? super T> observer);
    boolean removeObserver(Observer<? super T> observer);
    void removeObservers();
    int observersCount();

    void notifyObservers();
}
