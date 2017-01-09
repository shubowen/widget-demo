/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.xiaosu.lib.base.widget.drawerLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.colorLongPressedHighlight;
import static android.R.attr.duration;
import static android.R.attr.gravity;
import static android.R.attr.offset;
import static android.R.attr.top;

/**
 * DrawerLayout acts as a top-level container for window content that allows for
 * interactive "drawer" views to be pulled out from one or both vertical edges of the window.
 * <p>
 * <p>Drawer positioning and layout is controlled using the <code>android:layout_gravity</code>
 * attribute on child views corresponding to which side of the view you want the drawer
 * to emerge from: left or right (or start/end on platform versions that support layout direction.)
 * Note that you can only have one drawer view for each vertical edge of the window. If your
 * layout configures more than one drawer view per vertical edge of the window, an exception will
 * be thrown at runtime.
 * </p>
 * <p>
 * <p>To use a DrawerLayout, position your primary content view as the first child with
 * width and height of <code>match_parent</code> and no <code>layout_gravity></code>.
 * Add drawers as child views after the main content view and set the <code>layout_gravity</code>
 * appropriately. Drawers commonly use <code>match_parent</code> for height with a fixed width.</p>
 * <p>
 * <p>{@link DrawerListener} can be used to monitor the state and motion of drawer views.
 * Avoid performing expensive operations such as layout during animation as it can cause
 * stuttering; try to perform expensive operations during the {@link #STATE_IDLE} state.
 * {@link SimpleDrawerListener} offers default/no-op implementations of each callback method.</p>
 * <p>
 * <p>As per the <a href="{@docRoot}design/patterns/navigation-drawer.html">Android Design
 * guide</a>, any drawers positioned to the left/start should
 * always contain content for navigating around the application, whereas any drawers
 * positioned to the right/end should always contain actions to take on the current content.
 * This preserves the same navigation left, actions right structure present in the Action Bar
 * and elsewhere.</p>
 * <p>
 * <p>For more information about how to use DrawerLayout, read <a
 * href="{@docRoot}training/implementing-navigation/nav-drawer.html">Creating a Navigation
 * Drawer</a>.</p>
 */
