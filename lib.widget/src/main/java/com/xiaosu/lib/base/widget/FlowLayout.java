package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout";

    private List<Line> mLines = new LinkedList<>();
    private int mSizeWidth;
    private int mLineVerticalSpace;
    private float mLineMinHorizontalSpace;
    private boolean mFormat;
    private float mLineMaxHorizontalSpace;

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
        init(context);
    }

    private void init(Context context) {
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        mLineVerticalSpace = array.getDimensionPixelSize(R.styleable.FlowLayout_lineVerticalSpace, 0);
        mLineMinHorizontalSpace = array.getDimensionPixelSize(R.styleable.FlowLayout_lineMinHorizontalSpace, (int) (density * 6));
        mLineMaxHorizontalSpace = array.getDimensionPixelSize(R.styleable.FlowLayout_lineMaxHorizontalSpace, (int) (density * 20));
        mFormat = array.getBoolean(R.styleable.FlowLayout_format, false);
        array.recycle();
    }

    public FlowLayout(Context context) {
        this(context, null);
    }

    /**
     * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获得它的父容器为它设置的测量模式和大小
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        mSizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        //防止onMeasure走两次带来的问题
        mLines.clear();

        Line line = new Line();
        mLines.add(line);

        int cCount = getChildCount();
        // 遍历每个子元素
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);

            int[] ChildWH = measureChildAndGetWH(child, widthMeasureSpec, heightMeasureSpec);

            if (!line.addView(child, ChildWH)) {
                // TODO: 16/11/1 child.width > parent.width
                line = new Line();
                mLines.add(line);
                line.addView(child, ChildWH);
            }
        }
        int measuredHeight = 0;

        for (Line l : mLines)
            measuredHeight += l.mMaxHeight;

        if (mLines.size() > 1)
            measuredHeight += (mLines.size() - 1) * mLineVerticalSpace;

        //如果给定的高是固定的值
        if (modeHeight == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            //wrap_content
            setMeasuredDimension(mSizeWidth + getPaddingLeft() + getPaddingRight(), measuredHeight + getPaddingBottom() + getPaddingTop());
        }
    }

    private int[] measureChildAndGetWH(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
        return new int[]{child.getMeasuredWidth(), child.getMeasuredHeight()};
    }

    class Line {
        List<View> mViews = new LinkedList<>();
        int mMaxHeight;
        int mWidth;

        boolean addView(View child, int[] childWH) {
            if (mViews.size() > 0 && (mWidth + childWH[0]) > mSizeWidth)
                return false;
            else {
                mMaxHeight = Math.max(mMaxHeight, childWH[1]);
                mWidth += childWH[0] + mLineMinHorizontalSpace;
                mViews.add(child);
                return true;
            }
        }

        /**
         * @param l
         * @param t
         */
        public void layout(int l, int t) {
            int size = mViews.size();

            int dividerWidth = 0;
            if (size > 1)
                dividerWidth = (int) ((mSizeWidth - mWidth + mLineMinHorizontalSpace) / (size - 1));

            int left = l;
            for (int i = 0; i < size; i++) {
                View view = mViews.get(i);
                int paddingTop = (mMaxHeight - view.getMeasuredHeight()) / 2;
                int top = t + paddingTop;
                //间隙平分，高度居中
                view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());

                float gap = (mFormat ? dividerWidth : 0) + mLineMinHorizontalSpace;
                gap = gap > mLineMaxHorizontalSpace ? mLineMaxHorizontalSpace : gap;
                left += gap + view.getMeasuredWidth();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = getPaddingTop();
        int size = mLines.size();
        for (int i = 0; i < size; i++) {
            Line line = mLines.get(i);
            line.layout(getPaddingLeft(), top);
            top += line.mMaxHeight + mLineVerticalSpace;
        }
    }
}
