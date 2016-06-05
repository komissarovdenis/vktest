package com.test.app;

import android.app.Application;
import android.os.AsyncTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.test.ui.util.images.BitmapCache;
import com.test.ui.util.images.ImageDownloader;
import com.vk.sdk.VKSdk;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class VKApplication extends Application {
    private static final AtomicReference<VKApplication> instance = new AtomicReference<>();
    private final ListeningExecutorService backgroundTaskExecutor = listeningDecorator(
            executorServiceDecorator(AsyncTask.THREAD_POOL_EXECUTOR)
    );

    private final ImageDownloader imageDownloader = new ImageDownloader(BitmapCache.getInstance());

    public static VKApplication getInstance() {
        return instance.get();
    }

    public VKApplication() {
        super();
        instance.set(this);
    }

    public ListeningExecutorService getBackgroundTaskExecutor() {
        return backgroundTaskExecutor;
    }

    public ImageDownloader getImageDownloader() {
        return imageDownloader;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
    }

    public static ListeningExecutorService listeningDecorator(ExecutorService executorService) {
        return MoreExecutors.listeningDecorator(executorService);
    }

    public static ExecutorService executorServiceDecorator(Executor executor) {
        if (executor instanceof ExecutorService) {
            return (ExecutorService) executor;
        } else {
            return new ExecutorServiceDecorator(executor);
        }
    }
}
