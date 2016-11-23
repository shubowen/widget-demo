package com.xiaosu.lib.base.widget;


/**
 * HeadBar全局配置类
 */
public class HeadBarOption {

    /**
     * @return 返回图标的资源ID
     */
    public int getBackIconDrawableRes() {
        return R.drawable.back_vector;
    }

    /**
     * @return 标题文字的大小（单位dp）
     */
    public int getTitleSize() {
        return 18;
    }

    /**
     * @return 标题文字的颜色
     */
    public int getTitleColorRes() {
        return R.color.head_title_text_color;
    }

    /**
     * @return 左右文字的大小（单位dp）
     */
    public int getLeftAndRightTextSize() {
        return 14;
    }

    /**
     * @return 左右文字的颜色（单位dp）
     */
    public int getLeftAndRightTextColor() {
        return R.color.head_title_text_color;
    }

    /**
     * @return 返回图标的背景
     */
    public int getLeftIconBackgroundRes() {
        return R.drawable.back_bg;
    }

    /**
     * @return 标题背景颜色
     */
    public int getBackgroundColorRes() {
        return R.color.head_background;
    }

}
