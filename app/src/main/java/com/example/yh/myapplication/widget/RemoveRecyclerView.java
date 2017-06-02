package com.example.yh.myapplication.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.yh.myapplication.R;
import com.example.yh.myapplication.base.RecylerViewHolder;
import com.example.yh.myapplication.interfaces.OnItemClickListener;
import com.example.yh.myapplication.utils.Utils;


public class RemoveRecyclerView extends RecyclerView {
    private Context mContext;

    //上一次的触摸点
    private int mLastX, mLastY;
    //当前触摸的item的位置
    private int mPosition;

    //item对应的布局
    private View mItemLayout;
    //滑动后显示的第一个按钮
    private TextView mButton1;
    //滑动后显示的第二个按钮
    private TextView mButton2;

    //最大滑动距离(即删除按钮的宽度)
    private int mMaxLength;
    //是否在垂直滑动列表
    private boolean isDragging;
    //item是在否跟随手指移动
    private boolean isItemMoving;

    //item是否开始自动滑动
    private boolean isStartScroll;
    //删除按钮状态   0：关闭 1：将要关闭 2：将要打开 3：打开
    private int mDeleteBtnState;

    //检测手指在滑动过程中的速度
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private OnItemClickListener mListener;
    private String button1Content = "button1";
    private String button2Content = "button2";

    public RemoveRecyclerView(Context context) {
        this(context, null);
    }

    public RemoveRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoveRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        mScroller = new Scroller(context, new LinearInterpolator());
        mVelocityTracker = VelocityTracker.obtain();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)  {
        mVelocityTracker.addMovement(e);

        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDeleteBtnState == 0) {
                    View view = findChildViewUnder(x, y);
                    if (view == null) {
                        return false;
                    }

                    RecylerViewHolder viewHolder = (RecylerViewHolder) getChildViewHolder(view);

                    mItemLayout = viewHolder.getView(R.id.layout);

                    mPosition = viewHolder.getAdapterPosition();

                    mButton1 = viewHolder.getTextView(R.id.button1);
                    mButton2 = viewHolder.getTextView(R.id.button2);
                    mMaxLength = viewHolder.getView(R.id.more_layout).getWidth();
                    mButton1.setText(button1Content);
                    mButton2.setText(button2Content);
                    mButton1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null)
                                try {
                                    mListener.onDeleteClick(mPosition, 0);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            mItemLayout.scrollTo(0, 0);
                            mDeleteBtnState = 0;
                        }
                    });
                    mButton2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null)
                                try {
                                    mListener.onDeleteClick(mPosition, 1);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            mItemLayout.scrollTo(0, 0);
                            mDeleteBtnState = 0;
                        }
                    });
                } else if (mDeleteBtnState == 3) {
                    mScroller.startScroll(mItemLayout.getScrollX(), 0, -mMaxLength, 0, 200);
                    invalidate();
                    mDeleteBtnState = 0;
                    return false;
                } else {
                    return false;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                int dx = mLastX - x;
                int dy = mLastY - y;
                int scrollX = mItemLayout.getScrollX();
                if (Math.abs(dx) > Math.abs(dy)) {//左边界检测
                    isItemMoving = true;
                    if (scrollX + dx <= 0) {
                        mItemLayout.scrollTo(0, 0);
                        return true;
                    } else if (scrollX + dx >= mMaxLength) {//右边界检测
                        mItemLayout.scrollTo(mMaxLength, 0);
                        return true;
                    }
                    mItemLayout.scrollBy(dx, 0);//item跟随手指滑动
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isItemMoving && !isDragging && mListener != null) {
                    mListener.onItemClick(mItemLayout, mPosition);
                }
                isItemMoving = false;

                mVelocityTracker.computeCurrentVelocity(1000);//计算手指滑动的速度
                float xVelocity = mVelocityTracker.getXVelocity();//水平方向速度（向左为负）
                float yVelocity = mVelocityTracker.getYVelocity();//垂直方向速度

                int deltaX = 0;
                int upScrollX = mItemLayout.getScrollX();
                if (Math.abs(xVelocity) > 100 && Math.abs(xVelocity) > Math.abs(yVelocity)) {
                    if (xVelocity <= -100) {//左滑速度大于100，则删除按钮显示
                        deltaX = mMaxLength - upScrollX;
                        mDeleteBtnState = 2;
                    } else if (xVelocity > 100) {//右滑速度大于100，则删除按钮隐藏
                        deltaX = -upScrollX;
                        mDeleteBtnState = 1;
                    }
                } else {
                    if (upScrollX >= mMaxLength / 2) {//item的左滑动距离大于删除按钮宽度的一半，则则显示删除按钮
                        deltaX = mMaxLength - upScrollX;
                        mDeleteBtnState = 2;
                    } else if (upScrollX < mMaxLength / 2) {//否则隐藏
                        deltaX = -upScrollX;
                        mDeleteBtnState = 1;
                    }
                }

                //item自动滑动到指定位置
                mScroller.startScroll(upScrollX, 0, deltaX, 0, 200);
                isStartScroll = true;
                invalidate();

                mVelocityTracker.clear();
                break;
        }

        mLastX = x;
        mLastY = y;
        return super.onTouchEvent(e);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mItemLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else if (isStartScroll) {
            isStartScroll = false;
            if (mDeleteBtnState == 1) {
                mDeleteBtnState = 0;
            }

            if (mDeleteBtnState == 2) {
                mDeleteBtnState = 3;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        isDragging = state == SCROLL_STATE_DRAGGING;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setButton1Content(String button1Content) {
        if (Utils.isEmpty(button1Content)) return;
        this.button1Content = button1Content;
    }

    public void setButton2Content(String button2Content) {
        if (Utils.isEmpty(button2Content)) return;
        this.button2Content = button2Content;
    }
}