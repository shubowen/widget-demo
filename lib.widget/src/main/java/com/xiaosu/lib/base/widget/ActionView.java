package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 作者：疏博文 创建于 2016-03-28 18:34
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class ActionView extends RelativeLayout {

    private static final String TAG = "Mr.su";
    TextView tvLeft;
    TextView tvRight;
    View lineBottom;
    View lineTop;

    public ActionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.lay_action, this);

        tvLeft = (TextView) findViewById(R.id.tv_left);
        tvRight = (TextView) findViewById(R.id.tv_right);
        lineBottom = findViewById(R.id.line_bottom);
        lineTop = findViewById(R.id.line_top);

        float density = getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionView);

        tvLeft.setText(a.getString(R.styleable.ActionView_leftText));
        tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(R.styleable.ActionView_leftTextSize, (int) (16 * density)));
        tvLeft.setTextColor(a.getColor(R.styleable.ActionView_leftTextColor, 0XFF333333));
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(R.styleable.ActionView_rightTextSize, (int) (16 * density)));
        tvRight.setHint(a.getString(R.styleable.ActionView_rightText));

        if (a.hasValue(R.styleable.ActionView_rightTextHintColor))
            tvRight.setHintTextColor(a.getColor(R.styleable.ActionView_rightTextHintColor, 0xFF999999));
        //左边TextView的边距
        MarginLayoutParams layoutParams = (MarginLayoutParams) tvLeft.getLayoutParams();
        layoutParams.leftMargin = (int) a.getDimension(R.styleable.ActionView_leftTextMargin, density * 12);

        //右边TextView的边距
        MarginLayoutParams layoutParams1 = (MarginLayoutParams) tvRight.getLayoutParams();
        layoutParams1.rightMargin = (int) a.getDimension(R.styleable.ActionView_rightTextMargin, density * 12);

        boolean show_line_bottom = a.getBoolean(R.styleable.ActionView_show_line_bottom, true);
        boolean show_line_top = a.getBoolean(R.styleable.ActionView_show_line_top, false);


        int model = a.getInt(R.styleable.ActionView_model, 2);

        lineBottom.setVisibility(show_line_bottom ? VISIBLE : INVISIBLE);
        lineTop.setVisibility(show_line_top ? VISIBLE : INVISIBLE);

        MarginLayoutParams layoutParams2 = (MarginLayoutParams) lineBottom.getLayoutParams();
        MarginLayoutParams layoutParams3 = (MarginLayoutParams) lineTop.getLayoutParams();
        if (a.hasValue(R.styleable.ActionView_leftImgRes)) {
            try {
                Drawable drawable = a.getDrawable(R.styleable.ActionView_leftImgRes);
                tvLeft.setCompoundDrawablesWithIntrinsicBounds(
                        drawable, null, null, null);
                if (null != drawable) {
                    //下划线边距
                    if (a.getBoolean(R.styleable.ActionView_line_bottom_margin, true) && show_line_bottom)
                        switch (model) {
                            case 0:
                                layoutParams2.leftMargin = layoutParams.leftMargin + drawable.getMinimumWidth() + tvLeft.getCompoundDrawablePadding();
                                break;
                            case 1:
                                layoutParams2.rightMargin = layoutParams1.rightMargin;
                                break;
                            case 2:
                                layoutParams2.rightMargin = layoutParams1.rightMargin;
                                layoutParams2.leftMargin = layoutParams.leftMargin + drawable.getMinimumWidth() + tvLeft.getCompoundDrawablePadding();
                                break;
                        }
                    //上划线边距
                    if (a.getBoolean(R.styleable.ActionView_line_top_margin, true) && show_line_top)
                        switch (model) {
                            case 0:
                                layoutParams3.leftMargin = layoutParams.leftMargin + drawable.getMinimumWidth() + tvLeft.getCompoundDrawablePadding();
                                break;
                            case 1:
                                layoutParams3.rightMargin = layoutParams1.rightMargin;
                                break;
                            case 2:
                                layoutParams3.rightMargin = layoutParams1.rightMargin;
                                layoutParams3.leftMargin = layoutParams.leftMargin + drawable.getMinimumWidth() + tvLeft.getCompoundDrawablePadding();
                                break;
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
                layoutParams2.leftMargin = layoutParams.leftMargin + tvLeft.getCompoundPaddingLeft();
            }
        } else {
            if (a.getBoolean(R.styleable.ActionView_line_bottom_margin, true) && show_line_bottom)
                switch (model) {
                    case 0:
                        layoutParams2.leftMargin = layoutParams.leftMargin + tvLeft.getCompoundPaddingLeft();
                        break;
                    case 1:
                        layoutParams2.rightMargin = layoutParams1.rightMargin;
                        break;
                    case 2:
                        layoutParams2.rightMargin = layoutParams1.rightMargin;
                        layoutParams2.leftMargin = layoutParams.leftMargin + tvLeft.getCompoundPaddingLeft();
                        break;
                }


            if (a.getBoolean(R.styleable.ActionView_line_top_margin, true) && show_line_top)
                switch (model) {
                    case 0:
                        layoutParams3.leftMargin = layoutParams.leftMargin + tvLeft.getCompoundPaddingLeft();
                        break;
                    case 1:
                        layoutParams3.rightMargin = layoutParams1.rightMargin;
                        break;
                    case 2:
                        layoutParams3.rightMargin = layoutParams1.rightMargin;
                        layoutParams3.leftMargin = layoutParams.leftMargin + tvLeft.getCompoundPaddingLeft();
                        break;
                }
        }

        if (a.hasValue(R.styleable.ActionView_rightImgRes)) {
            try {
                tvRight.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        a.getResourceId(R.styleable.ActionView_rightImgRes, -1),
                        0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (a.hasValue(R.styleable.ActionView_rightImgRes_right) && a.hasValue(R.styleable.ActionView_rightImgRes_left)) {
            try {
                tvRight.setCompoundDrawablesWithIntrinsicBounds(
                        a.getResourceId(R.styleable.ActionView_rightImgRes_left, -1),
                        0,
                        a.getResourceId(R.styleable.ActionView_rightImgRes_right, -1),
                        0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (a.hasValue(R.styleable.ActionView_rightImgRes_left)) {
            try {
                tvRight.setCompoundDrawablesWithIntrinsicBounds(
                        a.getResourceId(R.styleable.ActionView_rightImgRes_left, -1),
                        0,
                        0,
                        0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (a.hasValue(R.styleable.ActionView_rightImgRes_right)) {
            tvRight.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    a.getResourceId(R.styleable.ActionView_rightImgRes_right, -1),
                    0);
        }

        tvRight.setCompoundDrawablePadding((int) a.getDimension(R.styleable.ActionView_rightDrawablePadding, 10 * density));
        a.recycle();
    }

    public void showBottomLine(boolean show) {
        lineBottom.setVisibility(show ? VISIBLE : GONE);
    }

    public void showTopLine(boolean show) {
        lineTop.setVisibility(show ? VISIBLE : GONE);
    }

    public void setRightDrawablePadding(int l, int t, int r, int b) {
        tvRight.setCompoundDrawablesWithIntrinsicBounds(
                l,
                t,
                r,
                b);
    }

    public TextView getTvRight() {
        return tvRight;
    }

    public void setTvRight(CharSequence sequence) {
        tvRight.setText(sequence);
    }

    public void setTvLeft(CharSequence sequence) {
        tvLeft.setText(sequence);

    }

    public void setTvLeftSize(int size) {
        tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    public void setTvRightSize(int size) {
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    public void setOnRightClickListener(OnClickListener listener) {
        tvRight.setOnClickListener(listener);
    }

    public TextView getTvLeft() {
        return tvLeft;
    }
}
