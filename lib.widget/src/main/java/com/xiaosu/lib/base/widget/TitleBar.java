package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * 作者：疏博文 创建于 2016-08-27 18:26
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class TitleBar extends RelativeLayout {

    private static final String TAG = "TitleBar";

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
//        LayoutInflater.from(context).inflate(R.layout.lay_head_bar, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeadBar);

        Log.i(TAG, "TitleBar: " + a.getString(R.styleable.HeadBar_headTitle));

        a.recycle();
    }
}
