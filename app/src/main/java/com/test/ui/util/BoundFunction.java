package com.test.ui.util;

import com.google.common.base.Function;

import java.util.concurrent.Callable;

public class BoundFunction<V, R> implements Callable<R> {
    private final Function<V, R> function;
    private final V boundParameter;

    public BoundFunction(Function<V, R> function, V boundParameter) {
        this.function = function;
        this.boundParameter = boundParameter;
    }

    public static <V, R> Callable<R> bind(Function<V, R> function, V boundParameter) {
        return new BoundFunction<>(function, boundParameter);
    }

    @Override
    public R call() {
        return function.apply(boundParameter);
    }
}
