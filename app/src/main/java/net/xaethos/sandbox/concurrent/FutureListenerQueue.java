/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xaethos.sandbox.concurrent;

import android.os.Handler;
import android.util.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FutureListenerQueue<V> {

    /**
     * The listeners to dispatch to.
     */
    private final LinkedList<Pair<ListenableFuture.Listener<V>, Handler>> listeners =
            new LinkedList<>();

    /**
     * Flag indicating whether our controlling future has resolved.
     */
    private boolean resolved = false;

    /**
     * Add the listener/handler pair to the list of pairs to execute.  Executes
     * the pair immediately if we've already started execution.
     */
    public void add(Future<V> future, ListenableFuture.Listener<V> listener, Handler handler) {

        if (listener == null) throw new NullPointerException("Listener was null.");
        if (handler == null) throw new NullPointerException("Handler was null.");

        boolean dispatchImmediate = false;

        // Lock while we check state.  We must maintain the lock while adding the
        // new pair so that another thread can't run the list out from under us.
        // We only add to the list if we have not yet started execution.
        synchronized (listeners) {
            if (!resolved) {
                listeners.add(new Pair<>(listener, handler));
            } else {
                dispatchImmediate = true;
            }
        }

        if (dispatchImmediate) {
            if (!future.isDone()) throw new IllegalStateException("Future is not yet done.");
            dispatchResolved(future, Collections.singleton(new Pair<>(listener, handler)));
        }
    }

    public void dispatchResolved(Future<V> future) {
        if (!future.isDone()) throw new IllegalStateException("Future is not yet done.");

        synchronized (listeners) {
            resolved = true;
        }

        dispatchResolved(future, listeners);
    }

    private void dispatchResolved(
            Future<V> future, Iterable<Pair<ListenableFuture.Listener<V>, Handler>> listeners) {
        V value = null;
        Throwable error = null;
        try {
            value = future.get();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted, but Future was done.", e);
        } catch (ExecutionException e) {
            error = e.getCause();
        } catch (CancellationException e) {
            error = e;
        }

        for (Pair<ListenableFuture.Listener<V>, Handler> pair : listeners) {
            pair.second.post(new DispatchRunnable<V>(pair.first, value, error));
        }

    }

    private static class DispatchRunnable<V> implements Runnable {
        private final ListenableFuture.Listener<V> mListener;
        private final V mValue;
        private final Throwable mError;

        private DispatchRunnable(ListenableFuture.Listener<V> listener, V value, Throwable error) {
            mListener = listener;
            mValue = value;
            mError = error;
        }

        @Override
        public void run() {
            if (mValue != null) {
                mListener.onSuccess(mValue);
            } else {
                mListener.onFailure(mError);
            }
        }
    }

}