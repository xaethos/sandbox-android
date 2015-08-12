package net.xaethos.sandbox.concurrent;

import android.os.Handler;
import android.os.Looper;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class AsyncTestThread {

    private final CountDownLatch mSetUpLatch = new CountDownLatch(1);

    private Looper mLooper;
    private Handler mHandler;

    private AsyncTestThread() {
    }

    public static AsyncTestThread setUp() throws InterruptedException {
        final AsyncTestThread testThread = new AsyncTestThread();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                testThread.mLooper = Looper.myLooper();
                testThread.mHandler = new Handler(testThread.mLooper);
                testThread.mSetUpLatch.countDown();
                Looper.loop();
            }
        });
        if (!testThread.mSetUpLatch.await(50, TimeUnit.MILLISECONDS)) {
            Assert.fail("Timeout awaiting for looper thread to start");
        }
        return testThread;
    }

    public void tearDown() {
        this.mLooper.quit();
    }

    public Looper getLooper() {
        return mLooper;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void awaitHandling() {
        awaitHandling(200, TimeUnit.MILLISECONDS);
    }

    public void awaitHandling(long timeout, TimeUnit unit) {
        final CountDownLatch latch = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(timeout, unit)) {
                Assert.fail("Timeout awaiting for looper thread to handle events");
            }
        } catch (InterruptedException e) {
            Assert.fail("Interrupted awaiting for looper thread to handle events");
        }
    }

}
