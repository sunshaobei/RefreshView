package sunsh.customview.refreshview.hfrv.Divider;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import sunsh.customview.refreshview.RefreshNLoadRecyclerView;
import sunsh.customview.refreshview.PullToRefreshRecyclerView;
import sunsh.customview.refreshview.RefreshNAutoLoadNestedRecyclerView;
import sunsh.customview.refreshview.hfrv.HeaderAndFooter.HeaderAndFooterAdapter;

/**
 * Created by sunsh on 2016/9/25.
 */
public abstract class BaseItemDecorationHelper {

    public abstract void onDraw(Canvas c, RecyclerView parent, Drawable divider, int height, int width);

    public abstract void getItemOffsets(Rect outRect, View view, RecyclerView parent, int height, int width);

    /**
     * 是不是头部
     */
    protected boolean isHeaderView(RecyclerView parent, View view) {
        if (parent.getAdapter() != null && parent.getAdapter() instanceof HeaderAndFooterAdapter) {
            int position = parent.getChildAdapterPosition(view);
            return position < ((HeaderAndFooterAdapter) parent.getAdapter()).getHeadersCount();
        }
        return false;
    }

    /**
     * 是不是尾部
     */
    protected boolean isFooterView(RecyclerView parent, View view) {
        if (parent.getAdapter() != null && parent.getAdapter() instanceof HeaderAndFooterAdapter) {
            int position = parent.getChildAdapterPosition(view);
            final HeaderAndFooterAdapter adapter = (HeaderAndFooterAdapter) parent.getAdapter();
            return position >= adapter.getHeadersCount() + adapter.getRealItemCount();
        }
        return false;
    }

    /**是不是刷新头部*/
    protected boolean isRefreshView(RecyclerView parent,View view) {
        if (parent instanceof PullToRefreshRecyclerView) {
            int position = parent.getChildAdapterPosition(view);
            PullToRefreshRecyclerView pullToRefreshRecyclerView = (PullToRefreshRecyclerView) parent;
            return position <  pullToRefreshRecyclerView.getRefreshViewCount();
        }
        return false;
    }

    /**是不是加载尾部*/
    protected boolean isLoadView(RecyclerView parent,View view) {
        if (parent instanceof RefreshNLoadRecyclerView) {
            int position = parent.getChildAdapterPosition(view);
            RefreshNLoadRecyclerView pullToLoadRecyclerView = (RefreshNLoadRecyclerView) parent;
            return position >= parent.getAdapter().getItemCount() - pullToLoadRecyclerView.getLoadViewCount();
        } else if (parent instanceof RefreshNAutoLoadNestedRecyclerView) {
            int position = parent.getChildAdapterPosition(view);
            RefreshNAutoLoadNestedRecyclerView autoLoadRecyclerView = (RefreshNAutoLoadNestedRecyclerView) parent;
            return position >= parent.getAdapter().getItemCount() - autoLoadRecyclerView.getLoadViewCount();
        }
        return false;
    }

    /**获得头部数量*/
    protected int getHeadersCount(RecyclerView parent) {
        if (parent.getAdapter() instanceof HeaderAndFooterAdapter) {
            HeaderAndFooterAdapter headerAndFooterAdapter = (HeaderAndFooterAdapter) parent.getAdapter();
            return headerAndFooterAdapter.getHeadersCount();
        }
        return 0;
    }

    /**获得刷新头部数量*/
    protected int getRefreshViewCount(RecyclerView parent) {
        if (parent instanceof PullToRefreshRecyclerView) {
            PullToRefreshRecyclerView pullToRefreshRecyclerView = (PullToRefreshRecyclerView) parent;
            return pullToRefreshRecyclerView.getRefreshViewCount();
        }
        return 0;
    }

    /**获得尾部数量*/
    protected int getFootersCount(RecyclerView parent) {
        if (parent.getAdapter() instanceof HeaderAndFooterAdapter) {
            HeaderAndFooterAdapter headerAndFooterAdapter = (HeaderAndFooterAdapter) parent.getAdapter();
            return headerAndFooterAdapter.getFootersCount();
        }
        return 0;
    }

    /**获得加载尾部数量*/
    protected int getLoadViewCount(RecyclerView parent) {
        if (parent instanceof RefreshNLoadRecyclerView) {
            RefreshNLoadRecyclerView pullToLoadRecyclerView = (RefreshNLoadRecyclerView) parent;
            return pullToLoadRecyclerView.getLoadViewCount();
        } else if (parent instanceof RefreshNAutoLoadNestedRecyclerView) {
            RefreshNAutoLoadNestedRecyclerView autoLoadRecyclerView = (RefreshNAutoLoadNestedRecyclerView) parent;
            return autoLoadRecyclerView.getLoadViewCount();
        }
        return 0;
    }

}
