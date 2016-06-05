package com.test.app;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorServiceDecorator extends AbstractExecutorService {
    private final Executor executor;

    private final Lock terminationLock = new ReentrantLock();
    private final Condition terminationMonitor = terminationLock.newCondition();

    private final AtomicInteger runningTasks = new AtomicInteger();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public ExecutorServiceDecorator(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        startTask();
        try {
            executor.execute(command);
        } finally {
            endTask();
        }
    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public void shutdown() {
        shutdown.set(true);
    }

    @NonNull @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public boolean isTerminated() {
        return isTerminated(runningTasks.get());
    }

    private boolean isTerminated(int tasksCount) {
        return shutdown.get() && tasksCount == 0;
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        terminationLock.lock();
        try {
            for (; ; ) {
                if (isTerminated()) {
                    return true;
                } else if (nanos <= 0) {
                    return false;
                } else {
                    nanos = terminationMonitor.awaitNanos(nanos);
                }
            }
        } finally {
            terminationLock.unlock();
        }
    }


    private void startTask() {
        if (isShutdown()) {
            throw new RejectedExecutionException("Executor already shutdown");
        }
        runningTasks.incrementAndGet();
    }

    private void endTask() {
        int newTasksCount = runningTasks.decrementAndGet();
        if (isTerminated(newTasksCount)) {
            terminationLock.lock();
            try {
                terminationMonitor.signalAll();
            } finally {
                terminationLock.unlock();
            }
        }
    }
}
