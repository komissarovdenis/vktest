package com.test.ui.util.observable;

public class ObservableDelegate<T extends Observable<T>, U> extends ObservableSupport<T> implements Observer<U> {
    protected ObservableDelegate() {
        super();
    }

    public ObservableDelegate(T observable) {
        super(observable);
    }

    public ObservableDelegate<T,U> delegateFrom(Observable<? extends U> observable) {
        observable.addObserver(this);
        return this;
    }

    @Override
    public void handleUpdate(U updatedObject) {
        notifyObservers();
    }

    @Override
    public boolean addObserver(Observer<? super T> observer) {
        return super.addObserver(observer);
    }

    @Override
    public boolean removeObserver(Observer<? super T> observer) {
        return super.removeObserver(observer);
    }

    @Override
    public void removeObservers() {
        super.removeObservers();
    }

    @Override
    public int observersCount() {
        return super.observersCount();
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

    @Override
    public String toString() {
        return "ObservableDelegate.of(" + getObserved() + ")";
    }
}
