package net.xaethos.sandbox.concurrent;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Future;

/**
 * A Future that can register listeners to be triggered once the value resolves.
 *
 * @param <V> the type of the Future value
 */
public interface ListenableFuture<V> extends Future<V> {

    /**
     * Add a listener to this Future. The listener will get called exactly once when the
     * computation is finished. If the future is already done, the listener will be called
     * immediately.
     * <p>
     * In either case, the listener will be called via the provided Handler. This means that even
     * if future is done, the listener call won't happen until after the current Looper event.
     * <p>
     * There is no guaranteed ordering of execution of listeners.
     *
     * @param listener the listener to call when future is done.
     * @param handler  a handler for the thread in which the listener will be called, or null for
     *                 the main thread.
     * @throws NullPointerException if the listener was null
     */
    void addListener(@NonNull Listener<V> listener, @Nullable Handler handler);

    interface Listener<V> {

        void onSuccess(V value);

        void onFailure(Throwable error);

    }

}
