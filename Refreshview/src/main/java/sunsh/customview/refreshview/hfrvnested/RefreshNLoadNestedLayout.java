package sunsh.customview.refreshview.hfrvnested;

/**
 * Created by sunsh on 2017/9/17.
 */
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 *
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import sunsh.customview.refreshview.SwipeRefreshLayout;
import sunsh.customview.refreshview.hfrvnested.PullToRefresh.PullToRefreshRecyclerView;


/**
 * The GZoomSwifrefresh should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. The GZoomSwifrefresh
 * will notify the listener each and every time the gesture is completed again;
 * the listener is responsible for correctly determining when to actually
 * initiate a refresh of its content. If the listener determines there should
 * not be a refresh, it must call setRefreshing(false) to cancel any visual
 * indication of a refresh. If an activity wishes to show just the progress
 * animation, it should call setRefreshing(true). To disable the gesture and
 * progress animation, call setEnabled(false) on the view.
 * <p>
 * This layout should be made the parent of the view that will be refreshed as a
 * result of the gesture and can only support one direct child. This view will
 * also be made the target of the gesture and will be forced to match both the
 * width and the height supplied in this layout. The GZoomSwifrefresh does not
 * provide accessibility events; instead, a menu item must be provided to allow
 * refresh of the content wherever this gesture is used.
 * </p>
 */
