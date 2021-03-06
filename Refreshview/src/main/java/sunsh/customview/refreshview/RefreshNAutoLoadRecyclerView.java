package sunsh.customview.refreshview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import sunsh.customview.refreshview.hfrv.AutoLoad.AutoLoadAdapter;
import sunsh.customview.refreshview.hfrv.AutoLoad.AutoLoadFooterCreator;
import sunsh.customview.refreshview.hfrv.DefaultHeaderAndFooterCreator.DefaultAutoLoadFooterCreator;
import sunsh.customview.refreshview.hfrv.PullToLoad.LoadListener;
import sunsh.customview.refreshview.hfrv.PullToLoad.OnLoadListener;


/**
 * Created by Administrator on 2016/9/26.
 */
public class RefreshNAutoLoadRecyclerView extends PullToRefreshRecyclerView {

    public RefreshNAutoLoadRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public RefreshNAutoLoadRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshNAutoLoadRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private View mLoadView;
    private AutoLoadAdapter mAdapter;
    private Adapter mRealAdapter;

    private boolean mIsLoading = false;
    private boolean mLoadMoreEnable = false;
    private boolean mNoMore = false;
    private View mNoMoreView;

    private AutoLoadFooterCreator mAutoLoadFooterCreator;
    private OnLoadListener mOnLoadListener;
    private LoadListener loadListener;


    private void init(Context context) {
        mAutoLoadFooterCreator = new DefaultAutoLoadFooterCreator();
        mLoadView = mAutoLoadFooterCreator.getLoadView(context,this);
        mNoMoreView = mAutoLoadFooterCreator.getNoMoreView(context,this);
    }

    /**
     * 隐藏加载尾部
     */
    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (mLoadView == null) return;
//        若数据不满一屏
        if (getAdapter() == null) return;
        if (getChildCount() >= getAdapter().getItemCount()) {
            if (mLoadView.getVisibility() != GONE) {
                mLoadView.setVisibility(GONE);
            }
        } else {
            if (mLoadView.getVisibility() != VISIBLE) {
                mLoadView.setVisibility(VISIBLE);
            }
        }
    }


    @Override
    public void setAdapter(Adapter adapter) {
        mRealAdapter = adapter;
        if (adapter instanceof AutoLoadAdapter) {
            mAdapter = (AutoLoadAdapter) adapter;
        } else{
            mAdapter = new AutoLoadAdapter(getContext(),adapter);
        }
        super.setAdapter(mAdapter);
        if (mLoadView != null) {
            mAdapter.setLoadView(mLoadView);
        }

    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE  && !mIsLoading && mLoadMoreEnable && mLoadView != null) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 1 && layoutManager.getItemCount() > layoutManager.getChildCount() && !mNoMore ) {
                mIsLoading = true;
                if (mOnLoadListener != null)
                    mOnLoadListener.onStartLoading(mRealAdapter.getItemCount());
                if (loadListener != null)
                    loadListener.onLoad();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**完成加载*/
    public void completeLoad(int loadItemCount) {
        mIsLoading = false;
        setLoadingViewGone();

        int startItem = mRealAdapter.getItemCount() + mAdapter.getHeadersCount() - loadItemCount;
        mAdapter.notifyItemRangeInserted(startItem, loadItemCount);

    }

    /**没有更多*/
    public void setNoMore(boolean noMore) {
        mIsLoading = false;
        mNoMore = noMore;
        if (mNoMore) {
            if (mNoMoreView != null){
                mAdapter.setLoadView(mNoMoreView);
            }
        } else if (mLoadView != null){
            mAdapter.setLoadView(mLoadView);
        }
    }

    /**设置加载监听*/
    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.mLoadMoreEnable = true;
        this.mOnLoadListener = onLoadListener;
    }
    /**设置加载监听*/
    public void setOnLoadListener(LoadListener o) {
        this.mLoadMoreEnable = true;
        this.loadListener = o;
    }

    /**获得加载中View和底部填充view的个数，用于绘制分割线*/
    public int getLoadViewCount() {
        if (mLoadView != null)
            return 1;
        return 0;
    }

    /**获得真正的adapter*/
    @Override
    public Adapter getRealAdapter() {
        return mRealAdapter;
    }

    /**
     * 设置自定义的加载尾部
     */
    public void setAutoLoadViewCreator(AutoLoadFooterCreator autoLoadFooterCreator) {
        if (autoLoadFooterCreator == null) {
            throw new IllegalArgumentException("the AutoLoadFooterCreator u set must not be null");
        } else {
            this.mAutoLoadFooterCreator = autoLoadFooterCreator;
            mLoadView = autoLoadFooterCreator.getLoadView(getContext(),this);
            mAdapter.setLoadView(mLoadView);
            mNoMoreView = autoLoadFooterCreator.getNoMoreView(getContext(),this);
        }
    }

}
