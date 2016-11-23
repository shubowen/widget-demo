package widget.demo.xiaosu.widget_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.xiaosu.lib.base.widget.FlowLayout;

public class MainActivity extends AppCompatActivity {

    String[] str = {"江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "江西省南", "物园", "大熊猫馆", "擅自翻越围", "的运", "名男子进", "作出反",
            "擅自翻越围", "的运", "名男子进", "作出反"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FlowLayout flowLayout = (FlowLayout) findViewById(R.id.flowLayout);
        for (int i = 0; i < str.length; i++) {
            TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.lay_textview_item, flowLayout, false);
            textView.setText(str[i]);
            flowLayout.addView(textView);
        }

    }
}
