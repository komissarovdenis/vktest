package com.test.ui.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public final class Optionals {
    private Optionals() {}

    public static <T, U> Optional<U> flatMap(Optional<T> optional, Function<? super T, Optional<U>> function) {
        return flatten(optional.transform(function));
    }

    public static <T> Optional<T> flatten(Optional<Optional<T>> enclosingOptional) {
        if (enclosingOptional.isPresent()) {
            return enclosingOptional.get();
        } else {
            return Optional.absent();
        }
    }
}
