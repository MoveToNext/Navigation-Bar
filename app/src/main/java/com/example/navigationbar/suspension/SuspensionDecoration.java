package com.example.navigationbar.suspension;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.example.navigationbar.CityBean;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * PackageName:  com.example.navigationbar
 * Description:
 * Created by :   Liu
 * date:         2017/10/9 下午5:43
 * </pre>
 */
public class SuspensionDecoration extends RecyclerView.ItemDecoration {
    private List<? extends ISuspensionInterface> mDatas;
    private Paint mPaint;
    private Rect mBounds;//用于存放测量文字Rect
    private int mTitleHeight;//title的高
    private static int mTitleFontSize;//title字体大小
    private final LayoutInflater mLyoutInflater;
    private int mHeaderViewCount = 0;
    private static int COLOR_TITLE_BG = Color.parseColor("#FFDFDFDF");
    private static int COLOR_TITLE_FONT = Color.parseColor("#FF999999");

    public SuspensionDecoration(Context context, List<? extends ISuspensionInterface> datas) {
        mDatas = datas;
        mPaint = new Paint();
        mBounds = new Rect();
        Log.d("SuspensionDecoration", "datas.size():" + datas.size());

        mTitleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        mTitleFontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics());
        mPaint.setTextSize(mTitleFontSize);
        mPaint.setAntiAlias(true);
        mLyoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int position = params.getViewLayoutPosition();
            position -= getHeaderViewCount();
            //pos为1，size为1，1>0? true
            if (mDatas == null || mDatas.isEmpty() || position > mDatas.size() - 1 || position < 0 || !mDatas.get(position).isShowSuspension()) {
                continue;//越界
            }

            if (position > -1) {
                if (position == 0) {
                    drawTitleArea(c, left, right, child, params, position);
                }else {
                    if (null != mDatas.get(position).getSuspensionTag() && !mDatas.get(position).getSuspensionTag().equals(mDatas.get(position - 1).getSuspensionTag())) {
                        //不为空 且跟前一个tag不一样了，说明是新的分类，也要title
                        drawTitleArea(c, left, right, child, params, position);
                    }
                }
            }
        }
    }

    private void drawTitleArea(Canvas c, int left, int right, View child, RecyclerView.LayoutParams params, int position) {
        mPaint.setColor(COLOR_TITLE_BG);
        c.drawRect(left, child.getTop() - params.topMargin - mTitleHeight, right, child.getTop() - params.topMargin, mPaint);
        mPaint.setColor(COLOR_TITLE_FONT);

        mPaint.getTextBounds(mDatas.get(position).getSuspensionTag(),0,mDatas.get(position).getSuspensionTag().length(), mBounds);
        c.drawText(mDatas.get(position).getSuspensionTag(), child.getPaddingLeft(), child.getTop() - params.topMargin - (mTitleHeight / 2 - mBounds.height() / 2), mPaint);
    }


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int pos = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        pos -= getHeaderViewCount();
        //pos为1，size为1，1>0? true
        if (mDatas == null || mDatas.isEmpty() || pos > mDatas.size() - 1 || pos < 0 || !mDatas.get(pos).isShowSuspension()) {
            return;//越界
        }

        String tag = mDatas.get(pos).getSuspensionTag();
        RecyclerView.ViewHolder viewHolderForLayoutPosition = parent.findViewHolderForLayoutPosition(pos);
        View child = viewHolderForLayoutPosition.itemView;

        boolean flag = false;//Canvas 是否移动过
        if (null != tag && !tag.equals(mDatas.get(pos + 1).getSuspensionTag())) {
//            if (child.getHeight() + child.getTop() < mTitleHeight) {
            if (child.getBottom() < mTitleHeight) {
                flag = true;
                c.save();
                //一种头部折叠起来的视效，个人觉得也还不错~
                //可与123行 c.drawRect 比较，只有bottom参数不一样，由于 child.getHeight() + child.getTop() < mTitleHeight，所以绘制区域是在不断的减小，有种折叠起来的感觉
                c.clipRect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getRight() - parent.getPaddingRight(), parent.getPaddingTop() + child.getHeight() + child.getTop());

                //类似饿了么点餐时,商品列表的悬停头部切换“动画效果”
                //上滑时，将canvas上移 （y为负数） ,所以后面canvas 画出来的Rect和Text都上移了，有种切换的“动画”感觉
//                c.translate(0, child.getHeight() + child.getTop() - mTitleHeight);
            }
        }
        mPaint.setColor(COLOR_TITLE_BG);
        c.drawRect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getRight() - parent.getPaddingRight(), parent.getPaddingTop() + mTitleHeight, mPaint);
        mPaint.setColor(COLOR_TITLE_FONT);
        mPaint.getTextBounds(tag, 0, tag.length(), mBounds);
        c.drawText(tag, child.getPaddingLeft(),
                parent.getPaddingTop() + mTitleHeight - (mTitleHeight / 2 - mBounds.height() / 2),
                mPaint);
        if (flag)
            c.restore();//恢复画布到之前保存的状态
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        position -= getHeaderViewCount();

        if (mDatas == null || mDatas.isEmpty() || position > mDatas.size() - 1) {//pos为1，size为1，1>0? true
            return;//越界
        }
        if (position > -1) {
            ISuspensionInterface titleCategoryInterface = mDatas.get(position);
            if (titleCategoryInterface.isShowSuspension()) {
                if (position == 0) {
                    outRect.set(0, mTitleHeight, 0, 0);
                }else {
                    if (null != titleCategoryInterface.getSuspensionTag() && !titleCategoryInterface.getSuspensionTag().equals(mDatas.get(position - 1).getSuspensionTag())) {
                        //不为空 且跟前一个tag不一样了，说明是新的分类，也要title
                        outRect.set(0, mTitleHeight, 0, 0);
                    }
                }
            }
        }
    }

    private int getHeaderViewCount() {
        return mHeaderViewCount;
    }

    public SuspensionDecoration setHeaderViewCount(int headerViewCount) {
        mHeaderViewCount = headerViewCount;
        return this;
    }

    public void setmDatas(ArrayList<CityBean> mDatas) {

    }
}
