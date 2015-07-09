package net.xaethos.sandbox.singletonloader;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.Loader;

public class SingletonLoader extends Loader<String> {

    private static SingletonLoader sInstance;

    protected final Handler mHandler = new Handler();

    protected String mData;
    protected int mCount;

    public SingletonLoader(Context context) {
        super(context);
    }

    public static SingletonLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SingletonLoader(context);
        }
        return sInstance;
    }

    @Override
    public final void deliverResult(String data) {
        if (isReset() || isAbandoned()) {
            return;
        }

        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        boolean loaded = false;

        if (mData != null) {
            loaded = true;
            deliverResult(mData);
        }

        if (takeContentChanged() || !loaded) {
            forceLoad();
        }
    }

    @Override
    public void forceLoad() {
        mHandler.postDelayed(new Delivery(), 1000);
        super.forceLoad();
    }

    @Override
    protected void onReset() {
        mHandler.removeCallbacks(null);
    }

    private class Delivery implements Runnable {

        @Override
        public void run() {
            deliverResult("Ping " + mCount++);
            mHandler.postDelayed(this, 1000);
        }
    }
}
