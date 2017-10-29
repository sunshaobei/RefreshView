## RefreshNLoadMoreNestedLayout
**基本使用**
###在xml中
``` python
 
    <android.support.design.widget.CoordinatorLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <android.support.design.widget.AppBarLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <android.support.design.widget.CollapsingToolbarLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:minHeight="0dp"
                app:expandedTitleMarginEnd="0dp"
                app:expandedTitleMarginStart="0dp"
                app:layout_scrollFlags="scroll">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="#ffffff"
                    android:gravity="center"
                    android:text="测试"
                    android:textSize="25dp" />
            </android.support.design.widget.CollapsingToolbarLayout>

            <TextView
                android:layout_width="match_parent"
                android:layout_height="40dp"
                android:background="#f5f566" />
        </android.support.design.widget.AppBarLayout>

        <sunsh.customview.refreshview.hfrvnested.RefreshNLoadNestedLayout
            android:id="@+id/refresh_loadmore_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_behavior="@string/appbar_scrolling_view_behavior"/>



    </android.support.design.widget.CoordinatorLayout>

```
###在activity中
``` python
class Main2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        startActivity(Intent(this,Main22Activity::class.java))
        val recyclerView = refresh_loadmore_layout.basseRV
        recyclerView.layoutManager = PTLLinearLayoutManager()
        val strings = ArrayList<String>()
        for (i in 0..4) {
            strings.add(i.toString() + "")
        }
        recyclerView.adapter = MyAdapter(this, strings, R.layout.item)
        recyclerView.setOnRefreshListener {
            recyclerView.postDelayed({
                recyclerView.completeRefresh()
                val textView = TextView(this@Main2Activity)
                textView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80)
                textView.setBackgroundColor(Color.parseColor("#ff0000"))
                textView.text = "加一个头部"
                textView.gravity = Gravity.CENTER
                recyclerView.addHeaderView(textView)
            }, 3000)
        }

        recyclerView.setOnLoadListener {
            recyclerView.postDelayed({
                recyclerView.completeLoad()
                val textView = TextView(this@Main2Activity)
                textView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80)
                textView.setBackgroundColor(Color.parseColor("#ff0000"))
                textView.text = "加一个尾部"
                textView.gravity = Gravity.CENTER
                recyclerView.addFooterView(textView)
            }, 3000)
        }
    }

    internal inner class MyAdapter(context: Context, datas: ArrayList<String>, layoutId: Int) : SimpleAdapter<String>(context, datas, layoutId) {

        override fun onBindViewHolder(holder: ViewHolder, data: String) {
            val view = holder.getView<TextView>(R.id.tv)
            view.text = data
        }
    }
}
```

