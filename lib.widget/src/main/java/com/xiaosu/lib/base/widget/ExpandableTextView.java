/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright 2014 Manabu Shimobe
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

package com.xiaosu.lib.base.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ExpandableTextView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = ExpandableTextView.class.getSimpleName();

    private static final int MAX_COLLAPSED_LINES = 8;

    private static final int DEFAULT_ANIM_DURATION = 300;

    protected TextView mTextView;

    protected CompoundButton mButton; // 展开和收起的按钮

    private boolean reLayout = true;//是否需要重新测量&布局

    private boolean mCollapsed = true; // 默认折叠

    private int mMaxCollapsedLines;

    private int mAnimationDuration;

    private boolean mAnimating;//动画正在执行

    /* Listener for callback */
    private OnExpandStateChangeListener mListener;

    private int mTextFullHeight = -1;
    private int mTextSmallHeight = -1;
    private String mCollapsedText;
    private String mExpandedText;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    @Override
    public void onClick(View view) {
        if (mButton.getVisibility() != View.VISIBLE) {
            return;
        }

        clearAnimation();

        Animation animation;
        if (mCollapsed) {
            animation = new ExpandCollapseAnimation(mTextView, mTextSmallHeight, mTextFullHeight);
        } else {
            animation = new ExpandCollapseAnimation(mTextView, mTextFullHeight, mTextSmallHeight);
        }

        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mAnimating = true;
                        mTextView.setMaxLines(Integer.MAX_VALUE);
                    }
                });
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mCollapsed = !mCollapsed;
                        mButton.setChecked(mCollapsed);
                        if (mCollapsed)
                            mTextView.setMaxLines(mMaxCollapsedLines);
                        refreshButtonText();
                        // clear animation here to avoid repeated applyTransformation() calls
                        clearAnimation();
                        // clear the animation flag
                        mAnimating = false;

                        // notify the listener
                        if (mListener != null) {
                            mListener.onExpandStateChanged(mTextView, !mCollapsed);
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        startAnimation(animation);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // while an animation is in progress, intercept all the touch events to children to
        // prevent extra clicks during the animation
        return mAnimating;
    }

    @Override
    protected void onFinishInflate() {
        findViews();

        refreshButtonText();
    }

    private void refreshButtonText() {
        if (!TextUtils.isEmpty(mCollapsedText) && mCollapsed) {
            mButton.setText(mCollapsedText);
        } else if (!TextUtils.isEmpty(mExpandedText) && !mCollapsed) {
            mButton.setText(mExpandedText);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mAnimating || !reLayout) {
            //动画执行期间，只进行一次测量
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        mButton.setVisibility(GONE);
        mTextView.setMaxLines(Integer.MAX_VALUE);
        // Measure
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 如果显示行数不大于mMaxCollapsedLines，则不需要折叠
        if (mTextView.getLineCount() <= mMaxCollapsedLines) {
            return;
        }

        //走到这里说明文字可能需要折叠，显示mButton
        mButton.setVisibility(VISIBLE);

        // 获取TextView完全展开高度
        if (mTextFullHeight == -1 || reLayout)
            mTextFullHeight = mTextView.getMeasuredHeight();

        // 重新测量获取TextView折叠的高度
        if (mTextSmallHeight == -1 || reLayout) {
            mTextView.setMaxLines(mMaxCollapsedLines);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mTextSmallHeight = mTextView.getMeasuredHeight();
        }

        //默认选择展开状态
        if (!mCollapsed) {
            mTextView.setMaxLines(Integer.MAX_VALUE);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        reLayout = false;//标记测量完毕
    }

    public void setOnExpandStateChangeListener(@Nullable OnExpandStateChangeListener listener) {
        mListener = listener;
    }

    public void setText(@Nullable CharSequence text) {
        reLayout = true;
        mTextView.setText(text);
        requestLayout();
    }

    @Nullable
    public CharSequence getText() {
        if (mTextView == null) {
            return "";
        }
        return mTextView.getText();
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mMaxCollapsedLines = a.getInt(R.styleable.ExpandableTextView_maxCollapsedLines, MAX_COLLAPSED_LINES);
        mAnimationDuration = a.getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION);
        mCollapsed = a.getBoolean(R.styleable.ExpandableTextView_collapsed, true);

        if (a.hasValue(R.styleable.ExpandableTextView_indicatorText)) {
            String indicatorText = a.getString(R.styleable.ExpandableTextView_indicatorText);
            if (null != indicatorText && indicatorText.contains("&")) {
                String[] split = indicatorText.split("&");
                try {
                    mCollapsedText = split[0];
                    mExpandedText = split[1];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        a.recycle();

        // enforces vertical orientation
        setOrientation(LinearLayout.VERTICAL);
    }

    private void findViews() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            //不再需要循环
            if (null != mTextView && null != mButton) return;

            View child = getChildAt(i);

            if (null == mButton && child instanceof CompoundButton) {
                mButton = (CompoundButton) child;
                mButton.setChecked(mCollapsed);
                mButton.setOnClickListener(this);
            }

            if (null == mTextView && child instanceof TextView) {
                mTextView = (TextView) child;
                mTextView.setOnClickListener(this);
            }
        }
    }

    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    private static boolean isPostLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha) {
        if (isPostHoneycomb()) {
            view.setAlpha(alpha);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
            // make it instant
            alphaAnimation.setDuration(0);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        Resources resources = context.getResources();
        if (isPostLollipop()) {
            return resources.getDrawable(resId, context.getTheme());
        } else {
            return resources.getDrawable(resId);
        }
    }

    private static int getRealTextViewHeight(@NonNull TextView textView) {
        int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return textHeight + padding;
    }

    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mTargetView.getLayoutParams().height = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            mTargetView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public interface OnExpandStateChangeListener {
        /**
         * Called when the expand/collapse animation has been finished
         *
         * @param textView   - TextView being expanded/collapsed
         * @param isExpanded - true if the TextView has been expanded
         */
        void onExpandStateChanged(TextView textView, boolean isExpanded);
    }
}