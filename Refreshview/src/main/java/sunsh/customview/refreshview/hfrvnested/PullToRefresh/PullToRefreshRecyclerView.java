package sunsh.customview.refreshview.hfrvnested.PullToRefresh;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import sunsh.customview.refreshview.hfrv.DefaultHeaderAndFooterCreator.DefaultRefreshHeaderCreator;
import sunsh.customview.refreshview.hfrv.HeaderAndFooter.HeaderAndFooterRecyclerView;
import sunsh.customview.refreshview.hfrv.PullToRefresh.OnRefreshListener;
import sunsh.customview.refreshview.hfrv.PullToRefresh.RefreshHeaderCreator;


/**
 * Created by sunsh on 2016/9/19.
 */
public class PullToRefreshRecyclerView extends HeaderAndFooterRecyclerView {

    public PullToRefreshRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    public int HmState = STATE_DEFAULT;
    //    初始
    public final static int STATE_DEFAULT = 0;
    //    正在下拉
    public final static int STATE_PULLING = 1;
    //    松手刷新
    public final static int STATE_RELEASE_TO_REFRESH = 2;
    //    刷新中
    public final static int STATE_REFRESHING = 3;

    private float mPullRatio = 0.5f;

    //   位于刷新View顶部的view，通过改变其高度来下拉
    private View topView;

    private View mRefreshView;
    public int mRefreshViewHeight = 0;

    private float mFirstY = 0;
    private boolean mPulling = false;

    //    是否可以下拉刷新
    private boolean mRefreshEnable = false;

    //    回弹动画
    private ValueAnimator valueAnimator;

    //    刷新监听
    private OnRefreshListener mOnRefreshListener;

    //    头部
    private RefreshHeaderCreator mRefreshHeaderCreator;

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (mRefreshView != null) {
            addHeaderView(topView);
            addHeaderView(mRefreshView);
        }
    }

    private void init(Context context) {
        if (topView == null) {
            topView = new View(context);
//            该view的高度不能为0，否则将无法判断是否已滑动到顶部
            topView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
//            设置默认LayoutManager
            setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//            初始化默认的刷新头部
            mRefreshHeaderCreator = new DefaultRefreshHeaderCreator();
            mRefreshView = mRefreshHeaderCreator.getRefreshView(context, this);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (mRefreshView != null && mRefreshViewHeight == 0) {
            mRefreshView.measure(0, 0);
            mRefreshViewHeight = mRefreshView.getLayoutParams().height;
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            marginLayoutParams.setMargins(marginLayoutParams.leftMargin, marginLayoutParams.topMargin - mRefreshViewHeight - 1, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
            setLayoutParams(marginLayoutParams);
        }
        super.onMeasure(widthSpec, heightSpec);
    }

    /**
     * 隐藏刷新头部
     */
    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
    }


    /***
     * 判断是否滑动到了顶部
     */
    private boolean isTop() {
        return !ViewCompat.canScrollVertically(this, -1);
    }

    /**
     * 判断当前是拖动中还是松手刷新
     * 刷新中不在此处判断，在手指抬起时才判断
     */
    public void HsetState(float distance) {
        if (!mRefreshEnable)return;
//        刷新中，状态不变
        if (HmState == STATE_REFRESHING) {

        } else if (distance == 0) {
            HmState = STATE_DEFAULT;
        }
//        松手刷新
        else if (distance >= mRefreshViewHeight) {
            int lastState = HmState;
            HmState = STATE_RELEASE_TO_REFRESH;
            if (mRefreshHeaderCreator != null)
                if (!mRefreshHeaderCreator.onReleaseToRefresh(distance, lastState))
                    return;
        }
//        正在拖动
        else if (distance < mRefreshViewHeight) {
            int lastState = HmState;
            HmState = STATE_PULLING;
            if (mRefreshHeaderCreator != null)
                if (!mRefreshHeaderCreator.onStartPull(distance, lastState))
                    return;
        }
        HstartPull(distance);
    }

    /**
     * 拖动或回弹时，改变顶部的margin
     */
    public void HstartPull(float distance) {
//            该view的高度不能为0，否则将无法判断是否已滑动到顶部
        if (distance < 1)
            distance = 1;
        if (topView != null) {
            LayoutParams layoutParams = (LayoutParams) topView.getLayoutParams();
            layoutParams.height = (int) distance;
            topView.setLayoutParams(layoutParams);
        }
    }

    /**
     * 松手回弹
     */
    public void HreplyPull() {
        if (!mRefreshEnable) return;
        mPulling = false;
//        回弹位置
        float destinationY = 0;
//        判断当前状态
//        若是刷新中，回弹
        if (HmState == STATE_REFRESHING) {
            destinationY = mRefreshViewHeight;
        }
//        若是松手刷新，刷新，回弹
        else if (HmState == STATE_RELEASE_TO_REFRESH) {
//            改变状态
            HmState = STATE_REFRESHING;
//            刷新
            if (mRefreshHeaderCreator != null)
                mRefreshHeaderCreator.onStartRefreshing();
            if (mOnRefreshListener != null)
                mOnRefreshListener.onStartRefreshing();
//            若在onStartRefreshing中调用了completeRefresh方法，将不会滚回初始位置，因此这里需加个判断
            if (HmState != STATE_REFRESHING) return;
            destinationY = mRefreshViewHeight;
        } else if (HmState == STATE_DEFAULT || HmState == STATE_PULLING) {
            HmState = STATE_DEFAULT;
        }

        LayoutParams layoutParams = (RecyclerView.LayoutParams) topView.getLayoutParams();
        float distance = layoutParams.height;
        if (distance <= 0) return;

        valueAnimator = ObjectAnimator.ofFloat(distance, destinationY).setDuration((long) (distance * 0.5));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float nowDistance = (float) animation.getAnimatedValue();
                HstartPull(nowDistance);
            }
        });
        valueAnimator.start();
    }

    /**
     * 结束刷新
     */
    public void completeRefresh() {
        if (mRefreshHeaderCreator != null)
            mRefreshHeaderCreator.onStopRefresh();
        HmState = STATE_DEFAULT;
        HreplyPull();

        mAdapter.notifyDataSetChanged();
        setLoadingViewGone();
    }

    /**
     * 设置监听
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mRefreshEnable = true;
        this.mOnRefreshListener = onRefreshListener;
    }

    /**
     * 设置自定义的刷新头部
     */
    public void setRefreshViewCreator(RefreshHeaderCreator refreshHeaderCreator) {
        this.mRefreshHeaderCreator = refreshHeaderCreator;
        if (mRefreshView != null && mAdapter != null) {
            mAdapter.removeHeaderView(topView);
            mAdapter.removeHeaderView(mRefreshView);
        }
        mRefreshView = refreshHeaderCreator.getRefreshView(getContext(), this);
//        若有适配器，添加到头部
        if (mAdapter != null) {
            addHeaderView(topView);
            addHeaderView(mRefreshView);
        }
    }

    /**
     * 获得刷新View和顶部填充view的个数，用于绘制分割线
     */
    public int getRefreshViewCount() {
        if (mRefreshView != null)
            return 2;
        return 0;
    }

    /**
     * 设置是否可以下拉
     */
    public void setRefreshEnable(boolean refreshEnable) {
        this.mRefreshEnable = refreshEnable;
    }

    /**
     * 设置下拉阻尼系数
     */
    public void setPullRatio(float pullRatio) {
        this.mPullRatio = pullRatio;
    }

}