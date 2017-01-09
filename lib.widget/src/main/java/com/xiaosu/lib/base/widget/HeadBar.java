package com.xiaosu.lib.base.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 作者：疏博文 创建于 2016-01-25 16:55
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class HeadBar extends RelativeLayout {

    static final String META_OPTION_KEY = "headOption";

    private static final String TAG = "HeadBar";

    TextView tvLeft;
    TextView tvMiddle;
    TextView tvRight;
    View line;

    private View centerLayout;

    private static HeadBarOption mOption;

    public HeadBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.lay_head_bar, this);

        installOption(context);

        findView();

        if (null != mOption) {
            tvLeft.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatDrawableManager.get().getDrawable(context, mOption.getBackIconDrawableRes()),
                    null,
                    null,
                    null);
            setBackgroundColor(getColor(mOption.getBackgroundColorRes()));

            tvLeft.setBackgroundResource(mOption.getLeftIconBackgroundRes());

            tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, mOption.getLeftAndRightTextSize());
            tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, mOption.getLeftAndRightTextSize());
            tvMiddle.setTextSize(TypedValue.COMPLEX_UNIT_SP, mOption.getTitleSize());
            tvMiddle.setTextColor(mOption.getTitleColorRes());
            tvLeft.setTextColor(mOption.getLeftAndRightTextColor());
            tvRight.setTextColor(mOption.getLeftAndRightTextColor());
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeadBar);

        boolean showLeftText = a.getBoolean(R.styleable.HeadBar_showLeftText, true);
        tvLeft.setVisibility(showLeftText ? VISIBLE : INVISIBLE);

        if (a.hasValue(R.styleable.HeadBar_android_background))
            setBackgroundColor(a.getColor(R.styleable.HeadBar_android_background,
                    getColor(mOption.getBackgroundColorRes())));

        if (showLeftText && a.hasValue(R.styleable.HeadBar_leftImgRes)) {
            try {
                tvLeft.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(a.getResourceId(R.styleable.HeadBar_leftImgRes, -1)),
                        null,
                        null,
                        null);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        if (showLeftText) {
            //默认点击左边的按钮是结束当前的Activity
            tvLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).onBackPressed();
                    }
                }
            });
        }

        boolean showLine = a.getBoolean(R.styleable.HeadBar_showLine, false);
        line.setVisibility(showLine ? VISIBLE : INVISIBLE);

        if (showLine) {
            line.setBackgroundColor(a.getColor(R.styleable.HeadBar_lineColor, 0XFFD8D8D8));
        }
        //中间的布局
        int centerLayoutId = a.getResourceId(R.styleable.HeadBar_centerLayout, -1);
        if (centerLayoutId == -1) {
            tvMiddle.setVisibility(VISIBLE);
            tvMiddle.setText(a.getString(R.styleable.HeadBar_headTitle));
            tvMiddle.setTextColor(a.getColor(R.styleable.HeadBar_titleColor, getColor(mOption.getTitleColorRes())));
        } else {
            setCenterLayout(centerLayoutId);
        }

        boolean showRightText = a.getBoolean(R.styleable.HeadBar_showRightText, false);
        if (showRightText) {
            tvRight.setVisibility(VISIBLE);
            tvRight.setText(a.getString(R.styleable.HeadBar_rightText));
            tvRight.setTextColor(a.getColor(R.styleable.HeadBar_rightTextColor, Color.WHITE));
            try {
                tvRight.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        getResources().getDrawable(a.getResourceId(R.styleable.HeadBar_rightImgRes, -1)),
                        null);
            } catch (Resources.NotFoundException e) {
                tvRight.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null);
            }
        } else {
            tvRight.setVisibility(INVISIBLE);
        }
        a.recycle();
    }

    private void findView() {
        tvLeft = (TextView) findViewById(R.id.tv_left);
        tvMiddle = (TextView) findViewById(R.id.tv_middle);
        tvRight = (TextView) findViewById(R.id.tv_right);
        line = findViewById(R.id.line);
    }

    private void installOption(Context context) {
        if (null != mOption) return;

        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo.metaData.containsKey(META_OPTION_KEY)) {
                String optionPath = appInfo.metaData.getString(META_OPTION_KEY);
                Class<? extends HeadBarOption> optionClass = (Class<? extends HeadBarOption>) Class.forName(optionPath);
                mOption = optionClass.newInstance();
            } else {
                mOption = new HeadBarOption();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //默认的Option
            mOption = new HeadBarOption();
        }
    }

    public View setCenterLayout(int layoutRes) {
        tvMiddle.setVisibility(GONE);
        centerLayout = LayoutInflater.from(getContext()).inflate(layoutRes, this, false);

        LayoutParams params = (LayoutParams) centerLayout.getLayoutParams();
        if (null == params) {
            params = new LayoutParams(-2, -2);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            centerLayout.setLayoutParams(params);
        }
        params.addRule(RelativeLayout.LEFT_OF, R.id.tv_right);
        params.addRule(RelativeLayout.RIGHT_OF, R.id.tv_left);

        if (params.width != -1) {
            //如果centerLayout不是match_parent
            FrameLayout frameLayout = new FrameLayout(getContext());
            LayoutParams containerParams = new LayoutParams(-1, -2);
            containerParams.addRule(RelativeLayout.CENTER_VERTICAL);

            containerParams.addRule(RelativeLayout.LEFT_OF, R.id.tv_right);
            containerParams.addRule(RelativeLayout.RIGHT_OF, R.id.tv_left);

            LayoutInflater.from(getContext()).inflate(layoutRes, frameLayout);
            addView(frameLayout, containerParams);
            return frameLayout.getChildAt(0);
        } else {
            addView(centerLayout);
        }

        return centerLayout;
    }

    public void setRightClickListener(OnClickListener listener) {
        tvRight.setOnClickListener(listener);
    }

    public void setLeftClickListener(OnClickListener listener) {
        tvLeft.setOnClickListener(listener);
    }

    public void setTvRight(CharSequence text) {
        tvRight.setText(text);
    }

    public void setTitle(CharSequence text) {
        tvMiddle.setText(text);
    }

    public View getCenterLayout() {
        return centerLayout;
    }

    public void showLine(boolean flag) {
        line.setVisibility(flag ? VISIBLE : INVISIBLE);
    }

    public String getRightText() {
        return tvRight.getText().toString();
    }

    public void showRightText(boolean show) {
        tvRight.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void setRightTextColor(int color) {
        tvRight.setTextColor(color);
    }

    public void setRightImage(int id) {
        tvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, id, 0);
    }

    public void setRightDrawable(Drawable drawable) {
        tvRight.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public void setTitleTextColor(int color) {
        tvMiddle.setTextColor(color);
    }

    public View getRightView() {
        return tvRight;
    }

    int getColor(int colorRes) {
        return getResources().getColor(colorRes);
    }

    public void showLeft(boolean show) {
        tvLeft.setVisibility(show ? VISIBLE : INVISIBLE);
    }

}
