package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 疏博文 新建于 16/12/28.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class ScrollView extends android.widget.ScrollView {

    private OnScrollChangedListener mOnScrollChangedListener;

    public ScrollView(Context context) {
        super(context);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (null != mOnScrollChangedListener)
            mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        mOnScrollChangedListener = onScrollChangedListener;
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

}
