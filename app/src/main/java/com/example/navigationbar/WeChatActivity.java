package com.example.navigationbar;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.navigationbar.suspension.SuspensionDecoration;

import java.util.ArrayList;

/**
 * <pre>
 * PackageName:  com.example.navigationbar
 * Description:
 * Created by :   Liu
 * date:         2017/10/10 下午1:18
 * </pre>
 */
public class WeChatActivity extends AppCompatActivity {

    private RecyclerView mRv;
    private CityAdapter mAdapter;
    private LinearLayoutManager mManager;
    private SuspensionDecoration mDecoration;
    private ArrayList<CityBean> mDatas = new ArrayList<>();
    private static final String INDEX_STRING_TOP = "↑";
    private NavigationBar mIndexBar;
    private TextView mTvSideBarHint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRv = (RecyclerView) findViewById(R.id.rv);
        mRv.setLayoutManager(mManager = new LinearLayoutManager(this));

        mAdapter = new CityAdapter(this, mDatas);
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(mDecoration = new SuspensionDecoration(this, mDatas));
        //如果add两个，那么按照先后顺序，依次渲染。
        mRv.addItemDecoration(new DividerItemDecoration(WeChatActivity.this,DividerItemDecoration.VERTICAL));

        //使用indexBar
        mTvSideBarHint = (TextView) findViewById(R.id.tvSideBarHint);//HintTextView
        mIndexBar = (NavigationBar) findViewById(R.id.indexBar);//IndexBar
        //indexbar初始化
        mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
                .setNeedRealIndex(true)//设置需要真实的索引
                .setmLayoutManager(mManager);//设置RecyclerView的LayoutManager
        //模拟线上加载数据
        initDatas(getResources().getStringArray(R.array.provinces));
    }

    private void initDatas(final String[] data) {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                //微信的头部 也是可以右侧IndexBar导航索引的，
                // 但是它不需要被ItemDecoration设一个标题titile
                mDatas.add((CityBean) new CityBean("新的朋友").setTop(true).setBaseIndexTag(INDEX_STRING_TOP));
                mDatas.add((CityBean) new CityBean("群聊").setTop(true).setBaseIndexTag(INDEX_STRING_TOP));
                mDatas.add((CityBean) new CityBean("标签").setTop(true).setBaseIndexTag(INDEX_STRING_TOP));
                mDatas.add((CityBean) new CityBean("公众号").setTop(true).setBaseIndexTag(INDEX_STRING_TOP));
                for (int i = 0; i < data.length; i++) {
                    CityBean cityBean = new CityBean();
                    cityBean.setCity(data[i]);//设置城市名称
                    mDatas.add(cityBean);
                }
                mAdapter.setDatas(mDatas);

                mIndexBar.setmSourceDatas(mDatas)//设置数据
                        .invalidate();
                mAdapter.notifyDataSetChanged();
            }
        },300);
    }


}
