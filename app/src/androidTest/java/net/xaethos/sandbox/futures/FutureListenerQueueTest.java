package net.xaethos.sandbox.futures;

import android.os.Handler;
import android.os.Looper;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@MediumTest
public class FutureListenerQueueTest extends AndroidTestCase {

    TestListener listener;
    Handler handler;
    FutureListenerQueue<Object> queue;

    TestLooper mTestLooper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTestLooper = TestLooper.setUp();

        listener = new TestListener();
        handler = new Handler(mTestLooper.getLooper());
        queue = new FutureListenerQueue<>();
    }

    @Override
    protected void tearDown() throws Exception {
        mTestLooper.tearDown();
        super.tearDown();
    }

    public void testAdd_beforeDispatch_noAction() throws Exception {
        final Object result = new Object();
        Future<Object> future = resolvedFuture(result);

        queue.add(future, listener, handler);

        waitForPost(handler);
        assertThat(listener, notCalled());
    }

    public void testAdd_afterDispatch_postOnNextTick() throws Exception {
        final Object result = new Object();
        Future<Object> future = resolvedFuture(result);

        queue.dispatchResolved(future);

        queue.add(future, listener, handler);
        assertThat(listener, notCalled());

        waitForPost(handler);
        assertThat(listener, wasSuccess(result));
    }

    public void testAdd_afterDispatch_failsIfUnresolved() throws Exception {
        queue.dispatchResolved(resolvedFuture(new Object()));

        Future<Object> future = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });

        try {
            queue.add(future, listener, handler);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testDispatchResolved_callsAllListeners() throws Exception {
        Object result = new Object();
        Future<Object> future = resolvedFuture(result);
        TestListener anotherListener = new TestListener();

        queue.add(future, listener, handler);
        queue.add(future, anotherListener, handler);
        queue.dispatchResolved(future);

        waitForPost(handler);

        for (TestListener l : new TestListener[]{listener, anotherListener}) {
            assertThat(l, wasSuccess(result));
        }
    }

    public void testDispatchResolved_failsIfUnresolved() throws Exception {
        Future<Object> future = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });

        try {
            queue.dispatchResolved(future);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testFailedFuture() throws Exception {
        final Exception error = new Exception("I done bad");
        FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw error;
            }
        });
        future.run();

        queue.add(future, listener, handler);
        queue.dispatchResolved(future);

        waitForPost(handler);
        assertThat(listener, wasFailure(error));
    }

    public void testCancelledFuture() throws Exception {
        FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw null;
            }
        });
        future.cancel(true);

        queue.add(future, listener, handler);
        queue.dispatchResolved(future);

        waitForPost(handler);
        assertThat(listener, wasFailure(CancellationException.class));
    }

    private Future<Object> resolvedFuture(final Object value) {
        FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return value;
            }
        });
        future.run();
        return future;
    }

    private void waitForPost(Handler handler) {
        final CountDownLatch latch = new CountDownLatch(1);
        handler.post(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });
        try {
            latch.await(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted waiting for Handler post: " + e.getMessage());
        }
    }

    private static class TestLooper implements Runnable {
        public final CountDownLatch setUpLatch = new CountDownLatch(1);
        private Looper mLooper;

        private TestLooper() {
        }

        public Looper getLooper() {
            return mLooper;
        }

        public static TestLooper setUp() {
            TestLooper instance = new TestLooper();
            Executors.newSingleThreadExecutor().execute(instance);
            try {
                instance.setUpLatch.await(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Couldn't set up test thread looper: " + e.getMessage());
            }
            return instance;
        }

        @Override
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            Looper.loop();
        }

        public void tearDown() {
            mLooper.quit();
        }
    }

    private static Matcher<TestListener> notCalled() {
        return TestListener.matches(null, null);
    }

    private static Matcher<TestListener> wasSuccess(Object value) {
        return TestListener.matches(is(value), null);
    }

    private static Matcher<TestListener> wasFailure(Throwable error) {
        return TestListener.matches(null, is(error));
    }

    private static Matcher<TestListener> wasFailure(Class<? extends Throwable> errorType) {
        return TestListener.matches(null, (Matcher<Throwable>) instanceOf(errorType));
    }

    private static class TestListener implements ListenableFuture.Listener<Object> {
        public Object value = null;
        public Throwable error = null;
        public int callCount = 0;

        @Override
        public synchronized void onSuccess(Object value) {
            this.callCount++;
            this.value = value;
        }

        @Override
        public synchronized void onFailure(Throwable error) {
            this.callCount++;
            this.error = error;
        }

        public static Matcher<TestListener> matches(
                final Matcher<Object> valueMatcher, final Matcher<Throwable> errorMatcher) {
            return new TypeSafeMatcher<TestListener>() {
                @Override
                public boolean matchesSafely(TestListener testListener) {
                    if (valueMatcher != null) {
                        if (errorMatcher != null) throw new IllegalStateException();
                        return testListener.callCount == 1 &&
                                valueMatcher.matches(testListener.value);
                    } else if (errorMatcher != null) {
                        return testListener.callCount == 1 &&
                                errorMatcher.matches(testListener.error);
                    } else {
                        return testListener.callCount == 0;
                    }
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText("listener with state");
                }
            };
        }
    }

}