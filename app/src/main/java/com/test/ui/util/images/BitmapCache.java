package com.test.ui.util.images;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import com.google.common.collect.ImmutableSet;

public class BitmapCache {
    private static final BitmapCache instance = new BitmapCache();
    private final LruCache<ImmutableSet<String>, Bitmap> memoryCache;

    public static BitmapCache getInstance() {
        return instance;
    }

    private BitmapCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<ImmutableSet<String>, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(ImmutableSet<String> key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void add(String key, Bitmap bitmap) {
        add(ImmutableSet.of(key), bitmap);
    }

    public Bitmap get(String key) {
        return get(ImmutableSet.of(key));
    }

    public void add(ImmutableSet<String> key, Bitmap bitmap) {
        if (get(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap get(ImmutableSet<String> key) {
        return memoryCache.get(key);
    }
}
