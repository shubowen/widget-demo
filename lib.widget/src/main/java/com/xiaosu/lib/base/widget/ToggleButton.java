package com.xiaosu.lib.base.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * 开关控件
 */
public class ToggleButton extends View {

    private static final String TAG = "ToggleButton";

    private Paint mBorderPaint;
    private int mBorderColor;
    private float mBorderWidth;
    private float mBorderRadius;

    private RectF mBound;
    private Paint mBackgroundPaint;
    private RectF mBordBound;
    private RectF mBackgroundBound;
    private int mToggleColor;
    private Paint mTogglePaint;
    private int mCenterX;
    private int mCenterY;
    private float mRadius;
    private String mLeftText;
    private Paint mTextPaint;
    private float mLeftTextY;
    private float mTextSize;
    private String mRightText;
    private float mLeftTextX;
    private float mRightTextX;
    private float mRightTextY;
    private GestureDetectorCompat mGestureDetector;

    private float mToggleX;
    private float mToggleStartX;
    private float mToggleEndX;

    private RectF mOffBound = new RectF();

    private boolean mAnimationRunning;
    private boolean mToggleOn = false;

    private Paint mOffPaint;
    private ColorStateList mBackgroundColor;
    private ColorStateList mTextColor;
    private int mTextSelectedColor;
    private int mTextDefaultColor;

    private OnStateChangedListener mOnStateChangedListener;

    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if (mAnimationRunning) return false;

            ValueAnimator animator = mToggleOn ?
                    ValueAnimator.ofFloat(1, 0) : ValueAnimator.ofFloat(0, 1);

            animator.setDuration(700);
            animator.setInterpolator(new SpringInterpolator(0.92f));
            animator.addUpdateListener(mAnimatorUpdateListener);
            animator.addListener(mAnimatorListener);
            animator.start();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            return true;
        }
    };

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float fraction = (float) animation.getAnimatedValue();
            mToggleX = evaluate(fraction, mToggleStartX, mToggleEndX);

            float offTop = evaluate(fraction, mBordBound.top, mCenterY);
            float offBottom = evaluate(fraction, mBordBound.bottom, mCenterY);
            float offRightX = evaluate(fraction, mBordBound.right, mToggleEndX);

            mOffBound.set(mToggleX - mRadius, offTop, offRightX, offBottom);

            if (mToggleX <= mToggleStartX && mToggleOn) {
                mToggleOn = false;
                if (null != mOnStateChangedListener) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mOnStateChangedListener.toggle(false, ToggleButton.this);
                        }
                    });
                }
            } else if (mToggleX >= mToggleEndX && !mToggleOn) {
                mToggleOn = true;
                if (null != mOnStateChangedListener) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mOnStateChangedListener.toggle(true, ToggleButton.this);
                        }
                    });
                }
            }

            postInvalidate();
        }
    };

    private Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimationRunning = false;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mAnimationRunning = true;
        }
    };

    public ToggleButton(Context context) {
        this(context, null);
        init(context);
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
        init(context);
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(context, attrs);
        init(context);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ToggleButton);

        mBorderColor = array.getColor(R.styleable.ToggleButton_tb_borderColor,
                getColor(R.color.toggle_bord_color));
        mToggleColor = array.getColor(R.styleable.ToggleButton_tb_toggleColor,
                getColor(R.color.toggle_toggle_color));

        mBackgroundColor = array.getColorStateList(R.styleable.ToggleButton_tb_backgroundColor);
        mTextColor = array.getColorStateList(R.styleable.ToggleButton_android_textColor);

        mBorderWidth = array.getDimensionPixelSize(R.styleable.ToggleButton_tb_borderWidth, getDimension(R.dimen.toggle_border_width));
        mBorderRadius = array.getDimensionPixelSize(R.styleable.ToggleButton_tb_borderRadius, getDimension(R.dimen.toggle_border_radius));
        mTextSize = array.getDimensionPixelSize(R.styleable.ToggleButton_android_textSize,
                getDimension(R.dimen.toggle_text_size));

        mLeftText = array.getString(R.styleable.ToggleButton_tb_leftText);
        mRightText = array.getString(R.styleable.ToggleButton_tb_rightText);
        array.recycle();
    }

    private void init(Context context) {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor.getDefaultColor());

        mTogglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTogglePaint.setStyle(Paint.Style.FILL);
        mTogglePaint.setColor(mToggleColor);

        mOffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOffPaint.setStyle(Paint.Style.FILL);
        mOffPaint.setColor(mBackgroundColor.getColorForState(new int[]{android.R.attr.state_checked},
                getColor(R.color.toggle_background_color)));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);

        if (null != mTextColor) {
            mTextSelectedColor = mTextColor.getColorForState(new int[]{android.R.attr.state_selected},
                    getColor(R.color.toggle_text_color));
            mTextDefaultColor = mTextColor.getDefaultColor();
            mTextPaint.setColor(mTextDefaultColor);
        } else {
            mTextPaint.setColor(getColor(R.color.toggle_text_color));
        }

        mGestureDetector = new GestureDetectorCompat(context, mOnGestureListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //背景
        canvas.drawRoundRect(mBound, mBorderRadius, mBorderRadius, mBackgroundPaint);
        //关闭的圆角矩形
        canvas.drawRoundRect(mOffBound, mBorderRadius, mBorderRadius, mOffPaint);
        //按钮
        canvas.drawCircle(mToggleX, mCenterY, mRadius, mTogglePaint);

        if (null != mLeftText) {
            if (null != mTextColor)
                mTextPaint.setColor(mToggleOn ? mTextDefaultColor : mTextSelectedColor);
            canvas.drawText(mLeftText, 0, mLeftText.length(), mLeftTextX, mLeftTextY, mTextPaint);
        }

        if (null != mRightText) {
            if (null != mTextColor)
                mTextPaint.setColor(mToggleOn ? mTextSelectedColor : mTextDefaultColor);
            canvas.drawText(mRightText, 0, mRightText.length(), mRightTextX, mRightTextY, mTextPaint);
        }
    }

    private int getColor(int color) {
        return getResources().getColor(color);
    }

    private int getDimension(int dimen) {
        return getResources().getDimensionPixelSize(dimen);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterX = w / 2;
        mToggleX = mToggleStartX = mCenterY = h / 2;
        mRadius = mCenterY - mBorderWidth;
        mToggleEndX = w - mCenterY;

        mBound = new RectF(0, 0, w, h);

        //内缩进距离，也就是边界线的宽度
        float insetSize = mBorderWidth;

        mBordBound = new RectF(mBound);
        mBordBound.inset(insetSize, insetSize);

        mOffBound.set(mBordBound);
        Log.i(TAG, "mOffBound: " + mOffBound);

        mBackgroundBound = new RectF(mBordBound);
        mBackgroundBound.inset(insetSize, insetSize);

        if (null != mLeftText) {
            Rect textBound = new Rect();
            mTextPaint.getTextBounds(mLeftText, 0, mLeftText.length(), textBound);
            mLeftTextX = mRadius - textBound.width() / 2;
            mLeftTextY = -textBound.top + (mBound.height() - textBound.height()) / 2;
        }

        if (null != mRightText) {
            Rect textBound = new Rect();
            mTextPaint.getTextBounds(mRightText, 0, mRightText.length(), textBound);
            mRightTextX = mToggleEndX - textBound.width() / 2;
            mRightTextY = -textBound.top + (mBound.height() - textBound.height()) / 2;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        mOnStateChangedListener = onStateChangedListener;
    }

    public interface OnStateChangedListener {
        void toggle(boolean toggleOn, ToggleButton toggleButton);
    }

}
