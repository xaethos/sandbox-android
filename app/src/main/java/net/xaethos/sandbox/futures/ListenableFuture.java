package net.xaethos.sandbox.futures;

import android.os.Handler;

import java.util.concurrent.Future;

/**
 * A Future that can register listeners to be triggered once the value resolves.
 *
 * @param <V> the type of the Future value
 */
public interface ListenableFuture<V> extends Future<V> {

    /**
     * Add a listener to this Future. The listener will get called exactly once.
     *
     * @param listener the listener to call when future is done.
     * @param handler  a handler for the thread in which the listener will be called, or null for the main thread.
     */
    void addListener(Listener listener, Handler handler);

    interface Listener<V> {

        void onSuccess(V value);

        void onFailure(Throwable error);

    }

}
