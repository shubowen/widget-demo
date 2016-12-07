package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


/**
 * 作者：疏博文 创建于 2016-03-14 12:22
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class GridLayout extends ViewGroup {

    private static final String TAG = "GridLayout";

    private float mItemMaxHeight;
    private int mColumnNum;
    private float mHorizontalSpace;
    private float mVerticalSpace;
    private float mOrgItemHeight;
    private float mItemMaxWidth;

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout);
        //每一行的item的个数
        mColumnNum = a.getInt(R.styleable.GridLayout_android_columnCount, 1);
        //item之间的横向的距离
        mHorizontalSpace = a.getDimension(R.styleable.GridLayout_horizontalSpace, 0);
        //item之间的纵向的距离
        mVerticalSpace = a.getDimension(R.styleable.GridLayout_verticalSpace, 0);
        //item的高度
        mOrgItemHeight = mItemMaxHeight = a.getDimension(R.styleable.GridLayout_itemHeight, -1);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //requestLayout之后,这个值要复原
        mItemMaxHeight = mOrgItemHeight;

        int childCount = getChildCount();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            throw new RuntimeException("layout_width不能指定为wrap_content");
        }

        int measureCount = childCount;
        //减去View.GONE的个数
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).getVisibility() == View.GONE)
                measureCount--;
        }

        if (measureCount > 0) {
            //计算行数
            int rowNum = measureCount / mColumnNum;
            rowNum = measureCount % mColumnNum == 0 ? rowNum : rowNum + 1;

            //先确定item的宽
            int mWidth = MeasureSpec.getSize(widthMeasureSpec);

            mItemMaxWidth = (mWidth - getPaddingLeft() - getPaddingRight() - mHorizontalSpace * (mColumnNum - 1)) / mColumnNum;

            //如果没有指定item的高,则将高设定为item的宽
            mItemMaxHeight = mOrgItemHeight == -1 ? mItemMaxWidth : mOrgItemHeight;

            int childParentWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) mItemMaxWidth, MeasureSpec.EXACTLY);
            int childParentHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) mItemMaxHeight, MeasureSpec.EXACTLY);

            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);

                if (getChildAt(i).getVisibility() == View.GONE) continue;

                final LayoutParams lp = child.getLayoutParams();
                final int cWidthMeasureSpec = getChildMeasureSpec(childParentWidthMeasureSpec, 0, lp.width);
                final int cHeightMeasureSpec = getChildMeasureSpec(childParentHeightMeasureSpec, 0, lp.height);

                child.measure(cWidthMeasureSpec, cHeightMeasureSpec);
            }

            //如果高度是wrap_content则需要确定高度
            if (heightMode != MeasureSpec.EXACTLY) {
                float mHeight = rowNum * mItemMaxHeight + mVerticalSpace * (rowNum - 1) + getPaddingBottom() + getPaddingTop();
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) mHeight, MeasureSpec.EXACTLY);
            }
        } else {
            //没有child设定为不占位置
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed) return;

        float left = getPaddingLeft();
        float top = getPaddingTop();

        int consumedCount = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (getChildAt(i).getVisibility() == View.GONE) continue;
            //左上角的y坐标
            //左上角x坐标
            float right = left + mItemMaxWidth;
            float bottom = top + mItemMaxHeight;

            int childMeasuredWidth = child.getMeasuredWidth();
            int childMeasuredHeight = child.getMeasuredHeight();


            // TODO: 16/7/8 添加Gravity支持,目前只支持center模式
            float LROffset = (mItemMaxWidth - childMeasuredWidth) * 0.5f;
            float TBOffset = (mItemMaxHeight - childMeasuredHeight) * 0.5f;

//            Log.i(TAG, "onLayout-pre: " + (left + LROffset) + ", " + (top + TBOffset));

            child.layout(Math.round(left + LROffset),
                    Math.round(top + TBOffset),
                    Math.round(right - LROffset),
                    Math.round(bottom - TBOffset));

//            Log.i(TAG, "onLayout-post: " + i + ", " + child.getClass().getSimpleName()
//                    + "---" + childMeasuredWidth + ", " + childMeasuredHeight
//                    + "---" + child.getLeft() + ", " + child.getTop());

            if (((consumedCount + 1) % mColumnNum) == 0) {
//                Log.i(TAG, "onLayout: 换行");
                //换行
                top = bottom + mVerticalSpace;
                left = getPaddingLeft();

//                Log.i(TAG, "onLayout-next: " + left + ", " + top);
            } else {
                left = right + mHorizontalSpace;
            }

            consumedCount++;
        }
    }

    public void setColumnNum(int columnNum) {
        mColumnNum = columnNum;
    }

    public void setHorizontalSpace(float horizontalSpace) {
        mHorizontalSpace = horizontalSpace;
    }

    public void setVerticalSpace(float verticalSpace) {
        mVerticalSpace = verticalSpace;
    }
}
