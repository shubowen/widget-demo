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
import android.widget.Toast;

import com.xiaosu.lib.base.widget.drawerLayout.DrawerLayout;

import java.util.ArrayList;
import java.util.List;


public class DrawerLayoutActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;

    List<String> data = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);
        findView();

        mRecyclerView.getLayoutManager().setAutoMeasureEnabled(true);
        for (int i = 0; i < 20; i++) {
            data.add("-----------" + i + "-----------");
        }
    }

    public void clickTitle(View view){
        Toast.makeText(this, "clickTitle", Toast.LENGTH_SHORT).show();
    }

    public void clickAnchor(View view) {
        if (!mDrawerLayout.isDrawerOpen()) {
            if (null == mRecyclerView.getAdapter()) {
                mDrawerLayout.openDrawer(true);
                mRecyclerView.setAdapter(new DrawerAdapter());
                return;
            }
            mDrawerLayout.openDrawer();
        } else {
            mDrawerLayout.closeDrawer();
        }
    }

    public void clickContent(View view) {
        Toast.makeText(this, "clickContent", Toast.LENGTH_SHORT).show();
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
        public void onBindViewHolder(final DrawerHolder holder, int position) {
            TextView item = (TextView) holder.itemView;
            item.setGravity(Gravity.CENTER);
            final String s = data.get(position);
            item.setText(s);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    data.remove(s);
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class DrawerHolder extends RecyclerView.ViewHolder {

        DrawerHolder(ViewGroup parent) {
            super(LayoutInflater.from(DrawerLayoutActivity.this)
                    .inflate(android.R.layout.simple_list_item_1, parent, false));
            itemView.setBackgroundColor(Color.WHITE);
        }

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen()) {
            mDrawerLayout.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
