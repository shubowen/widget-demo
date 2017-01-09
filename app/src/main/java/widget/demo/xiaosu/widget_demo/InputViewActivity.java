package widget.demo.xiaosu.widget_demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xiaosu.lib.base.widget.InputView;

/**
 * 疏博文 新建于 16/11/24.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class InputViewActivity extends AppCompatActivity {

    InputView mInputView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_input);
        findView();
        mInputView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInputView.setRightText("这是代码设置的文字");
            }
        }, 5000);
    }

    private void findView() {
        mInputView = (InputView) findViewById(R.id.inputView);
    }
}
