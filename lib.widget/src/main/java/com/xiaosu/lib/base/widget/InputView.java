package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 作者：疏博文 创建于 2016-08-08 14:20
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class InputView extends LinearLayout {

    private final TextView mTvHint;
    private final View mDivider;
    private final EditText mEtInput;
    private final Paint mUnderlinePaint;
    private boolean dividerMeasured;
    private final float mUnderlineHeight;
    private RectF mUnderLineRectF;
    private final int mUnderlineColor;
    private final boolean mShowUnderline;

    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.lay_input, this);
        mTvHint = (TextView) findViewById(R.id.tv_hint);
        mDivider = findViewById(R.id.divider);
        mEtInput = (EditText) findViewById(R.id.et_input);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        float density = getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InputView);

        MarginLayoutParams params = (MarginLayoutParams) mTvHint.getLayoutParams();
        params.leftMargin = (int) a.getDimension(R.styleable.InputView_leftMargin, 0);

        MarginLayoutParams params2 = (MarginLayoutParams) mEtInput.getLayoutParams();
        params2.rightMargin = (int) a.getDimension(R.styleable.InputView_rightMargin, 0);

        mEtInput.setEnabled(a.getBoolean(R.styleable.InputView_android_enabled, true));

        mDivider.setVisibility(a.getBoolean(R.styleable.InputView_showLine, true) ? VISIBLE : INVISIBLE);
        mTvHint.setText(a.hasValue(R.styleable.InputView_android_text) ?
                a.getText(R.styleable.InputView_android_text) : "");

        mTvHint.setTextColor(a.getColor(R.styleable.InputView_left_TextColor, 0XFF333333));

        if (a.hasValue(R.styleable.InputView_rightText))
            mEtInput.setText(a.getString(R.styleable.InputView_rightText));
        mEtInput.setGravity(a.getInt(R.styleable.InputView_android_gravity, Gravity.START | Gravity.CENTER_VERTICAL));
        mEtInput.setTextColor(a.getColor(R.styleable.InputView_rightTextColor, 0XFF333333));
        mEtInput.setHintTextColor(a.getColor(R.styleable.InputView_android_textColorHint, 0xFF999999));

        if (a.hasValue(R.styleable.InputView_android_minEms)) {
            mTvHint.setMinEms(a.getInt(R.styleable.InputView_android_minEms, 2));
        }

        try {
            if (a.hasValue(R.styleable.InputView_android_drawableLeft)) {
                mTvHint.setCompoundDrawablesWithIntrinsicBounds(a.getResourceId(R.styleable.InputView_android_drawableLeft, -1), 0, 0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mEtInput.setHint(a.hasValue(R.styleable.InputView_android_hint) ?
                a.getText(R.styleable.InputView_android_hint) : "");

        mUnderlineHeight = a.getDimension(R.styleable.InputView_underlineHeight, (float) (density * 0.5));
        mUnderlineColor = a.getColor(R.styleable.InputView_underlineColor, 0xFFD8D8D8);

        mEtInput.setInputType(a.getInt(R.styleable.InputView_android_inputType, InputType.TYPE_CLASS_TEXT));

        mShowUnderline = a.getBoolean(R.styleable.InputView_showUnderline, true);

        if (a.hasValue(R.styleable.InputView_android_imeOptions)) {
            mEtInput.setImeOptions(a.getInt(R.styleable.InputView_android_imeOptions, EditorInfo.IME_ACTION_DONE));
        }

        a.recycle();

        mUnderlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnderlinePaint.setColor(mUnderlineColor);

    }

    public void setLeftText(CharSequence text) {
        mTvHint.setText(text);
    }

    public void setRightText(CharSequence text) {
        mEtInput.setText(text);
    }

    public CharSequence getLeftText() {
        return mTvHint.getText();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!dividerMeasured) {
            mDivider.getLayoutParams().height = (int) (mTvHint.getMeasuredHeight() * 0.6);
            mDivider.requestLayout();
            dividerMeasured = true;
        }

        if (mShowUnderline)
            mUnderLineRectF = new RectF(0, getMeasuredHeight() - mUnderlineHeight, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mShowUnderline)
            canvas.drawRect(mUnderLineRectF, mUnderlinePaint);
    }

    public String getInputValue() {
        return mEtInput.getText().toString().trim();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mEtInput.setEnabled(enabled);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener l) {
        mEtInput.setOnEditorActionListener(l);
    }

}
