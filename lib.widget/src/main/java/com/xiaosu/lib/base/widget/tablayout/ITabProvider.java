package com.xiaosu.lib.base.widget.tablayout;

import android.view.View;
import android.view.ViewGroup;

/**
 * 疏博文 新建于 16/11/12.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public interface ITabProvider {

    View getTab(ViewGroup parent, int index);

    int getTextViewId();

}
