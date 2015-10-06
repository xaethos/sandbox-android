package net.xaethos.sandbox.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class BottomSheetView extends FrameLayout {

    private static final Property<BottomSheetView, Float> SHEET_TRANSLATION =
            new Property<BottomSheetView, Float>(Float.class, "sheetTranslation") {
                @Override
                public Float get(BottomSheetView object) {
                    return object.sheetTranslation;
                }

                @Override
                public void set(BottomSheetView object, Float value) {
                    object.setSheetTranslation(value);
                }
            };

    private static final long ANIMATION_DURATION = 300;

    public enum State {
        HIDDEN,
        PEEKED,
        EXPANDED
    }

    private State state = State.HIDDEN;

    private Animator currentAnimator;
    private TimeInterpolator animationInterpolator = new DecelerateInterpolator(1.6f);
    private Runnable runAfterDismiss;

    private OnDismissedListener onDismissedListener;
    private OnSheetStateChangeListener onSheetStateChangeListener;
    private final OnLayoutChangeListener onContentLayoutChangeListener =
            new ContentLayoutChangeListener();

    private Rect contentClipRect = new Rect();
    private boolean bottomSheetOwnsTouch;
    private boolean contentViewOwnsTouch;
    private float sheetTranslation;
    private VelocityTracker velocityTracker;
    private float minFlingVelocity;
    private float touchSlop;
    private boolean hasIntercepted;

    /**
     * Snapshot of the touch's x position on a down event
     */
    private float downX;

    /**
     * Snapshot of the touch's y position on a down event
     */
    private float downY;

    /**
     * Snapshot of the sheet's translation at the time of the last down event
     */
    private float downSheetTranslation;

    public BottomSheetView(Context context) {
        super(context);
        initialize();
    }

    public BottomSheetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomSheetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        minFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        touchSlop = viewConfiguration.getScaledTouchSlop();

        View dimView = new View(getContext());
        dimView.setBackgroundColor(Color.BLACK);
        dimView.setAlpha(0);
        dimView.setVisibility(INVISIBLE);
        super.addView(dimView,
                0,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setFocusableInTouchMode(true);
    }

    /**
     * Set dim and translation to the initial state
     */
    private void initializeSheet(final View contentView) {
        this.sheetTranslation = 0;
        this.contentClipRect.set(0, 0, getWidth(), getHeight());
        contentView.setTranslationY(getHeight());
        setDimAlpha(0);
        setState(State.HIDDEN);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) initializeSheet(getContentView());
    }

    /**
     * Don't call addView directly, use setContentView()
     */
    @Override
    public void addView(@NonNull View child) {
        if (getChildCount() > 1) {
            throw new IllegalArgumentException(
                    "You may not add more then one child of bottom sheet. The sheet view must" +
                            " be set with setContentView()");
        }
        setContentView(child);
    }

    @Override
    public void addView(@NonNull View child, int index) {
        addView(child);
    }

    @Override
    public void addView(@NonNull View child, int index, @NonNull ViewGroup.LayoutParams params) {
        addView(child);
    }

    @Override
    public void addView(@NonNull View child, @NonNull ViewGroup.LayoutParams params) {
        addView(child);
    }

    @Override
    public void addView(@NonNull View child, int width, int height) {
        addView(child);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        velocityTracker = VelocityTracker.obtain();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        velocityTracker.recycle();
        velocityTracker = null;
        cancelCurrentAnimation();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int bottomClip = (int) (getHeight() - Math.ceil(sheetTranslation));
        this.contentClipRect.set(0, 0, getWidth(), bottomClip);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isSheetShowing()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState dispatcherState = getKeyDispatcherState();
                if (dispatcherState != null) {
                    dispatcherState.handleUpEvent(event);
                }
                if (isSheetShowing() && event.isTracking() && !event.isCanceled()) {
                    dismissSheet();
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private void setSheetTranslation(float sheetTranslation) {
        this.sheetTranslation = sheetTranslation;
        int bottomClip = (int) (getHeight() - Math.ceil(sheetTranslation));

        this.contentClipRect.set(0, 0, getWidth(), bottomClip);
        getContentView().setTranslationY(getHeight() - sheetTranslation);

        setDimAlpha(getDimAlphaFromTranslation(sheetTranslation));
    }

    private float getDimAlphaFromTranslation(float sheetTranslation) {
        float progress = sheetTranslation / getMaxSheetTranslation();
        return progress * 0.8f;
    }

    private void setDimAlpha(float dimAlpha) {
        final View dimView = getChildAt(0);
        dimView.setAlpha(dimAlpha);
        dimView.setVisibility(dimAlpha > 0 ? VISIBLE : INVISIBLE);
    }

    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        boolean downAction = ev.getActionMasked() == MotionEvent.ACTION_DOWN;
        if (downAction) {
            hasIntercepted = false;
        }
        hasIntercepted = downAction && isSheetShowing();
        return hasIntercepted;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isSheetShowing()) {
            return false;
        }
        if (isAnimating()) {
            return false;
        }
        if (!hasIntercepted) {
            return onInterceptTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Snapshot the state of things when finger touches the screen.
            // This allows us to calculate deltas without losing precision which we would have if
            // we calculated deltas based on the previous touch.
            bottomSheetOwnsTouch = false;
            contentViewOwnsTouch = false;
            downY = event.getY();
            downX = event.getX();
            downSheetTranslation = sheetTranslation;
            velocityTracker.clear();
        }
        velocityTracker.addMovement(event);

        View contentView = getContentView();

        // The max translation is a hard limit.
        float maxSheetTranslation = getMaxSheetTranslation();

        float deltaY = downY - event.getY();

        if (!bottomSheetOwnsTouch && !contentViewOwnsTouch) {
            bottomSheetOwnsTouch = Math.abs(deltaY) > touchSlop;
            contentViewOwnsTouch = Math.abs(downX - event.getX()) > touchSlop;

            if (bottomSheetOwnsTouch) {
                if (state == State.PEEKED) {
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.offsetLocation(0, sheetTranslation - getHeight());
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    contentView.dispatchTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                contentViewOwnsTouch = false;
                downY = event.getY();
                downX = event.getX();
                deltaY = 0;
            }
        }

        // This is not the actual new sheet translation but a first approximation it will be
        // adjusted to account for max and min translations etc.
        float newSheetTranslation = downSheetTranslation + deltaY;

        if (bottomSheetOwnsTouch) {
            // If we are scrolling down and the sheet cannot scroll further, go out of expanded
            // mode.
            boolean scrollingDown = deltaY < 0;
            boolean canScrollUp = canScrollUp(contentView,
                    event.getX(),
                    event.getY() + (sheetTranslation - getHeight()));

            if (state == State.EXPANDED && scrollingDown && !canScrollUp) {
                // Reset variables so deltas are correctly calculated from the point at which the
                // sheet was 'detached' from the top.
                downY = event.getY();
                downSheetTranslation = sheetTranslation;
                velocityTracker.clear();
                setState(State.PEEKED);
                newSheetTranslation = sheetTranslation;

                // Dispatch a cancel event to the sheet to make sure its touch handling is
                // cleaned up nicely.
                MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                contentView.dispatchTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }

            // If we are at the top of the view we should go into expanded mode.
            if (state == State.PEEKED && newSheetTranslation > maxSheetTranslation) {
                setSheetTranslation(maxSheetTranslation);

                // Dispatch a down event to the sheet to make sure its touch handling is
                // initiated correctly.
                newSheetTranslation = Math.min(maxSheetTranslation, newSheetTranslation);
                MotionEvent downEvent = MotionEvent.obtain(event);
                downEvent.setAction(MotionEvent.ACTION_DOWN);
                contentView.dispatchTouchEvent(downEvent);
                downEvent.recycle();
                setState(State.EXPANDED);
            }

            if (state == State.EXPANDED) {
                // Dispatch the touch to the sheet if we are expanded so it can handle its own
                // internal scrolling.
                event.offsetLocation(0, sheetTranslation - getHeight());
                contentView.dispatchTouchEvent(event);
            } else {
                setSheetTranslation(newSheetTranslation);

                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // If touch is canceled, go back to previous state, a canceled touch should
                    // never commit an action.
                    peekSheet(contentView);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // If touch is released, go to a new state depending on velocity.
                    // If the velocity is not high enough we use the position of the sheet to
                    // determine the new state.
                    velocityTracker.computeCurrentVelocity(1000);
                    float velocityY = velocityTracker.getYVelocity();

                    if (Math.abs(velocityY) >= minFlingVelocity) {
                        if (velocityY < 0) {
                            peekSheet(contentView);
                        } else {
                            dismissSheet();
                        }
                    } else if (newSheetTranslation > (maxSheetTranslation * 2) / 3) {
                        peekSheet(contentView);
                    } else {
                        dismissSheet();
                    }
                }
            }
        } else {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect contentRect = new Rect();
            contentView.getHitRect(contentRect);

            // Dismiss if tap is outside of the bottom sheet content.
            if (event.getAction() == MotionEvent.ACTION_UP && !contentRect.contains(x, y)) {
                dismissSheet();
                return true;
            }

            event.offsetLocation(-contentRect.left, -contentRect.top);
            contentView.dispatchTouchEvent(event);
        }
        return true;
    }

    private boolean isAnimating() {
        return currentAnimator != null;
    }

    private void cancelCurrentAnimation() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
    }

    private boolean canScrollUp(View view, float x, float y) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                int childLeft = child.getLeft();
                int childTop = child.getTop();
                int childRight = child.getRight();
                int childBottom = child.getBottom();
                boolean intersects =
                        x > childLeft && x < childRight && y > childTop && y < childBottom;
                if (intersects && canScrollUp(child, x - childLeft, y - childTop)) {
                    return true;
                }
            }
        }
        return view.canScrollVertically(-1);
    }

    private void setState(State state) {
        if (this.state == state) return;

        this.state = state;
        if (onSheetStateChangeListener != null) {
            onSheetStateChangeListener.onSheetStateChanged(state);
        }
    }

    /**
     * @return The maximum translation for the presented sheet view. Translation is counted from
     * the bottom of the view.
     */
    public float getMaxSheetTranslation() {
        return Math.min(getHeight() - getPaddingTop(), getContentView().getHeight());
    }

    /**
     * @return The currently presented sheet content. If no sheet is currently presented null will
     * returned.
     */
    public View getContentView() {
        return getChildCount() > 1 ? getChildAt(1) : null;
    }

    /**
     * Set the content of the bottom sheet.
     *
     * @param contentView The sheet content of your application.
     */
    public void setContentView(View contentView) {
        setState(State.HIDDEN);
        while (getChildCount() > 1) removeViewAt(1);
        if (contentView == null) return;

        LayoutParams params = (LayoutParams) contentView.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL);
        }

        if (params.width == LayoutParams.WRAP_CONTENT) {
            // Center by default if they didn't specify anything
            if (params.gravity == -1) {
                params.gravity = Gravity.CENTER_HORIZONTAL;
            }
        }

        super.addView(contentView, -1, params);
        initializeSheet(contentView);
    }

    /**
     * Present a sheet view to the user.
     * If another sheet is currently presented, it will be dismissed, and the new sheet will be
     * shown after that
     *
     * @param layoutRes Layout resource for the sheet content.
     */
    public void showWithContentView(@LayoutRes int layoutRes) {
        showWithContentView(LayoutInflater.from(getContext()).inflate(layoutRes, this, false));
    }

    /**
     * Present a sheet view to the user.
     * If another sheet is currently presented, it will be dismissed, and the new sheet will be
     * shown after that
     *
     * @param sheetView The sheet to be presented.
     */
    public void showWithContentView(final View sheetView) {
        if (state != State.HIDDEN) {
            dismissSheet(new Runnable() {
                @Override
                public void run() {
                    showWithContentView(sheetView);
                }
            });
            return;
        }

        setContentView(sheetView);
        show(sheetView);
    }

    public void show() {
        final View contentView = getContentView();
        if (contentView == null) throw new IllegalStateException("show() called, but no content");
        show(contentView);
    }

    private void show(final View contentView) {
        if (state != State.HIDDEN) return;

        // Don't start animating until the sheet has been drawn once. This ensures that we don't
        // do layout while animating and that
        // the drawing cache for the view has been warmed up. tl;dr it reduces lag.
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                post(new Runnable() {
                    @Override
                    public void run() {
                        // Make sure sheet view is still here when first draw happens.
                        // In the case of a large lag it could be that the view is dismissed
                        // before it is drawn resulting in sheet view being null here.
                        if (getContentView() != null) {
                            peekSheet(contentView);
                        }
                    }
                });
                return true;
            }
        });
        contentView.addOnLayoutChangeListener(onContentLayoutChangeListener);
    }

    /**
     * Set the presented sheet to be in a peeked state.
     */
    private void peekSheet(final View contentView) {
        cancelCurrentAnimation();

        contentView.setLayerType(LAYER_TYPE_HARDWARE, null);
        ObjectAnimator anim =
                ObjectAnimator.ofFloat(this, SHEET_TRANSLATION, getMaxSheetTranslation());
        anim.setDuration(ANIMATION_DURATION);
        anim.setInterpolator(animationInterpolator);
        anim.addListener(new CancelDetectionAnimationListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                contentView.setLayerType(LAYER_TYPE_NONE, null);
                if (!canceled) {
                    currentAnimator = null;
                }
            }
        });
        anim.start();
        currentAnimator = anim;
        setState(State.PEEKED);
    }

    /**
     * Dismiss the sheet currently being presented.
     */
    public void dismissSheet() {
        dismissSheet(null);
    }

    private void dismissSheet(Runnable runAfterDismissThis) {
        if (state == State.HIDDEN) {
            runAfterDismiss = null;
            return;
        }
        // This must be set every time, including if the parameter is null
        // Otherwise a new sheet might be shown when the caller called dismiss after a
        // showWithSheet call
        runAfterDismiss = runAfterDismissThis;

        final View contentView = getContentView();
        contentView.removeOnLayoutChangeListener(onContentLayoutChangeListener);
        contentView.setLayerType(LAYER_TYPE_HARDWARE, null);

        cancelCurrentAnimation();

        ObjectAnimator anim = ObjectAnimator.ofFloat(this, SHEET_TRANSLATION, 0);
        anim.setDuration(ANIMATION_DURATION);
        anim.setInterpolator(animationInterpolator);
        anim.addListener(new CancelDetectionAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                contentView.setLayerType(LAYER_TYPE_NONE, null);
                if (!canceled) {
                    currentAnimator = null;
                    setState(State.HIDDEN);
                    removeView(contentView);

                    if (onDismissedListener != null) {
                        onDismissedListener.onDismissed(BottomSheetView.this);
                    }

                    // Remove sheet specific properties
                    if (runAfterDismiss != null) {
                        runAfterDismiss.run();
                        runAfterDismiss = null;
                    }
                }
            }
        });
        anim.start();
        currentAnimator = anim;
    }

    /**
     * @return The current state of the sheet.
     */
    public State getState() {
        return state;
    }

    /**
     * @return Whether or not a sheet is currently presented.
     */
    public boolean isSheetShowing() {
        return state != State.HIDDEN;
    }

    /**
     * Set a OnSheetStateChangeListener which will be notified when the state of the presented
     * sheet changes.
     *
     * @param onSheetStateChangeListener the listener to be notified.
     */
    public void setOnSheetStateChangeListener(
            OnSheetStateChangeListener onSheetStateChangeListener) {
        this.onSheetStateChangeListener = onSheetStateChangeListener;
    }

    public void setOnDismissedListener(OnDismissedListener onDismissedListener) {
        this.onDismissedListener = onDismissedListener;
    }

    /**
     * Utility class which registers if the animation has been canceled so that subclasses may
     * respond differently in onAnimationEnd
     */
    private class CancelDetectionAnimationListener extends AnimatorListenerAdapter {

        protected boolean canceled;

        @Override
        public void onAnimationCancel(Animator animation) {
            canceled = true;
        }

    }

    private class ContentLayoutChangeListener implements OnLayoutChangeListener {
        @Override
        public void onLayoutChange(
                View contentView,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {
            int oldHeight = oldBottom - oldTop;
            int newHeight = bottom - top;
            if (state != State.HIDDEN) {
                if (state == State.EXPANDED && newHeight < oldHeight) {
                    // The sheet can no longer be in the expanded state if it has shrunk
                    setState(State.PEEKED);
                }

                if (newHeight != oldHeight) {
                    setSheetTranslation(newHeight);
                }
            }
        }
    }

    public interface OnSheetStateChangeListener {
        void onSheetStateChanged(State state);
    }

    public interface OnDismissedListener {

        /**
         * Called when the presented sheet has been dismissed.
         *
         * @param bottomSheetView The bottom sheet which contained the presented sheet.
         */
        void onDismissed(BottomSheetView bottomSheetView);

    }
}