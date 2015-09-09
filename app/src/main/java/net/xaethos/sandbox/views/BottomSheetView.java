package net.xaethos.sandbox.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BottomSheetView extends ViewGroup implements NestedScrollingParent {
    private static final int MODE_HIDE = 0;
    private static final int MODE_WRAP = 1;
    private static final int MODE_FILL = 2;

    private final NestedScrollView mSheetView;
    private final NestedScrollingParentHelper mScrollingParentHelper;

    private int mMode;

    private int mSheetCurrentHeight;
    private int mSheetWrapHeight;

    public BottomSheetView(Context context) {
        super(context);
        mSheetView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    public BottomSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSheetView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    public BottomSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSheetView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomSheetView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mSheetView = new NestedScrollView(context);
        mScrollingParentHelper = new NestedScrollingParentHelper(this);
        initialize();
    }

    private void initialize() {
        mMode = MODE_WRAP;
        mSheetCurrentHeight = -1;
        mSheetView.setFillViewport(true);

        final LayoutParams params = generateDefaultLayoutParams();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.WRAP_CONTENT;
        super.addView(mSheetView, 0, params);
    }

    @Override
    public void addView(@NonNull View child) {
        if (mSheetView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mSheetView.addView(child);
    }

    @Override
    public void addView(@NonNull View child, int index) {
        if (mSheetView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mSheetView.addView(child, index);
    }

    @Override
    public void addView(@NonNull View child, ViewGroup.LayoutParams params) {
        if (mSheetView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mSheetView.addView(child, params);
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (mSheetView.getChildCount() > 0) {
            throw new IllegalStateException("BottomSheetView can host only one direct child");
        }
        mSheetView.addView(child, index, params);
    }

    public final void hide() {
        setMode(MODE_HIDE);
    }

    public final void wrap() {
        setMode(MODE_WRAP);
    }

    public final void fill() {
        setMode(MODE_FILL);
    }

    private void setMode(int mode) {
        if (mMode != mode) {
            mMode = mode;
            mSheetCurrentHeight = -1;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        // We'll first measure how tall the sheets wants to be
        int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, LayoutParams.MATCH_PARENT);
        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, LayoutParams.WRAP_CONTENT);

        mSheetView.measure(childWidthSpec, childHeightSpec);
        mSheetWrapHeight = mSheetView.getMeasuredHeight();

        // At this point, we have all we need for our own measurements: match parent as possible
        int width = widthSpecMode == MeasureSpec.UNSPECIFIED ? mSheetView.getMeasuredWidth() :
                widthSpecSize;
        int height = heightSpecMode == MeasureSpec.UNSPECIFIED ? mSheetView.getMeasuredHeight() :
                heightSpecSize;
        setMeasuredDimension(width, height);

        // We might need a second measurement pass for out child, though
        if (mSheetCurrentHeight < 0) {
            switch (mMode) {
            case MODE_FILL:
                mSheetCurrentHeight = height;
                break;
            case MODE_WRAP:
                mSheetCurrentHeight = mSheetWrapHeight;
                break;
            case MODE_HIDE:
            default:
                mSheetCurrentHeight = 0;
            }
        }

        if (mSheetView.getMeasuredHeight() != mSheetCurrentHeight) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(mSheetCurrentHeight, MeasureSpec.EXACTLY);
            mSheetView.measure(childWidthSpec, childHeightSpec);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child = getChildAt(0);
        if (child == null) return;

        int width = r - l;
        int height = b - t;
        int childTop = height - mSheetCurrentHeight;
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
        // When scrolling down, only consume overflow
        if (dy <= 0) return;

        int available = getHeight() - mSheetCurrentHeight;
        if (available <= 0) return;

        consumed[1] = Math.min(available, dy);
        mSheetCurrentHeight += consumed[1];
        requestLayout();
    }

    @Override
    public void onNestedScroll(
            View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        // Consume downward overflow only
        if (dyUnconsumed >= 0) return;
        if (mSheetCurrentHeight <= 0) return;

        mSheetCurrentHeight = Math.max(0, mSheetCurrentHeight + dyUnconsumed);
        requestLayout();
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

        switch (mMode) {
        case MODE_FILL:
            mSheetCurrentHeight = getHeight();
            break;
        case MODE_WRAP:
            mSheetCurrentHeight = mSheetWrapHeight;
            break;
        case MODE_HIDE:
            mSheetCurrentHeight = 0;
        }
        requestLayout();
    }

}