##效果
- **下拉**
![这里写图片描述](http://img.blog.csdn.net/20171030034014320?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzU5NTkyMzE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- **上拉**
![这里写图片描述](http://img.blog.csdn.net/20171030033935548?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzU5NTkyMzE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

###1.补充 没有更多数据时 setNoMore(true) ,注意在刷新时恢复setNoMore(false) （我内部没有封装调用刷新接口恢复）
###2.由于是在nested框架中使用，记得在该控件或者直接父控件上加上layout_behavior属性（不懂的请自学再来使用）否则会乱。

## RefreshNLoadMoreRecyclerView
**基本使用**

###在xml中
``` python
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main22"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.jimi_wu.ptlrecyclerviewsample.Main22Activity">

    <sunsh.customview.refreshview.RefreshNLoadRecyclerView
        android:id="@+id/refresh_loadmore_recycler"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </sunsh.customview.refreshview.RefreshNLoadRecyclerView>

</RelativeLayout>
```
###在activity中 使用基本与RefreshNLoadMoreNestedLayout一致，只是不需要在获取一个recyclerview RefreshNLoadMoreRecyclerView本身就是个recyclerview
``` python
class Main22Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main22)
        val strings = ArrayList<String>()
        for (i in 0..14) {
            strings.add(i.toString() + "")
        }
        refresh_loadmore_recycler.adapter = MyAdapter(this,strings,R.layout.item)
        refresh_loadmore_recycler.layoutManager = PTLLinearLayoutManager()
        refresh_loadmore_recycler.setOnRefreshListener {
            refresh_loadmore_recycler.postDelayed({
                refresh_loadmore_recycler.completeRefresh()

                val textView = TextView(this@Main22Activity)
                textView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80)
                textView.setBackgroundColor(Color.parseColor("#ff0000"))
                textView.text = "加一个头部"
                textView.gravity = Gravity.CENTER
                refresh_loadmore_recycler.addHeaderView(textView)
            }, 3000)
        }

        refresh_loadmore_recycler.setOnLoadListener {
            refresh_loadmore_recycler.postDelayed({
                strings.add("+1")
                strings.add("+2")
                strings.add("+3")
                refresh_loadmore_recycler.completeLoad()
            }, 3000)
        }
    }
    internal inner class MyAdapter(context: Context, datas: ArrayList<String>, layoutId: Int) : SimpleAdapter<String>(context, datas, layoutId) {

        override fun onBindViewHolder(holder: ViewHolder, data: String) {
            val view = holder.getView<TextView>(R.id.tv)
            view.text = data
        }
    }
}
```

##效果
- **下拉**
![这里写图片描述](http://img.blog.csdn.net/20171030034051552?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzU5NTkyMzE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- **上拉**
![这里写图片描述](http://img.blog.csdn.net/20171030033935548?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzU5NTkyMzE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


###1）.封装包中除了这两个还有其他几个使用基本类似不做一一介绍，RefreshNAutoLoadMoreNestedLayout、RefreshNAutoLoadMoreRecyclerView以及修改系统SwipeRefreshLayout 支持上拉效果的SwipeRefreshLayout（上拉效果与下拉一致）
###2）新增加的四个刷新控件均支持自定义上下拉样式setRefreshViewCreator（RefreshHeaderCreator），setLoadViewCreator（LoadFooterCreator），具体使用如下
``` python
/**
 * Created by sunsh on 2017/9/28.
 */
public class DefaultRefreshHeaderCreator extends RefreshHeaderCreator {

    private View mRefreshView;
    private ImageView iv;
    private TextView tv;

    private int rotationDuration = 200;

    private int loadingDuration = 1000;
    private ValueAnimator ivAnim;


    @Override
    public boolean onStartPull(float distance,int lastState) {
        if (lastState == PullToRefreshRecyclerView.STATE_DEFAULT ) {
            iv.setImageResource(R.drawable.arrow_down);
            iv.setRotation(0f);
            tv.setText("下拉刷新");
        } else if (lastState == PullToRefreshRecyclerView.STATE_RELEASE_TO_REFRESH) {
            startArrowAnim(0);
            tv.setText("下拉刷新");
        }
        return true;
    }

    @Override
    public void onStopRefresh() {
        if (ivAnim != null) {
            ivAnim.cancel();
        }
    }


    @Override
    public boolean onReleaseToRefresh(float distance,int lastState) {
        if (lastState == PullToRefreshRecyclerView.STATE_DEFAULT ) {
            iv.setImageResource(R.drawable.arrow_down);
            iv.setRotation(-180f);
            tv.setText("松手立即刷新");
        } else if (lastState == PullToRefreshRecyclerView.STATE_PULLING) {
            startArrowAnim(-180f);
            tv.setText("松手立即刷新");
        }
        return true;
    }

    @Override
    public void onStartRefreshing() {
        iv.setImageResource(R.drawable.loading);
        startLoadingAnim();
        tv.setText("正在刷新...");
    }

    @Override
    public View getRefreshView(Context context, RecyclerView recyclerView) {
        mRefreshView = LayoutInflater.from(context).inflate(R.layout.layout_ptr_ptl,recyclerView,false);
        iv = (ImageView) mRefreshView.findViewById(R.id.iv);
        tv = (TextView) mRefreshView.findViewById(R.id.tv);
        return mRefreshView;
    }

    private void startArrowAnim(float roration) {
        if (ivAnim != null) {
            ivAnim.cancel();
        }
        float startRotation = iv.getRotation();
        ivAnim = ObjectAnimator.ofFloat(startRotation,roration).setDuration(rotationDuration);
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
```
**上拉样式与下拉一样继承LoadFooterCreator，不列出了**

##还不会做gif，先凑合静态图吧，等我学会了再配gif上来吧。

##AndroidStudio 配置Project Gradle添加jitpack仓库
``` python
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```
在项目Module Gradle 中添加依赖
``` python
compile 'com.github.sunshaobei:Refreshview:3.9'
```

- **谢谢观看，不令赐教**
