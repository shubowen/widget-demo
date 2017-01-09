package widget.demo.xiaosu.widget_demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaosu.lib.base.widget.drawerLayout.DrawerLayout;


public class DrawerLayoutActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);
        findView();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new DrawerAdapter());
    }

    public void clickAnchor(View view) {
        if (!mDrawerLayout.isDrawerOpen()) {
            mDrawerLayout.openDrawer();
        } else {
            mDrawerLayout.closeDrawer();
        }
    }

    private void findView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    class DrawerAdapter extends RecyclerView.Adapter<DrawerHolder> {

        @Override
        public DrawerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DrawerHolder(parent);
        }

        @Override
        public void onBindViewHolder(DrawerHolder holder, int position) {
            TextView item = (TextView) holder.itemView;
            item.setGravity(Gravity.CENTER);
            item.setText("-----------" + position + "-----------");
        }

        @Override
        public int getItemCount() {
            return 20;
        }
    }

    class DrawerHolder extends RecyclerView.ViewHolder {

        DrawerHolder(ViewGroup parent) {
            super(LayoutInflater.from(DrawerLayoutActivity.this)
                    .inflate(android.R.layout.simple_list_item_1, parent, false));
            itemView.setBackgroundColor(Color.WHITE);
        }

    }

}
