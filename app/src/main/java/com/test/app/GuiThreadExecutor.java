package com.test.app;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Executor;

public final class GuiThreadExecutor implements Executor {
    private static final GuiThreadExecutor instance = new GuiThreadExecutor();

    private final Handler handler;
    private final Thread thread;

    private GuiThreadExecutor() {
        this(Looper.getMainLooper());
    }

    public GuiThreadExecutor(Looper looper) {
        this(new Handler(looper));
    }

    public GuiThreadExecutor(Handler handler) {
        this.handler = handler;
        this.thread = handler.getLooper().getThread();
    }

    public static GuiThreadExecutor getInstance() {
        return instance;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        Runnable task = new LoggedTask(command);
        if (Thread.currentThread() == this.thread) {
            task.run();
        } else {
            handler.post(task);
        }
    }


    private static class LoggedTask implements Runnable {
        private final Runnable task;

        public LoggedTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e) {
                Log.e("Gui thread", "Failure: {}", e);
                throw e;
            }
        }
    }
}
