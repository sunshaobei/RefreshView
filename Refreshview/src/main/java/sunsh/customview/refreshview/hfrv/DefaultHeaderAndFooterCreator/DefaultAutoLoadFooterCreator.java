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
import sunsh.customview.refreshview.hfrv.AutoLoad.AutoLoadFooterCreator;


/**
 * Created by sunsh on 2016/9/30.
 */
public class DefaultAutoLoadFooterCreator extends AutoLoadFooterCreator {

    protected View mAutoLoadFooter;
    protected ImageView iv;
    protected ValueAnimator ivAnim;
    private int loadingDuration = 1000;
    protected View mNoMoreView;

    @Override
    public View getLoadView(Context context, RecyclerView recyclerView) {
        if (mAutoLoadFooter == null) {
            mAutoLoadFooter = LayoutInflater.from(context).inflate(R.layout.layout_auto_load_footer,recyclerView,false);
            iv = (ImageView) mAutoLoadFooter.findViewById(R.id.iv);
            startLoadingAnim();
        }
        return mAutoLoadFooter;
    }

    @Override
    public View getNoMoreView(Context context, RecyclerView recyclerView) {
        if (mNoMoreView == null) {
            mNoMoreView = LayoutInflater.from(context).inflate(R.layout.hfrv_nomoreitem, recyclerView, false);
//            mNoMoreView.findViewById(R.id.iv).setVisibility(View.GONE);
//            ((TextView)mNoMoreView.findViewById(R.id.tv)).setText("没有更多了哦");
        }
        return mNoMoreView;
    }

    private void startLoadingAnim() {
        if (ivAnim != null) {
            ivAnim.cancel();
        }
        ivAnim = ObjectAnimator.ofFloat(0, 360).setDuration(loadingDuration);
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
