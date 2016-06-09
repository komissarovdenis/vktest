package com.test.ui.util.observable;

public class ObservableSupport<T> extends AbstractObservable<T> implements Observable<T> {
    private final T observed;

    public static <T> ObservableSupport<T> of(T observed) {
        return new ObservableSupport<T>(observed);
    }

    public static <T extends Observable<T>, U> ObservableDelegate<T,U> delegateOf(T observable) {
        return new ObservableDelegate<T,U>(observable);
    }

    @SuppressWarnings("unchecked")
    protected ObservableSupport() {
        super();
        this.observed = (T) this;
    }

    public ObservableSupport(T observed) {
        super();
        this.observed = observed;
    }

    @Override
    public void notifyObservers() {
        notifyObservers(getObserved());
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

    protected T getObserved() {
        return observed;
    }


    @Override public String toString() {
        if (observed == this) {
            return getClass().getSimpleName() + "()";
        } else {
            return getClass().getSimpleName() + ".of(" + observed + ")";
        }
    }
}
