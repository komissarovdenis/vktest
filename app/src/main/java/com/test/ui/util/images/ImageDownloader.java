package com.test.ui.util.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.VKApplication;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class ImageDownloader {
    private static final String LOG_TAG = "Image Downloader";
    private static final int AVATAR_EDGE_SIZE = 100;

    private final Map<ImmutableSet<String>, ListenableFuture<Bitmap>> tasks = new ConcurrentHashMap<>();
    private final BitmapCache bitmapCache;

    public ImageDownloader(BitmapCache bitmapCache) {
        this.bitmapCache = bitmapCache;
    }

    public ListenableFuture<Bitmap> downloadImage(String uri) {
        return downloadImage(ImmutableSet.of(uri));
    }

    public ListenableFuture<Bitmap> downloadImage(ImmutableSet<String> uris) {
        Bitmap cachedBitmap = bitmapCache.get(uris);
        if (cachedBitmap == null) {
            ListenableFuture<Bitmap> task = tasks.get(uris);
            if (task == null) {
                task = VKApplication.getInstance().getBackgroundTaskExecutor().submit(new DownloadTask(uris));
                tasks.put(uris, task);
            }
            return task;
        } else {
            return Futures.immediateFuture(cachedBitmap);
        }
    }

    public void cancelTask(String uri) {
        cancelTask(ImmutableSet.of(uri));
    }

    public void cancelTask(ImmutableSet<String> uris) {
        ListenableFuture<Bitmap> task = tasks.get(uris);
        if (task != null) {
            task.cancel(true);
            tasks.remove(uris);
        }
    }


    private class DownloadTask implements Callable<Bitmap> {
        private final ImmutableSet<String> uris;

        public DownloadTask(ImmutableSet<String> uris) {
            this.uris = uris;
        }

        @Override
        public Bitmap call() {
            List<Bitmap> bitmapList = new ArrayList<>();
            for (String uri : uris) {
                Bitmap cachedBitmap = bitmapCache.get(uri);
                if (cachedBitmap == null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(new URL(uri).openConnection().getInputStream());
                        bitmapCache.add(uri, bitmap);
                        bitmapList.add(bitmap);
                    } catch (InterruptedIOException e) {
                        // nothing
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Download failure " + e);
                    }
                } else {
                    bitmapList.add(cachedBitmap);
                }
            }
            tasks.remove(uris);
            if (bitmapList.isEmpty()) {
                return null;
            } else if (bitmapList.size() == 1) {
                return bitmapList.get(0);
            } else {
                Bitmap bitmap = AvatarUtils.createBitmap(bitmapList, AVATAR_EDGE_SIZE, AVATAR_EDGE_SIZE);
                bitmapCache.add(uris, bitmap);
                return bitmap;
            }
        }
    }
}
