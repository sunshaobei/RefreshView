package sunsh.customview.refreshview.hfrv.AutoLoad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by sunsh on 2016/9/30.
 */
public abstract class AutoLoadFooterCreator  {

    /***
     * 获得footer
     */
    public abstract View getLoadView(Context context, RecyclerView recyclerView);

    /***
     * 没有更多
     */
    public abstract View getNoMoreView(Context context,RecyclerView recyclerView);

}
