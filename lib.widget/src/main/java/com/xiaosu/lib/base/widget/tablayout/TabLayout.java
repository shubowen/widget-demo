package com.xiaosu.lib.base.widget.tablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaosu.lib.base.widget.R;

/**
 * 作者：疏博文 创建于 2016-08-17 18:41
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class TabLayout extends HorizontalScrollView {

    private LinearLayout mTabContainer;

    private TabAdapter mTabAdapter;

    private OnTabSelectedListener listener;

    private ITabProvider mTabProvider;

    private int lastPosition = -1;

    public TabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setHorizontalScrollBarEnabled(false);
        mTabContainer = new LinearLayout(context);
        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mTabContainer);

        parseAttrs(context, attrs);

        if (null == mTabProvider) {
            mTabProvider = new DefaultTabProvider();
        }
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout);
        if (a.hasValue(R.styleable.TabLayout_tabProvider)) {
            String tabProviderPath = a.getString(R.styleable.TabLayout_tabProvider);
            try {
                mTabProvider = (ITabProvider) Class.forName(tabProviderPath).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        a.recycle();
    }


    public void setAdapter(TabAdapter tabAdapter) {
        if (null != tabAdapter && mTabAdapter == null) {
            initTab(tabAdapter);
            mTabAdapter = tabAdapter;
        }
    }

    private void initTab(TabAdapter tabAdapter) {
        for (int i = 0; i < tabAdapter.getTabCount(); i++) {
            View tab = mTabProvider.getTab(mTabContainer, i);
            TextView tab_text = (TextView) tab.findViewById(mTabProvider.getTextViewId());
            tab.setTag(i);

            tab_text.setText(tabAdapter.getText(i));
            mTabContainer.addView(tab);

            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (int) view.getTag();
                    selectItem(position);
                }
            });
        }

        post(new Runnable() {
            @Override
            public void run() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        selectItem(0);
                    }
                });
            }
        });
    }

    public TabAdapter getAdapter() {
        return mTabAdapter;
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void selectItem(int position) {
        View selectedChild = mTabContainer.getChildAt(position);
        if (lastPosition == -1) {
            selectedChild.setSelected(true);
            if (null != listener)
                listener.onTabSelected(position);
        } else if (lastPosition != position) {
            selectedChild.setSelected(true);
            mTabContainer.getChildAt(lastPosition).setSelected(false);
            if (null != listener)
                listener.onTabSelected(position);
        }
        smoothScrollTo(selectedChild.getLeft(), 0);
        lastPosition = position;
    }

    public void notifyCurrentIndexSelected() {
        if (lastPosition == -1 || null == listener) return;

        listener.onTabSelected(lastPosition);
    }

    public int getCurrentIndex() {
        return lastPosition;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mTabContainer.setMinimumWidth(getWidth() - getPaddingRight() - getPaddingLeft());
        super.onLayout(changed, l, t, r, b);
    }

    public interface OnTabSelectedListener {

        void onTabSelected(int position);
    }

    class DefaultTabProvider implements ITabProvider {

        @Override
        public View getTab(ViewGroup parent, int index) {
            return LayoutInflater.from(getContext()).inflate(R.layout.lay_tab, parent, false);
        }

        @Override
        public int getTextViewId() {
            return R.id.tab_text;
        }
    }

}
