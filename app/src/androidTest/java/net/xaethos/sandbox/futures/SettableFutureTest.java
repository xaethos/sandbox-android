package net.xaethos.sandbox.futures;

import android.test.AndroidTestCase;

import junit.framework.TestCase;

public class SettableFutureTest extends AndroidTestCase {

    SettableFuture<Object> future;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        future = new SettableFuture<>();
    }

    public void testSet() throws Exception {
        Object value;
    }

    public void testSetException() throws Exception {

    }

    public void testAddListener() throws Exception {

    }
}