package de.bolz.gpsplayback;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppExecutors {

    private final Executor mainThread;
    private final Executor generalIO;
    private final Executor diskIO;
    private final Executor playbackThread;

    private Handler scheduleHandler;

    @Inject
    public AppExecutors() {
        this(new MainThreadExecutor(), Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());
    }

    public AppExecutors(Executor mainThread, Executor generalIO, Executor diskIO, Executor playbackThread) {
        this.mainThread = mainThread;
        this.generalIO = generalIO;
        this.diskIO = diskIO;
        this.playbackThread = playbackThread;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public Executor generalIO() {
        return generalIO;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor playbackThread() {
        return playbackThread;
    }

    public synchronized void schedule(Runnable runnable, Executor scheduleOn, long delay) {
        if (scheduleHandler == null) {
            initScheduleThread();
        }
        scheduleHandler.postDelayed(() -> scheduleOn.execute(runnable), delay);
    }

    private void initScheduleThread() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                scheduleHandler = new Handler(Looper.myLooper());
                countDownLatch.countDown();
                Looper.loop();
            }
        }.start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }


}
