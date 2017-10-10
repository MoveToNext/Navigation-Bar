package com.example.navigationbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.navigationbar.bean.BaseIndexPinyinBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * PackageName:  com.example.navigationbar
 * Description:  自定义导航view
 * Created by :   Liu
 * date:         2017/10/9 下午3:44
 * </pre>
 */
public class NavigationBar extends View {

    private int mPressedBackground;
    //#在最后面（默认的数据源）
    public static String[] INDEX_STRING = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    //是否需要根据实际的数据来生成索引数据源（例如 只有 A B C 三种tag，那么索引栏就 A B C 三项）
    private boolean isNeedRealIndex;
    //索引数据源
    private List<String> mIndexDatas;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private int mGapHeight;
    private double currentIndex;

    private TextView mPressedShowTextView;//用于特写显示正在被触摸的index值
    private boolean isSourceDatasAlreadySorted;//源数据 已经有序？
    private List<? extends BaseIndexPinyinBean> mSourceDatas;//Adapter的数据源
    private LinearLayoutManager mLayoutManager;

    private int mHeaderViewCount = 0;
    private IndexBarDataHelperImpl mDataHelper;

    public NavigationBar(Context context) {
        this(context, null);
    }

    public NavigationBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        mPressedBackground = Color.BLACK;//默认按下是纯黑色

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationBar, defStyleAttr, 0);
        int indexCount = typedArray.getIndexCount();
        //获取自定义属性
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            switch (index) {
                case R.styleable.NavigationBar_indexBarTextSize:
                    textSize = typedArray.getDimensionPixelSize(index,textSize);
                    break;
                case R.styleable.NavigationBar_indexBarPressBackground:
                    mPressedBackground = typedArray.getColor(index,mPressedBackground);
                    break;
            }
        }

        typedArray.recycle();
        initDatas();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setColor(Color.BLACK);

        //设置index触摸监听器
        setmOnIndexPressedListener(new onIndexPressedListener() {
            @Override
            public void onIndexPressed(int index, String text) {
                if (mPressedShowTextView != null) { //显示hintTexView
                    mPressedShowTextView.setVisibility(View.VISIBLE);
                    mPressedShowTextView.setText(text);
                }
                //滑动Rv
                if (mLayoutManager != null) {
                    int position = getPosByTag(text);
                    if (position != -1) {
                        mLayoutManager.scrollToPositionWithOffset(position, 0);
                    }
                }
            }

            @Override
            public void onMotionEventEnd() {
                //隐藏hintTextView
                if (mPressedShowTextView != null) {
                    mPressedShowTextView.setVisibility(View.GONE);
                }
            }
        });

        mDataHelper = new IndexBarDataHelperImpl();
    }

    public NavigationBar setmSourceDatas(List<? extends BaseIndexPinyinBean> mSourceDatas) {
        this.mSourceDatas = mSourceDatas;
        initSourceDatas();//对数据源进行初始化
        return this;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        if (null == mIndexDatas || mIndexDatas.isEmpty()) {
            return;
        }
        computeGapHeight();
    }

    private void computeGapHeight() {
        mGapHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / mIndexDatas.size();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureWidth = 0, measureHeight = 0;//最终测量出来的宽高

        Rect rect = new Rect();//存放每个绘制的index的Rect区域
        for (int i = 0; i < mIndexDatas.size(); i++) {
            String s = mIndexDatas.get(i);
            mPaint.getTextBounds(s, 0, s.length(), rect);
            measureWidth = Math.max(rect.width(), measureWidth);
            measureHeight = Math.max(rect.height(), measureHeight);
        }
        measureHeight *= mIndexDatas.size();

        switch (wMode) {
            case MeasureSpec.AT_MOST:
                measureWidth = Math.min(measureWidth, wSize);
                break;
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                measureWidth = wSize;
                break;
        }

        switch (hMode) {
            case MeasureSpec.AT_MOST:
                measureHeight = Math.min(measureHeight, hSize);
                break;
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                measureHeight = hSize;
                break;
        }

        measureWidth += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int paddingTop = getPaddingTop();

        String string;
        for (int i = 0; i < mIndexDatas.size(); i++) {
            string = mIndexDatas.get(i);
            if (i == currentIndex) {
                mPaint.setColor(Color.RED);
            }else {
                mPaint.setColor(Color.BLUE);
            }

            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            int baseline = (int) ((mGapHeight - fontMetrics.bottom - fontMetrics.top) / 2);

            canvas.drawText(string, mWidth / 2 - mPaint.measureText(string) / 2, paddingTop + mGapHeight * i + baseline, mPaint);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundColor(mPressedBackground);
                break;
            case MotionEvent.ACTION_MOVE:
                int pressI = (int) ((event.getY() - getPaddingTop()) / mGapHeight);
                //边界处理（在手指move时，有可能已经移出边界，防止越界）
                if (pressI < 0) {
                    pressI = 0;
                } else if (pressI >= mIndexDatas.size()) {
                    pressI = mIndexDatas.size() - 1;
                }
                currentIndex = pressI;
                invalidate();
                //回调监听器
                if (null != mOnIndexPressedListener && pressI > -1 && pressI < mIndexDatas.size()) {
                    mOnIndexPressedListener.onIndexPressed(pressI, mIndexDatas.get(pressI));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setBackgroundResource(android.R.color.transparent);//手指抬起时背景恢复透明
                //回调监听器
                if (null != mOnIndexPressedListener) {
                    mOnIndexPressedListener.onMotionEventEnd();
                }
                break;
        }

        return true;
    }


    /**
     * 当前被按下的index的监听器
     */
    public interface onIndexPressedListener {
        void onIndexPressed(int index, String text);//当某个Index被按下

        void onMotionEventEnd();//当触摸事件结束（UP CANCEL）
    }
    private onIndexPressedListener mOnIndexPressedListener;
    public void setmOnIndexPressedListener(onIndexPressedListener mOnIndexPressedListener) {
        this.mOnIndexPressedListener = mOnIndexPressedListener;
    }
    private void initDatas() {
        if (isNeedRealIndex) {
            mIndexDatas = new ArrayList<>();
        }else {
            mIndexDatas = Arrays.asList(INDEX_STRING);
        }
    }


    /**
     * 初始化原始数据源，并取出索引数据源
     *
     * @return
     */
    private void initSourceDatas() {
        //add by zhangxutong 2016 09 08 :解决源数据为空 或者size为0的情况,
        if (null == mSourceDatas || mSourceDatas.isEmpty()) {
            return;
        }
        if (!isSourceDatasAlreadySorted) {
            //排序sourceDatas
            mDataHelper.sortSourceDatas(mSourceDatas);
        } else {
            //汉语->拼音
            mDataHelper.convert(mSourceDatas);
            //拼音->tag
            mDataHelper.fillInexTag(mSourceDatas);
        }
        if (isNeedRealIndex) {
            mDataHelper.getSortedIndexDatas(mSourceDatas, mIndexDatas);
            computeGapHeight();
        }
        //sortData();
    }

    /**
     * 显示当前被按下的index的TextView
     *
     * @return
     */

    public NavigationBar setmPressedShowTextView(TextView mPressedShowTextView) {
        this.mPressedShowTextView = mPressedShowTextView;
        return this;
    }

    /**
     * 一定要在设置数据源{@link #setmSourceDatas(List)}之前调用
     *
     * @param needRealIndex
     * @return
     */
    public NavigationBar setNeedRealIndex(boolean needRealIndex) {
        isNeedRealIndex = needRealIndex;
        initDatas();
        return this;
    }


    public NavigationBar setmLayoutManager(LinearLayoutManager mLayoutManager) {
        this.mLayoutManager = mLayoutManager;
        return this;
    }

    /**
     * 根据传入的pos返回tag
     *
     * @param tag
     * @return
     */
    private int getPosByTag(String tag) {
        //add by zhangxutong 2016 09 08 :解决源数据为空 或者size为0的情况,
        if (null == mSourceDatas || mSourceDatas.isEmpty()) {
            return -1;
        }
        if (TextUtils.isEmpty(tag)) {
            return -1;
        }
        for (int i = 0; i < mSourceDatas.size(); i++) {
            if (tag.equals(mSourceDatas.get(i).getBaseIndexTag())) {
                return i + getHeaderViewCount();
            }
        }
        return -1;
    }

    public int getHeaderViewCount() {
        return mHeaderViewCount;
    }
}
