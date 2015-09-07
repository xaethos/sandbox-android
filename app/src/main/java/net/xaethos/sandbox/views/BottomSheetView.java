package net.xaethos.sandbox.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BottomSheetView extends ViewGroup implements NestedScrollingParent {

    private final NestedScrollView mScrollView;
    private final NestedScrollingParentHelper mScrollingParentHelper;

    private int mSheetOffset;
    private int mSheetHeight;

    public BottomSheetView(Context context) {
        super(context);
        mScrollView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    public BottomSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScrollView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    public BottomSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScrollView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomSheetView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScrollView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    private void initialize() {
        mSheetOffset = 0;
        mSheetHeight = 0;
        mScrollView.setFillViewport(true);
        super.addView(mScrollView, 0, generateDefaultLayoutParams());
    }

    @Override
    public void addView(@NonNull View child) {
        if (mScrollView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mScrollView.addView(child);
    }

    @Override
    public void addView(@NonNull View child, int index) {
        if (mScrollView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mScrollView.addView(child, index);
    }

    @Override
    public void addView(@NonNull View child, ViewGroup.LayoutParams params) {
        if (mScrollView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mScrollView.addView(child, params);
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (mScrollView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mScrollView.addView(child, index, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        // Child should always match our width, and we our parent's
        int childWidthSpec;
        // Measure child's desired height.
        int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        } else {
            childWidthSpec = MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY);
        }

        mScrollView.measure(childWidthSpec, childHeightSpec);

        int sheetHeight = mScrollView.getMeasuredHeight();
        if (sheetHeight != mSheetHeight) {
            // reset offsets
            mSheetHeight = sheetHeight;
            mSheetOffset = 0;
        }

        // We don't expect to ever be used in UNSPECIFIED mode, but check anyway.
        if (heightSpecMode != MeasureSpec.UNSPECIFIED) {
            // If we have scrolled up, add that to the sheet size
            if (mSheetOffset > 0) sheetHeight += mSheetOffset;

            if (sheetHeight >= heightSpecSize) {
                // Child wants to be larger that the space we have available, so we are in de facto
                // FILL mode.
                mScrollView.measure(childWidthSpec,
                        MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY));
            } else if (mSheetOffset > 0) {
                // If we have scrolled up, stretch child
                mScrollView.measure(childWidthSpec,
                        MeasureSpec.makeMeasureSpec(sheetHeight, MeasureSpec.EXACTLY));
            }
        }

        if (widthSpecMode == MeasureSpec.UNSPECIFIED) {
            setMeasuredDimension(mScrollView.getMeasuredWidthAndState(), heightSpecSize);
        } else {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child = getChildAt(0);
        if (child == null) return;

        int width = r - l;
        int height = b - t;
        int childHeight = mSheetHeight + mSheetOffset;
        int childTop = height - childHeight;
        child.layout(0, childTop, width, height);
    }

    @Override
    public int getNestedScrollAxes() {
        return mScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy == 0) return;

        int viewHeight = getHeight();
        int sheetHeight = mSheetHeight + mSheetOffset;

        if (dy > 0) {
            // going up
            int available = viewHeight - sheetHeight;
            if (available <= 0) return;
            consumed[1] = Math.min(available, dy);
        } else {
            // going down
            if (sheetHeight <= 0) return;
            consumed[1] = Math.max(dy, -sheetHeight);
        }

        mSheetOffset += consumed[1];
        requestLayout();
    }

    @Override
    public void onNestedScroll(
            View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        // Do nothing
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        // Do nothing
        return false;
    }

    @Override
    public void onStopNestedScroll(View child) {
        mScrollingParentHelper.onStopNestedScroll(child);
    }

}
