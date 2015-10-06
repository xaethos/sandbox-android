package net.xaethos.sandbox.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Temporary fix for https://code.google.com/p/android/issues/detail?id=169951
 */
public class NestedScrollView extends android.support.v4.widget.NestedScrollView {
    public NestedScrollView(final Context context) {
        super(context);
    }

    public NestedScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollView(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent event) {
        final int actionMasked = event.getActionMasked();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                // Defensive cleanup for new gesture
                ViewCompat.stopNestedScroll(this);
            }
        }

        final boolean result = super.dispatchTouchEvent(event);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Clean up after nested scrolls if this is the end of a gesture;
            // also cancel it if we tried an ACTION_DOWN but we didn't want the rest
            // of the gesture.
            if (actionMasked == MotionEvent.ACTION_UP ||
                    actionMasked == MotionEvent.ACTION_CANCEL ||
                    (actionMasked == MotionEvent.ACTION_DOWN && !result)) {
                ViewCompat.stopNestedScroll(this);
            }
        }

        return result;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.stopNestedScroll(this);
        }
    }
}