public class RefreshNLoadNestedLayout extends LinearLayout implements NestedScrollingParent,
        NestedScrollingChild {

    @VisibleForTesting
    static final int CIRCLE_DIAMETER = 40;
    @VisibleForTesting
    static final int CIRCLE_DIAMETER_LARGE = 56;

    private static final String LOG_TAG = SwipeRefreshLayout.class.getSimpleName();

    private static final int MAX_ALPHA = 255;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    private View mTarget; // the target of the gesture

    private int mTouchSlop;

    // If nested scrolling is enabled, the total amount that needed to be
    // consumed by this as the nested scrolling parent is used in place of the
    // overscroll determined by MOVE events in the onTouch handler
    /**
     * 顶部没有被消费的
     */
    private float mTotalUnconsumed;
    /**
     * 底部滑动没有被消费的
     */
    private float mTotalUnconsumedBottom;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private boolean mNestedScrollInProgress;

    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;

    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };


    private OnChildScrollUpCallback mChildScrollUpCallback;


    //add
    private boolean mBottomIsScrolling;

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public RefreshNLoadNestedLayout(Context context) {
        this(context, null);
    }

    public RefreshNLoadNestedRecyclerView getBasseRV(){
        return getLoadMore();
    }

    /**
     * Constructor that is called when inflating GZoomSwifrefresh from XML.
     *
     * @param context
     * @param attrs
     */
    public RefreshNLoadNestedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        RefreshNLoadNestedRecyclerView pullToLoadRecyclerView = new RefreshNLoadNestedRecyclerView(context);
        pullToLoadRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(pullToLoadRecyclerView);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        //按照顺序绘制
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        // the absolute offset has to take into account that the circle starts at an offset
        //这是错的，只是先初始化

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);


        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    public RefreshNLoadNestedRecyclerView getRv() {
        return getLoadMore();
    }

    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     * 这里是判断机型版本有没有大于安卓3.0
     * 小于返回true
     */
    private boolean isAlphaUsedForScale() {
        return android.os.Build.VERSION.SDK_INT < 11;//11 是Android3.0
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     * 这个方法是暴露给外部手动停止滑动的
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            //TODO refresh
            getRefresh().HsetState(getRefresh().mRefreshViewHeight);
        } else {
            getRefresh().completeRefresh();
        }
    }

    public void setBottomRefreshing(boolean refreshing, int loadCount) {
        if (refreshing) {
            getLoadMore().FsetState(getLoadMore().mLoadViewHeight);
        } else {
            getLoadMore().completeLoad(loadCount);
        }
    }


    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return getRefresh().HmState == PullToRefreshRecyclerView.STATE_REFRESHING;
    }

    /**
     * 这个就是要找到滑动的目标，也就是mTarget
     * ，他就是找到在子view中接着上面刷新圆环的第一个，也就是我们放在格局中的第一个
     */
    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {//手势目标
            mTarget = getChildAt(0);
        }
    }



    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null) {
            Log.e("fish", "canChildScrollUp;mChildScrollUpCallback != null");
            return mChildScrollUpCallback.canChildScrollUp(this, mTarget);

        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            Log.e("fish", "canChildScrollUp;Build.VERSION.SDK_INT < 14");
            if (mTarget instanceof AbsListView) {
                Log.e("fish", "canChildScrollUp;mTarget instanceof AbsListView");
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                Log.e("fish", "canChildScrollUp;mTarget ！instanceof AbsListView");
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            // Log.e("fish","canChildScrollUp;android.os.Build.VERSION.SDK_INT > 14");
            //这个可以用，判断是否可以向下拉
            // boolean re = ViewCompat.canScrollVertically(mTarget,1);
            //Log.e("fish","canChildScrollDown?"+re);
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    //自己写的方法

    /**
     * 判断是否而已向下拉
     */
    public boolean canChildScrollDown() {
        return ViewCompat.canScrollVertically(mTarget, 1);
    }


    /**
     * Set a callback to override {@link SwipeRefreshLayout#canChildScrollUp()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     *
     * @param callback Callback that should be called when canChildScrollUp() is called.
     */
    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        mChildScrollUpCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //先确定滑动的目标对象
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        /**isEnabled 可用
         *
         * canChildScrollUp 子view还可以向上拉
         * */
        if (!isEnabled() || canChildScrollUp()
                || getRefresh().HmState == PullToRefreshRecyclerView.STATE_REFRESHING || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e("fish", "onInterceptTouchEvent,ACTION_DOWN");
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE://这个move基本不触发
                Log.e("fish", "onInterceptTouchEvent,ACTION_MOVE");
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                Log.e("fish", "onInterceptTouchEvent,MotionEventCompat.ACTION_POINTER_UP");
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
                Log.e("fish", "onInterceptTouchEvent,motionEvent.ACTION_UP");
            case MotionEvent.ACTION_CANCEL:
                Log.e("fish", "onInterceptTouchEvent,MotionEvent.ACTION_CANCEL");
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //target 发起滑动的字view，可以不是当前view的直接子view
        //child 包含target的直接子view
        //返回true表示要与target配套滑动，为true则下面的accepted也会被调用
        //mReturningToStart是为了配合onTouchEvent的，这里我们不扩展
        return isEnabled() && getRefresh().HmState == PullToRefreshRecyclerView.STATE_DEFAULT && getLoadMore().FmState == RefreshNLoadNestedRecyclerView.STATE_DEFAULT //没有在刷新和返回途中
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;//竖直方向
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        Log.e(LOG_TAG, "onNestedScrollAccepted,axes=" + axes);
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);//调用自己child接口的方法，实现向上传递
        mTotalUnconsumed = 0;
        //
        mTotalUnconsumedBottom = 0;
        mNestedScrollInProgress = true;

    }

    /**
     * 在child开始滑动之前会通知parent的这个方法，看看能不能滑动dx dy这么多
     * 这里是回调，parent可以先于child进行滑动
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up
        // before allowing the list to scroll
        Log.e(LOG_TAG, "onNestedPreScroll,dy=" + dy + ",consume[1]==" + consumed[1]);
        if (dy > 0 && mTotalUnconsumed > 0 && getRefresh().HmState != PullToRefreshRecyclerView.STATE_REFRESHING) {//向下拖dy小于0，所以这是为了处理拖circle到一半然后又缩回去的情况
            if (dy > mTotalUnconsumed) {//拖动的很多，大于未消费的
                consumed[1] = dy - (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {//拖动一点，我们全部用给自己
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            moveSpinner(mTotalUnconsumed);//move 到这个位置
        }

        //处理底部的,圆圈已经出来了之后它又向下拖
        if (dy < 0 && mTotalUnconsumedBottom > 0 && getLoadMore().FmState != RefreshNLoadNestedRecyclerView.STATE_LOADING) {
            Log.e("fish", "dy<0 && mTotalUnconsumedBottom > 0+++dy==" + dy + ",mTotalUnconsumedBottom==" + mTotalUnconsumedBottom);
            if (-dy > mTotalUnconsumedBottom)//如果拖动的很多，就先给圆圈，然后还给子控件
            {
                consumed[1] = -(int) mTotalUnconsumedBottom;
                mTotalUnconsumedBottom = 0;
                mBottomIsScrolling = false;
            } else {//否则，先给父控件
                mTotalUnconsumedBottom += (dy);
                consumed[1] = dy;//原来传回去的是正数，结果越滑越快。。。改成负数之后就对了，开心
            }
            moveBottomSpinner(mTotalUnconsumedBottom);
        }

        // Now let our nested parent consume the leftovers
        /**计算它的父层的消耗，加上去*/
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {//父控件处理了才会返回true
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        Log.e(LOG_TAG, "onStopNestedScroll,mTotalUnconsumed=" + mTotalUnconsumed);
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll
        if (mTotalUnconsumed > 0 && getLoadMore().HmState != RefreshNLoadNestedRecyclerView.STATE_LOADING) {
            finishSpinner(mTotalUnconsumed);
            mTotalUnconsumed = 0;
        }
        if (mTotalUnconsumedBottom > 0 && getRefresh().HmState != PullToRefreshRecyclerView.STATE_REFRESHING) {
            Log.e("fish", "onStopNestedScroll,mTotalUnconsumedBottom > 0");
            finishSpinnerBottom(mTotalUnconsumedBottom);
            mTotalUnconsumedBottom = 0;
            mBottomIsScrolling = false;
        }
        // Dispatch up our nested parent
        stopNestedScroll();
    }

    /**
     * 这里是后于child的滑动
     */
    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        Log.e(LOG_TAG, "onNestedScroll,dyUnconsumed=" + dyUnconsumed + "mParentOffsetInWindow[1]" + mParentOffsetInWindow[1]);
        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {//向下拉
            mTotalUnconsumed += Math.abs(dy);
            moveSpinner(mTotalUnconsumed);
        } else if (dy > 0 && !canChildScrollDown()) //向上拉
        {
            mTotalUnconsumedBottom += (dy * 4);
            moveBottomSpinner(mTotalUnconsumedBottom);
            mBottomIsScrolling = true;
        }
    }

    // NestedScrollingChild

    /**
     * 子,设置嵌套滑动是否可用
     */
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    /**
     * 嵌套滑动是否可用
     */
    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    /**
     * 开始嵌套滑动（子）
     * axes表示方向，在ViewCompat.SCROLL_AXIS_HORIZONTAL横向
     * 还有纵向
     * <p>
     * 在这里面调用了mNestedScrollingChildHelper的startNestedScroll
     */
    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    /**
     * 子，停止嵌套滑动
     */
    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    /**
     * 是否有父view支持嵌套滑动
     */
    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    /**
     * 在处理滑动之后调用
     *
     * @param dxConsumed     x轴上 被消费的距离
     * @param dyConsumed     y轴上 被消费的距离
     * @param dxUnconsumed   x轴上 未被消费的距离
     * @param dyUnconsumed   y轴上 未被消费的距离
     * @param offsetInWindow view 的移动距离
     */
    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        //事先拦截

        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    /**
     * 一般在滑动之前调用, 在ontouch 中计算出滑动距离, 然后 调用改 方法, 就给支持的嵌套的父View 处理滑动事件
     *
     * @param dx             x 轴上滑动的距离, 相对于上一次事件, 不是相对于 down事件的 那个距离
     * @param dy             y 轴上滑动的距离
     * @param consumed       一个数组, 可以传 一个空的 数组,  表示 x 方向 或 y 方向的事件 是否有被消费
     * @param offsetInWindow 支持嵌套滑动到额父View 消费 滑动事件后 导致 本 View 的移动距离
     * @return 支持的嵌套的父View 是否处理了 滑动事件
     */
    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        Log.e("fish", "父：dispatchNestedPreScroll,dy=" + dy);
        //先拦截
        if (mBottomIsScrolling && mTotalUnconsumedBottom > 0 && dy < 0)//设置成功！但是子view还在动
        {
            Log.e("fish", "父：dispatchNestedPreScroll,mTotalUnconsumedBottom=" + mTotalUnconsumedBottom + ",dy==" + dy);
            if (-dy >= mTotalUnconsumedBottom)//向下拖动很大
            {
                moveBottomSpinner(mTotalUnconsumedBottom);
            } else {
                moveBottomSpinner(-dy);
                mTotalUnconsumedBottom -= dy;
                dy = 0;
            }
        }
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }


    /**
     * 进行实际circle的滑动绘制，包括颜色等设置
     *
     * @param overscrollTop 滑动的绝对值
     */
    private void moveBottomSpinner(float overscrollTop) {
        if (getLoadMore().getChildCount() < getLoadMore().getAdapter().getItemCount())
            getLoadMore().FsetState(overscrollTop);
    }

    private PullToRefreshRecyclerView getRefresh() {
        return ((PullToRefreshRecyclerView) getChildAt(0));
    }

    private RefreshNLoadNestedRecyclerView getLoadMore() {
        return ((RefreshNLoadNestedRecyclerView) getChildAt(0));
    }


    /**
     * 进行实际circle的滑动绘制，包括颜色等设置
     */
    private void moveSpinner(float overscrollTop) {
        getRefresh().HsetState(overscrollTop);
    }

    private void finishSpinner(float overscrollTop) {
        getRefresh().HreplyPull();
    }

    /**
     * 结束下半部的spinner
     */
    private void finishSpinnerBottom(float overscrollTop) {//在小于mTotalDragDistance的时候处理的不好，会在中间停止
        getLoadMore().FreplyPull();
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;

        Log.e("fish", "-onTouchEvent-;ACTION==" + ev.getAction());
        if (!isEnabled() || canChildScrollUp()
                || getRefresh().HmState == PullToRefreshRecyclerView.STATE_DEFAULT || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e("fish", "-onTouchEvent-;ACTION_DOWN");
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                Log.e("fish", "-onTouchEvent-;ACTION_MOVE");
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0 && getLoadMore().FmState != RefreshNLoadNestedRecyclerView.STATE_LOADING) {
                        moveSpinner(overscrollTop * 2);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                Log.e("fish", "-onTouchEvent-;MotionEventCompat.ACTION_POINTER_DOWN");
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG,
                            "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                Log.e("fish", "-onTouchEvent-;MotionEventCompat.ACTION_POINTER_UP");
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                ((PullToRefreshRecyclerView) getChildAt(0)).HreplyPull();
                Log.e("fish", "-onTouchEvent-;MotionEvent.ACTION_UP");
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged && getLoadMore().FmState != RefreshNLoadNestedRecyclerView.STATE_LOADING) {
                    final float y = ev.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
        }
    }


    /**
     * 回到顶部
     *
     * @param interpolatedTime 动画时间
     * @return 这里是根据当前位置 mFrom 回到 mOriginalOffsetTop
     */
    void moveToStart(float interpolatedTime) {

    }


    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        void onRefresh();
    }

    /**
     * 自建的下部刷新接口
     */
    public interface OnBottomRefreshListener {
        void onBottomRefresh();
    }

    /**
     * Classes that wish to override {@link SwipeRefreshLayout#canChildScrollUp()} method
     * behavior should implement this interface.
     */
    public interface OnChildScrollUpCallback {
        /**
         * Callback that will be called when {@link SwipeRefreshLayout#canChildScrollUp()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent GZoomSwifrefresh that this callback is overriding.
         * @param child  The child view of GZoomSwifrefresh.
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        boolean canChildScrollUp(RefreshNLoadNestedLayout parent, @Nullable View child);
    }
}
