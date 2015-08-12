package net.xaethos.sandbox.concurrent;

import android.os.Handler;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@MediumTest
public class FutureListenerQueueTest extends AndroidTestCase {

    TestListener listener;
    Handler handler;
    FutureListenerQueue<Object> queue;

    AsyncTestThread testThread;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testThread = AsyncTestThread.setUp();

        listener = new TestListener();
        handler = testThread.getHandler();
        queue = new FutureListenerQueue<>();
    }

    @Override
    protected void tearDown() throws Exception {
        testThread.tearDown();
        super.tearDown();
    }

    public void testAdd_beforeDispatch_noAction() throws Exception {
        final Object result = new Object();
        Future<Object> future = resolvedFuture(result);

        queue.add(future, listener, handler);

        testThread.awaitHandling();
        assertThat(listener, notCalled());
    }

    public void testAdd_afterDispatch_postOnNextTick() throws Exception {
        final Object result = new Object();
        Future<Object> future = resolvedFuture(result);

        queue.dispatchResolved(future);

        queue.add(future, listener, handler);
        assertThat(listener, notCalled());

        testThread.awaitHandling();
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

        testThread.awaitHandling();

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

        testThread.awaitHandling();
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

        testThread.awaitHandling();
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

    private static Matcher<TestListener> notCalled() {
        return TestListener.matches(null, null);
    }

    private static Matcher<TestListener> wasSuccess(Object value) {
        return TestListener.matches(is(value), null);
    }

    private static Matcher<TestListener> wasFailure(Throwable error) {
        return TestListener.matches(null, equalTo(error));
    }

    private static Matcher<TestListener> wasFailure(final Class<? extends Throwable> errorType) {
        Matcher<Throwable> instanceOf = new TypeSafeMatcher<Throwable>() {
            @Override
            public boolean matchesSafely(Throwable throwable) {
                return errorType.isInstance(throwable);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("instance of").appendText(errorType.getName());
            }
        };
        return TestListener.matches(null, instanceOf);
    }

    private static class TestListener implements ListenableFuture.Listener<Object> {
        public Object value = null;
        public Throwable error = null;
        public int callCount = 0;

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
    }

}