package net.xaethos.sandbox.concurrent;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.FutureTask;

public class SettableFuture<V> extends FutureTask<V> implements ListenableFuture<V> {

    private static final Runnable NOOP = new Runnable() {
        @Override
        public void run() {

        }
    };

    private final FutureListenerQueue<V> mListenerQueue = new FutureListenerQueue<>();

    public SettableFuture() {
        super(NOOP, null);
    }

    @Override
    public void set(V v) {
        super.set(v);
    }

    @Override
    public void setException(Throwable t) {
        super.setException(t);
    }

    @Override
    public void addListener(@NonNull Listener<V> listener, Handler handler) {
        mListenerQueue.add(this, listener, handler);
    }

    @Override
    protected void done() {
        mListenerQueue.dispatchResolved(this);
        super.done();
    }
}
