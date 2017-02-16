package widget.demo.xiaosu.widget_demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * 疏博文 新建于 17/1/11.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class TestLayout extends LinearLayout {

    public TestLayout(Context context) {
        super(context);
    }

    public TestLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        Log.i("offsetTopAndBottom", "执行--------: " + getTop());
    }
}
