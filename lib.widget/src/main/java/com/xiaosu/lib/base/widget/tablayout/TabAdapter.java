package com.xiaosu.lib.base.widget.tablayout;

import java.util.List;

/**
 * 作者：疏博文 创建于 2016-08-17 18:43
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public abstract class TabAdapter<T> {

    List<T> data;

    public TabAdapter(List<T> data) {
        this.data = data;
    }

    public String getText(int position) {
        return convert(data.get(position));

    }

    protected abstract String convert(T t);

    public int getTabCount() {
        return data.size();
    }

}
