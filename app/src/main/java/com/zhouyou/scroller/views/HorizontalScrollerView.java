package com.zhouyou.scroller.views;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by zhouyou on 16/8/9.
 */
public class HorizontalScrollerView extends ViewGroup {

    private static final String TAG = HorizontalScrollerView.class.getSimpleName();
    // 滑动的最小距离
    private int mTouchSlop;
    // 左边距离
    private int leftBorder;
    // 右边距离
    private int rightBorder;
    // 手指按下屏幕的x坐标
    private float mLastX;
    // 手指按下屏幕的y坐标
    private float mLastY;
    private float mLastInterceptX;
    private float mLastInterceptY;

    private int mPointerId;

    private int mMaxVelocity;
    private int mMinVelocity;

    private Scroller scroller;

    public HorizontalScrollerView(Context context) {
        this(context, null);
    }

    public HorizontalScrollerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        scroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childrenCount = getChildCount();
        for (int i = 0; i < childrenCount; i++) {
            View child = getChildAt(i);
            // 测量每一个人子控件大小
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childrenCount = getChildCount();
            for (int i = 0; i < childrenCount; i++) {
                View child = getChildAt(i);
                child.layout(i * child.getMeasuredWidth(), 0, (i + 1) * child.getMeasuredWidth(), child.getMeasuredHeight());
            }
            leftBorder = getChildAt(0).getLeft();
            rightBorder = getChildAt(childrenCount - 1).getRight();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        float mInterceptX = ev.getX();
        float mInterceptY = ev.getY();
        acquireVelocityTracker(ev);
        final VelocityTracker verTracker = mVelocityTracker;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointerId = ev.getPointerId(0);
                isIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //求伪瞬时速度
                verTracker.computeCurrentVelocity(1000, mMaxVelocity);
                final float velocityX = verTracker.getXVelocity(mPointerId);
                float deltaX = mInterceptX - mLastInterceptX;
                isIntercept = Math.abs(deltaX) > mTouchSlop || Math.abs(velocityX) > mMinVelocity;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isIntercept = false;
                releaseVelocityTracker();
                break;
            default:
                break;
        }
        mLastX = mInterceptX;
        mLastY = mInterceptY;
        mLastInterceptX = mInterceptX;
        mLastInterceptY = mInterceptY;
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float mTouchX = ev.getX();
        float mTouchY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float deltaX = mTouchX - mLastX;
                float scrollByStart = deltaX;
                if (getScrollX() - deltaX < leftBorder) {
                    scrollByStart = deltaX / 3;
                } else if (getScrollX() + getWidth() - deltaX > rightBorder) {
                    scrollByStart = deltaX / 3;
                }
                scrollBy((int) -scrollByStart, 0);
                break;
            case MotionEvent.ACTION_UP:
                // 当手指抬起时，根据当前的滚动值来判定应该滚动到哪个子控件的界面
                int targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
                if (targetIndex > getChildCount() - 1) {
                    targetIndex = getChildCount() - 1;
                } else if (targetIndex < 0) {
                    targetIndex = 0;
                }
                int dx = targetIndex * getWidth() - getScrollX();
                scroller.startScroll(getScrollX(), 0, dx, 0, 250);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        mLastX = mTouchX;
        mLastY = mTouchY;
        return super.onTouchEvent(ev);
    }

    /**
     * 向VelocityTracker添加MotionEvent
     *
     * @param event
     */
    private VelocityTracker mVelocityTracker;

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 释放VelocityTracker
     */
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }
}
