package sunsh.customview.refreshview.hfrv.DefaultHeaderAndFooterCreator;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import sunsh.customview.refreshview.R;
import sunsh.customview.refreshview.hfrv.PullToLoad.LoadFooterCreator;

/**
 * Created by sunsh on 2017/11/9.
 */

public class EmptyLoadFooterCreator extends LoadFooterCreator {
    private View mLoadView;
    private View mNoMoreView;

    @Override
    public boolean onStartPull(float distance, int lastState) {
        return true;
    }

    @Override
    public boolean onReleaseToLoad(float distance, int lastState) {
        return true;
    }

    @Override
    public void onStartLoading() {

    }

    @Override
    public void onStopLoad() {

    }

    @Override
    public View getNoMoreView(Context context, RecyclerView recyclerView) {
        if (mNoMoreView == null)
            mNoMoreView = LayoutInflater.from(context).inflate(R.layout.hfrv_empty_foot, null);
        return mNoMoreView;
    }

    @Override
    public View getLoadView(Context context, RecyclerView recyclerView) {
        if (mLoadView == null)
            mLoadView = LayoutInflater.from(context).inflate(R.layout.hfrv_empty_foot, null);
        return mLoadView;
    }
}
