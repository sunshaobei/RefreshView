package sunsh.customview.refreshview.hfrv.DefaultHeaderAndFooterCreator;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import sunsh.customview.refreshview.PullToRefreshRecyclerView;
import sunsh.customview.refreshview.R;
import sunsh.customview.refreshview.hfrv.PullToRefresh.RefreshHeaderCreator;


/**
 * Created by sunsh on 2016/9/28.
 */
public class EmptyRefreshHeaderCreator extends RefreshHeaderCreator {

    private View mRefreshView;


    @Override
    public boolean onStartPull(float distance, int lastState) {
        return true;
    }

    @Override
    public void onStopRefresh() {

    }


    @Override
    public boolean onReleaseToRefresh(float distance, int lastState) {
        return true;
    }

    @Override
    public void onStartRefreshing() {

    }

    @Override
    public View getRefreshView(Context context, RecyclerView recyclerView) {
        if (mRefreshView == null)
            mRefreshView = LayoutInflater.from(context).inflate(R.layout.hfrv_empty_foot, recyclerView, false);
        return mRefreshView;
    }

}
