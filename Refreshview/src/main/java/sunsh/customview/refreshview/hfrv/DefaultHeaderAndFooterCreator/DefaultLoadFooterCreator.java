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

import sunsh.customview.refreshview.R;
import sunsh.customview.refreshview.RefreshNLoadRecyclerView;
import sunsh.customview.refreshview.hfrv.PullToLoad.LoadFooterCreator;


/**
 * Created by sunsh on 2016/9/28.
 */
public class DefaultLoadFooterCreator extends LoadFooterCreator {

    private View mLoadView;
    private ImageView iv;
    private TextView tv;


    private int rotationDuration = 200;

    private int loadingDuration = 1000;
    private ValueAnimator ivAnim;

    private View mNoMoreView;


    @Override
    public boolean onStartPull(float distance, int lastState) {
        if (lastState == RefreshNLoadRecyclerView.STATE_DEFAULT) {
            iv.setImageResource(R.drawable.rl_arrow_down);
            iv.setRotation(-180f);
            tv.setText("上拉加载");
        } else if (lastState == RefreshNLoadRecyclerView.STATE_RELEASE_TO_LOAD) {
            startArrowAnim(-180f);
            tv.setText("上拉加载");
        }
        return true;
    }

    @Override
    public boolean onReleaseToLoad(float distance, int lastState) {
        if (lastState == RefreshNLoadRecyclerView.STATE_DEFAULT ) {
            iv.setImageResource(R.drawable.rl_arrow_down);
            iv.setRotation(0f);
            tv.setText("松手立即加载");
        } else if (lastState == RefreshNLoadRecyclerView.STATE_PULLING) {
            startArrowAnim(0f);
            tv.setText("松手立即加载");
        }
        return true;
    }

    @Override
    public void onStartLoading() {
        iv.setImageResource(R.drawable.rl_loading);
        startLoadingAnim();
        tv.setText("正在加载...");
    }

    @Override
    public void onStopLoad() {
        if (ivAnim != null) {
            ivAnim.cancel();
        }
    }

    @Override
    public View getLoadView(Context context, RecyclerView recyclerView) {
        if (mLoadView == null) {
            mLoadView = LayoutInflater.from(context).inflate(R.layout.layout_ptr_ptl,recyclerView,false);
            iv = (ImageView) mLoadView.findViewById(R.id.iv);
            tv = (TextView) mLoadView.findViewById(R.id.tv);
        }
        return mLoadView;
    }


    @Override
    public View getNoMoreView(Context context, RecyclerView recyclerView) {
        if (mNoMoreView == null) {
            mNoMoreView = LayoutInflater.from(context).inflate(R.layout.hfrv_nomoreitem,recyclerView,false);
//            mNoMoreView.findViewById(R.id.iv).setVisibility(View.GONE);
//            ((TextView) mNoMoreView.findViewById(R.id.tv)).setText("没有更多了哦");
        }
        return mNoMoreView;
    }


    private void startArrowAnim(float roration) {
        if (ivAnim != null) {
            ivAnim.cancel();
        }
        float startRotation = iv.getRotation();
        ivAnim = ObjectAnimator.ofFloat(startRotation, roration).setDuration(rotationDuration);
        ivAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iv.setRotation((Float) animation.getAnimatedValue());
            }
        });
        ivAnim.start();
    }

    private void startLoadingAnim() {
        if (ivAnim != null) {
            ivAnim.cancel();
        }
        ivAnim = ObjectAnimator.ofFloat(0,360).setDuration(loadingDuration);
        ivAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iv.setRotation((Float) animation.getAnimatedValue());
            }
        });
        ivAnim.setRepeatMode(ObjectAnimator.RESTART);
        ivAnim.setRepeatCount(ObjectAnimator.INFINITE);
        ivAnim.setInterpolator(new LinearInterpolator());
        ivAnim.start();
    }

}
