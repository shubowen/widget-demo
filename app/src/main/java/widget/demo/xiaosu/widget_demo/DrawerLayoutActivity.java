package widget.demo.xiaosu.widget_demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import com.xiaosu.lib.base.widget.drawerLayout.DrawerLayout;


public class DrawerLayoutActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    public void clickAnchor(View view){
        mDrawerLayout.openDrawer(Gravity.BOTTOM);
    }

}
