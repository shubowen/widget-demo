package com.xiaosu.lib.base.widget.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.xiaosu.lib.base.widget.R;


public class CoordinateLayout extends FrameLayout {

    public CoordinateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.left != -1 && params.centerY != -1)
                child.layout(params.left,
                        params.centerY - child.getMeasuredHeight() / 2,
                        params.left + child.getMeasuredWidth(),
                        params.centerY + child.getMeasuredHeight() / 2);

            if (params.right != -1 && params.centerY != -1)
                child.layout(params.right - child.getMeasuredWidth(),
                        params.centerY - child.getMeasuredHeight() / 2,
                        params.right,
                        params.centerY + child.getMeasuredHeight() / 2);
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        int left = -1;
        int top = -1;
        int right = -1;
        int centerY = -1;

        public LayoutParams() {
            super(-2, -2);
        }

        public LayoutParams(int right, int centerY) {
            super(-2, -2);
            this.right = right;
            this.centerY = centerY;
        }


        public void setLeftAndCenterY(float[] floats) {
            left = (int) floats[0];
            centerY = (int) floats[1];
        }

        public void setRightAndCenterY(float[] floats) {
            right = (int) floats[0];
            centerY = (int) floats[1];
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CoordinateLayout_Layout);
            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.CoordinateLayout_Layout_coordinateL) {
                    left = a.getInt(attr, 0);

                } else if (attr == R.styleable.CoordinateLayout_Layout_coordinateT) {
                    top = a.getInt(attr, 0);

                } else if (attr == R.styleable.CoordinateLayout_Layout_coordinateR) {
                    right = a.getInt(attr, 0);

                }
            }
            a.recycle();
        }
    }

}