public class DrawerLayout extends ViewGroup implements
        DrawerLayoutImpl,
        NestedScrollingParent,
        NestedScrollingChild {

    private static final String TAG = "ViewDragHelper";

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final ScrollerCompat mScroller;
    private final int mTouchSlop;

    private View mDrawerView;
    private View mContentView;
    private int mTotalOffset;
    private boolean mNestedScrollInProgress;
    private boolean mIsBeingDragged;
    private boolean mInterceptForNonNestedScrollChild;

    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    /**
     * Indicates that any drawers are in an idle, settled state. No animation is in progress.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * Indicates that a drawer is in the process of settling to a final position.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    /**
     * @hide
     */
    @IntDef({LOCK_MODE_UNLOCKED, LOCK_MODE_LOCKED_CLOSED, LOCK_MODE_LOCKED_OPEN,
            LOCK_MODE_UNDEFINED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface LockMode {
    }

    /**
     * The drawer is unlocked.
     */
    public static final int LOCK_MODE_UNLOCKED = 0;

    /**
     * The drawer is locked closed. The user may not open it, though
     * the app may open it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_CLOSED = 1;

    /**
     * The drawer is locked open. The user may not close it, though the app
     * may close it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_OPEN = 2;

    /**
     * The drawer's lock state is reset to default.
     */
    public static final int LOCK_MODE_UNDEFINED = 3;

    /**
     * @hide
     */
    @IntDef({Gravity.BOTTOM, GravityCompat.START})
    @Retention(RetentionPolicy.SOURCE)
    private @interface EdgeGravity {
    }


    private static final int MIN_DRAWER_MARGIN = 64; // dp
    private static final int DRAWER_ELEVATION = 10; //dp

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    /**
     * Length of time to delay before peeking the drawer.
     */
    private static final int PEEK_DELAY = 160; // ms

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * Experimental feature.
     */
    private static final boolean ALLOW_EDGE_LOCK = false;

    private static final boolean CHILDREN_DISALLOW_INTERCEPT = true;

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.layout_gravity
    };

    /**
     * Whether we can use NO_HIDE_DESCENDANTS accessibility importance.
     */
    private static final boolean CAN_HIDE_DESCENDANTS = Build.VERSION.SDK_INT >= 19;

    /**
     * Whether the drawer shadow comes from setting elevation on the drawer.
     */
    private static final boolean SET_DRAWER_SHADOW_FROM_ELEVATION =
            Build.VERSION.SDK_INT >= 21;

    private final ChildAccessibilityDelegate mChildAccessibilityDelegate =
            new ChildAccessibilityDelegate();
    private float mDrawerElevation;

    private int mMinDrawerMargin;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;
    private Paint mScrimPaint = new Paint();

    private int mDrawerState;
    private boolean mInLayout;
    private boolean mFirstLayout = true;

    private
    @LockMode
    int mLockModeLeft = LOCK_MODE_UNDEFINED;
    private
    @LockMode
    int mLockModeRight = LOCK_MODE_UNDEFINED;
    private
    @LockMode
    int mLockModeStart = LOCK_MODE_UNDEFINED;
    private
    @LockMode
    int mLockModeEnd = LOCK_MODE_UNDEFINED;

    private boolean mDisallowInterceptRequested;
    private boolean mChildrenCanceledTouch;

    private
    @Nullable
    DrawerListener mListener;
    private List<DrawerListener> mListeners;

    private float mInitialMotionY;

    private Drawable mStatusBarBackground;
    private Drawable mShadowLeftResolved;
    private Drawable mShadowRightResolved;

    private CharSequence mTitleLeft;
    private CharSequence mTitleRight;

    private Object mLastInsets;
    private boolean mDrawStatusBarBackground;

    /**
     * Shadow drawables for different gravity
     */
    private Drawable mShadowStart = null;
    private Drawable mShadowEnd = null;
    private Drawable mShadowLeft = null;
    private Drawable mShadowRight = null;

    private final ArrayList<View> mNonDrawerViews;

    /**
     * Listener for monitoring events about drawers.
     */
    public interface DrawerListener {
        /**
         * Called when a drawer's position changes.
         *
         * @param drawerView  The child view that was moved
         * @param slideOffset The new offset of this drawer within its range, from 0-1
         */
        public void onDrawerSlide(View drawerView, float slideOffset);

        /**
         * Called when a drawer has settled in a completely open state.
         * The drawer is interactive at this point.
         *
         * @param drawerView Drawer view that is now open
         */
        public void onDrawerOpened(View drawerView);

        /**
         * Called when a drawer has settled in a completely closed state.
         *
         * @param drawerView Drawer view that is now closed
         */
        public void onDrawerClosed(View drawerView);

        /**
         * Called when the drawer motion state changes. The new state will
         * be one of {@link #STATE_IDLE}, {@link #STATE_DRAGGING} or {@link #STATE_SETTLING}.
         *
         * @param newState The new drawer motion state
         */
        public void onDrawerStateChanged(@State int newState);
    }

    /**
     * Stub/no-op implementations of all methods of {@link DrawerListener}.
     * Override this if you only care about a few of the available callback methods.
     */
    public static abstract class SimpleDrawerListener implements DrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(View drawerView) {
        }

        @Override
        public void onDrawerClosed(View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    }

    public DrawerLayout(Context context) {
        this(context, null);
    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        final float density = getResources().getDisplayMetrics().density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // So that we can catch the back button
        setFocusableInTouchMode(true);

        ViewCompat.setImportantForAccessibility(this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate());
        ViewGroupCompat.setMotionEventSplittingEnabled(this, false);

        mDrawerElevation = DRAWER_ELEVATION * density;

        mNonDrawerViews = new ArrayList<View>();


        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mScroller = ScrollerCompat.create(context, sInterpolator);

        setNestedScrollingEnabled(true);
    }

    /**
     * Interpolator defining the animation curve for mScroller
     */
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    /**
     * Sets the base elevation of the drawer(s) relative to the parent, in pixels. Note that the
     * elevation change is only supported in API 21 and above.
     *
     * @param elevation The base depth position of the view, in pixels.
     */
    public void setDrawerElevation(float elevation) {
        mDrawerElevation = elevation;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (isDrawerView(child)) {
                ViewCompat.setElevation(child, mDrawerElevation);
            }
        }
    }

    /**
     * The base elevation of the drawer(s) relative to the parent, in pixels. Note that the
     * elevation change is only supported in API 21 and above. For unsupported API levels, 0 will
     * be returned as the elevation.
     *
     * @return The base depth position of the view, in pixels.
     */
    public float getDrawerElevation() {
        if (SET_DRAWER_SHADOW_FROM_ELEVATION) {
            return mDrawerElevation;
        }
        return 0f;
    }

    /**
     * @hide Internal use only; called to apply window insets when configured
     * with fitsSystemWindows="true"
     */
    @Override
    public void setChildInsets(Object insets, boolean draw) {
        mLastInsets = insets;
        mDrawStatusBarBackground = draw;
        setWillNotDraw(!draw && getBackground() == null);
        requestLayout();
    }

    /**
     * Set a simple drawable used for the left or right shadow. The drawable provided must have a
     * nonzero intrinsic width. For API 21 and above, an elevation will be set on the drawer
     * instead of the drawable provided.
     * <p>
     * <p>Note that for better support for both left-to-right and right-to-left layout
     * directions, a drawable for RTL layout (in additional to the one in LTR layout) can be
     * defined with a resource qualifier "ldrtl" for API 17 and above with the gravity
     * {@link GravityCompat#START}. Alternatively, for API 23 and above, the drawable can
     * auto-mirrored such that the drawable will be mirrored in RTL layout.</p>
     *
     * @param shadowDrawable Shadow drawable to use at the edge of a drawer
     * @param gravity        Which drawer the shadow should apply to
     */
    public void setDrawerShadow(Drawable shadowDrawable, @EdgeGravity int gravity) {
        /*
         * TODO Someone someday might want to set more complex drawables here.
         * They're probably nuts, but we might want to consider registering callbacks,
         * setting states, etc. properly.
         */
        if (SET_DRAWER_SHADOW_FROM_ELEVATION) {
            // No op. Drawer shadow will come from setting an elevation on the drawer.
            return;
        }
        if ((gravity & GravityCompat.START) == GravityCompat.START) {
            mShadowStart = shadowDrawable;
        } else if ((gravity & GravityCompat.END) == GravityCompat.END) {
            mShadowEnd = shadowDrawable;
        } else if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            mShadowLeft = shadowDrawable;
        } else if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            mShadowRight = shadowDrawable;
        } else {
            return;
        }
        resolveShadowDrawables();
        invalidate();
    }

    /**
     * Set a simple drawable used for the left or right shadow. The drawable provided must have a
     * nonzero intrinsic width. For API 21 and above, an elevation will be set on the drawer
     * instead of the drawable provided.
     * <p>
     * <p>Note that for better support for both left-to-right and right-to-left layout
     * directions, a drawable for RTL layout (in additional to the one in LTR layout) can be
     * defined with a resource qualifier "ldrtl" for API 17 and above with the gravity
     * {@link GravityCompat#START}. Alternatively, for API 23 and above, the drawable can
     * auto-mirrored such that the drawable will be mirrored in RTL layout.</p>
     *
     * @param resId   Resource id of a shadow drawable to use at the edge of a drawer
     * @param gravity Which drawer the shadow should apply to
     */
    public void setDrawerShadow(@DrawableRes int resId, @EdgeGravity int gravity) {
        setDrawerShadow(getResources().getDrawable(resId), gravity);
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a drawer is open.
     *
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(@ColorInt int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Set a listener to be notified of drawer events. Note that this method is deprecated
     * and you should use {@link #addDrawerListener(DrawerListener)} to add a listener and
     * {@link #removeDrawerListener(DrawerListener)} to remove a registered listener.
     *
     * @param listener Listener to notify when drawer events occur
     * @see DrawerListener
     * @see #addDrawerListener(DrawerListener)
     * @see #removeDrawerListener(DrawerListener)
     * @deprecated Use {@link #addDrawerListener(DrawerListener)}
     */
    @Deprecated
    public void setDrawerListener(DrawerListener listener) {
        // The logic in this method emulates what we had before support for multiple
        // registered listeners.
        if (mListener != null) {
            removeDrawerListener(mListener);
        }
        if (listener != null) {
            addDrawerListener(listener);
        }
        // Update the deprecated field so that we can remove the passed listener the next
        // time we're called
        mListener = listener;
    }

    /**
     * Adds the specified listener to the list of listeners that will be notified of drawer events.
     *
     * @param listener Listener to notify when drawer events occur.
     * @see #removeDrawerListener(DrawerListener)
     */
    public void addDrawerListener(@NonNull DrawerListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners == null) {
            mListeners = new ArrayList<DrawerListener>();
        }
        mListeners.add(listener);
    }

    /**
     * Removes the specified listener from the list of listeners that will be notified of drawer
     * events.
     *
     * @param listener Listener to remove from being notified of drawer events
     * @see #addDrawerListener(DrawerListener)
     */
    public void removeDrawerListener(@NonNull DrawerListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners == null) {
            // This can happen if this method is called before the first call to addDrawerListener
            return;
        }
        mListeners.remove(listener);
    }

    /**
     * Check the lock mode of the drawer with the given gravity.
     *
     * @param edgeGravity Gravity of the drawer to check
     * @return one of {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED} or
     * {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    @LockMode
    public int getDrawerLockMode(@EdgeGravity int edgeGravity) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);

        switch (edgeGravity) {
            case Gravity.LEFT:
                if (mLockModeLeft != LOCK_MODE_UNDEFINED) {
                    return mLockModeLeft;
                }
                int leftLockMode = (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ?
                        mLockModeStart : mLockModeEnd;
                if (leftLockMode != LOCK_MODE_UNDEFINED) {
                    return leftLockMode;
                }
                break;
            case Gravity.RIGHT:
                if (mLockModeRight != LOCK_MODE_UNDEFINED) {
                    return mLockModeRight;
                }
                int rightLockMode = (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ?
                        mLockModeEnd : mLockModeStart;
                if (rightLockMode != LOCK_MODE_UNDEFINED) {
                    return rightLockMode;
                }
                break;
            case GravityCompat.START:
                if (mLockModeStart != LOCK_MODE_UNDEFINED) {
                    return mLockModeStart;
                }
                int startLockMode = (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ?
                        mLockModeLeft : mLockModeRight;
                if (startLockMode != LOCK_MODE_UNDEFINED) {
                    return startLockMode;
                }
                break;
            case GravityCompat.END:
                if (mLockModeEnd != LOCK_MODE_UNDEFINED) {
                    return mLockModeEnd;
                }
                int endLockMode = (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ?
                        mLockModeRight : mLockModeLeft;
                if (endLockMode != LOCK_MODE_UNDEFINED) {
                    return endLockMode;
                }
                break;
        }

        return LOCK_MODE_UNLOCKED;
    }

    /**
     * Check the lock mode of the given drawer view.
     *
     * @param drawerView Drawer view to check lock mode
     * @return one of {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED} or
     * {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    @LockMode
    public int getDrawerLockMode(View drawerView) {
        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a drawer");
        }
        final int drawerGravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return getDrawerLockMode(drawerGravity);
    }

    /**
     * Sets the title of the drawer with the given gravity.
     * <p>
     * When accessibility is turned on, this is the title that will be used to
     * identify the drawer to the active accessibility service.
     *
     * @param edgeGravity Gravity.LEFT, RIGHT, START or END. Expresses which
     *                    drawer to set the title for.
     * @param title       The title for the drawer.
     */
    public void setDrawerTitle(@EdgeGravity int edgeGravity, CharSequence title) {
        final int absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this));
        if (absGravity == Gravity.LEFT) {
            mTitleLeft = title;
        } else if (absGravity == Gravity.RIGHT) {
            mTitleRight = title;
        }
    }

    /**
     * Returns the title of the drawer with the given gravity.
     *
     * @param edgeGravity Gravity.LEFT, RIGHT, START or END. Expresses which
     *                    drawer to return the title for.
     * @return The title of the drawer, or null if none set.
     * @see #setDrawerTitle(int, CharSequence)
     */
    @Nullable
    public CharSequence getDrawerTitle(@EdgeGravity int edgeGravity) {
        final int absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this));
        if (absGravity == Gravity.LEFT) {
            return mTitleLeft;
        } else if (absGravity == Gravity.RIGHT) {
            return mTitleRight;
        }
        return null;
    }

    private void updateChildrenImportantForAccessibility(View drawerView, boolean isDrawerOpen) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (!isDrawerOpen && !isDrawerView(child)
                    || isDrawerOpen && child == drawerView) {
                // Drawer is closed and this is a content view or this is an
                // open drawer view, so it should be visible.
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            } else {
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            }
        }
    }

    void dispatchOnDrawerSlide(View drawerView, float slideOffset) {
        if (mListeners != null) {
            // Notify the listeners. Do that from the end of the list so that if a listener
            // removes itself as the result of being called, it won't mess up with our iteration
            int listenerCount = mListeners.size();
            for (int i = listenerCount - 1; i >= 0; i--) {
                mListeners.get(i).onDrawerSlide(drawerView, slideOffset);
            }
        }
    }

    void setDrawerViewOffset(float slideOffset) {
        final LayoutParams lp = (LayoutParams) mDrawerView.getLayoutParams();
        if (slideOffset == lp.onScreen) {
            return;
        }

        if (slideOffset == 1.0f) lp.openState = LayoutParams.FLAG_IS_OPENED;
        if (slideOffset == 0.0f) lp.openState = LayoutParams.FLAG_IS_CLOSED;

        lp.onScreen = slideOffset;
        dispatchOnDrawerSlide(mDrawerView, slideOffset);
    }

    float getDrawerViewOffset(View drawerView) {
        return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
    }

    /**
     * @return the absolute gravity of the child drawerView, resolved according
     * to the current layout direction
     */
    int getDrawerViewAbsoluteGravity(View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));
    }

    boolean checkDrawerViewAbsoluteGravity(View drawerView, int checkFor) {
        final int absGravity = getDrawerViewAbsoluteGravity(drawerView);
        return (absGravity & checkFor) == checkFor;
    }

    View findOpenDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
            if ((childLp.openState & LayoutParams.FLAG_IS_OPENED) == 1) {
                return child;
            }
        }
        return null;
    }

    /**
     * @param gravity the gravity of the child to return. If specified as a
     *                relative value, it will be resolved according to the current
     *                layout direction.
     * @return the drawer with the specified gravity
     */
    View findDrawerWithGravity(int gravity) {
        final int absVerticalGravity = GravityCompat.getAbsoluteGravity(
                gravity, ViewCompat.getLayoutDirection(this)) & Gravity.VERTICAL_GRAVITY_MASK;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int childAbsGravity = getDrawerViewAbsoluteGravity(child);
            if ((childAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) == absVerticalGravity) {
                return child;
            }
        }
        return null;
    }

    /**
     * Simple gravity to string - only supports LEFT and RIGHT for debugging output.
     *
     * @param gravity Absolute gravity value
     * @return LEFT or RIGHT as appropriate, or a hex string
     */
    static String gravityToString(@EdgeGravity int gravity) {
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            return "LEFT";
        }
        if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return "RIGHT";
        }
        return Integer.toHexString(gravity);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Consume all of the space if specified
                // or pick a magic number from thin air otherwise.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (widthMode == MeasureSpec.AT_MOST) {
                    widthMode = MeasureSpec.EXACTLY;
                } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthMode = MeasureSpec.EXACTLY;
                    widthSize = 300;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    heightMode = MeasureSpec.EXACTLY;
                } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightMode = MeasureSpec.EXACTLY;
                    heightSize = 300;
                }
            } else {
                throw new IllegalArgumentException(
                        "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }

        setMeasuredDimension(widthSize, heightSize);

        // Only one drawer is permitted along each vertical edge (left / right). These two booleans
        // are tracking the presence of the edge drawers.
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mContentView == child) {
                // Content views get measured at exactly the layout's size.
                final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                        widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
                final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                        heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else if (mDrawerView == child) {
                if (SET_DRAWER_SHADOW_FROM_ELEVATION) {
                    if (ViewCompat.getElevation(child) != mDrawerElevation) {
                        ViewCompat.setElevation(child, mDrawerElevation);
                    }
                }
                final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                        lp.leftMargin + lp.rightMargin,
                        lp.width);
                final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                        mMinDrawerMargin + lp.topMargin + lp.bottomMargin,
                        lp.height);
                child.measure(drawerWidthSpec, drawerHeightSpec);
            } else {
                throw new IllegalStateException("Child " + child + " at index " + i +
                        " does not have a valid layout_gravity - must be Gravity.LEFT, " +
                        "Gravity.RIGHT or Gravity.NO_GRAVITY");
            }
        }
    }

    private void resolveShadowDrawables() {
        if (SET_DRAWER_SHADOW_FROM_ELEVATION) {
            return;
        }
        mShadowLeftResolved = resolveLeftShadow();
        mShadowRightResolved = resolveRightShadow();
    }

    private Drawable resolveLeftShadow() {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        // Prefer shadows defined with start/end gravity over left and right.
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
            if (mShadowStart != null) {
                // Correct drawable layout direction, if needed.
                mirror(mShadowStart, layoutDirection);
                return mShadowStart;
            }
        } else {
            if (mShadowEnd != null) {
                // Correct drawable layout direction, if needed.
                mirror(mShadowEnd, layoutDirection);
                return mShadowEnd;
            }
        }
        return mShadowLeft;
    }

    private Drawable resolveRightShadow() {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
            if (mShadowEnd != null) {
                // Correct drawable layout direction, if needed.
                mirror(mShadowEnd, layoutDirection);
                return mShadowEnd;
            }
        } else {
            if (mShadowStart != null) {
                // Correct drawable layout direction, if needed.
                mirror(mShadowStart, layoutDirection);
                return mShadowStart;
            }
        }
        return mShadowRight;
    }

    /**
     * Change the layout direction of the given drawable.
     * Return true if auto-mirror is supported and drawable's layout direction can be changed.
     * Otherwise, return false.
     */
    private boolean mirror(Drawable drawable, int layoutDirection) {
        if (drawable == null || !DrawableCompat.isAutoMirrored(drawable)) {
            return false;
        }

        DrawableCompat.setLayoutDirection(drawable, layoutDirection);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        final int width = r - l;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mContentView == child) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
            } else { // Drawer, if it wasn't onMeasure would have thrown an exception.
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                int childTop = getHeight() - Math.round(childHeight * lp.onScreen);
                float newOffset = (float) (childWidth + childTop) / childWidth;

                final boolean changeOffset = newOffset != lp.onScreen;

                child.layout(lp.leftMargin, childTop,
                        lp.leftMargin + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());

//                if (changeOffset) {
//                    setDrawerViewOffset(child, newOffset);
//                }

                final int newVisibility = lp.onScreen > 0 ? VISIBLE : INVISIBLE;
                if (child.getVisibility() != newVisibility) {
                    child.setVisibility(newVisibility);
                }
            }
        }
        mInLayout = false;
        mFirstLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = ((LayoutParams) mDrawerView.getLayoutParams()).onScreen;

        if (mScroller.computeScrollOffset()) {
            final int dy = mScroller.getCurrY() - mDrawerView.getTop();
            if (dy != 0) {
                moveDrawerVertical(dy);
            }
        }
    }

    /**
     * 纵向移动抽屉
     *
     * @param dy
     */
    private void moveDrawerVertical(int dy) {
        ViewCompat.offsetTopAndBottom(mDrawerView, dy);
        float offset = (getHeight() - mDrawerView.getTop()) * 1f / mDrawerView.getHeight();
        setDrawerViewOffset(offset);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private static boolean hasOpaqueBackground(View v) {
        final Drawable bg = v.getBackground();
        if (bg != null) {
            return bg.getOpacity() == PixelFormat.OPAQUE;
        }
        return false;
    }

    /**
     * Set a drawable to draw in the insets area for the status bar.
     * Note that this will only be activated if this DrawerLayout fitsSystemWindows.
     *
     * @param bg Background drawable to draw behind the status bar
     */
    public void setStatusBarBackground(Drawable bg) {
        mStatusBarBackground = bg;
        invalidate();
    }

    /**
     * Gets the drawable used to draw in the insets area for the status bar.
     *
     * @return The status bar background drawable, or null if none set
     */
    public Drawable getStatusBarBackgroundDrawable() {
        return mStatusBarBackground;
    }

    /**
     * Set a drawable to draw in the insets area for the status bar.
     * Note that this will only be activated if this DrawerLayout fitsSystemWindows.
     *
     * @param resId Resource id of a background drawable to draw behind the status bar
     */
    public void setStatusBarBackground(int resId) {
        mStatusBarBackground = resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null;
        invalidate();
    }

    /**
     * Set a drawable to draw in the insets area for the status bar.
     * Note that this will only be activated if this DrawerLayout fitsSystemWindows.
     *
     * @param color Color to use as a background drawable to draw behind the status bar
     *              in 0xAARRGGBB format.
     */
    public void setStatusBarBackgroundColor(@ColorInt int color) {
        mStatusBarBackground = new ColorDrawable(color);
        invalidate();
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        resolveShadowDrawables();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (mDrawStatusBarBackground && mStatusBarBackground != null) {
            mStatusBarBackground.setBounds(0, 0, getWidth(), getHeight());
            mStatusBarBackground.draw(c);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int height = getHeight();
        final boolean drawingContent = child == mContentView;
        int clipLeft = 0, clipRight = getWidth();

        final int restoreCount = canvas.save();
        if (drawingContent) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View v = getChildAt(i);
                if (v == child || v.getVisibility() != VISIBLE ||
                        !hasOpaqueBackground(v) || !isDrawerView(v) ||
                        v.getHeight() < height) {
                    continue;
                }

                if (checkDrawerViewAbsoluteGravity(v, Gravity.LEFT)) {
                    final int vright = v.getRight();
                    if (vright > clipLeft) clipLeft = vright;
                } else {
                    final int vleft = v.getLeft();
                    if (vleft < clipRight) clipRight = vleft;
                }
            }
            canvas.clipRect(clipLeft, 0, clipRight, getHeight());
        }
        final boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(restoreCount);

        if (mScrimOpacity > 0 && drawingContent) {
            final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
            final int imag = (int) (baseAlpha * mScrimOpacity);
            final int color = imag << 24 | (mScrimColor & 0xffffff);
            mScrimPaint.setColor(color);

            canvas.drawRect(clipLeft, 0, clipRight, getHeight(), mScrimPaint);
        } /*else if (mShadowLeftResolved != null
                && checkDrawerViewAbsoluteGravity(child, Gravity.BOTTOM)) {
            final int shadowWidth = mShadowLeftResolved.getIntrinsicWidth();
            final int childRight = child.getRight();
            final int drawerPeekDistance = mDragger.getEdgeSize();
            final float alpha =
                    Math.max(0, Math.min((float) childRight / drawerPeekDistance, 1.f));
            mShadowLeftResolved.setBounds(childRight, child.getTop(),
                    childRight + shadowWidth, child.getBottom());
            mShadowLeftResolved.setAlpha((int) (0xff * alpha));
            mShadowLeftResolved.draw(canvas);
        }*/
        return result;
    }

    boolean isContentView(View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }

    boolean isDrawerView(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(child));

        return (absGravity & Gravity.BOTTOM) != 0;
    }

    private boolean isDrawerViewOrChild(View child) {
        if (child == mDrawerView) return true;

        if (mDrawerView instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) mDrawerView;
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                if (parent.getChildAt(i) == child) return true;
            }
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        boolean interceptForTap = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionY = y;
                if (mScrimOpacity > 0) {
                    final View child = findTopChildUnder(this, (int) x, (int) y);
                    if (child != null && child == mContentView) {
                        interceptForTap = true;
                    } else if (child != null && child == mDrawerView && child instanceof ViewGroup) {
                        int insetX = (int) (x - child.getLeft());
                        int insetY = (int) (y - child.getTop());
                        View topChild = findTopChildUnder(child, insetX, insetY);
                        //点击到mDrawerView上不能嵌套滚动的View
                        if (null != topChild && !ViewCompat.isNestedScrollingEnabled(topChild)) {
                            mInterceptForNonNestedScrollChild = true;
                            return true;
                        }
                    }
                }
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float dy = ev.getY() - mInitialMotionY;
                if (Math.abs(dy) > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        boolean interceptForTap = false;
        mInterceptForNonNestedScrollChild = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionY = y;
                if (mScrimOpacity > 0) {
                    final View child = findTopChildUnder(this, (int) x, (int) y);
                    if (child != null && child == mContentView) {
                        interceptForTap = true;
                    } else if (child != null && child == mDrawerView && child instanceof ViewGroup) {
                        int insetX = (int) (x - child.getLeft());
                        int insetY = (int) (y - child.getTop());
                        View topChild = findTopChildUnder(child, insetX, insetY);
                        //点击到mDrawerView上不能嵌套滚动的View
                        if (null != topChild && !ViewCompat.isNestedScrollingEnabled(topChild)) {
                            mInterceptForNonNestedScrollChild = true;
                            return true;
                        }
                    }
                }
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float dy = ev.getY() - mInitialMotionY;
                if (Math.abs(dy) > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                Log.i(TAG, "onInterceptTouchEvent: " + mIsBeingDragged);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                mInterceptForNonNestedScrollChild = false;
            }
        }

        return interceptForTap || mIsBeingDragged;
    }

    float lastTouchY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                lastTouchY = ev.getY();

                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                mIsBeingDragged = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.i(TAG, "onTouchEvent: " + mIsBeingDragged);
                if (mIsBeingDragged) {
                    int dy = Math.round(ev.getY() - lastTouchY);
                    moveDrawerVertical(dy);
                    lastTouchY = ev.getY();
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                mIsBeingDragged = false;
                break;
            }
        }

        return mInterceptForNonNestedScrollChild;
    }

    View findTopChildUnder(View view, int x, int y) {
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            final int childCount = parent.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = parent.getChildAt(i);
                if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
                    return child;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isDrawerViewOrChild(child) && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    @Override
    public void onStopNestedScroll(View child) {

        if (mNestedScrollInProgress && mTotalOffset != 0) {
            boolean openDrawer = -mTotalOffset <= mDrawerView.getHeight() * 0.4f;
            if (openDrawer) {
                openDrawer();
            } else {
                closeDrawer();
            }
            mTotalOffset = 0;
        }

        mNestedScrollInProgress = false;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if ((mTotalOffset + dyUnconsumed) <= 0) {
            moveDrawerVertical(-dyUnconsumed);
            mTotalOffset += dyUnconsumed;
        } else {
            moveDrawerVertical(mTotalOffset);
            mTotalOffset = 0;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (CHILDREN_DISALLOW_INTERCEPT) {
            // If we have an edge touch we want to skip this and track it for later instead.
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
        mDisallowInterceptRequested = disallowIntercept;
    }

    /**
     * Open the specified drawer view by animating it into view.
     */
    public void openDrawer() {
        openDrawer(mDrawerView, true);
    }

    /**
     * Open the specified drawer view.
     *
     * @param drawerView Drawer view to open
     * @param animate    Whether opening of the drawer should be animated.
     */
    private void openDrawer(View drawerView, boolean animate) {
        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }

        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (mFirstLayout) {
            lp.onScreen = 1.f;
            lp.openState = LayoutParams.FLAG_IS_OPENED;

            updateChildrenImportantForAccessibility(drawerView, true);
        } else if (animate) {
            lp.openState = LayoutParams.FLAG_IS_OPENING;
            int top = getHeight() - drawerView.getHeight();

            if (drawerView.getVisibility() != VISIBLE) drawerView.setVisibility(VISIBLE);

            startScroll(top);
        }
        invalidate();
    }

    /**
     * Close the specified drawer view.
     *
     * @param drawerView Drawer view to close
     * @param animate    Whether closing of the drawer should be animated.
     */
    private void closeDrawer(View drawerView, boolean animate) {

        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (mFirstLayout) {
            lp.onScreen = 0.f;
            lp.openState = 0;
        } else if (animate) {
            lp.openState = LayoutParams.FLAG_IS_CLOSING;
            startScroll(getHeight());
        }
        invalidate();
    }

    void startScroll(int finalTop) {
        final int startTop = mDrawerView.getTop();
        final int dy = finalTop - startTop;
        mScroller.startScroll(mDrawerView.getLeft(), startTop, 0, dy, 300);
    }

    /**
     * Close the specified drawer by animating it out of view.
     */
    public void closeDrawer() {

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        Log.i(TAG, "closeDrawer: " + stackTrace[1]);
        Log.i(TAG, "closeDrawer: " + stackTrace[2]);

        closeDrawer(true);
    }

    /**
     * Close the specified drawer.
     *
     * @param animate Whether closing of the drawer should be animated.
     */
    public void closeDrawer(boolean animate) {
        closeDrawer(mDrawerView, animate);
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. If there is no drawer with the given gravity this method
     * will return false.
     *
     * @return true if the given drawer view is in an open state
     */
    public boolean isDrawerOpen() {
        LayoutParams drawerLp = (LayoutParams) mDrawerView.getLayoutParams();
        return drawerLp.openState == LayoutParams.FLAG_IS_OPENED;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere inbetween.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer is visible on-screen
     */
    public boolean isDrawerVisible(View drawer) {
        if (!isDrawerView(drawer)) {
            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        return ((LayoutParams) drawer.getLayoutParams()).onScreen > 0;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere in between.
     * If there is no drawer with the given gravity this method will return false.
     *
     * @param drawerGravity Gravity of the drawer to check
     * @return true if the given drawer is visible on-screen
     */
    public boolean isDrawerVisible(@EdgeGravity int drawerGravity) {
        final View drawerView = findDrawerWithGravity(drawerGravity);
        if (drawerView != null) {
            return isDrawerVisible(drawerView);
        }
        return false;
    }

    private boolean hasPeekingDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.isPeeking) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (getDescendantFocusability() == FOCUS_BLOCK_DESCENDANTS) {
            return;
        }

        // Only the views in the open drawers are focusables. Add normal child views when
        // no drawers are opened.
        final int childCount = getChildCount();
        boolean isDrawerOpen = false;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (isDrawerView(child)) {
                if (isDrawerOpen()) {
                    isDrawerOpen = true;
                    child.addFocusables(views, direction, focusableMode);
                }
            } else {
                mNonDrawerViews.add(child);
            }
        }

        if (!isDrawerOpen) {
            final int nonDrawerViewsCount = mNonDrawerViews.size();
            for (int i = 0; i < nonDrawerViewsCount; ++i) {
                final View child = mNonDrawerViews.get(i);
                if (child.getVisibility() == View.VISIBLE) {
                    child.addFocusables(views, direction, focusableMode);
                }
            }
        }

        mNonDrawerViews.clear();
    }

    private boolean hasVisibleDrawer() {
        return findVisibleDrawer() != null;
    }

    private View findVisibleDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (isDrawerView(child) && isDrawerVisible(child)) {
                return child;
            }
        }
        return null;
    }

    void cancelChildViewTouch() {
        // Cancel child touches
        if (!mChildrenCanceledTouch) {
            final long now = SystemClock.uptimeMillis();
            final MotionEvent cancelEvent = MotionEvent.obtain(now, now,
                    MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).dispatchTouchEvent(cancelEvent);
            }
            cancelEvent.recycle();
            mChildrenCanceledTouch = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && hasVisibleDrawer()) {
            KeyEventCompat.startTracking(event);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final View visibleDrawer = findVisibleDrawer();
            if (visibleDrawer != null && getDrawerLockMode(visibleDrawer) == LOCK_MODE_UNLOCKED) {
                closeDrawer();
            }
            return visibleDrawer != null;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        final View openDrawer = findOpenDrawer();
        if (openDrawer != null || isDrawerView(child)) {
            // A drawer is already open or the new view is a drawer, so the
            // new view should start out hidden.
            ViewCompat.setImportantForAccessibility(child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        } else {
            // Otherwise this is a content view and no drawer is open, so the
            // new view should start out visible.
            ViewCompat.setImportantForAccessibility(child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        // We only need a delegate here if the framework doesn't understand
        // NO_HIDE_DESCENDANTS importance.
        if (!CAN_HIDE_DESCENDANTS) {
            ViewCompat.setAccessibilityDelegate(child, mChildAccessibilityDelegate);
        }
    }

    private static boolean includeChildForAccessibility(View child) {
        // If the child is not important for accessibility we make
        // sure this hides the entire subtree rooted at it as the
        // IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDATS is not
        // supported on older platforms but we want to hide the entire
        // content and not opened drawers if a drawer is opened.
        return ViewCompat.getImportantForAccessibility(child)
                != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                && ViewCompat.getImportantForAccessibility(child)
                != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO;
    }

    /**
     * @return child在竖直方向上是否能向上滚动
     */
    public boolean canChildScrollUp(View target) {

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(target, -1) || target.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, -1);
        }
    }

    public boolean canChildScrollDown(View target) {

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                int count = absListView.getAdapter().getCount();
                int fristPos = absListView.getFirstVisiblePosition();
                if (fristPos == 0 && absListView.getChildAt(0).getTop() >= absListView.getPaddingTop()) {
                    return false;
                }
                int lastPos = absListView.getLastVisiblePosition();
                return lastPos > 0 && count > 0 && lastPos == count - 1;
            } else {
                return ViewCompat.canScrollVertically(target, 1) || target.getScrollY() < 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, 1);
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        private static final int FLAG_IS_OPENED = 0x1;
        private static final int FLAG_IS_OPENING = 0x2;
        private static final int FLAG_IS_CLOSED = 0x3;
        private static final int FLAG_IS_CLOSING = 0x4;

        public int gravity = Gravity.NO_GRAVITY;
        private float onScreen;
        private boolean isPeeking;
        private int openState;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

    class AccessibilityDelegate extends AccessibilityDelegateCompat {
        private final Rect mTmpRect = new Rect();

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            if (CAN_HIDE_DESCENDANTS) {
                super.onInitializeAccessibilityNodeInfo(host, info);
            } else {
                // Obtain a node for the host, then manually generate the list
                // of children to only include non-obscured views.
                final AccessibilityNodeInfoCompat superNode =
                        AccessibilityNodeInfoCompat.obtain(info);
                super.onInitializeAccessibilityNodeInfo(host, superNode);

                info.setSource(host);
                final ViewParent parent = ViewCompat.getParentForAccessibility(host);
                if (parent instanceof View) {
                    info.setParent((View) parent);
                }
                copyNodeInfoNoChildren(info, superNode);
                superNode.recycle();

                addChildrenForAccessibility(info, (ViewGroup) host);
            }

            info.setClassName(DrawerLayout.class.getName());

            // This view reports itself as focusable so that it can intercept
            // the back button, but we should prevent this view from reporting
            // itself as focusable to accessibility services.
            info.setFocusable(false);
            info.setFocused(false);
            info.removeAction(AccessibilityActionCompat.ACTION_FOCUS);
            info.removeAction(AccessibilityActionCompat.ACTION_CLEAR_FOCUS);
        }

        @Override
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);

            event.setClassName(DrawerLayout.class.getName());
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            // Special case to handle window state change events. As far as
            // accessibility services are concerned, state changes from
            // DrawerLayout invalidate the entire contents of the screen (like
            // an Activity or Dialog) and they should announce the title of the
            // new content.
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                final List<CharSequence> eventText = event.getText();
                final View visibleDrawer = findVisibleDrawer();
                if (visibleDrawer != null) {
                    final int edgeGravity = getDrawerViewAbsoluteGravity(visibleDrawer);
                    final CharSequence title = getDrawerTitle(edgeGravity);
                    if (title != null) {
                        eventText.add(title);
                    }
                }

                return true;
            }

            return super.dispatchPopulateAccessibilityEvent(host, event);
        }

        @Override
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                                                       AccessibilityEvent event) {
            if (CAN_HIDE_DESCENDANTS || includeChildForAccessibility(child)) {
                return super.onRequestSendAccessibilityEvent(host, child, event);
            }
            return false;
        }

        private void addChildrenForAccessibility(AccessibilityNodeInfoCompat info, ViewGroup v) {
            final int childCount = v.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = v.getChildAt(i);
                if (includeChildForAccessibility(child)) {
                    info.addChild(child);
                }
            }
        }

        /**
         * This should really be in AccessibilityNodeInfoCompat, but there unfortunately
         * seem to be a few elements that are not easily cloneable using the underlying API.
         * Leave it private here as it's not general-purpose useful.
         */
        private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat dest,
                                            AccessibilityNodeInfoCompat src) {
            final Rect rect = mTmpRect;

            src.getBoundsInParent(rect);
            dest.setBoundsInParent(rect);

            src.getBoundsInScreen(rect);
            dest.setBoundsInScreen(rect);

            dest.setVisibleToUser(src.isVisibleToUser());
            dest.setPackageName(src.getPackageName());
            dest.setClassName(src.getClassName());
            dest.setContentDescription(src.getContentDescription());

            dest.setEnabled(src.isEnabled());
            dest.setClickable(src.isClickable());
            dest.setFocusable(src.isFocusable());
            dest.setFocused(src.isFocused());
            dest.setAccessibilityFocused(src.isAccessibilityFocused());
            dest.setSelected(src.isSelected());
            dest.setLongClickable(src.isLongClickable());

            dest.addAction(src.getActions());
        }
    }

    @Override
    protected void onFinishInflate() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {

            if (null != mDrawerView && null != mContentView) break;

            View child = getChildAt(i);
            if (null == mDrawerView && isDrawerView(child)) {
                mDrawerView = child;
            } else if (null == mContentView && isContentView(child)) {
                mContentView = child;
            }
        }
    }

    final class ChildAccessibilityDelegate extends AccessibilityDelegateCompat {
        @Override
        public void onInitializeAccessibilityNodeInfo(View child,
                                                      AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(child, info);

            if (!includeChildForAccessibility(child)) {
                // If we are ignoring the sub-tree rooted at the child,
                // break the connection to the rest of the node tree.
                // For details refer to includeChildForAccessibility.
                info.setParent(null);
            }
        }
    }

}
