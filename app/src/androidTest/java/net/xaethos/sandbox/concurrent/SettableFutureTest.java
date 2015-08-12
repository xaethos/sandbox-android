package net.xaethos.sandbox.concurrent;

import android.test.AndroidTestCase;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SettableFutureTest extends AndroidTestCase {

    SettableFuture<Object> future;
    ListenableFuture.Listener<Object> listener;
    AsyncTestThread testThread;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testThread = AsyncTestThread.setUp();
        listener = mock(ListenableFuture.Listener.class);
        future = new SettableFuture<>();
    }

    @Override
    protected void tearDown() throws Exception {
        testThread.tearDown();
        super.tearDown();
    }

    public void testSetAndGet() throws Exception {
        final Object value = new Object();

        testThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                future.set(value);
            }
        });

        assertSame(value, future.get(50, TimeUnit.MILLISECONDS));
    }

    public void testSetWithListener() throws Exception {
        Object value = new Object();
        future.addListener(listener, testThread.getHandler());

        future.set(value);
        testThread.awaitHandling();

        verify(listener, only()).onSuccess(value);
        verifyNoMoreInteractions(listener);
    }

    public void testSetException() throws Exception {
        Throwable error = new Exception();
        future.addListener(listener, testThread.getHandler());

        future.setException(error);
        testThread.awaitHandling();

        verify(listener, only()).onFailure(error);
        verifyNoMoreInteractions(listener);
    }

}