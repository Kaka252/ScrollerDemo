package com.zhouyou.scroller.views;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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
        scroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childrenCount = getChildCount();
        for (int i = 0; i < childrenCount; i++) {
            View child = getChildAt(i);
            // 测量每一个人子控件大小
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childrenCount = getChildCount();
            for (int i = 0; i < childrenCount; i++) {
                View child = getChildAt(i);
                child.layout(i * getMeasuredWidth(), 0, i * getMeasuredWidth() + child.getMeasuredWidth(), child.getMeasuredHeight());
            }
            leftBorder = 0;
            rightBorder = childrenCount * getMeasuredWidth();
        }
    }

    private float mLastX;
    private float mLastY;
    private float mLastInterceptX;
    private float mLastInterceptY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        float mInterceptX = ev.getX();
        float mInterceptY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mInterceptX - mLastInterceptX;
                float deltaY = mInterceptY - mLastInterceptY;
                isIntercept = Math.abs(deltaX) - Math.abs(deltaY) > 0 && Math.abs(deltaX) > mTouchSlop;
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;
                break;
            default:
                break;
        }
        Log.d(TAG, "isIntercept: " + isIntercept);
        mLastX = mInterceptX;
        mLastY = mInterceptY;
        mLastInterceptX = mInterceptX;
        mLastInterceptY = mInterceptY;
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float mTouchX = event.getX();
        float mTouchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                float deltaX = mTouchX - mLastX;
                float scrollByStart = deltaX;
                if (getScrollX() - deltaX < leftBorder) {
                    scrollByStart = getScrollX() - leftBorder;
                } else if (getScrollX() + getWidth() - deltaX > rightBorder) {
                    scrollByStart = rightBorder - getWidth() - getScrollX();
                }
                scrollBy((int) -scrollByStart, 0);
                break;
            case MotionEvent.ACTION_UP:
                // 当手指抬起时，根据当前的滚动值来判定应该滚动到哪个子控件的界面
                int targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
                int dx = targetIndex * getWidth() - getScrollX();
                scroller.startScroll(getScrollX(), 0, dx, 0, 500);
                invalidate();
                break;
            default:
                break;
        }
        mLastX = mTouchX;
        mLastY = mTouchY;
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }
}
