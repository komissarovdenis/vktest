package com.test.ui.util.observable;

public interface Observer<T> {
    void handleUpdate(T updatedObject);
}
