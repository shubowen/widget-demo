package widget.demo.xiaosu.widget_demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xiaosu.lib.base.widget.ActionView;

/**
 * 疏博文 新建于 16/12/30.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class ActionViewActivity extends AppCompatActivity {

    ActionView mActionView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_view);
        findView();
        mActionView.setTvRight("在ellipsize指定marquee的情况下，设置重复滚动的次数，当设置为marquee_forever时表示无限次。");
        mActionView.setRightTextMarquee();
    }

    private void findView() {
        mActionView = (ActionView) findViewById(R.id.action_view);
    }
}
