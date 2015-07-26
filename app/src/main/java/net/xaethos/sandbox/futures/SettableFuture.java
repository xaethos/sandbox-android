package net.xaethos.sandbox.futures;

import android.os.Handler;

import java.util.concurrent.FutureTask;

public class SettableFuture<V> extends FutureTask<V> implements ListenableFuture<V> {

    private static final Runnable NOOP = new Runnable() {
        @Override
        public void run() {

        }
    };

    private final FutureListenerQueue mListenerQueue = new FutureListenerQueue();

    public SettableFuture() {
        super(NOOP, null);
    }

    @Override
    public void set(V v) {
        super.set(v);
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
    }

    @Override
    public void addListener(Listener listener, Handler handler) {
        mListenerQueue.add(this, listener, handler);
    }

}
